package com.sprint.mission.discodeit.storage.impl;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;

/**
 * 로컬 디스크에 바이너리 데이터를 저장/로드하는 구현체.
 * discodeit.storage.type=local 일 때만 Bean 등록.
 */
@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalDiskBinaryContentStorage implements BinaryContentStorage {

    /**
     * 예: discodeit.storage.local.root-path=C:/myproject/files
     * 기본값: C:/temp/
     */
    @Value("${discodeit.storage.local.root-path:C:/temp/}")
    private String rootPath;

    private Path root; // 루트 디렉터리를 Path로 관리

    /**
     * Bean이 생성된 후 자동 호출,
     * root 디렉터리가 없으면 생성한다.
     */
    @PostConstruct
    public void init() {
        root = Paths.get(rootPath);
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create root directory: " + rootPath, e);
        }
    }

    /**
     * 테스트 시 임시 디렉터리를 사용하고 싶다면 setBasePath(...)를 통해
     * rootPath를 덮어씌운 뒤, init()을 다시 호출해야 함.
     */
    public void setBasePath(String basePath) {
        this.rootPath = basePath;
        // init(); // <- 테스트에서 동적으로 변경 시, 여기서 init() 호출 가능
    }

    /**
     * 파일 경로 규칙: {root}/{contentId}
     */
    private Path resolvePath(UUID contentId) {
        return root.resolve(contentId.toString());
    }

    @Override
    public UUID put(UUID contentId, byte[] data) {
        Path path = resolvePath(contentId);
        try (OutputStream os = Files.newOutputStream(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            os.write(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + path, e);
        }
        return contentId;
    }

    @Override
    public InputStream get(UUID contentId) {
        Path path = resolvePath(contentId);
        if (!Files.exists(path)) {
            throw new RuntimeException("File not found: " + path);
        }
        try {
            return Files.newInputStream(path, StandardOpenOption.READ);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open file: " + path, e);
        }
    }

    @Override
    public ResponseEntity<?> download(BinaryContentDto contentDto) {
        UUID contentId = contentDto.id();
        Path path = resolvePath(contentId);
        if (!Files.exists(path)) {
            throw new RuntimeException("File not found: " + path);
        }

        try {
            long fileSize = Files.size(path);
            Resource resource = new InputStreamResource(Files.newInputStream(path));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + contentDto.fileName() + "\"")
                    .contentType(MediaType.parseMediaType(contentDto.contentType()))
                    .contentLength(fileSize)
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }
}
