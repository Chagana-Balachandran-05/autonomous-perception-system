package com.moderndaytech.perception.dataset;

import com.moderndaytech.perception.security.SecurityValidator;
import com.moderndaytech.perception.sensor.LiDARSensorData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ONCEDatasetLoaderTest {

    private ONCEDatasetLoader loader;

    @BeforeEach
    void setup() {
        loader = new ONCEDatasetLoader(new SecurityValidator());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/test-scenes/vehicle_scene.json",
        "/test-scenes/pedestrian_scene.json",
        "/test-scenes/mixed_scene.json",
        "/test-scenes/empty_scene.json"
    })
    void testLoadAnnotation_AllScenes_ParseSuccessfully(String scenePath) throws Exception {
        InputStream stream = getClass().getResourceAsStream(scenePath);
        assertThat(stream).as("Scene file must exist: " + scenePath).isNotNull();

        ONCESceneAnnotation annotation = loader.loadAnnotation(stream,
            scenePath.replace("/test-scenes/", ""));

        assertThat(annotation.getSceneId()).isNotBlank();
        assertThat(annotation.getLidarPoints()).isGreaterThan(0);
        assertThat(annotation.getObjectNames()).hasSize(annotation.getAnnotationCount());
        assertThat(annotation.getBoxes3d()).hasSize(annotation.getAnnotationCount());
    }

    @Test
    void testLoadAnnotation_VehicleScene_ContainsTwoCars() throws Exception {
        InputStream stream = getClass().getResourceAsStream(
            "/test-scenes/vehicle_scene.json");
        ONCESceneAnnotation annotation = loader.loadAnnotation(stream, "vehicle_scene.json");

        long carCount = annotation.getObjectNames().stream()
            .filter(name -> name.equalsIgnoreCase("Car"))
            .count();

        assertThat(carCount).isEqualTo(2);
        assertThat(annotation.getLidarPoints()).isEqualTo(85432);
    }

    @Test
    void testLoadAnnotation_EmptyScene_ZeroAnnotations() throws Exception {
        InputStream stream = getClass().getResourceAsStream(
            "/test-scenes/empty_scene.json");
        ONCESceneAnnotation annotation = loader.loadAnnotation(stream, "empty_scene.json");

        assertThat(annotation.getAnnotationCount()).isEqualTo(0);
        assertThat(annotation.getObjectNames()).isEmpty();
    }

    @Test
    void testLoadAnnotation_BoxFormat_SevenValuesPerBox() throws Exception {
        InputStream stream = getClass().getResourceAsStream(
            "/test-scenes/vehicle_scene.json");
        ONCESceneAnnotation annotation = loader.loadAnnotation(stream, "vehicle_scene.json");

        for (double[] box : annotation.getBoxes3d()) {
            assertThat(box).as("Each box_3d must have 7 values [x,y,z,l,w,h,yaw]")
                .hasSize(7);
        }
    }

    @Test
    void testBuildLiDARSensor_PointCountMatchesAnnotation() throws Exception {
        InputStream stream = getClass().getResourceAsStream(
            "/test-scenes/mixed_scene.json");
        ONCESceneAnnotation annotation = loader.loadAnnotation(stream, "mixed_scene.json");
        LiDARSensorData lidar = loader.buildLiDARSensor(annotation);

        assertThat(lidar.getPointCount()).isEqualTo(annotation.getLidarPoints());
        assertThat(lidar.isValid()).isTrue();
        assertThat(lidar.getSensorId()).contains(annotation.getSceneId());
    }

    @Test
    void testBuildLiDARSensor_LargePointCloud_IsWithinMemoryBounds() throws Exception {
        InputStream stream = getClass().getResourceAsStream(
            "/test-scenes/mixed_scene.json");
        ONCESceneAnnotation annotation = loader.loadAnnotation(stream, "mixed_scene.json");

        securityValidator_accept_reasonable_size(annotation.getLidarPoints());

        LiDARSensorData lidar = loader.buildLiDARSensor(annotation);
        assertThat(lidar.getPointCount()).isGreaterThan(100_000);
    }

    @Test
    void testONCEAnnotation_NamesAndBoxesMustMatch_ThrowsOnMismatch() {
        assertThatThrownBy(() -> new ONCESceneAnnotation(
            "test", "frame1", 1000L, 5000,
            java.util.List.of("Car"),
            java.util.List.of(new double[7], new double[7])
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("equal length");
    }

    @Test
    void testLoader_WithNullSecurityValidator_ThrowsOnConstruction() {
        assertThatThrownBy(() -> new ONCEDatasetLoader(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SecurityValidator cannot be null");
    }

    private void securityValidator_accept_reasonable_size(int pointCount) {
        new SecurityValidator().validateDataSize(pointCount * 4);
    }
}
