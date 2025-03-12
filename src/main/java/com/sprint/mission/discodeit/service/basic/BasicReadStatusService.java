package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  @Override
  public ReadStatus create(ReadStatusCreateRequest request) {
    UUID userId = request.userId();
    UUID channelId = request.channelId();

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User " + userId + " not found"));
    Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new NoSuchElementException("Channel " + channelId + " not found"));

    // 중복 체크 등
    boolean exists = readStatusRepository.findAllByUserId(userId).stream()
            .anyMatch(rs -> rs.getChannel().getId().equals(channelId));
    if (exists) {
      throw new IllegalArgumentException("ReadStatus already exists for user " + userId + " & channel " + channelId);
    }

    ReadStatus readStatus = new ReadStatus(user, channel, request.lastReadAt());
    return readStatusRepository.save(readStatus);
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public ReadStatus find(UUID readStatusId) {
    return readStatusRepository.findById(readStatusId)
            .orElseThrow(() -> new NoSuchElementException("ReadStatus " + readStatusId + " not found"));
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public List<ReadStatus> findAllByUserId(UUID userId) {
    return readStatusRepository.findAllByUserId(userId);
  }

  @Override
  public ReadStatus update(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
            .orElseThrow(() -> new NoSuchElementException("ReadStatus " + readStatusId + " not found"));

    // Dirty Checking
    readStatus.update(request.newLastReadAt());
    // 반환 시점에 굳이 save(...)를 안 해도 트랜잭션 종료 시 자동 반영
    return readStatus;
  }

  @Override
  public void delete(UUID readStatusId) {
    ReadStatus rs = readStatusRepository.findById(readStatusId)
            .orElseThrow(() -> new NoSuchElementException("ReadStatus " + readStatusId + " not found"));
    readStatusRepository.delete(rs);
  }
}
