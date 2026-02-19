package com.moderndaytech.perception.fusion;

import com.moderndaytech.perception.sensor.SensorData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorFusionProcessorTest {

    @Mock
    private FusionAlgorithm mockAlgorithm;

    @Test
    void testProcessor_WithMockAlgorithm_DelegatesToAlgorithm() {
        SensorData sensor = mock(SensorData.class);
        when(sensor.getTimestamp()).thenReturn(1_000L);

        List<SensorData> sensors = List.of(sensor);
        FusionResult expectedResult = new FusionResult("Mock", 100, 0.95, 1);

        when(mockAlgorithm.getName()).thenReturn("MockAlgorithm");
        when(mockAlgorithm.fuse(anyList())).thenReturn(expectedResult);

        SensorFusionProcessor processor = new SensorFusionProcessor(mockAlgorithm);

        FusionResult result = processor.processSensors(sensors);

        assertThat(result).isSameAs(expectedResult);
        verify(mockAlgorithm).fuse(anyList());
    }
}
