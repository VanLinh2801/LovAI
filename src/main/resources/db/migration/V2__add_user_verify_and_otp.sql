-- 1. Thêm cột is_verified cho bảng users
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Tạo bảng email_verification_tokens
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    otp_code      VARCHAR(10) NOT NULL,
    expires_at    TIMESTAMPTZ NOT NULL,
    verified      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Tạo index để tối ưu query
CREATE INDEX IF NOT EXISTS idx_email_verification_user
    ON email_verification_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_email_verification_expires
    ON email_verification_tokens(expires_at);
