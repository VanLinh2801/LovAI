-- ==========================================================
-- LovAI Migration: Clean up old location/tag schema
-- Keep only external_venues + activity_logs
-- Add photo_url + interacted_at
-- ==========================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'location_tags') THEN
        EXECUTE 'DROP TABLE IF EXISTS location_tags CASCADE';
    END IF;

    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'food_preferences') THEN
        EXECUTE 'DROP TABLE IF EXISTS food_preferences CASCADE';
    END IF;

    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'locations') THEN
        EXECUTE 'DROP TABLE IF EXISTS locations CASCADE';
    END IF;

    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'tags') THEN
        EXECUTE 'DROP TABLE IF EXISTS tags CASCADE';
    END IF;

    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'place_search_cache') THEN
        EXECUTE 'DROP TABLE IF EXISTS place_search_cache CASCADE';
    END IF;
END$$;


-- 2️⃣ Bảng EXTERNAL_VENUES — nguồn dữ liệu địa điểm chính
CREATE TABLE IF NOT EXISTS external_venues (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider         TEXT NOT NULL,                      -- ví dụ: 'goong', 'google'
    external_id      TEXT NOT NULL,                      -- ID từ provider
    venue_type       venue_type_enum NOT NULL,           -- 'FOOD' | 'PLACE' | 'OTHER'
    name             TEXT NOT NULL,
    address          TEXT,
    lat              DOUBLE PRECISION,
    lon              DOUBLE PRECISION,
    price_level      INT,
    rating           NUMERIC(2,1),
    provider_tags    JSONB NOT NULL DEFAULT '[]'::jsonb, -- tag gốc từ API
    normalized_tags  JSONB NOT NULL DEFAULT '[]'::jsonb, -- tag chuẩn hoá nội bộ (sau này dùng)
    photo_url        TEXT,                               -- thumbnail để hiển thị lại
    interacted_at    TIMESTAMPTZ,                        -- thời điểm user click / chọn
    meta             JSONB NOT NULL DEFAULT '{}'::jsonb,
    raw              JSONB,                              -- raw JSON từ API provider
    last_fetched_at  TIMESTAMPTZ,                        -- lần cuối gọi API
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (provider, external_id)
);

CREATE INDEX IF NOT EXISTS idx_ext_venues_provider_id
    ON external_venues(provider, external_id);
CREATE INDEX IF NOT EXISTS idx_ext_venues_normtags_gin
    ON external_venues USING GIN (normalized_tags);
CREATE INDEX IF NOT EXISTS idx_ext_venues_provtags_gin
    ON external_venues USING GIN (provider_tags);
CREATE INDEX IF NOT EXISTS idx_ext_venues_meta_gin
    ON external_venues USING GIN (meta);

-- Trigger tự cập nhật updated_at khi update
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_touch_externalvenues'
    ) THEN
        EXECUTE '
            CREATE TRIGGER trg_touch_externalvenues
            BEFORE UPDATE ON external_venues
            FOR EACH ROW
            EXECUTE FUNCTION touch_updated_at()
        ';
    END IF;
END$$;


-- 3️⃣ Bảng ACTIVITY_LOGS — ghi lại hành vi người dùng
CREATE TABLE IF NOT EXISTS activity_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    action       activity_action_enum NOT NULL,          -- 'search' | 'click' | 'save' | 'share' | ...
    target_type  TEXT,                                   -- 'external_venue', 'memory', 'plan', ...
    provider     TEXT,                                   -- ví dụ: 'goong'
    external_id  TEXT,                                   -- id từ API ngoài
    props        JSONB NOT NULL DEFAULT '{}'::jsonb,     -- metadata (search query, lat/lon, filters...)
    occurred_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_activity_user_time
    ON activity_logs(user_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_props_gin
    ON activity_logs USING GIN (props);
