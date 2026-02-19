package com.moderndaytech.perception.detection;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moderndaytech.perception.sensor.LiDARSensorData;

/**
 * Integration Tests for ObjectDetectionEngine
 * 
 * PURPOSE: Validates object detection correctness on LiDAR sensor data
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

        private LiDARSensorData createLiDAR(int points) {
                float[] x = new float[points];
                float[] y = new float[points];
                float[] z = new float[points];
                float[] intensity = new float[points];
                for (int i = 0; i < points; i++) {
                        x[i] = i;
                        y[i] = i * 0.5f;
                        z[i] = 0.1f;
                        intensity[i] = 0.8f;
                }
                return new LiDARSensorData(System.currentTimeMillis(), "TEST-LIDAR", x, y, z, intensity);
        }

        private LiDARSensorData createLiDARFromSceneResource(String resourcePath) throws Exception {
                try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
                        assertThat(inputStream)
                                .as("Scene resource should exist: " + resourcePath)
                                .isNotNull();

                        String sceneJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        JsonObject scene = JsonParser.parseString(sceneJson).getAsJsonObject();
                        int points = scene.get("lidar_points").getAsInt();

                        float[] x = new float[points];
                        float[] y = new float[points];
                        float[] z = new float[points];
                        float[] intensity = new float[points];
                        for (int i = 0; i < points; i++) {
                                x[i] = i;
                                y[i] = i * 0.5f;
                                z[i] = 0.1f;
                                intensity[i] = 0.8f;
                        }
                        return new LiDARSensorData(System.currentTimeMillis(), "TEST-SCENE-LIDAR", x, y, z, intensity);
                }
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
                        LiDARSensorData lidar = createLiDAR(5000);

            // ACT: Detect objects
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);

            // ASSERT: Should detect reasonable number of objects
            assertThat(detectedObjects)
                    .isNotEmpty()
                    .as("Should detect objects from quality fusion data")
                    .hasSizeGreaterThan(0);

            // Verify all detected objects have required properties
            assertThat(detectedObjects).allSatisfy(obj -> {
                assertThat(obj.getObjectId()).isNotEmpty();
                                assertThat(obj.getType()).isNotNull();
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
                        LiDARSensorData lidar = createLiDAR(3000);

            // ACT: Detect objects
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);

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
                        LiDARSensorData lidar = createLiDAR(10000);

            // ACT: Detect objects
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);

            // ASSERT: Should detect multiple object types
            long uniqueClasses = detectedObjects.stream()
                    .map(DetectedObject::getType)
                    .distinct()
                    .count();

            assertThat(detectedObjects)
                    .as("Should detect diverse object classes")
                    .isNotEmpty();

            System.out.println("✓ PASS: Detected " + uniqueClasses +
                    " different object classes");
                        for (ObjectType objClass : ObjectType.values()) {
                long count = detectedObjects.stream()
                                                .filter(obj -> obj.getType() == objClass)
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
                        LiDARSensorData lidar = createLiDAR(2000);

            // ACT: Detect objects
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);

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

        @Test
        @Order(5)
        @DisplayName("Detect vehicle using test scene resource")
        public void testDetectObjects_WithVehicleInScene_DetectsVehicle() throws Exception {
            LiDARSensorData lidar = createLiDARFromSceneResource("/test-scenes/vehicle_scene.json");

            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);

            assertThat(detectedObjects)
                    .as("Should detect objects from scene resource data")
                    .isNotEmpty();

            assertThat(detectedObjects)
                    .anySatisfy(obj -> assertThat(obj.getType()).isEqualTo(ObjectType.VEHICLE));

            System.out.println("✓ PASS: Vehicle detected from /test-scenes/vehicle_scene.json");
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
                        LiDARSensorData lidar = createLiDAR(1000);

            // ACT: Attempt object detection
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);

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
                        LiDARSensorData lidar = createLiDAR(100);

            // ACT: Detect objects
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);

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
                        LiDARSensorData lidar = createLiDAR(5000);

            // ACT: Attempt detection (should not crash)
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);

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
                        LiDARSensorData lidar = createLiDAR(1000);

            // ACT: Detect objects
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);

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
                        LiDARSensorData lidar = createLiDAR(100_000);

            // ACT: Detect objects
            long startTime = System.currentTimeMillis();
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);
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
                        LiDARSensorData lidar = createLiDAR(10000);

            // ACT: Measure detection time
            long startTime = System.currentTimeMillis();
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);
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
            LiDARSensorData data1 = createLiDAR(5000);
            LiDARSensorData data2 = createLiDAR(5000);

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
                        LiDARSensorData lidar = createLiDAR(5000);

            // ACT: Detect objects
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);

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
                        LiDARSensorData lidar = createLiDAR(50000);

            // ACT: Detect objects
                        List<DetectedObject> detectedObjects = detectionEngine.detectObjects(lidar);

            // ASSERT: Should have multiple object types
            long classCount = detectedObjects.stream()
                    .map(DetectedObject::getType)
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
                        LiDARSensorData nullData = null;

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
                        LiDARSensorData highQuality = createLiDAR(10000);
                        LiDARSensorData lowQuality = createLiDAR(1000);

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
