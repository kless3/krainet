package com.example.notification_service.consumer;

import com.example.notification_service.dto.NotificationRequest;
import com.example.notification_service.service.JsonService;
import com.example.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    private final NotificationService notificationService;
    private final JsonService jsonService;

    public MessageConsumer(NotificationService notificationService, JsonService jsonService) {
        this.notificationService = notificationService;
        this.jsonService = jsonService;
    }

    @KafkaListener(topics = "service-requests", groupId = "my-group")
    public void listen(String message) {

        NotificationRequest request = jsonService.fromJson(message, NotificationRequest.class);
        System.out.println(request.toString());
        logger.info("Получен запрос на отправку уведомления администраторам");
        logger.debug("Детали запроса: получатели={}, тема='{}', сообщение='{}'",
                request.getAdminEmails(),
                request.getSubject(),
                request.getMessage());

        try {
            notificationService.sendAdminNotification(request);
            logger.info("Уведомление успешно отправлено {} администраторам",
                    request.getAdminEmails().size());
            logger.debug("Тема уведомления: '{}'", request.getSubject());

        } catch (IllegalArgumentException e) {
            logger.warn("Некорректные параметры уведомления: {}", e.getMessage());

        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомления администраторам: {}", e.getMessage(), e);
        }
    }
}