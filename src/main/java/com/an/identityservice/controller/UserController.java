package com.an.identityservice.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.an.identityservice.Service.UserService;
import com.an.identityservice.dto.request.UserCreationRequest;
import com.an.identityservice.dto.request.UserUpdateRequest;
import com.an.identityservice.dto.response.ApiResponse;
import com.an.identityservice.dto.response.UserResponse;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;

    // spotless:off
    @PostMapping
    // valid để đảm bảo rằng dữ liệu đầu vào phải tuân thủ các ràng buộc đã được định nghĩa trong UserCreationRequest,
    // ví dụ như độ dài mật khẩu tối thiểu.
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest userCreationRequest) {
        log.debug("Controller: Create User");
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.createUser(userCreationRequest));
        return response;
    }

    //spotless:on
    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Username: {}", authentication.getName());
        authentication.getAuthorities().forEach(authority -> log.info("Authorities: {}", authority.getAuthority()));

        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/{userid}")
    ApiResponse<UserResponse> getUser(@PathVariable String userid) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userid))
                .build();
    }

    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("My Info")
                .result(userService.getMyInfo())
                .build();
    }

    @PutMapping("/{userid}")
    ApiResponse<UserResponse> updateUser(
            @PathVariable String userid, @RequestBody UserUpdateRequest userUpdateRequest) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userid, userUpdateRequest))
                .build();
    }

    @DeleteMapping("/{userid}")
    ApiResponse<Object> deleteUser(@PathVariable String userid) {
        userService.deleteUser(userid);
        return ApiResponse.builder()
                .code(1000)
                .message("User deleted successfully")
                .build();
    }
}
