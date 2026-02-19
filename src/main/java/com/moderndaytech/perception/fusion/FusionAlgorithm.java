package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import java.util.List;

/**
 * Strategy interface for sensor fusion algorithms.
 *
 * <h2>Design Intent:</h2>
 * <ul>
 *   <li>Defines the contract for pluggable fusion implementations such as Kalman, Particle, and weighted average.</li>
 *   <li>Allows runtime algorithm selection without changing the orchestration logic.</li>
 * </ul>
 *
 * <h2>SOLID Principles Applied:</h2>
 * <ul>
 *   <li><strong>Open-Closed Principle (OCP)</strong>: New algorithms are added by implementing this interface.</li>
 *   <li><strong>Liskov Substitution Principle (LSP)</strong>: Any implementation can substitute another transparently.</li>
 *   <li><strong>Dependency Inversion Principle (DIP)</strong>: High-level fusion processing depends on this abstraction.</li>
 * </ul>
 *
 * @author Chagana Balachandran
 * @version 1.0
 * @since 2026-02-10
 */
public interface FusionAlgorithm {
    FusionResult fuse(List<SensorData> sensorData);

    String getName();
}
