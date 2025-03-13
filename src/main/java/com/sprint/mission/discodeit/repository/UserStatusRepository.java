package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, UUID> {

  @Query("SELECT us FROM UserStatus us WHERE us.user.id = :userId")
  Optional<UserStatus> findByUserId(UUID userId);

  @Modifying
  @Query("DELETE FROM UserStatus us WHERE us.user.id = :userId")
  void deleteByUserId(UUID userId);

  Optional<UserStatus> findByUser(User user);
}

