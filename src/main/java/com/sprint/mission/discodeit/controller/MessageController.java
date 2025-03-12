package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.MessageApi;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController implements MessageApi {

  private final MessageService messageService;

  // ✅ JSON 및 Multipart 지원
  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<Message> create(
          @RequestBody MessageCreateRequest messageCreateRequest,  // ✅ JSON 요청 처리
          @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {
    System.out.println("Received MessageCreateRequest: " + messageCreateRequest);

    List<BinaryContentCreateRequest> attachmentRequests = Optional.ofNullable(attachments)
            .map(files -> files.stream()
                    .map(file -> {
                      try {
                        return new BinaryContentCreateRequest(
                                file.getOriginalFilename(),
                                file.getContentType(),
                                file.getBytes()
                        );
                      } catch (IOException e) {
                        throw new RuntimeException(e);
                      }
                    })
                    .toList())
            .orElse(new ArrayList<>());

    Message createdMessage = messageService.create(messageCreateRequest, attachmentRequests);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
  }

  @PatchMapping(path = "{messageId}")
  public ResponseEntity<Message> update(@PathVariable("messageId") UUID messageId,
                                        @RequestBody MessageUpdateRequest request) {
    Message updatedMessage = messageService.update(messageId, request);
    return ResponseEntity.ok(updatedMessage);
  }

  @DeleteMapping(path = "{messageId}")
  public ResponseEntity<Void> delete(@PathVariable("messageId") UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<Message>> findAllByChannelId(
          @RequestParam("channelId") UUID channelId
  ) {
    List<Message> messages = messageService.findAllByChannelId(channelId);
    return ResponseEntity.ok(messages);
  }

  @GetMapping("/paged")
  public PageResponse<Message> findAllByChannelIdPaged(
          @RequestParam("channelId") UUID channelId,
          @RequestParam(defaultValue = "0") int page
  ) {
    if (!(messageService instanceof BasicMessageService basicService)) {
      throw new IllegalStateException("Unsupported operation for " + messageService.getClass());
    }
    return basicService.findAllByChannelIdPaged(channelId, page);
  }
}
