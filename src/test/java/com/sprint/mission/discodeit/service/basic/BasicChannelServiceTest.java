package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BasicChannelService channelService;

    @Test
    void testCreatePublicChannel() {
        // given
        PublicChannelCreateRequest request = new PublicChannelCreateRequest("publicChannel", "desc");
        Channel mockChannel = new Channel(ChannelType.PUBLIC, "publicChannel", "desc");
        //mockChannel.setId(UUID.randomUUID());

        when(channelRepository.save(any(Channel.class))).thenReturn(mockChannel);

        // when
        Channel result = channelService.create(request);

        // then
        assertNotNull(result.getId());
        assertEquals(ChannelType.PUBLIC, result.getType());
        assertEquals("publicChannel", result.getName());
    }

    @Test
    void testFindSuccess() {
        // given
        UUID channelId = UUID.randomUUID();
        Channel mockChannel = new Channel(ChannelType.PUBLIC, "testChannel", "desc");
        //mockChannel.setId(channelId);

        when(channelRepository.findById(channelId)).thenReturn(Optional.of(mockChannel));

        // when
        ChannelDto result = channelService.find(channelId);

        // then
        assertNotNull(result);
        assertEquals("testChannel", result.name());
    }

    @Test
    void testFindNotFound() {
        // given
        UUID channelId = UUID.randomUUID();
        when(channelRepository.findById(channelId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> channelService.find(channelId));
    }

    @Test
    void testDeleteSuccess() {
        // given
        UUID channelId = UUID.randomUUID();
        Channel mockChannel = new Channel(ChannelType.PUBLIC, "testChannel", "desc");
        //mockChannel.setId(channelId);

        when(channelRepository.findById(channelId)).thenReturn(Optional.of(mockChannel));

        // when
        channelService.delete(channelId);

        // then
        verify(channelRepository, times(1)).delete(mockChannel);
    }
}
