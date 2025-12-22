package com.workshop.service;

import com.workshop.entity.Transaction;
import com.workshop.entity.User;
import com.workshop.exception.BusinessException;
import com.workshop.repository.TransactionRepository;
import com.workshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void addDrops(Long userId, Integer amount, String type, Long relatedId, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        user.setDrops(user.getDrops() + amount);
        userRepository.save(user);
        
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType(Transaction.TransactionType.valueOf(type.toUpperCase()));
        transaction.setChangeDrops(amount);
        transaction.setRelatedId(relatedId);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }
    
    @Transactional
    public void deductDrops(Long userId, Integer amount, String type, Long relatedId, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (user.getDrops() < amount) {
            throw new BusinessException("水滴不足");
        }
        
        user.setDrops(user.getDrops() - amount);
        userRepository.save(user);
        
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType(Transaction.TransactionType.valueOf(type.toUpperCase()));
        transaction.setChangeDrops(-amount);
        transaction.setRelatedId(relatedId);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }
    
    @Transactional
    public void addLightning(Long userId, Integer amount, String type, Long relatedId, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        user.setLightning(user.getLightning() + amount);
        checkAndUpgradeLevel(user);
        userRepository.save(user);
        
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType(Transaction.TransactionType.valueOf(type.toUpperCase()));
        transaction.setChangeLightning(amount);
        transaction.setRelatedId(relatedId);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }
    
    private void checkAndUpgradeLevel(User user) {
        int currentLevel = user.getLevel();
        int currentLightning = user.getLightning();
        
        int[] thresholds = {0, 0, 300, 800, 1500, 3000, 5000, 8000, 15000, 30000};
        
        for (int level = currentLevel + 1; level <= 9; level++) {
            if (currentLightning >= thresholds[level]) {
                user.setLevel(level);
            } else {
                break;
            }
        }
    }
}
