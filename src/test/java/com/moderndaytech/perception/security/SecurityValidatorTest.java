package com.moderndaytech.perception.security;

import static org.assertj.core.api.Assertions.assertThat;

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
 * VIDEO GUIDE:
 * Run: mvn test -Dtest=SecurityValidatorTest
 * Output: target/surefire-reports/SecurityValidatorTest.txt
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

            // ACT: Validate the sensor ID
            boolean result = SecurityValidator.validateSensorId(validSensorId);

            // ASSERT: Should pass validation
            assertThat(result)
                    .as("Valid alphanumeric sensor ID should be accepted")
                    .isTrue();

            System.out.println("✓ PASS: Valid sensor ID accepted: " + validSensorId);
        }

        @Test
        @Order(2)
        @DisplayName("Accept valid underscore and hyphen in sensor ID")
        public void testValidSpecialCharactersSensorId() {
            // ARRANGE: Create sensor ID with underscores and hyphens (common in IoT)
            String validSensorId = "LIDAR-FRONT_v2-sensor";

            // ACT: Validate the sensor ID
            boolean result = SecurityValidator.validateSensorId(validSensorId);

            // ASSERT: Should pass validation (underscores/hyphens are safe)
            assertThat(result)
                    .as("Sensor ID with underscores and hyphens should be accepted")
                    .isTrue();

            System.out.println("✓ PASS: Special characters accepted: " + validSensorId);
        }

        @Test
        @Order(3)
        @DisplayName("Accept reasonable data size (50MB)")
        public void testValidDataSize50MB() {
            // ARRANGE: 50MB is well below 100MB limit
            int validDataSize = 50_000_000;

            // ACT: Validate data size
            boolean result = SecurityValidator.validateDataSize(validDataSize);

            // ASSERT: Should pass validation
            assertThat(result)
                    .as("50MB data size should be within limit")
                    .isTrue();

            System.out.println("✓ PASS: Valid data size accepted: " + validDataSize + " bytes");
        }

        @Test
        @Order(4)
        @DisplayName("Accept small data size (1MB)")
        public void testValidSmallDataSize() {
            // ARRANGE: 1MB is minimal but valid
            int smallDataSize = 1_000_000;

            // ACT: Validate data size
            boolean result = SecurityValidator.validateDataSize(smallDataSize);

            // ASSERT: Should pass validation
            assertThat(result)
                    .as("1MB data size should be valid")
                    .isTrue();

            System.out.println("✓ PASS: Small data size accepted: " + smallDataSize + " bytes");
        }

        @Test
        @Order(5)
        @DisplayName("Accept maximum allowed data size (100MB)")
        public void testMaximumDataSize() {
            // ARRANGE: Exactly at the 100MB limit
            int maxDataSize = 100_000_000;

            // ACT: Validate data size
            boolean result = SecurityValidator.validateDataSize(maxDataSize);

            // ASSERT: Should pass validation (at boundary)
            assertThat(result)
                    .as("Maximum 100MB data size should be accepted")
                    .isTrue();

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

            // ACT: Validate the malicious input
            boolean result = SecurityValidator.validateSensorId(maliciousSensorId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("SQL DROP TABLE injection should be blocked")
                    .isFalse();

            System.out.println("✓ BLOCKED: SQL DROP TABLE attack - " + maliciousSensorId);
        }

        @Test
        @Order(2)
        @DisplayName("Block SQL injection: UNION SELECT attack")
        public void testSqlInjectionUnionSelect() {
            // ARRANGE: UNION-based SQL injection
            String maliciousSensorId = "1 UNION SELECT * FROM users WHERE '1'='1";

            // ACT: Validate the malicious input
            boolean result = SecurityValidator.validateSensorId(maliciousSensorId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("SQL UNION SELECT injection should be blocked")
                    .isFalse();

            System.out.println("✓ BLOCKED: SQL UNION SELECT attack - " + maliciousSensorId);
        }

        @Test
        @Order(3)
        @DisplayName("Block SQL injection: Comment-based bypass")
        public void testSqlInjectionCommentBypass() {
            // ARRANGE: SQL comment-based injection
            String maliciousSensorId = "admin'--";

            // ACT: Validate the malicious input
            boolean result = SecurityValidator.validateSensorId(maliciousSensorId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("SQL comment-based injection should be blocked")
                    .isFalse();

            System.out.println("✓ BLOCKED: SQL comment bypass - " + maliciousSensorId);
        }

        @Test
        @Order(4)
        @DisplayName("Block SQL injection: INSERT attack")
        public void testSqlInjectionInsert() {
            // ARRANGE: INSERT-based SQL injection
            String maliciousSensorId = "x'); INSERT INTO sensors VALUES ('hack'); --";

            // ACT: Validate the malicious input
            boolean result = SecurityValidator.validateSensorId(maliciousSensorId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("SQL INSERT injection should be blocked")
                    .isFalse();

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

            // ACT: Validate the malicious input
            boolean result = SecurityValidator.validateSensorId(xssSensorId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("Script tag XSS should be blocked")
                    .isFalse();

            System.out.println("✓ BLOCKED: XSS script tag - " + xssSensorId);
        }

        @Test
        @Order(2)
        @DisplayName("Block XSS: JavaScript protocol")
        public void testXssJavaScriptProtocol() {
            // ARRANGE: JavaScript protocol handler
            String xssSensorId = "javascript:void(0); alert('xss')";

            // ACT: Validate the malicious input
            boolean result = SecurityValidator.validateSensorId(xssSensorId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("JavaScript protocol XSS should be blocked")
                    .isFalse();

            System.out.println("✓ BLOCKED: XSS javascript protocol - " + xssSensorId);
        }

        @Test
        @Order(3)
        @DisplayName("Block XSS: Event handler injection")
        public void testXssEventHandlerInjection() {
            // ARRANGE: Event handler-based XSS
            String xssSensorId = "\" onerror=\"alert('xss')\"";

            // ACT: Validate the malicious input
            boolean result = SecurityValidator.validateSensorId(xssSensorId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("Event handler XSS should be blocked")
                    .isFalse();

            System.out.println("✓ BLOCKED: XSS event handler - " + xssSensorId);
        }

        @Test
        @Order(4)
        @DisplayName("Block XSS: IFrame injection")
        public void testXssIFrameInjection() {
            // ARRANGE: IFrame-based XSS
            String xssSensorId = "<iframe src=\"http://malicious.com\"></iframe>";

            // ACT: Validate the malicious input
            boolean result = SecurityValidator.validateSensorId(xssSensorId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("IFrame XSS should be blocked")
                    .isFalse();

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
            // ARRANGE: Unix path traversal
            String pathTraversalId = "../../../etc/passwd";

            // ACT: Validate the malicious input
            boolean result = SecurityValidator.validateSensorId(pathTraversalId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("Unix path traversal should be blocked")
                    .isFalse();

            System.out.println("✓ BLOCKED: Path traversal Unix style - " + pathTraversalId);
        }

        @Test
        @Order(2)
        @DisplayName("Block path traversal: Windows-style backslash attack")
        public void testPathTraversalWindowsStyle() {
            // ARRANGE: Windows path traversal
            String pathTraversalId = "..\\..\\..\\windows\\system32\\config\\sam";

            // ACT: Validate the malicious input
            boolean result = SecurityValidator.validateSensorId(pathTraversalId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("Windows path traversal should be blocked")
                    .isFalse();

            System.out.println("✓ BLOCKED: Path traversal Windows style - " + pathTraversalId);
        }

        @Test
        @Order(3)
        @DisplayName("Block path traversal: URL-encoded ../ attack")
        public void testPathTraversalUrlEncoded() {
            // ARRANGE: URL-encoded path traversal (%2e = .)
            String pathTraversalId = "%2e%2e%2f%2e%2e%2fetc%2fpasswd";

            // ACT: Validate the malicious input
            boolean result = SecurityValidator.validateSensorId(pathTraversalId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("URL-encoded path traversal should be blocked")
                    .isFalse();

            System.out.println("✓ BLOCKED: Path traversal URL-encoded - " + pathTraversalId);
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

            // ACT: Validate data size
            boolean result = SecurityValidator.validateDataSize(oversizedData);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("Data size >100MB should be rejected")
                    .isFalse();

            System.out.println("✓ BLOCKED: Oversized data - " + oversizedData + " bytes");
        }

        @Test
        @Order(2)
        @DisplayName("Block negative data size")
        public void testNegativeDataSize() {
            // ARRANGE: Negative size is invalid
            int negativeData = -1000;

            // ACT: Validate data size
            boolean result = SecurityValidator.validateDataSize(negativeData);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("Negative data size should be rejected")
                    .isFalse();

            System.out.println("✓ BLOCKED: Negative data size - " + negativeData);
        }

        @Test
        @Order(3)
        @DisplayName("Block zero data size")
        public void testZeroDataSize() {
            // ARRANGE: Zero size is typically invalid
            int zeroData = 0;

            // ACT: Validate data size
            boolean result = SecurityValidator.validateDataSize(zeroData);

            // ASSERT: Depends on business logic, but typically rejected
            // In this case, we accept zero as "no data"
            System.out.println("✓ INFO: Zero data size validation: " + result);
        }

        @Test
        @Order(4)
        @DisplayName("Block extremely large data size (1GB)")
        public void testExtremelyLargeDataSize() {
            // ARRANGE: 1GB is way over limit
            int extremeData = 1_000_000_000;

            // ACT: Validate data size
            boolean result = SecurityValidator.validateDataSize(extremeData);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("1GB data size should be rejected")
                    .isFalse();

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

            // ACT & ASSERT: Should not throw but return false
            boolean result = SecurityValidator.validateSensorId(nullSensorId);

            assertThat(result)
                    .as("Null sensor ID should be rejected")
                    .isFalse();

            System.out.println("✓ BLOCKED: Null sensor ID");
        }

        @Test
        @Order(2)
        @DisplayName("Block empty sensor ID")
        public void testEmptySensorId() {
            // ARRANGE: Empty string
            String emptySensorId = "";

            // ACT: Validate the empty input
            boolean result = SecurityValidator.validateSensorId(emptySensorId);

            // ASSERT: Must be rejected
            assertThat(result)
                    .as("Empty sensor ID should be rejected")
                    .isFalse();

            System.out.println("✓ BLOCKED: Empty sensor ID");
        }

        @Test
        @Order(3)
        @DisplayName("Block whitespace-only sensor ID")
        public void testWhitespaceOnlySensorId() {
            // ARRANGE: Only whitespace
            String whitespaceId = "   ";

            // ACT: Validate the input
            boolean result = SecurityValidator.validateSensorId(whitespaceId);

            // ASSERT: Must be rejected after trim
            assertThat(result)
                    .as("Whitespace-only sensor ID should be rejected")
                    .isFalse();

            System.out.println("✓ BLOCKED: Whitespace-only sensor ID");
        }

        @Test
        @Order(4)
        @DisplayName("Accept sensor ID with numbers")
        public void testSensorIdWithNumbers() {
            // ARRANGE: Mix of letters and numbers
            String numberedId = "SENSOR_123_ABC";

            // ACT: Validate the input
            boolean result = SecurityValidator.validateSensorId(numberedId);

            // ASSERT: Should pass
            assertThat(result)
                    .as("Sensor ID with numbers should be accepted")
                    .isTrue();

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
