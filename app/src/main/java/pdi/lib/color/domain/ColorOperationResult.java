package pdi.lib.color.domain;

import pdi.lib.core.domain.Image;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;

/**
 * Result object for color processing operations.
 * 
 * Contains all relevant information about a completed color operation,
 * including the original and processed images, operation metadata,
 * and performance metrics.
 * 
 * This class is immutable and provides a complete record of the operation
 * for tracking, debugging, and potential undo functionality.
 */
public class ColorOperationResult {

  private final Image originalImage;
  private final Image processedImage;
  private final String operationName;
  private final Map<String, Object> parameters;
  private final LocalDateTime processedAt;
  private final long processingTimeMs;

  /**
   * Creates a new color operation result.
   * 
   * @param originalImage    The original image before processing
   * @param processedImage   The image after processing
   * @param operationName    Name of the operation that was applied
   * @param parameters       Parameters used for the operation
   * @param processedAt      Timestamp when processing occurred
   * @param processingTimeMs Time taken to process in milliseconds
   */
  public ColorOperationResult(Image originalImage,
      Image processedImage,
      String operationName,
      Map<String, Object> parameters,
      LocalDateTime processedAt,
      long processingTimeMs) {
    this.originalImage = Objects.requireNonNull(originalImage, "Original image cannot be null");
    this.processedImage = Objects.requireNonNull(processedImage, "Processed image cannot be null");
    this.operationName = Objects.requireNonNull(operationName, "Operation name cannot be null");
    this.parameters = Collections.unmodifiableMap(
        Objects.requireNonNull(parameters, "Parameters cannot be null"));
    this.processedAt = Objects.requireNonNull(processedAt, "Processed timestamp cannot be null");
    this.processingTimeMs = processingTimeMs;

    if (processingTimeMs < 0) {
      throw new IllegalArgumentException("Processing time cannot be negative");
    }
  }

  /**
   * Gets the original image before processing.
   */
  public Image getOriginalImage() {
    return originalImage;
  }

  /**
   * Gets the processed image result.
   */
  public Image getProcessedImage() {
    return processedImage;
  }

  /**
   * Gets the name of the operation that was applied.
   */
  public String getOperationName() {
    return operationName;
  }

  /**
   * Gets the parameters used for the operation.
   */
  public Map<String, Object> getParameters() {
    return parameters;
  }

  /**
   * Gets the timestamp when processing occurred.
   */
  public LocalDateTime getProcessedAt() {
    return processedAt;
  }

  /**
   * Gets the processing time in milliseconds.
   */
  public long getProcessingTimeMs() {
    return processingTimeMs;
  }

  /**
   * Gets the processing time in seconds.
   */
  public double getProcessingTimeSeconds() {
    return processingTimeMs / 1000.0;
  }

  /**
   * Checks if the operation was successful (has both original and processed
   * images).
   */
  public boolean isSuccessful() {
    return originalImage != null && processedImage != null;
  }

  /**
   * Gets a human-readable summary of the operation.
   */
  public String getSummary() {
    return String.format("%s applied in %d ms", operationName, processingTimeMs);
  }

  @Override
  public String toString() {
    return String.format("ColorOperationResult{operation='%s', processingTime=%dms, processedAt=%s}",
        operationName, processingTimeMs, processedAt);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;

    ColorOperationResult that = (ColorOperationResult) obj;
    return processingTimeMs == that.processingTimeMs &&
        Objects.equals(originalImage.getId(), that.originalImage.getId()) &&
        Objects.equals(processedImage.getId(), that.processedImage.getId()) &&
        Objects.equals(operationName, that.operationName) &&
        Objects.equals(parameters, that.parameters) &&
        Objects.equals(processedAt, that.processedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(originalImage.getId(), processedImage.getId(),
        operationName, parameters, processedAt, processingTimeMs);
  }
}
