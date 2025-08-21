package dev.retrotv.fileserver.domain.files.dtos;

import java.io.Serializable;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionIdRequest implements Serializable {
    @NonNull private UUID sessionId;
}
