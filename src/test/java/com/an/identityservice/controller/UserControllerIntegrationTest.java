package com.an.identityservice.controller;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.an.identityservice.dto.request.UserCreationRequest;
import com.an.identityservice.dto.response.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

// Integration test: kiểm tra sự tương tác giữa các thành phần của ứng dụng, ví dụ như controller, service, repository
// và database. Mục tiêu là đảm bảo rằng các thành phần này hoạt động cùng nhau một cách chính xác.
@Slf4j
@SpringBootTest // Load the full application context for integration testing
@AutoConfigureMockMvc // tự động cấu hình MockMvc để test controller
@Testcontainers //
public class UserControllerIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource // giống như application.properties, nhưng giá trị được tạo động trong runtime.
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private MockMvc
            mockMvc; // MockMvc cho phép chúng ta mô phỏng các yêu cầu HTTP đến controller mà không cần phải khởi động
    // server thực sự.

    private UserCreationRequest userCreationRequest;
    private UserResponse userResponse;
    private LocalDate dob;

    @BeforeEach
    // chạy trước mỗi test method
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

        // WHEN, THEN:
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.username").value("annn"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.firstName").value("An"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.lastName").value("Nguyen"));

        log.info("Result: {}", response.andReturn().getResponse().getContentAsString());
    }
}
