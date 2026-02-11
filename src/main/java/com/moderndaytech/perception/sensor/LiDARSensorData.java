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
    private final List<Point3D> pointCloud;
    private final double maxRange;
    private final int pointCount;
    
    public LiDARSensorData(long timestamp, String sensorId, List<Point3D> pointCloud) {
        super(timestamp, sensorId, SensorType.LIDAR);
        this.pointCloud = pointCloud != null ? pointCloud : new ArrayList<>();
        this.pointCount = this.pointCloud.size();
        this.maxRange = calculateMaxRange();
    }
    
    /**
      * Static method to create realistic LiDAR data for tests.
      * Just call this and you'll get a LiDARSensorData object with typical values.
     */
    public static LiDARSensorData createRealisticData(String sensorId) {
        long timestamp = System.currentTimeMillis();
        List<Point3D> points = generateRealisticPointCloud(5000);
        return new LiDARSensorData(timestamp, sensorId, points);
    }
    
    @Override
    public boolean isValid() {
        // Data is valid if there’s a point cloud, not too many points, and a positive range
        return pointCloud != null && 
               pointCount > 0 && 
               pointCount < 2_000_000 && // Don’t go overboard!
               maxRange > 0;
    }
    
    @Override
    public String getMetricsReport() {
        // Give a quick summary of the LiDAR data
        return String.format(
            "LiDAR Metrics - Points: %d, Max Range: %.2fm, Avg Intensity: %.2f",
            pointCount, maxRange, calculateAverageIntensity()
        );
    }
    
    @Override
    public int getDataSize() {
        // Each point is about 16 bytes (x, y, z, intensity)
        return pointCount * 16;
    }
    
    @Override
    protected String performSensorSpecificProcessing() {
        // Logic fix: Define 'filteredPoints' before using it
        logger.debug("Filtering {} LiDAR points", pointCount);
        
        // Simulating processing: Let’s assume 95% of points remain after noise filtering
        int filteredPoints = (int) (pointCount * 0.95); 
        
        return String.format("Processed %d points (filtered %d noise points)", 
            filteredPoints, pointCount - filteredPoints);
    }
    
    /**
     * LiDAR-specific method: Get points within range
     */
    public List<Point3D> getPointsInRange(double minRange, double maxRange) {
        List<Point3D> filtered = new ArrayList<>();
        for (Point3D point : pointCloud) {
            double distance = Math.sqrt(point.x * point.x + point.y * point.y);
            if (distance >= minRange && distance <= maxRange) {
                filtered.add(point);
            }
        }
        return filtered;
    }
    
    public int getPointCount() {
        return pointCount;
    }
    
    public double getMaxRange() {
        return maxRange;
    }
    
    // Private helper methods
    private double calculateMaxRange() {
        return pointCloud.stream()
            .mapToDouble(p -> Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z))
            .max()
            .orElse(0.0);
    }
    
    private double calculateAverageIntensity() {
        return pointCloud.stream()
            .mapToDouble(p -> p.intensity)
            .average()
            .orElse(0.0);
    }
    
    private static List<Point3D> generateRealisticPointCloud(int numPoints) {
        List<Point3D> points = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < numPoints; i++) {
            double x = (random.nextDouble() - 0.5) * 100; // -50m to 50m
            double y = (random.nextDouble() - 0.5) * 100;
            double z = random.nextDouble() * 5; // 0 to 5m height
            double intensity = random.nextDouble() * 255;
            
            points.add(new Point3D(x, y, z, intensity));
        }
        
        return points;
    }
}

/**
 * Simple 3D point representation for LiDAR data
 */
class Point3D {
    final double x, y, z;
    final double intensity;
    
    public Point3D(double x, double y, double z, double intensity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.intensity = intensity;
    }
    
    @Override
    public String toString() {
        return String.format("Point3D(%.2f, %.2f, %.2f, i=%.1f)", x, y, z, intensity);
    }
}