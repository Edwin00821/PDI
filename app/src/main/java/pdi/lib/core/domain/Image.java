package pdi.lib.core.domain;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Domain entity representing an image in the PDI system.
 * This is the core business object that encapsulates all image-related data
 * and provides a clean interface for image operations.
 * 
 * Following Clean Architecture principles, this entity should be independent
 * of any framework or external library concerns.
 */
public class Image {
  private final String id;
  private final String originalFileName;
  private final BufferedImage imageData;
  private final ImageMetadata metadata;

  /**
   * Creates a new Image instance.
   * 
   * @param id               Unique identifier for this image
   * @param originalFileName Original name of the file when loaded
   * @param imageData        The actual image data as BufferedImage
   * @param metadata         Additional metadata about the image
   * @throws IllegalArgumentException if any required parameter is null
   */
  public Image(String id, String originalFileName, BufferedImage imageData, ImageMetadata metadata) {
    this.id = Objects.requireNonNull(id, "Image ID cannot be null");
    this.originalFileName = Objects.requireNonNull(originalFileName, "Original filename cannot be null");
    this.imageData = Objects.requireNonNull(imageData, "Image data cannot be null");
    this.metadata = Objects.requireNonNull(metadata, "Image metadata cannot be null");
  }

  // Getters
  public String getId() {
    return id;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public BufferedImage getImageData() {
    return imageData;
  }

  public ImageMetadata getMetadata() {
    return metadata;
  }

  // Convenience methods for common operations
  public int getWidth() {
    return imageData.getWidth();
  }

  public int getHeight() {
    return imageData.getHeight();
  }

  public int getType() {
    return imageData.getType();
  }

  /**
   * Creates a deep copy of this image.
   * Useful for non-destructive operations or backup purposes.
   * 
   * @return A new Image instance with copied data
   */
  public Image copy() {
    BufferedImage copiedImage = new BufferedImage(
        imageData.getWidth(),
        imageData.getHeight(),
        imageData.getType());
    copiedImage.getGraphics().drawImage(imageData, 0, 0, null);

    return new Image(
        this.id + "_copy",
        this.originalFileName,
        copiedImage,
        this.metadata);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    Image image = (Image) obj;
    return Objects.equals(id, image.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return String.format("Image{id='%s', fileName='%s', dimensions=%dx%d}",
        id, originalFileName, getWidth(), getHeight());
  }
}
