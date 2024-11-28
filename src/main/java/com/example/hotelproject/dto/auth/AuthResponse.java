package com.example.hotelproject.dto.auth;

import com.example.hotelproject.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String email;
    private UserRole role;
}
