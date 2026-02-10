package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

/**
 * Handles sensor fusion for the system.
 * Combines data from different sensors using the best available fusion algorithm.
 *
 * You can swap in different algorithms, and this class will use them—no need to change anything else. It's all about keeping things flexible and focused on fusing sensor data.
 */
public class SensorFusionProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SensorFusionProcessor.class);

    private final List<FusionAlgorithm> fusionAlgorithms;

    /**
     * Make a new fusion processor, giving it a list of algorithms it can use.
     */
    public SensorFusionProcessor(List<FusionAlgorithm> fusionAlgorithms) {
        this.fusionAlgorithms = fusionAlgorithms;
    }

    /**
     * Fuse the sensor data using whichever algorithm fits best.
     *
     * This method just picks the right algorithm and lets it do its thing.
     */
    public FusionResult fuseSensorData(List<SensorData> sensorDataList) {
        logger.info("Starting sensor fusion for {} sensors", sensorDataList.size());

        long startTime = System.currentTimeMillis();

        // First, make sure the input is good
        validateSensorData(sensorDataList);

        // Pick the best algorithm for the job
        FusionAlgorithm selectedAlgorithm = selectOptimalFusionAlgorithm(sensorDataList);

        logger.info("Selected algorithm: {}", selectedAlgorithm.getAlgorithmName());

        // Let the chosen algorithm handle the fusion
        FusionResult result = selectedAlgorithm.fuseSensorData(sensorDataList);

        long processingTime = System.currentTimeMillis() - startTime;

        logger.info("Sensor fusion completed in {}ms using {}",
            processingTime, selectedAlgorithm.getAlgorithmName());

        return result;
    }

    /**
     * Picks the best algorithm for the current sensor data, based on what’s available and what fits best.
     */
    private FusionAlgorithm selectOptimalFusionAlgorithm(List<SensorData> sensorDataList) {
        // This demonstrates polymorphism - we iterate through different
        // algorithm implementations and call their methods uniformly
        return fusionAlgorithms.stream()
                .filter(algorithm -> algorithm.isApplicable(sensorDataList))
                .max(Comparator.comparingDouble(algorithm -> algorithm.calculateConfidenceScore(sensorDataList)))
                .orElseThrow(() -> new IllegalStateException("No suitable fusion algorithm found"));
    }

    private void validateSensorData(List<SensorData> sensorDataList) {
        if (sensorDataList == null || sensorDataList.isEmpty()) {
            throw new IllegalArgumentException("Sensor data list cannot be null or empty");
        }

        long validSensors = sensorDataList.stream()
                .filter(SensorData::isValid)
                .count();

        if (validSensors == 0) {
            throw new IllegalArgumentException("No valid sensors found in data");
        }

        logger.debug("Validated {} sensors ({} valid)",
                sensorDataList.size(), validSensors);
    }

    /**
     * Get available fusion algorithms
     * Demonstrates polymorphism - returns list of different implementations
     */
    public List<FusionAlgorithm> getAvailableAlgorithms() {
        return fusionAlgorithms;
    }
}