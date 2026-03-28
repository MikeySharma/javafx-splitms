ALTER TABLE expenses ADD COLUMN title VARCHAR(255) NOT NULL DEFAULT '';

UPDATE expenses SET title = SUBSTRING(description, 1, 255) WHERE title = '';
