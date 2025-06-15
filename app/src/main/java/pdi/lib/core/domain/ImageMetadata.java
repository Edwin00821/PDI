package pdi.lib.core.domain;

import java.time.LocalDateTime;

/**
 * Value object containing metadata information about an image.
 * This includes technical details and processing history.
 * 
 * As a value object, it's immutable and equality is based on content.
 */
public class ImageMetadata {
  private final String format; // JPG, PNG, etc.
  private final long fileSizeBytes;
  private final int bitDepth;
  private final LocalDateTime loadedAt;
  private final String colorSpace; // RGB, GRAYSCALE, etc.

  /**
   * Creates image metadata.
   * 
   * @param format        Image format (e.g., "JPEG", "PNG")
   * @param fileSizeBytes Original file size in bytes
   * @param bitDepth      Color depth (8, 16, 24, 32)
   * @param loadedAt      When the image was loaded into the system
   * @param colorSpace    Color space representation
   */
  public ImageMetadata(String format, long fileSizeBytes, int bitDepth,
      LocalDateTime loadedAt, String colorSpace) {
    this.format = format;
    this.fileSizeBytes = fileSizeBytes;
    this.bitDepth = bitDepth;
    this.loadedAt = loadedAt;
    this.colorSpace = colorSpace;
  }

  // Getters
  public String getFormat() {
    return format;
  }

  public long getFileSizeBytes() {
    return fileSizeBytes;
  }

  public int getBitDepth() {
    return bitDepth;
  }

  public LocalDateTime getLoadedAt() {
    return loadedAt;
  }

  public String getColorSpace() {
    return colorSpace;
  }

  @Override
  public String toString() {
    return String.format("ImageMetadata{format='%s', size=%d bytes, depth=%d, colorSpace='%s'}",
        format, fileSizeBytes, bitDepth, colorSpace);
  }
}
