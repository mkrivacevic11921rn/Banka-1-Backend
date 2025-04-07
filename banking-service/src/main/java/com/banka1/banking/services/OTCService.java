package com.banka1.banking.services;

import com.banka1.banking.dto.OTCTransactionACKDTO;
import com.banka1.banking.models.Account;
import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.saga.OTCTransaction;
import com.banka1.banking.saga.OTCTransactionStage;
import com.banka1.common.listener.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OTCService {
    private final JmsTemplate jmsTemplate;
    private final MessageHelper messageHelper;
    private final String destinationOtcAck;
    private final Map<String, OTCTransaction> activeTransactions = new HashMap<>();
    private final AccountRepository accountRepository;

    public void rollback(String uid) {
        OTCTransaction transaction = activeTransactions.get(uid);
        if(transaction == null)
            return;

        switch (transaction.getStage()) {
            case INITIALIZED -> activeTransactions.remove(uid);
            case ASSETS_RESERVED -> {
                Account buyerAccount = accountRepository.findById(transaction.getBuyerAccountId()).orElseThrow();
                buyerAccount.setBalance(buyerAccount.getBalance() + transaction.getAmount());

                accountRepository.save(buyerAccount);
                activeTransactions.remove(uid);
            }
            case ASSETS_TRANSFERED, FINISHED -> {
                Account sellerAccount = accountRepository.findById(transaction.getSellerAccountId()).orElseThrow();
                Account buyerAccount = accountRepository.findById(transaction.getBuyerAccountId()).orElseThrow();

                buyerAccount.setBalance(buyerAccount.getBalance() + transaction.getAmount());
                sellerAccount.setBalance(sellerAccount.getBalance() - transaction.getAmount());

                accountRepository.save(buyerAccount);
                accountRepository.save(sellerAccount);
                activeTransactions.remove(uid);
            }
        }
    }

    public void proceed(String uid) {
        try {
            OTCTransaction transaction = activeTransactions.get(uid);
            if(transaction == null)
                throw new Exception("Invalid UID");

            switch (transaction.getStage()) {
                case INITIALIZED -> {
                    Account account = accountRepository.findById(transaction.getBuyerAccountId()).orElseThrow();

                    if(account.getBalance() < transaction.getAmount())
                        throw new Exception("Insufficient funds");

                    account.setBalance(account.getBalance() - transaction.getAmount());
                    accountRepository.save(account);

                    jmsTemplate.convertAndSend(destinationOtcAck, messageHelper.createTextMessage(new OTCTransactionACKDTO(
                            uid, false, ""
                    )));

                    transaction.setStage(OTCTransactionStage.ASSETS_RESERVED);
                }
                case ASSETS_RESERVED -> {
                    Account account = accountRepository.findById(transaction.getSellerAccountId()).orElseThrow();
                    account.setBalance(account.getBalance() + transaction.getAmount());
                    accountRepository.save(account);

                    jmsTemplate.convertAndSend(destinationOtcAck, messageHelper.createTextMessage(new OTCTransactionACKDTO(
                            uid, false, ""
                    )));

                    transaction.setStage(OTCTransactionStage.ASSETS_TRANSFERED);
                }
                case ASSETS_TRANSFERED -> {
                    transaction.setStage(OTCTransactionStage.FINISHED);

                    // TODO: provera konzistentnog stanja?

                    jmsTemplate.convertAndSend(destinationOtcAck, messageHelper.createTextMessage(new OTCTransactionACKDTO(
                            uid, false, ""
                    )));
                }
                case FINISHED -> activeTransactions.remove(uid);
            }
        } catch(Exception e) {
            jmsTemplate.convertAndSend(destinationOtcAck, messageHelper.createTextMessage(new OTCTransactionACKDTO(
                    uid, true, e.getMessage()
            )));
            rollback(uid);
        }
    }

    public void initiate(String uid, OTCTransaction transaction) {
        try {
            if(activeTransactions.containsKey(uid))
                throw new Exception("Invalid UID");

            Account sellerAccount = accountRepository.findById(transaction.getSellerAccountId()).orElseThrow();
            Account buyerAccount = accountRepository.findById(transaction.getBuyerAccountId()).orElseThrow();

            if(sellerAccount.getCurrencyType() != buyerAccount.getCurrencyType())
                throw new Exception("Currency type mismatch");

            activeTransactions.put(uid, transaction);
        } catch (Exception e) {
            jmsTemplate.convertAndSend(destinationOtcAck, messageHelper.createTextMessage(new OTCTransactionACKDTO(
                    uid, true, e.getMessage()
            )));
        }
    }
}
