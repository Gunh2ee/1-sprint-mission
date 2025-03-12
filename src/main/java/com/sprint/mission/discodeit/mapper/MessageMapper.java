package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final BinaryContentMapper binaryContentMapper;
    private final UserMapper userMapper;
    private final BinaryContentRepository binaryContentRepository;

    public MessageDto toDto(Message message) {
        if (message == null) {
            return null;
        }
        return new MessageDto(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getContent(),
                message.getChannel().getId(),
                userMapper.toDto(message.getAuthor()), // author
                message.getAttachments().stream()
                        .map(binaryContentMapper::toDto)
                        .collect(Collectors.toList())
        );
    }

    public Message toEntity(MessageDto dto) {
        if (dto == null) {
            return null;
        }
        Message message = new Message(dto.content());
        // channelId, author, attachments 등은 Service 로직에서 주입 or 조회
        return message;
    }
}
