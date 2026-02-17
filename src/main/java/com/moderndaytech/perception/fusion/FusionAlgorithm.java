package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import java.util.List;

/**
 * Interface for sensor fusion algorithms.
 * Defines the contract for combining multiple sensor readings into a unified result.
 * 
 * <p>This interface demonstrates the Strategy Pattern and Open-Closed Principle,
 * allowing new fusion algorithms to be added without modifying existing code.</p>
 * 
 * @author Chagana Balachandran
 * @version 1.0
 * @see KalmanFilterFusion
 * @see ParticleFilterFusion
 * @see WeightedAverageFusion
 */
public interface FusionAlgorithm {
    /**
     * Fuses multiple sensor data inputs into a single unified result.
     * 
     * @param sensorData List of sensor data to be fused
     * @return FusionResult containing the fused state and confidence
     * @throws IllegalArgumentException if sensorData is null or empty
     */
    FusionResult fuse(List<SensorData> sensorData);
    /**
     * Returns the name of this fusion algorithm.
     * 
     * @return Algorithm name (e.g., "Kalman Filter", "Particle Filter")
     */
    String getName();
}
