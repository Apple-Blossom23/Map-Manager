package com.workshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionType type;
    
    @Column(name = "change_drops", nullable = false)
    private Integer changeDrops = 0;
    
    @Column(name = "change_lightning", nullable = false)
    private Integer changeLightning = 0;
    
    @Column(name = "related_id")
    private Long relatedId;
    
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum TransactionType {
        CHECKIN,
        INVITE,
        TASK_LOGIN,
        TASK_VIEW,
        TASK_LIKE,
        TASK_DONATE,
        MAP_SOLD,
        MAP_REWARD,
        SYS_GRANT,
        MAP_DOWNLOAD_PAY,
        MAP_DONATE_PAY,
        REPORT_REWARD
    }
}
