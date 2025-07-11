-- IndiaDB Games - Initial Database Setup
-- This script creates the basic database structure

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create schemas
CREATE SCHEMA IF NOT EXISTS games;
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS analytics;

-- Set search path
SET search_path TO games, users, analytics, public;

-- Create enum types
CREATE TYPE platform_type AS ENUM ('STEAM', 'EPIC', 'GOG', 'OTHER');
CREATE TYPE price_currency AS ENUM ('INR', 'USD', 'EUR', 'GBP');
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN', 'MODERATOR');
CREATE TYPE notification_type AS ENUM ('PRICE_DROP', 'FREE_GAME', 'WISHLIST_AVAILABLE', 'FORUM_REPLY');

-- Games schema tables
CREATE TABLE games.platforms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    type platform_type NOT NULL,
    api_endpoint VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE games.games (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    short_description TEXT,
    developer VARCHAR(255),
    publisher VARCHAR(255),
    release_date DATE,
    genres TEXT[], -- Array of genre strings
    tags TEXT[], -- Array of tag strings
    metacritic_score INTEGER,
    steam_app_id BIGINT,
    epic_catalog_item_id VARCHAR(255),
    gog_product_id BIGINT,
    header_image_url VARCHAR(500),
    screenshots TEXT[], -- Array of screenshot URLs
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE games.game_prices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    game_id UUID NOT NULL REFERENCES games.games(id),
    platform_id INTEGER NOT NULL REFERENCES games.platforms(id),
    current_price DECIMAL(10,2),
    original_price DECIMAL(10,2),
    discount_percentage INTEGER DEFAULT 0,
    currency price_currency DEFAULT 'INR',
    is_free BOOLEAN DEFAULT false,
    is_on_sale BOOLEAN DEFAULT false,
    sale_end_date TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(game_id, platform_id)
);

CREATE TABLE games.price_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    game_id UUID NOT NULL REFERENCES games.games(id),
    platform_id INTEGER NOT NULL REFERENCES games.platforms(id),
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2),
    discount_percentage INTEGER DEFAULT 0,
    currency price_currency DEFAULT 'INR',
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users schema tables
CREATE TABLE users.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role user_role DEFAULT 'USER',
    steam_id VARCHAR(255),
    epic_account_id VARCHAR(255),
    gog_user_id VARCHAR(255),
    email_verified BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users.wishlists (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users.users(id),
    game_id UUID NOT NULL REFERENCES games.games(id),
    target_price DECIMAL(10,2),
    currency price_currency DEFAULT 'INR',
    platform_preference platform_type,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, game_id)
);

CREATE TABLE users.user_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users.users(id),
    type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    game_id UUID REFERENCES games.games(id),
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users.user_votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users.users(id),
    game_id UUID NOT NULL REFERENCES games.games(id),
    vote_month DATE NOT NULL, -- First day of the month
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, game_id, vote_month)
);

-- Forum tables
CREATE TABLE users.forum_posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    game_id UUID NOT NULL REFERENCES games.games(id),
    user_id UUID NOT NULL REFERENCES users.users(id),
    parent_post_id UUID REFERENCES users.forum_posts(id),
    title VARCHAR(255),
    content TEXT NOT NULL,
    likes_count INTEGER DEFAULT 0,
    is_deleted BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users.post_likes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id UUID NOT NULL REFERENCES users.forum_posts(id),
    user_id UUID NOT NULL REFERENCES users.users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(post_id, user_id)
);

-- Analytics schema tables
CREATE TABLE analytics.daily_stats (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date DATE NOT NULL UNIQUE,
    total_users INTEGER DEFAULT 0,
    active_users INTEGER DEFAULT 0,
    total_games INTEGER DEFAULT 0,
    total_price_updates INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_games_title ON games.games USING gin(title gin_trgm_ops);
CREATE INDEX idx_games_developer ON games.games(developer);
CREATE INDEX idx_games_publisher ON games.games(publisher);
CREATE INDEX idx_games_genres ON games.games USING gin(genres);
CREATE INDEX idx_games_steam_app_id ON games.games(steam_app_id);
CREATE INDEX idx_games_epic_catalog_item_id ON games.games(epic_catalog_item_id);
CREATE INDEX idx_games_gog_product_id ON games.games(gog_product_id);

CREATE INDEX idx_game_prices_game_platform ON games.game_prices(game_id, platform_id);
CREATE INDEX idx_game_prices_updated ON games.game_prices(last_updated);
CREATE INDEX idx_game_prices_sale ON games.game_prices(is_on_sale) WHERE is_on_sale = true;

CREATE INDEX idx_price_history_game_platform ON games.price_history(game_id, platform_id);
CREATE INDEX idx_price_history_recorded ON games.price_history(recorded_at);

CREATE INDEX idx_users_email ON users.users(email);
CREATE INDEX idx_users_username ON users.users(username);
CREATE INDEX idx_users_steam_id ON users.users(steam_id);

CREATE INDEX idx_wishlists_user ON users.wishlists(user_id);
CREATE INDEX idx_wishlists_game ON users.wishlists(game_id);
CREATE INDEX idx_wishlists_active ON users.wishlists(is_active) WHERE is_active = true;

CREATE INDEX idx_notifications_user ON users.user_notifications(user_id);
CREATE INDEX idx_notifications_unread ON users.user_notifications(user_id, is_read) WHERE is_read = false;

CREATE INDEX idx_votes_month ON users.user_votes(vote_month);
CREATE INDEX idx_votes_game_month ON users.user_votes(game_id, vote_month);

CREATE INDEX idx_forum_posts_game ON users.forum_posts(game_id);
CREATE INDEX idx_forum_posts_user ON users.forum_posts(user_id);
CREATE INDEX idx_forum_posts_parent ON users.forum_posts(parent_post_id);

-- Insert initial data
INSERT INTO games.platforms (name, type, api_endpoint) VALUES 
('Steam', 'STEAM', 'https://api.steampowered.com'),
('Epic Games Store', 'EPIC', 'https://api.epicgames.dev'),
('GOG', 'GOG', 'https://api.gog.com'),
('Other', 'OTHER', NULL);

-- Create a trigger to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply the trigger to relevant tables
CREATE TRIGGER update_games_updated_at BEFORE UPDATE ON games.games FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users.users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_forum_posts_updated_at BEFORE UPDATE ON users.forum_posts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
