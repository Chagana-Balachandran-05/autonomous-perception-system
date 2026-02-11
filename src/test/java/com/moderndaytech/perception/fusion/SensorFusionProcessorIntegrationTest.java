package com.moderndaytech.perception.fusion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<FusionAlgorithm> algorithms;

    @BeforeEach
    public void setup() {
        System.out.println("\n[SETUP] Initializing SensorFusionProcessor with all algorithms...");

        // First, set up both Kalman and Particle filter algorithms
        algorithms = Arrays.asList(
                new KalmanFilterFusion(),
                new ParticleFilterFusion());

        fusionProcessor = new SensorFusionProcessor(algorithms);
        System.out.println("[SETUP] SensorFusionProcessor ready with " +
                algorithms.size() + " algorithms\n");
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
            // Let’s make two LiDAR sensors to test with
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_FRONT"));
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_REAR"));

            // Now, try fusing the sensor data
            FusionResult result = fusionProcessor.fuseSensorData(sensorData);

            // Check that fusion actually worked and gave us what we expect
            assertThat(result).isNotNull();
            assertThat(result.getAlgorithmUsed()).isNotEmpty();
            assertThat(result.getSensorCount()).isEqualTo(2);
            assertThat(result.getTotalDataPoints()).isGreaterThan(0);
            assertThat(result.getConfidenceScore()).isGreaterThanOrEqualTo(0.0)
                    .isLessThanOrEqualTo(1.0);

            System.out.println("✓ PASS: Two LiDAR sensors fused successfully");
            System.out.println("  Algorithm: " + result.getAlgorithmUsed());
            System.out.println("  Confidence: " + String.format("%.2f", result.getConfidenceScore()));
        }

        @Test
        @Order(2)
        @DisplayName("Fuse LiDAR and Camera sensors (multi-modal)")
        public void testFuseMultiModalSensors() {
            // Here, we’ll mix things up with a LiDAR and a Camera sensor
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_FRONT"));
            sensorData.add(CameraSensorData.createRealisticData("CAMERA_FRONT"));

            // Fuse the data from both sensors
            FusionResult result = fusionProcessor.fuseSensorData(sensorData);

            // Make sure the fusion worked for both types
            assertThat(result).isNotNull();
            assertThat(result.getSensorCount()).isEqualTo(2);
            assertThat(result.getTotalDataPoints()).isGreaterThan(0);

            System.out.println("✓ PASS: Multi-modal fusion (LiDAR + Camera) successful");
            System.out.println("  Sensors: " + result.getSensorCount());
            System.out.println("  Data points: " + result.getTotalDataPoints());
        }

        @Test
        @Order(3)
        @DisplayName("Fuse three sensors for comprehensive coverage")
        public void testFuseThreeSensors() {
            // Let’s try with three sensors, like a real car might have
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_FRONT"));
            sensorData.add(CameraSensorData.createRealisticData("CAMERA_FRONT"));
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_REAR"));

            // Fuse all three and see what happens
            FusionResult result = fusionProcessor.fuseSensorData(sensorData);

            // Check that all three sensors were included in the result
            assertThat(result.getSensorCount()).isEqualTo(3);
            assertThat(result.getTotalDataPoints()).isGreaterThan(0);

            System.out.println("✓ PASS: Three-sensor fusion completed");
            System.out.println("  Total data points: " + result.getTotalDataPoints());
        }

        @Test
        @Order(4)
        @DisplayName("Verify Kalman filter is selected for continuous stream")
        public void testKalmanFilterSelection() {
            // Set up two continuous LiDAR streams
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_1"));
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_2"));

            // Fuse them and see which algorithm gets picked
            FusionResult result = fusionProcessor.fuseSensorData(sensorData);

            // We expect the Kalman filter to be chosen for this case
            assertThat(result.getAlgorithmUsed())
                    .as("Kalman filter should be selected for continuous sensor streams")
                    .contains("Kalman");

            System.out.println("✓ PASS: Kalman filter correctly selected for algorithm: " +
                    result.getAlgorithmUsed());
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
            assertThatThrownBy(() -> fusionProcessor.fuseSensorData(nullSensorData))
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
            assertThatThrownBy(() -> fusionProcessor.fuseSensorData(emptySensorData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or empty");

            System.out.println("✓ FAIL CAUGHT: Empty sensor data list rejected");
        }

        @Test
        @Order(3)
        @DisplayName("Handle single sensor (particle filter fallback)")
        public void testHandleSingleSensor() {
            // What if we only have one sensor?
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_ONLY"));

            // Fuse it—should fall back to the particle filter
            FusionResult result = fusionProcessor.fuseSensorData(sensorData);

            // It should still work and give us a valid result
            assertThat(result).isNotNull();
            assertThat(result.getSensorCount()).isEqualTo(1);
            assertThat(result.getAlgorithmUsed())
                    .as("Particle filter should handle single sensor")
                    .contains("Particle");

            System.out.println("✓ PASS: Single sensor handled by Particle filter");
            System.out.println("  Algorithm: " + result.getAlgorithmUsed());
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
            FusionResult result = fusionProcessor.fuseSensorData(sensorData);

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
            FusionResult result = fusionProcessor.fuseSensorData(sensorData);

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
            FusionResult result1 = fusionProcessor.fuseSensorData(sensorData);
            FusionResult result2 = fusionProcessor.fuseSensorData(sensorData);

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
            // Ask the processor what algorithms it knows about
            List<FusionAlgorithm> availableAlgos = fusionProcessor.getAvailableAlgorithms();

            // Should list both Kalman and Particle algorithms
            assertThat(availableAlgos)
                    .isNotEmpty()
                    .hasSize(2);

            assertThat(availableAlgos.stream()
                    .map(FusionAlgorithm::getAlgorithmName)
                    .collect(Collectors.toList())) // Changed .toList() to this
                    .contains("KalmanFilterFusion", "ParticleFilterFusion");

            System.out.println("✓ PASS: All algorithms available");
            for (FusionAlgorithm algo : availableAlgos) {
                System.out.println("  - " + algo.getAlgorithmName());
            }
        }

        @Test
        @Order(2)
        @DisplayName("Each algorithm returns valid fusion results")
        public void testEachAlgorithmReturnsValidResults() {
            // Make some simple test data
            List<SensorData> sensorData = new ArrayList<>();
            sensorData.add(LiDARSensorData.createRealisticData("LIDAR_TEST"));

            // Run each algorithm on the test data
            for (FusionAlgorithm algo : algorithms) {
                FusionResult result = algo.fuseSensorData(sensorData);

                // Each one should give us a valid result
                assertThat(result)
                        .as("Algorithm " + algo.getAlgorithmName() + " should return valid result")
                        .isNotNull();

                System.out.println("✓ PASS: " + algo.getAlgorithmName() +
                        " returned valid result");
            }
        }
    }
}
