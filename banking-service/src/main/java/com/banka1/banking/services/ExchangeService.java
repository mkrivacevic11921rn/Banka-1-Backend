package com.banka1.banking.services;

import com.banka1.banking.dto.CustomerDTO;
import com.banka1.banking.dto.ExchangeMoneyTransferDTO;
import com.banka1.banking.dto.NotificationDTO;
import com.banka1.banking.listener.MessageHelper;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.Currency;
import com.banka1.banking.models.ExchangePair;
import com.banka1.banking.models.Transfer;
import com.banka1.banking.models.helper.CurrencyType;
import com.banka1.banking.models.helper.TransferStatus;
import com.banka1.banking.models.helper.TransferType;
import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.repository.CurrencyRepository;
import com.banka1.banking.repository.ExchangePairRepository;
import com.banka1.banking.repository.TransferRepository;
import com.banka1.banking.utils.ExcludeFromGeneratedJacocoReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ExchangeService {

    private final AccountRepository accountRepository;

    private final CurrencyRepository currencyRepository;

    private final TransferRepository transferRepository;

    private final JmsTemplate jmsTemplate;

    private final MessageHelper messageHelper;

    private final String destinationEmail;

    private final UserServiceCustomer userServiceCustomer;

    private final OtpTokenService otpTokenService;

    private final ExchangePairRepository exchangePairRepository;

    public ExchangeService(AccountRepository accountRepository, CurrencyRepository currencyRepository, TransferRepository transferRepository, JmsTemplate jmsTemplate, MessageHelper messageHelper, @Value("send-email") String destinationEmail, UserServiceCustomer userServiceCustomer, OtpTokenService otpTokenService, ExchangePairRepository exchangePairRepository) {
        this.accountRepository = accountRepository;
        this.currencyRepository = currencyRepository;
        this.transferRepository = transferRepository;
        this.jmsTemplate = jmsTemplate;
        this.messageHelper = messageHelper;
        this.destinationEmail = destinationEmail;
        this.userServiceCustomer = userServiceCustomer;
        this.otpTokenService = otpTokenService;
        this.exchangePairRepository = exchangePairRepository;
    }

    public boolean validateExchangeTransfer(ExchangeMoneyTransferDTO exchangeMoneyTransferDTO){

        Optional<Account> fromAccountOtp = accountRepository.findById(exchangeMoneyTransferDTO.getAccountFrom());
        Optional<Account> toAccountOtp = accountRepository.findById(exchangeMoneyTransferDTO.getAccountTo());

        if (fromAccountOtp.isEmpty() || toAccountOtp.isEmpty()){
            return false;
        }

        Account fromAccount = fromAccountOtp.get();
        Account toAccount = toAccountOtp.get();

        if (fromAccount.getCurrencyType().equals(toAccount.getCurrencyType())){
            return false;
        }

        if (!fromAccount.getOwnerID().equals(toAccount.getOwnerID())){
            return false;
        }

        return true;
    }

    public Long createExchangeTransfer(ExchangeMoneyTransferDTO exchangeMoneyTransferDTO) {

        Optional<Account> fromAccountDTO = accountRepository.findById(exchangeMoneyTransferDTO.getAccountFrom());
        Optional<Account> toAccountDTO = accountRepository.findById(exchangeMoneyTransferDTO.getAccountTo());

        if (fromAccountDTO.isPresent() && toAccountDTO.isPresent()) {

            Account fromAccount = fromAccountDTO.get();
            Account toAccount = toAccountDTO.get();

            // PROVERITI DA LI SE VALUTE SALJU U DTO
            Currency fromCurrency = currencyRepository.findByCode(fromAccount.getCurrencyType())
                    .orElseThrow(() -> new IllegalArgumentException("Greska"));

            Currency toCurrency = currencyRepository.findByCode(toAccount.getCurrencyType())
                    .orElseThrow(() -> new IllegalArgumentException("Greska"));

            Long customerId = fromAccount.getOwnerID();
            CustomerDTO customerData = userServiceCustomer.getCustomerById(customerId);

            if (customerData == null) {
                throw new IllegalArgumentException("Korisnik nije pronađen");
            }

            String email = customerData.getEmail();
            String firstName = customerData.getFirstName();
            String lastName = customerData.getLastName();

            Transfer transfer = new Transfer();
            transfer.setFromAccountId(fromAccount);
            transfer.setToAccountId(toAccount);
            transfer.setAmount(exchangeMoneyTransferDTO.getAmount());
            transfer.setStatus(TransferStatus.PENDING);
            transfer.setType(TransferType.EXCHANGE);
            transfer.setFromCurrency(fromCurrency);
            transfer.setToCurrency(toCurrency);
            transfer.setCreatedAt(System.currentTimeMillis());

            transferRepository.saveAndFlush(transfer);

            String otpCode = otpTokenService.generateOtp(transfer.getId());
            transfer.setOtp(otpCode);

            transferRepository.save(transfer);

            NotificationDTO emailDto = new NotificationDTO();
            emailDto.setSubject("Verifikacija");
            emailDto.setEmail(email);
            emailDto.setMessage("Vaš verifikacioni kod je: " + otpCode);
            emailDto.setFirstName(firstName);
            emailDto.setLastName(lastName);
            emailDto.setType("email");

            jmsTemplate.convertAndSend(destinationEmail, messageHelper.createTextMessage(emailDto));
            return transfer.getId();
        }

        return null;
    }


    private CurrencyType parseCurrency(String currency) {
        try {
            return CurrencyType.valueOf(currency.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Nepoznata valuta: " + currency);
        }

    @ExcludeFromGeneratedJacocoReport("Wrapper method")
    public Map<String, Object> calculatePreviewExchangeAutomatic(String fromCurrency, String toCurrency, Double amount) {
        if(fromCurrency.equals("RSD") || toCurrency.equals("RSD"))
            return calculatePreviewExchange(fromCurrency, toCurrency, amount);
        else
            return calculatePreviewExchangeForeign(fromCurrency, toCurrency, amount);
    }

    public Map<String, Object> calculatePreviewExchange(String fromCurrency, String toCurrency, Double amount) {
        boolean isToRSD = toCurrency.equalsIgnoreCase("RSD");
        boolean isFromRSD = fromCurrency.equalsIgnoreCase("RSD");

        if (!isToRSD && !isFromRSD) {
            throw new RuntimeException("Ova funkcija podržava samo konverzije između RSD i druge valute.");
        }


        CurrencyType base = parseCurrency(isToRSD ? fromCurrency : "RSD");
        CurrencyType target = parseCurrency(isToRSD ? "RSD" : toCurrency);

        Optional<ExchangePair> exchangePairOpt = exchangePairRepository
                .findByBaseCurrencyCodeAndTargetCurrencyCode(base, target);

        double exchangeRate;
        if (exchangePairOpt.isPresent()) {
            exchangeRate = exchangePairOpt.get().getExchangeRate();
        } else {
            Optional<ExchangePair> reverseOpt = exchangePairRepository
                    .findByBaseCurrencyCodeAndTargetCurrencyCode(target, base);

            if (reverseOpt.isEmpty()) {
                throw new RuntimeException("Kurs nije pronađen za traženu konverziju.");
            }
            exchangeRate = 1 / reverseOpt.get().getExchangeRate();
        }


        ExchangePair exchangePair = exchangePairOpt.get();
        double exchangeRate = exchangePair.getExchangeRate();
        double convertedAmount = amount * exchangeRate;
        double fee = convertedAmount * 0.01;
        if (fromCurrency.equalsIgnoreCase("RSD") && toCurrency.equalsIgnoreCase("RSD")) {
            fee = 0.0;
        }
        double finalAmount = convertedAmount - fee;

        double provision;
        if (fromCurrency.equalsIgnoreCase("RSD")) {
            provision = fee * 1 / exchangeRate;
            exchangeRate = 1 / exchangeRate;
        }else {
            provision = fee;
        }


        return Map.of(
                "exchangeRate", exchangeRate,
                "convertedAmount", convertedAmount,
                "fee", fee,
                "provision", provision,
                "finalAmount", finalAmount
        );
    }


    public Map<String, Object> calculatePreviewExchangeForeign(String fromCurrency, String toCurrency, Double amount) {
        if (fromCurrency.equalsIgnoreCase("RSD") || toCurrency.equalsIgnoreCase("RSD")) {
            throw new RuntimeException("Ova metoda je samo za konverziju strane valute u stranu valutu.");
        }

        // NESTO -> RSD
        CurrencyType from = parseCurrency(fromCurrency);
        CurrencyType to = parseCurrency(toCurrency);
        CurrencyType rsd = CurrencyType.RSD;

        Optional<ExchangePair> firstExchangeOpt = exchangePairRepository
                .findByBaseCurrencyCodeAndTargetCurrencyCode(from, rsd);

        double firstExchangeRate;
        if (firstExchangeOpt.isPresent()) {
            firstExchangeRate = firstExchangeOpt.get().getExchangeRate();
        } else {
            Optional<ExchangePair> reverseFirstOpt = exchangePairRepository
                    .findByBaseCurrencyCodeAndTargetCurrencyCode(rsd, from);

            if (reverseFirstOpt.isEmpty()) {
                throw new RuntimeException("Kurs za " + fromCurrency + " prema RSD nije pronađen.");
            }
            firstExchangeRate = 1 / reverseFirstOpt.get().getExchangeRate();
        }

        double amountInRSD = amount * firstExchangeRate;
        double firstFee = amountInRSD * 0.01;
        double remainingRSD = amountInRSD - firstFee;

        // RSD -> NESTO
        Optional<ExchangePair> secondExchangeOpt = exchangePairRepository
                .findByBaseCurrencyCodeAndTargetCurrencyCode(rsd, to);

        double secondExchangeRate;
        if (secondExchangeOpt.isPresent()) {
            secondExchangeRate = secondExchangeOpt.get().getExchangeRate();
        } else {
            Optional<ExchangePair> reverseSecondOpt = exchangePairRepository
                    .findByBaseCurrencyCodeAndTargetCurrencyCode(to, rsd);

            if (reverseSecondOpt.isEmpty()) {
                throw new RuntimeException("Kurs za RSD prema " + toCurrency + " nije pronađen.");
            }
            secondExchangeRate = 1 / reverseSecondOpt.get().getExchangeRate();
        }

        double secondExchangeRate = secondExchangeOpt.get().getExchangeRate();
        double amountInTargetCurrency = remainingRSD * secondExchangeRate;
        double secondFee = amountInTargetCurrency * 0.01;
        double finalAmount = amountInTargetCurrency - secondFee;
        double totalFee = firstFee + secondFee;

        secondExchangeRate = 1 / secondExchangeRate;
        return Map.of(
                "firstExchangeRate", firstExchangeRate,
                "secondExchangeRate", secondExchangeRate,
                "totalFee", totalFee,
                "provision", totalFee,
                "fee", totalFee,
                "finalAmount", finalAmount
        );
    }
}
