package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.stereotype.Component;

@Component
public class BinaryContentMapper {

    public BinaryContentDto toDto(BinaryContent entity) {
        if (entity == null) {
            return null;
        }
        // byte[]가 사라졌으므로 DTO에 bytes=null 또는 아예 제거
        return new BinaryContentDto(
                entity.getId(),
                entity.getFileName(),
                entity.getSize(),
                entity.getContentType(),
                null
        );
    }

    public BinaryContent toEntity(BinaryContentDto dto) {
        if (dto == null) {
            return null;
        }
        // 3-파라미터 생성자만 존재
        return new BinaryContent(
                dto.fileName(),
                dto.size(),
                dto.contentType()
        );
    }
}
