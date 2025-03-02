package com.banka1.banking.services;

import com.banka1.banking.dto.request.CreateAccountDTO;
import com.banka1.banking.dto.request.UpdateAccountDTO;
import com.banka1.banking.listener.MessageHelper;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.helper.AccountSubtype;
import com.banka1.banking.repository.AccountRepository;

import org.modelmapper.ModelMapper;
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

    public Account updateAccount(Long accountId, UpdateAccountDTO updateAccountDTO) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Račun sa ID-jem " + accountId + " nije pronađen"));

        Optional.ofNullable(updateAccountDTO.getReservedBalance()).ifPresent(account::setReservedBalance);
        Optional.ofNullable(updateAccountDTO.getExpirationDate()).ifPresent(account::setExpirationDate);
        Optional.ofNullable(updateAccountDTO.getDailyLimit()).ifPresent(account::setDailyLimit);
        Optional.ofNullable(updateAccountDTO.getMonthlyLimit()).ifPresent(account::setMonthlyLimit);
        Optional.ofNullable(updateAccountDTO.getSubtype()).ifPresent(account::setSubtype);
        Optional.ofNullable(updateAccountDTO.getStatus()).ifPresent(account::setStatus);
        Optional.ofNullable(updateAccountDTO.getMonthlyMaintenanceFee()).ifPresent(account::setMonthlyMaintenanceFee);

        return accountRepository.save(account);
    }

}
