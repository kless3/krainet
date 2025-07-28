package com.example.user_service.controller;

import com.example.user_service.config.JwtCore;
import com.example.user_service.dto.AuthResponse;
import com.example.user_service.dto.SigninRequest;
import com.example.user_service.dto.SignupRequest;
import com.example.user_service.dto.UserDto;
import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class SecurityController {

    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;

    public SecurityController(UserService userService,
                              AuthenticationManager authenticationManager,
                              JwtCore jwtCore) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtCore = jwtCore;
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@RequestBody SigninRequest request) {
        logger.info("Запрос на вход пользователя: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            String jwt = jwtCore.generateToken(authentication);
            User user = userService.findByUsername(request.getUsername());

            logger.info("Пользователь {} успешно аутентифицирован", request.getUsername());
            logger.debug("Сгенерирован JWT-токен для пользователя: {}", user.getUsername());

            return ResponseEntity.ok(new AuthResponse(
                    jwt,
                    new UserDto(
                            user.getId(),
                            user.getUsername()
                    )
            ));
        } catch (Exception e) {
            logger.error("Ошибка аутентификации для пользователя {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignupRequest request) {
        logger.info("Запрос на регистрацию пользователя: {}", request.getUsername());

        try {
            userService.registerUser(request);
            logger.info("Пользователь {} успешно зарегистрирован", request.getUsername());

            return ResponseEntity.status(HttpStatus.OK)
                    .body("You are successfully signed up!");
        } catch (Exception e) {
            logger.error("Ошибка регистрации пользователя {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Registration failed: " + e.getMessage());
        }
    }
}