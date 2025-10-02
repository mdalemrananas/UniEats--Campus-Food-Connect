package com.unieats.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReportFileManager {
    private static final String REPORTS_DIR = "src/main/resources/reports";
    private static final String ATTACHMENTS_SUBDIR = "attachments";
    
    /**
     * Initialize the reports directory structure
     */
    public static void initializeDirectories() {
        try {
            Path reportsPath = Paths.get(REPORTS_DIR);
            Path attachmentsPath = reportsPath.resolve(ATTACHMENTS_SUBDIR);
            
            // Create directories if they don't exist
            Files.createDirectories(attachmentsPath);
            
            System.out.println("Reports directory initialized: " + reportsPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to initialize reports directory: " + e.getMessage());
        }
    }
    
    /**
     * Save a file to the reports attachments directory
     * @param sourceFile The original file to copy
     * @param reportId The report ID for organizing files
     * @return The relative path to the saved file
     */
    public static String saveAttachment(File sourceFile, int reportId) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IllegalArgumentException("Source file does not exist");
        }
        
        // Create report-specific subdirectory
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportSubDir = "report_" + reportId + "_" + timestamp;
        
        Path reportsPath = Paths.get(REPORTS_DIR);
        Path attachmentsPath = reportsPath.resolve(ATTACHMENTS_SUBDIR);
        Path reportPath = attachmentsPath.resolve(reportSubDir);
        
        // Create the report-specific directory
        Files.createDirectories(reportPath);
        
        // Generate unique filename to avoid conflicts
        String originalName = sourceFile.getName();
        String extension = getFileExtension(originalName);
        String baseName = getFileNameWithoutExtension(originalName);
        String uniqueName = baseName + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        
        // Copy file to reports directory
        Path targetPath = reportPath.resolve(uniqueName);
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path from reports directory
        Path relativePath = reportsPath.relativize(targetPath);
        return relativePath.toString().replace("\\", "/");
    }
    
    /**
     * Get the full path to a report attachment
     * @param relativePath The relative path stored in database
     * @return The full path to the file
     */
    public static String getAttachmentPath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        
        Path reportsPath = Paths.get(REPORTS_DIR);
        Path fullPath = reportsPath.resolve(relativePath);
        return fullPath.toString();
    }
    
    /**
     * Check if an attachment file exists
     * @param relativePath The relative path stored in database
     * @return true if file exists, false otherwise
     */
    public static boolean attachmentExists(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return false;
        }
        
        Path reportsPath = Paths.get(REPORTS_DIR);
        Path fullPath = reportsPath.resolve(relativePath);
        return Files.exists(fullPath);
    }
    
    /**
     * Delete an attachment file
     * @param relativePath The relative path stored in database
     * @return true if file was deleted, false otherwise
     */
    public static boolean deleteAttachment(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return false;
        }
        
        try {
            Path reportsPath = Paths.get(REPORTS_DIR);
            Path fullPath = reportsPath.resolve(relativePath);
            return Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            System.err.println("Failed to delete attachment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Parse attachments JSON and return list of relative paths
     * @param attachmentsJson JSON string containing attachment paths
     * @return List of relative paths
     */
    public static List<String> parseAttachments(String attachmentsJson) {
        List<String> attachments = new ArrayList<>();
        
        if (attachmentsJson == null || attachmentsJson.trim().isEmpty() || "[]".equals(attachmentsJson.trim())) {
            return attachments;
        }
        
        try {
            // Simple JSON parsing for array of strings
            String json = attachmentsJson.trim();
            if (json.startsWith("[") && json.endsWith("]")) {
                json = json.substring(1, json.length() - 1);
                if (!json.isEmpty()) {
                    String[] paths = json.split(",");
                    for (String path : paths) {
                        String cleanPath = path.trim();
                        if (cleanPath.startsWith("\"") && cleanPath.endsWith("\"")) {
                            cleanPath = cleanPath.substring(1, cleanPath.length() - 1);
                        }
                        attachments.add(cleanPath);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse attachments JSON: " + e.getMessage());
        }
        
        return attachments;
    }
    
    /**
     * Build attachments JSON from list of relative paths
     * @param attachments List of relative paths
     * @return JSON string
     */
    public static String buildAttachmentsJson(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < attachments.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(attachments.get(i)).append("\"");
        }
        json.append("]");
        
        return json.toString();
    }
    
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }
    
    private static String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
}
