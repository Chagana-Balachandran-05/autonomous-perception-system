package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class SensorFusionProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SensorFusionProcessor.class);
    private static final long SYNC_WINDOW_MS = 50;

    private final FusionAlgorithm algorithm; // depends on ABSTRACTION not implementation

    // Constructor injection - DIP in action
    public SensorFusionProcessor(FusionAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public FusionResult processSensors(List<SensorData> sensors) {
        if (sensors == null) {
            throw new IllegalArgumentException("Sensor list cannot be null or empty");
        }
        if (sensors.isEmpty()) {
            throw new IllegalArgumentException("Sensor list cannot be null or empty");
        }

        logger.info("Starting fusion with {} sensors using {}",
            sensors.size(), algorithm.getName());
        try {
                FusionResult result = algorithm.fuse(sensors); // polymorphic call
            logger.info("Fusion completed: confidence={}", result.getConfidence());
            return result;
        } catch (Exception e) {
            logger.error("Fusion failed: {}", e.getMessage(), e);
            return FusionResult.empty();
        }
    }

    public String getCurrentAlgorithmName() {
        return algorithm.getName();
    }
}