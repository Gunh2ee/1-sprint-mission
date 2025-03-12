package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // 클래스 단위 트랜잭션
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;

  /**
   * 공개 채널 생성
   */
  @Override
  public Channel create(PublicChannelCreateRequest request) {
    Channel channel = new Channel(
            ChannelType.PUBLIC,
            request.name(),
            request.description()
    );
    // 새 엔티티이므로 save() 필요
    return channelRepository.save(channel);
  }

  /**
   * 비공개 채널 생성
   * - Channel 생성
   * - 각 참가자(User)에 대해 ReadStatus를 만들고, channel.addReadStatus(...)로 연결
   * - cascade=ALL + orphanRemoval=true 덕분에 channel만 save(...) 해도 ReadStatus가 자동 영속화
   */
  @Override
  public Channel create(PrivateChannelCreateRequest request) {
    // 1) 채널 생성
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);

    // 2) 참가자 목록(유저 ID들)을 실제 User 엔티티로 변환 → ReadStatus 생성 → channel에 추가
    for (UUID userId : request.participantIds()) {
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new NoSuchElementException("User " + userId + " not found"));

      ReadStatus rs = new ReadStatus(user, channel, Instant.MIN);
      // 부모에 add → cascade=ALL로 인해 자식도 영속화
      channel.addReadStatus(rs);
    }

    // 3) 채널 저장 → 연관된 ReadStatus도 함께 DB 반영
    return channelRepository.save(channel);
  }

  /**
   * 채널 단건 조회 (읽기 전용)
   */
  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public ChannelDto find(UUID channelId) {
    Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new NoSuchElementException("Channel " + channelId + " not found"));
    return toDto(channel);
  }

  /**
   * 특정 유저가 볼 수 있는 채널 목록
   */
  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public List<ChannelDto> findAllByUserId(UUID userId) {
    // 유저가 속한 채널 목록 (간단히 JPQL을 써도 됨)
    // 여기서는 DB 모든 채널을 불러와 필터링
    List<Channel> allChannels = channelRepository.findAll();

    return allChannels.stream()
            .filter(ch -> {
              if (ch.getType() == ChannelType.PUBLIC) {
                return true;
              }
              // PRIVATE 채널인 경우, readStatuses에 userId가 포함되어야
              return ch.getReadStatuses().stream()
                      .anyMatch(rs -> rs.getUser().getId().equals(userId));
            })
            .map(this::toDto)
            .collect(Collectors.toList());
  }

  /**
   * 공개 채널 수정
   * - Dirty Checking으로 channel.update(...)만 호출해도 자동 반영
   */
  @Override
  public Channel update(UUID channelId, PublicChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new NoSuchElementException("Channel " + channelId + " not found"));

    if (channel.getType() == ChannelType.PRIVATE) {
      throw new IllegalArgumentException("Private channel cannot be updated");
    }

    // Dirty Checking → 트랜잭션 종료 시점에 UPDATE 쿼리
    channel.update(request.newName(), request.newDescription());

    // 굳이 channelRepository.save(...)를 호출하지 않아도 DB 반영됨
    // 하지만 즉시 반영 원하면 save 호출 가능
    return channel;
  }

  /**
   * 채널 삭제
   * - cascade=ALL + orphanRemoval=true 이므로,
   *   channel만 지워도 연관된 Message, ReadStatus가 모두 삭제됨
   */
  @Override
  public void delete(UUID channelId) {
    Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new NoSuchElementException("Channel " + channelId + " not found"));

    // 자식(Message, ReadStatus)도 함께 제거
    channelRepository.delete(channel);
  }

  /**
   * Entity -> DTO 변환
   */
  private ChannelDto toDto(Channel channel) {
    // 마지막 메시지 시각
    // LAZY 로딩: channel.getMessages() 접근 시점에 쿼리 발생 가능
    Instant lastMessageAt = channel.getMessages().stream()
            .map(Message::getCreatedAt)
            .max(Instant::compareTo)
            .orElse(Instant.MIN);

    // PRIVATE 채널인 경우, 참가자 목록 추출
    List<UserDto> participants = new ArrayList<>();
    if (channel.getType() == ChannelType.PRIVATE) {
      // 참가자 목록을 UserDto로 변환하여 리스트에 추가
      for (ReadStatus rs : channel.getReadStatuses()) {
        UserDto userDto = new UserDto(
                rs.getUser().getId(),
                rs.getUser().getUsername(),
                rs.getUser().getEmail(),
                rs.getUser().getProfile() != null ? rs.getUser().getProfile().getId() : null,
                null // 온라인 상태는 따로 처리할 수 있음
        );
        participants.add(userDto);
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
}
