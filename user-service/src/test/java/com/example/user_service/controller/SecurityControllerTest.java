package com.example.user_service.controller;

import com.example.user_service.config.JwtCore;
import com.example.user_service.dto.AuthResponse;
import com.example.user_service.dto.SigninRequest;
import com.example.user_service.dto.SignupRequest;
import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtCore jwtCore;

    @InjectMocks
    private SecurityController securityController;

    private SigninRequest signinRequest;
    private SignupRequest signupRequest;
    private User testUser;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        signinRequest = new SigninRequest();
        signinRequest.setUsername("testuser");
        signinRequest.setPassword("password");

        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setPassword("password");
        signupRequest.setEmail("new@example.com");
        signupRequest.setFirstName("New");
        signupRequest.setLastName("User");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        authentication = mock(Authentication.class);
    }

    @Test
    void signIn_ShouldReturnAuthResponse_WhenCredentialsValid() {
        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(jwtCore.generateToken(authentication))
                .thenReturn("test.jwt.token");
        when(userService.findByUsername("testuser"))
                .thenReturn(testUser);

        ResponseEntity<AuthResponse> response = securityController.signIn(signinRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test.jwt.token", response.getBody().getToken());
        assertEquals(1L, response.getBody().getUser().getId());
        assertEquals("testuser", response.getBody().getUser().getUsername());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtCore).generateToken(authentication);
    }

    @Test
    void signIn_ShouldReturnUnauthorized_WhenCredentialsInvalid() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<AuthResponse> response = securityController.signIn(signinRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void signUp_ShouldReturnSuccessMessage_WhenRegistrationSuccessful() {
        doNothing().when(userService).registerUser(any(SignupRequest.class));

        ResponseEntity<String> response = securityController.signUp(signupRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("You are successfully signed up!", response.getBody());
        verify(userService).registerUser(signupRequest);
    }

    @Test
    void signUp_ShouldReturnBadRequest_WhenRegistrationFails() {
        doThrow(new RuntimeException("Username already exists"))
                .when(userService).registerUser(any(SignupRequest.class));

        ResponseEntity<String> response = securityController.signUp(signupRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Registration failed"));
        verify(userService).registerUser(signupRequest);
    }
}