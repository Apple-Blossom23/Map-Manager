package com.workshop.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String bio;
    private String role;
    private Integer level;
    private Integer lightning;
    private Integer drops;
    private String inviteCode;
    private String status;
    private LocalDateTime createdAt;
}
