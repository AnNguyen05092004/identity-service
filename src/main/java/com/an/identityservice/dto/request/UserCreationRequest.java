package com.an.identityservice.dto.request;

import com.an.identityservice.validator.DobConstraint;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 4, message = "USERNAME_INVALID")
    String username;

    @Size(min = 6, message = "PASSWORD_INVALID")
    String password;
    String firstName;
    String lastName;

    // Custom annotation
    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;
}
