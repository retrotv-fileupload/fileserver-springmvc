package dev.retrotv.fileserver.domain.files.dtos;

import io.micrometer.common.lang.NonNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionIdRequest {
    @NonNull String sessionId;
}
