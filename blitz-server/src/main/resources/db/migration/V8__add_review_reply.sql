-- Add merchant reply column to reviews
-- Use standard MySQL syntax (IF NOT EXISTS is MariaDB-only)
ALTER TABLE blitz_review ADD COLUMN reply TEXT DEFAULT NULL AFTER content;
