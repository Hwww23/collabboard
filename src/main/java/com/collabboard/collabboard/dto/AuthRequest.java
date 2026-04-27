package com.collabboard.collabboard.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
    private String displayName;  // only used for register
}