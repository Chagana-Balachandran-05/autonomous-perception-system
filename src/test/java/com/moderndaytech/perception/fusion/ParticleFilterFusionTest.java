package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.CameraSensorData;
import com.moderndaytech.perception.sensor.LiDARSensorData;
import com.moderndaytech.perception.sensor.SensorData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParticleFilterFusionTest {

    private ParticleFilterFusion fusion;

    @BeforeEach
    void setup() {
        fusion = new ParticleFilterFusion();
    }

    @Test
    void testFuse_WithValidSensors_ReturnsValidResult() {
        List<SensorData> sensors = Arrays.asList(createLiDAR(1000), createCamera());
        FusionResult result = fusion.fuse(sensors);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getConfidence()).isBetween(0.0, 1.0);
        assertThat(result.getSensorCount()).isEqualTo(2);
    }

    @Test
    void testFuse_WithEmptyList_ReturnsEmptyResult() {
        FusionResult result = fusion.fuse(Collections.emptyList());
        assertThat(result.getSensorCount()).isEqualTo(0);
        assertThat(result.getConfidence()).isEqualTo(0.0);
    }

    @Test
    void testFuse_WithNullList_ReturnsEmptyResult() {
        FusionResult result = fusion.fuse(null);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void testFuse_WithSingleSensor_StillProducesResult() {
        FusionResult result = fusion.fuse(List.of(createLiDAR(500)));
        assertThat(result).isNotNull();
        assertThat(result.getConfidence()).isGreaterThan(0.0);
    }

    @Test
    void testGetName_ReturnsNonEmptyString() {
        assertThat(fusion.getName()).isNotBlank();
    }

    @Test
    void testFuse_MoreValidSensors_ProducesHigherConfidence() {
        List<SensorData> oneSensor = List.of(createLiDAR(1000));
        List<SensorData> twoSensors = Arrays.asList(createLiDAR(1000), createCamera());

        FusionResult one = fusion.fuse(oneSensor);
        FusionResult two = fusion.fuse(twoSensors);

        assertThat(two.getConfidence()).isGreaterThanOrEqualTo(one.getConfidence());
    }

    private LiDARSensorData createLiDAR(int points) {
        float[] arr = new float[points];
        return new LiDARSensorData(System.currentTimeMillis(),
            "TEST-LIDAR", arr, arr, arr, arr);
    }

    private CameraSensorData createCamera() {
        return new CameraSensorData(System.currentTimeMillis(),
            "TEST-CAM", new byte[100], 10, 10);
    }
}
