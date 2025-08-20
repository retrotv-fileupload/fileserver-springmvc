package dev.retrotv.fileserver.domain.files;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import dev.retrotv.fileserver.domain.files.dtos.InitData;
import dev.retrotv.fileserver.domain.files.dtos.SessionIdRequest;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/files")
public class FileController {

	@GetMapping("/download/{id}")
	public ResponseEntity<String> downloadFile(@PathVariable String id) {
		// 파일 다운로드 로직 구현 필요
		return ResponseEntity.ok("Download file: " + id);
	}

	@GetMapping("/upload/status/{id}")
	public ResponseEntity<String> uploadStatus(@PathVariable String id) {
		// 업로드 상태 조회 로직 구현 필요
		return ResponseEntity.ok("Upload status for: " + id);
	}

	@PostMapping("/upload/init")
	public ResponseEntity<String> uploadInit(@RequestBody InitData initData) {
		// 업로드 초기화 로직 구현 필요
		return ResponseEntity.ok("Upload init: " + initData.getFileName());
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
