package dev.retrotv.fileserver.domain.files.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "FILE_METADATA")
public class MetadataEntity {

	@EmbeddedId
	private MetadataId id;

	@Column(name = "META_VALUE", length = 1024)
	private String metaValue;

    // FileEntity와의 연관관계 필드 제거 (단방향)
}
