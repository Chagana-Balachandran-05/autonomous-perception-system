package com.moderndaytech.perception.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiDARSensorData extends SensorData {
    private static final Logger logger = LoggerFactory.getLogger(LiDARSensorData.class);

    private final float[] x;
    private final float[] y;
    private final float[] z;
    private final float[] intensity;

    public LiDARSensorData(long timestamp, String sensorId,
                           float[] x, float[] y, float[] z, float[] intensity) {
        super(timestamp, sensorId, SensorType.LIDAR);
        this.x = x;
        this.y = y;
        this.z = z;
        this.intensity = intensity;
    }

    @Override
    public boolean isValid() {
        return x != null && x.length > 0;
    }

    @Override
    public int getDataSize() {
        return x.length;
    }

    @Override
    public String getMetricsReport() {
        return String.format("LiDAR[id=%s, points=%d, timestamp=%d]",
            getSensorId(), x.length, getTimestamp());
    }

    @Override
    protected String performSensorSpecificProcessing() {
        return "PROCESSED_LIDAR:" + x.length + "_POINTS_AT_" + getTimestamp();
    }

    public int getPointCount() { return x.length; }

    public Point3D getPoint(int index) {
        return new Point3D(x[index], y[index], z[index], intensity[index]);
    }
}
