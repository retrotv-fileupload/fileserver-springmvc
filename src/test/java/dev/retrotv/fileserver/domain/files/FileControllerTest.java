package dev.retrotv.fileserver.domain.files;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import dev.retrotv.fileserver.domain.files.dtos.ChunkUploadResponse;
import dev.retrotv.fileserver.domain.files.dtos.InitData;
import dev.retrotv.fileserver.domain.files.dtos.UploadFileInfo;
import dev.retrotv.fileserver.domain.files.dtos.UploadSession;
import dev.retrotv.fileserver.domain.files.dtos.UploadStatusResponse;
import dev.retrotv.fileserver.enums.StatusCode;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
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
    void test_uploadStatus() throws Exception {
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
            .andExpect(content().contentType(Objects.requireNonNull(APPLICATION_JSON)))
            .andExpect(jsonPath("$.data.sessionId").value(sessionId.toString()))
            .andExpect(jsonPath("$.data.fileName").value("test.txt"))
            .andExpect(jsonPath("$.data.fileSize").value(1024))
            .andExpect(jsonPath("$.data.progress").value(100.0))
            .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("upload/init 테스트")
    void test_uploadInit() throws Exception {
        UUID sessionId = UUID.randomUUID();
        InitData initData = new InitData("test.txt", 1024L, 1, "test", "text/plain", new ArrayList<>());
        UploadSession session = new UploadSession(sessionId, "test.txt", StatusCode.INITIALIZED, 1024L, 1, "text/plain", "test", new ArrayList<>(), new HashSet<>(), LocalDateTime.now(), LocalDateTime.now());
        given(
            fileService.initializeUploadSession(initData)
        ).willReturn(session);

        ResultActions actions = this.mockMvc.perform(
            post("/api/v1/files/upload/init")
                .contentType(Objects.requireNonNull(APPLICATION_JSON))
                .content(
                    """
                    {
                        "fileName": "test.txt",
                        "fileSize": 1024,
                        "totalChunks": 1,
                        "mimeType": "text/plain",
                        "subDir": "test",
                        "tags": []
                    }
                    """
                )
        );

        String responseBody = actions.andReturn().getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(Objects.requireNonNull(APPLICATION_JSON)))
            .andExpect(jsonPath("$.data.sessionId").value(sessionId.toString()));
    }

    @Test
    @DisplayName("upload/chunk 테스트")
    void test_uploadChunk() throws Exception {
        UUID sessionId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("chunk", "test.txt", "text/plain", "base64-encoded-chunk-data".getBytes());
        ChunkUploadResponse response = new ChunkUploadResponse(
            StatusCode.COMPLETED,
            "청크 업로드 성공",
            true,
            1,
            100,
            1,
            1,
            true
        );
        given(
            fileService.saveChunk(
                any(UUID.class),
                anyInt(),
                any(MultipartFile.class)
            )
        ).willReturn(response);

        ResultActions actions = this.mockMvc.perform(
            multipart("/api/v1/files/upload/chunk")
                .file(file)
                .param("sessionId", sessionId.toString())
                .param("chunkIndex", "1")
        );

        // ArgumentCaptor는 실제 Controller로 전달된 값을 가져오는 데 사용된다
        ArgumentCaptor<MultipartFile> captor = ArgumentCaptor.forClass(MultipartFile.class);

        // 세번째 인자를 captor로 캡처
        verify(fileService).saveChunk(eq(sessionId), eq(1), captor.capture());

        // 해당 인자의 값을 대입
        MultipartFile actualFile = captor.getValue();

        assertEquals(actualFile, file);
        assertEquals(actualFile.getOriginalFilename(), file.getOriginalFilename());
        assertArrayEquals(actualFile.getBytes(), file.getBytes());

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(Objects.requireNonNull(APPLICATION_JSON)))
            .andExpect(jsonPath("$.data.success").value(response.isSuccess()))
            .andExpect(jsonPath("$.data.message").value(response.getMessage()))
            .andExpect(jsonPath("$.data.status").value(response.getStatus().name()));
    }

    @Test
    @DisplayName("upload/complete 테스트")
    void test_uploadComplete() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UploadFileInfo fileInfo = new UploadFileInfo(
            sessionId,
            "test.txt",
            1024L,
            "text/plain",
            new ArrayList<>()
        );
        given(
            fileService.mergeChunks(sessionId)
        ).willReturn(fileInfo);

        ResultActions actions = this.mockMvc.perform(
            post("/api/v1/files/upload/complete")
                .contentType(Objects.requireNonNull(APPLICATION_JSON))
                .content(
                    Objects.requireNonNull(
                        """
                        {
                            "sessionId": "%s"
                        }
                        """.formatted(sessionId)
                    )
                )
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(Objects.requireNonNull(APPLICATION_JSON)))
            .andExpect(jsonPath("$.data.id").value(fileInfo.getId().toString()))
            .andExpect(jsonPath("$.data.fileName").value(fileInfo.getFileName()))
            .andExpect(jsonPath("$.data.fileSize").value(fileInfo.getFileSize()))
            .andExpect(jsonPath("$.data.mimeType").value(fileInfo.getMimeType()))
            .andExpect(jsonPath("$.data.tags").value(fileInfo.getTags()));
    }

    @Test
    @DisplayName("upload/cancel 테스트")
    void test_uploadCancel() throws Exception {
        UUID sessionId = UUID.randomUUID();

        ResultActions actions = this.mockMvc.perform(
            delete("/api/v1/files/upload/cancel")
                .contentType(Objects.requireNonNull(APPLICATION_JSON))
                .content(
                    Objects.requireNonNull(
                        """
                        {
                            "sessionId": "%s"
                        }
                        """.formatted(sessionId)
                    )
                )
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(Objects.requireNonNull(APPLICATION_JSON)))
            .andExpect(jsonPath("$.message").value("업로드가 취소되었습니다."));

        verify(fileService).uploadCancel(sessionId);
    }
}