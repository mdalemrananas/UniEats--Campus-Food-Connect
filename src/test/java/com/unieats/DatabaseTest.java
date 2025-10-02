package com.unieats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {
    
    private DatabaseManager dbManager;
    
    @BeforeEach
    void setUp() {
        dbManager = DatabaseManager.getInstance();
    }
    
    @Test
    void testCreateUser() {
        // Create a test user
        User testUser = new User("testuser", "test@example.com", "password123", "Test User", "1234567890", "student");
        
        // Test user creation
        boolean result = dbManager.createUser(testUser);
        assertTrue(result, "User should be created successfully");
        
        // Verify user exists
        User retrievedUser = dbManager.getUserByUsername("testuser");
        assertNotNull(retrievedUser, "User should be retrievable by username");
        assertEquals("testuser", retrievedUser.getUsername());
        assertEquals("test@example.com", retrievedUser.getEmail());
        assertEquals("Test User", retrievedUser.getFullName());
        assertEquals("1234567890", retrievedUser.getPhoneNumber());
        assertEquals("student", retrievedUser.getUserCategory());
        
        // Clean up
        dbManager.deleteUser(retrievedUser.getId());
    }
    
    @Test
    void testDuplicateUsername() {
        // Create first user
        User user1 = new User("duplicateuser", "user1@example.com", "password123", "User One", null, "student");
        dbManager.createUser(user1);
        
        // Try to create second user with same username
        User user2 = new User("duplicateuser", "user2@example.com", "password456", "User Two", null, "student");
        boolean result = dbManager.createUser(user2);
        assertFalse(result, "Should not allow duplicate username");
        
        // Clean up
        User retrievedUser = dbManager.getUserByUsername("duplicateuser");
        if (retrievedUser != null) {
            dbManager.deleteUser(retrievedUser.getId());
        }
    }
    
    @Test
    void testDuplicateEmail() {
        // Create first user
        User user1 = new User("user1", "duplicate@example.com", "password123", "User One", null, "student");
        dbManager.createUser(user1);
        
        // Try to create second user with same email
        User user2 = new User("user2", "duplicate@example.com", "password456", "User Two", null, "seller");
        boolean result = dbManager.createUser(user2);
        assertFalse(result, "Should not allow duplicate email");
        
        // Clean up
        User retrievedUser = dbManager.getUserByEmail("duplicate@example.com");
        if (retrievedUser != null) {
            dbManager.deleteUser(retrievedUser.getId());
        }
    }
    
    @Test
    void testUpdateUser() {
        // Create a test user
        User testUser = new User("updateuser", "update@example.com", "password123", "Update User", null, "student");
        dbManager.createUser(testUser);
        
        // Retrieve and update user
        User retrievedUser = dbManager.getUserByUsername("updateuser");
        retrievedUser.setFullName("Updated User Name");
        retrievedUser.setPhoneNumber("9876543210");
        retrievedUser.setUserCategory("seller");
        
        // Test update
        boolean result = dbManager.updateUser(retrievedUser);
        assertTrue(result, "User should be updated successfully");
        
        // Verify update
        User updatedUser = dbManager.getUserByUsername("updateuser");
        assertEquals("Updated User Name", updatedUser.getFullName());
        assertEquals("9876543210", updatedUser.getPhoneNumber());
        assertEquals("seller", updatedUser.getUserCategory());
        
        // Clean up
        dbManager.deleteUser(updatedUser.getId());
    }
    
    @Test
    void testDeleteUser() {
        // Create a test user
        User testUser = new User("deleteuser", "delete@example.com", "password123", "Delete User", null, "student");
        dbManager.createUser(testUser);
        
        // Retrieve user to get ID
        User retrievedUser = dbManager.getUserByUsername("deleteuser");
        assertNotNull(retrievedUser, "User should exist before deletion");
        
        // Delete user
        boolean result = dbManager.deleteUser(retrievedUser.getId());
        assertTrue(result, "User should be deleted successfully");
        
        // Verify deletion
        User deletedUser = dbManager.getUserByUsername("deleteuser");
        assertNull(deletedUser, "User should not exist after deletion");
    }
} 