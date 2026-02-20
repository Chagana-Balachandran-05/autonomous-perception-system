package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KalmanFilterFusion implements FusionAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(KalmanFilterFusion.class);

    @Override
    public FusionResult fuse(List<SensorData> sensorData) {
        if (sensorData == null || sensorData.isEmpty()) {
            return FusionResult.empty();
        }

        logger.info("KalmanFilterFusion: fusing {} sensors", sensorData.size());

        int totalPoints = sensorData.stream()
            .mapToInt(SensorData::getDataSize)
            .sum();

        double confidence = calculateFusionConfidence(sensorData);

        return new FusionResult(getName(), totalPoints, confidence, sensorData.size());
    }

    private double calculateFusionConfidence(List<SensorData> sensorData) {
        long validCount = sensorData.stream().filter(SensorData::isValid).count();
        return validCount == 0 ? 0.0 : (double) validCount / sensorData.size() * 0.95;
    }

    @Override
    public String getName() {
        return "KalmanFilterFusion";
    }
}
