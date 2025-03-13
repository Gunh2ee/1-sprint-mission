package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage; // 추가
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentStorage binaryContentStorage; // 주입

    @Override
    public User create(UserCreateRequest userCreateRequest,
                       Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        String username = userCreateRequest.username();
        String email = userCreateRequest.email();

        // 유효성 검사
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User with username " + username + " already exists");
        }

        // 프로필(BinaryContent) 생성 (메타 정보 + 스토리지)
        BinaryContent profile = optionalProfileCreateRequest
                .map(profileRequest -> {
                    byte[] bytes = profileRequest.bytes();
                    BinaryContent bc = new BinaryContent(
                            profileRequest.fileName(),
                            (long) bytes.length,
                            profileRequest.contentType()
                    );
                    bc = binaryContentRepository.save(bc);

                    // byte[] -> 스토리지
                    binaryContentStorage.put(bc.getId(), bytes);

                    return bc;
                })
                .orElse(null);

        // User 엔티티 생성
        String password = userCreateRequest.password();
        User user = new User(username, email, password);

        // 프로필 연결
        if (profile != null) {
            user.setProfile(profile);
        }

        // 저장
        User createdUser = userRepository.save(user);

        // UserStatus 생성
        Instant now = Instant.now();
        UserStatus userStatus = new UserStatus(createdUser, now);
        userStatusRepository.save(userStatus);

        return createdUser;
    }

    @Override
    public UserDto find(UUID userId) {
        return userRepository.findById(userId)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public User update(UUID userId,
                       UserUpdateRequest userUpdateRequest,
                       Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));

        // 중복 검사
        String newUsername = userUpdateRequest.newUsername();
        String newEmail = userUpdateRequest.newEmail();
        if (userRepository.existsByEmail(newEmail) && !newEmail.equals(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + newEmail + " already exists");
        }
        if (userRepository.existsByUsername(newUsername) && !newUsername.equals(user.getUsername())) {
            throw new IllegalArgumentException("User with username " + newUsername + " already exists");
        }

        // 기존 프로필 삭제 (메타+스토리지)
        if (user.getProfile() != null && optionalProfileCreateRequest.isPresent()) {
            binaryContentRepository.deleteById(user.getProfile().getId());
            // 스토리지에서도 삭제하려면 별도 메소드 필요 (binaryContentStorage.delete(...))
            user.setProfile(null);
        }

        // 새 프로필 (메타+스토리지)
        BinaryContent newProfile = optionalProfileCreateRequest
                .map(profileRequest -> {
                    byte[] bytes = profileRequest.bytes();
                    BinaryContent bc = new BinaryContent(
                            profileRequest.fileName(),
                            (long) bytes.length,
                            profileRequest.contentType()
                    );
                    bc = binaryContentRepository.save(bc);

                    binaryContentStorage.put(bc.getId(), bytes);
                    return bc;
                })
                .orElse(null);

        // User 업데이트
        String newPassword = userUpdateRequest.newPassword();
        user.update(newUsername, newEmail, newPassword, null);

        if (newProfile != null) {
            user.setProfile(newProfile);
        }

        return userRepository.save(user);
    }

    @Override
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));

        // 프로필 삭제
        if (user.getProfile() != null) {
            binaryContentRepository.deleteById(user.getProfile().getId());
        }

        // UserStatus 삭제
        userStatusRepository.deleteByUserId(userId);

        userRepository.deleteById(userId);
    }

    private UserDto toDto(User user) {
        Boolean online = userStatusRepository.findByUser(user)
                .map(UserStatus::isOnline)
                .orElse(null);

        UUID profileId = user.getProfile() != null
                ? user.getProfile().getId()
                : null;

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                profileId,
                online
        );
    }
}
