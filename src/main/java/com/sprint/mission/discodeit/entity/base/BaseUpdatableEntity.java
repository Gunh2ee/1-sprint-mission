package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;


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

    protected void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }
}