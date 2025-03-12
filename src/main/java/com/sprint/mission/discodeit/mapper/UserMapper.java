package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentRepository binaryContentRepository;

    public UserDto toDto(User user, boolean online) {
        if (user == null) {
            return null;
        }
        // profile: BinaryContent → id
        // online 상태는 Service/Mapper 호출 시점에 결정
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfile() != null ? user.getProfile().getId() : null,
                online
        );
    }

    // Entity → DTO (online 여부를 알 수 없으면 null로)
    public UserDto toDto(User user) {
        return toDto(user, false);
    }

    // DTO → Entity
    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User(
                dto.username(),
                dto.email(),
                "******"
        );
        // profile 설정
        if (dto.profile() != null) {
            // DB에서 BinaryContent를 찾거나, 새로 생성할 수도 있음
            BinaryContent profile = binaryContentRepository.findById(dto.profile())
                    .orElse(null);
            user.setProfile(profile);
        }
        return user;
    }
}
