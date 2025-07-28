package com.example.user_service.service;

import com.example.user_service.client.MessageProducer;
import com.example.user_service.dto.NotificationRequest;
import com.example.user_service.dto.SignupRequest;
import com.example.user_service.dto.UserProfileResponse;
import com.example.user_service.dto.UserUpdateRequest;
import com.example.user_service.entity.Role;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.UserDetailsImpl;
import com.example.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        User user = createTestUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetailsImpl result = (UserDetailsImpl) userService.loadUserByUsername("testuser");

        assertEquals(user.getUsername(), result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotExists() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nonexistent"));
    }

    @Test
    void registerUser_ShouldSaveUser_WhenValidRequest() {
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password");
        request.setFirstName("New");
        request.setLastName("User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(Collections.emptyList());

        userService.registerUser(request);

        verify(userRepository).save(any(User.class));
        verify(messageProducer).sendMessage(anyString());
    }

    @Test
    void registerUser_ShouldThrowException_WhenUsernameExists() {
        SignupRequest request = new SignupRequest();
        request.setUsername("existinguser");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerUser(request));
    }

    @Test
    void getUserProfileByUsername_ShouldReturnProfile_WhenUserExists() {
        User user = createTestUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserProfileResponse result = userService.getUserProfileByUsername("testuser");

        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void editUser_ShouldUpdateUser_WhenValidRequest() {
        User user = createTestUser();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("updateduser");
        request.setEmail("updated@example.com");
        request.setPassword("newpassword");
        request.setFirstName("Updated");
        request.setLastName("User");

        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(Collections.emptyList());

        userService.editUser(user, request);

        assertEquals("updateduser", user.getUsername());
        assertEquals("updated@example.com", user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_ShouldRemoveUser() {
        User user = createTestUser();
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(Collections.emptyList());

        userService.deleteUser(user);

        verify(userRepository).delete(user);
        verify(messageProducer).sendMessage(anyString());
    }

    @Test
    void getAdminEmails_ShouldReturnAdminEmails() {
        User admin = createTestUser();
        admin.setRole(Role.ADMIN);
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of(admin));

        List<String> result = userService.getAdminEmails();

        assertEquals(1, result.size());
        assertEquals("test@example.com", result.get(0));
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
        return user;
    }
}