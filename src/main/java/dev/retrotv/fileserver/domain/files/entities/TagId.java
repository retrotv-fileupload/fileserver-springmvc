package dev.retrotv.fileserver.domain.files.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TagId implements Serializable {

    @Column(name = "FILE_ID", length = 36)
    private UUID fileId;

    @Column(name = "KEY", length = 128)
    private String key;
}
