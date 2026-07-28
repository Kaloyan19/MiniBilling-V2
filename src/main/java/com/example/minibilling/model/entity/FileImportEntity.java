package com.example.minibilling.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "file_imports")
public class FileImportEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private ImportType type;

    private String filename;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private UserEntity uploadedBy;

    @Column(name = "uploaded_at")
    private OffsetDateTime uploadedAt;

    @Lob
    private byte[] file;

    public FileImportEntity() {}

    public String getId() { return id; }
    public ImportType getType() { return type; }
    public String getFilename() { return filename; }
    public UserEntity getUploadedBy() { return uploadedBy; }
    public OffsetDateTime getUploadedAt() { return uploadedAt; }
    public byte[] getFile() { return file; }

    public void setId(String id) { this.id = id; }
    public void setFilename(String filename) { this.filename = filename; }
    public void setUploadedAt(OffsetDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setType(ImportType type) { this.type = type; }
}
