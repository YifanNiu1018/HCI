-- 修复 comments 表结构，使 dish_id 和 note_id 可以为 NULL
-- 执行方法：在 H2 Console 中执行此 SQL，或通过应用启动时执行

ALTER TABLE comments ALTER COLUMN dish_id DROP NOT NULL;
ALTER TABLE comments ALTER COLUMN note_id DROP NOT NULL;

