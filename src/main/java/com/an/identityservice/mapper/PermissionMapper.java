package com.an.identityservice.mapper;

import org.mapstruct.Mapper;

import com.an.identityservice.dto.request.PermissionRequest;
import com.an.identityservice.dto.response.PermissionResponse;
import com.an.identityservice.entity.Permission;

@Mapper(componentModel = "spring") // báo cho MapStruct tạo bean cho mapper này
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
