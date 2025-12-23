package com.workshop.service;

import com.workshop.dto.user.*;
import com.workshop.entity.User;
import com.workshop.exception.BusinessException;
import com.workshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemConfigService systemConfigService;
    private final MinioService minioService;
    
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        return mapToProfileResponse(user);
    }
    
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            user.setNickname(request.getNickname().trim());
        }
        
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        
        user = userRepository.save(user);
        return mapToProfileResponse(user);
    }
    
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("旧密码错误");
        }
        
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    @Transactional
    public void changeEmail(Long userId, ChangeEmailRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("密码错误");
        }
        
        if (user.getEmail().equals(request.getNewEmail())) {
            throw new BusinessException("新邮箱不能与当前邮箱相同");
        }
        
        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new BusinessException("该邮箱已被使用");
        }
        
        String emailDomain = request.getNewEmail().substring(request.getNewEmail().indexOf("@") + 1);
        if (!systemConfigService.isEmailDomainAllowed(emailDomain)) {
            throw new BusinessException("该邮箱后缀不允许使用");
        }
        
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
    }
    
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只支持图片文件");
        }
        
        // 验证文件大小 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }
        
        // 获取文件扩展名
        String extension = contentType.substring(contentType.indexOf("/") + 1);
        if ("jpeg".equals(extension)) {
            extension = "jpg";
        }
        
        // 如果用户已有头像，先删除旧头像
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            minioService.deleteFile(user.getAvatar());
        }
        
        // 使用有意义的文件名: avatars/{username}_{userId}_{timestamp}.{extension}
        String timestamp = String.valueOf(System.currentTimeMillis());
        String objectName = "avatars/" + user.getUsername() + "_" + userId + "_" + timestamp + "." + extension;
        
        // 上传到MinIO
        minioService.uploadFile(objectName, file);
        
        // 保存完整对象路径到数据库
        user.setAvatar(objectName);
        userRepository.save(user);
        
        // 返回后端代理URL，带时间戳以破坏缓存
        return "/api/user/avatar/" + userId + "?t=" + timestamp;
    }
    
    @Transactional
    public void removeAvatar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            // 从MinIO删除文件
            minioService.deleteFile(user.getAvatar());
        }
        
        user.setAvatar(null);
        userRepository.save(user);
    }
    
    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        // 返回后端代理URL而不是直接的MinIO URL
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            // 从文件名中提取时间戳（格式：avatars/username_userId_timestamp.ext）
            String avatarPath = user.getAvatar();
            String timestamp = extractTimestampFromPath(avatarPath);
            response.setAvatar("/api/user/avatar/" + user.getId() + "?t=" + timestamp);
        } else {
            response.setAvatar(null);
        }
        response.setBio(user.getBio());
        response.setRole(user.getRole().name());
        response.setLevel(user.getLevel());
        response.setLightning(user.getLightning());
        response.setDrops(user.getDrops());
        response.setInviteCode(user.getInviteCode());
        response.setStatus(user.getStatus().name());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
    
    private String extractTimestampFromPath(String path) {
        // 从路径中提取时间戳：avatars/username_userId_timestamp.ext -> timestamp
        try {
            String filename = path.substring(path.lastIndexOf("/") + 1);
            String nameWithoutExt = filename.substring(0, filename.lastIndexOf("."));
            String[] parts = nameWithoutExt.split("_");
            if (parts.length >= 3) {
                return parts[2]; // timestamp部分
            }
        } catch (Exception e) {
            // 如果解析失败，返回当前时间戳
        }
        return String.valueOf(System.currentTimeMillis());
    }
}
