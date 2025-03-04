package com.banka1.banking.services;

import com.banka1.banking.models.Receiver;
import com.banka1.banking.repository.ReceiverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReceiverService {

    private final ReceiverRepository receiverRepository;

    @Autowired
    public ReceiverService(ReceiverRepository receiverRepository) {
        this.receiverRepository = receiverRepository;
    }

    public void saveReceiver(Long customerId,String accountNumber,String firstName,String lastName,String adress){
        Optional<Receiver> existingReceiver = receiverRepository.findByCustomerIdAndAccountNumber(customerId,accountNumber);

        if (existingReceiver.isEmpty()) {

            Receiver receiver = new Receiver();
            receiver.setCustomerId(customerId);
            receiver.setAccountNumber(accountNumber);
            receiver.setFirstName(firstName);
            receiver.setLastName(lastName);
            receiver.setAddress(adress);

            receiverRepository.save(receiver);
        }
    }

    public List<Receiver> getReceiverByCustomer(Long customerId){
        return receiverRepository.findAllByCustomerId(customerId);
    }


}
