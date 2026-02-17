package com.moderndaytech.perception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.moderndaytech.perception.detection.ObjectDetectionEngine;
import com.moderndaytech.perception.detection.DetectedObject;
import com.moderndaytech.perception.fusion.FusionAlgorithm;
import com.moderndaytech.perception.fusion.SensorFusionProcessor;
import com.moderndaytech.perception.fusion.FusionResult;
import com.moderndaytech.perception.fusion.KalmanFilterFusion;
import com.moderndaytech.perception.fusion.ParticleFilterFusion;
import com.moderndaytech.perception.security.SecurityValidator;
import com.moderndaytech.perception.sensor.CameraSensorData;
import com.moderndaytech.perception.sensor.LiDARSensorData;
import com.moderndaytech.perception.sensor.SensorData;

/**
 * The main class for the Autonomous Perception System.
 * Shows off all four OOP pillars and several SOLID principles in action.
 *
 * SOLID Principles:
 * - SRP: Each class does one thing
 * - OCP: Easy to extend with new algorithms
 * - LSP: Sensor subclasses can be swapped
 * - ISP: Interfaces are focused
 * - DIP: Depends on abstractions
 *
 * OOP Pillars:
 * - Abstraction: SensorData is abstract
 * - Encapsulation: Fields are private
 * - Inheritance: LiDAR/Camera extend SensorData
 * - Polymorphism: Multiple FusionAlgorithm implementations
 */
public class AutonomousPerceptionSystem {
    private static final Logger logger = LoggerFactory.getLogger(AutonomousPerceptionSystem.class);

    private final SensorFusionProcessor fusionProcessor;
    private final ObjectDetectionEngine detectionEngine;

    /**
     * Constructor with Dependency Injection (DIP principle)
     */
    public AutonomousPerceptionSystem(SensorFusionProcessor fusionProcessor,
            ObjectDetectionEngine detectionEngine) {
        this.fusionProcessor = fusionProcessor;
        this.detectionEngine = detectionEngine;
    }

    /**
     * Process perception frame - main application logic
     * Demonstrates SRP: Single responsibility for orchestrating perception pipeline
     */
    public PerceptionResult processPerceptionFrame(List<SensorData> sensorDataList) {
        logger.info("=== Processing Perception Frame ===");
        logger.info("Input: {} sensors", sensorDataList.size());

        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Validate sensor data (Security-first approach)
            validateSensorData(sensorDataList);

            // Step 2: Sensor fusion (Demonstrates Polymorphism)
            FusionResult fusionResult = fusionProcessor.processSensors(sensorDataList);
            logger.info("Fusion completed: {}", fusionResult);

            // Step 3: Object detection
            List<DetectedObject> detectedObjects = detectionEngine.detectObjects(fusionResult);
            logger.info("Detection completed: {} objects found", detectedObjects.size());

            // Step 4: Generate result
            long processingTime = System.currentTimeMillis() - startTime;

            PerceptionResult result = new PerceptionResult(
                    detectedObjects,
                    fusionResult,
                    processingTime,
                    true,
                    null);

            logger.info("=== Perception Processing Complete ===");
            logger.info("Total time: {}ms", processingTime);
            logger.info("Objects detected: {}", detectedObjects.size());

            return result;

        } catch (SecurityException e) {
            // For security risks, re-throw so the automated security test detects the attack
            logger.error("SECURITY ALERT: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // For general errors, return a failure result object
            logger.error("Simulation error encountered: {}", e.getMessage());
            return PerceptionResult.failure(e.getMessage()); // This now matches Step 1
        }
    }

    /**
     * Validate sensor data with security checks
     */
    private void validateSensorData(List<SensorData> sensorDataList) {
        if (sensorDataList == null || sensorDataList.isEmpty()) {
            throw new IllegalArgumentException("Sensor data list cannot be null or empty");
        }

        for (SensorData sensor : sensorDataList) {
            // Security validation (now throws exceptions)
            SecurityValidator validator = new SecurityValidator();
            validator.validateSensorId(sensor.getSensorId());
            validator.validateDataSize(sensor.getDataSize());
        }
    }

    /**
     * Main method - demonstrates the complete system
     */
    public static void main(String[] args) {
        logger.info("╔════════════════════════════════════════════════════════╗");
        logger.info("║   Autonomous Perception System - ONCE Dataset         ║");
        logger.info("║   Advanced Programming Assignment - Tasks 3 & 4       ║");
        logger.info("╚════════════════════════════════════════════════════════╝");

        // Create fusion algorithm (Polymorphism - choose one implementation)
        FusionAlgorithm algorithm = new KalmanFilterFusion();

        // Initialize system components (Dependency Injection - DIP)
        SensorFusionProcessor fusionProcessor = new SensorFusionProcessor(algorithm);
        ObjectDetectionEngine detectionEngine = new ObjectDetectionEngine();
        AutonomousPerceptionSystem system = new AutonomousPerceptionSystem(
            fusionProcessor,
            detectionEngine);

        // Create sensor data (Demonstrates Inheritance and Encapsulation)
        logger.info("\n--- Creating Sensor Data ---");
        List<SensorData> sensorData = new ArrayList<>();

        // LiDAR sensor (Inheritance from SensorData)
        LiDARSensorData lidar = LiDARSensorData.createRealisticData("LIDAR_FRONT");
        sensorData.add(lidar);
        logger.info("Created: {}", lidar);
        logger.info("  {}", lidar.getMetricsReport());

        // Camera sensor (Encapsulation - private fields)
        CameraSensorData camera = CameraSensorData.createRealisticData("CAMERA_FRONT");
        sensorData.add(camera);
        logger.info("Created: {}", camera);
        logger.info("  {}", camera.getMetricsReport());

        // Process perception frame
        logger.info("\n--- Processing Perception Frame ---");
        PerceptionResult result = system.processPerceptionFrame(sensorData);

        // Display results
        logger.info("\n--- Perception Results ---");
        logger.info("Success: {}", result.isSuccess());
        logger.info("Processing Time: {}ms", result.getProcessingTimeMs());
        logger.info("Objects Detected: {}", result.getDetectedObjects().size());

        if (result.isSuccess()) {
            logger.info("\n--- Detected Objects ---");
            for (DetectedObject obj : result.getDetectedObjects()) {
                logger.info("  {}", obj);
            }
        }

        logger.info("\n╔════════════════════════════════════════════════════════╗");
        logger.info("║   Perception System Demo Complete                     ║");
        logger.info("╚════════════════════════════════════════════════════════╝");
    }
}