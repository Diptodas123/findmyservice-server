package com.FindMyService.model.dto;

import com.FindMyService.model.enums.Role;
import lombok.Data;

@Data
public class RegisterRequestDto {
    private String name;
    private String email;
    private String password;
    private Role role;
}
