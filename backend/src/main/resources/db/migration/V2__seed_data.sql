-- CyberOpus – V2 Seed Data
-- Demo credentials (passwords are BCrypt hashes of the plaintext shown):
--   alice@demo.com    → Password123!
--   bob@demo.com      → Password123!
--   admin@cyberopus.com → Admin123!
--
-- BCrypt strength 12 hashes generated offline

-- ---------------------------------------------------------------
-- USERS
-- ---------------------------------------------------------------
INSERT INTO users (first_name, last_name, email, password_hash, phone, role) VALUES
('Alice',  'Demo',  'alice@demo.com',         '$2a$12$R9PPdNv/vROtyQoYG3bmTeiMArimyWN02/sxv9IbnDCKNgELFbp1e', '555-0101', 'CUSTOMER'),
('Bob',    'Demo',  'bob@demo.com',            '$2a$12$R9PPdNv/vROtyQoYG3bmTeiMArimyWN02/sxv9IbnDCKNgELFbp1e', '555-0102', 'CUSTOMER'),
('Admin',  'User',  'admin@cyberopus.com',     '$2a$12$R/sxyhb//I5JattCz4mNzO5ypmVfG/eUZZs1F6BkvVaDWK2Vp.rjW', NULL,       'ADMIN');

-- ---------------------------------------------------------------
-- CARTS (one per user)
-- ---------------------------------------------------------------
INSERT INTO carts (user_id) VALUES
((SELECT id FROM users WHERE email = 'alice@demo.com')),
((SELECT id FROM users WHERE email = 'bob@demo.com')),
((SELECT id FROM users WHERE email = 'admin@cyberopus.com'));

-- ---------------------------------------------------------------
-- REWARD POINTS
-- alice starts with 500 pts (= $5.00) to demo redemption
-- ---------------------------------------------------------------
INSERT INTO reward_points (user_id, balance) VALUES
((SELECT id FROM users WHERE email = 'alice@demo.com'),    500),
((SELECT id FROM users WHERE email = 'bob@demo.com'),        0),
((SELECT id FROM users WHERE email = 'admin@cyberopus.com'), 0);

-- ---------------------------------------------------------------
-- CATEGORIES
-- ---------------------------------------------------------------
INSERT INTO categories (name, description) VALUES
('Technology',   'Programming, software engineering, and computer science'),
('Fiction',      'Novels, short stories, and literary fiction'),
('Science',      'Physics, biology, astronomy, and natural sciences'),
('Business',     'Management, entrepreneurship, and economics'),
('Self-Help',    'Personal development and productivity'),
('Biography',    'Life stories of remarkable people');

-- ---------------------------------------------------------------
-- BRANDS / PUBLISHERS
-- ---------------------------------------------------------------
INSERT INTO brands (name, description) VALUES
('O''Reilly Media',         'Technical books and learning resources'),
('Packt Publishing',        'Practical guides for developers'),
('Penguin Books',           'Literary fiction and popular non-fiction'),
('Harvard Business Press',  'Business and leadership titles'),
('No Starch Press',         'Geek-friendly technology books');

-- ---------------------------------------------------------------
-- BOOKS (20 titles)
-- ---------------------------------------------------------------
INSERT INTO books (title, author, description, isbn, price, stock_quantity, category_id, brand_id, available) VALUES
-- Technology
('Spring Boot in Action',          'Craig Walls',         'A comprehensive guide to building Spring Boot applications.',        '9781617292545', 39.99, 50, (SELECT id FROM categories WHERE name = 'Technology'), (SELECT id FROM brands WHERE name = 'No Starch Press'), true),
('Clean Code',                     'Robert C. Martin',    'A handbook of agile software craftsmanship.',                        '9780132350884', 34.99, 40, (SELECT id FROM categories WHERE name = 'Technology'), (SELECT id FROM brands WHERE name = 'O''Reilly Media'), true),
('Designing Data-Intensive Apps',  'Martin Kleppmann',    'Principles behind reliable, scalable, and maintainable systems.',   '9781449373320', 49.99, 35, (SELECT id FROM categories WHERE name = 'Technology'), (SELECT id FROM brands WHERE name = 'O''Reilly Media'), true),
('The Pragmatic Programmer',       'David Thomas',        'Your journey to mastery.',                                          '9780135957059', 44.99, 30, (SELECT id FROM categories WHERE name = 'Technology'), (SELECT id FROM brands WHERE name = 'No Starch Press'), true),
('Java: The Complete Reference',   'Herbert Schildt',     'Comprehensive Java 17 reference guide.',                            '9781260463419', 54.99, 20, (SELECT id FROM categories WHERE name = 'Technology'), (SELECT id FROM brands WHERE name = 'Packt Publishing'), true),
-- Fiction
('The Great Gatsby',               'F. Scott Fitzgerald', 'A story of the fabulously wealthy Jay Gatsby.',                    '9780743273565',  9.99, 80, (SELECT id FROM categories WHERE name = 'Fiction'), (SELECT id FROM brands WHERE name = 'Penguin Books'), true),
('To Kill a Mockingbird',          'Harper Lee',          'A tale of racial injustice in the American Deep South.',           '9780061935466', 12.99, 60, (SELECT id FROM categories WHERE name = 'Fiction'), (SELECT id FROM brands WHERE name = 'Penguin Books'), true),
('1984',                           'George Orwell',       'A dystopian social science fiction novel.',                        '9780451524935',  8.99, 70, (SELECT id FROM categories WHERE name = 'Fiction'), (SELECT id FROM brands WHERE name = 'Penguin Books'), true),
('The Hitchhiker''s Guide',        'Douglas Adams',       'A comic science fiction series.',                                  '9781529046137', 11.99, 55, (SELECT id FROM categories WHERE name = 'Fiction'), (SELECT id FROM brands WHERE name = 'Penguin Books'), true),
-- Science
('A Brief History of Time',        'Stephen Hawking',     'The history of the universe for the general reader.',              '9780553380163', 14.99, 45, (SELECT id FROM categories WHERE name = 'Science'), (SELECT id FROM brands WHERE name = 'Penguin Books'), true),
('The Gene',                       'Siddhartha Mukherjee','An intimate history of genetics.',                                 '9781476733524', 18.99, 30, (SELECT id FROM categories WHERE name = 'Science'), (SELECT id FROM brands WHERE name = 'Penguin Books'), true),
('Cosmos',                         'Carl Sagan',          'A personal voyage through the universe.',                          '9780345539434', 16.99, 25, (SELECT id FROM categories WHERE name = 'Science'), (SELECT id FROM brands WHERE name = 'Penguin Books'), true),
-- Business
('Zero to One',                    'Peter Thiel',         'Notes on startups, or how to build the future.',                   '9780804139021', 19.99, 50, (SELECT id FROM categories WHERE name = 'Business'), (SELECT id FROM brands WHERE name = 'Harvard Business Press'), true),
('Good to Great',                  'Jim Collins',         'Why some companies make the leap and others don''t.',              '9780066620992', 22.99, 40, (SELECT id FROM categories WHERE name = 'Business'), (SELECT id FROM brands WHERE name = 'Harvard Business Press'), true),
('The Lean Startup',               'Eric Ries',           'How today''s entrepreneurs use continuous innovation.',            '9780307887894', 17.99, 35, (SELECT id FROM categories WHERE name = 'Business'), (SELECT id FROM brands WHERE name = 'Harvard Business Press'), true),
-- Self-Help
('Atomic Habits',                  'James Clear',         'An easy and proven way to build good habits.',                     '9780735211292', 21.99, 60, (SELECT id FROM categories WHERE name = 'Self-Help'), (SELECT id FROM brands WHERE name = 'Penguin Books'), true),
('Deep Work',                      'Cal Newport',         'Rules for focused success in a distracted world.',                 '9781455586691', 18.99, 45, (SELECT id FROM categories WHERE name = 'Self-Help'), (SELECT id FROM brands WHERE name = 'O''Reilly Media'), true),
('Thinking, Fast and Slow',        'Daniel Kahneman',     'The two systems that drive the way we think.',                     '9780374533557', 16.99, 40, (SELECT id FROM categories WHERE name = 'Self-Help'), (SELECT id FROM brands WHERE name = 'Penguin Books'), true),
-- Biography
('Steve Jobs',                     'Walter Isaacson',     'The exclusive biography of Steve Jobs.',                           '9781451648539', 24.99, 30, (SELECT id FROM categories WHERE name = 'Biography'), (SELECT id FROM brands WHERE name = 'Penguin Books'), true),
('Elon Musk',                      'Walter Isaacson',     'A deeply reported account of the world''s most compelling entrepreneur.','9781982181284', 28.99, 25, (SELECT id FROM categories WHERE name = 'Biography'), (SELECT id FROM brands WHERE name = 'Penguin Books'), true);

-- ---------------------------------------------------------------
-- ADDRESSES for alice
-- ---------------------------------------------------------------
INSERT INTO addresses (user_id, full_name, line1, city, state, postal_code, country, phone, is_default)
VALUES
((SELECT id FROM users WHERE email = 'alice@demo.com'), 'Alice Demo', '123 Main Street', 'San Francisco', 'CA', '94105', 'US', '555-0101', true),
((SELECT id FROM users WHERE email = 'alice@demo.com'), 'Alice Demo (Work)', '456 Tech Park Blvd', 'San Jose', 'CA', '95110', 'US', '555-0199', false);

-- ---------------------------------------------------------------
-- PRE-SEEDED ORDERS for alice (to demo history, recommendations, cancellation)
-- ---------------------------------------------------------------

-- Order 1: DELIVERED 10 days ago (not cancellable – provides recommendation data)
INSERT INTO orders (order_number, user_id, address_snapshot, subtotal, reward_discount, delivery_charge, total_amount, status, placed_at)
VALUES (
    'ORD-SEED0001',
    (SELECT id FROM users WHERE email = 'alice@demo.com'),
    '{"fullName":"Alice Demo","line1":"123 Main Street","city":"San Francisco","state":"CA","postalCode":"94105","country":"US"}',
    73.98, 0.00, 4.99, 78.97,
    'DELIVERED',
    NOW() - INTERVAL '10 days'
);

INSERT INTO order_items (order_id, book_id, book_title_snapshot, book_author_snapshot, price_snapshot, quantity)
VALUES
((SELECT id FROM orders WHERE order_number = 'ORD-SEED0001'),
 (SELECT id FROM books WHERE isbn = '9781617292545'), 'Spring Boot in Action', 'Craig Walls', 39.99, 1),
((SELECT id FROM orders WHERE order_number = 'ORD-SEED0001'),
 (SELECT id FROM books WHERE isbn = '9780132350884'), 'Clean Code', 'Robert C. Martin', 34.99, 1);

INSERT INTO payments (order_id, payment_method, masked_card_number, card_holder_name, transaction_ref, amount, status, initiated_at, completed_at)
VALUES (
    (SELECT id FROM orders WHERE order_number = 'ORD-SEED0001'),
    'CREDIT_CARD', '****1234', 'Alice Demo', 'TXN-SEED0001', 78.97, 'SUCCESS', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'
);

INSERT INTO reward_point_transactions (user_id, order_id, type, points, description)
VALUES (
    (SELECT id FROM users WHERE email = 'alice@demo.com'),
    (SELECT id FROM orders WHERE order_number = 'ORD-SEED0001'),
    'EARN', 789, 'Earned for order ORD-SEED0001'
);

-- Order 2: CONFIRMED 1 hour ago (CANCELLABLE – for cancellation demo)
INSERT INTO orders (order_number, user_id, address_snapshot, subtotal, reward_discount, delivery_charge, total_amount, status, placed_at)
VALUES (
    'ORD-SEED0002',
    (SELECT id FROM users WHERE email = 'alice@demo.com'),
    '{"fullName":"Alice Demo","line1":"123 Main Street","city":"San Francisco","state":"CA","postalCode":"94105","country":"US"}',
    21.99, 5.00, 4.99, 21.98,
    'CONFIRMED',
    NOW() - INTERVAL '1 hour'
);

INSERT INTO order_items (order_id, book_id, book_title_snapshot, book_author_snapshot, price_snapshot, quantity)
VALUES (
    (SELECT id FROM orders WHERE order_number = 'ORD-SEED0002'),
    (SELECT id FROM books WHERE isbn = '9780735211292'), 'Atomic Habits', 'James Clear', 21.99, 1
);

INSERT INTO payments (order_id, payment_method, masked_card_number, card_holder_name, transaction_ref, amount, status, initiated_at, completed_at)
VALUES (
    (SELECT id FROM orders WHERE order_number = 'ORD-SEED0002'),
    'DEBIT_CARD', '****5678', 'Alice Demo', 'TXN-SEED0002', 21.98, 'SUCCESS', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'
);

INSERT INTO reward_point_transactions (user_id, order_id, type, points, description)
VALUES (
    (SELECT id FROM users WHERE email = 'alice@demo.com'),
    (SELECT id FROM orders WHERE order_number = 'ORD-SEED0002'),
    'EARN', 219, 'Earned for order ORD-SEED0002'
);

-- Deduct the 500 points redeemed for order 2
INSERT INTO reward_point_transactions (user_id, order_id, type, points, description)
VALUES (
    (SELECT id FROM users WHERE email = 'alice@demo.com'),
    (SELECT id FROM orders WHERE order_number = 'ORD-SEED0002'),
    'REDEEM', -500, 'Redeemed at checkout for order ORD-SEED0002'
);

-- Order 3: CANCELLED (shows cancelled state in history)
INSERT INTO orders (order_number, user_id, address_snapshot, subtotal, reward_discount, delivery_charge, total_amount, status, placed_at)
VALUES (
    'ORD-SEED0003',
    (SELECT id FROM users WHERE email = 'alice@demo.com'),
    '{"fullName":"Alice Demo","line1":"123 Main Street","city":"San Francisco","state":"CA","postalCode":"94105","country":"US"}',
    14.99, 0.00, 4.99, 19.98,
    'CANCELLED',
    NOW() - INTERVAL '3 days'
);

INSERT INTO order_items (order_id, book_id, book_title_snapshot, book_author_snapshot, price_snapshot, quantity)
VALUES (
    (SELECT id FROM orders WHERE order_number = 'ORD-SEED0003'),
    (SELECT id FROM books WHERE isbn = '9780553380163'), 'A Brief History of Time', 'Stephen Hawking', 14.99, 1
);

INSERT INTO payments (order_id, payment_method, masked_card_number, card_holder_name, transaction_ref, amount, status, initiated_at, completed_at)
VALUES (
    (SELECT id FROM orders WHERE order_number = 'ORD-SEED0003'),
    'CREDIT_CARD', '****9012', 'Alice Demo', 'TXN-SEED0003', 19.98, 'REFUNDED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days 23 hours'
);
