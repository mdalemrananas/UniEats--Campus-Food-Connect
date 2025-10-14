package com.unieats.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:unieats.db";

    // =========================================================
    // =============== INITIALIZATION ==========================
    // =========================================================
    public static void initializeTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            try {
                stmt.execute("SELECT shop_id FROM food_items LIMIT 1");
                System.out.println("food_items table already exists with correct schema");
            } catch (SQLException e) {
                System.out.println("food_items table will be created by DatabaseManager");
            }
            System.out.println("✅ Seller database tables initialized successfully");
        } catch (SQLException e) {
            System.err.println("❌ Error initializing database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================
    // =============== FOOD ITEM OPERATIONS ====================
    // =========================================================
    public static boolean insertFoodItem(String name, double price, int stock,
                                         String description, String images,
                                         double discount, int shop_id) {
        String sql = "INSERT INTO food_items (name, price, stock, description, images, discount, shop_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, stock);
            pstmt.setString(4, description);
            pstmt.setString(5, images);
            pstmt.setDouble(6, discount);
            pstmt.setInt(7, shop_id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ObservableList<FoodItem_seller> getFoodsBySeller(int shopId) {
        ObservableList<FoodItem_seller> foodItems = FXCollections.observableArrayList();
        String query = "SELECT id, name, price FROM food_items WHERE shop_id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, shopId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                FoodItem_seller item = new FoodItem_seller();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("name"));
                item.setPrice(rs.getDouble("price"));
                foodItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foodItems;
    }

    public static boolean updateFoodPrice(int foodId, double newPrice) {
        String sql = "UPDATE food_items SET price = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, foodId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating food price: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteFood(int foodId) {
        String sql = "DELETE FROM food_items WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, foodId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting food item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // =============== SELLER ORDER DISPLAY ====================
    // =========================================================
    public static ObservableList<Order_seller> getSellerOrders() {
        ObservableList<Order_seller> orders = FXCollections.observableArrayList();
        String sql = """
            SELECT 
                o.id               AS order_id,
                o.user_id          AS user_id,
                o.shop_id          AS shop_id,
                o.status           AS status,
                o.created_at       AS created_at,
                fi.name            AS food_name,
                oi.quantity        AS quantity,
                oi.price           AS price
            FROM order_items oi
            JOIN orders o     ON oi.order_id = o.id
            JOIN food_items fi ON oi.item_id = fi.id
            ORDER BY o.created_at DESC
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Order_seller order = new Order_seller();
                order.setId(rs.getInt("order_id"));
                order.setCustomerName("User #" + rs.getInt("user_id"));
                order.setFoodName(rs.getString("food_name"));
                order.setQuantity(rs.getInt("quantity"));
                order.setTotalPrice(rs.getDouble("price"));
                order.setStatus(rs.getString("status"));
                order.setShopId(rs.getInt("shop_id"));
                order.setCreatedAt(rs.getString("created_at"));
                orders.add(order);
            }

            System.out.println("✅ Loaded " + orders.size() + " order_items joined rows for Order Management");

        } catch (SQLException e) {
            System.err.println("❌ Error loading seller_orders: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    // =========================================================
    // =============== OTHER ORDER OPERATIONS ==================
    // =========================================================
    public static ObservableList<Order_seller> getOrdersBySeller(int shopId) {
        ObservableList<Order_seller> orders = FXCollections.observableArrayList();
        String sql = """
            SELECT o.id, o.user_id, o.shop_id, o.total_price, o.status, o.created_at,
                   GROUP_CONCAT(fi.name, ', ') AS food_name,
                   SUM(oi.quantity) AS quantity
            FROM orders o
            JOIN order_items oi ON o.id = oi.order_id
            JOIN food_items fi ON oi.item_id = fi.id
            WHERE o.shop_id = ?
            GROUP BY o.id
            ORDER BY o.created_at DESC
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, shopId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order_seller order = new Order_seller();
                order.setId(rs.getInt("id"));
                order.setCustomerName("User #" + rs.getInt("user_id"));
                order.setFoodName(rs.getString("food_name"));
                order.setQuantity(rs.getInt("quantity"));
                order.setTotalPrice(rs.getDouble("total_price"));
                order.setStatus(rs.getString("status"));
                order.setShopId(rs.getInt("shop_id"));
                order.setCreatedAt(rs.getString("created_at"));
                orders.add(order);
            }

            System.out.println("✅ Loaded " + orders.size() + " joined orders for shop " + shopId);

        } catch (SQLException e) {
            System.err.println("❌ Error getting joined orders: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    public static boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, java.time.LocalDateTime.now().toString());
            pstmt.setInt(3, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void addSampleOrders(int shopId) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            int userId;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users LIMIT 1")) {
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("No users found. Cannot create sample orders.");
                    return;
                }
                userId = rs.getInt("id");
            }

            List<Integer> foodItemIds = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM food_items WHERE shop_id = ? LIMIT 3")) {
                stmt.setInt(1, shopId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    foodItemIds.add(rs.getInt("id"));
                }
            }

            if (foodItemIds.isEmpty()) {
                System.out.println("No food items found for shop " + shopId + ". Cannot create sample orders.");
                return;
            }

            String orderSql = "INSERT INTO orders (user_id, shop_id, total_price, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
            String orderItemSql = "INSERT INTO order_items (order_id, item_id, quantity, price) VALUES (?, ?, ?, ?)";

            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement orderItemStmt = conn.prepareStatement(orderItemSql)) {

                String timestamp = java.time.LocalDateTime.now().toString();

                // Sample Order 1
                orderStmt.setInt(1, userId);
                orderStmt.setInt(2, shopId);
                orderStmt.setDouble(3, 500.0);
                orderStmt.setString(4, "pending");
                orderStmt.setString(5, timestamp);
                orderStmt.setString(6, timestamp);
                orderStmt.executeUpdate();

                ResultSet keys = orderStmt.getGeneratedKeys();
                if (keys.next()) {
                    int orderId = keys.getInt(1);
                    orderItemStmt.setInt(1, orderId);
                    orderItemStmt.setInt(2, foodItemIds.get(0));
                    orderItemStmt.setInt(3, 2);
                    orderItemStmt.setDouble(4, 250.0);
                    orderItemStmt.executeUpdate();
                }

                // Sample Order 2
                orderStmt.setInt(1, userId);
                orderStmt.setInt(2, shopId);
                orderStmt.setDouble(3, 400.0);
                orderStmt.setString(4, "preparing");
                orderStmt.setString(5, timestamp);
                orderStmt.setString(6, timestamp);
                orderStmt.executeUpdate();

                keys = orderStmt.getGeneratedKeys();
                if (keys.next()) {
                    int orderId = keys.getInt(1);
                    orderItemStmt.setInt(1, orderId);
                    orderItemStmt.setInt(2, foodItemIds.size() > 1 ? foodItemIds.get(1) : foodItemIds.get(0));
                    orderItemStmt.setInt(3, 1);
                    orderItemStmt.setDouble(4, 400.0);
                    orderItemStmt.executeUpdate();
                }

                conn.commit();
                System.out.println("✅ Sample orders added successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding sample orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static ObservableList<Review> getAllReviews() {
    ObservableList<Review> reviews = FXCollections.observableArrayList();
    String sql = "SELECT id, user_id, food_item_id, rating, comment, created_at FROM reviews";

    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            Review review = new Review();
            review.setId(rs.getInt("id"));
            review.setUserId(rs.getInt("user_id"));
            review.setFoodId(rs.getInt("food_item_id")); // ✅ FIXED LINE
            review.setRating(rs.getInt("rating"));
            review.setComment(rs.getString("comment"));
            review.setCreatedAt(rs.getString("created_at"));
            reviews.add(review);
        }

        System.out.println("✅ Loaded " + reviews.size() + " reviews from database.");

    } catch (SQLException e) {
        System.err.println("❌ Error loading reviews: " + e.getMessage());
        e.printStackTrace();
    }

    return reviews;
}


    // =========================================================
    // =============== MODEL CLASSES ===========================
    // =========================================================
    public static class FoodItem_seller {
        private int id;
        private String name;
        private double price;
        private int shopId;
        private String createdAt;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public int getShopId() { return shopId; }
        public void setShopId(int shopId) { this.shopId = shopId; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    public static class Order_seller {
        private int id;
        private String customerName;
        private String foodName;
        private int quantity;
        private double totalPrice;
        private String status;
        private int shopId;
        private String createdAt;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getFoodName() { return foodName; }
        public void setFoodName(String foodName) { this.foodName = foodName; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getShopId() { return shopId; }
        public void setShopId(int shopId) { this.shopId = shopId; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
    public static class Review {
    private int id;
    private int userId;
    private int foodId;
    private int rating;
    private String comment;
    private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

}
