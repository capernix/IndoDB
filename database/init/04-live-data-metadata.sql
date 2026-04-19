-- Live data metadata fields for source/freshness visibility

ALTER TABLE games.games
    ADD COLUMN IF NOT EXISTS data_source VARCHAR(32),
    ADD COLUMN IF NOT EXISTS sync_status VARCHAR(32),
    ADD COLUMN IF NOT EXISTS last_synced_at TIMESTAMP;

ALTER TABLE games.game_prices
    ADD COLUMN IF NOT EXISTS data_source VARCHAR(32),
    ADD COLUMN IF NOT EXISTS sync_status VARCHAR(32),
    ADD COLUMN IF NOT EXISTS last_synced_at TIMESTAMP;

UPDATE games.games
SET data_source = 'SEEDED'
WHERE data_source IS NULL;

UPDATE games.games
SET sync_status = 'SUCCESS'
WHERE sync_status IS NULL;

UPDATE games.games
SET last_synced_at = COALESCE(last_synced_at, updated_at, created_at, CURRENT_TIMESTAMP)
WHERE last_synced_at IS NULL;

UPDATE games.game_prices
SET data_source = 'SEEDED'
WHERE data_source IS NULL;

UPDATE games.game_prices
SET sync_status = 'SUCCESS'
WHERE sync_status IS NULL;

UPDATE games.game_prices
SET last_synced_at = COALESCE(last_synced_at, last_updated, created_at, CURRENT_TIMESTAMP)
WHERE last_synced_at IS NULL;

ALTER TABLE games.games
    ALTER COLUMN data_source SET NOT NULL,
    ALTER COLUMN sync_status SET NOT NULL;

ALTER TABLE games.game_prices
    ALTER COLUMN data_source SET NOT NULL,
    ALTER COLUMN sync_status SET NOT NULL;

ALTER TABLE games.games
    ALTER COLUMN data_source SET DEFAULT 'SEEDED',
    ALTER COLUMN sync_status SET DEFAULT 'SUCCESS';

ALTER TABLE games.game_prices
    ALTER COLUMN data_source SET DEFAULT 'SEEDED',
    ALTER COLUMN sync_status SET DEFAULT 'SUCCESS';

CREATE INDEX IF NOT EXISTS idx_games_data_source ON games.games(data_source);
CREATE INDEX IF NOT EXISTS idx_games_last_synced_at ON games.games(last_synced_at DESC);
CREATE INDEX IF NOT EXISTS idx_game_prices_data_source ON games.game_prices(data_source);
CREATE INDEX IF NOT EXISTS idx_game_prices_last_synced_at ON games.game_prices(last_synced_at DESC);
