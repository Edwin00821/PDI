package pdi.lib.color.domain;

import java.util.Map;

import pdi.lib.core.domain.Image;

/**
 * Core domain interface for color operations in the PDI system.
 * 
 * This interface defines the contract for all color manipulation operations
 * following the Strategy pattern. Each implementation represents a specific
 * algorithm or approach to color processing.
 * 
 * Following Clean Architecture principles, this interface is part of the
 * domain layer and should not depend on any external frameworks or libraries.
 */
public interface ColorOperation {

  /**
   * Applies a color operation to the given image.
   * 
   * @param image      The source image to process
   * @param parameters Operation-specific parameters (brightness level, contrast
   *                   factor, etc.)
   * @return A new Image instance with the operation applied
   * @throws IllegalArgumentException if image is null or parameters are invalid
   */
  Image apply(Image image, Map<String, Object> parameters);

  /**
   * Gets the name of this color operation.
   * Used for logging, UI display, and operation identification.
   * 
   * @return A human-readable name for this operation
   */
  String getOperationName();

  /**
   * Validates if the provided parameters are valid for this operation.
   * 
   * @param parameters The parameters to validate
   * @return true if parameters are valid, false otherwise
   */
  boolean isValidParameters(Map<String, Object> parameters);

  /**
   * Gets the expected parameter keys for this operation.
   * Useful for validation and UI generation.
   * 
   * @return Array of parameter keys this operation expects
   */
  String[] getExpectedParameters();
}
