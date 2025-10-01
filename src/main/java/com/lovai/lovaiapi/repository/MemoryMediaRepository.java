package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.MemoryMedia;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemoryMediaRepository extends JpaRepository<MemoryMedia, UUID> {
    
    @Query("SELECT mm FROM MemoryMedia mm WHERE mm.memory.id = :memoryId ORDER BY mm.createdAt ASC")
    List<MemoryMedia> findByMemoryId(@Param("memoryId") UUID memoryId);
    
    @Query("SELECT mm FROM MemoryMedia mm WHERE mm.url = :url")
    MemoryMedia findByUrl(@Param("url") String url);
    
    @Query("SELECT COUNT(mm) FROM MemoryMedia mm WHERE mm.memory.id = :memoryId")
    long countByMemoryId(@Param("memoryId") UUID memoryId);
    
    @Query("SELECT mm FROM MemoryMedia mm WHERE mm.memory.id = :memoryId AND mm.mediaType = :mediaType ORDER BY mm.createdAt ASC")
    List<MemoryMedia> findByMemoryIdAndMediaType(@Param("memoryId") UUID memoryId, @Param("mediaType") String mediaType);
    
    @Query("DELETE FROM MemoryMedia mm WHERE mm.memory.id = :memoryId")
    void deleteByMemoryId(@Param("memoryId") UUID memoryId);
    
    @Query("SELECT mm FROM MemoryMedia mm WHERE mm.memory.id = :memoryId ORDER BY mm.createdAt ASC")
    List<MemoryMedia> findByMemoryIdWithPagination(@Param("memoryId") UUID memoryId, Pageable pageable);
}
