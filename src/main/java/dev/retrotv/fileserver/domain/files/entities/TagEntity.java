package dev.retrotv.fileserver.domain.files.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "FILE_METADATA")
public class TagEntity {

	@EmbeddedId
	private TagId id;

	@Column(name = "VALUE", length = 1024)
	private String value;
}
