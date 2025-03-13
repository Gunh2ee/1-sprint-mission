package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "users")  // 테이블명: users
public class User extends BaseUpdatableEntity {

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    // ✅ 1:1 관계 - 프로필 이미지 (BinaryContent)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    @Setter
    private BinaryContent profile;

    // ✅ 1:N 관계 - 사용자가 작성한 메시지들
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    // ✅ 1:N 관계 - 사용자가 참여한 읽기 상태(ReadStatus)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadStatus> readStatuses = new ArrayList<>();

    // ✅ 1:1 관계 - 사용자 상태(UserStatus) (양방향)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserStatus userStatus;

    protected User() {
        // JPA 기본 생성자
    }

    public User(String username, String email, String password) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * 사용자 정보 업데이트
     */
    public void update(String newUsername, String newEmail, String newPassword, BinaryContent newProfile) {
        boolean anyValueUpdated = false;

        if (newUsername != null && !newUsername.equals(this.username)) {
            this.username = newUsername;
            anyValueUpdated = true;
        }
        if (newEmail != null && !newEmail.equals(this.email)) {
            this.email = newEmail;
            anyValueUpdated = true;
        }
        if (newPassword != null && !newPassword.equals(this.password)) {
            this.password = newPassword;
            anyValueUpdated = true;
        }
        if (newProfile != null && !newProfile.equals(this.profile)) {
            this.profile = newProfile;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) {
            touchUpdatedAt();
        }
    }

    /**
     * 메시지 추가 및 삭제
     */
    public void addMessage(Message message) {
        messages.add(message);
        message.setAuthor(this);
    }

    public void removeMessage(Message message) {
        messages.remove(message);
        message.setAuthor(null);
    }

    /**
     * 읽기 상태(ReadStatus) 추가 및 삭제
     */
    public void addReadStatus(ReadStatus rs) {
        readStatuses.add(rs);
        rs.setUser(this);
    }

    public void removeReadStatus(ReadStatus rs) {
        readStatuses.remove(rs);
        rs.setUser(null);
    }

    /**
     * 사용자 상태(UserStatus) 설정
     */
    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
        if (userStatus != null) {
            userStatus.setUser(this);
        }
    }
}
