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
        if (x == null || y == null || z == null || intensity == null) {
            throw new IllegalArgumentException("Point cloud arrays cannot be null");
        }
        if (x.length != y.length || x.length != z.length || x.length != intensity.length) {
            throw new IllegalArgumentException(
                "Point cloud arrays must have equal length. x=" + x.length +
                " y=" + y.length + " z=" + z.length + " intensity=" + intensity.length);
        }
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
        if (index < 0 || index >= x.length) {
            throw new IndexOutOfBoundsException(
                "Point index " + index + " out of bounds for size " + x.length);
        }
        return new Point3D(x[index], y[index], z[index], intensity[index]);
    }
}
