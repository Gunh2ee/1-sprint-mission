package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;

@Getter
@Entity
@Table(name = "user_statuses")  // 테이블명 user_statuses (User와 충돌 방지)
public class UserStatus extends BaseUpdatableEntity {

  // ✅ 1:1 관계 - User가 주인 (양방향)
  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  @Setter
  private User user;

  @Column(name = "last_active_at", nullable = false)
  private Instant lastActiveAt;

  protected UserStatus() {
    // JPA 기본 생성자
  }

  public UserStatus(User user, Instant lastActiveAt) {
    super();
    this.user = user;
    this.lastActiveAt = lastActiveAt;
  }

  /**
   * 마지막 활동 시간 업데이트
   */
  public void update(Instant newLastActiveAt) {
    if (newLastActiveAt != null && !newLastActiveAt.equals(this.lastActiveAt)) {
      this.lastActiveAt = newLastActiveAt;
      touchUpdatedAt();
    }
  }

  /**
   * 사용자 온라인 상태 확인 (최근 5분 이내 활동)
   */
  public boolean isOnline() {
    Instant fiveMinutesAgo = Instant.now().minus(Duration.ofMinutes(5));
    return lastActiveAt != null && lastActiveAt.isAfter(fiveMinutesAgo);
  }
}
