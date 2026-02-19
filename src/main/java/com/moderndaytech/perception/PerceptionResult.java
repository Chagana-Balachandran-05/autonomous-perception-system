package com.moderndaytech.perception;

import com.moderndaytech.perception.detection.DetectedObject;
import com.moderndaytech.perception.fusion.FusionResult;

import java.util.List;

public class PerceptionResult {
    private final FusionResult fusionResult;
    private final List<DetectedObject> detectedObjects;
    private final long processingTimeMs;

    public PerceptionResult(FusionResult fusionResult,
                            List<DetectedObject> detectedObjects,
                            long processingTimeMs) {
        this.fusionResult = fusionResult;
        this.detectedObjects = detectedObjects;
        this.processingTimeMs = processingTimeMs;
    }

    public FusionResult getFusionResult() { return fusionResult; }
    public List<DetectedObject> getDetectedObjects() { return detectedObjects; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    public int getObjectCount() { return detectedObjects.size(); }
}