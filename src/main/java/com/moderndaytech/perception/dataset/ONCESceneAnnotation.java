package com.moderndaytech.perception.dataset;

import java.util.List;

public class ONCESceneAnnotation {

    private final String sceneId;
    private final String frameId;
    private final long timestamp;
    private final int lidarPoints;
    private final List<String> objectNames;
    private final List<double[]> boxes3d;

    public ONCESceneAnnotation(String sceneId, String frameId, long timestamp,
                               int lidarPoints, List<String> objectNames,
                               List<double[]> boxes3d) {
        if (sceneId == null || sceneId.isEmpty()) {
            throw new IllegalArgumentException("Scene ID cannot be null or empty");
        }
        if (lidarPoints < 0) {
            throw new IllegalArgumentException("LiDAR point count cannot be negative");
        }
        if (objectNames == null || boxes3d == null) {
            throw new IllegalArgumentException("Annotation lists cannot be null");
        }
        if (objectNames.size() != boxes3d.size()) {
            throw new IllegalArgumentException(
                "Object names and boxes_3d must have equal length. names="
                + objectNames.size() + " boxes=" + boxes3d.size());
        }
        this.sceneId = sceneId;
        this.frameId = frameId;
        this.timestamp = timestamp;
        this.lidarPoints = lidarPoints;
        this.objectNames = List.copyOf(objectNames);
        this.boxes3d = boxes3d;
    }

    public String getSceneId() { return sceneId; }
    public String getFrameId() { return frameId; }
    public long getTimestamp() { return timestamp; }
    public int getLidarPoints() { return lidarPoints; }
    public List<String> getObjectNames() { return objectNames; }
    public List<double[]> getBoxes3d() { return boxes3d; }
    public int getAnnotationCount() { return objectNames.size(); }

    @Override
    public String toString() {
        return String.format("ONCESceneAnnotation[scene=%s, frame=%s, points=%d, objects=%d]",
            sceneId, frameId, lidarPoints, objectNames.size());
    }
}
