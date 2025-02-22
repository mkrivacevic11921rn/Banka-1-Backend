package com.banka1.notification.DTO.response;

import com.banka1.notification.model.helper.UserType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Getter
@Setter
public class NotificationDTO {
    private String subject;
    private String body;
    private String notificationType;
    private UserType role;
    private String userId;
    private LocalDateTime sentAt;
}
