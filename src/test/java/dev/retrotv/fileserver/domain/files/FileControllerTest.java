package dev.retrotv.fileserver.domain.files;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import dev.retrotv.fileserver.domain.files.dtos.UploadStatusResponse;
import dev.retrotv.fileserver.enums.StatusCode;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@WebMvcTest(controllers = { FileController.class })
class FileControllerTest {

    @MockitoBean
    private FileService fileService;

    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("upload/status/{sessionId} 테스트")
    void testUploadStatus() throws Exception {
        UUID sessionId = UUID.randomUUID();
        given(
            fileService.getUploadStatus(sessionId)
        ).willReturn(
            new UploadStatusResponse(
                sessionId,
                StatusCode.COMPLETED,
                "test.txt",
                100.0,
                1,
                1,
                new HashSet<>(),
                1024L,
                LocalDateTime.now()
            )
        );

        ResultActions actions = this.mockMvc.perform(
            get("/api/v1/files/upload/status/{sessionId}", sessionId)
        );

        actions.andExpect(status().isOk())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andExpect(jsonPath("$.data.sessionId").value(sessionId.toString()))
               .andExpect(jsonPath("$.data.fileName").value("test.txt"))
               .andExpect(jsonPath("$.data.fileSize").value(1024))
               .andExpect(jsonPath("$.data.progress").value(100.0))
               .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}
