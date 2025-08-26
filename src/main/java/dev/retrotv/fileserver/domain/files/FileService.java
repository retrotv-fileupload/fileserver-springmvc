package dev.retrotv.fileserver.domain.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.f4b6a3.uuid.UuidCreator;

import dev.retrotv.fileserver.common.exception.ChunkMergeException;
import dev.retrotv.fileserver.common.exception.ChunkUploadException;
import dev.retrotv.fileserver.domain.files.dtos.ChunkUploadResponse;
import dev.retrotv.fileserver.domain.files.dtos.FileInfo;
import dev.retrotv.fileserver.domain.files.dtos.InitData;
import dev.retrotv.fileserver.domain.files.dtos.UploadSession;
import dev.retrotv.fileserver.domain.files.dtos.UploadStatusResponse;
import dev.retrotv.fileserver.domain.files.entities.FileEntity;
import dev.retrotv.fileserver.enums.StatusCode;
import lombok.NonNull;

@Service
public class FileService {
    private static final int CHUNK_SIZE = 8 * 1024;

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

    public ChunkUploadResponse saveChunk(UUID sessionId, int chunkIndex, MultipartFile chunk) {
        UploadSession session = uploadSessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Upload session not found");
        }

        // 청크 저장 경로 생성 (예: /uploads/{sessionId}/tmp)
        String uploadDir = "uploads/";
        String tmpDir = uploadDir + "tmp/" + sessionId;
        File dir = new File(tmpDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 인덱스를 10자리 0으로 채움
        String paddedIndex = String.format("%010d", chunkIndex);
        String chunkFileName = sessionId + "_chunk_" + paddedIndex;
        File chunkFile = new File(dir, chunkFileName);

        try (InputStream in = chunk.getInputStream();
            OutputStream out = new FileOutputStream(chunkFile)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            throw new ChunkUploadException("청크 저장 실패: " + e.getMessage(), e);
        }

        // 청크 저장 로직 구현
        session.getUploadedChunks().add(chunkIndex);
        session.setLastActivity(LocalDateTime.now());

        return new ChunkUploadResponse(
            session.getUploadedChunks().size() == session.getTotalChunks() ? StatusCode.ALL_CHUNKS_UPLOADED.getCode() : StatusCode.UPLOADING.getCode(),
            "청크 업로드 성공",
            true,
            chunkIndex,
            (session.getUploadedChunks().size() / session.getTotalChunks()) * 100,
            session.getUploadedChunks().size(),
            session.getTotalChunks(),
            session.getUploadedChunks().size() == session.getTotalChunks()
        );
    }

    public FileInfo mergeChunks(UUID sessionId) {
        UploadSession session = uploadSessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Upload session not found");
        }

        // 모든 청크가 업로드되었는지 확인
        if (session.getUploadedChunks().size() != session.getTotalChunks()) {
            throw new IllegalStateException("모든 청크가 업로드되지 않았습니다.");
        }

        // 청크 병합 로직
        File mergedDir = new File("uploads/" + sessionId);
        if (!mergedDir.exists()) {
            mergedDir.mkdirs();
        }
        File mergedFile = new File(mergedDir, session.getFileName());
        
        try (OutputStream out = new FileOutputStream(mergedFile)) {
            for (int i = 0; i < session.getTotalChunks(); i++) {
                File chunkFile = new File("uploads/" + sessionId + "/tmp/" + sessionId + "_chunk_" + String.format("%010d", i));
                if (chunkFile.exists()) {
                    Files.copy(chunkFile.toPath(), out);
                }
            }
        } catch (IOException e) {
            throw new ChunkMergeException("청크 병합 실패: " + e.getMessage(), e);
        }

        FileEntity entity = FileEntity.builder()
            .id(sessionId)
            .originalFileName(session.getFileName())
            .fileName(session.getFileName())
            .filePath("uploads/" + sessionId + "/" + session.getFileName())
            .mimeType(session.getMimeType())
            .size(session.getFileSize())
            .hash(getSha256Hash(mergedFile))
            .build();

        FileEntity savedEntity = fileRepository.save(entity);
        FileInfo fileInfo = new FileInfo(
            savedEntity.getId(),
            savedEntity.getOriginalFileName(),
            savedEntity.getSize(),
            savedEntity.getMimeType()
        );

        return fileInfo;
    }

    public void cancelUpload(UUID sessionId) {
        UploadSession session = uploadSessions.remove(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Upload session not found");
        }

        // 임시 파일 및 디렉토리 삭제
        File tmpDir = new File("uploads/tmp/" + sessionId);
        if (tmpDir.exists()) {
            for (File file : tmpDir.listFiles()) {
                file.delete();
            }
            tmpDir.delete();
        }
    }

    // 새로운 업로드 세션 생성
    private UploadSession createNewUploadSession(InitData initData) {
        UUID sessionId = UuidCreator.getTimeOrderedEpoch();

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

    private String getSha256Hash(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();

            // 바이트 배열을 16진수 문자열로 변환
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 해시 생성 실패: " + e.getMessage(), e);
        }
    }
}
