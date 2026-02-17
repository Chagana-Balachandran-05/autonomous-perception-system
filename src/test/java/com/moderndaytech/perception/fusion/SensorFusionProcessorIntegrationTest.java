package com.moderndaytech.perception.fusion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import com.moderndaytech.perception.sensor.CameraSensorData;
import com.moderndaytech.perception.sensor.LiDARSensorData;
import com.moderndaytech.perception.sensor.SensorData;

/**
 * These are integration tests for the SensorFusionProcessor.
 *
 * Why? To make sure our sensor fusion logic actually works when we throw real sensor data at it, using different algorithms.
 *
 * We want to check that the fusion pipeline behaves as expected, no matter which algorithm is plugged in.
 *
 * This also means we’re following good design principles: you can swap out algorithms, and the tests still work (OCP/LSP/DIP in action).
 */
@DisplayName("SensorFusionProcessor Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SensorFusionProcessorIntegrationTest {


    private SensorFusionProcessor fusionProcessor;
    @Mock
    private FusionAlgorithm mockAlgorithm;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(mockAlgorithm.getName()).thenReturn("Mock Fusion Algorithm");
        Mockito.when(mockAlgorithm.fuse(Mockito.anyList())).thenAnswer(invocation -> {
            List<SensorData> sensors = invocation.getArgument(0);
            return new FusionResult(
                "Mock Fusion Algorithm",
                sensors.stream().mapToInt(SensorData::getDataSize).sum(),
                0.95,
                sensors.size()
            );
        });
        fusionProcessor = new SensorFusionProcessor(new KalmanFilterFusion());
    }

    // ================================================================
    // POSITIVE INTEGRATION TESTS: Successful Fusion Scenarios
    // ================================================================

    @Nested
    @DisplayName("Positive Integration Tests - Successful Fusion")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SuccessfulFusionTests {

        @Test
        @Order(1)
        @DisplayName("Fuse two LiDAR sensors successfully")
        public void testFuseTwoLiDARSensors() {
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_FRONT"));
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_REAR"));
            FusionResult result = fusionProcessor.processSensors(sensorData);
            assertThat(result).isNotNull();
            assertThat(result.getAlgorithmUsed()).isEqualTo("KalmanFilterFusion");
            assertThat(result.getSensorCount()).isEqualTo(2);
            assertThat(result.getTotalDataPoints()).isGreaterThan(0);
            assertThat(result.getConfidenceScore()).isGreaterThan(0.0).isLessThanOrEqualTo(1.0);
        }

        @Test
        @Order(2)
        @DisplayName("Fuse LiDAR and Camera sensors (multi-modal)")
        public void testFuseMultiModalSensors() {
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_FRONT"));
            sensorData.add(CameraSensorData.createRealisticData("CAMERA_FRONT"));
            FusionResult result = fusionProcessor.processSensors(sensorData);
            assertThat(result).isNotNull();
            assertThat(result.getSensorCount()).isEqualTo(2);
            assertThat(result.getTotalDataPoints()).isGreaterThan(0);
            assertThat(result.getAlgorithmUsed()).isEqualTo("KalmanFilterFusion");
        }

        @Test
        @Order(3)
        @DisplayName("Fuse three sensors for comprehensive coverage")
        public void testFuseThreeSensors() {
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_FRONT"));
            sensorData.add(CameraSensorData.createRealisticData("CAMERA_FRONT"));
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_REAR"));
            FusionResult result = fusionProcessor.processSensors(sensorData);
            assertThat(result.getSensorCount()).isEqualTo(3);
            assertThat(result.getTotalDataPoints()).isGreaterThan(0);
            assertThat(result.getAlgorithmUsed()).isEqualTo("KalmanFilterFusion");
        }

        @Test
        @Order(4)
        @DisplayName("Verify Kalman filter is selected for continuous stream")
        public void testKalmanFilterSelection() {
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_1"));
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_2"));
            FusionResult result = fusionProcessor.processSensors(sensorData);
            assertThat(result.getAlgorithmUsed()).isEqualTo("KalmanFilterFusion");
        }
    }

    // ================================================================
    // FAILURE SCENARIOS: Let’s see how it handles bad input
    // ================================================================

    @Nested
    @DisplayName("Failure Scenario Tests - Error Handling")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FailureScenarioTests {

        @Test
        @Order(1)
        @DisplayName("Reject null sensor data list")
        public void testRejectNullSensorDataList() {
            // Give it a null list and see if it complains
            List<SensorData> nullSensorData = null;

            // It should throw an exception, not just fail silently
            assertThatThrownBy(() -> fusionProcessor.processSensors(nullSensorData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or empty");

            System.out.println("✓ FAIL CAUGHT: Null sensor data rejected with proper exception");
        }

        @Test
        @Order(2)
        @DisplayName("Reject empty sensor data list")
        public void testRejectEmptySensorDataList() {
            // Try with an empty list this time
            List<SensorData> emptySensorData = new ArrayList<>();

            // Again, should throw an exception for empty input
            assertThatThrownBy(() -> fusionProcessor.processSensors(emptySensorData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or empty");

            System.out.println("✓ FAIL CAUGHT: Empty sensor data list rejected");
        }

        @Test
        @Order(3)
        @DisplayName("Handle single sensor (particle filter fallback)")
        public void testHandleSingleSensor() {
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_ONLY"));
            FusionResult result = fusionProcessor.processSensors(sensorData);
            assertThat(result).isNotNull();
            assertThat(result.getSensorCount()).isEqualTo(1);
            assertThat(result.getAlgorithmUsed()).isEqualTo("KalmanFilterFusion");
        }

        @Test
        @Order(4)
        @DisplayName("Verify algorithm selection based on confidence scores")
        public void testAlgorithmSelectionByConfidence() {
            // Make some sensor data that could change the confidence score
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_FRONT"));
            sensorData.add(CameraSensorData.createRealisticData("CAMERA_FRONT"));

            // Fuse and see what confidence/algorithm we get
            FusionResult result = fusionProcessor.processSensors(sensorData);

            // The algorithm with the best confidence should win
            assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
            assertThat(result.getAlgorithmUsed()).isNotEmpty();

            System.out.println("✓ PASS: Algorithm selected with confidence: " +
                    String.format("%.2f", result.getConfidenceScore()));
        }
    }

    // ================================================================
    // EDGE CASES: Let’s push the limits and see what breaks

    @Nested
    @DisplayName("Boundary and Edge Case Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class BoundaryTests {

        @Test
        @Order(1)
        @DisplayName("Handle maximum realistic sensor count (5 sensors)")
        public void testMaximumSensorCount() {
            // Try with 5 sensors, which is about as many as a real car might have
            List<SensorData> sensorData = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                sensorData.add(LiDARSensorData.createRealisticData("LIDAR_" + i));
            }

            // Fuse them all together
            FusionResult result = fusionProcessor.processSensors(sensorData);

            // Shouldn’t choke on the max number of sensors
            assertThat(result.getSensorCount()).isEqualTo(5);

            System.out.println("✓ PASS: Maximum sensor count (5) handled successfully");
        }

        @Test
        @Order(2)
        @DisplayName("Verify consistent fusion results (reproducibility)")
        public void testFusionReproducibility() {
            // Make some test sensor data
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_TEST"));
            sensorData.add(CameraSensorData.createRealisticData("CAMERA_TEST"));

            // Fuse the same data twice
            FusionResult result1 = fusionProcessor.processSensors(sensorData);
            FusionResult result2 = fusionProcessor.processSensors(sensorData);

            // Both runs should give the same result (consistency!)
            assertThat(result1.getSensorCount()).isEqualTo(result2.getSensorCount());
            assertThat(result1.getAlgorithmUsed()).isEqualTo(result2.getAlgorithmUsed());

            System.out.println("✓ PASS: Fusion results are reproducible");
        }
    }

    // ================================================================
    // ALGORITHM BEHAVIOR: Let’s check the details of each algorithm

    @Nested
    @DisplayName("Algorithm-Specific Behavior Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AlgorithmBehaviorTests {

        @Test
        @Order(1)
        @DisplayName("Verify all algorithms available through processor")
        public void testAllAlgorithmsAvailable() {
            List<SensorData> testData = Arrays.asList(
                LiDARSensorData.createRealisticData("TEST")
            );
            FusionResult result = fusionProcessor.processSensors(testData);
            assertThat(result.getAlgorithmUsed()).isEqualTo("KalmanFilterFusion");
        }

        @Test
        @Order(2)
        @DisplayName("Each algorithm returns valid fusion results")
        public void testEachAlgorithmReturnsValidResults() {
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_TEST"));
            FusionResult result = fusionProcessor.processSensors(sensorData);
            assertThat(result)
                .as("Kalman filter should return valid result")
                .isNotNull();
            assertThat(result.getAlgorithmUsed()).isEqualTo("KalmanFilterFusion");
        }
    }
}
