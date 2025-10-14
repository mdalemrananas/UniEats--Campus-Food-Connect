package com.unieats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:unieats.db";
    private static DatabaseManager instance;
    private static User currentUser;
    
    private DatabaseManager() {
        initializeDatabase();
    }

    /**
     * Update only the profile picture path for a user.
     */
    public boolean updateUserProfilePicture(int userId, String profilePath) {
        String sql = "UPDATE users SET profile_picture = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, profilePath);
            pstmt.setString(2, LocalDateTime.now().toString());
            pstmt.setInt(3, userId);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating profile picture: " + e.getMessage());
            return false;
        }
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Gets the currently logged-in user
     * @return the current User object, or null if no user is logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Sets the currently logged-in user
     * @param user the User object to set as current user
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create tables if they don't exist
            try (Statement stmt = conn.createStatement()) {
                // Users
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        email TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        full_name TEXT NOT NULL,
                        profile_picture TEXT DEFAULT NULL,
                        phone_no TEXT DEFAULT NULL,
                        address TEXT DEFAULT NULL,
                        user_category TEXT NOT NULL CHECK(user_category IN ('student', 'seller')),
                        status TEXT NOT NULL DEFAULT 'pending' CHECK(status IN ('approved','pending','rejected')),
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                """);

                // Shops (stall owners)
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS shops (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        owner_id INTEGER NOT NULL,
                        shop_name TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'pending' CHECK(status IN ('pending', 'approved', 'rejected')),
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(owner_id) REFERENCES users(id)
                    )
                """);

                // Food items
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS food_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        shop_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        price REAL NOT NULL,
                        points_multiplier REAL NOT NULL DEFAULT 1.0,
                        stock INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(shop_id) REFERENCES shops(id)
                    )
                """);

                // Cart
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cart (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        item_id INTEGER NOT NULL,
                        quantity INTEGER NOT NULL DEFAULT 1,
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id),
                        FOREIGN KEY(item_id) REFERENCES food_items(id)
                    )
                """);

                // Wishlist
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS wishlist (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        item_id INTEGER NOT NULL,
                        quantity INTEGER NOT NULL DEFAULT 1,
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id),
                        FOREIGN KEY(item_id) REFERENCES food_items(id)
                    )
                """);

                // Orders
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        shop_id INTEGER NOT NULL,
                        total_price REAL NOT NULL,
                        status TEXT NOT NULL DEFAULT 'pending' CHECK(status IN ('pending','preparing','delivered','cancelled','completed')),
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id),
                        FOREIGN KEY(shop_id) REFERENCES shops(id)
                    )
                """);

                // Order items
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS order_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER NOT NULL,
                        item_id INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        price REAL NOT NULL,
                        FOREIGN KEY(order_id) REFERENCES orders(id),
                        FOREIGN KEY(item_id) REFERENCES food_items(id)
                    )
                """);

                // Reward points (shop-wise)
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS reward_points (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        shop_id INTEGER NOT NULL,
                        points REAL NOT NULL DEFAULT 0,
                        UNIQUE(user_id, shop_id),
                        FOREIGN KEY(user_id) REFERENCES users(id),
                        FOREIGN KEY(shop_id) REFERENCES shops(id)
                    )
                """);

                // Reports (quality reports)
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS reports (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        shop_id INTEGER NOT NULL,
                        item_id INTEGER,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        attachments TEXT DEFAULT '[]',
                        status TEXT NOT NULL DEFAULT 'open' CHECK(status IN ('open','reviewing','resolved','rejected')),
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id),
                        FOREIGN KEY(shop_id) REFERENCES shops(id),
                        FOREIGN KEY(item_id) REFERENCES food_items(id)
                    )
                """);

                // Reviews (shared for shops or food items)
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS reviews (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        shop_id INTEGER,
                        food_item_id INTEGER,
                        rating INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5),
                        comment TEXT,
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id) REFERENCES users(id),
                        FOREIGN KEY(shop_id) REFERENCES shops(id),
                        FOREIGN KEY(food_item_id) REFERENCES food_items(id),
                        CHECK ((shop_id IS NOT NULL AND food_item_id IS NULL) OR (shop_id IS NULL AND food_item_id IS NOT NULL))
                    )
                """);

                // Payments
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER NOT NULL,
                        payment_method TEXT NOT NULL CHECK(payment_method IN ('card', 'cash', 'digital_wallet') OR payment_method LIKE 'digital_wallet_%'),
                        amount REAL NOT NULL,
                        status TEXT NOT NULL DEFAULT 'pending' CHECK(status IN ('pending', 'completed', 'failed', 'refunded')),
                        transaction_id TEXT,
                        payment_details TEXT,
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(order_id) REFERENCES orders(id)
                    )
                """);

                // Order status history
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS order_status_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        notes TEXT,
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(order_id) REFERENCES orders(id)
                    )
                """);

                // Ensure backward compatibility: add any missing columns in 'users' and 'reports'
                ensureUsersTableColumns(conn);
                ensureReportsTableColumns(conn);
                
                // Update payments table constraint if needed
                updatePaymentsTableConstraint(conn);
                
                // Initialize reports directory
                com.unieats.util.ReportFileManager.initializeDirectories();

                System.out.println("Database initialized successfully");
            }
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ensures the 'users' table contains all expected columns.
     */
    private void ensureUsersTableColumns(Connection conn) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("PRAGMA table_info(users)")) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (name != null) columns.add(name.toLowerCase());
            }
        }

        try (Statement s = conn.createStatement()) {
            if (!columns.contains("profile_picture")) {
                s.execute("ALTER TABLE users ADD COLUMN profile_picture TEXT DEFAULT NULL");
            }
            if (!columns.contains("phone_no")) {
                s.execute("ALTER TABLE users ADD COLUMN phone_no TEXT DEFAULT NULL");
            }
            if (!columns.contains("address")) {
                s.execute("ALTER TABLE users ADD COLUMN address TEXT DEFAULT NULL");
            }
            if (!columns.contains("status")) {
                s.execute("ALTER TABLE users ADD COLUMN status TEXT NOT NULL DEFAULT 'pending'");
            }
        }
    }

    /**
     * Updates the payments table constraint to allow digital_wallet_* payment methods
     */
    private void updatePaymentsTableConstraint(Connection conn) throws SQLException {
        // No-op migration: preserve existing payments table to avoid data loss across restarts/logouts.
        // Legacy databases will continue working; new installs get the correct schema via CREATE TABLE IF NOT EXISTS above.
    }

    /**
     * Ensures the 'reports' table contains all expected columns.
     * Adds missing columns using ALTER TABLE for databases created before these columns existed.
     */
    private void ensureReportsTableColumns(Connection conn) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("PRAGMA table_info(reports)")) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (name != null) columns.add(name.toLowerCase());
            }
        }

        try (Statement s = conn.createStatement()) {
            // Note: When adding NOT NULL columns to existing tables, provide a DEFAULT value.
            if (!columns.contains("item_id")) {
                s.execute("ALTER TABLE reports ADD COLUMN item_id INTEGER");
            }
            if (!columns.contains("title")) {
                s.execute("ALTER TABLE reports ADD COLUMN title TEXT DEFAULT ''");
            }
            if (!columns.contains("description")) {
                s.execute("ALTER TABLE reports ADD COLUMN description TEXT DEFAULT ''");
            }
            if (!columns.contains("attachments")) {
                s.execute("ALTER TABLE reports ADD COLUMN attachments TEXT DEFAULT '[]'");
            }
            
            // Check if address and description columns exist in shops table, add if not
            ResultSet shopColumns = s.executeQuery("PRAGMA table_info(shops)");
            Set<String> shopColumnNames = new HashSet<>();
            while (shopColumns.next()) {
                shopColumnNames.add(shopColumns.getString("name"));
            }
            shopColumns.close();
            
            if (!shopColumnNames.contains("address")) {
                s.execute("ALTER TABLE shops ADD COLUMN address TEXT DEFAULT ''");
            }
            if (!shopColumnNames.contains("description")) {
                s.execute("ALTER TABLE shops ADD COLUMN description TEXT DEFAULT ''");
            }
            
            // Create order_requests table if it doesn't exist
            s.execute("""
                CREATE TABLE IF NOT EXISTS order_requests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    customer_name TEXT NOT NULL,
                    shop_id INTEGER NOT NULL,
                    total_price REAL NOT NULL,
                    status TEXT NOT NULL DEFAULT 'pending' CHECK(status IN ('pending', 'accepted', 'declined')),
                    order_time TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    FOREIGN KEY(shop_id) REFERENCES shops(id)
                )
            """);
            
            // Create order_items table if it doesn't exist
            s.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL,
                    food_item_id INTEGER NOT NULL,
                    food_item_name TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    unit_price REAL NOT NULL,
                    total_price REAL NOT NULL,
                    FOREIGN KEY(order_id) REFERENCES order_requests(id) ON DELETE CASCADE
                )
            """);
            if (!columns.contains("status")) {
                s.execute("ALTER TABLE reports ADD COLUMN status TEXT NOT NULL DEFAULT 'open'");
            }
            if (!columns.contains("created_at")) {
                s.execute("ALTER TABLE reports ADD COLUMN created_at TEXT DEFAULT CURRENT_TIMESTAMP");
            }
            if (!columns.contains("updated_at")) {
                s.execute("ALTER TABLE reports ADD COLUMN updated_at TEXT DEFAULT CURRENT_TIMESTAMP");
            }
        }
    }
    
    /**
     * Create a new user
     * @param user the user to create
     * @return true if successful, false otherwise
     */
    public boolean createUser(User user) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(false); // Start transaction
            
            // Insert user
            String userSql = "INSERT INTO users (email, password, full_name, user_category, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(userSql)) {
                
                pstmt.setString(1, user.getEmail());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getFullName());
                pstmt.setString(4, user.getUserCategory());
                pstmt.setString(5, user.getStatus() == null ? "pending" : user.getStatus());
                
                String timestamp = LocalDateTime.now().toString();
                pstmt.setString(6, timestamp);
                pstmt.setString(7, timestamp);
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }
                
                // Get the last inserted row ID
                int userId;
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    userId = rs.getInt(1);
                }
                
                // If user is a seller, create a shop with default name
                if ("seller".equalsIgnoreCase(user.getUserCategory())) {
                    String shopName = user.getFullName() + "'s Shop";
                    
                    // Insert shop in the same transaction
                    String shopSql = "INSERT INTO shops (owner_id, shop_name, status, created_at, updated_at) VALUES (?, ?, 'pending', ?, ?)";
                    try (PreparedStatement shopStmt = conn.prepareStatement(shopSql)) {
                        shopStmt.setInt(1, userId);
                        shopStmt.setString(2, shopName);
                        shopStmt.setString(3, timestamp);
                        shopStmt.setString(4, timestamp);
                        
                        int shopRows = shopStmt.executeUpdate();
                        if (shopRows == 0) {
                            conn.rollback();
                            return false;
                        }
                    }
                }
                
                conn.commit(); // Commit the transaction
                return true;
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.err.println("Error in createUser: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Create a new shop for a seller
     * @param ownerId The ID of the shop owner (user)
     * @param shopName The name of the shop
     * @return true if successful, false otherwise
     * @deprecated Use the transaction-based approach in createUser instead
     */
    @Deprecated
    public boolean createShop(int ownerId, String shopName) {
        String sql = "INSERT INTO shops (owner_id, shop_name, status, created_at, updated_at) VALUES (?, ?, 'pending', ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ownerId);
            pstmt.setString(2, shopName);
            
            // Use simple string format for timestamps
            String timestamp = LocalDateTime.now().toString();
            pstmt.setString(3, timestamp);
            pstmt.setString(4, timestamp);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating shop: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    

    
    /**
     * Get user by email address
     * @param email the email address to search for
     * @return User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get user by id (primary key)
     * @param id the user id
     * @return User object if found, null otherwise
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting user by id: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }
    
    public boolean updateUser(User user) {
        String sql = """
            UPDATE users 
            SET email = ?, password = ?, full_name = ?, user_category = ?, phone_no = ?, address = ?, status = ?, updated_at = ?
            WHERE id = ?
            """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getUserCategory());
            pstmt.setString(5, user.getPhoneNo());
            pstmt.setString(6, user.getAddress());
            pstmt.setString(7, user.getStatus() == null ? "pending" : user.getStatus());
            pstmt.setString(8, LocalDateTime.now().toString());
            pstmt.setInt(9, user.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                com.unieats.services.EventNotifier.notifyChange("users");
            }
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    

    
    /**
     * Get the shop status for a seller
     * @param userId The ID of the user (seller)
     * @return The shop status ('pending', 'approved', 'rejected'), or null if user is not a seller or has no shop
     */
    public String getShopStatus(int userId) {
        String sql = "SELECT status FROM shops WHERE owner_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting shop status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null; // User is not a seller or has no shop
    }
    
    public boolean isEmailExists(String email) {
        return getUserByEmail(email) != null;
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getInt("id"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("full_name"),
            rs.getString("user_category")
        );
        
        try {
            String pic = rs.getString("profile_picture");
            user.setProfilePicture(pic);
        } catch (SQLException ignored) {}
        try {
            String phone = rs.getString("phone_no");
            user.setPhoneNo(phone);
        } catch (SQLException ignored) {}
        try {
            String addr = rs.getString("address");
            user.setAddress(addr);
        } catch (SQLException ignored) {}
        try {
            String st = rs.getString("status");
            user.setStatus(st);
        } catch (SQLException ignored) {}
        
        try {
            user.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
            user.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at")));
        } catch (Exception e) {
            System.err.println("Error parsing timestamps: " + e.getMessage());
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
        }
        
        return user;
    }
    
    public void closeConnection() {
        // SQLite automatically manages connections, but this method is provided for future extensibility
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}