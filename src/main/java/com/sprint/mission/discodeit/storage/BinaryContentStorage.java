package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.UUID;

/**
 * 바이너리 데이터(파일)를 별도 스토리지에 저장/로드/다운로드하는 컴포넌트.
 * DB에는 메타정보만 저장하고, 실제 byte[]는 여기서 관리.
 */
public interface BinaryContentStorage {

    /**
     * 이진 데이터를 스토리지에 저장.
     */
    UUID put(UUID contentId, byte[] data);

    /**
     * 스토리지에서 이진 데이터를 로드.
     */
    InputStream get(UUID contentId);

    /**
     * HTTP로 파일을 다운로드할 수 있는 ResponseEntity를 생성.
     */
    ResponseEntity<?> download(BinaryContentDto contentDto);
}
