package com.workshop.service;

import com.workshop.dto.auth.AuthResponse;
import com.workshop.dto.auth.LoginRequest;
import com.workshop.dto.auth.RegisterRequest;
import com.workshop.entity.User;
import com.workshop.exception.BusinessException;
import com.workshop.repository.UserRepository;
import com.workshop.security.JwtTokenProvider;
import com.workshop.util.InviteCodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SystemConfigService systemConfigService;
    private final TransactionService transactionService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (!systemConfigService.isRegistrationEnabled()) {
            throw new BusinessException("注册功能已关闭");
        }
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        
        String emailDomain = request.getEmail().substring(request.getEmail().indexOf("@") + 1);
        if (!systemConfigService.isEmailDomainAllowed(emailDomain)) {
            throw new BusinessException("该邮箱后缀不允许注册");
        }
        
        String clientIp = getClientIp(httpRequest);
        long ipRegistrationCount = userRepository.countByRegistrationIpAndDate(clientIp, LocalDate.now());
        int dailyLimit = systemConfigService.getDailyIpRegistrationLimit();
        if (ipRegistrationCount >= dailyLimit) {
            throw new BusinessException("该IP今日注册次数已达上限");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRegistrationIp(clientIp);
        
        String inviteCode;
        do {
            inviteCode = InviteCodeGenerator.generate();
        } while (userRepository.existsByInviteCode(inviteCode));
        user.setInviteCode(inviteCode);
        
        if (request.getInviteCode() != null && !request.getInviteCode().isEmpty()) {
            User inviter = userRepository.findByInviteCode(request.getInviteCode())
                    .orElseThrow(() -> new BusinessException("邀请码无效"));
            user.setInviterId(inviter.getId());
        }
        
        user = userRepository.save(user);
        
        if (user.getInviterId() != null) {
            handleInviteReward(user.getId(), user.getInviterId());
        }
        
        String token = jwtTokenProvider.generateToken(
                user.getId(), 
                user.getUsername(), 
                user.getRole().name()
        );
        
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getNickname(), user.getRole().name());
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (!systemConfigService.isLoginEnabled()) {
            throw new BusinessException("登录功能已关闭");
        }
        
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new BusinessException("账号已被封禁: " + user.getBanReason());
        }
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        String token = jwtTokenProvider.generateToken(
                user.getId(), 
                user.getUsername(), 
                user.getRole().name()
        );
        
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getNickname(), user.getRole().name());
    }
    
    private void handleInviteReward(Long inviteeId, Long inviterId) {
        transactionService.addDrops(inviteeId, 20, "invite", inviterId, "被邀请奖励");
        transactionService.addDrops(inviterId, 10, "invite", inviteeId, "邀请奖励");
        transactionService.addLightning(inviterId, 10, "invite", inviteeId, "邀请奖励");
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
