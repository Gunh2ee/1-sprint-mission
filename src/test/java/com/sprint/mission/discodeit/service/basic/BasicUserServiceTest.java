package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private UserStatusRepository userStatusRepository;

    @InjectMocks
    private BasicUserService userService;

    @Test
    void testCreateSuccess() {
        // given
        UserCreateRequest req = new UserCreateRequest("testuser", "test@example.com", "pass123");
        User mockUser = new User("testuser", "test@example.com", "pass123");
        //mockUser.setId(UUID.randomUUID());

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserStatus mockStatus = new UserStatus(mockUser, new Date().toInstant());
        //mockStatus.setId(UUID.randomUUID());
        when(userStatusRepository.save(any(UserStatus.class))).thenReturn(mockStatus);

        // when
        User created = userService.create(req, Optional.empty());

        // then
        assertNotNull(created);
        assertEquals("testuser", created.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userStatusRepository, times(1)).save(any(UserStatus.class));
    }

    @Test
    void testCreateDuplicateEmail() {
        // given
        UserCreateRequest req = new UserCreateRequest("testuser", "test@example.com", "pass123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> userService.create(req, Optional.empty()));
    }

    @Test
    void testFindSuccess() {
        // given
        UUID userId = UUID.randomUUID();
        User mockUser = new User("testuser", "test@example.com", "pass123");
        //mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userStatusRepository.findByUser(mockUser)).thenReturn(Optional.empty());

        // when
        UserDto dto = userService.find(userId);

        // then
        assertNotNull(dto);
        assertEquals("testuser", dto.username());
        assertEquals("test@example.com", dto.email());
    }
}
