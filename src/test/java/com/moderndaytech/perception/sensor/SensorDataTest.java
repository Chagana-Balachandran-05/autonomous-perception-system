package com.moderndaytech.perception.sensor;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SensorDataTest {

    @Test
    void testProcessData_WithValidSensor_ProcessesSuccessfully() {
        SensorData sensor = createTestLiDARSensor();
        String result = sensor.processData();
        assertThat(result).isNotNull();
        assertThat(result).contains("PROCESSED");
    }

    @Test
    void testConstructor_WithNegativeTimestamp_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() ->
            new LiDARSensorData(-1L, "TEST-01",
                new float[]{1.0f}, new float[]{1.0f},
                new float[]{0.0f}, new float[]{0.8f}))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Timestamp must be positive");
    }

    @Test
    void testConstructor_WithNullSensorId_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() ->
            new LiDARSensorData(System.currentTimeMillis(), null,
                new float[]{1.0f}, new float[]{1.0f},
                new float[]{0.0f}, new float[]{0.8f}))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Sensor ID cannot be null");
    }

    @Test
    void testLiDARConstructor_WithNullArrays_ThrowsException() {
        assertThatThrownBy(() ->
            new LiDARSensorData(System.currentTimeMillis(), "LIDAR-01",
                null, null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");
    }

    @Test
    void testLiDARConstructor_WithMismatchedArrayLengths_ThrowsException() {
        assertThatThrownBy(() ->
            new LiDARSensorData(System.currentTimeMillis(), "LIDAR-01",
                new float[]{1.0f, 2.0f}, new float[]{1.0f},
                new float[]{0.0f}, new float[]{0.8f}))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("equal length");
    }

    @Test
    void testGetMetricsReport_ReturnsCompleteMetrics() {
        SensorData sensor = createTestLiDARSensor();
        String metrics = sensor.getMetricsReport();
        assertThat(metrics).contains("LiDAR");
        assertThat(metrics).contains("TEST-LIDAR-01");
    }

    @Test
    void testIsValid_WithValidData_ReturnsTrue() {
        SensorData sensor = createTestLiDARSensor();
        assertThat(sensor.isValid()).isTrue();
    }

    private LiDARSensorData createTestLiDARSensor() {
        float[] x = {1.0f, 2.0f, 3.0f};
        float[] y = {0.5f, 1.0f, 1.5f};
        float[] z = {0.0f, 0.0f, 0.0f};
        float[] intensity = {0.8f, 0.9f, 0.7f};
        return new LiDARSensorData(System.currentTimeMillis(), "TEST-LIDAR-01", x, y, z, intensity);
    }
}
