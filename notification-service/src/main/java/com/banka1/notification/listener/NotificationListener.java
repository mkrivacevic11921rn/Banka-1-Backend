package com.banka1.notification.listener;

import com.banka1.notification.DTO.response.NotificationDTO;
import com.banka1.notification.listener.helper.MessageHelper;
import com.banka1.notification.service.EmailService;
import com.banka1.notification.service.NotificationService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    @Autowired
    NotificationService notificationService;

    public NotificationListener(MessageHelper messageHelper, EmailService emailService) {
        this.messageHelper = messageHelper;
        this.emailService = emailService;
    }

    private MessageHelper messageHelper;
    private EmailService emailService;

    @JmsListener(destination = "${destination.email}", concurrency = "5-10")
    public void onActivationMessage(Message message) throws JMSException {
        System.out.println("Message received: " + message);
        NotificationDTO email = messageHelper.getMessage(message, NotificationDTO.class);
        System.out.println("Email: " + email.getType());
//        notificationService.createNotification(email);
        emailService.sendEmail(email);
    }
}
