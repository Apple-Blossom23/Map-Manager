package com.workshop.service;

import com.workshop.dto.user.*;
import com.workshop.entity.User;
import com.workshop.exception.BusinessException;
import com.workshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemConfigService systemConfigService;
    
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
        
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().trim());
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
    
    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
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
}
