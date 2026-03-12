package com.an.identityservice.mapper;

import com.an.identityservice.dto.request.RoleRequest;
import com.an.identityservice.dto.response.RoleResponse;
import com.an.identityservice.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.lang.annotation.Target;

@Mapper(componentModel = "spring") // báo cho MapStruct tạo bean cho mapper này
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true) // Bỏ qua trường permissions khi mapping từ RoleRequest sang Role
    Role toRole(RoleRequest roleRequest);

    RoleResponse toRoleResponse(Role role);
}
