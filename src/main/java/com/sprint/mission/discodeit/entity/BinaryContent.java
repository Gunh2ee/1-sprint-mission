package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Entity
@Table(name = "binary_contents")
public class BinaryContent extends BaseEntity {

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "size")
  private Long size;

  @Column(name = "content_type")
  private String contentType;

  // byte[] 제거

  // 여러 BinaryContent가 하나의 Message에 속할 수 있음 (N:1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id")
  @Setter
  private Message message;

  protected BinaryContent() {
    // JPA 기본 생성자
  }


  public BinaryContent(String fileName, Long size, String contentType) {
    super();
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
  }
}
