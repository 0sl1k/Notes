package ua.glek.notes.Model.Dto;

import lombok.Data;

import java.util.List;

@Data
public class SignUpDto {
    private String email;
    private String username;
    private String password;
    private List<String> roles;
}