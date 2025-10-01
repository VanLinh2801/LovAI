-- ==================================================================
-- Migration V3: Add couple_invites (no token) + avatar columns
-- ==================================================================

-- 1) Add columns to users
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS avatar TEXT,
    ADD COLUMN IF NOT EXISTS phone TEXT;

-- 2) Add column to couples
ALTER TABLE couples
    ADD COLUMN IF NOT EXISTS avatar TEXT;

-- 3) Create couple_invites table
CREATE TABLE IF NOT EXISTS couple_invites (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inviter_id    UUID NOT NULL REFERENCES users(id),
    invitee_id    UUID NOT NULL REFERENCES users(id),
    expires_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    responded_at  TIMESTAMPTZ,
    CONSTRAINT couple_invites_distinct CHECK (inviter_id IS DISTINCT FROM invitee_id)
);

CREATE INDEX IF NOT EXISTS idx_couple_invites_invitee
    ON couple_invites(invitee_id);
