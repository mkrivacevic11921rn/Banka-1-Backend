package com.banka1.banking.listener;

import com.banka1.banking.dto.OTCTransactionACKDTO;
import com.banka1.banking.dto.OTCTransactionInitiationDTO;
import com.banka1.banking.saga.OTCTransaction;
import com.banka1.banking.services.OTCService;
import com.banka1.common.listener.MessageHelper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OTCListener {
    private final OTCService otcService;
    private final MessageHelper messageHelper;

    @JmsListener(destination = "${destination.otc.ack.bank}", concurrency = "5-10")
    public void onAckMessage(Message message) throws JMSException {
        OTCTransactionACKDTO dto = messageHelper.getMessage(message, OTCTransactionACKDTO.class);
        if(dto.isFailure()) {
            log.error(dto.getMessage());
            otcService.rollback(dto.getUid());
        } else {
            otcService.proceed(dto.getUid());
        }
    }

    @JmsListener(destination = "${destination.otc.init}", concurrency = "5-10")
    public void onInitMessage(Message message) throws JMSException {
        OTCTransactionInitiationDTO dto = messageHelper.getMessage(message, OTCTransactionInitiationDTO.class);
        OTCTransaction transaction = new OTCTransaction(dto.getSellerAccountId(), dto.getBuyerAccountId(), dto.getAmount());
        otcService.initiate(dto.getUid(), transaction);
    }
}
