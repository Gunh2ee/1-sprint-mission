package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BinaryContentRepository extends JpaRepository<BinaryContent, UUID> {

  // 쿼리 메소드 추가 예정
  // 사실 JpaRepository 기본 메소드 findAllById(...)로 대체 가능
}
