package com.workshop.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;
    
    @Size(max = 500, message = "简介不能超过500个字符")
    private String bio;
}
