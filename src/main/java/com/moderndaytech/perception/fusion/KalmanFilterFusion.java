package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import java.util.List;

public class KalmanFilterFusion implements FusionAlgorithm {

    @Override
    public boolean isApplicable(List<SensorData> sensorDataList) {
        // Kalman filter is best when you have at least two continuous sensor streams
        return sensorDataList != null && sensorDataList.size() >= 2;
    }

    @Override
    public double calculateConfidenceScore(List<SensorData> sensorDataList) {
        // If there’s no data, confidence is zero
        if (sensorDataList == null || sensorDataList.isEmpty()) {
            return 0.0;
        }

        // Confidence is just the ratio of valid sensors to total sensors
        long validSensors = sensorDataList.stream()
                .filter(SensorData::isValid)
                .count();

        return (double) validSensors / sensorDataList.size();
    }

        @Override
        public FusionResult fuseSensorData(List<SensorData> sensorDataList) {
        // Just a print to show which algorithm is running
        System.out.println("[Kalman Filter] Fusing " + sensorDataList.size() + " sensors");

        // Add up all the data points from every sensor
        int totalDataPoints = sensorDataList.stream()
            .mapToInt(SensorData::getDataSize)
            .sum();

        // Figure out how confident we are in this fusion
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
        return "KalmanFilterFusion";
    }
}
