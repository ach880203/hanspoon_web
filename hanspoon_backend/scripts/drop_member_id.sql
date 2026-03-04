-- Backup first!
-- 1) Optional: show create table to check foreign keys
--    SHOW CREATE TABLE instructor\G
-- 2) If there's a foreign key constraint on member_id, drop it first:
--    ALTER TABLE instructor DROP FOREIGN KEY <fk_name>;
-- 3) Then drop the column:
ALTER TABLE instructor DROP COLUMN member_id;

-- If you prefer to make member_id nullable instead of dropping:
-- ALTER TABLE instructor MODIFY COLUMN member_id BIGINT NULL;
