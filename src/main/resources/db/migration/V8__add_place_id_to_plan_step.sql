-- Thêm cột place_id vào bảng plan_steps
ALTER TABLE plan_steps
ADD COLUMN place_id UUID REFERENCES external_venues(id) ON DELETE SET NULL;

-- Tạo index cho place_id để tối ưu truy vấn
CREATE INDEX IF NOT EXISTS idx_plan_steps_place
    ON plan_steps(place_id);