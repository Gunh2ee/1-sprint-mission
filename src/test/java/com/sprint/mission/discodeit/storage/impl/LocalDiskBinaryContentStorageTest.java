package com.sprint.mission.discodeit.storage.impl;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocalDiskBinaryContentStorageTest {

    @TempDir
    Path tempDir; // JUnit5가 제공하는 임시 디렉터리

    private BinaryContentStorage storage;

    @BeforeEach
    void setUp() {
        // 1) 직접 인스턴스 생성
        LocalDiskBinaryContentStorage impl = new LocalDiskBinaryContentStorage();
        // 2) 테스트마다 임시 디렉터리를 basePath로 설정
        impl.setBasePath(tempDir.toString());
        // 3) Bean이 아니므로 @PostConstruct가 자동 실행되지 않음 → 수동으로 init() 호출
        impl.init();

        this.storage = impl;
    }

    /**
     * put(...)으로 파일을 쓰고, get(...)으로 다시 읽어오는 테스트
     */
    @Test
    void testPutAndGet() throws IOException {
        // given
        UUID contentId = UUID.randomUUID();
        byte[] data = "Hello Windows".getBytes();

        // when
        storage.put(contentId, data);

        // then
        // get(...)으로 다시 읽어와서 내용이 동일한지 확인
        try (InputStream is = storage.get(contentId)) {
            byte[] readBytes = is.readAllBytes();
            assertArrayEquals(data, readBytes, "파일에 쓴 데이터와 읽은 데이터가 달라요!");
        }
    }

    /**
     * 존재하지 않는 contentId로 get(...)을 호출하면 예외 발생
     */
    @Test
    void testGetFileNotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when & then
        assertThrows(RuntimeException.class, () -> storage.get(nonExistentId));
    }

    /**
     * put(...)으로 파일을 생성 후, download(...)로 다운로드 응답을 검증
     * 스트림을 명시적으로 닫아 Windows에서 임시 디렉터리 삭제 문제를 방지
     */
    @Test
    void testDownload() throws IOException {
        // given
        UUID contentId = UUID.randomUUID();
        byte[] data = "Download Test in Windows".getBytes();
        storage.put(contentId, data);

        // BinaryContentDto (파일 메타 정보)
        BinaryContentDto dto = new BinaryContentDto(
                contentId,
                "download.txt",
                (long) data.length,
                "text/plain",
                null
        );

        // when
        ResponseEntity<?> response = storage.download(dto);

        // then
        assertNotNull(response, "ResponseEntity가 null이면 안 됩니다.");
        assertTrue(response.getStatusCode().is2xxSuccessful(), "다운로드 응답이 2xx여야 합니다.");
        assertNotNull(response.getHeaders().getContentDisposition());
        assertTrue(response.getHeaders().getContentDisposition().isAttachment(),
                "Content-Disposition이 attachment가 아님!");
        assertEquals("text/plain", response.getHeaders().getContentType().toString());

        // body가 InputStreamResource인지 확인하고, 스트림을 명시적으로 닫아 lock 해제
        Object body = response.getBody();
        assertNotNull(body, "Response body가 null이면 안 됩니다.");
        if (body instanceof InputStreamResource isr) {
            try (InputStream is = isr.getInputStream()) {
                // 필요하다면 is.readAllBytes() 등으로 실제 내용 확인 가능
            }
        }
    }
}
