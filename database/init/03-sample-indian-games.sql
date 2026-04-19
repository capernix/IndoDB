-- IndiaDB Games - Realistic Indian Gaming Sample Data
-- Popular games with actual Indian pricing (INR)

-- Insert Gaming Platforms (with conflict handling)
INSERT INTO games.platforms (name, type, api_endpoint, is_active) VALUES
('Steam', 'STEAM', 'https://api.steampowered.com', true),
('Epic Games Store', 'EPIC', 'https://api.epicgames.dev', true),
('GOG', 'GOG', 'https://api.gog.com', true)
ON CONFLICT (name) DO UPDATE SET
    api_endpoint = EXCLUDED.api_endpoint,
    is_active = EXCLUDED.is_active;

-- Insert Popular Games in Indian Market
INSERT INTO games.games (id, title, description, short_description, developer, publisher, release_date, genres, tags, metacritic_score, steam_app_id, epic_catalog_item_id, gog_product_id, itad_id, header_image_url, is_active) VALUES

-- AAA Games Popular in India
('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'Grand Theft Auto V', 'The biggest, most dynamic and most diverse open world ever created and now packed with layers of new detail.', 'Open world action-adventure in Los Santos', 'Rockstar North', 'Rockstar Games', '2013-09-17', ARRAY['Action', 'Adventure', 'Open World'], ARRAY['Crime', 'Multiplayer', 'Driving'], 97, 271590, 'gta-v-epic', 1207658691, NULL, 'https://cdn.akamai.steamstatic.com/steam/apps/271590/header.jpg', true),

('6ba7b810-9dad-11d1-80b4-00c04fd430c8', 'Counter-Strike 2', 'The premier competitive FPS experience, now with Source 2 engine upgrades.', 'Tactical FPS with competitive gameplay', 'Valve', 'Valve', '2023-09-27', ARRAY['Action', 'FPS'], ARRAY['Competitive', 'Tactical', 'Multiplayer'], 87, 730, 'cs2-epic', NULL, NULL, 'https://cdn.akamai.steamstatic.com/steam/apps/730/header.jpg', true),

('6ba7b811-9dad-11d1-80b4-00c04fd430c8', 'Valorant', 'A 5v5 character-based tactical FPS where precise gunplay meets unique agent abilities.', 'Character-based tactical shooter', 'Riot Games', 'Riot Games', '2020-06-02', ARRAY['Action', 'FPS'], ARRAY['Tactical', 'Competitive', 'Free to Play'], 80, NULL, 'valorant-epic', NULL, NULL, 'https://cdn.akamai.steamstatic.com/steam/apps/1172470/header.jpg', true),

-- Popular Indie Games in India
('6ba7b812-9dad-11d1-80b4-00c04fd430c8', 'Fall Guys', 'A massively multiplayer party royale game with up to 60 players online in a series of escalating elimination rounds.', 'Battle royale party game', 'Mediatonic', 'Epic Games', '2020-08-04', ARRAY['Action', 'Party'], ARRAY['Battle Royale', 'Multiplayer', 'Casual'], 79, 1097150, 'fall-guys-epic', NULL, NULL, 'https://cdn.akamai.steamstatic.com/steam/apps/1097150/header.jpg', true),

-- Strategy Games (Popular in India)
('6ba7b813-9dad-11d1-80b4-00c04fd430c8', 'Age of Empires IV', 'One of the most beloved real-time strategy games returns to glory with Age of Empires IV.', 'Real-time strategy with historical campaigns', 'Relic Entertainment', 'Xbox Game Studios', '2021-10-28', ARRAY['Strategy', 'RTS'], ARRAY['Historical', 'Multiplayer', 'Campaign'], 81, 1466860, 'aoe4-epic', NULL, NULL, 'https://cdn.akamai.steamstatic.com/steam/apps/1466860/header.jpg', true),

-- Racing Games
('6ba7b814-9dad-11d1-80b4-00c04fd430c8', 'Forza Horizon 5', 'Your Ultimate Horizon Adventure awaits! Explore the vibrant and ever-evolving open world landscapes of Mexico.', 'Open world racing in Mexico', 'Playground Games', 'Xbox Game Studios', '2021-11-09', ARRAY['Racing', 'Open World'], ARRAY['Cars', 'Exploration', 'Multiplayer'], 92, 1551360, 'forza-horizon-5', NULL, NULL, 'https://cdn.akamai.steamstatic.com/steam/apps/1551360/header.jpg', true),

-- Free-to-Play Popular in India
('6ba7b815-9dad-11d1-80b4-00c04fd430c8', 'Apex Legends', 'Choose from a diverse cast of Legends, each with their own unique personality, strengths and abilities.', 'Battle royale with unique legends', 'Respawn Entertainment', 'Electronic Arts', '2019-02-04', ARRAY['Action', 'Battle Royale'], ARRAY['Free to Play', 'Competitive', 'Team-based'], 89, 1172470, 'apex-legends', NULL, NULL, 'https://cdn.akamai.steamstatic.com/steam/apps/1172470/header.jpg', true),

-- Budget Indie Favorites
('6ba7b816-9dad-11d1-80b4-00c04fd430c8', 'Among Us', 'An online and local party game of teamwork and betrayal for 4-15 players.', 'Social deduction party game', 'InnerSloth', 'InnerSloth', '2018-06-15', ARRAY['Action', 'Party'], ARRAY['Social Deduction', 'Multiplayer', 'Indie'], 85, 945360, 'among-us', 1456460669, NULL, 'https://cdn.akamai.steamstatic.com/steam/apps/945360/header.jpg', true),

-- RPG Popular in India
('6ba7b817-9dad-11d1-80b4-00c04fd430c8', 'The Witcher 3: Wild Hunt', 'As war rages on throughout the Northern Realms, you take on the greatest contract of your life — tracking down the Child of Destiny.', 'Open world fantasy RPG', 'CD PROJEKT RED', 'CD PROJEKT RED', '2015-05-19', ARRAY['RPG', 'Open World'], ARRAY['Fantasy', 'Story Rich', 'Mature'], 95, 292030, 'witcher-3', 1207658924, NULL, 'https://cdn.akamai.steamstatic.com/steam/apps/292030/header.jpg', true),

-- Current Epic Free Game
('6ba7b818-9dad-11d1-80b4-00c04fd430c8', 'Rocket League', 'A high-powered hybrid of arcade-style soccer and vehicular mayhem with easy-to-understand controls.', 'Soccer meets driving in this physics-based multiplayer game', 'Psyonix', 'Psyonix', '2015-07-07', ARRAY['Sports', 'Racing'], ARRAY['Soccer', 'Cars', 'Competitive'], 86, 252950, 'rocket-league', NULL, NULL, 'https://cdn.akamai.steamstatic.com/steam/apps/252950/header.jpg', true);

-- Insert Realistic Indian Prices (INR)
INSERT INTO games.game_prices (id, game_id, platform_id, current_price, original_price, discount_percentage, currency, is_free, is_on_sale, sale_end_date, last_updated) VALUES

-- GTA V - Different prices across platforms
('550e8400-e29b-41d4-a716-446655440001', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 1, 1499.00, 2999.00, 50, 'INR', false, true, '2025-07-20 23:59:59', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 2, 0.00, 2999.00, 100, 'INR', true, false, NULL, CURRENT_TIMESTAMP), -- Free on Epic

-- Counter-Strike 2 - Free on Steam
('550e8400-e29b-41d4-a716-446655440003', '6ba7b810-9dad-11d1-80b4-00c04fd430c8', 1, 0.00, 0.00, 0, 'INR', true, false, NULL, CURRENT_TIMESTAMP),

-- Valorant - Free to Play
('550e8400-e29b-41d4-a716-446655440004', '6ba7b811-9dad-11d1-80b4-00c04fd430c8', 2, 0.00, 0.00, 0, 'INR', true, false, NULL, CURRENT_TIMESTAMP),

-- Fall Guys - Free to Play
('550e8400-e29b-41d4-a716-446655440005', '6ba7b812-9dad-11d1-80b4-00c04fd430c8', 1, 0.00, 0.00, 0, 'INR', true, false, NULL, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440006', '6ba7b812-9dad-11d1-80b4-00c04fd430c8', 2, 0.00, 0.00, 0, 'INR', true, false, NULL, CURRENT_TIMESTAMP),

-- Age of Empires IV - Premium pricing
('550e8400-e29b-41d4-a716-446655440007', '6ba7b813-9dad-11d1-80b4-00c04fd430c8', 1, 2399.00, 3999.00, 40, 'INR', false, true, '2025-07-25 23:59:59', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440008', '6ba7b813-9dad-11d1-80b4-00c04fd430c8', 2, 2999.00, 3999.00, 25, 'INR', false, true, '2025-07-15 23:59:59', CURRENT_TIMESTAMP),

-- Forza Horizon 5 - Premium racing
('550e8400-e29b-41d4-a716-446655440009', '6ba7b814-9dad-11d1-80b4-00c04fd430c8', 1, 3499.00, 4999.00, 30, 'INR', false, true, '2025-07-18 23:59:59', CURRENT_TIMESTAMP),

-- Apex Legends - Free to Play
('550e8400-e29b-41d4-a716-446655440010', '6ba7b815-9dad-11d1-80b4-00c04fd430c8', 1, 0.00, 0.00, 0, 'INR', true, false, NULL, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440011', '6ba7b815-9dad-11d1-80b4-00c04fd430c8', 2, 0.00, 0.00, 0, 'INR', true, false, NULL, CURRENT_TIMESTAMP),

-- Among Us - Budget friendly
('550e8400-e29b-41d4-a716-446655440012', '6ba7b816-9dad-11d1-80b4-00c04fd430c8', 1, 169.00, 339.00, 50, 'INR', false, true, '2025-07-22 23:59:59', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440013', '6ba7b816-9dad-11d1-80b4-00c04fd430c8', 3, 249.00, 339.00, 26, 'INR', false, true, '2025-07-16 23:59:59', CURRENT_TIMESTAMP),

-- The Witcher 3 - Great value RPG
('550e8400-e29b-41d4-a716-446655440014', '6ba7b817-9dad-11d1-80b4-00c04fd430c8', 1, 699.00, 1399.00, 50, 'INR', false, true, '2025-07-30 23:59:59', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440015', '6ba7b817-9dad-11d1-80b4-00c04fd430c8', 3, 899.00, 1399.00, 36, 'INR', false, true, '2025-07-19 23:59:59', CURRENT_TIMESTAMP),

-- Rocket League - Free to Play
('550e8400-e29b-41d4-a716-446655440016', '6ba7b818-9dad-11d1-80b4-00c04fd430c8', 1, 0.00, 0.00, 0, 'INR', true, false, NULL, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440017', '6ba7b818-9dad-11d1-80b4-00c04fd430c8', 2, 0.00, 0.00, 0, 'INR', true, false, NULL, CURRENT_TIMESTAMP);

-- Add some price history for trending analysis
INSERT INTO games.price_history (id, game_id, platform_id, price, original_price, discount_percentage, currency, recorded_at) VALUES
-- GTA V price drop history
('660e8400-e29b-41d4-a716-446655440001', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 1, 2999.00, 2999.00, 0, 'INR', CURRENT_TIMESTAMP - INTERVAL '30 days'),
('660e8400-e29b-41d4-a716-446655440002', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 1, 2399.00, 2999.00, 20, 'INR', CURRENT_TIMESTAMP - INTERVAL '15 days'),
('660e8400-e29b-41d4-a716-446655440003', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 1, 1499.00, 2999.00, 50, 'INR', CURRENT_TIMESTAMP - INTERVAL '5 days'),

-- Age of Empires IV price progression
('660e8400-e29b-41d4-a716-446655440004', '6ba7b813-9dad-11d1-80b4-00c04fd430c8', 1, 3999.00, 3999.00, 0, 'INR', CURRENT_TIMESTAMP - INTERVAL '60 days'),
('660e8400-e29b-41d4-a716-446655440005', '6ba7b813-9dad-11d1-80b4-00c04fd430c8', 1, 2399.00, 3999.00, 40, 'INR', CURRENT_TIMESTAMP - INTERVAL '10 days');

-- Add some sample wishlist data and votes for trending algorithm
INSERT INTO users.users (id, username, email, password_hash, first_name, last_name, role, is_active) VALUES
('770e8400-e29b-41d4-a716-446655440001', 'gamer_raj', 'raj@example.com', '$2a$10$dummy_hash', 'Raj', 'Patel', 'USER', true),
('770e8400-e29b-41d4-a716-446655440002', 'mumbai_gamer', 'mumbai@example.com', '$2a$10$dummy_hash', 'Priya', 'Shah', 'USER', true),
('770e8400-e29b-41d4-a716-446655440003', 'delhi_player', 'delhi@example.com', '$2a$10$dummy_hash', 'Arjun', 'Kumar', 'USER', true);

-- Sample wishlists (makes games trending)
INSERT INTO users.wishlists (id, user_id, game_id, target_price, currency, is_active) VALUES
('880e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 999.00, 'INR', true),
('880e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440002', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 1200.00, 'INR', true),
('880e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440003', '6ba7b813-9dad-11d1-80b4-00c04fd430c8', 1999.00, 'INR', true),
('880e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440001', '6ba7b817-9dad-11d1-80b4-00c04fd430c8', 499.00, 'INR', true);

-- Sample monthly votes (makes games "hottest")
INSERT INTO users.user_votes (id, user_id, game_id, vote_month) VALUES
('990e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', '6ba7b810-9dad-11d1-80b4-00c04fd430c8', '2025-07-01'),
('990e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440002', '6ba7b810-9dad-11d1-80b4-00c04fd430c8', '2025-07-01'),
('990e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440003', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', '2025-07-01'),
('990e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440001', '6ba7b817-9dad-11d1-80b4-00c04fd430c8', '2025-07-01');

-- Success message
SELECT 'IndiaDB Games sample data inserted successfully! 🎮' as status;
SELECT 'Total Games: ' || COUNT(*) FROM games.games WHERE is_active = true;
SELECT 'Total Prices: ' || COUNT(*) FROM games.game_prices;
SELECT 'Free Games Available: ' || COUNT(*) FROM games.game_prices WHERE is_free = true;
SELECT 'Games on Sale: ' || COUNT(*) FROM games.game_prices WHERE is_on_sale = true;
