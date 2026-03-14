package com.an.identityservice.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.an.identityservice.dto.request.PermissionRequest;
import com.an.identityservice.dto.response.PermissionResponse;
import com.an.identityservice.entity.Permission;
import com.an.identityservice.mapper.PermissionMapper;
import com.an.identityservice.repository.PermissionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class PermissionService {

    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse createPermission(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    // id là name của permission
    public void deletePermission(String permission) {
        permissionRepository.deleteById(permission);
    }
}
