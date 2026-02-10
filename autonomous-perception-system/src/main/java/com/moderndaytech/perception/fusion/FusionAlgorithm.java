package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import java.util.List;

/**
 * Interface for sensor fusion algorithms.
 * If you want to add a new way to fuse sensors, just implement this interface.
 * The rest of the system doesn't care which algorithm you use, as long as you stick to this contract.
 */
public interface FusionAlgorithm {

    /**
     * Should this algorithm handle the current sensor data? Return true if yes.
     */
    boolean isApplicable(List<SensorData> sensorDataList);

    /**
     * How confident are we that this algorithm is the right fit for the data? Higher is better.
     */
    double calculateConfidenceScore(List<SensorData> sensorDataList);

    /**
     * Actually do the fusion and return the result.
     */
    FusionResult fuseSensorData(List<SensorData> sensorDataList);

    /**
     * What’s the name of this algorithm? Used for logs and metrics.
     */
    String getAlgorithmName();
}
