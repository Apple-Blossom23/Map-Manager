package com.workshop.service;

import com.workshop.entity.SystemConfig;
import com.workshop.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemConfigService {
    
    private final SystemConfigRepository systemConfigRepository;
    public boolean isRegistrationEnabled() {
        return getBooleanConfig("registration_enabled", true);
    }
    
    public boolean isLoginEnabled() {
        return getBooleanConfig("login_enabled", true);
    }
    
    public boolean isEmailDomainAllowed(String domain) {
        try {
            String value = getConfig("allowed_email_domains", "");
            if (value.isEmpty()) {
                return false;
            }
            // 使用逗号分隔字符串并去除空格
            String[] domains = value.split(",");
            for (String d : domains) {
                if (d.trim().equalsIgnoreCase(domain)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    public int getDailyIpRegistrationLimit() {
        return getIntConfig("daily_ip_registration_limit", 3);
    }
    
    public int getCheckinFixedDrops() {
        return getIntConfig("checkin_fixed_drops", 5);
    }
    
    public boolean isCheckinRandomEnabled() {
        return getBooleanConfig("checkin_random_enabled", true);
    }
    
    private String getConfig(String key, String defaultValue) {
        return systemConfigRepository.findById(key)
                .map(SystemConfig::getValue)
                .orElse(defaultValue);
    }
    
    private boolean getBooleanConfig(String key, boolean defaultValue) {
        String value = getConfig(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
    
    private int getIntConfig(String key, int defaultValue) {
        String value = getConfig(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
