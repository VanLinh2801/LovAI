-- ==========================================================
-- V9: Create table save_place for user's saved venues
-- Links users to external_venues with uniqueness per user+venue
-- ==========================================================

CREATE TABLE IF NOT EXISTS save_place (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    venue_id     UUID NOT NULL REFERENCES external_venues(id) ON DELETE CASCADE,
    note         TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, venue_id)
);

-- Useful indexes
CREATE INDEX IF NOT EXISTS idx_save_place_user ON save_place(user_id);
CREATE INDEX IF NOT EXISTS idx_save_place_venue ON save_place(venue_id);
CREATE INDEX IF NOT EXISTS idx_save_place_user_created_at ON save_place(user_id, created_at DESC);

-- Auto-update updated_at on update
CREATE TRIGGER trg_touch_save_place
BEFORE UPDATE ON save_place FOR EACH ROW EXECUTE FUNCTION touch_updated_at();


