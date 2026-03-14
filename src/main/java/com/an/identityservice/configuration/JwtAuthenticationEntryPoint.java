package com.an.identityservice.configuration;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.an.identityservice.dto.response.ApiResponse;
import com.an.identityservice.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Khi client gọi API mà không có JWT hoặc JWT sai, Spring Security sẽ gọi class này để trả về response lỗi dạng JSON
// Phân biệt: - chưa login -> gọi JwtAuthenticationEntryPoint để trả về lỗi Unauthenticated
//              - đã login nhưng không có quyền truy cập -> gọi AccessDeniedHandler để trả về lỗi Unauthorized
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}
