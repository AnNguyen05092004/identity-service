package com.an.identityservice.Service;

import com.an.identityservice.dto.request.UserCreationRequest;
import com.an.identityservice.dto.response.UserResponse;
import com.an.identityservice.entity.User;
import com.an.identityservice.exception.AppException;
import com.an.identityservice.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
}
