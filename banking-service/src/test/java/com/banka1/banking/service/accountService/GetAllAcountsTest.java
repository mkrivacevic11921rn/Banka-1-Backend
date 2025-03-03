package com.banka1.banking.service.accountService;

import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.services.AccountService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
public class GetAllAcountsTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private AccountService accountService;
}
