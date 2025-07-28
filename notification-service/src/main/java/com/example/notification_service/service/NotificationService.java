package com.example.notification_service.service;

import com.example.notification_service.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender mailSender;
    public static final String SENDER_EMAIL = "ofbrick121@gmail.com";

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAdminNotification(NotificationRequest request) {
        List<String> adminEmails = request.getAdminEmails();

        if (adminEmails == null || adminEmails.isEmpty()) {
            logger.warn("Пустой список адресов для отправки уведомления");
            throw new IllegalArgumentException("Список email администраторов пуст");
        }

        logger.info("Начало отправки уведомления администраторам. Количество получателей: {}", adminEmails.size());
        logger.debug("Тема уведомления: '{}'", request.getSubject());

        int successCount = 0;
        int failCount = 0;

        for (String email : adminEmails) {
            try {
                if (!isValidEmail(email)) {
                    logger.warn("Некорректный email адрес: {}", email);
                    failCount++;
                    continue;
                }

                SimpleMailMessage message = createEmailMessage(email, request);
                mailSender.send(message);
                successCount++;

                logger.debug("Уведомление успешно отправлено на: {}", email);
            } catch (MailException e) {
                failCount++;
                logger.error("Ошибка при отправке уведомления на {}: {}", email, e.getMessage());
            }
        }

        logger.info("Итог отправки: успешно - {}, с ошибками - {}", successCount, failCount);

        if (failCount > 0) {
            logger.warn("Не все уведомления были доставлены. Проблемные адреса: {} из {}",
                    failCount, adminEmails.size());
        }
    }

    public SimpleMailMessage createEmailMessage(String email, NotificationRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(SENDER_EMAIL);
        message.setTo(email);
        message.setSubject(request.getSubject());
        message.setText(request.getMessage());
        return message;
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}