package com.moderndaytech.perception;

import java.util.List;
import com.moderndaytech.perception.detection.DetectedObject;
import com.moderndaytech.perception.fusion.FusionResult;

/**
 * Simple container for the final output of the perception pipeline.
 * Holds the list of detected objects, fusion metadata, and performance metrics.
 * If you want to know what the system saw and how well it did, this is where you look.
 */
public class PerceptionResult {
    private final List<DetectedObject> detectedObjects;
    private final FusionResult fusionResult;
    private final long processingTimeMs;
    private final boolean success;

    // Constructor to initialize all the final fields
    public PerceptionResult(List<DetectedObject> detectedObjects,
            FusionResult fusionResult,
            long processingTimeMs,
            boolean success) {
        this.detectedObjects = detectedObjects;
        this.fusionResult = fusionResult;
        this.processingTimeMs = processingTimeMs;
        this.success = success;
    }

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }

    public FusionResult getFusionResult() {
        return fusionResult;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public boolean isSuccess() {
        return success;
    }
}
