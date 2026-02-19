package com.moderndaytech.perception.detection;

public class DetectedObject {
    private final String objectId;
    private final ObjectType type;
    private final Position3D position;
    private final double confidence;

    public DetectedObject(String objectId, ObjectType type,
                          Position3D position, double confidence) {
        this.objectId = objectId;
        this.type = type;
        this.position = position;
        this.confidence = confidence;
    }

    public String getObjectId() { return objectId; }
    public ObjectType getType() { return type; }
    public Position3D getPosition() { return position; }
    public double getConfidence() { return confidence; }

    @Override
    public String toString() {
        return String.format("DetectedObject[id=%s, type=%s, confidence=%.2f, pos=%s]",
            objectId, type, confidence, position);
    }
}
