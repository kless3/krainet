package com.example.user_service.controller;

import com.example.user_service.dto.UserProfileResponse;
import com.example.user_service.dto.UserUpdateRequest;
import com.example.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/secured")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(Principal principal) {
        if (principal == null) {
            logger.warn("Попытка доступа к профилю без аутентификации");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User not found");
        }

        logger.info("Запрос профиля пользователя: {}", principal.getName());
        try {
            UserProfileResponse userProfile = userService
                    .getUserProfileByUsername(principal.getName());
            logger.debug("Получен профиль для {}: email={}",
                    principal.getName(),
                    userProfile.getEmail());
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            logger.error("Ошибка получения профиля для {}: {}",
                    principal.getName(),
                    e.getMessage(),
                    e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editProfile(Principal principal,
                                         @RequestBody UserUpdateRequest userUpdateRequest) {
        if (principal == null) {
            logger.warn("Попытка редактирования без аутентификации");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User not found");
        }

        logger.info("Запрос на редактирование профиля: {}", principal.getName());
        logger.debug("Данные для обновления: {}", userUpdateRequest);

        try {
            userService.editUser(
                    userService.findByUsername(principal.getName()),
                    userUpdateRequest);

            UserProfileResponse updatedProfile = userService
                    .getUserProfileByUsername(userUpdateRequest.getUsername());

            logger.info("Профиль {} успешно обновлен", principal.getName());
            logger.debug("Обновленные данные: {}", updatedProfile);

            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            logger.error("Ошибка редактирования профиля {}: {}",
                    principal.getName(),
                    e.getMessage(),
                    e);
            return ResponseEntity.internalServerError()
                    .body("Update failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteProfile(Principal principal) {
        if (principal == null) {
            logger.warn("Попытка удаления профиля без аутентификации");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User not found");
        }

        logger.info("Запрос на удаление профиля: {}", principal.getName());

        try {
            userService.deleteUser(
                    userService.findByUsername(principal.getName()));

            logger.info("Профиль {} успешно удален", principal.getName());
            return ResponseEntity.ok("Your account was successfully deleted");
        } catch (Exception e) {
            logger.error("Ошибка удаления профиля {}: {}",
                    principal.getName(),
                    e.getMessage(),
                    e);
            return ResponseEntity.internalServerError()
                    .body("Deletion failed: " + e.getMessage());
        }
    }
}