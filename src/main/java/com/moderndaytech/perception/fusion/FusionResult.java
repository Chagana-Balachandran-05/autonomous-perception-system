package com.moderndaytech.perception.fusion;

/**
 * Holds the result of fusing sensor data.
 * Tells you which algorithm was used, how much data was processed, how confident the system is, and how many sensors were involved.
 * If you want a summary of the fusion process, this is it.
 */
public class FusionResult {
    private final String algorithmUsed;
    private final int totalDataPoints;
    private final double confidenceScore;
    private final int sensorCount;

    public FusionResult(String algorithmUsed, int totalDataPoints,
            double confidenceScore, int sensorCount) {
        this.algorithmUsed = algorithmUsed;
        this.totalDataPoints = totalDataPoints;
        this.confidenceScore = confidenceScore;
        this.sensorCount = sensorCount;
    }

    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    public int getTotalDataPoints() {
        return totalDataPoints;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public int getSensorCount() {
        return sensorCount;
    }

    @Override
    public String toString() {
        return String.format("FusionResult[algorithm=%s, dataPoints=%d, confidence=%.2f, sensors=%d]",
                algorithmUsed, totalDataPoints, confidenceScore, sensorCount);
    }
}
