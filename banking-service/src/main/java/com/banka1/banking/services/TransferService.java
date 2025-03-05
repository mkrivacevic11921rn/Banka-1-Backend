package com.banka1.banking.services;

import com.banka1.banking.models.Account;
import com.banka1.banking.models.Transaction;
import com.banka1.banking.models.Transfer;
import com.banka1.banking.models.helper.TransferStatus;
import com.banka1.banking.models.helper.TransferType;
import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.repository.TransactionRepository;
import com.banka1.banking.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransferService(TransferRepository transferRepository, TransactionRepository transactionRepository, AccountRepository accountRepository) {
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
    public String processInternalTransfer(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId).orElseThrow(() -> new RuntimeException("Transfer not found"));

        // Provera statusa i tipa transfera
        if (!transfer.getStatus().equals(TransferStatus.PENDING)) {
            throw new RuntimeException("Transfer is not in pending state");
        }

        if (!transfer.getType().equals(TransferType.INTERNAL)) {
            throw new RuntimeException("Invalid transfer type for this process");
        }

        Account fromAccount = transfer.getFromAccountId();
        Account toAccount = transfer.getToAccountId();

        //Ukoliko na racunu ne postoji dovoljno sredstava za izvrsenje
        if (fromAccount.getBalance() < transfer.getAmount()) {
            transfer.setStatus(TransferStatus.FAILED);
            transferRepository.save(transfer);
            throw new RuntimeException("Insufficient funds");
        }

        try{
            // Azuriranje balansa
            fromAccount.setBalance(fromAccount.getBalance() - transfer.getAmount());
            toAccount.setBalance(toAccount.getBalance() + transfer.getAmount());
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Kreiranje transakcija
            Transaction debitTransaction = new Transaction();
            debitTransaction.setFromAccountId(fromAccount);
            debitTransaction.setToAccountId(toAccount);
            debitTransaction.setAmount(transfer.getAmount());
            debitTransaction.setCurrency(transfer.getFromCurrency());
            debitTransaction.setTimestamp(System.currentTimeMillis());
            debitTransaction.setDescription("Debit transaction for transfer " + transferId);
            debitTransaction.setTransfer(transfer);

            Transaction creditTransaction = new Transaction();
            creditTransaction.setFromAccountId(fromAccount);
            creditTransaction.setToAccountId(toAccount);
            creditTransaction.setAmount(transfer.getAmount());
            creditTransaction.setCurrency(transfer.getToCurrency());
            creditTransaction.setTimestamp(System.currentTimeMillis());
            creditTransaction.setDescription("Credit transaction for transfer " + transferId);
            creditTransaction.setTransfer(transfer);

            transactionRepository.save(debitTransaction);
            transactionRepository.save(creditTransaction);

            transfer.setStatus(TransferStatus.COMPLETED);
            transfer.setCompletedAt(System.currentTimeMillis());
            transferRepository.save(transfer);

            return "Transfer completed successfully";
        }catch (Exception e) {
            throw new RuntimeException("Transaction failed, rollback initiated", e);
        }

    }

    @Transactional
    public String processExternalTransfer(Long transferId){
        return null;
    }

}
