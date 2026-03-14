package com.an.identityservice.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.an.identityservice.dto.request.UserCreationRequest;
import com.an.identityservice.dto.response.UserResponse;
import com.an.identityservice.entity.User;
import com.an.identityservice.exception.AppException;
import com.an.identityservice.repository.UserRepository;

@SpringBootTest
@TestPropertySource(
        "/test.properties") // Đọc cấu hình file để overide file application.yaml, ví dụ như cấu hình datasource để kết
// nối đến database test thay vì database production
public class UserServiceTest {

    @Autowired
    UserService userService;

    @MockitoBean
    UserRepository userRepository;

    private UserCreationRequest userCreationRequest;
    private UserResponse userResponse;
    private User user;
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

        user = User.builder()
                .id("5516fa1392ba")
                .username("annn")
                .firstName("An")
                .lastName("Nguyen")
                .dob(dob)
                .build();
    }

    @Test
    void createUser_validRequest_success() {
        // GIVEN
        when(userRepository.existsByUsername(userCreationRequest.getUsername())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // WHEN
        UserResponse response = userService.createUser(userCreationRequest);

        // THEN
        assertThat(response.getId()).isEqualTo("5516fa1392ba");
        assertThat(response.getUsername()).isEqualTo(userCreationRequest.getUsername());
    }

    @Test
    void createUser_userExisted_fail() {
        // GIVEN
        when(userRepository.existsByUsername(userCreationRequest.getUsername())).thenReturn(true);

        // WHEN
        var exception = Assertions.assertThrows(AppException.class, () -> userService.createUser(userCreationRequest));
        assertThat(exception.getErrorCode().getCode()).isEqualTo(1001); // USER_EXISTS
    }

    @Test
    @WithMockUser(
            username = "annn") // Giả lập một user đã đăng nhập với username là "annn" để test phương thức getMyInfo, vì
    // phương thức này cần lấy thông tin user từ SecurityContext
    void getMyInfo_valid_success() {
        // GIVEN
        when(userRepository.findByUsername(userCreationRequest.getUsername())).thenReturn(Optional.of(user));

        // WHEN
        UserResponse response = userService.getMyInfo();

        // THEN
        assertThat(response.getUsername()).isEqualTo(userCreationRequest.getUsername());
        assertThat(response.getId()).isEqualTo("5516fa1392ba");
    }

    @Test
    @WithMockUser(
            username = "annn") // Giả lập một user đã đăng nhập với username là "annn" để test phương thức getMyInfo, vì
    // phương thức này cần lấy thông tin user từ SecurityContext
    void getMyInfo_userNotExist_fail() {
        // GIVEN
        when(userRepository.findByUsername(userCreationRequest.getUsername())).thenReturn(Optional.ofNullable(null));

        // WHEN
        var exception = Assertions.assertThrows(AppException.class, () -> userService.getMyInfo());
        assertThat(exception.getErrorCode().getCode()).isEqualTo(1006); // USER_NOT_EXISTS
    }
}
