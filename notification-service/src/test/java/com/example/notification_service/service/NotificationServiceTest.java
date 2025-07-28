package com.example.notification_service.service;

import com.example.notification_service.dto.NotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequest validRequest;
    private NotificationRequest emptyEmailsRequest;
    private NotificationRequest withInvalidEmailsRequest;

    @BeforeEach
    void setUp() {
        validRequest = new NotificationRequest(
                Arrays.asList("admin1@example.com", "admin2@example.com"),
                "Test Subject",
                "Test Message"
        );

        emptyEmailsRequest = new NotificationRequest(
                Collections.emptyList(),
                "Empty List",
                "Should fail"
        );

        withInvalidEmailsRequest = new NotificationRequest(
                Arrays.asList("valid@example.com", "invalid-email"),
                "Invalid Emails",
                "Test"
        );
    }

    @Test
    void sendAdminNotification_ShouldSendToAllValidEmails() {
        notificationService.sendAdminNotification(validRequest);

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAdminNotification_ShouldThrowException_WhenEmptyEmailsList() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationService.sendAdminNotification(emptyEmailsRequest));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAdminNotification_ShouldSkipInvalidEmails() {
        notificationService.sendAdminNotification(withInvalidEmailsRequest);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAdminNotification_ShouldHandleMailException() {
        doThrow(new MailException("SMTP error") {}).when(mailSender).send(any(SimpleMailMessage.class));

        NotificationRequest singleEmailRequest = new NotificationRequest(
                Collections.singletonList("test@example.com"),
                "Subject",
                "Message"
        );

        notificationService.sendAdminNotification(singleEmailRequest);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void createEmailMessage_ShouldSetCorrectFields() {
        NotificationRequest request = new NotificationRequest(
                Collections.singletonList("to@example.com"),
                "Test Subject",
                "Test Message"
        );

        SimpleMailMessage message = notificationService.createEmailMessage("to@example.com", request);

        assertEquals(NotificationService.SENDER_EMAIL, message.getFrom());
        assertEquals("to@example.com", message.getTo()[0]);
        assertEquals("Test Subject", message.getSubject());
        assertEquals("Test Message", message.getText());
    }

    @Test
    void isValidEmail_ShouldReturnTrueForValidEmails() {
        assertTrue(notificationService.isValidEmail("valid@example.com"));
        assertTrue(notificationService.isValidEmail("user.name+tag@domain.co.uk"));
    }
}