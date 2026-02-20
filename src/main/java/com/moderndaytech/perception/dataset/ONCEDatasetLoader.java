package com.moderndaytech.perception.dataset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moderndaytech.perception.security.SecurityValidator;
import com.moderndaytech.perception.sensor.LiDARSensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Loads and parses ONCE dataset annotation files into system sensor objects.
 *
 * <p>The ONCE dataset (Mao et al., 2021) provides autonomous driving scenes
 * with LiDAR point counts and 3D bounding box annotations. This loader parses
 * the JSON annotation format and converts it into the system's internal
 * {@link LiDARSensorData} and {@link ONCESceneAnnotation} representations.</p>
 *
 * <h2>SOLID Principles Applied:</h2>
 * <ul>
 *   <li><strong>SRP</strong>: This class has one responsibility — loading and
 *       parsing ONCE dataset files. It does not perform detection or fusion.</li>
 *   <li><strong>DIP</strong>: Depends on {@link SecurityValidator} abstraction
 *       for input validation before processing external data.</li>
 * </ul>
 *
 * <h2>Large Dataset Design:</h2>
 * <p>LiDAR point clouds in the ONCE dataset contain 60,000–120,000 points per
 * frame. This loader uses primitive {@code float[]} arrays (not {@code Float[]})
 * to minimise memory overhead — a 100,000-point cloud stored as float[] uses
 * 400KB vs approximately 2.4MB using Float[] with object boxing overhead.</p>
 *
 * @author Chagana Balachandran
 */
public class ONCEDatasetLoader {

    private static final Logger logger = LoggerFactory.getLogger(ONCEDatasetLoader.class);
    private static final RandomGenerator random = new java.util.SplittableRandom(42);

    private final SecurityValidator securityValidator;

    /**
     * DIP: SecurityValidator injected, not instantiated internally.
     */
    public ONCEDatasetLoader(SecurityValidator securityValidator) {
        if (securityValidator == null) {
            throw new IllegalArgumentException("SecurityValidator cannot be null");
        }
        this.securityValidator = securityValidator;
    }

    /**
     * Parses an ONCE annotation JSON file from the given input stream.
     *
     * @param inputStream the JSON annotation file stream
     * @param sourceName  used for logging and validation (e.g. filename)
     * @return parsed scene annotation
     * @throws IllegalArgumentException if the JSON is missing required fields
     * @throws IOException              if the stream cannot be read
     */
    public ONCESceneAnnotation loadAnnotation(InputStream inputStream,
                                              String sourceName) throws IOException {
        securityValidator.validateSensorId(sourceName.replace(".json", "").replace("/", "-"));

        logger.info("Loading ONCE annotation from: {}", sanitizeForLog(sourceName));

        try (InputStreamReader reader = new InputStreamReader(
                inputStream, StandardCharsets.UTF_8)) {

            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            String sceneId = getRequiredString(root, "scene_id", sourceName);
            String frameId = getRequiredString(root, "frame_id", sourceName);
            long timestamp = root.has("timestamp")
                ? root.get("timestamp").getAsLong()
                : System.currentTimeMillis();
            int lidarPoints = getRequiredInt(root, "lidar_points", sourceName);

            JsonObject annos = root.getAsJsonObject("annos");
            List<String> names = new ArrayList<>();
            List<double[]> boxes = new ArrayList<>();

            if (annos != null) {
                JsonArray namesArr = annos.getAsJsonArray("names");
                JsonArray boxesArr = annos.getAsJsonArray("boxes_3d");

                if (namesArr != null && boxesArr != null) {
                    for (JsonElement el : namesArr) {
                        names.add(el.getAsString());
                    }
                    for (JsonElement el : boxesArr) {
                        JsonArray box = el.getAsJsonArray();
                        double[] b = new double[7];
                        for (int i = 0; i < Math.min(7, box.size()); i++) {
                            b[i] = box.get(i).getAsDouble();
                        }
                        boxes.add(b);
                    }
                }
            }

            ONCESceneAnnotation annotation = new ONCESceneAnnotation(
                sceneId, frameId, timestamp, lidarPoints, names, boxes);

            logger.info("Loaded scene {}: {} LiDAR points, {} annotated objects",
                sanitizeForLog(sceneId),
                sanitizeForLog(lidarPoints),
                sanitizeForLog(annotation.getAnnotationCount()));

            return annotation;
        }
    }

    /**
     * Converts an ONCE annotation into a {@link LiDARSensorData} object
     * by generating a synthetic point cloud scaled to the annotation's
     * declared point count.
     *
     * <p>In the real ONCE dataset, binary .bin files hold the raw point clouds.
     * Those files are not included in this repository due to size (each frame
     * is 4–6MB). Instead, we generate a statistically representative point
     * cloud with the correct number of points, using a fixed random seed for
     * reproducibility. The annotation metadata (object positions, class labels)
     * is real ONCE-format data parsed from the JSON files.</p>
     *
     * @param annotation parsed ONCE annotation
     * @return LiDARSensorData with point count matching the annotation
     */
    public LiDARSensorData buildLiDARSensor(ONCESceneAnnotation annotation) {
        int n = annotation.getLidarPoints();
        logger.info("Building LiDAR sensor with {} points for scene {}",
            sanitizeForLog(n),
            sanitizeForLog(annotation.getSceneId()));

        float[] x = new float[n];
        float[] y = new float[n];
        float[] z = new float[n];
        float[] intensity = new float[n];

        for (int i = 0; i < n; i++) {
            x[i] = (float) ((random.nextDouble() - 0.5) * 100.0);
            y[i] = (float) (random.nextDouble() * 80.0);
            z[i] = (float) (random.nextDouble() * 5.0);
            intensity[i] = (float) (random.nextDouble() * 255.0);
        }

        return new LiDARSensorData(
            annotation.getTimestamp(),
            "ONCE-LIDAR-" + annotation.getSceneId(),
            x, y, z, intensity
        );
    }

    private String getRequiredString(JsonObject obj, String field, String source) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            throw new IllegalArgumentException(
                "Required field '" + field + "' missing in: " + source);
        }
        return obj.get(field).getAsString();
    }

    private int getRequiredInt(JsonObject obj, String field, String source) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            throw new IllegalArgumentException(
                "Required field '" + field + "' missing in: " + source);
        }
        return obj.get(field).getAsInt();
    }

    private String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\\r\\n\\t]", "_")
                    .substring(0, Math.min(input.length(), 100));
    }

    private String sanitizeForLog(long value) {
        return String.valueOf(value);
    }

    private String sanitizeForLog(int value) {
        return String.valueOf(value);
    }
}
