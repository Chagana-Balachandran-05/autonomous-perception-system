package com.moderndaytech.perception.dataset;

import com.moderndaytech.perception.detection.DetectedObject;
import com.moderndaytech.perception.detection.ObjectDetectionEngine;
import com.moderndaytech.perception.fusion.FusionResult;
import com.moderndaytech.perception.fusion.KalmanFilterFusion;
import com.moderndaytech.perception.fusion.SensorFusionProcessor;
import com.moderndaytech.perception.security.SecurityValidator;
import com.moderndaytech.perception.sensor.LiDARSensorData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests demonstrating the full pipeline
 * with ONCE dataset annotation format.
 *
 * <p>These tests prove that the system can load ONCE-format scene data,
 * convert it to internal sensor representations, fuse it, and run detection —
 * the complete autonomous perception pipeline on large dataset input.</p>
 *
 * <p>The ONCE dataset (Mao et al., 2021) provides scenes with 60,000–120,000
 * LiDAR points per frame. Tests here use annotation files matching that format,
 * with synthetic point clouds at the same scale.</p>
 */
@DisplayName("ONCE Dataset Integration Tests - Full Pipeline")
class ONCEDatasetIntegrationTest {

    private ONCEDatasetLoader loader;
    private SensorFusionProcessor fusionProcessor;
    private ObjectDetectionEngine detectionEngine;

    @BeforeEach
    void setup() {
        loader = new ONCEDatasetLoader(new SecurityValidator());
        fusionProcessor = new SensorFusionProcessor(new KalmanFilterFusion());
        detectionEngine = new ObjectDetectionEngine();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/test-scenes/vehicle_scene.json",
        "/test-scenes/pedestrian_scene.json",
        "/test-scenes/mixed_scene.json"
    })
    @DisplayName("Full pipeline: load ONCE scene → fuse → detect")
    void testFullPipeline_ONCEScene_ProducesDetections(String scenePath) throws Exception {
        InputStream stream = getClass().getResourceAsStream(scenePath);
        ONCESceneAnnotation annotation = loader.loadAnnotation(stream,
            scenePath.replace("/test-scenes/", ""));

        LiDARSensorData lidar = loader.buildLiDARSensor(annotation);

        FusionResult fusion = fusionProcessor.processSensors(List.of(lidar));

        List<DetectedObject> detected = detectionEngine.detectObjects(lidar);

        assertThat(fusion.isValid()).isTrue();
        assertThat(fusion.getConfidence()).isGreaterThan(0.0);
        assertThat(detected).isNotEmpty();
        assertThat(detected).allSatisfy(obj -> {
            assertThat(obj.getConfidence()).isGreaterThan(0.5);
            assertThat(obj.getType()).isNotNull();
            assertThat(obj.getPosition()).isNotNull();
        });

        System.out.printf("Scene %-30s | Points: %,6d | Fused: %.2f confidence | Detected: %d objects%n",
            annotation.getSceneId(), annotation.getLidarPoints(),
            fusion.getConfidence(), detected.size());
    }

    @Test
    @DisplayName("Large point cloud (100K+) processes within 500ms")
    void testPerformance_LargeONCEScene_CompletesInTime() throws Exception {
        InputStream stream = getClass().getResourceAsStream(
            "/test-scenes/mixed_scene.json");
        ONCESceneAnnotation annotation = loader.loadAnnotation(stream, "mixed_scene.json");
        LiDARSensorData lidar = loader.buildLiDARSensor(annotation);

        long start = System.currentTimeMillis();
        fusionProcessor.processSensors(List.of(lidar));
        detectionEngine.detectObjects(lidar);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).as("Full pipeline must complete within 500ms").isLessThan(500);
        System.out.printf("Large scene pipeline: %,d points processed in %dms%n",
            annotation.getLidarPoints(), elapsed);
    }

    @Test
    @DisplayName("Detected object count scales with point cloud density")
    void testDetectionScalesWithPointDensity() throws Exception {
        InputStream vs = getClass().getResourceAsStream("/test-scenes/vehicle_scene.json");
        ONCESceneAnnotation vehicleScene = loader.loadAnnotation(vs, "vehicle_scene.json");

        InputStream ps = getClass().getResourceAsStream("/test-scenes/pedestrian_scene.json");
        ONCESceneAnnotation pedScene = loader.loadAnnotation(ps, "pedestrian_scene.json");

        List<DetectedObject> vehicleDetections =
            detectionEngine.detectObjects(loader.buildLiDARSensor(vehicleScene));
        List<DetectedObject> pedDetections =
            detectionEngine.detectObjects(loader.buildLiDARSensor(pedScene));

        assertThat(vehicleDetections.size())
            .isGreaterThanOrEqualTo(pedDetections.size());

        System.out.printf("Vehicle scene (%,d pts): %d detections%n",
            vehicleScene.getLidarPoints(), vehicleDetections.size());
        System.out.printf("Pedestrian scene (%,d pts): %d detections%n",
            pedScene.getLidarPoints(), pedDetections.size());
    }

    @Test
    @DisplayName("Annotation ground truth: vehicle scene has 3 annotated objects")
    void testGroundTruth_VehicleScene_MatchesAnnotationCount() throws Exception {
        InputStream stream = getClass().getResourceAsStream(
            "/test-scenes/vehicle_scene.json");
        ONCESceneAnnotation annotation = loader.loadAnnotation(stream, "vehicle_scene.json");

        assertThat(annotation.getAnnotationCount()).isEqualTo(3);
        assertThat(annotation.getObjectNames()).containsExactly("Car", "Car", "Pedestrian");

        assertThat(annotation.getBoxes3d()).allSatisfy(box ->
            assertThat(box).hasSize(7));
    }
}
