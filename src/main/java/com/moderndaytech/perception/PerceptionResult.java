package com.moderndaytech.perception;

import java.util.List;
import java.util.ArrayList;
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
    private final String errorMessage; // Added to handle error details

    // Constructor to initialize all the fields
    public PerceptionResult(List<DetectedObject> detectedObjects,
            FusionResult fusionResult,
            long processingTimeMs,
            boolean success,
            String errorMessage) {
        this.detectedObjects = detectedObjects;
        this.fusionResult = fusionResult;
        this.processingTimeMs = processingTimeMs;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static PerceptionResult failure(String message) {
        return new PerceptionResult(new ArrayList<>(), null, 0, false, message);
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

    public String getErrorMessage() {
        return errorMessage;
    }
}