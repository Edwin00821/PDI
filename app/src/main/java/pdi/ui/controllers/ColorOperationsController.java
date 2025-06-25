package pdi.ui.controllers;

import java.util.Objects;

import pdi.lib.core.domain.Image;
import pdi.lib.color.application.ColorProcessorService;
import pdi.lib.color.infrastructure.RGBExtractionOperation;
import pdi.ui.components.DialogManager;
import pdi.ui.handlers.ImageColorHandler;
import pdi.ui.handlers.ImageColorHandlerCallback;

/**
 * Controller for coordinating color operations between UI and business logic.
 * 
 * This controller manages the interaction between the UI components and the
 * color processing logic, handling user input validation, dialog management,
 * and status updates. It acts as the coordinator between different UI handlers
 * and the business logic.
 */
public class ColorOperationsController implements ImageColorHandlerCallback {

  private final DialogManager dialogManager;
  private final ImageColorHandler imageColorHandler;

  // Callbacks for UI updates
  private ImageUpdateCallback imageUpdateCallback;
  private StatusUpdateCallback statusUpdateCallback;
  private TitleUpdateCallback titleUpdateCallback;

  /**
   * Creates a new ColorOperationsController.
   * 
   * @param dialogManager         Manager for showing dialogs
   * @param colorProcessorService Service for color processing operations
   */
  public ColorOperationsController(DialogManager dialogManager,
      ColorProcessorService colorProcessorService) {
    this.dialogManager = Objects.requireNonNull(dialogManager, "DialogManager cannot be null");
    this.imageColorHandler = new ImageColorHandler(
        Objects.requireNonNull(colorProcessorService, "ColorProcessorService cannot be null"));

    // Set this controller as the callback for the handler
    this.imageColorHandler.setCallback(this);
  }

  /**
   * Applies grayscale conversion to the specified image.
   * 
   * @param image Image to process
   */
  public void applyGrayscale(Image image) {
    if (!validateImageLoaded(image))
      return;

    imageColorHandler.applyGrayscale(image);
  }

  /**
   * Applies brightness adjustment with user input.
   * 
   * @param image Image to process
   */
  public void applyBrightness(Image image) {
    if (!validateImageLoaded(image))
      return;

    Integer brightnessLevel = dialogManager.showBrightnessInputDialog();
    if (brightnessLevel != null) {
      imageColorHandler.applyBrightness(image, brightnessLevel);
    }
  }

  /**
   * Applies contrast adjustment with user input.
   * 
   * @param image Image to process
   */
  public void applyContrast(Image image) {
    if (!validateImageLoaded(image))
      return;

    Double contrastFactor = dialogManager.showContrastInputDialog();
    if (contrastFactor != null) {
      imageColorHandler.applyContrast(image, contrastFactor);
    }
  }

  /**
   * Applies RGB channel extraction for the specified channel.
   * 
   * @param image   Image to process
   * @param channel RGB channel to extract
   */
  public void applyRGBExtraction(Image image, RGBExtractionOperation.Channel channel) {
    if (!validateImageLoaded(image))
      return;

    imageColorHandler.applyRGBExtraction(image, channel);
  }

  /**
   * Applies color operation based on operation type string.
   * This method provides a unified interface for different operation types.
   * 
   * @param image         Image to process
   * @param operationType Type of operation to perform
   */
  public void applyColorOperation(Image image, String operationType) {
    if (!validateImageLoaded(image))
      return;

    switch (operationType.toLowerCase()) {
      case "grayscale":
        applyGrayscale(image);
        break;
      case "brightness":
        applyBrightness(image);
        break;
      case "contrast":
        applyContrast(image);
        break;
      case "rgb_red":
        applyRGBExtraction(image, RGBExtractionOperation.Channel.RED);
        break;
      case "rgb_green":
        applyRGBExtraction(image, RGBExtractionOperation.Channel.GREEN);
        break;
      case "rgb_blue":
        applyRGBExtraction(image, RGBExtractionOperation.Channel.BLUE);
        break;
      default:
        updateStatus("Operation not implemented: " + operationType);
    }
  }

  /**
   * Validates that an image is loaded before processing.
   * 
   * @param image Image to validate
   * @return true if valid, false otherwise
   */
  private boolean validateImageLoaded(Image image) {
    if (image == null) {
      dialogManager.showErrorDialog("No Image", "Please load an image first.");
      return false;
    }
    return true;
  }

  // ImageColorHandlerCallback implementation

  @Override
  public void onProcessingStarted(String operationName) {
    updateStatus("Applying " + operationName.toLowerCase() + "...");
  }

  @Override
  public void onProcessingCompleted(Image processedImage, String operationName, long processingTime) {
    // Update the displayed image
    if (imageUpdateCallback != null) {
      imageUpdateCallback.onImageUpdate(processedImage);
    }

    // Update status with processing information
    updateStatus(String.format("%s applied (%d ms)", operationName, processingTime));

    // Update window title to reflect the operation
    if (titleUpdateCallback != null) {
      String newTitle = "PDI - " + processedImage.getOriginalFileName() + " [" + operationName + "]";
      titleUpdateCallback.onTitleUpdate(newTitle);
    }
  }

  @Override
  public void onProcessingFailed(String operationName, String errorMessage) {
    dialogManager.showErrorDialog("Processing Error",
        "Error applying " + operationName.toLowerCase() + ": " + errorMessage);
    updateStatus("Error applying " + operationName.toLowerCase());
  }

  @Override
  public void onInvalidParameters(String operationName, String errorMessage) {
    dialogManager.showErrorDialog("Invalid Parameters",
        "Invalid parameters for " + operationName.toLowerCase() + ": " + errorMessage);
    updateStatus("Invalid parameters for " + operationName.toLowerCase());
  }

  // Helper method for status updates
  private void updateStatus(String message) {
    if (statusUpdateCallback != null) {
      statusUpdateCallback.onStatusUpdate(message);
    }
  }

  // Callback setters for UI integration

  /**
   * Sets the callback for image updates.
   * 
   * @param callback Callback to handle image updates
   */
  public void setImageUpdateCallback(ImageUpdateCallback callback) {
    this.imageUpdateCallback = callback;
  }

  /**
   * Sets the callback for status updates.
   * 
   * @param callback Callback to handle status updates
   */
  public void setStatusUpdateCallback(StatusUpdateCallback callback) {
    this.statusUpdateCallback = callback;
  }

  /**
   * Sets the callback for title updates.
   * 
   * @param callback Callback to handle title updates
   */
  public void setTitleUpdateCallback(TitleUpdateCallback callback) {
    this.titleUpdateCallback = callback;
  }

  // Callback interfaces for UI updates

  /**
   * Callback interface for image updates.
   */
  @FunctionalInterface
  public interface ImageUpdateCallback {
    void onImageUpdate(Image image);
  }

  /**
   * Callback interface for status updates.
   */
  @FunctionalInterface
  public interface StatusUpdateCallback {
    void onStatusUpdate(String status);
  }

  /**
   * Callback interface for title updates.
   */
  @FunctionalInterface
  public interface TitleUpdateCallback {
    void onTitleUpdate(String title);
  }
}
