package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.UserStatusDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserStatusMapper {

    public UserStatusDto toDto(UserStatus entity) {
        if (entity == null) {
            return null;
        }
        return new UserStatusDto(
                entity.getId(),
                entity.getUser().getId(),
                entity.getLastActiveAt()
        );
    }

    public UserStatus toEntity(UserStatusDto dto) {
        if (dto == null) {
            return null;
        }
        // userId를 통해 User를 조회한 뒤 UserStatus 생성
        return null;
    }
}
