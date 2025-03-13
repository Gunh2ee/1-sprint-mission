package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse; // 페이지 DTO
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    public Message create(MessageCreateRequest req, List<BinaryContentCreateRequest> attachmentRequests) {
        Channel channel = channelRepository.findById(req.channelId())
                .orElseThrow(() -> new NoSuchElementException("Channel with id " + req.channelId() + " not found"));
        User author = userRepository.findById(req.authorId())
                .orElseThrow(() -> new NoSuchElementException("Author with id " + req.authorId() + " not found"));

        Message message = new Message(req.content());
        message.setChannel(channel);
        message.setAuthor(author);

        // 첨부파일(메타 정보 + 스토리지)
        for (BinaryContentCreateRequest attachmentReq : attachmentRequests) {
            byte[] bytes = attachmentReq.bytes();
            BinaryContent bc = new BinaryContent(
                    attachmentReq.fileName(),
                    (long) bytes.length,
                    attachmentReq.contentType()
            );
            bc = binaryContentRepository.save(bc);

            // 실제 이진 데이터는 스토리지에
            binaryContentStorage.put(bc.getId(), bytes);

            message.addAttachment(bc);
        }

        return messageRepository.save(message);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Message find(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message " + messageId + " not found"));
    }

    /**
     * 기존: 채널 ID로 모든 메시지 조회 (페이징 없음)
     */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Message> findAllByChannelId(UUID channelId) {
        return messageRepository.findAllByChannelId(channelId);
    }

    /**
     * 새 메소드:
     * 채널(channelId)의 메시지를 최근(createdAt DESC) 순으로
     * 50개씩 pageNumber 페이지 조회해 PageResponse로 반환
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public PageResponse<Message> findAllByChannelIdPaged(UUID channelId, int pageNumber) {
        // 1) 페이지 크기 50, 정렬: createdAt desc
        int pageSize = 50;
        PageRequest pageRequest = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by("createdAt").descending()
        );

        // 2) JPA Page<Message> 조회
        Page<Message> page = messageRepository.findAllByChannelId(channelId, pageRequest);

        // 3) Page -> PageResponse
        List<Message> content = page.getContent();
        boolean hasNext = page.hasNext();
        // totalElements는 필요 없다면 null
        Long totalElements = page.getTotalElements();
        // Long totalElements = null; // ← 이렇게 하면 총개수 미사용

        // 4) PageResponse 생성
        return new PageResponse<>(
                content,       // content
                pageNumber,    // number
                pageSize,      // size
                hasNext,       // hasNext
                totalElements  // totalElements
        );
    }

    @Override
    public Message update(UUID messageId, MessageUpdateRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message " + messageId + " not found"));
        message.update(request.newContent());
        return message;
    }

    @Override
    public void delete(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message " + messageId + " not found"));
        messageRepository.delete(message);
    }
}
