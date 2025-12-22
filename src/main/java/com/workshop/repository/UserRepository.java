package com.workshop.repository;

import com.workshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByInviteCode(String inviteCode);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByInviteCode(String inviteCode);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.registrationIp = :ip " +
           "AND DATE(u.createdAt) = :date")
    long countByRegistrationIpAndDate(@Param("ip") String ip, @Param("date") LocalDate date);
}
