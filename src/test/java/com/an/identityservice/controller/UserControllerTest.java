package com.an.identityservice.controller;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.an.identityservice.Service.UserService;
import com.an.identityservice.dto.request.UserCreationRequest;
import com.an.identityservice.dto.response.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

// Nguyên tắc: Khi test layer trên thì mock layer dưới,
// Tức là khi test controller thì mock service, khi test service thì mock repository. Điều này giúp chúng ta tập trung
// vào việc test logic của layer hiện tại mà không bị ảnh hưởng bởi các layer khác.

@Slf4j
@SpringBootTest // Load the full application context for integration testing
@AutoConfigureMockMvc // tự động cấu hình MockMvc để test controller
@TestPropertySource(
        "/test.properties") // Đọc cấu hình file để overide file application.yaml, ví dụ như cấu hình datasource để kết
// nối đến database test thay vì database production
public class UserControllerTest {

    @Autowired
    private MockMvc
            mockMvc; // MockMvc cho phép chúng ta mô phỏng các yêu cầu HTTP đến controller mà không cần phải khởi động
    // server thực sự.

    @MockitoBean // //thay bean UserService thật bằng một Mockito mock.
    private UserService userService;

    private UserCreationRequest userCreationRequest;
    private UserResponse userResponse;
    private LocalDate dob;

    @BeforeEach // chạy trước mỗi test method
    void initData() {
        dob = LocalDate.of(1990, 1, 1);
        userCreationRequest = UserCreationRequest.builder()
                .username("annn")
                .firstName("An")
                .lastName("Nguyen")
                .password("123456")
                .dob(dob)
                .build();

        userResponse = UserResponse.builder()
                .id("5516fa1392ba")
                .username("annn")
                .firstName("An")
                .lastName("Nguyen")
                .dob(dob)
                .build();
    }

    @Test
    //
    void createUser_validRequest_success() throws Exception {
        log.info("Create User test executed");

        // GIVEN:
        ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper dùng để convert object Java -> JSON.
        objectMapper.registerModule(
                new JavaTimeModule()); // Đăng ký module để hỗ trợ serializing/deserializing LocalDate
        String content = objectMapper.writeValueAsString(userCreationRequest);

        Mockito.when(userService.createUser(ArgumentMatchers.any())).thenReturn(userResponse);

        // WHEN, THEN:
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value("5516fa1392ba"));
    }

    @Test
    void createUser_usernameInvalid_fail() throws Exception {
        // GIVEN:
        userCreationRequest.setUsername("an"); // test username quá ngắn
        ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper dùng để convert object Java -> JSON.
        objectMapper.registerModule(
                new JavaTimeModule()); // Đăng ký module để hỗ trợ serializing/deserializing LocalDate
        String content = objectMapper.writeValueAsString(userCreationRequest);

        // WHEN, THEN:
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1003))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Username must be at least 4 characters"));
    }
}
