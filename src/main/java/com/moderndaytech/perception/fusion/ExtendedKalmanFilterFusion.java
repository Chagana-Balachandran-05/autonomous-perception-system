package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import java.util.List;

/**
 * Extended Kalman Filter fusion implementation.
 * Demonstrates OCP - added without modifying any existing class.
 */
public class ExtendedKalmanFilterFusion implements FusionAlgorithm {

    @Override
    public FusionResult fuse(List<SensorData> sensorData) {
        if (sensorData == null || sensorData.isEmpty()) {
            return FusionResult.empty();
        }
        int totalPoints = sensorData.stream().mapToInt(SensorData::getDataSize).sum();
        double confidence = Math.min(0.93,
            (double) sensorData.stream().filter(SensorData::isValid).count()
            / sensorData.size() * 0.95);
        return new FusionResult(getName(), totalPoints, confidence, sensorData.size());
    }

    @Override
    public String getName() {
        return "Extended Kalman Filter";
    }
}
