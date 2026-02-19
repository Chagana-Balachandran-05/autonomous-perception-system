package com.moderndaytech.perception.security;

import com.moderndaytech.perception.fusion.KalmanFilterFusion;
import com.moderndaytech.perception.fusion.FusionAlgorithm;
import com.moderndaytech.perception.fusion.SensorFusionProcessor;
import com.moderndaytech.perception.sensor.LiDARSensorData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

class AutonomousPerceptionSecurityTest {

    private final SecurityValidator validator = new SecurityValidator();

    @ParameterizedTest
    @ValueSource(strings = {
        "sensor'; DROP TABLE users--",
        "sensor' OR '1'='1",
        "sensor'; DELETE FROM sensors WHERE '1'='1'--",
        "sensor' UNION SELECT * FROM passwords--",
        "sensor'; EXEC sp_MSForEachTable 'DROP TABLE ?'--"
    })
    void testSQLInjectionPrevention_WithVariousPatterns_AllBlocked(String maliciousInput) {
        assertThatThrownBy(() -> validator.validateSensorId(maliciousInput))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Invalid sensor ID pattern");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert('XSS')>",
        "javascript:alert('XSS')",
        "<iframe src='javascript:alert(\"XSS\")'></iframe>",
        "<svg/onload=alert('XSS')>"
    })
    void testXSSPrevention_WithVariousPayloads_AllBlocked(String xssPayload) {
        assertThatThrownBy(() -> validator.validateSensorId(xssPayload))
            .isInstanceOf(SecurityException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "../../etc/passwd",
        "../../../windows/system32/config",
        "..\\..\\..\\windows\\system32",
        "/etc/passwd",
        "C:\\Windows\\System32\\config\\SAM"
    })
    void testPathTraversalPrevention_WithVariousAttempts_AllBlocked(String maliciousPath) {
        assertThatThrownBy(() -> validator.validateFilePath(maliciousPath))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Path traversal");
    }

    @Test
    void testMemoryExhaustionPrevention_WithHugeInput_Rejected() {
        assertThatThrownBy(() -> validator.validateDataSize(Integer.MAX_VALUE / 2))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Data size exceeds limit");
    }

    @Test
    void testMemoryExhaustionPrevention_WithReasonableInput_Accepted() {
        assertThatNoException().isThrownBy(() -> validator.validateDataSize(1_000_000));
    }

    @Test
    void testCompleteWorkflow_WithMaliciousInput_FailsSafely() {
        String maliciousSensorId = "sensor'; DROP TABLE users--";
        float[] maliciousData = new float[]{1.0f, 2.0f};

        assertThatThrownBy(() -> {
            validator.validateSensorId(maliciousSensorId);

            LiDARSensorData sensor = new LiDARSensorData(
                System.currentTimeMillis(),
                maliciousSensorId,
                maliciousData, maliciousData, maliciousData, maliciousData
            );

            FusionAlgorithm algorithm = new KalmanFilterFusion();
            SensorFusionProcessor processor = new SensorFusionProcessor(algorithm);
            processor.processSensors(Arrays.asList(sensor));
        })
            .isInstanceOf(SecurityException.class);
    }
}
