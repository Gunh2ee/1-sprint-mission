package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
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
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;

  @Override
  public UserStatus create(UserStatusCreateRequest request) {
    UUID userId = request.userId();

    // 1) 유저 존재 여부 확인
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User " + userId + " not found"));

    // 2) 이미 UserStatus가 있으면 예외
    if (userStatusRepository.findByUserId(userId).isPresent()) {
      throw new IllegalArgumentException("UserStatus for user " + userId + " already exists");
    }

    // 3) UserStatus 생성 & 저장
    UserStatus userStatus = new UserStatus(user, request.lastActiveAt());
    return userStatusRepository.save(userStatus);
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public UserStatus find(UUID userStatusId) {
    return userStatusRepository.findById(userStatusId)
            .orElseThrow(() -> new NoSuchElementException("UserStatus " + userStatusId + " not found"));
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public List<UserStatus> findAll() {
    return userStatusRepository.findAll();
  }

  @Override
  public UserStatus update(UUID userStatusId, UserStatusUpdateRequest request) {
    // 1) UserStatus 조회
    UserStatus userStatus = userStatusRepository.findById(userStatusId)
            .orElseThrow(() -> new NoSuchElementException("UserStatus " + userStatusId + " not found"));

    // 2) 마지막 활동 시간 업데이트
    userStatus.update(request.newLastActiveAt());

    // 3) DB 반영
    return userStatusRepository.save(userStatus);
  }

  @Override
  public UserStatus updateByUserId(UUID userId, UserStatusUpdateRequest request) {
    // 1) 유저 조회
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User " + userId + " not found"));

    // 2) 해당 유저의 UserStatus 조회
    UserStatus userStatus = userStatusRepository.findByUser(user)
            .orElseThrow(() -> new NoSuchElementException("UserStatus for user " + userId + " not found"));

    // 3) 마지막 활동 시간 업데이트
    userStatus.update(request.newLastActiveAt());

    // 4) DB 반영
    return userStatusRepository.save(userStatus);
  }

  @Override
  public void delete(UUID userStatusId) {
    if (!userStatusRepository.existsById(userStatusId)) {
      throw new NoSuchElementException("UserStatus with id " + userStatusId + " not found");
    }
    userStatusRepository.deleteById(userStatusId);
  }
}
