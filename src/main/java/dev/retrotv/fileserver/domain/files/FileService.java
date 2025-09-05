package dev.retrotv.fileserver.domain.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.github.f4b6a3.uuid.UuidCreator;

import dev.retrotv.fileserver.common.exception.ChunkMergeException;
import dev.retrotv.fileserver.common.exception.ChunkSaveException;
import dev.retrotv.fileserver.common.exception.FileDownloadException;
import dev.retrotv.fileserver.common.exception.HashingFailException;
import dev.retrotv.fileserver.common.exception.SessionNotFoundException;
import dev.retrotv.fileserver.common.properties.FileServerProperties;
import dev.retrotv.fileserver.domain.files.dtos.ChunkUploadResponse;
import dev.retrotv.fileserver.domain.files.dtos.FileInfo;
import dev.retrotv.fileserver.domain.files.dtos.InitData;
import dev.retrotv.fileserver.domain.files.dtos.UploadSession;
import dev.retrotv.fileserver.domain.files.dtos.UploadStatusResponse;
import dev.retrotv.fileserver.domain.files.entities.FileEntity;
import dev.retrotv.fileserver.domain.files.entities.TagEntity;
import dev.retrotv.fileserver.enums.StatusCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileService {
    private final FileRepository fileRepository;
    private final Map<UUID, UploadSession> uploadSessions;
    private final FileServerProperties fileServerProperties;

    public File getFileById(UUID id) {
        FileEntity fileEntity = fileRepository.findById(id)
            .orElseThrow(() -> new FileDownloadException("해당하는 ID의 파일에 대한 정보가 데이터베이스 상에 존재하지 않습니다. ID: " + id));
    
        File file = new File(fileEntity.getFilePath());
        
        if (!file.exists()) {
            throw new FileDownloadException("해당하는 파일이 물리적으로 존재하지 않거나 경로가 잘못 되었습니다. 경로: " + fileEntity.getFilePath());
        }

        return file;
    }

    /**
     * FileService 생성자
     * 
     * @param fileRepository FileRepository 객체
     * @param fileServerProperties FileServerProperties 객체
     */
    public FileService(FileRepository fileRepository, FileServerProperties fileServerProperties) {
        this.fileRepository = fileRepository;
        this.uploadSessions = new HashMap<>();
        this.fileServerProperties = fileServerProperties;
    }

    /**
     * 업로드 세션 초기화
     * 
     * @param initData 세션 초기화 정보
     * @return 업로드 세션
     */
    public UploadSession initializeUploadSession(@NonNull InitData initData) {
        UploadSession uploadSession = createNewUploadSession(initData);
        uploadSessions.put(uploadSession.getSessionId(), uploadSession);

        return uploadSession;
    }

    /**
     * 업로드 상태 조회
     * 
     * @param sessionId 세션 ID
     * @return 업로드 상태 응답
     */
    public UploadStatusResponse getUploadStatus(@NonNull UUID sessionId) {
        return createUploadStatusResponse(sessionId);
    }

    /**
     * 청크 파일 저장
     * 
     * @param sessionId 세션 ID
     * @param chunkIndex 청크 인덱스
     * @param chunk 청크 파일
     * @return 청크 업로드 응답
     */
    public ChunkUploadResponse saveChunk(@NonNull UUID sessionId, int chunkIndex, @NonNull MultipartFile chunk) {
        saveChunkFile(sessionId, chunkIndex, chunk);
        return createChunkUploadResponse(sessionId, chunkIndex);
    }

    /**
     * 청크 병합
     * 
     * @param sessionId 세션 ID
     * @return 파일 정보
     */
    @Transactional
    public FileInfo mergeChunks(@NonNull UUID sessionId) {
        UploadSession session = getSession(sessionId);

        // 모든 청크가 업로드되었는지 확인
        if (session.getUploadedChunks().size() != session.getTotalChunks()) {
            throw new ChunkMergeException("모든 청크가 업로드되지 않았습니다.");
        }

        // 청크 병합
        File mergedFile = merge(sessionId);

        // 데이터베이스에 파일 정보 저장
        FileInfo fileInfo = saveFileInfo(sessionId, mergedFile);

        // 임시 파일 및 디렉토리 삭제
        removeTmpFiles(sessionId);

        return fileInfo;
    }

    /**
     * 업로드 취소
     * 
     * @param sessionId 취소할 세션 ID
     */
    public void uploadCancel(@NonNull UUID sessionId) {
        removeSession(sessionId);

        // 임시 파일 및 디렉토리 삭제
        removeTmpFiles(sessionId);
    }

    // 새로운 업로드 세션 생성
    private UploadSession createNewUploadSession(InitData initData) {
        return new UploadSession(
            UuidCreator.getTimeOrderedEpoch(), // UUID v7
            initData.getFileName(),
            StatusCode.INITIALIZED,
            initData.getFileSize(),
            initData.getTotalChunks(),
            initData.getMimeType(),
            initData.getSubDir(),
            initData.getTags(),
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

    // 파일의 SHA-256 해시 값 생성
    private String getSha256Hash(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[fileServerProperties.getChunkSize()];
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
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new HashingFailException("파일 해시 값 생성 실패", ex);
        }
    }

    // 업로드 세션 조회
    private UploadSession getSession(UUID sessionId) {
        UploadSession session = uploadSessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException();
        }

        return session;
    }

    // 업로드 세션 삭제
    private void removeSession(UUID sessionId) {
        UploadSession session = uploadSessions.remove(sessionId);
        if (session == null) {
            throw new SessionNotFoundException();
        }
    }

    // 임시 청크 파일 삭제
    private void removeTmpFiles(UUID sessionId) {
        File tmpDir = new File(fileServerProperties.getTempDir() + sessionId);
        if (tmpDir.exists()) {
            for (File file : tmpDir.listFiles()) {
                file.delete();
            }
            tmpDir.delete();
        }
    }

    // 임시 파일 저장 디렉토리 생성
    private String createTmpDir(UUID sessionId) {
        String tmpDirPath = fileServerProperties.getTempDir() + sessionId;
        File tmpDir = new File(tmpDirPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        return tmpDir.getPath();
    }

    // 청크 파일명 생성
    private String createChunkFileName(UploadSession session, int chunkIndex) {
        int totalChunks = session.getTotalChunks();
        int padLength = String.valueOf(totalChunks).length();
        String paddedIndex = String.format("%0" + padLength + "d", chunkIndex);

        return session.getSessionId() + "_chunk_" + paddedIndex;
    }

    // 업로드 상태 응답 생성
    private UploadStatusResponse createUploadStatusResponse(UUID sessionId) {
        UploadSession session = getSession(sessionId);

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

    private ChunkUploadResponse createChunkUploadResponse(UUID sessionId, int chunkIndex) {
        UploadSession session = getSession(sessionId);

        session.getUploadedChunks().add(chunkIndex);
        session.setLastActivity(LocalDateTime.now());

        return new ChunkUploadResponse(
            session.getUploadedChunks().size() == session.getTotalChunks() ? StatusCode.ALL_CHUNKS_UPLOADED : StatusCode.UPLOADING,
            "청크 업로드 성공",
            true,
            chunkIndex,
            (session.getUploadedChunks().size() / session.getTotalChunks()) * 100,
            session.getUploadedChunks().size(),
            session.getTotalChunks(),
            session.getUploadedChunks().size() == session.getTotalChunks()
        );
    }

    private File merge(UUID sessionId) {
        UploadSession session = getSession(sessionId);

        // 청크 병합 로직
        File mergedDir = new File(fileServerProperties.getUploadDir());
        if (!mergedDir.exists()) {
            mergedDir.mkdirs();
        }
        File mergedFile = new File(mergedDir, session.getSessionId().toString());
        
        try (OutputStream out = new FileOutputStream(mergedFile)) {
            for (int i = 0; i < session.getTotalChunks(); i++) {
                String tmpDir = createTmpDir(sessionId);
                String chunkFileName = createChunkFileName(session, i);

                log.debug("청크 파일명: {}", chunkFileName);

                File chunkFile = new File(tmpDir, chunkFileName);
                if (chunkFile.exists()) {
                    Files.copy(chunkFile.toPath(), out);
                }
            };

            return mergedFile;
        } catch (IOException e) {
            throw new ChunkMergeException("청크 병합 실패: " + e.getMessage(), e);
        }
    }

    private FileInfo saveFileInfo(UUID sessionId, File mergedFile) {
        UploadSession session = getSession(sessionId);

        // 파일 태그 엔티티 리스트 생성
        List<TagEntity> tags = session.getTags() == null ? null :
                                      session.getTags()
                                          .stream()
                                          .map(
                                              tag -> tag.toEntity(sessionId)).collect(Collectors.toList()
                                          );
        
        // 파일 엔티티 생성
        FileEntity savedEntity = fileRepository.save(
            new FileEntity(
                sessionId,
                mergedFile.getPath(),
                getSha256Hash(mergedFile),
                tags,
                session
            )
        );

        // 파일 정보 반환
        return new FileInfo(
            savedEntity.getId(),
            savedEntity.getOriginalFileName(),
            savedEntity.getSize(),
            savedEntity.getMimeType(),
            session.getTags()
        );
    }

    // 청크 Multipart를 파일로 저장
    private void saveChunkFile(UUID sessionId, int chunkIndex, MultipartFile chunk) {
        UploadSession session = getSession(sessionId);

        String dir = createTmpDir(sessionId);
        String chunkFileName = createChunkFileName(session, chunkIndex);
        File chunkFile = new File(dir, chunkFileName);

        try (
            InputStream in = chunk.getInputStream();
            OutputStream out = new FileOutputStream(chunkFile)
        ) {
            byte[] buffer = new byte[fileServerProperties.getChunkSize()];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException ex) {
            throw new ChunkSaveException("청크 저장 실패: " + ex.getMessage(), ex);
        }
    }
}
