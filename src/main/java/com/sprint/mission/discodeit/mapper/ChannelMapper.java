package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

    private final UserMapper userMapper;

    public ChannelDto toDto(Channel channel) {
        if (channel == null) {
            return null;
        }
        // 마지막 메시지 시간
        Instant lastMessageAt = channel.getMessages().stream()
                .map(m -> m.getCreatedAt())
                .max(Instant::compareTo)
                .orElse(Instant.MIN);

        List<UserDto> participants = new ArrayList<>();
        if (channel.getType() == ChannelType.PRIVATE) {
            for (ReadStatus rs : channel.getReadStatuses()) {

                participants.add(
                        userMapper.toDto(rs.getUser())
                );
            }
        }
        return new ChannelDto(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                participants,
                lastMessageAt
        );
    }

    public Channel toEntity(ChannelDto dto) {
        if (dto == null) {
            return null;
        }
        Channel channel = new Channel(
                dto.type(),
                dto.name(),
                dto.description()
        );
        // participants는 Channel ↔ ReadStatus ↔ User 구조
        return channel;
    }
}
