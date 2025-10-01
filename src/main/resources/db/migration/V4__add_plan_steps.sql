-- V4__add_plan_steps.sql
-- Migration: Drop place_snapshot in date_plans & add plan_steps table

-- 1. Drop column place_snapshot from date_plans
ALTER TABLE date_plans
    DROP COLUMN IF EXISTS place_snapshot;

-- 2. Create plan_steps table
CREATE TABLE plan_steps (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id        UUID NOT NULL REFERENCES date_plans(id) ON DELETE CASCADE,
    step_order     INT NOT NULL,
    action_type    TEXT, -- e.g., eat, movie, cafe, walk...
    place_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    note           TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 3. Indexes
CREATE INDEX IF NOT EXISTS idx_plan_steps_plan
    ON plan_steps(plan_id);

-- 4. Trigger to auto-update updated_at
CREATE TRIGGER trg_touch_plan_steps
BEFORE UPDATE ON plan_steps
FOR EACH ROW
EXECUTE FUNCTION touch_updated_at();
