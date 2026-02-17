package com.moderndaytech.perception.test;

import com.moderndaytech.perception.sensor.SensorData;
import com.moderndaytech.perception.sensor.LiDARSensorData;
import com.moderndaytech.perception.sensor.CameraSensorData;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Factory for creating test sensor data
 * 
 * Centralized location for test data creation - follows DRY principle.
 * When sensor constructors change, only update here instead of every test.
 * Demonstrates good testing practices and maintainability.
 */
public class TestDataFactory {

    private static final Random random = new Random(42); // Fixed seed for reproducibility

    /**
     * Create a realistic LiDAR sensor with sample point cloud data
     */
    public static LiDARSensorData createTestLiDARSensor() {
        long timestamp = System.currentTimeMillis();
        String sensorId = "TEST-LIDAR-001";
        int numPoints = 100;
        float[] x = new float[numPoints];
        float[] y = new float[numPoints];
        float[] z = new float[numPoints];
        float[] intensity = new float[numPoints];
        for (int i = 0; i < numPoints; i++) {
            x[i] = (float)((random.nextDouble() - 0.5) * 50);
            y[i] = (float)((random.nextDouble() - 0.5) * 50);
            z[i] = (float)(random.nextDouble() * 3);
            intensity[i] = (float)(random.nextDouble() * 255);
        }
        return new LiDARSensorData(timestamp, sensorId, x, y, z, intensity);
    }

    /**
     * Create a realistic camera sensor with sample image data
     */
    public static CameraSensorData createTestCameraSensor() {
        long timestamp = System.currentTimeMillis();
        String sensorId = "TEST-CAMERA-001";
        
        // Create sample image data (small size for testing: 100x100 RGB)
        int width = 100;
        int height = 100;
        byte[] imageData = new byte[width * height * 3]; // RGB format
        
        // Fill with sample data (gradient pattern for visual testing)
        for (int i = 0; i < imageData.length; i++) {
            imageData[i] = (byte) (i % 256);
        }
        
        return new CameraSensorData(timestamp, sensorId, width, height, imageData);
    }

    /**
     * Create a list of multiple test sensors for fusion testing
     */
    public static List<SensorData> createTestSensors() {
        List<SensorData> sensors = new ArrayList<>();
        sensors.add(createTestLiDARSensor());
        sensors.add(createTestCameraSensor());
        return sensors;
    }

    /**
     * Create a LiDAR sensor with custom number of points
     */
    public static LiDARSensorData createTestLiDARSensor(int numPoints) {
        long timestamp = System.currentTimeMillis();
        String sensorId = "TEST-LIDAR-CUSTOM";
        
        float[] x = new float[numPoints];
        float[] y = new float[numPoints];
        float[] z = new float[numPoints];
        float[] intensity = new float[numPoints];
        for (int i = 0; i < numPoints; i++) {
            x[i] = (float)((random.nextDouble() - 0.5) * 50);
            y[i] = (float)((random.nextDouble() - 0.5) * 50);
            z[i] = (float)(random.nextDouble() * 3);
            intensity[i] = (float)(random.nextDouble() * 255);
        }
        return new LiDARSensorData(timestamp, sensorId, x, y, z, intensity);
    }

    /**
     * Create a camera sensor with custom dimensions
     */
    public static CameraSensorData createTestCameraSensor(int width, int height) {
        long timestamp = System.currentTimeMillis();
        String sensorId = "TEST-CAMERA-CUSTOM";
        
        byte[] imageData = new byte[width * height * 3];
        for (int i = 0; i < imageData.length; i++) {
            imageData[i] = (byte) (i % 256);
        }
        
        return new CameraSensorData(timestamp, sensorId, width, height, imageData);
    }

    /**
     * Create an INVALID LiDAR sensor (for negative testing)
     */
    public static LiDARSensorData createInvalidLiDARSensor() {
        long timestamp = -1; // Invalid timestamp
        String sensorId = "INVALID-LIDAR";
        float[] x = new float[0];
        float[] y = new float[0];
        float[] z = new float[0];
        float[] intensity = new float[0];
        return new LiDARSensorData(timestamp, sensorId, x, y, z, intensity);
    }

    /**
     * Create multiple sensors with same timestamp (for synchronization testing)
     */
    public static List<SensorData> createSynchronizedSensors(long timestamp) {
        List<SensorData> sensors = new ArrayList<>();
        
        String lidarId = "SYNC-LIDAR-001";
        float[] x = new float[50];
        float[] y = new float[50];
        float[] z = new float[50];
        float[] intensity = new float[50];
        for (int i = 0; i < 50; i++) {
            x[i] = (float)(i * 0.5);
            y[i] = (float)(i * 0.5);
            z[i] = 1.0f;
            intensity[i] = 128.0f;
        }
        sensors.add(new LiDARSensorData(timestamp, lidarId, x, y, z, intensity));
        
        String cameraId = "SYNC-CAMERA-001";
        byte[] imageData = new byte[100 * 100 * 3];
        sensors.add(new CameraSensorData(timestamp, cameraId, 100, 100, imageData));
        
        return sensors;
    }
}
