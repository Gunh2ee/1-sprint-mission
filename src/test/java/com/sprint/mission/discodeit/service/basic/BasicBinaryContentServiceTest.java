package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage; // 추가
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicBinaryContentServiceTest {

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private BinaryContentStorage binaryContentStorage; // 추가 (필수)

    @InjectMocks
    private BasicBinaryContentService binaryContentService;

    @Test
    void testCreate() {
        // given
        BinaryContentCreateRequest request = new BinaryContentCreateRequest(
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        // 엔티티(메타 정보만)
        BinaryContent mockEntity = new BinaryContent(
                "test.txt",
                11L, // "Hello World".length()
                "text/plain"
        );

        // Mock 동작 정의
        when(binaryContentRepository.save(any(BinaryContent.class))).thenReturn(mockEntity);
        when(binaryContentStorage.put(any(UUID.class), any(byte[].class))).thenReturn(UUID.randomUUID());

        // when
        BinaryContent result = binaryContentService.create(request);

        // then
        assertNotNull(result);
        assertEquals("test.txt", result.getFileName());
        assertEquals(11L, result.getSize());
        assertEquals("text/plain", result.getContentType());

        // Storage, Repository 호출 여부 검증 (선택)
        verify(binaryContentRepository, times(1)).save(any(BinaryContent.class));
        verify(binaryContentStorage, times(1)).put(any(UUID.class), eq("Hello World".getBytes()));
    }

    @Test
    void testFindSuccess() {
        // given
        UUID id = UUID.randomUUID();
        BinaryContent mockEntity = new BinaryContent("sample.txt", 6L, "text/plain");

        when(binaryContentRepository.findById(id)).thenReturn(Optional.of(mockEntity));

        // when
        BinaryContent result = binaryContentService.find(id);

        // then
        assertNotNull(result);
        assertEquals("sample.txt", result.getFileName());
        assertEquals(6L, result.getSize());
        assertEquals("text/plain", result.getContentType());
    }

    @Test
    void testFindNotFound() {
        // given
        UUID id = UUID.randomUUID();
        when(binaryContentRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> binaryContentService.find(id));
    }

    @Test
    void testDeleteSuccess() {
        // given
        UUID id = UUID.randomUUID();
        when(binaryContentRepository.existsById(id)).thenReturn(true);

        // when
        binaryContentService.delete(id);

        // then
        verify(binaryContentRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteNotFound() {
        // given
        UUID id = UUID.randomUUID();
        when(binaryContentRepository.existsById(id)).thenReturn(false);

        // when & then
        assertThrows(NoSuchElementException.class, () -> binaryContentService.delete(id));
    }
}
