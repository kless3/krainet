package com.example.user_service.controller;

import com.example.user_service.dto.UserProfileResponse;
import com.example.user_service.dto.UserUpdateRequest;
import com.example.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    @InjectMocks
    private UserController userController;

    private UserProfileResponse profileResponse;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        profileResponse = new UserProfileResponse();
        profileResponse.setUsername("testuser");
        profileResponse.setEmail("test@example.com");

        updateRequest = new UserUpdateRequest();
        updateRequest.setUsername("newusername");
        updateRequest.setEmail("new@example.com");
    }

    @Test
    void getProfile_ShouldReturnProfile_WhenUserAuthenticated() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserProfileByUsername("testuser")).thenReturn(profileResponse);

        ResponseEntity<?> response = userController.getProfile(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profileResponse, response.getBody());
        verify(userService).getUserProfileByUsername("testuser");
    }

    @Test
    void getProfile_ShouldReturnBadRequest_WhenPrincipalNull() {
        ResponseEntity<?> response = userController.getProfile(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void getProfile_ShouldReturnInternalError_WhenServiceFails() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserProfileByUsername("testuser"))
                .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.getProfile(principal);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void editProfile_ShouldUpdateProfile_WhenValidRequest() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserProfileByUsername("newusername")).thenReturn(profileResponse);

        ResponseEntity<?> response = userController.editProfile(principal, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profileResponse, response.getBody());
        verify(userService).editUser(any(), eq(updateRequest));
    }

    @Test
    void editProfile_ShouldReturnBadRequest_WhenPrincipalNull() {
        ResponseEntity<?> response = userController.editProfile(null, updateRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void editProfile_ShouldReturnInternalError_WhenServiceFails() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserProfileByUsername("newusername"))
                .thenThrow(new RuntimeException("Update error"));

        ResponseEntity<?> response = userController.editProfile(principal, updateRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Update failed"));
    }

    @Test
    void deleteProfile_ShouldReturnSuccess_WhenUserExists() {
        when(principal.getName()).thenReturn("testuser");

        ResponseEntity<String> response = userController.deleteProfile(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Your account was successfully deleted", response.getBody());
        verify(userService).deleteUser(any());
    }

    @Test
    void deleteProfile_ShouldReturnBadRequest_WhenPrincipalNull() {
        ResponseEntity<String> response = userController.deleteProfile(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void deleteProfile_ShouldReturnInternalError_WhenServiceFails() {
        when(principal.getName()).thenReturn("testuser");
        doThrow(new RuntimeException("Delete error"))
                .when(userService)
                .deleteUser(any());

        ResponseEntity<String> response = userController.deleteProfile(principal);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Deletion failed"));
    }
}