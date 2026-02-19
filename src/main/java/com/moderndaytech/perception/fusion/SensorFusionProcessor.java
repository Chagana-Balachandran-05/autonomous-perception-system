package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates multi-sensor data fusion using pluggable fusion algorithms.
 * Implements the Strategy pattern through dependency injection of FusionAlgorithm implementations.
 *
 * <h2>SOLID Principles Applied:</h2>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP)</strong>: Only coordinates fusion workflow.
 *       Does NOT implement fusion math (delegated to FusionAlgorithm), does NOT validate security,
 *       and does NOT detect objects.</li>
 *   <li><strong>Open-Closed Principle (OCP)</strong>: New fusion algorithms can be added without
 *       modifying this class - create a new FusionAlgorithm implementation and inject it.</li>
 *   <li><strong>Liskov Substitution Principle (LSP)</strong>: Any FusionAlgorithm implementation
 *       works identically through this processor.</li>
 *   <li><strong>Dependency Inversion Principle (DIP)</strong>: Depends on FusionAlgorithm
 *       abstraction, not concrete implementations.</li>
 * </ul>
 *
 * <h2>Design Patterns:</h2>
 * <ul>
 *   <li><strong>Strategy Pattern</strong>: Fusion algorithm is injected and can be changed at runtime.</li>
 *   <li><strong>Dependency Injection</strong>: Constructor injection of FusionAlgorithm keeps behavior configurable.</li>
 *   <li><strong>Template Workflow</strong>: processSensors() defines a consistent orchestration sequence.</li>
 * </ul>
 *
 * <h2>Clean Code Techniques:</h2>
 * <ul>
 *   <li><strong>Guard Clauses</strong>: Early return on null or empty sensor lists.</li>
 *   <li><strong>Logging</strong>: SLF4J logging for normal flow and failure scenarios.</li>
 *   <li><strong>Single Level of Abstraction</strong>: High-level orchestration with delegated details.</li>
 * </ul>
 *
 * @author Chagana Balachandran
 * @version 1.0
 * @since 2026-02-10
 *
 * @see FusionAlgorithm
 * @see KalmanFilterFusion
 * @see ParticleFilterFusion
 */
public class SensorFusionProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SensorFusionProcessor.class);
    private static final long SYNC_WINDOW_MS = 50;

    private final FusionAlgorithm algorithm;

    public SensorFusionProcessor(FusionAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public FusionResult processSensors(List<SensorData> sensors) {
        logger.info("Starting fusion with {} sensors using {}", sensors.size(), sanitizeForLog(algorithm.getName()));

        if (sensors == null || sensors.isEmpty()) {
            logger.warn("No sensors provided for fusion");
            return FusionResult.empty();
        }

        List<SensorData> synchronized_sensors = findSynchronizedSensors(
            sensors, sensors.get(0).getTimestamp());

        try {
            FusionResult result = algorithm.fuse(synchronized_sensors);
            logger.info("Fusion completed: confidence={}", result.getConfidence());
            return result;
        } catch (Exception e) {
            String safeMsg = sanitizeForLog(e.getMessage());
            logger.error("Fusion failed: {}", safeMsg);
            throw new RuntimeException("Fusion failure", e);
        }
    }

    private List<SensorData> findSynchronizedSensors(List<SensorData> sensors, long targetTime) {
        List<SensorData> synced = new ArrayList<>();
        for (SensorData sensor : sensors) {
            long timeDiff = Math.abs(sensor.getTimestamp() - targetTime);
            if (timeDiff <= SYNC_WINDOW_MS) {
                synced.add(sensor);
            }
        }
        return synced.isEmpty() ? sensors : synced;
    }

    private String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\r\n\t]", "_").substring(0, Math.min(input.length(), 100));
    }
}