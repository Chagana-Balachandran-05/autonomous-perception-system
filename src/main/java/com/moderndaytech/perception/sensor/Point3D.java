package com.moderndaytech.perception.sensor;

public class Point3D {
    public final double x, y, z;
    public final double intensity;

    public Point3D(double x, double y, double z, double intensity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.intensity = intensity;
    }

    @Override
    public String toString() {
        return String.format("Point3D(%.2f, %.2f, %.2f, i=%.1f)", x, y, z, intensity);
    }
}