package pdi.lib.core.infrastructure;

import javax.imageio.ImageIO;

import pdi.lib.core.application.ImageLoader;
import pdi.lib.core.domain.Image;
import pdi.lib.core.domain.ImageMetadata;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of ImageLoader that loads images from files using Java's
 * ImageIO.
 * 
 * This implementation supports all formats that ImageIO can handle (typically
 * JPEG, PNG, GIF, BMP, WBMP). It automatically detects the image format and
 * extracts basic metadata during the loading process.
 */
public class FileImageLoader implements ImageLoader {

  // Supported file extensions - ImageIO typically supports these
  private static final String[] SUPPORTED_EXTENSIONS = {
      "jpg", "jpeg", "png", "gif", "bmp", "wbmp"
  };

  // Set for fast lookup
  private static final Set<String> SUPPORTED_EXTENSIONS_SET = new HashSet<>(Arrays.asList(SUPPORTED_EXTENSIONS));

  @Override
  public Optional<Image> loadFromFile(File file) {
    Objects.requireNonNull(file, "File cannot be null");

    if (!supportsFile(file)) {
      return Optional.empty();
    }

    try {
      // Load the image using ImageIO
      BufferedImage bufferedImage = ImageIO.read(file);

      if (bufferedImage == null) {
        // ImageIO returns null if it can't read the file
        return Optional.empty();
      }

      // Generate unique ID for this image
      String imageId = generateImageId(file);

      // Extract metadata
      ImageMetadata metadata = extractMetadata(file, bufferedImage);

      // Create domain object
      Image image = new Image(imageId, file.getName(), bufferedImage, metadata);

      return Optional.of(image);

    } catch (IOException e) {
      // Log the error in a real application
      System.err.println("Failed to load image from file: " + file.getPath() + " - " + e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public boolean supportsFile(File file) {
    if (file == null || !file.exists() || !file.isFile()) {
      return false;
    }

    String fileName = file.getName().toLowerCase();
    int lastDot = fileName.lastIndexOf('.');

    if (lastDot == -1) {
      return false; // No extension
    }

    String extension = fileName.substring(lastDot + 1);
    return SUPPORTED_EXTENSIONS_SET.contains(extension);
  }

  @Override
  public String[] getSupportedExtensions() {
    return SUPPORTED_EXTENSIONS.clone(); // Return copy to prevent modification
  }

  /**
   * Generates a unique identifier for the image based on file properties.
   * 
   * @param file The source file
   * @return Unique string identifier
   */
  private String generateImageId(File file) {
    // Simple ID generation - in production, consider using UUID
    return "img_" + file.getName() + "_" + System.currentTimeMillis();
  }

  /**
   * Extracts metadata from the file and loaded image.
   * 
   * @param file          The source file
   * @param bufferedImage The loaded image data
   * @return ImageMetadata object with extracted information
   */
  private ImageMetadata extractMetadata(File file, BufferedImage bufferedImage) {
    // Determine format from file extension
    String format = getFileExtension(file).toUpperCase();

    // Get file size
    long fileSize = file.length();

    // Determine bit depth from BufferedImage type
    int bitDepth = getBitDepth(bufferedImage);

    // Determine color space
    String colorSpace = getColorSpace(bufferedImage);

    return new ImageMetadata(
        format,
        fileSize,
        bitDepth,
        LocalDateTime.now(),
        colorSpace);
  }

  /**
   * Extracts file extension from filename.
   */
  private String getFileExtension(File file) {
    String fileName = file.getName();
    int lastDot = fileName.lastIndexOf('.');
    return lastDot == -1 ? "" : fileName.substring(lastDot + 1);
  }

  /**
   * Determines bit depth from BufferedImage type.
   */
  private int getBitDepth(BufferedImage image) {
    switch (image.getType()) {
      case BufferedImage.TYPE_BYTE_BINARY:
        return 1;
      case BufferedImage.TYPE_BYTE_GRAY:
        return 8;
      case BufferedImage.TYPE_3BYTE_BGR:
      case BufferedImage.TYPE_INT_RGB:
        return 24;
      case BufferedImage.TYPE_4BYTE_ABGR:
      case BufferedImage.TYPE_INT_ARGB:
        return 32;
      default:
        return 24; // Default assumption
    }
  }

  /**
   * Determines color space from BufferedImage type.
   */
  private String getColorSpace(BufferedImage image) {
    switch (image.getType()) {
      case BufferedImage.TYPE_BYTE_GRAY:
        return "GRAYSCALE";
      case BufferedImage.TYPE_3BYTE_BGR:
      case BufferedImage.TYPE_INT_RGB:
        return "RGB";
      case BufferedImage.TYPE_4BYTE_ABGR:
      case BufferedImage.TYPE_INT_ARGB:
        return "ARGB";
      default:
        return "RGB"; // Default assumption
    }
  }
}
