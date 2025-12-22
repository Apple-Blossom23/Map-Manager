package com.workshop.controller;

import com.workshop.dto.ApiResponse;
import com.workshop.dto.user.*;
import com.workshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile(@AuthenticationPrincipal Long userId) {
        UserProfileResponse profile = userService.getProfile(userId);
        return ApiResponse.success(profile);
    }
    
    @PutMapping("/profile")
    public ApiResponse<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = userService.updateProfile(userId, request);
        return ApiResponse.success("资料更新成功", profile);
    }
    
    @PostMapping("/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return ApiResponse.success("密码修改成功", null);
    }
    
    @PostMapping("/email")
    public ApiResponse<Void> changeEmail(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangeEmailRequest request) {
        userService.changeEmail(userId, request);
        return ApiResponse.success("邮箱修改成功", null);
    }
}
