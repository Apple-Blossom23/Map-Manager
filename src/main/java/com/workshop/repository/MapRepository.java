package com.workshop.repository;

import com.workshop.entity.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapRepository extends JpaRepository<Map, Long> {
    
    Page<Map> findByStatus(Map.MapStatus status, Pageable pageable);
    
    Page<Map> findByAuthorId(Long authorId, Pageable pageable);
    
    Page<Map> findByAuthorIdAndStatus(Long authorId, Map.MapStatus status, Pageable pageable);
}
