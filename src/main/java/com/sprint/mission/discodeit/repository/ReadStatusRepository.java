package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  // 기존: findAllByUserId(UUID userId), findAllByChannelId(UUID channelId), etc.
  // 예시:
  @Query("SELECT rs FROM ReadStatus rs WHERE rs.user.id = :userId")
  List<ReadStatus> findAllByUserId(UUID userId);

  @Query("SELECT rs FROM ReadStatus rs WHERE rs.channel.id = :channelId")
  List<ReadStatus> findAllByChannelId(UUID channelId);

  @Modifying
  @Query("DELETE FROM ReadStatus rs WHERE rs.channel.id = :channelId")
  void deleteAllByChannelId(UUID channelId);
}
