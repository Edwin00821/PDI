package pdi.lib.color.infrastructure;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Map;

import pdi.lib.core.domain.Image;
import pdi.lib.core.domain.ImageMetadata;

import pdi.lib.color.domain.ColorOperation;

/**
 * Implementation for adjusting image brightness.
 * 
 * This operation modifies the brightness of an image by adding or subtracting
 * a constant value to each color channel of every pixel.
 * 
 * Brightness adjustment is a linear operation where:
 * - Positive values make the image brighter
 * - Negative values make the image darker
 * - Values are clamped to the valid range [0, 255]
 */
public class BrightnessOperation implements ColorOperation {

  private static final String OPERATION_NAME = "Brightness Adjustment";
  private static final String[] EXPECTED_PARAMETERS = { "level" };

  // Valid brightness range (-255 to +255)
  private static final int MIN_BRIGHTNESS = -255;
  private static final int MAX_BRIGHTNESS = 255;

  @Override
  public Image apply(Image image, Map<String, Object> parameters) {
    if (image == null) {
      throw new IllegalArgumentException("Image cannot be null");
    }

    // Extract and validate brightness level parameter
    int brightnessLevel = parseBrightnessParameter(parameters);

    BufferedImage sourceImage = image.getImageData();
    BufferedImage adjustedImage = new BufferedImage(
        sourceImage.getWidth(),
        sourceImage.getHeight(),
        sourceImage.getType());

    // Process each pixel to adjust brightness
    for (int y = 0; y < sourceImage.getHeight(); y++) {
      for (int x = 0; x < sourceImage.getWidth(); x++) {
        int rgb = sourceImage.getRGB(x, y);
        int adjustedRGB = adjustBrightness(rgb, brightnessLevel);
        adjustedImage.setRGB(x, y, adjustedRGB);
      }
    }

    // Create new metadata for the brightness-adjusted image
    ImageMetadata newMetadata = new ImageMetadata(
        image.getMetadata().getFormat(),
        image.getMetadata().getFileSizeBytes(), // Approximation
        image.getMetadata().getBitDepth(),
        LocalDateTime.now(),
        image.getMetadata().getColorSpace());

    // Generate new ID for the processed image
    String newId = image.getId() + "_brightness_" + brightnessLevel + "_" + System.currentTimeMillis();

    return new Image(
        newId,
        image.getOriginalFileName(),
        adjustedImage,
        newMetadata);
  }

  /**
   * Parses and validates the brightness level parameter from the parameters map.
   * 
   * @param parameters The parameters map containing the brightness level
   * @return The parsed brightness level
   * @throws IllegalArgumentException if brightness parameter is invalid
   */
  private int parseBrightnessParameter(Map<String, Object> parameters) {
    Object levelParam = parameters.get("level");

    if (levelParam == null) {
      throw new IllegalArgumentException("Brightness level parameter is required");
    }

    int brightnessLevel;
    if (levelParam instanceof Integer) {
      brightnessLevel = (Integer) levelParam;
    } else if (levelParam instanceof Number) {
      brightnessLevel = ((Number) levelParam).intValue();
    } else if (levelParam instanceof String) {
      try {
        brightnessLevel = Integer.parseInt((String) levelParam);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            String.format("Invalid brightness level '%s'. Must be a number.", levelParam));
      }
    } else {
      throw new IllegalArgumentException(
          String.format("Brightness level must be a number, got: %s",
              levelParam.getClass().getSimpleName()));
    }

    // Validate range
    if (brightnessLevel < MIN_BRIGHTNESS || brightnessLevel > MAX_BRIGHTNESS) {
      throw new IllegalArgumentException(
          String.format("Brightness level must be between %d and %d, got: %d",
              MIN_BRIGHTNESS, MAX_BRIGHTNESS, brightnessLevel));
    }

    return brightnessLevel;
  }

  /**
   * Adjusts the brightness of a single RGB pixel.
   * 
   * @param rgb             The original RGB value as an integer
   * @param brightnessLevel The brightness adjustment level (-255 to +255)
   * @return The brightness-adjusted RGB value
   */
  private int adjustBrightness(int rgb, int brightnessLevel) {
    // Extract individual color components
    int alpha = (rgb >> 24) & 0xFF;
    int red = (rgb >> 16) & 0xFF;
    int green = (rgb >> 8) & 0xFF;
    int blue = rgb & 0xFF;

    // Adjust each channel and clamp to valid range [0, 255]
    int adjustedRed = clamp(red + brightnessLevel);
    int adjustedGreen = clamp(green + brightnessLevel);
    int adjustedBlue = clamp(blue + brightnessLevel);

    // Reconstruct the RGB value
    return (alpha << 24) | (adjustedRed << 16) | (adjustedGreen << 8) | adjustedBlue;
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
      parseBrightnessParameter(parameters);
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
   * Utility method to create parameters for brightness adjustment.
   * 
   * @param level The brightness level (-255 to +255)
   * @return A Map with the correct parameter structure
   */
  public static Map<String, Object> createParameters(int level) {
    if (level < MIN_BRIGHTNESS || level > MAX_BRIGHTNESS) {
      throw new IllegalArgumentException(
          String.format("Brightness level must be between %d and %d",
              MIN_BRIGHTNESS, MAX_BRIGHTNESS));
    }
    return Map.of("level", level);
  }
}
