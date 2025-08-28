package dev.retrotv.fileserver.domain.files.dtos;

import java.io.Serializable;
import java.util.UUID;

import dev.retrotv.fileserver.domain.files.entities.TagEntity;
import dev.retrotv.fileserver.domain.files.entities.TagId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag implements Serializable {
    private String key;
    private String value;

    public TagEntity toEntity(UUID sessionId) {
        return new TagEntity(new TagId(sessionId, this.key), this.value);
    }
}
