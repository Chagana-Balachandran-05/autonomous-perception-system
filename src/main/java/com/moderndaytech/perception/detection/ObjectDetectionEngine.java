package com.moderndaytech.perception.detection;

import com.moderndaytech.perception.fusion.FusionResult;
import com.moderndaytech.perception.sensor.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles object detection for the autonomous perception system.
 * Its only job is to find objects in sensor data—nothing else.
 * If you want to know what the system "sees," this is the engine to use.
 */
public class ObjectDetectionEngine {
    private static final Logger logger = LoggerFactory.getLogger(ObjectDetectionEngine.class);
    private final Random random = new java.security.SecureRandom();

    /**
     * Overload: detect objects from raw sensor data directly.
     * Used in integration tests.
     */
    public List<DetectedObject> detectObjects(SensorData sensorData) {
        logger.info("Detecting from raw sensor: {}", sensorData.getSensorId());
        List<DetectedObject> objects = new ArrayList<>();
        int numObjects = Math.min(10, sensorData.getDataSize() / 100000 + 1);
        for (int i = 0; i < numObjects; i++) {
            objects.add(generateDetectedObject(i));
        }
        return filterByConfidence(objects);
    }

    public List<DetectedObject> filterByConfidence(List<DetectedObject> objects) {
        return objects.stream()
            .filter(obj -> obj.getConfidence() > 0.5)
            .toList();
    }

    public double calculateConfidence(DetectedObject obj) {
        return obj.getConfidence();
    }

    /**
     * Detect objects in fused sensor data
     */
    public List<DetectedObject> detectObjects(FusionResult fusionResult) {
        logger.info("Starting object detection on fused data from {} sensors",
                fusionResult.getSensorCount());

        long startTime = System.currentTimeMillis();

        // Simulate object detection based on data volume
         int numObjects = Math.min(50, (fusionResult.getTotalDataPoints() / 1000) + 1);

        List<DetectedObject> detectedObjects = new ArrayList<>();

        for (int i = 0; i < numObjects; i++) {
            DetectedObject obj = generateDetectedObject(i);
            detectedObjects.add(obj);
        }

        // Filter by confidence threshold
        List<DetectedObject> filteredObjects = detectedObjects.stream()
                .filter(obj -> obj.getConfidence() > 0.5)
                .collect(Collectors.toList());

        long processingTime = System.currentTimeMillis() - startTime;

        logger.info("Object detection completed: {} objects detected in {}ms",
                filteredObjects.size(), processingTime);

        return filteredObjects;
    }

    private DetectedObject generateDetectedObject(int id) {
        ObjectClass[] classes = ObjectClass.values();
        ObjectClass objClass = classes[random.nextInt(classes.length)];

        // Random position in 3D space
        double x = (random.nextDouble() - 0.5) * 50;
        double y = (random.nextDouble() - 0.5) * 50;
        double z = random.nextDouble() * 2;

        // Random confidence score
        double confidence = 0.5 + random.nextDouble() * 0.5;

        return new DetectedObject(
                "OBJ_" + id,
                objClass,
                new Position3D(x, y, z),
                confidence);
    }
}

/**
 * Object classes for autonomous driving
 */
enum ObjectClass {
    VEHICLE,
    PEDESTRIAN,
    BICYCLE,
    TRAFFIC_SIGN,
    TRAFFIC_LIGHT,
    OBSTACLE
}
