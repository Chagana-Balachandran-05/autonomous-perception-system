package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import java.util.List;

public class MockFusionAlgorithm implements FusionAlgorithm {

    private final FusionResult fixedResult;

    public MockFusionAlgorithm(FusionResult fixedResult) {
        this.fixedResult = fixedResult;
    }

    @Override
    public FusionResult fuse(List<SensorData> sensorData) {
        return fixedResult;
    }

    @Override
    public String getName() {
        return "MockFusionAlgorithm";
    }
}
