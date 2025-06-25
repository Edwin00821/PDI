package pdi.lib.color.application;

import pdi.lib.color.domain.ColorOperation;
import pdi.lib.color.domain.ColorOperationResult;
import pdi.lib.core.domain.Image;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application service for coordinating color processing operations.
 * 
 * This service acts as the orchestrator for color operations, providing
 * a clean interface for the presentation layer while encapsulating the
 * business logic of operation execution and result tracking.
 * 
 * Following Clean Architecture, this service belongs to the application
 * layer and coordinates between domain objects and external concerns.
 */
public class ColorProcessorService {

  private static final Logger logger = Logger.getLogger(ColorProcessorService.class.getName());

  /**
   * Processes an image with the specified color operation and parameters.
   * 
   * This method handles the complete workflow of color processing:
   * 1. Validates inputs
   * 2. Executes the operation
   * 3. Measures performance
   * 4. Returns detailed results
   * 
   * @param image      The source image to process
   * @param operation  The color operation to apply
   * @param parameters Operation-specific parameters
   * @return ColorOperationResult containing the processed image and metadata
   * @throws IllegalArgumentException if any parameter is invalid
   * @throws RuntimeException         if processing fails
   */
  public ColorOperationResult processImage(Image image, ColorOperation operation,
      Map<String, Object> parameters) {

    // Input validation with detailed error messages
    validateInputs(image, operation, parameters);

    // Log the operation start
    logger.info(String.format("Starting color operation: %s on image: %s",
        operation.getOperationName(), image.getOriginalFileName()));

    // Execute operation with performance tracking
    long startTime = System.currentTimeMillis();
    LocalDateTime processedAt = LocalDateTime.now();

    try {
      Image processedImage = operation.apply(image, parameters);
      long processingTime = System.currentTimeMillis() - startTime;

      // Validate the result
      if (processedImage == null) {
        throw new RuntimeException("Operation returned null image");
      }

      logger.info(String.format("Color operation completed successfully in %d ms", processingTime));

      return new ColorOperationResult(
          image,
          processedImage,
          operation.getOperationName(),
          parameters,
          processedAt,
          processingTime);

    } catch (IllegalArgumentException e) {
      // Re-throw validation errors as-is
      logger.log(Level.WARNING, "Invalid arguments for color operation", e);
      throw e;
    } catch (Exception e) {
      long processingTime = System.currentTimeMillis() - startTime;
      String errorMessage = String.format(
          "Failed to execute color operation '%s' after %d ms: %s",
          operation.getOperationName(), processingTime, e.getMessage());

      logger.log(Level.SEVERE, errorMessage, e);
      throw new RuntimeException(errorMessage, e);
    }
  }

  /**
   * Validates the inputs for color processing.
   * 
   * @param image      The image to validate
   * @param operation  The operation to validate
   * @param parameters The parameters to validate
   * @throws IllegalArgumentException if any input is invalid
   */
  private void validateInputs(Image image, ColorOperation operation, Map<String, Object> parameters) {
    Objects.requireNonNull(image, "Image cannot be null");
    Objects.requireNonNull(operation, "Color operation cannot be null");
    Objects.requireNonNull(parameters, "Parameters cannot be null");

    // Validate image has data
    if (image.getImageData() == null) {
      throw new IllegalArgumentException("Image has no image data");
    }

    // Validate operation-specific parameters
    if (!operation.isValidParameters(parameters)) {
      String expectedParams = String.join(", ", operation.getExpectedParameters());
      throw new IllegalArgumentException(
          String.format("Invalid parameters for operation '%s'. Expected: [%s]",
              operation.getOperationName(),
              expectedParams.isEmpty() ? "none" : expectedParams));
    }
  }

  /**
   * Validates if an operation can be performed with the given parameters.
   * 
   * @param operation  The color operation to validate
   * @param parameters The parameters to validate
   * @return true if the operation can be performed, false otherwise
   */
  public boolean canProcess(ColorOperation operation, Map<String, Object> parameters) {
    try {
      Objects.requireNonNull(operation, "Operation cannot be null");
      Objects.requireNonNull(parameters, "Parameters cannot be null");
      return operation.isValidParameters(parameters);
    } catch (Exception e) {
      logger.log(Level.FINE, "Cannot process operation", e);
      return false;
    }
  }

  /**
   * Gets information about what parameters an operation expects.
   * Useful for UI generation and user guidance.
   * 
   * @param operation The color operation to inspect
   * @return Array of expected parameter names
   * @throws IllegalArgumentException if operation is null
   */
  public String[] getOperationParameters(ColorOperation operation) {
    Objects.requireNonNull(operation, "Color operation cannot be null");
    return operation.getExpectedParameters();
  }

  /**
   * Gets a human-readable description of the operation requirements.
   * 
   * @param operation The color operation to describe
   * @return Description string
   */
  public String getOperationDescription(ColorOperation operation) {
    Objects.requireNonNull(operation, "Color operation cannot be null");

    String[] params = operation.getExpectedParameters();
    if (params.length == 0) {
      return String.format("Operation '%s' requires no parameters",
          operation.getOperationName());
    } else {
      return String.format("Operation '%s' requires parameters: %s",
          operation.getOperationName(), String.join(", ", params));
    }
  }
}
