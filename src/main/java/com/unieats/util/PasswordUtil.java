package com.unieats.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Hash a password using SHA-256
     * @param password the plain text password
     * @return the hashed password
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            // Fallback to simple hash if SHA-256 is not available
            return Integer.toString(password.hashCode());
        }
    }
    
    /**
     * Verify a password against a stored hash
     * @param plainPassword the plain text password to verify
     * @param storedHash the stored hash to compare against
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        String inputHash = hashPassword(plainPassword);
        return inputHash.equals(storedHash);
    }
    
    /**
     * Generate a random salt (for future enhancement)
     * @return a random salt string
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
} 