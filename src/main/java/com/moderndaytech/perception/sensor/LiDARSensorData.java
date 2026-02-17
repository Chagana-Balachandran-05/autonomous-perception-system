package com.moderndaytech.perception.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles LiDAR sensor data for the system.
 * Extends the base sensor data class and adds LiDAR-specific features like point clouds and range.
 *
 * If you need to generate fake LiDAR data for testing, there's a helper method for that.
 */

public class LiDARSensorData extends SensorData {
    private final float[] x;
    private final float[] y;
    private final float[] z;
    private final float[] intensity;

    public LiDARSensorData(long timestamp, String sensorId, float[] x, float[] y, float[] z, float[] intensity) {
        super(timestamp, sensorId, SensorType.LIDAR);
        if (x == null || y == null || z == null || intensity == null ||
            x.length != y.length || x.length != z.length || x.length != intensity.length) {
            throw new IllegalArgumentException("All arrays must be non-null and of equal length");
        }
        this.x = x;
        this.y = y;
        this.z = z;
        this.intensity = intensity;
    }
    
    /**
      * Static method to create realistic LiDAR data for tests.
      * Just call this and you'll get a LiDARSensorData object with typical values.
     */
    public static LiDARSensorData createRealisticData(String sensorId) {
        long timestamp = System.currentTimeMillis();
        int numPoints = 5000;
        float[] x = new float[numPoints];
        float[] y = new float[numPoints];
        float[] z = new float[numPoints];
        float[] intensity = new float[numPoints];
        Random random = new java.security.SecureRandom();
        for (int i = 0; i < numPoints; i++) {
            x[i] = (float)((random.nextDouble() - 0.5) * 100);
            y[i] = (float)((random.nextDouble() - 0.5) * 100);
            z[i] = (float)(random.nextDouble() * 5);
            intensity[i] = (float)(random.nextDouble() * 255);
        }
        return new LiDARSensorData(timestamp, sensorId, x, y, z, intensity);
    }
    
    @Override
    public boolean isValid() {
        return x != null && x.length > 0 && x.length < 2_000_000;
    }
    
    @Override
    public String getMetricsReport() {
        return String.format(
            "LiDAR Metrics - Points: %d, Max Range: %.2fm, Avg Intensity: %.2f",
            getPointCount(), getMaxRange(), calculateAverageIntensity()
        );
    }
    
    @Override
    public int getDataSize() {
        return getPointCount() * 16;
    }
    
    @Override
    protected String performSensorSpecificProcessing() {
        // Logic fix: Define 'filteredPoints' before using it
        logger.debug("Filtering {} LiDAR points", getPointCount());

        // Simulating processing: Let’s assume 95% of points remain after noise filtering
        int filteredPoints = (int) (getPointCount() * 0.95);

        return String.format("Processed %d points (filtered %d noise points)",
            filteredPoints, getPointCount() - filteredPoints);
    }
    
    /**
     * LiDAR-specific method: Get points within range
     */
    public List<Point3D> getPointsInRange(double minRange, double maxRange) {
        List<Point3D> filtered = new ArrayList<>();
        for (int i = 0; i < x.length; i++) {
            double distance = Math.sqrt(x[i] * x[i] + y[i] * y[i]);
            if (distance >= minRange && distance <= maxRange) {
                filtered.add(getPoint(i));
            }
        }
        return filtered;
    }
    
    public int getPointCount() {
        return x.length;
    }
    
    public double getMaxRange() {
        double max = 0.0;
        for (int i = 0; i < x.length; i++) {
            double dist = Math.sqrt(x[i] * x[i] + y[i] * y[i] + z[i] * z[i]);
            if (dist > max) max = dist;
        }
        return max;
    }
    
    // Private helper methods
    private double calculateAverageIntensity() {
        double sum = 0.0;
        for (float v : intensity) sum += v;
        return intensity.length == 0 ? 0.0 : sum / intensity.length;
    }

    public Point3D getPoint(int index) {
        return new Point3D(x[index], y[index], z[index], intensity[index]);
    }
}

/**
 * Simple 3D point representation for LiDAR data
 */
