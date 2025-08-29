package dev.retrotv.fileserver.domain.files;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import dev.retrotv.fileserver.domain.files.dtos.InitData;
import dev.retrotv.fileserver.domain.files.dtos.SessionIdRequest;
import dev.retrotv.framework.foundation.common.response.Response;
import dev.retrotv.framework.foundation.common.response.SingleDataResponse;
import dev.retrotv.framework.foundation.common.response.SuccessResponse;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FileController {
    private final FileService fileService;

	/**
	 * 파일 컨트롤러 생성자
	 * 
	 * @param fileService 파일 서비스
	 */
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

	/**
	 * 파일 다운로드
	 * 
	 * @param id 파일 ID
	 * @return
	 */
	@GetMapping("/download/{id}")
	public ResponseEntity<String> download(@PathVariable String id) {
		return ResponseEntity.ok("Download file: " + id);
	}

	/**
	 * 업로드 상태 조회
	 * 
	 * @param sessionId 업로드 상태를 조회할 세션 ID (UUID 형식)
	 * @return
	 */
	@GetMapping("/upload/status/{sessionId:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}")
	public ResponseEntity<Response> uploadStatus(@PathVariable UUID sessionId) {
		return ResponseEntity.ok(
			new SingleDataResponse<>(fileService.getUploadStatus(sessionId))
		);
	}

	/**
	 * 파일 업로드 초기화
	 * 
	 * @param initData 초기화 정보
	 * @return
	 */
	@PostMapping("/upload/init")
	public ResponseEntity<Response> uploadInit(@RequestBody InitData initData) {
		return ResponseEntity.ok(
			new SingleDataResponse<>(fileService.initializeUploadSession(initData))
		);
	}

	/**
	 * 파일 청크 업로드
	 * 
	 * @param sessionId 세션 ID
	 * @param chunkIndex 청크 인덱스
	 * @param chunk 파일 청크
	 * @return
	 */
	@PostMapping("/upload/chunk")
	public ResponseEntity<Response> uploadChunk(
        @RequestParam UUID sessionId,
        @RequestParam int chunkIndex,
        @RequestParam MultipartFile chunk
    ) {
		// 파일 청크 업로드 로직 구현 필요
		return ResponseEntity.ok(
			new SingleDataResponse<>(fileService.saveChunk(sessionId, chunkIndex, chunk))
		);
	}

	/**
	 * 파일 업로드 완료 알림
	 * 
	 * @param request 업로드가 완료된 세션의 ID가 담긴 요청
	 * @return
	 */
	@PostMapping("/upload/complete")
	public ResponseEntity<Response> uploadComplete(@RequestBody SessionIdRequest request) {
		return ResponseEntity.ok(
			new SingleDataResponse<>(fileService.mergeChunks(request.getSessionId()))
		);
	}

	/**
	 * 파일 업로드 취소
	 * 
	 * @param request 업로드를 취소할 세션의 ID가 담긴 요청
	 * @return
	 */
	@DeleteMapping("/upload/cancel")
	public ResponseEntity<Response> uploadCancel(@RequestBody SessionIdRequest request) {
		fileService.cancelUpload(request.getSessionId());
		return ResponseEntity.ok(
			new SuccessResponse("업로드가 취소되었습니다.")
		);
	}
}
