package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticleFilterFusion implements FusionAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(ParticleFilterFusion.class);

    @Override
    public FusionResult fuse(List<SensorData> sensorData) {
        logger.info("[Particle Filter] Fusing {} sensors", sensorData.size());

        int totalDataPoints = sensorData.stream()
            .mapToInt(SensorData::getDataSize)
            .sum();

        double confidence = calculateConfidenceScore(sensorData);

        return new FusionResult(
            getName(),
            totalDataPoints,
            confidence,
            sensorData.size());
    }

    private double calculateConfidenceScore(List<SensorData> sensorData) {
        // Example confidence calculation
        return (double) sensorData.stream().filter(SensorData::isValid).count() / sensorData.size();
    }

    @Override
    public String getName() {
        return "Particle Filter Fusion";
    }
}
