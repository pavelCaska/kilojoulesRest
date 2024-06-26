package com.pc.kilojoulesrest.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
//    @NotBlank(message = "Username is empty or missing.")
    @Size(min = 6, message = "Username must be at least 6 characters long.")
    private String username;

    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$", message = "Password must consists of a minimum of 6 letters and/or digits.")
    private String password;
}
