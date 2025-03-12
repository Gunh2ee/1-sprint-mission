-- ✅ PostgreSQL에서 UUID 확장 모듈 활성화
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ✅ binary_contents 테이블
CREATE TABLE IF NOT EXISTS binary_contents (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               file_name VARCHAR(255) NOT NULL,
                                               content_type VARCHAR(100) NOT NULL,
                                               bytes BYTEA NOT NULL
);

-- ✅ users 테이블
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     username VARCHAR(50) NOT NULL UNIQUE,
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     profile_id UUID,
                                     FOREIGN KEY (profile_id) REFERENCES binary_contents(id) ON DELETE SET NULL
);

-- ✅ channels 테이블
CREATE TABLE IF NOT EXISTS channels (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        name VARCHAR(80) NOT NULL UNIQUE,
                                        type VARCHAR(20) NOT NULL CHECK (type IN ('NM', 'ENCRYPTED', 'PRIVATE')),
                                        description TEXT  -- ← description 컬럼 추가
);

-- ✅ messages 테이블
CREATE TABLE IF NOT EXISTS messages (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        content TEXT NOT NULL,
                                        channel_id UUID NOT NULL,
                                        author_id UUID,
                                        FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
                                        FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ✅ user_statuses 테이블
CREATE TABLE IF NOT EXISTS user_statuses (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             user_id UUID UNIQUE NOT NULL,
                                             last_active_at TIMESTAMP NOT NULL,
                                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ✅ read_statuses 테이블
CREATE TABLE IF NOT EXISTS read_statuses (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             user_id UUID NOT NULL,
                                             channel_id UUID NOT NULL,
                                             last_read_at TIMESTAMP NOT NULL,
                                             UNIQUE (user_id, channel_id),
                                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                             FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE
);

-- ✅ message_attachments 테이블
CREATE TABLE IF NOT EXISTS message_attachments (
                                                   message_id UUID NOT NULL,
                                                   attachment_id UUID NOT NULL,
                                                   PRIMARY KEY (message_id, attachment_id),
                                                   FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
                                                   FOREIGN KEY (attachment_id) REFERENCES binary_contents(id) ON DELETE CASCADE
);
