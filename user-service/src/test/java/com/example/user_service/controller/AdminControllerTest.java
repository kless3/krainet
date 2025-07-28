package com.example.user_service.controller;

import com.example.user_service.dto.UserUpdateRequest;
import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    private User testUser;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        updateRequest = new UserUpdateRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPassword("newpassword");
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("User");
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        when(userService.getAllUsers()).thenReturn(List.of(testUser));

        ResponseEntity<List<User>> response = adminController.getAllUsers();

        assertEquals(1, response.getBody().size());
        assertEquals(testUser, response.getBody().get(0));
        assertEquals(200, response.getStatusCodeValue());
        verify(userService).getAllUsers();
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList() {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        ResponseEntity<List<User>> response = adminController.getAllUsers();

        assertTrue(response.getBody().isEmpty());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void editUser_ShouldUpdateUserSuccessfully() {
        when(userService.findById(1L)).thenReturn(testUser);

        ResponseEntity<String> response = adminController.editUser(1L, updateRequest);

        assertEquals("User has been updated successfully", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        verify(userService).editUser(testUser, updateRequest);
    }

    @Test
    void editUser_ShouldReturnErrorWhenUserNotFound() {
        when(userService.findById(1L)).thenThrow(new RuntimeException("User not found"));

        ResponseEntity<String> response = adminController.editUser(1L, updateRequest);

        assertEquals("Ошибка сервера", response.getBody());
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void deleteUser_ShouldDeleteUserSuccessfully() {
        when(userService.findById(1L)).thenReturn(testUser);

        ResponseEntity<String> response = adminController.deleteUser(1L);

        assertEquals("User has been deleted successfully", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        verify(userService).deleteUser(testUser);
    }

    @Test
    void deleteUser_ShouldReturnErrorWhenUserNotFound() {
        when(userService.findById(1L)).thenThrow(new RuntimeException("User not found"));

        ResponseEntity<String> response = adminController.deleteUser(1L);

        assertEquals("Ошибка сервера", response.getBody());
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void deleteUser_ShouldReturnErrorWhenDeleteFails() {
        when(userService.findById(1L)).thenReturn(testUser);
        doThrow(new RuntimeException("Delete failed")).when(userService).deleteUser(testUser);

        ResponseEntity<String> response = adminController.deleteUser(1L);

        assertEquals("Ошибка сервера", response.getBody());
        assertEquals(500, response.getStatusCodeValue());
    }
}