package com.an.identityservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.an.identityservice.dto.request.UserCreationRequest;
import com.an.identityservice.dto.request.UserUpdateRequest;
import com.an.identityservice.dto.response.UserResponse;
import com.an.identityservice.entity.User;

@Mapper(componentModel = "spring") // báo cho MapStruct tạo bean cho mapper này
public interface UserMapper {
    User toUser(UserCreationRequest userCreationRequest);

    @Mapping(
            target = "roles",
            ignore = true) // Bỏ qua trường roles khi cập nhật, vì chúng ta sẽ không cập nhật roles thông qua
    // UserUpdateRequest
    void updateUser(@MappingTarget User user, UserUpdateRequest userUpdateRequest);

    // @Mapping(source = "firstName", target = "lastName") // Để map trường firstName của User sang lastName của
    // UserResponse
    // @Mapping(target = "lastName", ignore = true) // Bỏ qua trường lastName trong UserResponse
    UserResponse toUserResponse(User user);
}
