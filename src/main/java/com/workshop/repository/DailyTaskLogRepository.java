package com.workshop.repository;

import com.workshop.entity.DailyTaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyTaskLogRepository extends JpaRepository<DailyTaskLog, Long> {
    
    Optional<DailyTaskLog> findByUserIdAndDate(Long userId, LocalDate date);
}
