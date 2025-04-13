package com.banka1.banking.services;

import com.banka1.banking.models.Account;
import com.banka1.banking.models.helper.CurrencyType;
import com.banka1.banking.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private BankAccountUtils bankAccountUtils;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private OrderService orderService;

    private Account userAccount;
    private Account bankAccount;

    @BeforeEach
    void setUp() {

        userAccount = new Account();
        userAccount.setId(1L);
        userAccount.setBalance(1000.0);
        userAccount.setCurrencyType(CurrencyType.USD);
        userAccount.setOwnerID(1L);

        bankAccount = new Account();
        bankAccount.setId(2L);
        bankAccount.setBalance(5000.0);
        bankAccount.setCurrencyType(CurrencyType.USD);
    }

    @Test
    public void testExecuteOrder_InvalidDirection() {

        when(accountService.findById(1L)).thenReturn(userAccount);
        when(bankAccountUtils.getBankAccountForCurrency(CurrencyType.USD)).thenReturn(bankAccount);


        assertThrows(RuntimeException.class, () -> orderService.executeOrder("invalidDirection", 1L, 1L, 100.0));
    }

    @Test
    public void testExecuteOrder_Sell_Success() {

        when(accountService.findById(1L)).thenReturn(userAccount);
        when(bankAccountUtils.getBankAccountForCurrency(CurrencyType.USD)).thenReturn(bankAccount);

        Double amount = 100.0;


        Double finalAmount = orderService.executeOrder("sell", 1L, 1L, amount);


        assertEquals(100.0, finalAmount);
        assertEquals(1100.0, userAccount.getBalance()); // user balance should increase by 100
        assertEquals(4900.0, bankAccount.getBalance()); // bank account balance should decrease by 100
        verify(accountRepository).save(userAccount);
        verify(accountRepository).save(bankAccount);
    }

    @Test
    public void testExecuteOrder_Buy_Success() {

        when(accountService.findById(1L)).thenReturn(userAccount);
        when(bankAccountUtils.getBankAccountForCurrency(CurrencyType.USD)).thenReturn(bankAccount);

        Double amount = 100.0;

        Double finalAmount = orderService.executeOrder("buy", 1L, 1L, amount);


        assertEquals(100.0, finalAmount);
        assertEquals(900.0, userAccount.getBalance()); // user balance should decrease by 100
        assertEquals(5100.0, bankAccount.getBalance()); // bank account balance should increase by 100
        verify(accountRepository).save(userAccount);
        verify(accountRepository).save(bankAccount);
    }
}