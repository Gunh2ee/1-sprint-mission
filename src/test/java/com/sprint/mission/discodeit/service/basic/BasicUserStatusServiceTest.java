package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicUserStatusServiceTest {

    @Mock
    private UserStatusRepository userStatusRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BasicUserStatusService userStatusService;

    @Test
    void testCreateSuccess() {
        // given
        UUID userId = UUID.randomUUID();
        Instant lastActiveAt = Instant.now();
        UserStatusCreateRequest req = new UserStatusCreateRequest(userId, lastActiveAt);

        // Mock user
        User mockUser = new User("testuser", "test@example.com", "pass123");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Mock userStatusRepository.findByUserId => No existing status
        when(userStatusRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Mock userStatusRepository.save => return mockStatus with some ID
        UserStatus mockStatus = new UserStatus(mockUser, lastActiveAt) {
            private final UUID mockId = UUID.randomUUID();
            @Override
            public UUID getId() {
                return mockId;
            }
        };
        when(userStatusRepository.save(any(UserStatus.class))).thenReturn(mockStatus);

        // when
        UserStatus result = userStatusService.create(req);

        // then
        assertNotNull(result.getId(), "Created UserStatus should have an ID");
        verify(userStatusRepository, times(1)).save(any(UserStatus.class));
    }

    @Test
    void testCreateDuplicate() {
        // given
        UUID userId = UUID.randomUUID();
        Instant lastActiveAt = Instant.now();
        UserStatusCreateRequest req = new UserStatusCreateRequest(userId, lastActiveAt);

        // Mock user
        User mockUser = new User("testuser", "test@example.com", "pass");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Mock existing UserStatus
        UserStatus existingStatus = new UserStatus(mockUser, Instant.now());
        when(userStatusRepository.findByUserId(userId))
                .thenReturn(Optional.of(existingStatus));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> userStatusService.create(req));
    }

    @Test
    void testCreateUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        Instant lastActiveAt = Instant.now();
        UserStatusCreateRequest req = new UserStatusCreateRequest(userId, lastActiveAt);

        // Mock user not found
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> userStatusService.create(req));
    }

    @Test
    void testUpdateByUserIdSuccess() {
        // given
        UUID userId = UUID.randomUUID();
        User mockUser = new User("testuser", "test@example.com", "pass");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Existing status
        UserStatus mockStatus = new UserStatus(mockUser, Instant.now()) {
            private final UUID mockId = UUID.randomUUID();
            @Override
            public UUID getId() {
                return mockId;
            }
        };
        when(userStatusRepository.findByUser(mockUser)).thenReturn(Optional.of(mockStatus));

        // Also mock userStatusRepository.save(...)
        when(userStatusRepository.save(mockStatus)).thenReturn(mockStatus);

        // when
        UserStatusUpdateRequest updateReq = new UserStatusUpdateRequest(Instant.now().plusSeconds(60));
        UserStatus updated = userStatusService.updateByUserId(userId, updateReq);

        // then
        assertNotNull(updated);
        verify(userStatusRepository, times(1)).save(mockStatus);
    }

    @Test
    void testUpdateByUserIdUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class,
                () -> userStatusService.updateByUserId(userId, new UserStatusUpdateRequest(Instant.now())));
    }

    @Test
    void testUpdateByUserIdUserStatusNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        User mockUser = new User("testuser", "test@example.com", "pass");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // No existing userStatus
        when(userStatusRepository.findByUser(mockUser)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class,
                () -> userStatusService.updateByUserId(userId, new UserStatusUpdateRequest(Instant.now())));
    }
}
