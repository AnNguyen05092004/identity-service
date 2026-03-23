package com.an.identityservice.Service;

import java.util.HashSet;
import java.util.List;

import com.an.identityservice.mapper.ProfileMapper;
import com.an.identityservice.repository.httpclient.ProfileClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.an.identityservice.dto.request.UserCreationRequest;
import com.an.identityservice.dto.request.UserUpdateRequest;
import com.an.identityservice.dto.response.UserResponse;
import com.an.identityservice.entity.User;
import com.an.identityservice.enums.Role;
import com.an.identityservice.exception.AppException;
import com.an.identityservice.exception.ErrorCode;
import com.an.identityservice.mapper.UserMapper;
import com.an.identityservice.repository.RoleRepository;
import com.an.identityservice.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor // Tự động tạo constructor với tất cả các trường được đánh dấu là final => không cần phải sử
// dụng @Autowired nữa, Spring sẽ tự động inject các dependency thông qua constructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    //    @Autowired
    //    private final
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    ProfileClient profileClient;
    ProfileMapper profileMapper;

    public UserResponse createUser(UserCreationRequest userCreationRequest) {
        log.info("Service: create user");

        //        if (userRepository.existsByUsername(userCreationRequest.getUsername())) {
        //            throw new AppException(ErrorCode.USER_EXISTS);
        //        }

        User user = userMapper.toUser(userCreationRequest);
        user.setPassword(passwordEncoder.encode(userCreationRequest.getPassword()));

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        // user.setRoles(roles);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) { // lỗi duplicate vì đã đặt là unique
            throw new AppException(ErrorCode.USER_EXISTS);
        }
        var profileRequest = profileMapper.toProfileCreationRequest(userCreationRequest);
        profileRequest.setUserId(user.getId());
        var profileResponse = profileClient.createProfile(profileRequest);

        log.info("Service: create profile response: {}", profileResponse);

        return userMapper.toUserResponse(user);
    }

    // @PreAuthorize("hasRole('ADMIN')") // Lọc trc, Chỉ cho phép người dùng có vai trò ADMIN truy cập vào phương thức
    // này
    @PreAuthorize("hasAuthority('APPROVE_POST')") // Phân quyền theo permission.
    public List<UserResponse> getUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize(
            "returnObject.username == authentication.name") // Lọc sau, khi đó đã có giá trị trả về để đem ra so sánh
    public UserResponse getUser(String userid) {
        log.info("Getting user with id: {}", userid);
        return userMapper.toUserResponse(
                userRepository.findById(userid).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS)));
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(String userid, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userid).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        userMapper.updateUser(user, userUpdateRequest);
        user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));

        var roles = roleRepository.findAllById(userUpdateRequest.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userid) {
        userRepository.deleteById(userid);
    }
}
