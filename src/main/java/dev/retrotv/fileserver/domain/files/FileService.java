package dev.retrotv.fileserver.domain.files;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.retrotv.fileserver.domain.files.dtos.InitData;
import dev.retrotv.fileserver.domain.files.dtos.UploadSession;
import dev.retrotv.fileserver.domain.files.dtos.UploadStatusResponse;
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

    public UploadStatusResponse getUploadStatus(UUID sessionId) {
        UploadSession session = uploadSessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Upload session not found");
        }
        
        // 세션의 마지막 활동 시간을 갱신
        session.setLastActivity(LocalDateTime.now());
        return new UploadStatusResponse(
            sessionId,
            session.getStatus(),
            session.getFileName(),
            calculateProgress(session),
            session.getUploadedChunks().size(),
            session.getTotalChunks(),
            getMissingChunks(session),
            session.getFileSize(),
            session.getLastActivity()
        );
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

    // 누락된 청크 정보
    private Set<Integer> getMissingChunks(UploadSession session) {
        Set<Integer> missingChunks = new HashSet<>();
        for (int i = 0; i < session.getTotalChunks(); i++) {
            if (!session.getUploadedChunks().contains(i)) {
                missingChunks.add(i);
            }
        }

        return missingChunks;
    }

    // 진행율 계산
    private double calculateProgress(UploadSession session) {
        if (session.getTotalChunks() == 0) {
            return 0.0;
        }

        return Math.round(
            (double) session.getUploadedChunks().size() / session.getTotalChunks() * 100
        );
    }
}
