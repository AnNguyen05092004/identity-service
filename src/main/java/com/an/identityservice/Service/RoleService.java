package com.an.identityservice.Service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import com.an.identityservice.dto.request.RoleRequest;
import com.an.identityservice.dto.response.RoleResponse;
import com.an.identityservice.mapper.RoleMapper;
import com.an.identityservice.repository.PermissionRepository;
import com.an.identityservice.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
// Tự động tạo constructor với tất cả các trường được đánh dấu là final => không cần phải sử dụng @Autowired nữa, Spring
// sẽ tự động inject các dependency thông qua constructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;
    PermissionRepository permissionRepository;

    public RoleResponse createRole(RoleRequest request) {
        var role = roleMapper.toRole(request);

        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    public void deleteRole(String role) {
        roleRepository.deleteById(role);
    }
}
