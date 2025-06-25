package pdi.lib.color.infrastructure;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Map;

import pdi.lib.core.domain.Image;
import pdi.lib.core.domain.ImageMetadata;

import pdi.lib.color.domain.ColorOperation;

/**
 * Implementation for extracting individual RGB channels from a color image.
 * 
 * This operation extracts a specific color channel (Red, Green, or Blue)
 * and creates an image showing only that channel in its original color.
 * 
 * The extracted channel is displayed where:
 * - The selected channel maintains its original intensity
 * - Other channels are set to zero (black)
 * - This creates a colored image showing only the selected channel
 */
public class RGBExtractionOperation implements ColorOperation {

  private static final String OPERATION_NAME = "RGB Channel Extraction";
  private static final String[] EXPECTED_PARAMETERS = { "channel" };

  // Valid channel options
  public enum Channel {
    RED, GREEN, BLUE
  }

  @Override
  public Image apply(Image image, Map<String, Object> parameters) {
    if (image == null) {
      throw new IllegalArgumentException("Image cannot be null");
    }

    // Extract and validate channel parameter
    Channel channel = parseChannelParameter(parameters);

    BufferedImage sourceImage = image.getImageData();
    // Use TYPE_INT_RGB to maintain color information
    BufferedImage extractedImage = new BufferedImage(
        sourceImage.getWidth(),
        sourceImage.getHeight(),
        BufferedImage.TYPE_INT_RGB);

    // Process each pixel to extract the specified channel
    for (int y = 0; y < sourceImage.getHeight(); y++) {
      for (int x = 0; x < sourceImage.getWidth(); x++) {
        int rgb = sourceImage.getRGB(x, y);
        int newRGB = extractChannelAsColor(rgb, channel);
        extractedImage.setRGB(x, y, newRGB);
      }
    }

    // Create new metadata for the extracted channel image
    ImageMetadata newMetadata = new ImageMetadata(
        image.getMetadata().getFormat(),
        image.getMetadata().getFileSizeBytes(), // Approximation
        24, // RGB is 24-bit (8 bits per channel)
        LocalDateTime.now(),
        channel.name() + "_CHANNEL");

    // Generate new ID for the processed image
    String newId = image.getId() + "_" + channel.name().toLowerCase() + "_" + System.currentTimeMillis();

    return new Image(
        newId,
        image.getOriginalFileName(),
        extractedImage,
        newMetadata);
  }

  /**
   * Parses and validates the channel parameter from the parameters map.
   * 
   * @param parameters The parameters map containing the channel specification
   * @return The parsed Channel enum value
   * @throws IllegalArgumentException if channel parameter is invalid
   */
  private Channel parseChannelParameter(Map<String, Object> parameters) {
    Object channelParam = parameters.get("channel");

    if (channelParam == null) {
      throw new IllegalArgumentException("Channel parameter is required");
    }

    if (channelParam instanceof Channel) {
      return (Channel) channelParam;
    }

    if (channelParam instanceof String) {
      try {
        return Channel.valueOf(((String) channelParam).toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format("Invalid channel '%s'. Valid options: RED, GREEN, BLUE", channelParam));
      }
    }

    throw new IllegalArgumentException(
        String.format("Channel parameter must be a String or Channel enum, got: %s",
            channelParam.getClass().getSimpleName()));
  }

  /**
   * Extracts the specified color channel and creates a colored RGB value.
   * Only the selected channel retains its value, others are set to 0.
   * 
   * @param rgb     The original RGB value as an integer
   * @param channel The channel to extract
   * @return The new RGB value with only the selected channel active
   */
  private int extractChannelAsColor(int rgb, Channel channel) {
    int red = (rgb >> 16) & 0xFF;
    int green = (rgb >> 8) & 0xFF;
    int blue = rgb & 0xFF;

    switch (channel) {
      case RED:
        // Keep only red channel, set green and blue to 0
        return (red << 16) | (0 << 8) | 0;
      case GREEN:
        // Keep only green channel, set red and blue to 0
        return (0 << 16) | (green << 8) | 0;
      case BLUE:
        // Keep only blue channel, set red and green to 0
        return (0 << 16) | (0 << 8) | blue;
      default:
        throw new IllegalArgumentException("Unsupported channel: " + channel);
    }
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
      parseChannelParameter(parameters);
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
   * Utility method to create parameters for RGB extraction.
   * 
   * @param channel The channel to extract
   * @return A Map with the correct parameter structure
   */
  public static Map<String, Object> createParameters(Channel channel) {
    return Map.of("channel", channel);
  }

  /**
   * Utility method to create parameters for RGB extraction using string.
   * 
   * @param channelName The name of the channel ("RED", "GREEN", or "BLUE")
   * @return A Map with the correct parameter structure
   */
  public static Map<String, Object> createParameters(String channelName) {
    return Map.of("channel", channelName.toUpperCase());
  }
}
