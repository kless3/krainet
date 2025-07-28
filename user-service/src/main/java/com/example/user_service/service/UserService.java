package com.example.user_service.service;

import com.example.user_service.client.MessageProducer;
import com.example.user_service.dto.NotificationRequest;
import com.example.user_service.dto.SignupRequest;
import com.example.user_service.dto.UserProfileResponse;
import com.example.user_service.dto.UserUpdateRequest;
import com.example.user_service.entity.Role;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.example.user_service.service.JsonConverter.convertToJson;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private MessageProducer messageProducer;

    public void sendRequest(NotificationRequest request) {
        String jsonRequest = convertToJson(request);
        messageProducer.sendMessage(jsonRequest);
    }

    public UserService(PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       MessageProducer messageProducer) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.messageProducer = messageProducer;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Поиск пользователя по username: {}", username);
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("Пользователь {} не найден", username);
                        return new UsernameNotFoundException("Username not found");
                    });
            logger.info("Успешно загружен пользователь: {}", username);
            return UserDetailsImpl.build(user);
        } catch (Exception e) {
            logger.error("Ошибка загрузки пользователя {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    public void registerUser(SignupRequest request) {
        logger.info("Начало регистрации пользователя: {}", request.getUsername());

        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                logger.warn("Попытка регистрации занятого username: {}", request.getUsername());
                throw new RuntimeException("Username in use");
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                logger.warn("Попытка регистрации занятого email: {}", request.getEmail());
                throw new RuntimeException("Email in use");
            }

            User user = new User();
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            userRepository.save(user);
            logger.info("Пользователь {} успешно зарегистрирован", user.getUsername());

            List<String> adminEmails = getAdminEmails();
            logger.debug("Отправка уведомления администраторам: {}", adminEmails);

            sendRequest(new NotificationRequest(
                    adminEmails,
                    "Создан пользователь " + request.getUsername(),
                    "Создан пользователь с именем - " + request.getUsername() +
                            " и почтой - " + request.getEmail()));

        } catch (Exception e) {
            logger.error("Ошибка регистрации пользователя {}: {}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    public List<String> getAdminEmails() {
        logger.debug("Получение email администраторов");
        try {
            List<String> emails = new ArrayList<>();
            List<User> admins = userRepository.findByRole(Role.ADMIN);

            admins.forEach(admin -> emails.add(admin.getEmail()));
            logger.debug("Найдено {} администраторов", admins.size());

            return emails;
        } catch (Exception e) {
            logger.error("Ошибка получения email администраторов: {}", e.getMessage(), e);
            throw e;
        }
    }

    public User findById(Long id) {
        logger.debug("Поиск пользователя по ID: {}", id);
        try {
            return userRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Пользователь с ID {} не найден", id);
                        return new UsernameNotFoundException("User not found");
                    });
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public User findByUsername(String username) {
        logger.debug("Поиск пользователя по username: {}", username);
        try {
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("Пользователь {} не найден", username);
                        return new UsernameNotFoundException("User not found");
                    });
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    public UserProfileResponse getUserProfileByUsername(String username) {
        logger.info("Формирование профиля для пользователя: {}", username);
        try {
            User user = findByUsername(username);
            UserProfileResponse response = new UserProfileResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole());

            logger.debug("Сформирован профиль: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Ошибка формирования профиля {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    public void editUser(User user, UserUpdateRequest request) {
        logger.info("Редактирование пользователя: {}", user.getUsername());
        logger.debug("Новые данные: {}", request);

        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                logger.warn("Попытка изменения на занятый username: {}", request.getUsername());
                throw new RuntimeException("Username in use");
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                logger.warn("Попытка изменения на занятый email: {}", request.getEmail());
                throw new RuntimeException("Email in use");
            }

            String oldUsername = user.getUsername();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            userRepository.save(user);
            logger.info("Пользователь {} успешно обновлен", oldUsername);

            sendRequest(new NotificationRequest(
                    getAdminEmails(),
                    "Изменен пользователь " + oldUsername,
                    "Изменен пользователь с именем - " + request.getUsername() +
                            " и почтой - " + request.getEmail()));

        } catch (Exception e) {
            logger.error("Ошибка редактирования пользователя {}: {}", user.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    public void deleteUser(User user) {
        logger.warn("Инициировано удаление пользователя: {}", user.getUsername());
        try {
            userRepository.delete(user);
            logger.info("Пользователь {} успешно удален", user.getUsername());

            sendRequest(new NotificationRequest(
                    getAdminEmails(),
                    "Удален пользователь " + user.getUsername(),
                    "Удален пользователь с именем - " + user.getUsername() +
                            " и почтой - " + user.getEmail()));

        } catch (Exception e) {
            logger.error("Ошибка удаления пользователя {}: {}", user.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    public List<User> getAllUsers() {
        logger.debug("Запрос всех пользователей");
        try {
            List<User> users = userRepository.findAll();
            logger.info("Найдено {} пользователей", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Ошибка получения списка пользователей: {}", e.getMessage(), e);
            throw e;
        }
    }
}