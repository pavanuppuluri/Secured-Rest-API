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
public class UserUpdateDto {

    @NotBlank
    @Size(min = 6, max = 45, message = "User name must be between 6 and 45 characters long")
    private String username;

    @NotBlank
    @Size(max = 100, message = "First name must be less than 100 characters long")
    private String firstname;

    @NotBlank
    @Size(max = 100, message = "Last name must be less than 100 characters long")
    private String lastname;

    @Email
    private String email;
}
