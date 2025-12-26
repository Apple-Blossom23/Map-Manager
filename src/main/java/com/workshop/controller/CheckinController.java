package com.workshop.controller;

import com.workshop.dto.ApiResponse;
import com.workshop.dto.checkin.CheckinResponse;
import com.workshop.dto.checkin.CheckinStatusResponse;
import com.workshop.service.CheckinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckinController {
    
    private final CheckinService checkinService;
    
    @PostMapping
    public ApiResponse<CheckinResponse> checkin(@AuthenticationPrincipal Long userId) {
        log.info("User {} attempting to check in", userId);
        CheckinResponse response = checkinService.checkin(userId);
        return ApiResponse.success("签到成功", response);
    }
    
    @GetMapping("/status")
    public ApiResponse<CheckinStatusResponse> getCheckinStatus(@AuthenticationPrincipal Long userId) {
        CheckinStatusResponse response = checkinService.getCheckinStatus(userId);
        return ApiResponse.success(response);
    }
}
