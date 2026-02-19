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

        // DIP: inject concrete algorithm into processor
        FusionAlgorithm algorithm = new KalmanFilterFusion();
        SensorFusionProcessor processor = new SensorFusionProcessor(algorithm);
        ObjectDetectionEngine detectionEngine = new ObjectDetectionEngine();

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

        // Process
        long start = System.currentTimeMillis();
        FusionResult fusionResult = processor.processSensors(sensors);
        List<DetectedObject> detected = detectionEngine.detectObjects(lidar);
        long elapsed = System.currentTimeMillis() - start;

        PerceptionResult result = new PerceptionResult(fusionResult, detected, elapsed);
        logger.info("Perception complete: {} objects detected in {}ms",
            result.getObjectCount(), result.getProcessingTimeMs());

        // Swap to particle filter (DIP and polymorphism demo)
        FusionAlgorithm particleAlgorithm = new ParticleFilterFusion();
        SensorFusionProcessor particleProcessor = new SensorFusionProcessor(particleAlgorithm);
        FusionResult particleResult = particleProcessor.processSensors(sensors);
        logger.info("Particle filter result: confidence={}", particleResult.getConfidence());
    }
}