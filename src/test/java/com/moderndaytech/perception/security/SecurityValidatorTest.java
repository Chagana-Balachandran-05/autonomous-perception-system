package com.moderndaytech.perception.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class SecurityValidatorTest {

    private SecurityValidator validator;

    @BeforeEach
    void setup() {
        validator = new SecurityValidator();
    }

    @Test
    void testValidateSensorId_WithValidId_AcceptsInput() {
        assertThatNoException().isThrownBy(() -> validator.validateSensorId("LIDAR-01"));
    }

    @Test
    void testValidateSensorId_WithNullId_ThrowsException() {
        assertThatThrownBy(() -> validator.validateSensorId(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testValidateSensorId_WithEmptyId_ThrowsException() {
        assertThatThrownBy(() -> validator.validateSensorId(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testValidateSensorId_WithWhitespaceId_ThrowsException() {
        assertThatThrownBy(() -> validator.validateSensorId("   "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "sensor'; DROP TABLE users--",
        "sensor' OR '1'='1",
        "'; DELETE FROM sensors WHERE '1'='1'--",
        "sensor' UNION SELECT * FROM passwords--"
    })
    void testValidateSensorId_WithSQLInjection_ThrowsSecurityException(String malicious) {
        assertThatThrownBy(() -> validator.validateSensorId(malicious))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Invalid sensor ID pattern");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert('XSS')>",
        "javascript:alert('XSS')",
        "<iframe src='javascript:alert(1)'></iframe>",
        "<svg/onload=alert('XSS')>"
    })
    void testValidateSensorId_WithXSS_ThrowsSecurityException(String xss) {
        assertThatThrownBy(() -> validator.validateSensorId(xss))
            .isInstanceOf(SecurityException.class);
    }

    @Test
    void testValidateSensorId_WithPathTraversalPattern_ThrowsException() {
        assertThatThrownBy(() -> validator.validateSensorId("../etc/passwd"))
            .isInstanceOf(SecurityException.class);
    }

    @Test
    void testValidateSensorId_WithVeryLongId_ThrowsException() {
        String veryLong = "A".repeat(10000);
        assertThatThrownBy(() -> validator.validateSensorId(veryLong))
            .isInstanceOf(SecurityException.class);
    }

    @Test
    void testValidateSensorId_WithSpecialCharacters_HandledCorrectly() {
        assertThatNoException()
            .isThrownBy(() -> validator.validateSensorId("LIDAR-01_primary"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "../../etc/passwd",
        "../../../windows/system32",
        "..\\..\\windows\\system32"
    })
    void testValidateFilePath_WithPathTraversal_ThrowsSecurityException(String path) {
        assertThatThrownBy(() -> validator.validateFilePath(path))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Path traversal");
    }

    @Test
    void testValidateDataSize_WithHugeInput_ThrowsSecurityException() {
        assertThatThrownBy(() -> validator.validateDataSize(Integer.MAX_VALUE / 2))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Data size exceeds limit");
    }

    @Test
    void testValidateDataSize_WithReasonableInput_Accepted() {
        assertThatNoException().isThrownBy(() -> validator.validateDataSize(1_000_000));
    }
}
