package com.moderndaytech.perception.sensor;

public class CameraSensorData extends SensorData {

    private final byte[] imageData;
    private final int width;
    private final int height;

    public CameraSensorData(long timestamp, String sensorId,
                            byte[] imageData, int width, int height) {
        super(timestamp, sensorId, SensorType.CAMERA);
        this.imageData = imageData;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean isValid() {
        return imageData != null && imageData.length > 0 && width > 0 && height > 0;
    }

    @Override
    public int getDataSize() {
        return imageData.length;
    }

    @Override
    public String getMetricsReport() {
        return String.format("Camera[id=%s, size=%dx%d, bytes=%d, timestamp=%d]",
            getSensorId(), width, height, imageData.length, getTimestamp());
    }

    @Override
    protected String performSensorSpecificProcessing() {
        return "PROCESSED_CAMERA:" + width + "x" + height + "_AT_" + getTimestamp();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
