package dev.retrotv.fileserver.domain.files;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import dev.retrotv.fileserver.common.exception.ChunkMergeException;
import dev.retrotv.fileserver.common.properties.FileServerProperties;
import dev.retrotv.fileserver.domain.files.dtos.ChunkUploadResponse;
import dev.retrotv.fileserver.domain.files.dtos.InitData;
import dev.retrotv.fileserver.domain.files.dtos.UploadFileInfo;
import dev.retrotv.fileserver.domain.files.dtos.UploadSession;
import dev.retrotv.fileserver.domain.files.dtos.UploadStatusResponse;
import dev.retrotv.fileserver.domain.files.entities.FileEntity;
import dev.retrotv.fileserver.enums.StatusCode;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileServerProperties fileServerProperties;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        when(fileServerProperties.getChunkSize()).thenReturn(1024);
        when(fileServerProperties.getUploadDir()).thenReturn("uploads/");
        when(fileServerProperties.getTempDir()).thenReturn("tmp/");
    }

    @Test
    @MockitoSettings(strictness = LENIENT)
    @DisplayName("initializeUploadSession 테스트")
    void test_initializeUploadSession() {
        assertThrows(
            NullPointerException.class,
            () -> fileService.initializeUploadSession(null)
        );

        // given
        InitData initData = new InitData();
        initData.setFileName("test.txt");
        initData.setFileSize(2048);
        initData.setMimeType("text/plain");
        initData.setSubDir("test");
        initData.setTags(new ArrayList<>());
        initData.setTotalChunks(2);

        // when
        UploadSession uploadSession = fileService.initializeUploadSession(initData);

        // then
        assertNotNull(uploadSession);
        assertEquals(initData.getFileName(), uploadSession.getFileName());
        assertEquals(initData.getFileSize(), uploadSession.getFileSize());
        assertEquals(initData.getMimeType(), uploadSession.getMimeType());
        assertEquals(initData.getSubDir(), uploadSession.getSubDir());
        assertEquals(initData.getTotalChunks(), uploadSession.getTotalChunks());
    }

    @Test
    @DisplayName("getUploadStatus 테스트")
    @MockitoSettings(strictness = LENIENT)
    void test_getUploadStatus() {
        assertThrows(
            NullPointerException.class,
            () -> fileService.getUploadStatus(null)
        );

        // given
        InitData initData = new InitData();
        initData.setFileName("test.txt");
        initData.setFileSize(2048);
        initData.setMimeType("text/plain");
        initData.setSubDir("test");
        initData.setTags(new ArrayList<>());
        initData.setTotalChunks(2);

        UploadSession uploadSession = fileService.initializeUploadSession(initData);

        // when
        UploadStatusResponse response = fileService.getUploadStatus(uploadSession.getSessionId());

        // then
        assertNotNull(response);
        assertEquals(uploadSession.getSessionId(), response.getSessionId());
        assertEquals(0, response.getUploadedChunks());
    }

    @Test
    @DisplayName("saveChunk 테스트")
    @MockitoSettings(strictness = LENIENT)
    void test_saveChunk() {
        MultipartFile validChunk = new MockMultipartFile("file_chunk_001", new byte[1024]);
        assertThrows(
            NullPointerException.class,
            () -> fileService.saveChunk(null, 0, validChunk)
        );

        UUID validSessionId = UUID.randomUUID();
        assertThrows(
            NullPointerException.class,
            () -> fileService.saveChunk(validSessionId, 0, null)
        );

        // given
        InitData initData = new InitData();
        initData.setFileName("test.txt");
        initData.setFileSize(2048);
        initData.setMimeType("text/plain");
        initData.setSubDir("test");
        initData.setTags(new ArrayList<>());
        initData.setTotalChunks(2);

        int chunkIndex = 1;
        MultipartFile chunk = new MockMultipartFile("file_chunk_001", new byte[1024]);

        UploadSession uploadSession = fileService.initializeUploadSession(initData);

        // when
        ChunkUploadResponse response = fileService.saveChunk(uploadSession.getSessionId(), chunkIndex, chunk);

        // then
        assertNotNull(response);
        assertEquals(StatusCode.UPLOADING, response.getStatus());
    }

    @Test
    @DisplayName("mergeChunks 테스트")
    @MockitoSettings(strictness = LENIENT)
    void test_mergeChunks() {
        NullPointerException exception1 = assertThrows(
            NullPointerException.class,
            () -> fileService.mergeChunks(null)
        );
        assertNotNull(exception1);

        // given
        InitData initData = new InitData();
        initData.setFileName("test.txt");
        initData.setFileSize(2048);
        initData.setMimeType("text/plain");
        initData.setSubDir("test");
        initData.setTags(new ArrayList<>());
        initData.setTotalChunks(2);
        UploadSession uploadSession = fileService.initializeUploadSession(initData);

        // when
        @SuppressWarnings("java:S5778")
        ChunkMergeException ex = assertThrows(
            ChunkMergeException.class,
            () -> fileService.mergeChunks(uploadSession.getSessionId())
        );
        assertEquals("모든 청크가 업로드되지 않았습니다.", ex.getMessage());

        fileService.saveChunk(uploadSession.getSessionId(), 1, new MockMultipartFile("file_chunk_1", new byte[1024]));
        fileService.saveChunk(uploadSession.getSessionId(), 2, new MockMultipartFile("file_chunk_2", new byte[1024]));

        FileEntity fileEntity = new FileEntity(
            uploadSession.getSessionId(),
            fileServerProperties.getUploadDir() + "/" + uploadSession.getSessionId().toString(),
            "wzexrcdvfbctfvgbjhtrdfthvh",
            new ArrayList<>(),
            uploadSession
        );

        given(
            fileRepository.save(fileEntity)
        ).willReturn(fileEntity);

        UploadFileInfo fileInfo = fileService.mergeChunks(uploadSession.getSessionId());

        ArgumentCaptor<FileEntity> captor = ArgumentCaptor.forClass(FileEntity.class);

        // capture 값은 런타임에 대입되기 때문에, save 메서드에 null이 전달 될 수 있다는 경고를 띄우므로 무시해도 됨
        verify(fileRepository).save(captor.capture());
        FileEntity actualEntity = captor.getValue();

        // then
        assertNotNull(fileInfo);
        assertEquals(fileEntity.getId(), actualEntity.getId());
        assertEquals(fileEntity.getOriginalFileName(), actualEntity.getOriginalFileName());
    }

    @Test
    @DisplayName("uploadCancel 테스트")
    @MockitoSettings(strictness = LENIENT)
    void test_uploadCancel() {
        assertThrows(
            NullPointerException.class,
            () -> fileService.uploadCancel(null)
        );

        // given
        InitData initData = new InitData();
        initData.setFileName("test.txt");
        initData.setFileSize(2048);
        initData.setMimeType("text/plain");
        initData.setSubDir("test");
        initData.setTags(new ArrayList<>());
        initData.setTotalChunks(2);

        UploadSession uploadSession = fileService.initializeUploadSession(initData);
        fileService.uploadCancel(uploadSession.getSessionId());

        // then
        assertThrows(
            dev.retrotv.fileserver.common.exception.SessionNotFoundException.class,
            () -> {
                // private 필드라면 getUploadStatus 등 public 메서드로 접근
                fileService.getUploadStatus(uploadSession.getSessionId());
            }
        );
    }

    @AfterAll
    static void cleanUp() {
        deleteDirectory(new File("uploads/"));
        deleteDirectory(new File("tmp/"));
    }

    static void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
            }
            dir.delete();
        }
    }
}
