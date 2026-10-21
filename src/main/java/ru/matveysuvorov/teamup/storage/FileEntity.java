package ru.matveysuvorov.teamup.storage;

import jakarta.persistence.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="files")
public class FileEntity {

    @Id
    @Column(name="id")
    private UUID id;

    @Column(name="storage_key", nullable = false, unique = true)
    private String storageKey;

    @Column(name="original_filename", nullable = false)
    private String originalFilename;

    @Column(name="content_type", nullable = false)
    private String contentType;

    @Column(name="size", nullable = false)
    private Long size;

    @Column(name = "uploader_id", nullable = false)
    private String uploaderId;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
}
