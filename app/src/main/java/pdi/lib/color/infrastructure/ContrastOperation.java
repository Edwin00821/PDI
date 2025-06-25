package pdi.lib.color.infrastructure;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Map;

import pdi.lib.core.domain.Image;
import pdi.lib.core.domain.ImageMetadata;

import pdi.lib.color.domain.ColorOperation;

/**
 * Implementation for adjusting image contrast.
 * 
 * This operation modifies the contrast of an image by applying a multiplicative
 * factor to each color channel. The operation uses the formula:
 * newValue = factor * (oldValue - 128) + 128
 * 
 * This formula:
 * - Centers the adjustment around middle gray (128)
 * - Values > 1.0 increase contrast (darker darks, brighter brights)
 * - Values < 1.0 decrease contrast (everything moves toward middle gray)
 * - Factor of 1.0 leaves the image unchanged
 */
public class ContrastOperation implements ColorOperation {

  private static final String OPERATION_NAME = "Contrast Adjustment";
  private static final String[] EXPECTED_PARAMETERS = { "factor" };

  // Valid contrast range (0.0 to 3.0)
  private static final double MIN_CONTRAST = 0.0;
  private static final double MAX_CONTRAST = 3.0;
  private static final double MIDDLE_GRAY = 128.0;

  @Override
  public Image apply(Image image, Map<String, Object> parameters) {
    if (image == null) {
      throw new IllegalArgumentException("Image cannot be null");
    }

    // Extract and validate contrast factor parameter
    double contrastFactor = parseContrastParameter(parameters);

    BufferedImage sourceImage = image.getImageData();
    BufferedImage adjustedImage = new BufferedImage(
        sourceImage.getWidth(),
        sourceImage.getHeight(),
        sourceImage.getType());

    // Process each pixel to adjust contrast
    for (int y = 0; y < sourceImage.getHeight(); y++) {
      for (int x = 0; x < sourceImage.getWidth(); x++) {
        int rgb = sourceImage.getRGB(x, y);
        int adjustedRGB = adjustContrast(rgb, contrastFactor);
        adjustedImage.setRGB(x, y, adjustedRGB);
      }
    }

    // Create new metadata for the contrast-adjusted image
    ImageMetadata newMetadata = new ImageMetadata(
        image.getMetadata().getFormat(),
        image.getMetadata().getFileSizeBytes(), // Approximation
        image.getMetadata().getBitDepth(),
        LocalDateTime.now(),
        image.getMetadata().getColorSpace());

    // Generate new ID for the processed image
    String newId = image.getId() + "_contrast_" + contrastFactor + "_" + System.currentTimeMillis();

    return new Image(
        newId,
        image.getOriginalFileName(),
        adjustedImage,
        newMetadata);
  }

  /**
   * Parses and validates the contrast factor parameter from the parameters map.
   * 
   * @param parameters The parameters map containing the contrast factor
   * @return The parsed contrast factor
   * @throws IllegalArgumentException if contrast parameter is invalid
   */
  private double parseContrastParameter(Map<String, Object> parameters) {
    Object factorParam = parameters.get("factor");

    if (factorParam == null) {
      throw new IllegalArgumentException("Contrast factor parameter is required");
    }

    double contrastFactor;
    if (factorParam instanceof Double) {
      contrastFactor = (Double) factorParam;
    } else if (factorParam instanceof Float) {
      contrastFactor = ((Float) factorParam).doubleValue();
    } else if (factorParam instanceof Integer) {
      contrastFactor = ((Integer) factorParam).doubleValue();
    } else if (factorParam instanceof Number) {
      contrastFactor = ((Number) factorParam).doubleValue();
    } else if (factorParam instanceof String) {
      try {
        contrastFactor = Double.parseDouble((String) factorParam);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            String.format("Invalid contrast factor '%s'. Must be a number.", factorParam));
      }
    } else {
      throw new IllegalArgumentException(
          String.format("Contrast factor must be a number, got: %s",
              factorParam.getClass().getSimpleName()));
    }

    // Validate range
    if (contrastFactor < MIN_CONTRAST || contrastFactor > MAX_CONTRAST) {
      throw new IllegalArgumentException(
          String.format("Contrast factor must be between %.1f and %.1f, got: %.2f",
              MIN_CONTRAST, MAX_CONTRAST, contrastFactor));
    }

    return contrastFactor;
  }

  /**
   * Adjusts the contrast of a single RGB pixel.
   * 
   * Uses the formula: newValue = factor * (oldValue - 128) + 128
   * This centers the adjustment around middle gray (128).
   * 
   * @param rgb            The original RGB value as an integer
   * @param contrastFactor The contrast adjustment factor (0.0 to 3.0)
   * @return The contrast-adjusted RGB value
   */
  private int adjustContrast(int rgb, double contrastFactor) {
    // Extract individual color components
    int alpha = (rgb >> 24) & 0xFF;
    int red = (rgb >> 16) & 0xFF;
    int green = (rgb >> 8) & 0xFF;
    int blue = rgb & 0xFF;

    // Apply contrast adjustment formula to each channel
    int adjustedRed = applyContrastToChannel(red, contrastFactor);
    int adjustedGreen = applyContrastToChannel(green, contrastFactor);
    int adjustedBlue = applyContrastToChannel(blue, contrastFactor);

    // Reconstruct the RGB value
    return (alpha << 24) | (adjustedRed << 16) | (adjustedGreen << 8) | adjustedBlue;
  }

  /**
   * Applies contrast adjustment to a single color channel.
   * 
   * @param channelValue   The original channel value (0-255)
   * @param contrastFactor The contrast factor
   * @return The adjusted channel value, clamped to [0, 255]
   */
  private int applyContrastToChannel(int channelValue, double contrastFactor) {
    // Apply contrast formula: newValue = factor * (oldValue - 128) + 128
    double adjustedValue = contrastFactor * (channelValue - MIDDLE_GRAY) + MIDDLE_GRAY;

    // Clamp to valid range and round to integer
    return clamp((int) Math.round(adjustedValue));
  }

  /**
   * Clamps a value to the valid color range [0, 255].
   * 
   * @param value The value to clamp
   * @return The clamped value
   */
  private int clamp(int value) {
    return Math.max(0, Math.min(255, value));
  }

  @Override
  public String getOperationName() {
    return OPERATION_NAME;
  }

  @Override
  public boolean isValidParameters(Map<String, Object> parameters) {
    if (parameters == null) {
      return false;
    }

    try {
      parseContrastParameter(parameters);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public String[] getExpectedParameters() {
    return EXPECTED_PARAMETERS.clone(); // Defensive copy
  }

  /**
   * Utility method to create parameters for contrast adjustment.
   * 
   * @param factor The contrast factor (0.0 to 3.0)
   * @return A Map with the correct parameter structure
   */
  public static Map<String, Object> createParameters(double factor) {
    if (factor < MIN_CONTRAST || factor > MAX_CONTRAST) {
      throw new IllegalArgumentException(
          String.format("Contrast factor must be between %.1f and %.1f",
              MIN_CONTRAST, MAX_CONTRAST));
    }
    return Map.of("factor", factor);
  }
}
