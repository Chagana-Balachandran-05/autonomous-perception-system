package com.moderndaytech.perception.detection;

public class Position3D {
    private final double x;
    private final double y;
    private final double z;

    public Position3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    @Override
    public String toString() {
        return String.format("Position3D[x=%.2f, y=%.2f, z=%.2f]", x, y, z);
    }
}
