package dev.retrotv.fileserver.domain.files;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.retrotv.fileserver.domain.files.dtos.InitData;
import dev.retrotv.fileserver.domain.files.dtos.UploadSession;
import dev.retrotv.fileserver.enums.StatusCode;
import lombok.NonNull;

@Service
public class FileService {
    private final FileRepository fileRepository;
    private final Map<UUID, UploadSession> uploadSessions;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        this.uploadSessions = new HashMap<>();
    }

    public UploadSession initializeUploadSession(@NonNull InitData initData) {
        UploadSession uploadSession = createNewUploadSession(initData);
        uploadSessions.put(uploadSession.getSessionId(), uploadSession);

        return uploadSession;
    }

    // 새로운 업로드 세션 생성
    private UploadSession createNewUploadSession(InitData initData) {
        UUID sessionId = UUID.randomUUID();

        return new UploadSession(
            sessionId,
            initData.getFileName(),
            StatusCode.INITIALIZED.getCode(),
            initData.getFileSize(),
            initData.getTotalChunks(),
            initData.getMimeType(),
            initData.getSubDir(),
            new HashSet<>(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}
