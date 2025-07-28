package com.example.user_service.controller;

import com.example.user_service.dto.UserUpdateRequest;
import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Запрос на получение всех пользователей");
        List<User> users = userService.getAllUsers();
        logger.debug("Найдено пользователей: {}", users.size());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> editUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        logger.info("Запрос на обновление пользователя с ID: {}", id);

        try {
            User user = userService.findById(id);
            logger.debug("Найден пользователь для обновления: {}", user.getEmail());

            userService.editUser(user, request);
            logger.info("Пользователь с ID {} успешно обновлен", id);

            return ResponseEntity.ok("User has been updated successfully");
        } catch (Exception e) {
            logger.error("Ошибка при обновлении пользователя с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Ошибка сервера");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        logger.info("Запрос на удаление пользователя с ID: {}", id);

        try {
            User user = userService.findById(id);
            logger.debug("Найден пользователь для удаления: {}", user.getEmail());

            userService.deleteUser(user);
            logger.info("Пользователь с ID {} успешно удален", id);

            return ResponseEntity.ok("User has been deleted successfully");
        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Ошибка сервера");
        }
    }
}