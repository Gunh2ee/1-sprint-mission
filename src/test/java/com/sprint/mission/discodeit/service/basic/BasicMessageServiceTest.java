package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
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
class BasicMessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private UserRepository userRepository;

    // 추가: 첨부파일을 DB에 저장하는 로직
    @Mock
    private BinaryContentRepository binaryContentRepository;

    // 추가: 실제 byte[]를 스토리지에 넣는 로직
    @Mock
    private BinaryContentStorage binaryContentStorage;

    @InjectMocks
    private BasicMessageService messageService;

    @Test
    void testCreateSuccess() {
        // given
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MessageCreateRequest req = new MessageCreateRequest("Hello", channelId, userId);

        Channel mockChannel = new Channel(ChannelType.PUBLIC, "channelName", "desc");
        User mockUser = new User("username", "email", "pass");

        // Mock: 채널/유저 찾기
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(mockChannel));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Mock: 메시지 저장
        Message mockMessage = new Message("Hello");
        when(messageRepository.save(any(Message.class))).thenReturn(mockMessage);

        // Mock: BinaryContent 저장 (메타 정보)
        BinaryContent mockBinaryContent = new BinaryContent("file.txt", 4L, "text/plain");
        when(binaryContentRepository.save(any(BinaryContent.class))).thenReturn(mockBinaryContent);

        // Mock: BinaryContentStorage
        when(binaryContentStorage.put(any(UUID.class), any(byte[].class))).thenReturn(UUID.randomUUID());

        // 첨부파일 요청
        List<BinaryContentCreateRequest> attachments = List.of(
                new BinaryContentCreateRequest("file.txt", "text/plain", "test".getBytes())
        );

        // when
        Message result = messageService.create(req, attachments);

        // then
        assertNotNull(result, "Message result should not be null");
        assertEquals("Hello", result.getContent());

        // verify message save
        verify(messageRepository, times(1)).save(any(Message.class));

        // verify binary content save & storage put
        verify(binaryContentRepository, times(1)).save(any(BinaryContent.class));
        verify(binaryContentStorage, times(1)).put(any(UUID.class), eq("test".getBytes()));
    }

    @Test
    void testCreateChannelNotFound() {
        // given
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MessageCreateRequest req = new MessageCreateRequest("Hello", channelId, userId);

        // 채널 없으면 Optional.empty()
        when(channelRepository.findById(channelId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class,
                () -> messageService.create(req, List.of()));
    }
}
