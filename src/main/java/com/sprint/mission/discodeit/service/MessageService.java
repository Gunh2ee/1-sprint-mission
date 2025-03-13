package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    Message create(MessageCreateRequest req, List<BinaryContentCreateRequest> attachments);

    Message find(UUID messageId);

    List<Message> findAllByChannelId(UUID channelId);

    Message update(UUID messageId, MessageUpdateRequest request);

    void delete(UUID messageId);

    /**
     * 새로 추가:
     * 채널의 메시지를 최근순(50개씩)으로 페이징 조회 → PageResponse 형태
     */
    PageResponse<Message> findAllByChannelIdPaged(UUID channelId, int pageNumber);
}
