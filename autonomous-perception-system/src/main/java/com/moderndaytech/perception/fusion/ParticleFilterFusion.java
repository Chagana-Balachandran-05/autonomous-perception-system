package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import java.util.List;

public class ParticleFilterFusion implements FusionAlgorithm {

    @Override
    public boolean isApplicable(List<SensorData> sensorDataList) {
        // Particle filter is a good fallback, works even if you only have one sensor or things are non-linear
        return sensorDataList != null && sensorDataList.size() >= 1;
    }

    @Override
    public double calculateConfidenceScore(List<SensorData> sensorDataList) {
        // If there’s no data, confidence is zero
        if (sensorDataList == null || sensorDataList.isEmpty()) {
            return 0.0;
        }

        // Particle filter confidence: a bit different, maxes out at 0.9
        return Math.min(0.9, sensorDataList.size() * 0.3);
    }

        @Override
        public FusionResult fuseSensorData(List<SensorData> sensorDataList) {
        // Print out which algorithm is running
        System.out.println("[Particle Filter] Fusing " + sensorDataList.size() + " sensors");

        // Add up all the data points
        int totalDataPoints = sensorDataList.stream()
            .mapToInt(SensorData::getDataSize)
            .sum();

        // Calculate confidence for this fusion
        double confidence = calculateConfidenceScore(sensorDataList);

        // Return the result, including which algorithm was used and how many sensors we had
        return new FusionResult(
            getAlgorithmName(),
            totalDataPoints,
            confidence,
            sensorDataList.size());
        }

    @Override
    public String getAlgorithmName() {
        return "ParticleFilterFusion";
    }
}
