package com.an.identityservice.mapper;

import com.an.identityservice.dto.request.PermissionRequest;
import com.an.identityservice.dto.request.UserCreationRequest;
import com.an.identityservice.dto.request.UserUpdateRequest;
import com.an.identityservice.dto.response.PermissionResponse;
import com.an.identityservice.dto.response.UserResponse;
import com.an.identityservice.entity.Permission;
import com.an.identityservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring") // báo cho MapStruct tạo bean cho mapper này
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
