package com.workshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "maps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Map {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "cover_image")
    private String coverImage;
    
    @Column(name = "file_url", nullable = false)
    private String fileUrl;
    
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MapType type;
    
    @Column(name = "original_author", length = 100)
    private String originalAuthor;
    
    @Column(name = "original_link")
    private String originalLink;
    
    @Column(name = "auth_proof")
    private String authProof;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MapStatus status = MapStatus.PENDING;
    
    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;
    
    @Column(name = "download_cost", nullable = false)
    private Integer downloadCost = 0;
    
    @Column(nullable = false)
    private Integer views = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum MapType {
        ORIGINAL, REPOST
    }
    
    public enum MapStatus {
        PENDING, APPROVED, REJECTED
    }
}
