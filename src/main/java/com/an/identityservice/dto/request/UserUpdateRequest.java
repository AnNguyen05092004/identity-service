package com.an.identityservice.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.an.identityservice.validator.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    String firstName;
    String lastName;

    @DobConstraint(min = 18, message = "INVALID_DOB") // Custom annotation
    LocalDate dob;

    List<String> roles;
}
