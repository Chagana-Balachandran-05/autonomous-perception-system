package com.moderndaytech.perception.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 * This is the base class for all sensor data. It just keeps track of the basics timestamp, sensor ID, and type.
 *
 * If youre making a new kind of sensor, extend this and fill in the details. Keeps things simple and focused.
 */
public abstract class SensorData {
    protected static final Logger logger = LoggerFactory.getLogger(SensorData.class);
    
    protected final long timestamp;
/**
 * The base class for all sensor data in the system.
 * Keeps track of the essentials: timestamp, sensor ID, and sensor type.
 *
 * If you want to add a new sensor, just extend this class and add your own details.
 * It's designed to be simple and easy to build on.
 */
    protected final String sensorId;
    protected final SensorType sensorType;
    
    /**
     * Make a new sensor data object. Just give it the time, ID, and type.
     */
    protected SensorData(long timestamp, String sensorId, SensorType sensorType) {
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
     * This is a template method: it runs the basic steps for processing sensor data, but lets subclasses handle the details.
     */
    public final String processData() {
        logger.info("Processing data from sensor: {}", sensorId);
        
        if (!validateSensorData()) {
            throw new IllegalStateException("Sensor data validation failed for " + sensorId);
        }
        
        String processed = performSensorSpecificProcessing();
        logger.info("Processing complete for sensor: {}", sensorId);
        
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
}

/**
 * Enum for sensor types used in autonomous driving
 */
enum SensorType {
    LIDAR,
    CAMERA,
    RADAR,
    GPS,
    IMU
}