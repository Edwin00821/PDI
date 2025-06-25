package pdi.lib.color.infrastructure;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Map;

import pdi.lib.core.domain.Image;
import pdi.lib.core.domain.ImageMetadata;

import pdi.lib.color.domain.ColorOperation;

/**
 * Implementation of grayscale conversion using the luminance method.
 * 
 * This operation converts a color image to grayscale using the standard
 * luminance formula: Gray = 0.299*R + 0.587*G + 0.114*B
 * 
 * This formula accounts for human eye sensitivity to different colors,
 * producing more natural-looking grayscale images than simple averaging.
 */
public class GrayscaleOperation implements ColorOperation {

  private static final String OPERATION_NAME = "Grayscale Conversion";
  private static final String[] EXPECTED_PARAMETERS = {}; // No parameters needed

  // Luminance coefficients based on human eye sensitivity
  private static final double RED_COEFFICIENT = 0.299;
  private static final double GREEN_COEFFICIENT = 0.587;
  private static final double BLUE_COEFFICIENT = 0.114;

  @Override
  public Image apply(Image image, Map<String, Object> parameters) {
    if (image == null) {
      throw new IllegalArgumentException("Image cannot be null");
    }

    BufferedImage sourceImage = image.getImageData();
    BufferedImage grayscaleImage = new BufferedImage(
        sourceImage.getWidth(),
        sourceImage.getHeight(),
        BufferedImage.TYPE_INT_RGB);

    // Process each pixel
    for (int y = 0; y < sourceImage.getHeight(); y++) {
      for (int x = 0; x < sourceImage.getWidth(); x++) {
        int rgb = sourceImage.getRGB(x, y);
        int grayValue = convertToGrayscale(rgb);

        // Create grayscale RGB value (same value for R, G, B)
        int grayRGB = (grayValue << 16) | (grayValue << 8) | grayValue;
        grayscaleImage.setRGB(x, y, grayRGB);
      }
    }

    // Create new metadata for the grayscale image
    ImageMetadata newMetadata = new ImageMetadata(
        image.getMetadata().getFormat(),
        image.getMetadata().getFileSizeBytes(), // Approximation
        8, // Grayscale is typically 8-bit
        LocalDateTime.now(),
        "GRAYSCALE");

    // Generate new ID for the processed image
    String newId = image.getId() + "_grayscale_" + System.currentTimeMillis();

    return new Image(
        newId,
        image.getOriginalFileName(),
        grayscaleImage,
        newMetadata);
  }

  /**
   * Converts an RGB value to grayscale using the luminance formula.
   * 
   * @param rgb The RGB value as an integer
   * @return The grayscale value (0-255)
   */
  private int convertToGrayscale(int rgb) {
    // Extract individual color components
    int red = (rgb >> 16) & 0xFF;
    int green = (rgb >> 8) & 0xFF;
    int blue = rgb & 0xFF;

    // Apply luminance formula and clamp to valid range
    double grayValue = (RED_COEFFICIENT * red) +
        (GREEN_COEFFICIENT * green) +
        (BLUE_COEFFICIENT * blue);

    return Math.max(0, Math.min(255, (int) Math.round(grayValue)));
  }

  @Override
  public String getOperationName() {
    return OPERATION_NAME;
  }

  @Override
  public boolean isValidParameters(Map<String, Object> parameters) {
    // Grayscale conversion doesn't need any parameters
    return parameters != null;
  }

  @Override
  public String[] getExpectedParameters() {
    return EXPECTED_PARAMETERS.clone(); // Defensive copy
  }
}
