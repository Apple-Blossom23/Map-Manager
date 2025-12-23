package com.workshop.controller;

import com.workshop.dto.ApiResponse;
import com.workshop.dto.user.*;
import com.workshop.entity.User;
import com.workshop.exception.BusinessException;
import com.workshop.repository.UserRepository;
import com.workshop.service.MinioService;
import com.workshop.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final MinioService minioService;
    private final UserRepository userRepository;
    
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
    
    @PostMapping("/avatar")
    public ApiResponse<String> uploadAvatar(
            @AuthenticationPrincipal Long userId,
            @RequestParam("file") MultipartFile file) {
        try {
            String avatarUrl = userService.uploadAvatar(userId, file);
            return ApiResponse.success("头像上传成功", avatarUrl);
        } catch (IOException e) {
            return ApiResponse.error("头像上传失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("文件格式不支持");
        }
    }
    
    @DeleteMapping("/avatar")
    public ApiResponse<Void> removeAvatar(@AuthenticationPrincipal Long userId) {
        userService.removeAvatar(userId);
        return ApiResponse.success("头像移除成功", null);
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
    
    @GetMapping("/avatar/{userId}")
    public void getAvatar(@PathVariable Long userId, HttpServletResponse response) throws Exception {
        log.info("Fetching avatar for userId: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        log.info("User found: {}, avatar path: {}", user.getUsername(), user.getAvatar());
        
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            log.warn("User {} has no avatar set", userId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "用户未设置头像");
            return;
        }
        
        // 使用数据库中存储的完整对象路径
        String objectName = user.getAvatar();
        log.info("Attempting to fetch from MinIO: {}", objectName);
        
        try (InputStream stream = minioService.getFile(objectName)) {
            if (stream == null) {
                log.error("File not found in MinIO: {}", objectName);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "头像文件不存在");
                return;
            }
            
            // 从文件名提取扩展名
            String extension = objectName.substring(objectName.lastIndexOf(".") + 1).toLowerCase();
            String contentType = "image/" + extension;
            if ("jpg".equals(extension)) {
                contentType = "image/jpeg";
            } else if ("svg".equals(extension)) {
                contentType = "image/svg+xml";
            }
            
            log.info("Serving avatar with content type: {}", contentType);
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "public, max-age=86400");
            StreamUtils.copy(stream, response.getOutputStream());
            log.info("Avatar served successfully for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error fetching avatar for userId: {}", userId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取头像失败: " + e.getMessage());
        }
    }
}
