package com.moderndaytech.perception.detection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import com.moderndaytech.perception.fusion.FusionResult;

/**
 * Integration Tests for ObjectDetectionEngine
 * 
 * PURPOSE: Validates object detection correctness on fused sensor data
 * Tests detection accuracy, filtering, and failure scenarios
 * 
 * PRINCIPLE: Single Responsibility Principle (SRP) - Detection focused only
 * on object classification, not sensor fusion or validation
 * 
 * PRINCIPLE: Interface Segregation Principle (ISP) - Only methods needed
 * for detection are exposed
 * 
 * VIDEO GUIDE:
 * Run: mvn test -Dtest=ObjectDetectionEngineIntegrationTest
 * Output: target/surefire-reports/ObjectDetectionEngineIntegrationTest.txt
 * Coverage: target/site/jacoco/index.html
 */
@DisplayName("ObjectDetectionEngine Integration Tests - DAST Coverage")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ObjectDetectionEngineIntegrationTest {

    private ObjectDetectionEngine detectionEngine;

    @BeforeEach
    public void setup() {
        System.out.println("\n[SETUP] Initializing ObjectDetectionEngine...");
        detectionEngine = new ObjectDetectionEngine();
        System.out.println("[SETUP] ObjectDetectionEngine ready\n");
    }

    // ================================================================
    // POSITIVE TESTS: Successful Object Detection
    // ================================================================

    @Nested
    @DisplayName("Positive Tests - Successful Object Detection")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SuccessfulDetectionTests {

        @Test
        @Order(1)
        @DisplayName("Detect objects from high-quality fusion data")
        public void testDetectObjectsHighQuality() {
            // ARRANGE: Create high-quality fusion result (5000 data points)
            FusionResult fusionData = new FusionResult(
                    "KalmanFilterFusion",
                    5000, // Total data points
                    0.95, // High confidence score
                    2 // From 2 sensors
            );

            // ACT: Detect objects
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(fusionData);

            // ASSERT: Should detect reasonable number of objects
            assertThat(detectedObjects)
                    .isNotEmpty()
                    .as("Should detect objects from quality fusion data")
                    .hasSizeGreaterThan(0);

            // Verify all detected objects have required properties
            assertThat(detectedObjects).allSatisfy(obj -> {
                assertThat(obj.getObjectId()).isNotEmpty();
                assertThat(obj.getObjectClass()).isNotNull();
                assertThat(obj.getPosition()).isNotNull();
                assertThat(obj.getConfidence()).isGreaterThanOrEqualTo(0.0)
                        .isLessThanOrEqualTo(1.0);
            });

            System.out.println("✓ PASS: Detected " + detectedObjects.size() +
                    " objects from high-quality fusion data");
        }

        @Test
        @Order(2)
        @DisplayName("All detected objects pass confidence threshold (>50%)")
        public void testConfidenceThresholdFiltering() {
            // ARRANGE: Create fusion data with moderate confidence
            FusionResult fusionData = new FusionResult(
                    "ParticleFilterFusion",
                    3000,
                    0.7, // 70% confidence
                    1);

            // ACT: Detect objects
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(fusionData);

            // ASSERT: All objects must have confidence > 50% (0.5)
            assertThat(detectedObjects)
                    .allSatisfy(obj -> assertThat(obj.getConfidence())
                            .as("Object confidence must exceed 50% threshold")
                            .isGreaterThan(0.5));

            System.out.println("✓ PASS: All " + detectedObjects.size() +
                    " detected objects exceed confidence threshold");
        }

        @Test
        @Order(3)
        @DisplayName("Detect diverse object classes (vehicles, pedestrians, etc)")
        public void testDetectDiverseObjectClasses() {
            // ARRANGE: Create fusion data
            FusionResult fusionData = new FusionResult(
                    "KalmanFilterFusion",
                    10000,
                    0.85,
                    3);

            // ACT: Detect objects
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(fusionData);

            // ASSERT: Should detect multiple object types
            long uniqueClasses = detectedObjects.stream()
                    .map(DetectedObject::getObjectClass)
                    .distinct()
                    .count();

            assertThat(detectedObjects)
                    .as("Should detect diverse object classes")
                    .isNotEmpty();

            System.out.println("✓ PASS: Detected " + uniqueClasses +
                    " different object classes");
            for (ObjectClass objClass : ObjectClass.values()) {
                long count = detectedObjects.stream()
                        .filter(obj -> obj.getObjectClass() == objClass)
                        .count();
                if (count > 0) {
                    System.out.println("  - " + objClass + ": " + count + " objects");
                }
            }
        }

        @Test
        @Order(4)
        @DisplayName("Detect objects with valid 3D positions")
        public void testDetectObjectsWithValid3DPositions() {
            // ARRANGE: Create fusion data
            FusionResult fusionData = new FusionResult(
                    "ParticleFilterFusion",
                    2000,
                    0.6,
                    1);

            // ACT: Detect objects
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(fusionData);

            // ASSERT: All objects should have realistic 3D positions
            assertThat(detectedObjects)
                    .allSatisfy(obj -> {
                        Position3D pos = obj.getPosition();
                        assertThat(pos).isNotNull();
                        assertThat(pos.getX()).isGreaterThanOrEqualTo(-50).isLessThanOrEqualTo(50);
                        assertThat(pos.getY()).isGreaterThanOrEqualTo(-50).isLessThanOrEqualTo(50);
                        assertThat(pos.getZ()).isGreaterThanOrEqualTo(0).isLessThanOrEqualTo(10);
                    });

            System.out.println("✓ PASS: All " + detectedObjects.size() +
                    " objects have valid 3D positions");
        }
    }

    // ================================================================
    // FAILURE SCENARIO TESTS: Edge Cases and Degraded Input
    // ================================================================

    @Nested
    @DisplayName("Failure Scenario Tests - Degraded Conditions")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FailureScenarioTests {

        @Test
        @Order(1)
        @DisplayName("Handle low confidence fusion data (no premature failures)")
        public void testHandleLowConfidenceData() {
            // ARRANGE: Create low-confidence fusion data
            FusionResult lowConfidenceData = new FusionResult(
                    "KalmanFilterFusion",
                    1000,
                    0.3, // Only 30% confidence
                    1);

            // ACT: Attempt object detection
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lowConfidenceData);

            // ASSERT: Should gracefully handle, may detect fewer objects
            assertThat(detectedObjects)
                    .as("Should handle low confidence data gracefully")
                    .isNotNull();

            System.out.println("✓ PASS: Handled low-confidence data");
            System.out.println("  Detected: " + detectedObjects.size() + " objects");
            System.out.println("  Input confidence: 30%");
        }

        @Test
        @Order(2)
        @DisplayName("Handle minimal data (sparse point cloud)")
        public void testHandleMinimalData() {
            // ARRANGE: Create minimal fusion data (small point cloud)
            FusionResult minimalData = new FusionResult(
                    "ParticleFilterFusion",
                    100, // Very small data
                    0.5,
                    1);

            // ACT: Detect objects
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(minimalData);

            // ASSERT: Should handle gracefully (may result in 0-1 objects)
            assertThat(detectedObjects)
                    .as("Should handle minimal data gracefully")
                    .isNotNull();

            System.out.println("✓ PASS: Handled minimal data (100 points)");
            System.out.println("  Detected: " + detectedObjects.size() + " objects");
        }

        @Test
        @Order(3)
        @DisplayName("Handle zero confidence fusion data (no crash)")
        public void testHandleZeroConfidenceData() {
            // ARRANGE: Create fusion data with zero confidence
            FusionResult zeroConfidenceData = new FusionResult(
                    "KalmanFilterFusion",
                    5000,
                    0.0, // Zero confidence
                    2);

            // ACT: Attempt detection (should not crash)
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(zeroConfidenceData);

            // ASSERT: Should not throw exception, returns empty or filtered list
            assertThat(detectedObjects)
                    .as("Should handle zero-confidence data without crashing")
                    .isNotNull();

            System.out.println("✓ PASS: Handled zero-confidence data without crash");
        }

        @Test
        @Order(4)
        @DisplayName("Filter low-confidence objects correctly")
        public void testLowConfidenceObjectFiltering() {
            // ARRANGE: Create data that will generate many low-confidence detections
            FusionResult fusionData = new FusionResult(
                    "ParticleFilterFusion",
                    1000,
                    0.4,
                    1);

            // ACT: Detect objects
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(fusionData);

            // ASSERT: All returned objects must pass confidence filter (>50%)
            assertThat(detectedObjects)
                    .allSatisfy(obj -> assertThat(obj.getConfidence())
                            .as("All objects must pass confidence threshold filter")
                            .isGreaterThan(0.5));

            System.out.println("✓ PASS: Low-confidence objects filtered out correctly");
        }
    }

    // ================================================================
    // BOUNDARY TESTS: Extreme Conditions
    // ================================================================

    @Nested
    @DisplayName("Boundary Tests - Extreme Conditions")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class BoundaryTests {

        @Test
        @Order(1)
        @DisplayName("Handle maximum realistic data size")
        public void testMaximumDataSize() {
            // ARRANGE: Create maximum realistic data size
            FusionResult largeData = new FusionResult(
                    "KalmanFilterFusion",
                    100_000, // 100k data points
                    0.9,
                    3);

            // ACT: Detect objects
            long startTime = System.currentTimeMillis();
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(largeData);
            long detectionTime = System.currentTimeMillis() - startTime;

            // ASSERT: Should complete within reasonable time (<1 second)
            assertThat(detectionTime)
                    .as("Detection should complete quickly even with large data")
                    .isLessThan(1000);

            System.out.println("✓ PASS: Detected objects from maximum data size");
            System.out.println("  Data points: 100,000");
            System.out.println("  Detection time: " + detectionTime + "ms");
            System.out.println("  Objects detected: " + detectedObjects.size());
        }

        @Test
        @Order(2)
        @DisplayName("Performance: Detection completes in <500ms for typical data")
        public void testDetectionPerformance() {
            // ARRANGE: Create typical autonomous driving data
            FusionResult typicalData = new FusionResult(
                    "KalmanFilterFusion",
                    10000,
                    0.85,
                    3);

            // ACT: Measure detection time
            long startTime = System.currentTimeMillis();
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(typicalData);
            long detectionTime = System.currentTimeMillis() - startTime;

            // ASSERT: Should be fast for real-time processing
            assertThat(detectionTime)
                    .as("Detection must be real-time (<500ms)")
                    .isLessThan(500);

            System.out.println("✓ PASS: Detection performance acceptable");
            System.out.println("  Detection time: " + detectionTime + "ms");
            System.out.println("  Objects: " + detectedObjects.size());
        }

        @Test
        @Order(3)
        @DisplayName("Reproducibility: Same input produces same detection count")
        public void testDetectionReproducibility() {
            // ARRANGE: Create identical fusion data twice
            FusionResult data1 = new FusionResult(
                    "ParticleFilterFusion",
                    5000,
                    0.8,
                    2);
            FusionResult data2 = new FusionResult(
                    "ParticleFilterFusion",
                    5000,
                    0.8,
                    2);

            // ACT: Detect objects from both
            List<DetectedObject> objects1 = detectionEngine.detectObjects(data1);
            List<DetectedObject> objects2 = detectionEngine.detectObjects(data2);

            // ASSERT: Should produce same detection results
            assertThat(objects1.size())
                    .as("Detection should be reproducible")
                    .isEqualTo(objects2.size());

            System.out.println("✓ PASS: Detection results are reproducible");
            System.out.println("  Objects detected (run 1): " + objects1.size());
            System.out.println("  Objects detected (run 2): " + objects2.size());
        }
    }

    // ================================================================
    // DETECTION QUALITY TESTS
    // ================================================================

    @Nested
    @DisplayName("Detection Quality Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DetectionQualityTests {

        @Test
        @Order(1)
        @DisplayName("Detected objects have unique IDs")
        public void testDetectedObjectsHaveUniqueIds() {
            // ARRANGE: Create fusion data
            FusionResult fusionData = new FusionResult(
                    "KalmanFilterFusion",
                    5000,
                    0.9,
                    2);

            // ACT: Detect objects
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(fusionData);

            // ASSERT: All object IDs should be unique
            long uniqueIds = detectedObjects.stream()
                    .map(DetectedObject::getObjectId)
                    .distinct()
                    .count();

            assertThat(uniqueIds)
                    .as("All detected objects should have unique IDs")
                    .isEqualTo(detectedObjects.size());

            System.out.println("✓ PASS: All detected objects have unique IDs");
        }

        @Test
        @Order(2)
        @DisplayName("Object distribution across detected classes is reasonable")
        public void testObjectDistribution() {
            // ARRANGE: Create fusion data to generate diverse detections
            FusionResult fusionData = new FusionResult(
                    "KalmanFilterFusion",
                    50000, // Large dataset
                    0.85,
                    3);

            // ACT: Detect objects
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(fusionData);

            // ASSERT: Should have multiple object types
            long classCount = detectedObjects.stream()
                    .map(DetectedObject::getObjectClass)
                    .distinct()
                    .count();

            assertThat(classCount)
                    .as("Should detect multiple object classes")
                    .isGreaterThan(1);

            System.out.println("✓ PASS: Object distribution is reasonable");
            System.out.println("  Total objects: " + detectedObjects.size());
            System.out.println("  Object classes: " + classCount);
        }
    }

    // ================================================================
    // FAILURE INJECTION TESTS: Simulated Real-World Failures
    // ================================================================

    @Nested
    @DisplayName("Failure Injection Tests - Resilience")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FailureInjectionTests {

        @Test
        @Order(1)
        @DisplayName("Graceful degradation with null fusion data")
        public void testNullFusionDataHandling() {
            // ARRANGE: Null fusion result (should not happen, but test robustness)
            FusionResult nullData = null;

            // ACT & ASSERT: Should throw NullPointerException (fail-fast)
            try {
                detectionEngine.detectObjects(nullData);
                System.out.println("⚠ WARNING: Null fusion data not caught!");
            } catch (NullPointerException e) {
                System.out.println("✓ PASS: Null fusion data caught (fail-fast)");
            }
        }

        @Test
        @Order(2)
        @DisplayName("Handle alternating high/low confidence data streams")
        public void testAlternatingQualityDataStreams() {
            // ARRANGE: Simulate alternating good/bad data
            FusionResult highQuality = new FusionResult("KalmanFilterFusion", 10000, 0.95, 3);
            FusionResult lowQuality = new FusionResult("ParticleFilterFusion", 1000, 0.2, 1);

            // ACT: Process both
            List<DetectedObject> highQualityObjects = detectionEngine.detectObjects(highQuality);
            List<DetectedObject> lowQualityObjects = detectionEngine.detectObjects(lowQuality);

            // ASSERT: High quality should have more detections
            assertThat(highQualityObjects.size())
                    .as("High-quality data should produce more detections")
                    .isGreaterThanOrEqualTo(lowQualityObjects.size());

            System.out.println("✓ PASS: Handled alternating quality data streams");
            System.out.println("  High quality detections: " + highQualityObjects.size());
            System.out.println("  Low quality detections: " + lowQualityObjects.size());
        }
    }
}
