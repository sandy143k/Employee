package com.example.EmpApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String email;
    private String accessToken;
    private String refreshToken;
    private Date accessTokenExpiry;
}