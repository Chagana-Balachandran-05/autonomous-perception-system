package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import java.util.List;

/**
 * Mock fusion algorithm for testing purposes.
 * Returns predictable results for unit testing.
 * 
 * @author Chagana Balachandran
 * @version 1.0
 */
public class MockFusionAlgorithm implements FusionAlgorithm {
    private final FusionResult predefinedResult;

    /**
     * Creates a mock algorithm with default result.
     */
    public MockFusionAlgorithm() {
        this.predefinedResult = new FusionResult(
            "Mock Fusion Algorithm",
            100,  // Mock data size
            0.95, // Mock confidence
            2     // Mock sensor count
        );
    }

    /**
     * Creates a mock algorithm with predefined result.
     * 
     * @param predefinedResult The result to return from fuse()
     */
    public MockFusionAlgorithm(FusionResult predefinedResult) {
        this.predefinedResult = predefinedResult;
    }

    @Override
    public FusionResult fuse(List<SensorData> sensorData) {
        if (sensorData == null || sensorData.isEmpty()) {
            throw new IllegalArgumentException("Sensor data cannot be null or empty");
        }
        // Return predefined result for predictable testing
        return predefinedResult;
    }

    @Override
    public String getName() {
        return "Mock Fusion Algorithm";
    }
}
