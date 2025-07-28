package com.example.user_service.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
}
