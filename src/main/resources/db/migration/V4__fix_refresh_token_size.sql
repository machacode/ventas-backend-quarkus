-- V4: Ampliar columna token en refresh_tokens
ALTER TABLE refresh_tokens
ALTER COLUMN token TYPE TEXT;