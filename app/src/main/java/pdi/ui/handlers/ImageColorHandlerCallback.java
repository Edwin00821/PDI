package pdi.ui.handlers;

import pdi.lib.core.domain.Image;

/**
 * Callback interface for handling image color processing events.
 * 
 * This interface defines the contract for receiving notifications
 * about the different stages of color processing operations,
 * allowing for better separation of concerns and easier testing.
 */
public interface ImageColorHandlerCallback {

  /**
   * Called when a color processing operation starts.
   * 
   * @param operationName Name of the operation being performed
   */
  void onProcessingStarted(String operationName);

  /**
   * Called when a color processing operation completes successfully.
   * 
   * @param processedImage The resulting processed image
   * @param operationName  Name of the operation that was performed
   * @param processingTime Time taken for the operation in milliseconds
   */
  void onProcessingCompleted(Image processedImage, String operationName, long processingTime);

  /**
   * Called when a color processing operation fails due to processing errors.
   * 
   * @param operationName Name of the operation that failed
   * @param errorMessage  Description of the error that occurred
   */
  void onProcessingFailed(String operationName, String errorMessage);

  /**
   * Called when a color processing operation fails due to invalid parameters.
   * 
   * @param operationName Name of the operation with invalid parameters
   * @param errorMessage  Description of the parameter validation error
   */
  void onInvalidParameters(String operationName, String errorMessage);
}
