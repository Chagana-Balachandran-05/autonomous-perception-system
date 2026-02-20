package com.moderndaytech.perception;

import com.moderndaytech.perception.detection.DetectedObject;
import com.moderndaytech.perception.detection.ObjectDetectionEngine;
import com.moderndaytech.perception.fusion.FusionAlgorithm;
import com.moderndaytech.perception.fusion.FusionResult;
import com.moderndaytech.perception.fusion.KalmanFilterFusion;
import com.moderndaytech.perception.fusion.ParticleFilterFusion;
import com.moderndaytech.perception.fusion.SensorFusionProcessor;
import com.moderndaytech.perception.sensor.CameraSensorData;
import com.moderndaytech.perception.sensor.LiDARSensorData;
import com.moderndaytech.perception.sensor.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class AutonomousPerceptionSystem {
    private static final Logger logger = LoggerFactory.getLogger(AutonomousPerceptionSystem.class);

    public static void main(String[] args) {
        logger.info("=== ModernDayTech Autonomous Perception System Starting ===");

        // Create sample sensor data
        float[] xs = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        float[] ys = {0.5f, 1.0f, 1.5f, 2.0f, 2.5f};
        float[] zs = {0.0f, 0.0f, 0.1f, 0.1f, 0.0f};
        float[] intensity = {0.8f, 0.9f, 0.7f, 0.85f, 0.75f};

        LiDARSensorData lidar = new LiDARSensorData(
            System.currentTimeMillis(), "LIDAR-01", xs, ys, zs, intensity);
        CameraSensorData camera = new CameraSensorData(
            System.currentTimeMillis(), "CAM-01", new byte[1920 * 1080 * 3], 1920, 1080);

        List<SensorData> sensors = Arrays.asList(lidar, camera);

        // DIP + Polymorphism: algorithm selected at runtime based on sensor count
        // With 2+ sensors: Kalman is accurate and fast
        // With 1 sensor: Particle handles higher uncertainty
        boolean highUncertainty = sensors.size() < 2;
        FusionAlgorithm algorithm = highUncertainty
            ? new ParticleFilterFusion()
            : new KalmanFilterFusion();

        logger.info("Selected algorithm: "
            + sanitizeForLog(algorithm.getName())
            + " (highUncertainty="
            + highUncertainty
            + ")");

        // Single processor — algorithm injected, same call regardless of which algorithm runs
        SensorFusionProcessor processor = new SensorFusionProcessor(algorithm);
        ObjectDetectionEngine detectionEngine = new ObjectDetectionEngine();

        // Process
        long start = System.currentTimeMillis();
        FusionResult fusionResult = processor.processSensors(sensors);
        List<DetectedObject> detected = detectionEngine.detectObjects(lidar);
        long elapsed = System.currentTimeMillis() - start;

        PerceptionResult result = new PerceptionResult(fusionResult, detected, elapsed);
        logger.info("Perception complete: "
            + result.getObjectCount()
            + " objects detected in "
            + result.getProcessingTimeMs()
            + "ms using "
            + sanitizeForLog(fusionResult.getAlgorithmUsed()));
    }

    private static String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\\r\\n\\t]", "_")
                .substring(0, Math.min(input.length(), 100));
    }
}