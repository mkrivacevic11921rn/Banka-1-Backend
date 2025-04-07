package com.banka1.banking.services;

import com.banka1.banking.dto.OTCTransactionACKDTO;
import com.banka1.banking.models.Account;
import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.saga.OTCTransaction;
import com.banka1.common.listener.MessageHelper;
import jakarta.jms.JMSException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OTCService {
    private final JmsTemplate jmsTemplate;
    private final MessageHelper messageHelper;
    @Value("${destination.otc.ack.trade}")
    private final String destinationOtcAck;
    private final Map<String, OTCTransaction> activeTransactions = new HashMap<>();
    private final AccountRepository accountRepository;
    private final TaskScheduler taskScheduler;

    private void sendFailureMessage(String uid, String message) throws JMSException {
        jmsTemplate.convertAndSend(destinationOtcAck, messageHelper.createTextMessage(new OTCTransactionACKDTO(
                uid, true, message
        )));
    }

    private void retryableFailureMessage(String uid, String message, boolean rollback) {
        try {
            sendFailureMessage(uid, message);
            if(rollback) rollback(uid);
        } catch(JMSException jms) {
            taskScheduler.schedule(() -> retryableFailureMessage(uid, message, rollback), Instant.now().plusSeconds(5));
        }
    }

    private void nextStage(String uid) {
        OTCTransaction transaction = activeTransactions.get(uid);
        if(transaction == null)
            return;

        try {
            jmsTemplate.convertAndSend(destinationOtcAck, messageHelper.createTextMessage(new OTCTransactionACKDTO(
                    uid, false, ""
            )));
            transaction.nextStage();
        } catch (JMSException jms) {
            taskScheduler.schedule(() -> nextStage(uid), Instant.now().plusSeconds(5));
        }
    }

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

                    nextStage(uid);
                }
                case ASSETS_RESERVED -> {
                    Account account = accountRepository.findById(transaction.getSellerAccountId()).orElseThrow();
                    account.setBalance(account.getBalance() + transaction.getAmount());
                    accountRepository.save(account);

                    nextStage(uid);
                }
                case ASSETS_TRANSFERED -> {
                    // provera konzistentnog stanja?

                    nextStage(uid);
                }
                case FINISHED -> activeTransactions.remove(uid);
            }
        } catch(Exception e) {
            retryableFailureMessage(uid, e.getMessage(), true);
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
            retryableFailureMessage(uid, e.getMessage(), false);
        }
    }
}
