package com.banka1.banking.service.accountService;

import com.banka1.banking.dto.request.UpdateAccountDTO;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.helper.AccountStatus;
import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateAccountTest {
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private AccountService accountService;

    private UpdateAccountDTO updateAccountDTO;
    @BeforeEach
    void setup() {
        updateAccountDTO = new UpdateAccountDTO();
        updateAccountDTO.setStatus(AccountStatus.BLOCKED);
    }

    @Test
    public void testUpdateAccountStatus() {
        Long accountId = 1L;
        Account acc = new Account();
        acc.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(acc));

        accountService.updateAccount(accountId, updateAccountDTO);

        assertEquals(AccountStatus.BLOCKED, acc.getStatus());
        verify(accountRepository, times(1)).save(acc);
    }

    @Test
    public void testUpdateAccountLimit() {
        Long accountId = 1L;
        Double newLimit = 1000.5;
        Account acc = new Account();
        acc.setDailyLimit(newLimit);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(acc));

        accountService.updateAccount(accountId, updateAccountDTO);

        assertEquals(newLimit, acc.getDailyLimit());
        verify(accountRepository, times(1)).save(acc);
    }

}
