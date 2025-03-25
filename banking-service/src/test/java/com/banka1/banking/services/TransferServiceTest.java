package com.banka1.banking.services;


import com.banka1.banking.models.*;
import com.banka1.banking.models.helper.*;
import com.banka1.banking.repository.*;
import com.banka1.banking.dto.CustomerDTO;
import com.banka1.banking.dto.InternalTransferDTO;
import com.banka1.banking.dto.MoneyTransferDTO;
import com.banka1.banking.dto.NotificationDTO;
import com.banka1.banking.listener.MessageHelper;
import com.banka1.banking.models.*;
import com.banka1.banking.models.helper.CurrencyType;
import com.banka1.banking.models.helper.TransferStatus;
import com.banka1.banking.models.helper.TransferType;
import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.repository.CurrencyRepository;
import com.banka1.banking.repository.TransactionRepository;
import com.banka1.banking.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import javax.jms.TextMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
public class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Account fromAccount;
    private Account toAccount;
    private Currency rsdCurrency;
    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private MessageHelper messageHelper;

    @Mock
    private UserServiceCustomer userServiceCustomer;

    @Mock
    private OtpTokenService otpTokenService;

    @Mock
    private BankAccountUtils bankAccountUtils;

    @Mock
    private ExchangeService exchangeService;
    
    
    private Transfer internalTransfer;
    private Transfer externalTransfer;
    private Transfer exchangeTransfer;
    private Transfer foreignTransfer;
    private Account fromAccountForeign;
    private Account toAccountForeign;
    private Account bankAccountUSD;
    private Account bankAccountEUR;
    private Currency usdCurrency;
    private Currency eurCurrency;
    private CustomerDTO customerDTO;
    private CustomerDTO customerDTO2;
    private Transfer pendingTransfer;
    private Company bankCompany;

    @BeforeEach
    void setUp() {
        rsdCurrency = currencyRepository.findByCode(CurrencyType.RSD).orElseGet(() -> {
            Currency c = new Currency();
            c.setCode(CurrencyType.RSD);
            c.setName("Serbian Dinar");
            c.setCountry("Serbia");
            c.setSymbol("RSD");
            return currencyRepository.save(c);
        });

                bankCompany = new Company();
        bankCompany.setName("Banka");

        // Setup test accounts
        fromAccount = new Account();
        fromAccount.setOwnerID(1L);
        fromAccount.setAccountNumber("111111");
        fromAccount.setBalance(100.0);
        fromAccount.setReservedBalance(0.0);
        fromAccount.setType(AccountType.CURRENT);
        fromAccount.setCurrencyType(CurrencyType.RSD);
        fromAccount.setSubtype(AccountSubtype.STANDARD);
        fromAccount.setCreatedDate(System.currentTimeMillis());
        fromAccount.setExpirationDate(System.currentTimeMillis() + 31536000000L);
        fromAccount.setDailyLimit(50000.0);
        fromAccount.setMonthlyLimit(1000000.0);
        fromAccount.setDailySpent(0.0);
        fromAccount.setMonthlySpent(0.0);
        fromAccount.setStatus(AccountStatus.ACTIVE);
        fromAccount.setEmployeeID(1L);
        fromAccount.setMonthlyMaintenanceFee(500.0);
        fromAccount = accountRepository.save(fromAccount);

        toAccount = new Account();
        toAccount.setOwnerID(2L);
        toAccount.setAccountNumber("222222");
        toAccount.setBalance(50.0);
        toAccount.setReservedBalance(0.0);
        toAccount.setType(AccountType.CURRENT);
        toAccount.setCurrencyType(CurrencyType.RSD);
        toAccount.setSubtype(AccountSubtype.STANDARD);
        toAccount.setCreatedDate(System.currentTimeMillis());
        toAccount.setExpirationDate(System.currentTimeMillis() + 31536000000L);
        toAccount.setDailyLimit(50000.0);
        toAccount.setMonthlyLimit(1000000.0);
        toAccount.setDailySpent(0.0);
        toAccount.setMonthlySpent(0.0);
        toAccount.setStatus(AccountStatus.ACTIVE);
        toAccount.setEmployeeID(2L);
        toAccount.setMonthlyMaintenanceFee(500.0);
        toAccount = accountRepository.save(toAccount);
        toAccount.setId(2L);
        toAccount.setOwnerID(200L); // Different owner
        toAccount.setAccountNumber("987654321");
        toAccount.setBalance(500.0);
        toAccount.setCurrencyType(CurrencyType.USD);

        fromAccountForeign = new Account();
        fromAccountForeign.setId(2L);
        fromAccountForeign.setOwnerID(100L);
        fromAccountForeign.setAccountNumber("223456789");
        fromAccountForeign.setBalance(1000.0);
        fromAccountForeign.setCurrencyType(CurrencyType.EUR);

        toAccountForeign = new Account();
        toAccountForeign.setId(3L);
        toAccountForeign.setOwnerID(200L);
        toAccountForeign.setAccountNumber("987654322");
        toAccountForeign.setBalance(500.0);
        toAccountForeign.setCurrencyType(CurrencyType.EUR);

        bankAccountEUR = new Account();
        bankAccountEUR.setId(100L);
        bankAccountEUR.setOwnerID(1L);
        bankAccountEUR.setAccountNumber("111111111");
        bankAccountEUR.setBalance(1000000.0);
        bankAccountEUR.setCurrencyType(CurrencyType.EUR);
        bankAccountEUR.setCompany(bankCompany);

        bankAccountUSD = new Account();
        bankAccountUSD.setId(100L);
        bankAccountUSD.setOwnerID(1L);
        bankAccountUSD.setAccountNumber("111111112");
        bankAccountUSD.setBalance(1000000.0);
        bankAccountUSD.setCurrencyType(CurrencyType.USD);
        bankAccountUSD.setCompany(bankCompany);

        // Setup currencies
        usdCurrency = new Currency();
        usdCurrency.setCode(CurrencyType.USD);

        eurCurrency = new Currency();
        eurCurrency.setCode(CurrencyType.EUR);

        // Setup customer data
        customerDTO = new CustomerDTO();
        customerDTO.setId(100L);
        customerDTO.setFirstName("John");
        customerDTO.setLastName("Doe");
        customerDTO.setEmail("john.doe@example.com");

        customerDTO2 = new CustomerDTO();
        customerDTO2.setId(200L);
        customerDTO2.setFirstName("Jane");
        customerDTO2.setLastName("Doe");
        customerDTO2.setEmail("jane.doe@example.com");

        // Setup pending transfer
        pendingTransfer = new Transfer();
        pendingTransfer.setId(1L);
        pendingTransfer.setFromAccountId(fromAccount);
        pendingTransfer.setToAccountId(toAccount);
        pendingTransfer.setAmount(100.0);
        pendingTransfer.setStatus(TransferStatus.PENDING);
        pendingTransfer.setCreatedAt(System.currentTimeMillis() - 1000); // Created 1 second ago

        // Setup internal transfer
        internalTransfer = new Transfer();
        internalTransfer.setId(1L);
        internalTransfer.setFromAccountId(fromAccount);
        internalTransfer.setToAccountId(toAccount);
        internalTransfer.setAmount(100.0);
        internalTransfer.setStatus(TransferStatus.PENDING);
        internalTransfer.setType(TransferType.INTERNAL);
        internalTransfer.setFromCurrency(usdCurrency);
        internalTransfer.setToCurrency(usdCurrency);

        // Setup external transfer
        externalTransfer = new Transfer();
        externalTransfer.setId(2L);
        externalTransfer.setFromAccountId(fromAccount);
        externalTransfer.setToAccountId(toAccount);
        externalTransfer.setAmount(100.0);
        externalTransfer.setStatus(TransferStatus.PENDING);
        externalTransfer.setType(TransferType.EXTERNAL);
        externalTransfer.setFromCurrency(usdCurrency);
        externalTransfer.setToCurrency(usdCurrency);

        foreignTransfer = new Transfer();
        foreignTransfer.setId(3L);
        foreignTransfer.setFromAccountId(fromAccount);
        foreignTransfer.setToAccountId(toAccountForeign);
        foreignTransfer.setAmount(100.0);
        foreignTransfer.setStatus(TransferStatus.PENDING);
        foreignTransfer.setType(TransferType.FOREIGN);
        foreignTransfer.setFromCurrency(usdCurrency);
        foreignTransfer.setToCurrency(eurCurrency);

        exchangeTransfer = new Transfer();
        exchangeTransfer.setId(4L);
        exchangeTransfer.setFromAccountId(fromAccountForeign);
        exchangeTransfer.setToAccountId(fromAccount);
        exchangeTransfer.setAmount(100.0);
        exchangeTransfer.setStatus(TransferStatus.PENDING);
        exchangeTransfer.setType(TransferType.EXCHANGE);
        exchangeTransfer.setFromCurrency(eurCurrency);
        exchangeTransfer.setToCurrency(usdCurrency);
    }

    @Test
    void testCreateInternalTransfer_Success() {
        toAccount.setOwnerID(100L);

        InternalTransferDTO dto = new InternalTransferDTO();
        dto.setFromAccountId(fromAccount.getId());
        dto.setToAccountId(toAccount.getId());
        dto.setAmount(100.0);

        // Setup mocks
        when(accountRepository.findById(fromAccount.getId())).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccount.getId())).thenReturn(Optional.of(toAccount));
        when(currencyRepository.findByCode(CurrencyType.USD)).thenReturn(Optional.of(usdCurrency));
        when(userServiceCustomer.getCustomerById(100L)).thenReturn(customerDTO);
        when(transferRepository.saveAndFlush(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(otpTokenService.generateOtp(1L)).thenReturn("123456");

        // Execute
        Long result = transferService.createInternalTransfer(dto);

        // Verify
        assertEquals(1L, result);

        // Verify transfer was created and saved
        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).saveAndFlush(transferCaptor.capture());

        Transfer savedTransfer = transferCaptor.getValue();
        assertEquals(fromAccount, savedTransfer.getFromAccountId());
        assertEquals(toAccount, savedTransfer.getToAccountId());
        assertEquals(100.0, savedTransfer.getAmount());
        assertEquals(TransferStatus.PENDING, savedTransfer.getStatus());
        assertEquals(usdCurrency, savedTransfer.getFromCurrency());
        assertEquals(usdCurrency, savedTransfer.getToCurrency());

        // Verify OTP was generated and set
        verify(otpTokenService).generateOtp(1L);
        verify(transferRepository).save(transferCaptor.capture());
        assertEquals("123456", transferCaptor.getValue().getOtp());

        // Verify notification was sent
        ArgumentCaptor<NotificationDTO> notificationCaptor = ArgumentCaptor.forClass(NotificationDTO.class);
        verify(messageHelper).createTextMessage(notificationCaptor.capture());

        NotificationDTO sentNotification = notificationCaptor.getValue();
        assertEquals("Verifikacija", sentNotification.getSubject());
        assertEquals("john.doe@example.com", sentNotification.getEmail());
        assertTrue(sentNotification.getMessage().contains("123456"));
        assertEquals("John", sentNotification.getFirstName());
        assertEquals("Doe", sentNotification.getLastName());
    }

    @Test
    void rollbackInternalTransferWhenInsufficientFunds() {
        final Transfer newTransfer = new Transfer();
        newTransfer.setFromAccountId(fromAccount);
        newTransfer.setToAccountId(toAccount);
        newTransfer.setAmount(200.0);
        newTransfer.setType(TransferType.INTERNAL);
        newTransfer.setStatus(TransferStatus.PENDING);
        newTransfer.setFromCurrency(rsdCurrency);
        newTransfer.setToCurrency(rsdCurrency);
        transferRepository.save(newTransfer);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transferService.processInternalTransfer(newTransfer.getId());
        });

        assertTrue(exception.getMessage().contains("Insufficient funds"));

        Transfer failedTransfer = transferRepository.findById(newTransfer.getId()).orElseThrow();
        assertEquals(TransferStatus.FAILED, failedTransfer.getStatus());
        assertEquals(100.0, accountRepository.findById(fromAccount.getId()).orElseThrow().getBalance());
        assertEquals(50.0, accountRepository.findById(toAccount.getId()).orElseThrow().getBalance());
        assertEquals(0, transactionRepository.findAll().size());
    }

    @Test
    void rollbackExternalTransferWhenInsufficientFunds() {
        final Transfer newTransfer = new Transfer();
        newTransfer.setFromAccountId(fromAccount);
        newTransfer.setToAccountId(toAccount);
        newTransfer.setAmount(200.0);
        newTransfer.setType(TransferType.EXTERNAL);
        newTransfer.setStatus(TransferStatus.PENDING);
        newTransfer.setFromCurrency(rsdCurrency);
        newTransfer.setToCurrency(rsdCurrency);
        transferRepository.save(newTransfer);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transferService.processExternalTransfer(newTransfer.getId());
        });

        assertTrue(exception.getMessage().contains("Insufficient balance for transfer"));

        Transfer failedTransfer = transferRepository.findById(newTransfer.getId()).orElseThrow();
        assertEquals(TransferStatus.FAILED, failedTransfer.getStatus());
        assertEquals(100.0, accountRepository.findById(fromAccount.getId()).orElseThrow().getBalance());
        assertEquals(50.0, accountRepository.findById(toAccount.getId()).orElseThrow().getBalance());
        assertEquals(0, transactionRepository.findAll().size());
    }

    @Test
    void noDuplicateTransactionsForInternalTransfer() {
        // Korisnik ima oba naloga (from i to su njegovi nalozi)
        toAccount.setOwnerID(1L); // Sad oba naloga pripadaju korisniku sa ID 1
        accountRepository.save(toAccount);

        // Kreiramo uspešan interni transfer
        Transfer newTransfer = new Transfer();
        newTransfer.setFromAccountId(fromAccount);
        newTransfer.setToAccountId(toAccount);
        newTransfer.setAmount(50.0);
        newTransfer.setType(TransferType.INTERNAL);
        newTransfer.setStatus(TransferStatus.PENDING);
        newTransfer.setFromCurrency(rsdCurrency);
        newTransfer.setToCurrency(rsdCurrency);
        newTransfer = transferRepository.save(newTransfer);

        // Pozivamo servis za obradu internog transfera
        transferService.processInternalTransfer(newTransfer.getId());

        // Provera da su obe transakcije kreirane u bazi
        assertEquals(2, transactionRepository.findAll().size());

        // Sada simuliramo API poziv koji povlači transakcije korisnika sa ID 1
        var transactions = transactionRepository
                .findByFromAccountIdInOrToAccountIdIn(
                        accountRepository.findByOwnerID(1L),
                        accountRepository.findByOwnerID(1L)
                );

        // Backend servis će u finalnoj verziji ovo filtrirati na 1 transakciju
        // Simulacija mape iz TransactionService
        Map<Long, Transaction> transferToTransaction = new HashMap<>();
        for (Transaction tx : transactions) {
            transferToTransaction.putIfAbsent(tx.getTransfer().getId(), tx);
        }

        assertEquals(1, transferToTransaction.size()); // Backend će vratiti samo jednu transakciju po transferu
    }


    @Test
    void testProcessTransfer_Internal() {
        when(transferRepository.findById(1L)).thenReturn(Optional.of(internalTransfer));
        when(accountRepository.save(any(Account.class))).thenReturn(null);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(null);

        String result = transferService.processTransfer(1L);

        assertEquals("Transfer completed successfully", result);
        assertEquals(TransferStatus.COMPLETED, internalTransfer.getStatus());
        assertEquals(900.0, fromAccount.getBalance());
        assertEquals(600.0, toAccount.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testProcessTransfer_Exchange() {
        when(transferRepository.findById(4L)).thenReturn(Optional.of(exchangeTransfer));
        when(accountRepository.save(any(Account.class))).thenReturn(null);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(null);
        when(userServiceCustomer.getCustomerById(100L)).thenReturn(customerDTO);

        when(exchangeService.calculatePreviewExchangeAutomatic(anyString(), anyString(), any())).thenReturn(
                Map.of(
                        "finalAmount", 90.0
                )
        );
        when(bankAccountUtils.getBankAccountForCurrency(eurCurrency.getCode())).thenReturn(bankAccountEUR);
        when(bankAccountUtils.getBankAccountForCurrency(usdCurrency.getCode())).thenReturn(bankAccountUSD);

        String result = transferService.processTransfer(4L);

        assertEquals("Transfer completed successfully", result);
        assertEquals(TransferStatus.COMPLETED, exchangeTransfer.getStatus());
        assertEquals(900.0, fromAccountForeign.getBalance());
        assertEquals(1090.0, fromAccount.getBalance());
        assertEquals(1000100.0, bankAccountEUR.getBalance());
        assertEquals(999910.0, bankAccountUSD.getBalance());
        verify(transactionRepository, times(3)).save(any(Transaction.class));
    }

    @Test
    void testProcessTransfer_External() {
        when(transferRepository.findById(2L)).thenReturn(Optional.of(externalTransfer));
        when(accountRepository.save(any(Account.class))).thenReturn(null);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(null);

        String result = transferService.processTransfer(2L);

        assertEquals("Transfer completed successfully", result);
        assertEquals(TransferStatus.COMPLETED, externalTransfer.getStatus());
        assertEquals(900.0, fromAccount.getBalance());
        assertEquals(600.0, toAccount.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testProcessTransfer_Foreign() {
        when(transferRepository.findById(3L)).thenReturn(Optional.of(foreignTransfer));
        when(accountRepository.save(any(Account.class))).thenReturn(null);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(null);
        when(userServiceCustomer.getCustomerById(200L)).thenReturn(customerDTO2);

        when(exchangeService.calculatePreviewExchangeAutomatic(anyString(), anyString(), any())).thenReturn(
                Map.of(
                        "finalAmount", 90.0
                )
        );
        when(bankAccountUtils.getBankAccountForCurrency(eurCurrency.getCode())).thenReturn(bankAccountEUR);
        when(bankAccountUtils.getBankAccountForCurrency(usdCurrency.getCode())).thenReturn(bankAccountUSD);

        String result = transferService.processTransfer(3L);

        assertEquals("Transfer completed successfully", result);
        assertEquals(TransferStatus.COMPLETED, foreignTransfer.getStatus());
        assertEquals(900.0, fromAccount.getBalance());
        assertEquals(590.0, toAccountForeign.getBalance());
        assertEquals(1000100.0, bankAccountUSD.getBalance());
        assertEquals(999910.0, bankAccountEUR.getBalance());
        verify(transactionRepository, times(3)).save(any(Transaction.class));
    }

    @Test
    void testProcessInternalTransfer_InsufficientFunds() {
        fromAccount.setBalance(50.0);
        internalTransfer.setAmount(100.0);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(internalTransfer));

        assertThrows(RuntimeException.class, () -> transferService.processInternalTransfer(1L));
        assertEquals(TransferStatus.FAILED, internalTransfer.getStatus());
    }

    @Test
    void testProcessExternalTransfer_InsufficientFunds() {
        fromAccount.setBalance(50.0);
        externalTransfer.setAmount(100.0);

        when(transferRepository.findById(2L)).thenReturn(Optional.of(externalTransfer));

        assertThrows(RuntimeException.class, () -> transferService.processExternalTransfer(2L));
        assertEquals(TransferStatus.FAILED, externalTransfer.getStatus());
    }
}
