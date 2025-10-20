package com.example.mediaservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFile, String> {
    boolean existsImageById(String id);
}
