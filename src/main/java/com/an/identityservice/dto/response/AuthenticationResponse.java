package com.an.identityservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.security.SecureRandom;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String token;
    boolean authenticated;

}
