package com.moderndaytech.perception.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Validates all external inputs for security vulnerabilities before processing.
 * Implements defense-in-depth security strategy with multiple validation layers.
 *
 * <h2>SOLID Principles Applied:</h2>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP)</strong>: This class has exactly one
 *       responsibility - security validation. It does NOT process sensors, fuse data, or detect
 *       objects. Security concerns are isolated here.</li>
 *   <li><strong>Dependency Inversion Principle (DIP)</strong>: High-level components depend on
 *       this validator through method calls, not on specific security implementations.</li>
 * </ul>
 *
 * <h2>Security Threats Mitigated:</h2>
 * <ul>
 *   <li><strong>SQL Injection (CWE-89)</strong>: Detects patterns like ' OR '1'='1, UNION SELECT,
 *       DROP TABLE, and other SQL manipulation attempts.</li>
 *   <li><strong>Cross-Site Scripting (XSS - CWE-79)</strong>: Blocks script tags, event handlers,
 *       and javascript: protocol attempts.</li>
 *   <li><strong>Path Traversal (CWE-22)</strong>: Prevents ../ sequences and absolute path access
 *       attempts to restricted directories.</li>
 *   <li><strong>Memory Exhaustion (CWE-400)</strong>: Enforces size limits to prevent denial of
 *       service through excessive memory allocation.</li>
 * </ul>
 *
 * <h2>Clean Code Techniques:</h2>
 * <ul>
 *   <li><strong>Fail-Fast Validation</strong>: Throws exceptions immediately on security violations
 *       rather than allowing invalid data to propagate.</li>
 *   <li><strong>Clear Method Names</strong>: validateSensorId(), validateFilePath() - no ambiguity
 *       about what each method validates.</li>
 *   <li><strong>Regex Patterns</strong>: Compiled regex patterns (static final) for efficiency.</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * SecurityValidator validator = new SecurityValidator();
 *
 * try {
 *     validator.validateSensorId(userInput);  // Check for SQL injection, XSS
 *     validator.validateFilePath(filePath);   // Check for path traversal
 *     validator.validateDataSize(dataLength); // Check for memory exhaustion
 *     // Proceed with processing - data is safe
 * } catch (SecurityException e) {
 *     // Log attack attempt and reject request
 *     logger.error("Security violation: {}", e.getMessage());
 *     throw new RuntimeException("Invalid input", e);
 * }
 * }</pre>
 *
 * @author Chagana Balachandran
 * @version 1.0
 * @since 2026-02-10
 */
public class SecurityValidator {
    private static final Logger logger = LoggerFactory.getLogger(SecurityValidator.class);
    private static final int MAX_SENSOR_ID_LENGTH = 1000;

    private static final Pattern SQL_INJECTION_PATTERN =
        Pattern.compile("('.+(--)|(;))|((\\bOR\\b).*(=))|drop table|delete from|union select",
            Pattern.CASE_INSENSITIVE);

    public void validateSensorId(String sensorId) {
        if (sensorId == null || sensorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Sensor ID cannot be null or empty");
        }
        if (sensorId.length() > MAX_SENSOR_ID_LENGTH) {
            logger.error("Security: Sensor ID length exceeds limit: {}", sensorId.length());
            throw new SecurityException("Sensor ID exceeds maximum allowed length");
        }
        if (containsSQLInjectionPattern(sensorId)) {
            logger.error("Security: SQL injection attempt detected in sensor ID: {}", sanitizeForLog(sensorId));
            throw new SecurityException("Invalid sensor ID pattern detected");
        }
        if (containsXSSPattern(sensorId)) {
            logger.error("Security: XSS attempt detected in sensor ID: {}", sanitizeForLog(sensorId));
            throw new SecurityException("XSS pattern detected in sensor ID");
        }
        if (containsPathTraversal(sensorId)) {
            logger.error("Security: Path traversal attempt detected in sensor ID: {}", sanitizeForLog(sensorId));
            throw new SecurityException("Path traversal detected in sensor ID");
        }
    }

    public void validateFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        String rawPath = filePath.trim();
        String normalizedSeparators = rawPath.replace("\\", "/");
        String lowerPath = normalizedSeparators.toLowerCase();

        // Check traversal patterns first
        if (lowerPath.contains("..") || lowerPath.contains("%2e") || lowerPath.contains("%252e")
            || lowerPath.contains("../") || lowerPath.contains("/etc/") || lowerPath.contains("/passwd")) {
            logger.error("Security: Path traversal attempt detected: {}", sanitizeForLog(filePath));
            throw new SecurityException("Path traversal detected");
        }

        // Block absolute paths (Unix, Windows drive letters, and UNC)
        if (lowerPath.startsWith("/") || lowerPath.startsWith("//") || lowerPath.matches("^[a-z]:/.*")) {
            logger.error("Security: Absolute path access attempt detected: {}", sanitizeForLog(filePath));
            throw new SecurityException("Path traversal detected");
        }

        Path path = Paths.get(rawPath).normalize();
        if (path.isAbsolute()) {
            logger.error("Security: Absolute path access attempt detected: {}", sanitizeForLog(filePath));
            throw new SecurityException("Path traversal detected");
        }
    }

    public void validateDataSize(int dataSize) {
        final int MAX_DATA_SIZE = 100_000_000;
        if (dataSize < 0) {
            throw new IllegalArgumentException("Data size cannot be negative");
        }
        if (dataSize > MAX_DATA_SIZE) {
            throw new SecurityException("Data size exceeds limit: " + dataSize);
        }
    }

    private boolean containsSQLInjectionPattern(String input) {
        String lower = input.toLowerCase();
        String[] keywords = {"drop table", "delete from", "insert into",
            "union select", "exec(", "execute(", "--", ";--", "/*", "*/"};
        for (String kw : keywords) {
            if (lower.contains(kw)) return true;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    private boolean containsXSSPattern(String input) {
        String lower = input.toLowerCase();
        String[] patterns = {"<script", "javascript:", "onerror=", "onload=",
            "<iframe", "<svg", "<object", "<embed"};
        for (String p : patterns) {
            if (lower.contains(p)) return true;
        }
        return false;
    }

    private boolean containsPathTraversal(String input) {
        return input.contains("../") || input.contains("..\\")
            || input.contains("%2e%2e") || input.contains("%252e");
    }

    private String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\r\n\t]", "_").substring(0, Math.min(input.length(), 100));
    }
}
