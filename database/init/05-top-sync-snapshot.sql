-- Top-2000 snapshot storage for deterministic ingestion runs

CREATE TABLE IF NOT EXISTS games.top_steam_snapshot_items (
    id UUID PRIMARY KEY,
    snapshot_date DATE NOT NULL,
    steam_app_id BIGINT NOT NULL,
    rank INT NOT NULL,
    tier VARCHAR(16) NOT NULL,
    source VARCHAR(32) NOT NULL,
    fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS games.top_steam_snapshot_runs (
    id UUID PRIMARY KEY,
    snapshot_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_items INT NOT NULL,
    official_count INT NOT NULL,
    tail_count INT NOT NULL,
    validation_passed BOOLEAN NOT NULL,
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_top_snapshot_items_date_app
    ON games.top_steam_snapshot_items (snapshot_date, steam_app_id);

CREATE INDEX IF NOT EXISTS idx_top_snapshot_items_date_rank
    ON games.top_steam_snapshot_items (snapshot_date, rank);

CREATE INDEX IF NOT EXISTS idx_top_snapshot_items_app
    ON games.top_steam_snapshot_items (steam_app_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_top_snapshot_runs_date
    ON games.top_steam_snapshot_runs (snapshot_date);
