package com.staykonnect.backend.dto;

import com.staykonnect.backend.entity.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
}
