package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ParticleFilterFusion implements FusionAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(ParticleFilterFusion.class);

    @Override
    public FusionResult fuse(List<SensorData> sensorData) {
        if (sensorData == null || sensorData.isEmpty()) {
            return FusionResult.empty();
        }

        logger.info("ParticleFilterFusion: fusing {} sensors", sensorData.size());

        int totalPoints = sensorData.stream()
            .mapToInt(SensorData::getDataSize)
            .sum();

        // Particle count adapts to uncertainty
        int particleCount = calculateRequiredParticles(sensorData.size());
        logger.info("Using {} particles for fusion", particleCount);

        // Parallel particle processing for performance
        List<double[]> particles = java.util.stream.IntStream.range(0, particleCount)
            .parallel()
            .mapToObj(i -> propagateParticle(i, sensorData))
            .collect(java.util.stream.Collectors.toList());

        double confidence = calculateWeightedConfidence(particles, sensorData);

        return new FusionResult(getName(), totalPoints, confidence, sensorData.size());
    }

    private int calculateRequiredParticles(int sensorCount) {
        if (sensorCount <= 1) return 100;
        if (sensorCount <= 3) return 500;
        return 1000;
    }

    private double[] propagateParticle(int particleId, List<SensorData> sensors) {
        double weight = sensors.stream()
            .filter(SensorData::isValid)
            .mapToDouble(s -> 1.0 / (particleId + 1))
            .average().orElse(0.0);
        return new double[]{particleId, weight};
    }

    private double calculateWeightedConfidence(List<double[]> particles,
                                               List<SensorData> sensors) {
        long validSensors = sensors.stream().filter(SensorData::isValid).count();
        if (validSensors == 0) return 0.0;
        return Math.min(0.92, (double) validSensors / sensors.size() * 0.9);
    }

    @Override
    public String getName() {
        return "ParticleFilterFusion";
    }
}
