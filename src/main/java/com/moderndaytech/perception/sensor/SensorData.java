package com.moderndaytech.perception.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all sensor data types in the autonomous perception system.
 * Implements the Template Method design pattern for sensor data processing.
 *
 * <h2>SOLID Principles Applied:</h2>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP)</strong>: This class has one responsibility -
 *       managing sensor data representation and validation. It does not handle fusion, detection,
 *       or security validation.</li>
 *   <li><strong>Open-Closed Principle (OCP)</strong>: Extensible through inheritance
 *       (LiDARSensorData, CameraSensorData) without modifying this base class.</li>
 *   <li><strong>Liskov Substitution Principle (LSP)</strong>: Any subclass can be used wherever
 *       SensorData is expected without breaking functionality.</li>
 *   <li><strong>Dependency Inversion Principle (DIP)</strong>: Higher-level components depend on
 *       this abstraction, not on concrete sensor implementations.</li>
 * </ul>
 *
 * <h2>Design Patterns:</h2>
 * <ul>
 *   <li><strong>Template Method</strong>: The {@link #processData()} method defines the algorithm
 *       skeleton, while subclasses implement sensor-specific behavior via {@link #isValid()} and
 *       {@link #performSensorSpecificProcessing()}.</li>
 *   <li><strong>Encapsulation</strong>: Fields are protected/private with controlled access through
 *       public getters. No setters exist - data is immutable after construction.</li>
 * </ul>
 *
 * <h2>Clean Code Techniques:</h2>
 * <ul>
 *   <li><strong>Meaningful Names</strong>: Methods like isValid(), processData(), getMetricsReport()
 *       clearly express their intent without needing additional documentation.</li>
 *   <li><strong>Guard Clauses</strong>: The processData() method validates data before processing,
 *       failing fast on invalid input.</li>
 *   <li><strong>DRY (Don't Repeat Yourself)</strong>: Common timestamp, ID, and type handling
 *       exists once here rather than duplicated in every sensor subclass.</li>
 *   <li><strong>Final Fields</strong>: Immutability prevents accidental state changes and makes
 *       the class thread-safe.</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Create specific sensor data
 * SensorData lidar = new LiDARSensorData(timestamp, "LIDAR-01", pointCloudData);
 *
 * // Process through abstract interface
 * if (lidar.isValid()) {
 *     String result = lidar.processData();
 *     System.out.println(lidar.getMetricsReport());
 * }
 * }</pre>
 *
 * @author Chagana Balachandran
 * @version 1.0
 * @since 2026-02-10
 *
 * @see LiDARSensorData
 * @see CameraSensorData
 * @see com.moderndaytech.perception.fusion.SensorFusionProcessor
 */
public abstract class SensorData {
    protected static final Logger logger = LoggerFactory.getLogger(SensorData.class);
    
    protected final long timestamp;
    protected final String sensorId;
    protected final SensorType sensorType;
    
    /**
     * Make a new sensor data object. Just give it the time, ID, and type.
     */
    protected SensorData(long timestamp, String sensorId, SensorType sensorType) {
        if (timestamp <= 0) {
            throw new IllegalArgumentException(
                "Timestamp must be positive, received: " + timestamp);
        }
        if (sensorId == null || sensorId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Sensor ID cannot be null or empty");
        }
        if (sensorType == null) {
            throw new IllegalArgumentException("SensorType cannot be null");
        }
        this.timestamp = timestamp;
        this.sensorId = sensorId;
        this.sensorType = sensorType;
    }
    
    // Subclasses have to fill these in
    public abstract boolean isValid();
    public abstract String getMetricsReport();
    public abstract int getDataSize();
    
    // These are available to all sensors
    public long getTimestamp() { 
        return timestamp; 
    }
    
    public String getSensorId() { 
        return sensorId; 
    }
    
    public SensorType getSensorType() { 
        return sensorType; 
    }
    
    /**
     * Template method that defines the sensor data processing workflow.
     * Validates data before processing and logs the operation.
     *
     * <p>This method is final to enforce the processing sequence:
     * <ol>
     *   <li>Validate sensor data using {@link #validateSensorData()}</li>
     *   <li>Perform sensor-specific processing via {@link #performSensorSpecificProcessing()}</li>
     *   <li>Return processed result</li>
     * </ol>
     *
     * <p>Subclasses cannot override this method but must implement the abstract methods
     * it calls to define sensor-specific behavior.
     *
     * @return processed sensor data as a string representation
     * @throws IllegalStateException if sensor data fails validation
     *
     * @see #validateSensorData()
     * @see #performSensorSpecificProcessing()
     */
    public final String processData() {
        logger.info("Processing data from sensor: {}", sanitizeForLog(sensorId));
        
        if (!validateSensorData()) {
            throw new IllegalStateException("Sensor data validation failed for " + sensorId);
        }
        
        String processed = performSensorSpecificProcessing();
        logger.info("Processing complete for sensor: {}", sanitizeForLog(sensorId));
        
        return processed;
    }
    
    protected abstract String performSensorSpecificProcessing();
    
    private boolean validateSensorData() {
        return isValid() && timestamp > 0 && sensorId != null;
    }
    
    @Override
    public String toString() {
        return String.format("SensorData[type=%s, id=%s, timestamp=%d]", 
            sensorType, sensorId, timestamp);
    }

    private String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\r\n\t]", "_").substring(0, Math.min(input.length(), 100));
    }
}

