package dev.retrotv.fileserver.domain.files.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FILES")
public class FileEntity {

	@Id
	@Column(name = "ID", length = 36)
	private UUID id;

	@Column(name = "ORIGINAL_FILE_NAME", length = 512, nullable = false)
	private String originalFileName;

	@Column(name = "FILE_NAME", length = 512, nullable = false)
	private String fileName;

	@Column(name = "FILE_PATH", length = 1024, nullable = false)
	private String filePath;

	@Column(name = "MIME_TYPE", length = 128, nullable = false)
	private String mimeType;

	@Column(name = "SIZE", nullable = false)
	private Long size;

	@Column(name = "HASH", length = 64)
	private String hash;

	@Column(name = "DESCRIPTION", length = 1024)
	private String description;

	@Builder.Default
	@Column(name = "IS_ACTIVE", nullable = false)
	private Boolean isActive = true;

	@Builder.Default
	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Builder.Default
	@Column(name = "UPDATED_AT", nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

	@Column(name = "UPLOADED_BY")
	private String uploadedBy;

	@Column(name = "CATEGORY", length = 128)
	private String category;

	@JoinColumn(name = "FILE_ID")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<MetadataEntity> metadata;

	@PrePersist
	public void generateId() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	public void updateTimestamp() {
		this.updatedAt = LocalDateTime.now();
	}
}
