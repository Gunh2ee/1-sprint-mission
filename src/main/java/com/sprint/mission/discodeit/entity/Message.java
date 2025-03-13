package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "messages")
public class Message extends BaseUpdatableEntity {

    @Column(name = "content", nullable = false)
    private String content;

    // 다대일: 여러 Message가 하나의 Channel에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    @Setter
    private Channel channel;

    // 다대일: 여러 Message가 하나의 User(작성자)에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @Setter
    private User author;

    // 일대다: 하나의 Message에 여러 BinaryContent(첨부파일)
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BinaryContent> attachments = new ArrayList<>();

    protected Message() {
    }

    public Message(String content) {
        super();
        this.content = content;
    }

    public void update(String newContent) {
        boolean anyValueUpdated = false;
        if (newContent != null && !newContent.equals(this.content)) {
            this.content = newContent;
            anyValueUpdated = true;
        }

        if (anyValueUpdated) {
            touchUpdatedAt();
        }
    }

    public void addAttachment(BinaryContent bc) {
        attachments.add(bc);
        bc.setMessage(this);
    }

    public void removeAttachment(BinaryContent bc) {
        attachments.remove(bc);
        bc.setMessage(null);
    }
}
