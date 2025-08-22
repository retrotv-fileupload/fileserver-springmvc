package dev.retrotv.fileserver.domain.files;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import dev.retrotv.fileserver.domain.files.dtos.InitData;
import dev.retrotv.fileserver.domain.files.dtos.SessionIdRequest;
import dev.retrotv.framework.foundation.common.response.ErrorResponse;
import dev.retrotv.framework.foundation.common.response.Response;
import dev.retrotv.framework.foundation.common.response.SingleDataResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

	@GetMapping("/download/{id}")
	public ResponseEntity<String> download(@PathVariable String id) {
		log.debug("다운로드 요청된 파일 ID: {}", id);
		return ResponseEntity.ok("Download file: " + id);
	}

	@GetMapping("/upload/status/{sessionId}")
	public ResponseEntity<Response> uploadStatus(@PathVariable String sessionId) {
		log.debug("업로드 상태를 조회할 세션 ID: {}", sessionId);
		if (sessionId == null || sessionId.isEmpty()) {
			return ResponseEntity.badRequest().body(new ErrorResponse("Invalid session ID"));
		}

		return ResponseEntity.ok(
			new SingleDataResponse<>("Upload status for: " + sessionId)
		);
	}

	@PostMapping("/upload/init")
	public ResponseEntity<Response> uploadInit(@RequestBody InitData initData) {
		return ResponseEntity.ok(
			new SingleDataResponse<>(fileService.initializeUploadSession(initData))
		);
	}

	@PostMapping("/upload/chunk")
	public ResponseEntity<String> uploadChunk(
        @RequestParam String sessionId,
        @RequestParam int chunkIndex,
        @RequestParam MultipartFile chunk
    ) {
		// 파일 청크 업로드 로직 구현 필요
		return ResponseEntity.ok("Upload chunk");
	}

	@PostMapping("/upload/complete")
	public ResponseEntity<String> uploadComplete(@RequestBody SessionIdRequest request) {
		// 업로드 완료 처리 로직 구현 필요
		return ResponseEntity.ok("Upload complete: " + request.getSessionId());
	}

	@DeleteMapping("/upload/cancel")
	public ResponseEntity<String> uploadCancel(@RequestBody SessionIdRequest request) {
		// 업로드 취소 처리 로직 구현 필요
		return ResponseEntity.ok("Upload cancel: " + request.getSessionId());
	}
}
