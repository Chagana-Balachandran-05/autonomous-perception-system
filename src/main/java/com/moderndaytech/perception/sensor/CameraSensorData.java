package com.moderndaytech.perception.sensor;

/**
 * Handles all the camera sensor data for the system.
 * This class extends SensorData and adds camera-specific details like resolution, brightness, and pixel data.
 *
 * All fields are private so nobody can mess with the internals unless they use the right methods.
 * If you want to create a new camera sensor, this is your starting point.
 */
public class CameraSensorData extends SensorData {
    // These fields hold all the specifics for the camera
    private final int imageWidth;
    private final int imageHeight;
    private final int brightness;
    private final String colorSpace;
    private final byte[] imageData;
    
    public CameraSensorData(long timestamp, String sensorId, 
                           int imageWidth, int imageHeight, byte[] imageData) {
        super(timestamp, sensorId, SensorType.CAMERA);
        
        // Quick sanity check on the image dimensions
        if (imageWidth <= 0 || imageHeight <= 0) {
            throw new IllegalArgumentException("Image dimensions must be positive!");
        }
        
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.imageData = imageData != null ? imageData : new byte[0];
        this.brightness = calculateBrightness();
        this.colorSpace = "RGB";
    }
    
    /**
     * Creates some realistic-looking camera data for testing purposes.
     * Just call this and you'll get a CameraSensorData object with typical values.
     */
    public static CameraSensorData createRealisticData(String sensorId) {
        long timestamp = System.currentTimeMillis();
        int width = 1920;
        int height = 1080;
        byte[] fakeImage = new byte[width * height * 3]; // RGB
        
        // Simulate image data
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        for (int i = 0; i < fakeImage.length; i++) {
            fakeImage[i] = (byte) (secureRandom.nextDouble() * 128 + 64); // Mid-range values
        }
        
        return new CameraSensorData(timestamp, sensorId, width, height, fakeImage);
    }
    
    @Override
    public boolean isValid() {
        // Data is valid if the image has positive dimensions, there’s data, and brightness is in range
        return imageWidth > 0 && 
               imageHeight > 0 && 
               imageData != null && 
               imageData.length > 0 &&
               brightness >= 0 && brightness <= 255;
    }
    
    @Override
    public String getMetricsReport() {
        return String.format(
            "Camera Metrics - Resolution: %dx%d, Brightness: %d, Size: %.2fMB",
            imageWidth, imageHeight, brightness, getDataSize() / (1024.0 * 1024.0)
        );
    }
    
    @Override
    public int getDataSize() {
        return imageData.length;
    }
    
    @Override
    protected String performSensorSpecificProcessing() {
        // Camera-specific processing: denoising, histogram equalization
        logger.debug("Processing {}x{} camera image", imageWidth, imageHeight);
        
        // Simulate processing
        int processedPixels = imageWidth * imageHeight;
        
        return String.format("Processed %d pixels, brightness adjusted to %d", 
            processedPixels, brightness);
    }
    
    // Controlled access to encapsulated data through getters
    public int getImageWidth() {
        return imageWidth;
    }
    
    public int getImageHeight() {
        return imageHeight;
    }
    
    public int getBrightness() {
        return brightness;
    }
    
    public String getColorSpace() {
        return colorSpace;
    }
    
    /**
     * Returns a COPY of image data to prevent external modification
     * This demonstrates deep encapsulation
     */
    public byte[] getImageData() {
        byte[] copy = new byte[imageData.length];
        System.arraycopy(imageData, 0, copy, 0, imageData.length);
        return copy;
    }
    
    /**
     * Get image statistics - demonstrates encapsulation with computed properties
     */
    public ImageStatistics getStatistics() {
        return new ImageStatistics(
            imageWidth,
            imageHeight,
            brightness,
            calculateContrast()
        );
    }
    
    // Private helper methods - implementation details hidden
    private int calculateBrightness() {
        if (imageData.length == 0) return 0;
        
        long sum = 0;
        int samples = Math.min(1000, imageData.length); // Sample for performance
        
        for (int i = 0; i < samples; i++) {
            sum += (imageData[i] & 0xFF);
        }
        
        return (int) (sum / samples);
    }
    
    private double calculateContrast() {
        if (imageData.length == 0) return 0.0;
        
        // Simplified contrast calculation
        int min = 255, max = 0;
        int samples = Math.min(1000, imageData.length);
        
        for (int i = 0; i < samples; i++) {
            int value = imageData[i] & 0xFF;
            if (value < min) min = value;
            if (value > max) max = value;
        }
        
        return max - min;
    }
}

/**
 * Immutable data class for image statistics
 * Demonstrates encapsulation at the data transfer level
 */
class ImageStatistics {
    private final int width;
    private final int height;
    private final int brightness;
    private final double contrast;
    
    public ImageStatistics(int width, int height, int brightness, double contrast) {
        this.width = width;
        this.height = height;
        this.brightness = brightness;
        this.contrast = contrast;
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getBrightness() { return brightness; }
    public double getContrast() { return contrast; }
    
    @Override
    public String toString() {
        return String.format("ImageStats[%dx%d, brightness=%d, contrast=%.1f]",
            width, height, brightness, contrast);
    }
}