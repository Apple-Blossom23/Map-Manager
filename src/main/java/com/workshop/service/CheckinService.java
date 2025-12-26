package com.workshop.service;

import com.workshop.dto.checkin.CheckinResponse;
import com.workshop.dto.checkin.CheckinStatusResponse;
import com.workshop.entity.DailyTaskLog;
import com.workshop.entity.User;
import com.workshop.exception.BusinessException;
import com.workshop.repository.DailyTaskLogRepository;
import com.workshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckinService {
    
    private final DailyTaskLogRepository dailyTaskLogRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();
    
    @Transactional
    public CheckinResponse checkin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        LocalDate today = LocalDate.now();
        
        DailyTaskLog taskLog = dailyTaskLogRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> {
                    DailyTaskLog newLog = new DailyTaskLog();
                    newLog.setUserId(userId);
                    newLog.setDate(today);
                    newLog.setIsCheckedIn(false);
                    newLog.setLoginRewarded(false);
                    newLog.setViewCount(0);
                    newLog.setLikeCount(0);
                    newLog.setDonateDrops(0);
                    return newLog;
                });
        
        if (taskLog.getIsCheckedIn()) {
            throw new BusinessException("今日已签到，请明天再来");
        }
        
        int dropsReward = generateNormalDistribution();
        
        user.setDrops(user.getDrops() + dropsReward);
        userRepository.save(user);
        
        taskLog.setIsCheckedIn(true);
        dailyTaskLogRepository.save(taskLog);
        
        log.info("User {} checked in successfully, earned {} drops", userId, dropsReward);
        
        CheckinResponse response = new CheckinResponse();
        response.setDrops(dropsReward);
        response.setTotalDrops(user.getDrops());
        response.setCheckedInToday(true);
        response.setLastCheckinDate(today);
        
        return response;
    }
    
    public CheckinStatusResponse getCheckinStatus(Long userId) {
        LocalDate today = LocalDate.now();
        
        DailyTaskLog taskLog = dailyTaskLogRepository.findByUserIdAndDate(userId, today)
                .orElse(null);
        
        boolean checkedInToday = taskLog != null && taskLog.getIsCheckedIn();
        LocalDate lastCheckinDate = checkedInToday ? today : null;
        
        CheckinStatusResponse response = new CheckinStatusResponse();
        response.setCheckedInToday(checkedInToday);
        response.setLastCheckinDate(lastCheckinDate);
        response.setCanCheckin(!checkedInToday);
        
        return response;
    }
    
    private int generateNormalDistribution() {
        double u1 = random.nextDouble();
        double u2 = random.nextDouble();
        double z0 = Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
        
        int value = (int) Math.round(8 + z0 * 2.5);
        
        return Math.max(1, Math.min(15, value));
    }
}
