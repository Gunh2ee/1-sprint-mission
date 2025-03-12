-- ✅ 중복된 데이터를 방지하기 위해 기존 데이터를 먼저 삭제
DELETE FROM messages WHERE channel_id IN (SELECT id FROM channels WHERE name = 'General');
DELETE FROM channels WHERE name = 'General';
DELETE FROM users WHERE username = 'testuser';

-- ✅ 사용자 추가
INSERT INTO users (id, username, email, password, profile_id)
VALUES (gen_random_uuid(), 'testuser', 'test@example.com', 'hashed_password', NULL);

-- ✅ 채널 추가
INSERT INTO channels (id, type, name, description)
VALUES (gen_random_uuid(), 'PUBLIC', 'General', 'Welcome to the general chat!');

-- ✅ 메시지 추가 (중복 데이터 방지를 위해 `LIMIT 1` 추가)
INSERT INTO messages (id, content, channel_id, author_id)
VALUES (
           gen_random_uuid(), 'Hello, world!',
           (SELECT id FROM channels WHERE name = 'General' LIMIT 1),
       (SELECT id FROM users WHERE username = 'testuser' LIMIT 1)
    );
