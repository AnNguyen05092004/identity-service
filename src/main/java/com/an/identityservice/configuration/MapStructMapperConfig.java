package com.an.identityservice.configuration;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.an.identityservice.mapper.PermissionMapper;
import com.an.identityservice.mapper.RoleMapper;
import com.an.identityservice.mapper.UserMapper;

@Configuration
public class MapStructMapperConfig {

    @Bean
    @Primary
    public PermissionMapper permissionMapper() {
        return Mappers.getMapper(PermissionMapper.class);
    }

    @Bean
    @Primary
    public RoleMapper roleMapper() {
        return Mappers.getMapper(RoleMapper.class);
    }

    @Bean
    @Primary
    public UserMapper userMapper() {
        return Mappers.getMapper(UserMapper.class);
    }
}
