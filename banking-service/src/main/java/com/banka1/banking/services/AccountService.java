package com.banka1.banking.services;

import com.banka1.banking.dto.request.CreateAccountDTO;
import com.banka1.banking.listener.MessageHelper;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.helper.AccountSubtype;
import com.banka1.banking.repository.AccountRepository;

import org.modelmapper.ModelMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;


@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final JmsTemplate jmsTemplate;
    private final MessageHelper messageHelper;

    public AccountService(AccountRepository accountRepository, JmsTemplate jmsTemplate, MessageHelper messageHelper) {
        this.accountRepository = accountRepository;
        this.jmsTemplate = jmsTemplate;
        this.messageHelper = messageHelper;
    }

    private ModelMapper modelMapper;

    public Account createAccount(CreateAccountDTO createAccountDTO) {
        if (accountRepository.existsByOwnerID((createAccountDTO.getOwnerID())) &&
                accountRepository.existsByAccountNumber((createAccountDTO.getAccountNumber()))) {
            throw new RuntimeException("Nalog sa ovim parametrima već postoji!");
        }

        Account account = modelMapper.map(createAccountDTO, Account.class);
        account.setBalance(0.0);
        account.setReservedBalance(0.0);
        account.setSubtype(AccountSubtype.STANDARD);
        account.setCreatedDate(Instant.now().getEpochSecond());
        account.setExpirationDate(account.getCreatedDate() + 365 * 24 * 60 * 60);
        account.setDailySpent(0.0);
        account.setMonthlySpent(0.0);
        account.setMonthlyMaintenanceFee(0.0);
//        account.setEmployeeID(null);
//      obavestenje korisniku racuna na mejl da mu je kreiran racun?
//        String verificationCode = UUID.randomUUID().toString();
//        owner.setVerificationCode(verificationCode);
//
//        NotificationDTO emailDTO = new NotificationDTO();
//        emailDTO.setSubject("Nalog uspešno kreiran");
//        emailDTO.setEmail(owner.getEmail());
//        emailDTO
//                .setMessage("Vaš racun je uspešno kreiran");
//        emailDTO.setFirstName(owner.getFirstName());
//        emailDTO.setLastName(owner.getLastName());
//        emailDTO.setType("email");

//        jmsTemplate.convertAndSend(destinationEmail, messageHelper.createTextMessage(emailDTO));

        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<Account> getAccountsByOwnerId(Long ownerId) {
        return accountRepository.findByOwnerID(ownerId);
    }
}
