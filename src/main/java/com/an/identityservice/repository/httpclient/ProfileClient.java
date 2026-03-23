package com.an.identityservice.repository.httpclient;

import com.an.identityservice.dto.request.ProfileCreationRequest;
import com.an.identityservice.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//
@FeignClient(name = "profile-service", url = "${app.services.profile}")
public interface ProfileClient {
    @PostMapping(value = "/internal/users", produces = "application/json")
    UserProfileResponse createProfile(@RequestBody ProfileCreationRequest profileCreationRequest);

}
