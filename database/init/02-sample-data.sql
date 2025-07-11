-- Sample data for testing and development
-- This file contains test data to help with development

-- Insert some sample games (you can replace with real data later)
INSERT INTO games.games (id, title, description, short_description, developer, publisher, release_date, genres, tags, steam_app_id, header_image_url) VALUES 
(
    uuid_generate_v4(),
    'Cyberpunk 2077',
    'Cyberpunk 2077 is an open-world, action-adventure story set in Night City, a megalopolis obsessed with power, glamour and body modification.',
    'An open-world, action-adventure story set in Night City.',
    'CD PROJEKT RED',
    'CD PROJEKT RED',
    '2020-12-10',
    ARRAY['Action', 'RPG', 'Open World'],
    ARRAY['Cyberpunk', 'Open World', 'RPG', 'Sci-fi', 'Action'],
    1091500,
    'https://cdn.akamai.steamstatic.com/steam/apps/1091500/header.jpg'
),
(
    uuid_generate_v4(),
    'Baldur''s Gate 3',
    'Baldur''s Gate 3 is a story-rich, party-based RPG set in the universe of Dungeons & Dragons.',
    'A story-rich, party-based RPG set in the universe of Dungeons & Dragons.',
    'Larian Studios',
    'Larian Studios',
    '2023-08-03',
    ARRAY['RPG', 'Strategy', 'Turn-Based'],
    ARRAY['RPG', 'D&D', 'Turn-Based Combat', 'Story Rich', 'Fantasy'],
    1086940,
    'https://cdn.akamai.steamstatic.com/steam/apps/1086940/header.jpg'
),
(
    uuid_generate_v4(),
    'Hades',
    'Hades is a god-like rogue-like dungeon crawler that combines the best aspects of Supergiant''s critically acclaimed titles.',
    'A god-like rogue-like dungeon crawler.',
    'Supergiant Games',
    'Supergiant Games',
    '2020-09-17',
    ARRAY['Action', 'Indie', 'Roguelike'],
    ARRAY['Roguelike', 'Action', 'Indie', 'Greek Mythology', 'Hack and Slash'],
    1145360,
    'https://cdn.akamai.steamstatic.com/steam/apps/1145360/header.jpg'
);

-- Insert sample prices for these games
INSERT INTO games.game_prices (game_id, platform_id, current_price, original_price, discount_percentage, currency, is_on_sale) 
SELECT 
    g.id,
    1, -- Steam platform
    CASE 
        WHEN g.title = 'Cyberpunk 2077' THEN 2999.00
        WHEN g.title = 'Baldur''s Gate 3' THEN 3999.00
        WHEN g.title = 'Hades' THEN 699.00
    END,
    CASE 
        WHEN g.title = 'Cyberpunk 2077' THEN 2999.00
        WHEN g.title = 'Baldur''s Gate 3' THEN 3999.00
        WHEN g.title = 'Hades' THEN 999.00
    END,
    CASE 
        WHEN g.title = 'Hades' THEN 30
        ELSE 0
    END,
    'INR',
    CASE 
        WHEN g.title = 'Hades' THEN true
        ELSE false
    END
FROM games.games g;

-- Insert sample price history
INSERT INTO games.price_history (game_id, platform_id, price, original_price, discount_percentage, currency, recorded_at)
SELECT 
    gp.game_id,
    gp.platform_id,
    gp.current_price,
    gp.original_price,
    gp.discount_percentage,
    gp.currency,
    CURRENT_TIMESTAMP - INTERVAL '1 day'
FROM games.game_prices gp;

-- Insert sample admin user (password: admin123 - you should hash this properly)
INSERT INTO users.users (id, username, email, password_hash, first_name, last_name, role, email_verified, is_active) VALUES 
(
    uuid_generate_v4(),
    'admin',
    'admin@indiadb.games',
    '$2a$10$rGKl/vJHnGWZPfUEbQ1QGO5VZqNzZqgGNqCjzOmvZjCKfhg8RJxTm', -- This is bcrypt hash of 'admin123'
    'Admin',
    'User',
    'ADMIN',
    true,
    true
);

-- Insert sample regular user
INSERT INTO users.users (id, username, email, password_hash, first_name, last_name, role, email_verified, is_active) VALUES 
(
    uuid_generate_v4(),
    'testuser',
    'test@example.com',
    '$2a$10$rGKl/vJHnGWZPfUEbQ1QGO5VZqNzZqgGNqCjzOmvZjCKfhg8RJxTm', -- This is bcrypt hash of 'admin123'
    'Test',
    'User',
    'USER',
    true,
    true
);

-- Insert sample wishlist items
INSERT INTO users.wishlists (user_id, game_id, target_price, currency, platform_preference)
SELECT 
    u.id,
    g.id,
    2000.00,
    'INR',
    'STEAM'
FROM users.users u, games.games g 
WHERE u.username = 'testuser' AND g.title = 'Cyberpunk 2077';

-- Insert sample forum posts
INSERT INTO users.forum_posts (game_id, user_id, title, content)
SELECT 
    g.id,
    u.id,
    'Great game!',
    'This is an amazing game with great graphics and storyline. Highly recommended!'
FROM games.games g, users.users u 
WHERE g.title = 'Cyberpunk 2077' AND u.username = 'testuser';

-- Insert sample daily stats
INSERT INTO analytics.daily_stats (date, total_users, active_users, total_games, total_price_updates) VALUES 
(CURRENT_DATE - INTERVAL '1 day', 2, 1, 3, 3),
(CURRENT_DATE, 2, 2, 3, 0);
