package dev.retrotv.fileserver.domain.files;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.retrotv.fileserver.domain.files.dtos.SessionIdRequest;

class SessionIdRequestTest {
    
    @Test
    @DisplayName("NonNull 테스트")
    void test_nonNull() {
        SessionIdRequest request = new SessionIdRequest();
        assertThrows(NullPointerException.class, () -> request.setSessionId(null));
    }
}
