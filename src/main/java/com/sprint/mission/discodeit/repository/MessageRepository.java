package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

  // 1) 채널 ID로 메시지 전체 조회 (기존)
  @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId")
  List<Message> findAllByChannelId(UUID channelId);

  // 2) 채널 ID로 메시지를 페이징 조회 (신규)
  @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId")
  Page<Message> findAllByChannelId(UUID channelId, Pageable pageable);

  // (예시) 채널 ID 기반 삭제
  @Modifying
  @Query("DELETE FROM Message m WHERE m.channel.id = :channelId")
  void deleteAllByChannelId(UUID channelId);
}
