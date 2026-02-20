package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.LiDARSensorData;
import com.moderndaytech.perception.sensor.CameraSensorData;
import com.moderndaytech.perception.sensor.SensorData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class SensorFusionProcessorIntegrationTest {

    @Test
    void testProcessSensors_WithLiDARAndCamera_ProducesFusedResult() {
        long baseTimestamp = System.currentTimeMillis();
        LiDARSensorData lidar = createLiDARSensor(baseTimestamp);
        CameraSensorData camera = createCameraSensor(baseTimestamp + 120);
        List<SensorData> sensors = Arrays.asList(lidar, camera);

        FusionAlgorithm algorithm = new KalmanFilterFusion();
        SensorFusionProcessor processor = new SensorFusionProcessor(algorithm);

        FusionResult result = processor.processSensors(sensors);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getSensorCount()).isGreaterThan(0);
        assertThat(result.getTimestamp()).isGreaterThan(0);
    }

    @Test
    void testProcessSensors_WithCustomSyncWindow_ExcludesOutOfWindowSensors() {
        long baseTimestamp = System.currentTimeMillis();
        LiDARSensorData lidar = createLiDARSensor(baseTimestamp);
        CameraSensorData camera = createCameraSensor(baseTimestamp + 200);
        List<SensorData> sensors = Arrays.asList(lidar, camera);

        SensorFusionProcessor tightProcessor =
            new SensorFusionProcessor(new KalmanFilterFusion(), 100);
        SensorFusionProcessor looseProcessor =
            new SensorFusionProcessor(new KalmanFilterFusion(), 300);

        FusionResult tightResult = tightProcessor.processSensors(sensors);
        FusionResult looseResult = looseProcessor.processSensors(sensors);

        assertThat(looseResult.getSensorCount())
            .isGreaterThanOrEqualTo(tightResult.getSensorCount());
    }

    @ParameterizedTest
    @MethodSource("provideFusionAlgorithms")
    void testFusionAlgorithm_ProducesValidResult(FusionAlgorithm algorithm) {
        List<SensorData> sensors = Arrays.asList(createLiDARSensor(), createCameraSensor());
        FusionResult result = algorithm.fuse(sensors);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getConfidence()).isBetween(0.0, 1.0);
    }

    @ParameterizedTest
    @MethodSource("provideFusionAlgorithms")
    void testProcessSensors_WithDifferentAlgorithms_AllProduceValidResults(FusionAlgorithm algorithm) {
        List<SensorData> sensors = Arrays.asList(createLiDARSensor(), createCameraSensor());
        SensorFusionProcessor processor = new SensorFusionProcessor(algorithm);
        FusionResult result = processor.processSensors(sensors);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getSensorCount()).isGreaterThan(0);
        assertThat(result.getTimestamp()).isGreaterThan(0);
    }

    static Stream<FusionAlgorithm> provideFusionAlgorithms() {
        return Stream.of(
            new KalmanFilterFusion(),
            new ParticleFilterFusion(),
            new ExtendedKalmanFilterFusion()
        );
    }

    private LiDARSensorData createLiDARSensor() {
        return createLiDARSensor(System.currentTimeMillis());
    }

    private LiDARSensorData createLiDARSensor(long timestamp) {
        float[] x = {1.0f, 2.0f, 3.0f};
        float[] y = {0.5f, 1.0f, 1.5f};
        float[] z = {0.0f, 0.0f, 0.0f};
        float[] intensity = {0.8f, 0.9f, 0.7f};
        return new LiDARSensorData(timestamp, "TEST-LIDAR-01", x, y, z, intensity);
    }

    private CameraSensorData createCameraSensor() {
        return createCameraSensor(System.currentTimeMillis());
    }

    private CameraSensorData createCameraSensor(long timestamp) {
        return new CameraSensorData(timestamp, "TEST-CAM-01",
            new byte[100], 10, 10);
    }
}
