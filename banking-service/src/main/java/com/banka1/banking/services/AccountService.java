package com.banka1.banking.services;

import com.banka1.banking.dto.request.CreateAccountDTO;
import com.banka1.banking.dto.request.UpdateAccountDTO;
import com.banka1.banking.listener.MessageHelper;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.helper.AccountSubtype;
import com.banka1.banking.repository.AccountRepository;

import com.banka1.user.repository.CustomerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final JmsTemplate jmsTemplate;
    private final MessageHelper messageHelper;
//    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, JmsTemplate jmsTemplate, MessageHelper messageHelper/*, CustomerRepository customerRepository*/) {
        this.accountRepository = accountRepository;
        this.jmsTemplate = jmsTemplate;
        this.messageHelper = messageHelper;
//        this.customerRepository = customerRepository;
    }
    @Autowired
    private ModelMapper modelMapper;

    public Account createAccount(CreateAccountDTO createAccountDTO) {

//        if (! customerRepository.existsById(createAccountDTO.getOwnerID())) {
//            create customer
//        }
//
        if (accountRepository.existsByOwnerID((createAccountDTO.getOwnerID())) &&
                accountRepository.existsByAccountNumber((createAccountDTO.getAccountNumber()))) {
            throw new RuntimeException("Nalog sa ovim parametrima već postoji!");
        }
        System.out.println(createAccountDTO.getOwnerID());
        Account account = modelMapper.map(createAccountDTO, Account.class);
        account.setSubtype(AccountSubtype.STANDARD);
        account.setReservedBalance(100.0);
        account.setCreatedDate(Instant.now().getEpochSecond());
        account.setExpirationDate(account.getCreatedDate() + 4 * 365 * 24 * 60 * 60);
        account.setDailySpent(0.0);
        account.setMonthlySpent(0.0);
        account.setMonthlyMaintenanceFee(0.0);
        account.setEmployeeID(Long.valueOf(1));

//      obavestenje korisniku racuna na mejl da mu je kreiran racun
//
//        NotificationDTO emailDTO = new NotificationDTO();
//        emailDTO.setSubject("Račun uspešno kreiran");
//        emailDTO.setEmail(owner.getEmail());
//        emailDTO.setMessage("Vaš racun je uspešno kreiran");
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

    public Account updateAccount(Long accountId, UpdateAccountDTO updateAccountDTO) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Račun sa ID-jem " + accountId + " nije pronađen"));


        Optional.ofNullable(updateAccountDTO.getDailyLimit()).ifPresent(account::setDailyLimit);
        Optional.ofNullable(updateAccountDTO.getMonthlyLimit()).ifPresent(account::setMonthlyLimit);
        Optional.ofNullable(updateAccountDTO.getStatus()).ifPresent(account::setStatus);

        return accountRepository.save(account);
    }

}
