package com.moderndaytech.perception.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates input for security threats in the perception system.
 * Designed with security-first principles (DevSecOps) and keeps its job focused: just security validation.
 * If you want to make sure sensor data is safe, this is the class to use.
 */
public class SecurityValidator {
    private static final Logger logger = LoggerFactory.getLogger(SecurityValidator.class);
    
    /**
     * Validate sensor ID for security threats
     * Prevents SQL injection, XSS, and other injection attacks
     */
    public static boolean validateSensorId(String sensorId) {
        if (sensorId == null || sensorId.trim().isEmpty()) {
            logger.warn("Security: Empty sensor ID detected");
            return false;
        }
        
        // Check for SQL injection patterns
        if (containsSqlInjectionPattern(sensorId)) {
            logger.error("Security: SQL injection attempt detected in sensor ID: {}", sensorId);
            return false;
        }
        
        // Check for XSS patterns
        if (containsXssPattern(sensorId)) {
            logger.error("Security: XSS attempt detected in sensor ID: {}", sensorId);
            return false;
        }
        
        // Check for path traversal
        if (containsPathTraversal(sensorId)) {
            logger.error("Security: Path traversal attempt detected in sensor ID: {}", sensorId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate data size to prevent memory exhaustion attacks
     */
    public static boolean validateDataSize(int dataSize) {
        final int MAX_DATA_SIZE = 100_000_000; // 100MB limit
        
        if (dataSize < 0) {
            logger.warn("Security: Negative data size detected");
            return false;
        }
        
        if (dataSize > MAX_DATA_SIZE) {
            logger.error("Security: Data size exceeds limit: {} bytes (max: {})", 
                dataSize, MAX_DATA_SIZE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Detect SQL injection patterns
     */
    private static boolean containsSqlInjectionPattern(String input) {
        String lowerInput = input.toLowerCase();
        String[] sqlKeywords = {
            "drop table", "delete from", "insert into", "update ", 
            "union select", "exec(", "execute(", "--", ";--", "/*", "*/"
        };
        
        for (String keyword : sqlKeywords) {
            if (lowerInput.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Detect XSS patterns
     */
    private static boolean containsXssPattern(String input) {
        String lowerInput = input.toLowerCase();
        String[] xssPatterns = {
            "<script", "javascript:", "onerror=", "onload=", 
            "<iframe", "<object", "<embed"
        };
        
        for (String pattern : xssPatterns) {
            if (lowerInput.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Detect path traversal attempts
     */
    private static boolean containsPathTraversal(String input) {
        return input.contains("../") || input.contains("..\\") || 
               input.contains("%2e%2e") || input.contains("%252e");
    }
    
    /**
     * Sanitize input by removing potentially dangerous characters
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        
        String cleaned = input.replaceAll("<[^>]*>", "");
        
        cleaned = cleaned.replaceAll("(?i)script", "");
        
        return cleaned;
    }

}