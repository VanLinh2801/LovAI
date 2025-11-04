ALTER TABLE external_venues
    DROP COLUMN IF EXISTS photo_url,
    ADD COLUMN photo_urls JSONB NOT NULL DEFAULT '[]'::jsonb;