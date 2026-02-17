package com.moderndaytech.perception.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Validates input for security threats in the perception system.
 * Single Responsibility Principle: ONLY handles security validation.
 * DevSecOps Shift-Left: validation happens before any processing.
 */
public class SecurityValidator {
    private static final Logger logger = LoggerFactory.getLogger(SecurityValidator.class);
    private static final int MAX_DATA_SIZE = 100_000_000; // 100MB

    /**
     * Validate sensor ID - blocks SQL injection, XSS, path traversal.
     * Throws SecurityException if malicious input detected.
     */
    public void validateSensorId(String sensorId) {
        if (sensorId == null || sensorId.trim().isEmpty()) {
            logger.warn("Security: Empty sensor ID rejected");
            throw new IllegalArgumentException("Sensor ID cannot be null or empty");
        }
        if (containsSqlInjectionPattern(sensorId)) {
            logger.error("Security: SQL injection attempt: {}", sensorId);
            throw new SecurityException("Invalid sensor ID pattern detected - SQL injection attempt");
        }
        if (containsXssPattern(sensorId)) {
            logger.error("Security: XSS attempt: {}", sensorId);
            throw new SecurityException("Invalid sensor ID pattern detected - XSS attempt");
        }
        if (containsPathTraversal(sensorId)) {
            logger.error("Security: Path traversal in sensor ID: {}", sensorId);
            throw new SecurityException("Path traversal detected");
        }
        logger.debug("Security: Sensor ID validated OK: {}", sensorId);
    }

    /**
     * Validate file path - blocks directory traversal attacks.
     * Throws SecurityException if traversal detected.
     */
    public void validateFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        Path path = Paths.get(filePath).normalize();
        if (path.toString().contains("..")) {
            logger.error("Security: Path traversal detected: {}", filePath);
            throw new SecurityException("Path traversal detected");
        }
        if (filePath.contains("../") || filePath.contains("..\\")
                || filePath.contains("%2e%2e") || filePath.contains("%252e")) {
            logger.error("Security: Path traversal pattern: {}", filePath);
            throw new SecurityException("Path traversal detected");
        }
        if (filePath.startsWith("/etc/") || filePath.toLowerCase().contains("system32")) {
            logger.error("Security: Sensitive path blocked: {}", filePath);
            throw new SecurityException("Path traversal detected");
        }
        logger.debug("Security: File path validated OK: {}", filePath);
    }

    /**
     * Validate data size - blocks memory exhaustion attacks.
     * Throws SecurityException if size exceeds 100MB limit.
     */
    public void validateDataSize(int dataSize) {
        if (dataSize < 0) {
            throw new IllegalArgumentException("Data size cannot be negative");
        }
        if (dataSize > MAX_DATA_SIZE) {
            logger.error("Security: Data size exceeds limit: {} bytes", dataSize);
            throw new SecurityException("Data size exceeds limit: " + dataSize);
        }
        logger.debug("Security: Data size validated OK: {} bytes", dataSize);
    }

    /**
     * Sanitize input by stripping HTML/script tags.
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        String cleaned = input.replaceAll("<[^>]*>", "");
        cleaned = cleaned.replaceAll("(?i)script", "");
        return cleaned;
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private boolean containsSqlInjectionPattern(String input) {
        String lower = input.toLowerCase();
        String[] keywords = {
            "drop table", "delete from", "insert into", "union select",
            "exec(", "execute(", "--", ";--", "/*", "*/", "' or ", "1=1"
        };
        for (String kw : keywords) {
            if (lower.contains(kw)) return true;
        }
        return false;
    }

    private boolean containsXssPattern(String input) {
        String lower = input.toLowerCase();
        String[] patterns = {
            "<script", "javascript:", "onerror=", "onload=",
            "<iframe", "<object", "<embed", "<svg", "alert("
        };
        for (String p : patterns) {
            if (lower.contains(p)) return true;
        }
        return false;
    }

    private boolean containsPathTraversal(String input) {
        return input.contains("../") || input.contains("..\\")
            || input.contains("%2e%2e") || input.contains("%252e");
    }
}
