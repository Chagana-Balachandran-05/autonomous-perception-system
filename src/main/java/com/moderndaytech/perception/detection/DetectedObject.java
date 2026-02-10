package com.moderndaytech.perception.detection;

/**
 * Represents an object detected by the perception system.
 * Stores its ID, class, position, and confidence score.
 * If you want details about what was found, this is the class to check.
 */
public class DetectedObject {
    private final String objectId;
    private final ObjectClass objectClass;
    private final Position3D position;
    private final double confidence;

    public DetectedObject(String objectId, ObjectClass objectClass,
            Position3D position, double confidence) {
        this.objectId = objectId;
        this.objectClass = objectClass;
        this.position = position;
        this.confidence = confidence;
    }

    public String getObjectId() {
        return objectId;
    }

    public ObjectClass getObjectClass() {
        return objectClass;
    }

    public Position3D getPosition() {
        return position;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return String.format("DetectedObject[id=%s, class=%s, pos=%s, conf=%.2f]",
                objectId, objectClass, position, confidence);
    }
}
