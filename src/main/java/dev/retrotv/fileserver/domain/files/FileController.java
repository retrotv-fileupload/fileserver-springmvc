package dev.retrotv.fileserver.domain.files;

import org.springframework.web.bind.annotation.*;
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
	public ResponseEntity<String> uploadInit() {
		// 업로드 초기화 로직 구현 필요
		return ResponseEntity.ok("Upload init");
	}

	@PostMapping("/upload/chunk")
	public ResponseEntity<String> uploadChunk() {
		// 파일 청크 업로드 로직 구현 필요
		return ResponseEntity.ok("Upload chunk");
	}

	@PostMapping("/upload/complete")
	public ResponseEntity<String> uploadComplete() {
		// 업로드 완료 처리 로직 구현 필요
		return ResponseEntity.ok("Upload complete");
	}

	@DeleteMapping("/upload/cancel")
	public ResponseEntity<String> uploadCancel() {
		// 업로드 취소 처리 로직 구현 필요
		return ResponseEntity.ok("Upload cancel");
	}
}
