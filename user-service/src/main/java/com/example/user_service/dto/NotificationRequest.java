package com.example.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NotificationRequest {
    private List<String> adminEmails;
    private String subject;
    private String message;
}
