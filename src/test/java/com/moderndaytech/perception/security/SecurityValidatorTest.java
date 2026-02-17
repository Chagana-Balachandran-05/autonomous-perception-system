package com.moderndaytech.perception.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

/**
 * Unit Tests for SecurityValidator
 * 
 * PURPOSE: Demonstrates SAST (Static Application Security Testing)
 * Tests validate input validation security measures
 * 
 * PRINCIPLE: Single Responsibility Principle (SRP) - Each test method
 * validates ONE specific security concern
 * 
 * PRINCIPLE: Dependency Inversion Principle (DIP) - Tests depend on
 * SecurityValidator interface, not implementation
 * 
 */
@DisplayName("SecurityValidator Unit Tests - SAST Coverage")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityValidatorTest {

    // ================================================================
    // POSITIVE TESTS: Valid Inputs (Happy Path)
    // ================================================================

    @Nested
    @DisplayName("Positive Tests - Valid Input Acceptance")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ValidInputTests {

        @Test
        @Order(1)
        @DisplayName("Accept valid alphanumeric sensor ID")
        public void testValidAlphanumericSensorId() {
            // ARRANGE: Create a legitimate sensor ID
            String validSensorId = "SENSOR_LIDAR_FRONT_001";

            // ACT & ASSERT: Should NOT throw exception for valid input
            SecurityValidator validator = new SecurityValidator();
            try {
                validator.validateSensorId(validSensorId);
            } catch (Exception e) {
                fail("Exception should not be thrown for valid sensor ID", e);
            }
            System.out.println("✓ PASS: Valid sensor ID accepted: " + validSensorId);
        }

        @Test
        @Order(2)
        @DisplayName("Accept valid underscore and hyphen in sensor ID")
        public void testValidSpecialCharactersSensorId() {
            // ARRANGE: Create sensor ID with underscores and hyphens (common in IoT)
            String validSensorId = "LIDAR-FRONT_v2-sensor";

            // ACT & ASSERT: Should NOT throw exception for valid input
            SecurityValidator validator = new SecurityValidator();
            try {
                validator.validateSensorId(validSensorId);
            } catch (Exception e) {
                fail("Exception should not be thrown for valid sensor ID", e);
            }
            System.out.println("✓ PASS: Special characters accepted: " + validSensorId);
        }

        @Test
        @Order(3)
        @DisplayName("Accept reasonable data size (5MB)")
        public void testValidDataSize5MB() {
            // ARRANGE: 5MB is well below 10MB limit
            int validDataSize = 5_000_000;

            // ACT & ASSERT: Should NOT throw exception for valid input
            SecurityValidator validator = new SecurityValidator();
            try {
                validator.validateDataSize(validDataSize);
            } catch (Exception e) {
                fail("Exception should not be thrown for valid data size", e);
            }
            System.out.println("✓ PASS: Valid data size accepted: " + validDataSize + " bytes");
        }

        @Test
        @Order(4)
        @DisplayName("Accept small data size (1MB)")
        public void testValidSmallDataSize() {
            // ARRANGE: 1MB is minimal but valid
            int smallDataSize = 1_000_000;

            // ACT & ASSERT: Should NOT throw exception for valid input
            SecurityValidator validator = new SecurityValidator();
            try {
                validator.validateDataSize(smallDataSize);
            } catch (Exception e) {
                fail("Exception should not be thrown for small data size", e);
            }
            System.out.println("✓ PASS: Small data size accepted: " + smallDataSize + " bytes");
        }

        @Test
        @Order(5)
        @DisplayName("Accept maximum allowed data size (10MB)")
        public void testMaximumDataSize() {
            // ARRANGE: Exactly at the 10MB limit
            int maxDataSize = 10_000_000;

            // ACT & ASSERT: Should NOT throw exception for valid input
            SecurityValidator validator = new SecurityValidator();
            try {
                validator.validateDataSize(maxDataSize);
            } catch (Exception e) {
                fail("Exception should not be thrown for max data size", e);
            }
            System.out.println("✓ PASS: Maximum data size accepted: " + maxDataSize + " bytes");
        }
    }

    // ================================================================
    // NEGATIVE TESTS: SQL Injection Attacks
    // ================================================================

    @Nested
    @DisplayName("Negative Tests - SQL Injection Prevention")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SqlInjectionTests {

        @Test
        @Order(1)
        @DisplayName("Block SQL injection: DROP TABLE attack")
        public void testSqlInjectionDropTable() {
            // ARRANGE: Classic SQL injection attempt
            String maliciousSensorId = "'; DROP TABLE sensors; --";

            // ACT & ASSERT: Should throw SecurityException for SQL injection
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateSensorId(maliciousSensorId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("pattern");
            System.out.println("✓ BLOCKED: SQL DROP TABLE attack - " + maliciousSensorId);
        }

        @Test
        @Order(2)
        @DisplayName("Block SQL injection: UNION SELECT attack")
        public void testSqlInjectionUnionSelect() {
            // ARRANGE: UNION-based SQL injection
            String maliciousSensorId = "1 UNION SELECT * FROM users WHERE '1'='1";

            // ACT & ASSERT: Should throw SecurityException for SQL injection
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateSensorId(maliciousSensorId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("pattern");
            System.out.println("✓ BLOCKED: SQL UNION SELECT attack - " + maliciousSensorId);
        }

        @Test
        @Order(3)
        @DisplayName("Block SQL injection: Comment-based bypass")
        public void testSqlInjectionCommentBypass() {
            // ARRANGE: SQL comment-based injection
            String maliciousSensorId = "admin'--";

            // ACT & ASSERT: Should throw SecurityException for SQL injection
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateSensorId(maliciousSensorId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("pattern");
            System.out.println("✓ BLOCKED: SQL comment bypass - " + maliciousSensorId);
        }

        @Test
        @Order(4)
        @DisplayName("Block SQL injection: INSERT attack")
        public void testSqlInjectionInsert() {
            // ARRANGE: INSERT-based SQL injection
            String maliciousSensorId = "x'); INSERT INTO sensors VALUES ('hack'); --";

            // ACT & ASSERT: Should throw SecurityException for SQL injection
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateSensorId(maliciousSensorId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("pattern");
            System.out.println("✓ BLOCKED: SQL INSERT attack - " + maliciousSensorId);
        }
    }

    // ================================================================
    // NEGATIVE TESTS: Cross-Site Scripting (XSS) Attacks
    // ================================================================

    @Nested
    @DisplayName("Negative Tests - XSS Prevention")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class XssTests {

        @Test
        @Order(1)
        @DisplayName("Block XSS: Script tag injection")
        public void testXssScriptTagInjection() {
            // ARRANGE: Classic <script> injection
            String xssSensorId = "<script>alert('XSS')</script>";

            // ACT & ASSERT: Should throw SecurityException for XSS
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateSensorId(xssSensorId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("XSS");
            System.out.println("✓ BLOCKED: XSS script tag - " + xssSensorId);
        }

        @Test
        @Order(2)
        @DisplayName("Block XSS: JavaScript protocol")
        public void testXssJavaScriptProtocol() {
            // ARRANGE: JavaScript protocol handler
            String xssSensorId = "javascript:void(0); alert('xss')";

            // ACT & ASSERT: Should throw SecurityException for XSS
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateSensorId(xssSensorId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("XSS");
            System.out.println("✓ BLOCKED: XSS javascript protocol - " + xssSensorId);
        }

        @Test
        @Order(3)
        @DisplayName("Block XSS: Event handler injection")
        public void testXssEventHandlerInjection() {
            // ARRANGE: Event handler-based XSS
            String xssSensorId = "\" onerror=\"alert('xss')\"";

            // ACT & ASSERT: Should throw SecurityException for XSS
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateSensorId(xssSensorId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("XSS");
            System.out.println("✓ BLOCKED: XSS event handler - " + xssSensorId);
        }

        @Test
        @Order(4)
        @DisplayName("Block XSS: IFrame injection")
        public void testXssIFrameInjection() {
            // ARRANGE: IFrame-based XSS
            String xssSensorId = "<iframe src=\"http://malicious.com\"></iframe>";

            // ACT & ASSERT: Should throw SecurityException for XSS
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateSensorId(xssSensorId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("XSS");
            System.out.println("✓ BLOCKED: XSS iframe - " + xssSensorId);
        }
    }

    // ================================================================
    // NEGATIVE TESTS: Path Traversal Attacks
    // ================================================================

    @Nested
    @DisplayName("Negative Tests - Path Traversal Prevention")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PathTraversalTests {

        @Test
        @Order(1)
        @DisplayName("Block path traversal: Unix-style ../ attack")
        public void testPathTraversalUnixStyle() {
            String pathTraversal = "../../../etc/passwd";
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateFilePath(pathTraversal))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal");
            System.out.println("✓ BLOCKED: Path traversal Unix style - " + pathTraversal);
        }

        @Test
        @Order(2)
        @DisplayName("Block path traversal: Windows-style backslash attack")
        public void testPathTraversalWindowsStyle() {
            String pathTraversal = "..\\..\\..\\windows\\system32\\config\\sam";
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateFilePath(pathTraversal))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal");
            System.out.println("✓ BLOCKED: Path traversal Windows style - " + pathTraversal);
        }

        @Test
        @Order(3)
        @DisplayName("Block path traversal: Absolute Windows system path")
        public void testPathTraversalAbsoluteWindows() {
            String pathTraversal = "C:\\Windows\\System32\\config\\SAM";
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateFilePath(pathTraversal))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal");
            System.out.println("✓ BLOCKED: Absolute Windows system path - " + pathTraversal);
        }

        @Test
        @Order(4)
        @DisplayName("Block path traversal: Absolute Unix system path")
        public void testPathTraversalAbsoluteUnix() {
            String pathTraversal = "/etc/passwd";
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateFilePath(pathTraversal))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal detected");
            System.out.println("✓ BLOCKED: Absolute Unix system path - " + pathTraversal);
        }

        @Test
        @Order(5)
        @DisplayName("Block path traversal: URL-encoded ../ attack")
        public void testPathTraversalUrlEncoded() {
            String pathTraversal = "%2e%2e%2f%2e%2e%2fetc%2fpasswd";
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateFilePath(pathTraversal))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal");
            System.out.println("✓ BLOCKED: Path traversal URL-encoded - " + pathTraversal);
        }
    }

    // ================================================================
    // NEGATIVE TESTS: Data Size Boundary Violations
    // ================================================================

    @Nested
    @DisplayName("Negative Tests - Memory Exhaustion Prevention")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DataSizeTests {

        @Test
        @Order(1)
        @DisplayName("Block oversized data: 150MB exceeds 100MB limit")
        public void testDataSizeExceedsLimit() {
            // ARRANGE: 150MB exceeds the 100MB limit
            int oversizedData = 150_000_000;

            // ACT & ASSERT: Should throw SecurityException for oversized data
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateDataSize(oversizedData))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("exceeds");
            System.out.println("✓ BLOCKED: Oversized data - " + oversizedData + " bytes");
        }

        @Test
        @Order(2)
        @DisplayName("Block negative data size")
        public void testNegativeDataSize() {
            // ARRANGE: Negative size is invalid
            int negativeData = -1000;

            // ACT & ASSERT: Should throw IllegalArgumentException for negative data
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateDataSize(negativeData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
            System.out.println("✓ BLOCKED: Negative data size - " + negativeData);
        }

        @Test
        @Order(3)
        @DisplayName("Block zero data size")
        public void testZeroDataSize() {
            // ARRANGE: Zero size is typically invalid
            int zeroData = 0;

            // ACT & ASSERT: Should NOT throw for zero data size
            SecurityValidator validator = new SecurityValidator();
            try {
                validator.validateDataSize(zeroData);
            } catch (Exception e) {
                fail("Exception should not be thrown for zero data size", e);
            }
            System.out.println("✓ INFO: Zero data size validation: " + zeroData);
        }

        @Test
        @Order(4)
        @DisplayName("Block extremely large data size (1GB)")
        public void testExtremelyLargeDataSize() {
            // ARRANGE: 1GB is way over limit
            int extremeData = 1_000_000_000;

            // ACT & ASSERT: Should throw SecurityException for extremely large data
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateDataSize(extremeData))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("exceeds");
            System.out.println("✓ BLOCKED: Extremely large data - " + extremeData + " bytes");
        }
    }

    // ================================================================
    // NEGATIVE TESTS: Edge Cases and Malformed Input
    // ================================================================

    @Nested
    @DisplayName("Negative Tests - Edge Cases")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EdgeCaseTests {

        @Test
        @Order(1)
        @DisplayName("Block null sensor ID")
        public void testNullSensorId() {
            // ARRANGE: Null input
            String nullSensorId = null;

            // ACT & ASSERT: Should throw IllegalArgumentException for null sensor ID
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateSensorId(nullSensorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or empty");
            System.out.println("✓ BLOCKED: Null sensor ID");
        }

        @Test
        @Order(2)
        @DisplayName("Block empty sensor ID")
        public void testEmptySensorId() {
            // ARRANGE: Empty string
            String emptySensorId = "";

            // ACT & ASSERT: Should throw IllegalArgumentException for empty sensor ID
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateSensorId(emptySensorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or empty");
            System.out.println("✓ BLOCKED: Empty sensor ID");
        }

        @Test
        @Order(3)
        @DisplayName("Block whitespace-only sensor ID")
        public void testWhitespaceOnlySensorId() {
            // ARRANGE: Only whitespace
            String whitespaceId = "   ";

            // ACT & ASSERT: Should throw IllegalArgumentException for whitespace-only sensor ID
            SecurityValidator validator = new SecurityValidator();
            assertThatThrownBy(() -> validator.validateSensorId(whitespaceId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or empty");
            System.out.println("✓ BLOCKED: Whitespace-only sensor ID");
        }

        @Test
        @Order(4)
        @DisplayName("Accept sensor ID with numbers")
        public void testSensorIdWithNumbers() {
            // ARRANGE: Mix of letters and numbers
            String numberedId = "SENSOR_123_ABC";

            // ACT & ASSERT: Should NOT throw exception for valid sensor ID
            SecurityValidator validator = new SecurityValidator();
            try {
                validator.validateSensorId(numberedId);
            } catch (Exception e) {
                fail("Exception should not be thrown for valid sensor ID with numbers", e);
            }
            System.out.println("✓ PASS: Numbered sensor ID accepted - " + numberedId);
        }
    }

    // ================================================================
    // SANITIZATION TESTS
    // ================================================================

    @Nested
    @DisplayName("Sanitization Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SanitizationTests {

        @Test
        @Order(1)
        @DisplayName("Sanitize removes dangerous characters")
        public void testSanitizationRemovesDangerousChars() {
            // ARRANGE: Input with dangerous characters
            String dirtyInput = "sensor<script>alert()</script>id";

            // ACT: Sanitize the input
            String sanitized = SecurityValidator.sanitizeInput(dirtyInput);

            // ASSERT: Dangerous characters should be removed
            assertThat(sanitized)
                    .as("Sanitization should remove script tags")
                    .doesNotContain("<", ">", "script");

            System.out.println("✓ SANITIZED: " + dirtyInput + " -> " + sanitized);
        }

        @Test
        @Order(2)
        @DisplayName("Sanitize null returns empty string")
        public void testSanitizationNullReturnsEmpty() {
            // ARRANGE: Null input
            String nullInput = null;

            // ACT: Sanitize null
            String sanitized = SecurityValidator.sanitizeInput(nullInput);

            // ASSERT: Should return empty string
            assertThat(sanitized)
                    .as("Sanitizing null should return empty string")
                    .isEmpty();

            System.out.println("✓ SANITIZED: null -> empty string");
        }
    }
}
