package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * 수정 시점(수정일)도 함께 필요한 추상 클래스.
 * BaseEntity를 상속받아 updatedAt 필드를 확장
 * Instant updatedAt
 */
@MappedSuperclass
public abstract class BaseUpdatableEntity extends BaseEntity {

    @LastModifiedDate
    @Column(name = "updated_at")
    protected Instant updatedAt;

    protected BaseUpdatableEntity() {
        super();
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 스프링 데이터 JPA 자동으로 세팅해주지 않는 상황에서
     * 직접 수정 시간을 찍고 싶다면 이 메소드를 호출?
     */
    protected void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }
}