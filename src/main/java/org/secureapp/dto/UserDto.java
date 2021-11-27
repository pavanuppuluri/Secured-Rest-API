package org.secureapp.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserDto {

    @NotBlank
    @Size(min = 6, max = 45, message = "User name must be between 6 and 45 characters long")
    private String username;

    @NotBlank
    @Size(min = 8, max = 200, message = "Password must be between 8 and 200 characters long")
    private String password;

    @NotBlank
    @Size(max = 100, message = "First name must be less than 100 characters long")
    private String firstname;

    @NotBlank
    @Size(max = 100, message = "Last name must be less than 100 characters long")
    private String lastname;

    @Email
    private String email;
}
