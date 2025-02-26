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
    private String email;
    private String subject;
    private String message;
    private String firstName;
    private String lastName;
    private String type;
    private Long userId;
    private UserType userType;
}
