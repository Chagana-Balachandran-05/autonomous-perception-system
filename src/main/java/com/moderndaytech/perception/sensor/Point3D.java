package com.moderndaytech.perception.sensor;

public class Point3D {
    private final float x;
    private final float y;
    private final float z;
    private final float intensity;

    public Point3D(float x, float y, float z, float intensity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.intensity = intensity;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public float getIntensity() { return intensity; }
}
