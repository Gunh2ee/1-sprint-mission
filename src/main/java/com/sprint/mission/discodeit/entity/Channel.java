package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "channels")  // 테이블명: channels
public class Channel extends BaseUpdatableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ChannelType type;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    // 양방향 매핑: Channel(1) : Message(N)
    // 메시지 쪽에서 channel 필드를 "channel"이라 했으므로 mappedBy="channel"
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    // 양방향 매핑: Channel(1) : ReadStatus(N)
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadStatus> readStatuses = new ArrayList<>();

    protected Channel() {
        // JPA 기본 생성자
    }

    public Channel(ChannelType type, String name, String description) {
        super();
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public void update(String newName, String newDescription) {
        boolean anyValueUpdated = false;
        if (newName != null && !newName.equals(this.name)) {
            this.name = newName;
            anyValueUpdated = true;
        }
        if (newDescription != null && !newDescription.equals(this.description)) {
            this.description = newDescription;
            anyValueUpdated = true;
        }

        if (anyValueUpdated) {
            // Auditing을 쓰지 않는 상황이면 수동으로 updatedAt 갱신 가능
            touchUpdatedAt();
        }
    }

    public void addMessage(Message message) {
        messages.add(message);
        message.setChannel(this);
    }

    public void removeMessage(Message message) {
        messages.remove(message);
        message.setChannel(null);
    }

    public void addReadStatus(ReadStatus rs) {
        readStatuses.add(rs);
        rs.setChannel(this);
    }

    public void removeReadStatus(ReadStatus rs) {
        readStatuses.remove(rs);
        rs.setChannel(null);
    }
}
