package ru.matveysuvorov.teamup.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, Integer> {

    Optional<FileEntity> findById(UUID id);
    Optional<FileEntity> findByUploaderId(String uploaderId);
    boolean existsByUploaderId(String uploaderId);

}
