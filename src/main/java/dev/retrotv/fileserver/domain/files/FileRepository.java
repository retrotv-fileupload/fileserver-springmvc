package dev.retrotv.fileserver.domain.files;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.retrotv.fileserver.domain.files.entities.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {

}
