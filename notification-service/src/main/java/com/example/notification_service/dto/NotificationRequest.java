package com.example.notification_service.dto;

import lombok.Data;

import java.util.List;


public class NotificationRequest {
    private List<String> adminEmails;
    private String subject;
    private String message;

    public NotificationRequest() {
    }

    public NotificationRequest(List<String> adminEmails, String subject, String message) {
        this.adminEmails = adminEmails;
        this.subject = subject;
        this.message = message;
    }

    public List<String> getAdminEmails() {
        return adminEmails;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }


    @Override
    public String toString() {
        return "NotificationRequest{" +
                "adminEmails=" + adminEmails +
                ", subject='" + subject + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}