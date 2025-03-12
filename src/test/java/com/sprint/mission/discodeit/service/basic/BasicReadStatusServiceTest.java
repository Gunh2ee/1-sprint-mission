package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicReadStatusServiceTest {

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChannelRepository channelRepository;

    @InjectMocks
    private BasicReadStatusService readStatusService;

    @Test
    void testCreateSuccess() {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, Instant.now());

        User mockUser = new User("username", "email", "pass");
        //mockUser.setId(userId);
        Channel mockChannel = new Channel(ChannelType.PUBLIC, "channelName", "desc");
        //mockChannel.setId(channelId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(mockChannel));
        when(readStatusRepository.findAllByUserId(userId)).thenReturn(List.of());

        ReadStatus mockRS = new ReadStatus(mockUser, mockChannel, Instant.now());
        //mockRS.setId(UUID.randomUUID());
        when(readStatusRepository.save(any(ReadStatus.class))).thenReturn(mockRS);

        // when
        ReadStatus result = readStatusService.create(request);

        // then
        assertNotNull(result.getId());
        verify(readStatusRepository, times(1)).save(any(ReadStatus.class));
    }

    @Test
    void testCreateUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, Instant.now());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> readStatusService.create(request));
    }
}
