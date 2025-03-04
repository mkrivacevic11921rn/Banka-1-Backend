package com.banka1.banking.services;

import com.banka1.banking.models.Account;
import com.banka1.banking.models.Transaction;
import com.banka1.banking.models.Transfer;
import com.banka1.banking.models.helper.TransferStatus;
import com.banka1.banking.models.helper.TransferType;
import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.repository.TransactionRepository;
import com.banka1.banking.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TransferService {
    private final TransferRepository transferRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransferService(TransferRepository transferRepository,
                           TransactionRepository transactionRepository,
                           AccountRepository accountRepository) {
        this.transferRepository = transferRepository;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public String processTransfer(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        return transfer.getType().equals(TransferType.INTERNAL)
                ? processInternalTransfer(transferId)
                : processExternalTransfer(transferId);
    }

    @Transactional
    public String processExternalTransfer(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (!transfer.getStatus().equals(TransferStatus.PENDING)) {
            throw new RuntimeException("Transfer is not in pending state");
        }

        Account fromAccount = transfer.getFromAccountId();
        Account toAccount = transfer.getToAccountId();
        Double amount = transfer.getAmount();

        if (fromAccount.getBalance() < amount) {
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setNote("Insufficient balance");
            transferRepository.save(transfer);
            throw new RuntimeException("Insufficient balance for transfer");
        }

        try {
            fromAccount.setBalance(fromAccount.getBalance() - amount);
            accountRepository.save(fromAccount);

            toAccount.setBalance(toAccount.getBalance() + amount);
            accountRepository.save(toAccount);

            Transaction debitTransaction = new Transaction();
            debitTransaction.setFromAccountId(fromAccount);
            debitTransaction.setToAccountId(toAccount);
            debitTransaction.setAmount(amount);
            debitTransaction.setCurrency(transfer.getFromCurrency());
            debitTransaction.setTimestamp(Instant.now().toEpochMilli());
            debitTransaction.setDescription("Debit transaction for transfer " + transfer.getId());
            debitTransaction.setTransfer(transfer);
            transactionRepository.save(debitTransaction);

            Transaction creditTransaction = new Transaction();
            creditTransaction.setFromAccountId(fromAccount);
            creditTransaction.setToAccountId(toAccount);
            creditTransaction.setAmount(amount);
            creditTransaction.setCurrency(transfer.getToCurrency());
            creditTransaction.setTimestamp(Instant.now().toEpochMilli());
            creditTransaction.setDescription("Credit transaction for transfer " + transfer.getId());
            creditTransaction.setTransfer(transfer);
            transactionRepository.save(creditTransaction);

            transfer.setStatus(TransferStatus.COMPLETED);
            transfer.setCompletedAt(Instant.now().toEpochMilli());
            transferRepository.save(transfer);

            return "Transfer completed successfully";
        } catch (Exception e) {
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setNote("Error during processing: " + e.getMessage());
            transferRepository.save(transfer);
            throw new RuntimeException("Transfer processing failed", e);
        }
    }

    @Transactional
    public String processInternalTransfer(Long transferId) {

        return null;
    }

}
