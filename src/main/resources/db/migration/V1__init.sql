-- LovAI: Full schema for MVP (PostgreSQL 14+)

-- ---------- Extensions ----------
CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- gen_random_uuid(), crypto utils
CREATE EXTENSION IF NOT EXISTS citext;     -- case-insensitive text (CITEXT)
CREATE EXTENSION IF NOT EXISTS btree_gin;  -- GIN for btree-compatible ops (optional)

-- ---------- Enums ----------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'gender_enum') THEN
        CREATE TYPE gender_enum AS ENUM ('male','female','other','prefer_not_say');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'plan_status_enum') THEN
        CREATE TYPE plan_status_enum AS ENUM ('planned','done','canceled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'platform_enum') THEN
        CREATE TYPE platform_enum AS ENUM ('android','ios','web');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'notif_channel_enum') THEN
        CREATE TYPE notif_channel_enum AS ENUM ('IN_APP','PUSH');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'notif_status_enum') THEN
        CREATE TYPE notif_status_enum AS ENUM ('PENDING','SCHEDULED','SENT','CANCELED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'delivery_status_enum') THEN
        CREATE TYPE delivery_status_enum AS ENUM ('QUEUED','DELIVERED','FAILED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'venue_type_enum') THEN
        CREATE TYPE venue_type_enum AS ENUM ('FOOD','PLACE','OTHER');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'activity_action_enum') THEN
        CREATE TYPE activity_action_enum AS ENUM ('search','click','save','open','delete','share');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'preference_enum') THEN
        CREATE TYPE preference_enum AS ENUM ('like','dislike');
    END IF;
END$$;

-- ---------- Touch updated_at trigger ----------
CREATE OR REPLACE FUNCTION touch_updated_at() RETURNS trigger AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END; $$ LANGUAGE plpgsql;

-- ==================================================================
-- 1) Core
-- ==================================================================

-- Users (Account)
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           CITEXT UNIQUE NOT NULL,
    password_hash   TEXT NOT NULL,
    name            TEXT,
    gender          gender_enum,
    date_of_birth   DATE,
    settings_json   JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users (deleted_at);
CREATE INDEX IF NOT EXISTS idx_users_settings_gin ON users USING GIN (settings_json);

CREATE TRIGGER trg_touch_users
BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

-- Partner Profiles
CREATE TABLE partner_profiles (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    name           TEXT NOT NULL,
    gender         gender_enum,
    date_of_birth  DATE,
    phone          TEXT,
    email          CITEXT,
    notes          TEXT,
    meta           JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_partner_profiles_meta_gin ON partner_profiles USING GIN (meta);

CREATE TRIGGER trg_touch_partner_profiles
BEFORE UPDATE ON partner_profiles FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

-- Couples
CREATE TABLE couples (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title            TEXT,
    anniversary_date DATE,
    user1_id         UUID NOT NULL REFERENCES users(id),
    user2_id         UUID REFERENCES users(id),
    partner_profile_id UUID REFERENCES partner_profiles(id),
    meta             JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at       TIMESTAMPTZ,
    CONSTRAINT couples_distinct_members CHECK (user1_id IS DISTINCT FROM user2_id),
    CONSTRAINT couples_partner_xor CHECK (
        ((user2_id IS NOT NULL)::int + (partner_profile_id IS NOT NULL)::int) <= 1
    )
);
CREATE INDEX IF NOT EXISTS idx_couples_user1 ON couples(user1_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_couples_user2 ON couples(user2_id) WHERE user2_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_couples_partner_profile ON couples(partner_profile_id) WHERE partner_profile_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_couples_meta_gin ON couples USING GIN (meta);
CREATE INDEX IF NOT EXISTS idx_couples_deleted_at ON couples (deleted_at);

CREATE TRIGGER trg_touch_couples
BEFORE UPDATE ON couples FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

-- Memories
CREATE TABLE memories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    couple_id       UUID NOT NULL REFERENCES couples(id),
    title           TEXT NOT NULL,
    description     TEXT,
    happened_at     TIMESTAMPTZ,
    location_text   TEXT,
    media_count     INT NOT NULL DEFAULT 0,
    meta            JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ
    -- Full-text search (generated column to allow GIN index safely)
--    search_tsv tsvector GENERATED ALWAYS AS (
--        setweight(to_tsvector('simple', coalesce(title,'')), 'A') ||
--        setweight(to_tsvector('simple', coalesce(description,'')), 'B')
--    ) STORED
);

ALTER TABLE memories ADD COLUMN search_tsv tsvector;

CREATE FUNCTION memories_tsvector_update() RETURNS trigger AS $$
BEGIN
  NEW.search_tsv :=
    setweight(to_tsvector('simple', coalesce(NEW.title,'')), 'A') ||
    setweight(to_tsvector('simple', coalesce(NEW.description,'')), 'B');
  RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_memories_tsvector
BEFORE INSERT OR UPDATE ON memories
FOR EACH ROW EXECUTE FUNCTION memories_tsvector_update();


CREATE INDEX IF NOT EXISTS idx_memories_tsv ON memories USING GIN (search_tsv);
CREATE INDEX IF NOT EXISTS idx_memories_couple ON memories(couple_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_memories_meta_gin ON memories USING GIN (meta);

CREATE TRIGGER trg_touch_memories
BEFORE UPDATE ON memories FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

-- Memory media
CREATE TABLE memory_media (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    memory_id   UUID NOT NULL REFERENCES memories(id) ON DELETE CASCADE,
    url         TEXT NOT NULL,
    media_type  TEXT CHECK (media_type IN ('image','video','audio','file')),
    width       INT,
    height      INT,
    exif_json   JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_media_memory ON memory_media(memory_id);
CREATE INDEX IF NOT EXISTS idx_media_exif_gin ON memory_media USING GIN (exif_json);

-- Date plans
CREATE TABLE date_plans (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    couple_id         UUID NOT NULL REFERENCES couples(id),
    title             TEXT NOT NULL,
    start_time        TIMESTAMPTZ,
    end_time          TIMESTAMPTZ,
    status            plan_status_enum NOT NULL DEFAULT 'planned',
    note              TEXT,
    place_snapshot    JSONB,
    weather_snapshot  JSONB,
    meta              JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    CONSTRAINT plan_time_check CHECK (end_time IS NULL OR start_time IS NULL OR end_time >= start_time)
);
CREATE INDEX IF NOT EXISTS idx_plans_couple ON date_plans(couple_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_plans_status ON date_plans(status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_plans_place_gin ON date_plans USING GIN (place_snapshot);
CREATE INDEX IF NOT EXISTS idx_plans_weather_gin ON date_plans USING GIN (weather_snapshot);
CREATE INDEX IF NOT EXISTS idx_plans_meta_gin ON date_plans USING GIN (meta);

CREATE TRIGGER trg_touch_date_plans
BEFORE UPDATE ON date_plans FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

-- Locations
CREATE TABLE locations (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          TEXT NOT NULL,
    address       TEXT,
    lat           DOUBLE PRECISION,
    lon           DOUBLE PRECISION,
    price_level   INT,
    rating        NUMERIC(2,1),
    tags          JSONB NOT NULL DEFAULT '[]'::jsonb,
    meta          JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at    TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_locations_tags_gin ON locations USING GIN (tags);
CREATE INDEX IF NOT EXISTS idx_locations_meta_gin ON locations USING GIN (meta);

CREATE TRIGGER trg_touch_locations
BEFORE UPDATE ON locations FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

CREATE TABLE tags (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        TEXT UNIQUE NOT NULL,
    name        TEXT NOT NULL,
    meta        JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_touch_tags
BEFORE UPDATE ON tags FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

CREATE TABLE location_tags (
    location_id UUID NOT NULL REFERENCES locations(id) ON DELETE CASCADE,
    tag_id      UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (location_id, tag_id)
);

CREATE TABLE food_preferences (
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tag_id      UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    preference  preference_enum NOT NULL,
    note        TEXT,
    PRIMARY KEY (user_id, tag_id)
);

-- ==================================================================
-- 2) Third-party integration (reference + cache)
-- ==================================================================

CREATE TABLE external_venues (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider         TEXT NOT NULL,
    external_id      TEXT NOT NULL,
    venue_type       venue_type_enum NOT NULL,
    name             TEXT NOT NULL,
    address          TEXT,
    lat              DOUBLE PRECISION,
    lon              DOUBLE PRECISION,
    price_level      INT,
    rating           NUMERIC(2,1),
    provider_tags    JSONB NOT NULL DEFAULT '[]'::jsonb,
    normalized_tags  JSONB NOT NULL DEFAULT '[]'::jsonb,
    meta             JSONB NOT NULL DEFAULT '{}'::jsonb,
    raw              JSONB,
    last_fetched_at  TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (provider, external_id)
);
CREATE INDEX IF NOT EXISTS idx_ext_venues_normtags_gin ON external_venues USING GIN (normalized_tags);
CREATE INDEX IF NOT EXISTS idx_ext_venues_provtags_gin ON external_venues USING GIN (provider_tags);
CREATE INDEX IF NOT EXISTS idx_ext_venues_meta_gin      ON external_venues USING GIN (meta);

CREATE TRIGGER trg_touch_externalvenues
BEFORE UPDATE ON external_venues FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

CREATE TABLE place_search_cache (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    query_fingerprint  TEXT NOT NULL,
    results            JSONB NOT NULL,
    ttl_seconds        INT NOT NULL DEFAULT 3600,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    expire_at          TIMESTAMPTZ
);

-- Trigger function
CREATE OR REPLACE FUNCTION set_expire_at() RETURNS trigger AS $$
BEGIN
  NEW.expire_at := NEW.created_at + (NEW.ttl_seconds * interval '1 second');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: before insert or update
CREATE TRIGGER trg_set_expire_at
BEFORE INSERT OR UPDATE ON place_search_cache
FOR EACH ROW
EXECUTE FUNCTION set_expire_at();

-- Index
CREATE UNIQUE INDEX IF NOT EXISTS uniq_place_search_cache_fpr ON place_search_cache(query_fingerprint);
CREATE INDEX IF NOT EXISTS idx_place_search_cache_expire ON place_search_cache(expire_at);


CREATE TABLE activity_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    action       activity_action_enum NOT NULL,
    target_type  TEXT,
    provider     TEXT,
    external_id  TEXT,
    props        JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_activity_user_time ON activity_logs(user_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_props_gin  ON activity_logs USING GIN (props);

-- ==================================================================
-- 3) Notifications
-- ==================================================================

CREATE TABLE push_tokens (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform     platform_enum NOT NULL,
    token        TEXT NOT NULL,
    device_id    TEXT,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS uniq_push_token ON push_tokens(token);
CREATE INDEX IF NOT EXISTS idx_push_user_active ON push_tokens(user_id) WHERE is_active;

CREATE TABLE notifications (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_code  TEXT,
    channel        notif_channel_enum NOT NULL,
    title          TEXT NOT NULL,
    body           TEXT NOT NULL,
    payload        JSONB NOT NULL DEFAULT '{}'::jsonb,
    category       TEXT,
    created_by     UUID REFERENCES users(id),
    status         notif_status_enum NOT NULL DEFAULT 'PENDING',
    scheduled_at   TIMESTAMPTZ,
    sent_at        TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_notif_status_sched ON notifications(status, scheduled_at);
CREATE INDEX IF NOT EXISTS idx_notif_payload_gin   ON notifications USING GIN (payload);

CREATE TABLE notification_recipients (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id  UUID NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    delivery_status  delivery_status_enum,
    delivered_at     TIMESTAMPTZ,
    read_at          TIMESTAMPTZ,
    error_code       TEXT,
    retry_count      INT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_notirec_notif   ON notification_recipients(notification_id);
CREATE INDEX IF NOT EXISTS idx_notirec_user    ON notification_recipients(user_id);
CREATE INDEX IF NOT EXISTS idx_notirec_status  ON notification_recipients(delivery_status);

CREATE TABLE notification_events (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id  UUID NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    user_id          UUID REFERENCES users(id) ON DELETE SET NULL,
    event_type       TEXT NOT NULL,
    meta             JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_notievents_notif_time ON notification_events(notification_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_notievents_meta_gin   ON notification_events USING GIN (meta);

CREATE TABLE notification_templates (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        TEXT NOT NULL,
    lang        TEXT NOT NULL,
    title_tpl   TEXT NOT NULL,
    body_tpl    TEXT NOT NULL,
    payload_tpl JSONB,
    meta        JSONB NOT NULL DEFAULT '{}'::jsonb,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (code, lang)
);

-- ==================================================================
-- 4) Settings
-- ==================================================================

CREATE TABLE app_settings (
    key         TEXT PRIMARY KEY,
    value       JSONB NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_touch_app_settings
BEFORE UPDATE ON app_settings FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

CREATE TABLE couple_settings (
    couple_id   UUID NOT NULL REFERENCES couples(id) ON DELETE CASCADE,
    key         TEXT NOT NULL,
    value       JSONB NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (couple_id, key)
);
CREATE TRIGGER trg_touch_couple_settings
BEFORE UPDATE ON couple_settings FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

CREATE TABLE user_settings (
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    key         TEXT NOT NULL,
    value       JSONB NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, key)
);
CREATE TRIGGER trg_touch_user_settings
BEFORE UPDATE ON user_settings FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

CREATE TABLE user_notification_prefs (
    user_id        UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    push_enabled   BOOLEAN NOT NULL DEFAULT TRUE,
    quiet_start    TIME,
    quiet_end      TIME,
    dnd_enabled    BOOLEAN NOT NULL DEFAULT FALSE,
    categories     JSONB NOT NULL DEFAULT '[]'::jsonb,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_touch_user_prefs
BEFORE UPDATE ON user_notification_prefs FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

CREATE TABLE user_notification_mutes (
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category     TEXT NOT NULL,
    muted_until  TIMESTAMPTZ,
    PRIMARY KEY (user_id, category)
);

-- =================== END OF SCHEMA ================================
