package com.banka1.banking.services;



import com.banka1.banking.dto.OTCTransactionACKDTO;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.helper.CurrencyType;
import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.saga.OTCTransaction;
import com.banka1.banking.saga.OTCTransactionStage;
import com.banka1.common.listener.MessageHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.TaskScheduler;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class OtcServiceTest {
    @InjectMocks
    private OTCService otcService;
    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private MessageHelper messageHelper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TaskScheduler taskScheduler;


    @Test
    public void testInitiate_Success() {
        String uid = "uid1";
        Account buyer = new Account();
        buyer.setId(1L);
        buyer.setCurrencyType(CurrencyType.valueOf("USD"));

        Account seller = new Account();
        seller.setId(2L);
        seller.setCurrencyType(CurrencyType.valueOf("USD"));

        OTCTransaction transaction = new OTCTransaction(1L, 2L, 100.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(messageHelper.createTextMessage(any(OTCTransactionACKDTO.class))).thenReturn("ACK");

        otcService.initiate(uid, transaction);

    }

    @Test
    public void testProceed_InitializedStage_Success() {
        String uid = "uid2";

        Account seller = new Account();
        seller.setId(1L);
        seller.setBalance(200.0);
        seller.setCurrencyType(CurrencyType.valueOf("USD"));

        Account buyer = new Account();
        buyer.setId(2L);
        buyer.setBalance(300.0);
        buyer.setCurrencyType(CurrencyType.valueOf("USD"));

        OTCTransaction transaction = new OTCTransaction(1L, 2L, 100.0);
        transaction.setStage(OTCTransactionStage.INITIALIZED);


        when(accountRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(messageHelper.createTextMessage(any(OTCTransactionACKDTO.class))).thenReturn("ACK");
        otcService.initiate(uid, transaction);

        otcService.proceed(uid);


        assertEquals(200.0, buyer.getBalance()); // Balance should be reduced by 100
    }

    @Test
    public void testProceed_AssertStage_Success() {
        String uid = "uid2";

        Account seller = new Account();
        seller.setId(1L);
        seller.setBalance(200.0);
        seller.setCurrencyType(CurrencyType.valueOf("USD"));

        Account buyer = new Account();
        buyer.setId(2L);
        buyer.setBalance(300.0);
        buyer.setCurrencyType(CurrencyType.valueOf("USD"));

        OTCTransaction transaction = new OTCTransaction(1L, 2L, 100.0);
        transaction.nextStage();


        when(accountRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(messageHelper.createTextMessage(any(OTCTransactionACKDTO.class))).thenReturn("ACK");
        otcService.initiate(uid, transaction);

        otcService.proceed(uid);


        assertEquals(300.0, buyer.getBalance());
    }

    @Test
    public void testRollback_AssetsReserved() {
        String uid = "uid3";

        Account seller = new Account();
        seller.setId(1L);
        seller.setBalance(200.0);
        seller.setCurrencyType(CurrencyType.valueOf("USD"));

        Account buyer = new Account();
        buyer.setId(2L);
        buyer.setBalance(300.0);
        buyer.setCurrencyType(CurrencyType.valueOf("USD"));


        OTCTransaction transaction = new OTCTransaction(1L, 2L, 50.0);
        transaction.nextStage();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(buyer));
        otcService.initiate(uid, transaction);


        otcService.rollback(uid);


    }

    @Test
    public void testRollback_AssetsTransferred() {
        String uid = "uid3";

        Account seller = new Account();
        seller.setId(1L);
        seller.setBalance(200.0);
        seller.setCurrencyType(CurrencyType.valueOf("USD"));

        Account buyer = new Account();
        buyer.setId(2L);
        buyer.setBalance(300.0);
        buyer.setCurrencyType(CurrencyType.valueOf("USD"));


        OTCTransaction transaction = new OTCTransaction(1L, 2L, 50.0);
        transaction.nextStage();
        transaction.nextStage();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(buyer));
        otcService.initiate(uid, transaction);


        otcService.rollback(uid);

    }

    @Test
    public void testPayPremium_Success() {
        Account from = new Account();
        from.setId(1L);
        from.setBalance(200.0);
        from.setCurrencyType(CurrencyType.valueOf("USD"));

        Account to = new Account();
        to.setId(2L);
        to.setBalance(50.0);
        to.setCurrencyType(CurrencyType.valueOf("USD"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(to));

        otcService.payPremium(1L, 2L, 100.0);

        assertEquals(100.0, from.getBalance());
        assertEquals(150.0, to.getBalance());
        verify(accountRepository).save(from);
        verify(accountRepository).save(to);
    }

    @Test
    public void testPayPremium_InsufficientFunds() {
        Account from = new Account();
        from.setId(1L);
        from.setBalance(20.0);
        from.setCurrencyType(CurrencyType.valueOf("USD"));

        Account to = new Account();
        to.setId(2L);
        to.setCurrencyType(CurrencyType.valueOf("USD"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(to));

        otcService.payPremium(1L, 2L, 100.0);

        verify(accountRepository, never()).save(any());
    }
}
