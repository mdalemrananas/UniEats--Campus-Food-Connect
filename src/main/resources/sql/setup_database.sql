-- UniEats Database Setup Script
-- This script creates the necessary tables for seller functionality

-- Create food_items table
CREATE TABLE IF NOT EXISTS food_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    price REAL NOT NULL,
    seller_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_name TEXT NOT NULL,
    food_name TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    total_price REAL NOT NULL,
    status TEXT DEFAULT 'Pending',
    seller_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data for testing (optional)
-- Sample food items for seller_id = 1
INSERT OR IGNORE INTO food_items (name, price, seller_id) VALUES 
('Chicken Burger', 250.00, 1),
('Pizza Margherita', 400.00, 1),
('French Fries', 80.00, 1),
('Coca Cola', 50.00, 1),
('Chicken Wings', 300.00, 1);

-- Sample orders for seller_id = 1
INSERT OR IGNORE INTO orders (customer_name, food_name, quantity, total_price, status, seller_id) VALUES 
('John Doe', 'Chicken Burger', 2, 500.00, 'Pending', 1),
('Jane Smith', 'Pizza Margherita', 1, 400.00, 'In Progress', 1),
('Bob Wilson', 'French Fries', 3, 240.00, 'Completed', 1),
('Alice Johnson', 'Chicken Wings', 1, 300.00, 'Pending', 1),
('Charlie Brown', 'Coca Cola', 4, 200.00, 'Completed', 1);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_food_items_seller_id ON food_items(seller_id);
CREATE INDEX IF NOT EXISTS idx_orders_seller_id ON orders(seller_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);

-- Display table information
SELECT 'Database setup completed successfully!' as message;
SELECT 'food_items table created with ' || COUNT(*) || ' sample records' as food_items_info FROM food_items;
SELECT 'orders table created with ' || COUNT(*) || ' sample records' as orders_info FROM orders;
