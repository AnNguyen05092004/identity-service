package com.an.identityservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
// DTO for logout request, containing the token to be invalidated
public class LogoutRequest {
    String token;
}
