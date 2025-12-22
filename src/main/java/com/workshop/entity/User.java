package com.workshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, length = 50)
    private String nickname;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    private String avatar;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;
    
    @Column(nullable = false)
    private Integer level = 0;
    
    @Column(nullable = false)
    private Integer lightning = 0;
    
    @Column(nullable = false)
    private Integer drops = 0;
    
    @Column(name = "invite_code", nullable = false, unique = true, length = 8)
    private String inviteCode;
    
    @Column(name = "inviter_id")
    private Long inviterId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column(name = "ban_reason", columnDefinition = "TEXT")
    private String banReason;
    
    @Column(name = "registration_ip", length = 45)
    private String registrationIp;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum UserRole {
        USER, CREATOR, ADMIN
    }
    
    public enum UserStatus {
        ACTIVE, BANNED
    }
}
