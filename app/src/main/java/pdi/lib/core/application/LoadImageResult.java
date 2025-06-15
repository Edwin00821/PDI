package pdi.lib.core.application;

import pdi.lib.core.domain.Image;

/**
 * Result object for image loading operations.
 * 
 * This follows the Result pattern to handle success/failure scenarios
 * in a more explicit and type-safe way than throwing exceptions.
 * It provides clear feedback about what happened during the loading process.
 */
public class LoadImageResult {
  private final boolean success;
  private final Image image;
  private final String errorMessage;

  private LoadImageResult(boolean success, Image image, String errorMessage) {
    this.success = success;
    this.image = image;
    this.errorMessage = errorMessage;
  }

  /**
   * Creates a successful result with the loaded image.
   * 
   * @param image The successfully loaded image
   * @return Success result containing the image
   */
  public static LoadImageResult success(Image image) {
    return new LoadImageResult(true, image, null);
  }

  /**
   * Creates a failure result with an error message.
   * 
   * @param errorMessage Description of what went wrong
   * @return Failure result with error details
   */
  public static LoadImageResult failure(String errorMessage) {
    return new LoadImageResult(false, null, errorMessage);
  }

  // Getters
  public boolean isSuccess() {
    return success;
  }

  public boolean isFailure() {
    return !success;
  }

  public Image getImage() {
    return image;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public String toString() {
    if (success) {
      return "LoadImageResult{success=true, image=" + image + "}";
    } else {
      return "LoadImageResult{success=false, error='" + errorMessage + "'}";
    }
  }
}
