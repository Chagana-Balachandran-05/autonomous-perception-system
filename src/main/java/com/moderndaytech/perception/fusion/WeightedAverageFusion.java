package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import java.util.List;

public class WeightedAverageFusion implements FusionAlgorithm {

    @Override
    public FusionResult fuse(List<SensorData> sensorData) {
        if (sensorData == null || sensorData.isEmpty()) {
            return FusionResult.empty();
        }

        int totalPoints = sensorData.stream()
            .mapToInt(SensorData::getDataSize)
            .sum();

        long validCount = sensorData.stream()
            .filter(SensorData::isValid)
            .count();

        if (validCount == 0) {
            return FusionResult.empty();
        }

        double confidence = Math.min(0.80,
            (double) validCount / sensorData.size() * 0.85);

        return new FusionResult(getName(), totalPoints, confidence, sensorData.size());
    }

    @Override
    public String getName() {
        return "Weighted Average Fusion";
    }
}
