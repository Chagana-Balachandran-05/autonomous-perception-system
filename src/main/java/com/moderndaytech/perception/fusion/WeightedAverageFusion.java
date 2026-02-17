package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import java.util.List;

public class WeightedAverageFusion implements FusionAlgorithm {

    @Override
    public FusionResult fuse(List<SensorData> sensorData) {
        if (sensorData == null || sensorData.isEmpty()) {
            throw new IllegalArgumentException("Sensor data cannot be null or empty");
        }

        int totalDataPoints = sensorData.stream()
            .mapToInt(SensorData::getDataSize)
            .sum();

        double confidence = (double) sensorData.stream()
            .filter(SensorData::isValid)
            .count() / sensorData.size();

        return new FusionResult(
            getName(),
            totalDataPoints,
            confidence,
            sensorData.size()
        );
    }

    @Override
    public String getName() {
        return "Weighted Average Fusion";
    }
}
