package com.moderndaytech.perception.detection;

/**
 * Represents a position in 3D space.
 * Stores x, y, and z coordinates.
 * If you need to know where something is, this is the class for it.
 */
public class Position3D {
    private final double x, y, z;

    public Position3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}
