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
    private final Object estimatedState;
    private final long timestamp;

    public FusionResult(String algorithmUsed, int totalDataPoints,
            double confidenceScore, int sensorCount) {
        this(algorithmUsed, totalDataPoints, confidenceScore, sensorCount, null);
    }

    public FusionResult(String algorithmUsed, int totalDataPoints,
            double confidenceScore, int sensorCount, Object estimatedState) {
        this.algorithmUsed = algorithmUsed;
        this.totalDataPoints = totalDataPoints;
        this.confidenceScore = confidenceScore;
        this.sensorCount = sensorCount;
        this.estimatedState = estimatedState;
        this.timestamp = System.currentTimeMillis();
    }


    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    public int getTotalDataPoints() {
        return totalDataPoints;
    }

    public double getConfidence() {
        return confidenceScore;
    }

    public int getSensorCount() {
        return sensorCount;
    }

    public Object getEstimatedState() {
        return estimatedState;
    }


    public boolean isValid() {
        return sensorCount > 0 && confidenceScore >= 0.0;
    }

    public static FusionResult empty() {
        return new FusionResult("None", 0, 0.0, 0);
    }


    // Used in integration tests  
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("FusionResult[algorithm=%s, dataPoints=%d, confidence=%.2f, sensors=%d]",
                algorithmUsed, totalDataPoints, confidenceScore, sensorCount);
    }
}
