package pdi.ui.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pdi.lib.core.domain.Image;
import pdi.lib.color.application.ColorProcessorService;
import pdi.lib.color.domain.ColorOperationResult;
import pdi.lib.color.infrastructure.BrightnessOperation;
import pdi.lib.color.infrastructure.ContrastOperation;
import pdi.lib.color.infrastructure.GrayscaleOperation;
import pdi.lib.color.infrastructure.RGBExtractionOperation;

/**
 * Handler class for managing color processing operations on images.
 * 
 * This class encapsulates all color-related operations and uses a callback
 * interface to communicate results back to the UI layer, promoting better
 * separation of concerns and testability.
 */
public class ImageColorHandler {

  private final ColorProcessorService colorProcessorService;
  private ImageColorHandlerCallback callback;

  /**
   * Creates a new ImageColorHandler with the specified color processor service.
   * 
   * @param colorProcessorService Service for performing color operations
   */
  public ImageColorHandler(ColorProcessorService colorProcessorService) {
    this.colorProcessorService = Objects.requireNonNull(colorProcessorService,
        "ColorProcessorService cannot be null");
  }

  /**
   * Sets the callback for receiving processing events.
   * 
   * @param callback Callback to receive processing events
   */
  public void setCallback(ImageColorHandlerCallback callback) {
    this.callback = callback;
  }

  /**
   * Applies grayscale conversion to the specified image.
   * 
   * @param image Image to process
   */
  public void applyGrayscale(Image image) {
    if (image == null) {
      notifyInvalidParameters("Grayscale Conversion", "No image provided");
      return;
    }

    processColorOperation("Grayscale Conversion", () -> {
      GrayscaleOperation grayscaleOperation = new GrayscaleOperation();
      Map<String, Object> parameters = new HashMap<>();
      return colorProcessorService.processImage(image, grayscaleOperation, parameters);
    });
  }

  /**
   * Applies brightness adjustment to the specified image.
   * 
   * @param image           Image to process
   * @param brightnessLevel Brightness level (-255 to +255)
   */
  public void applyBrightness(Image image, int brightnessLevel) {
    if (image == null) {
      notifyInvalidParameters("Brightness Adjustment", "No image provided");
      return;
    }

    processColorOperation("Brightness Adjustment", () -> {
      BrightnessOperation brightnessOperation = new BrightnessOperation();
      Map<String, Object> parameters = BrightnessOperation.createParameters(brightnessLevel);
      return colorProcessorService.processImage(image, brightnessOperation, parameters);
    });
  }

  /**
   * Applies contrast adjustment to the specified image.
   * 
   * @param image          Image to process
   * @param contrastFactor Contrast factor (0.0 to 3.0)
   */
  public void applyContrast(Image image, double contrastFactor) {
    if (image == null) {
      notifyInvalidParameters("Contrast Adjustment", "No image provided");
      return;
    }

    processColorOperation("Contrast Adjustment", () -> {
      ContrastOperation contrastOperation = new ContrastOperation();
      Map<String, Object> parameters = ContrastOperation.createParameters(contrastFactor);
      return colorProcessorService.processImage(image, contrastOperation, parameters);
    });
  }

  /**
   * Applies RGB channel extraction to the specified image.
   * 
   * @param image   Image to process
   * @param channel RGB channel to extract
   */
  public void applyRGBExtraction(Image image, RGBExtractionOperation.Channel channel) {
    if (image == null) {
      notifyInvalidParameters("RGB Channel Extraction", "No image provided");
      return;
    }

    if (channel == null) {
      notifyInvalidParameters("RGB Channel Extraction", "No channel specified");
      return;
    }

    String operationName = channel.name() + " Channel Extraction";
    processColorOperation(operationName, () -> {
      RGBExtractionOperation rgbOperation = new RGBExtractionOperation();
      Map<String, Object> parameters = RGBExtractionOperation.createParameters(channel);
      return colorProcessorService.processImage(image, rgbOperation, parameters);
    });
  }

  /**
   * Generic method for processing color operations with consistent error
   * handling.
   * 
   * @param operationName     Name of the operation being performed
   * @param operationSupplier Supplier that performs the actual operation
   */
  private void processColorOperation(String operationName,
      java.util.function.Supplier<ColorOperationResult> operationSupplier) {

    // Notify processing started
    notifyProcessingStarted(operationName);

    try {
      ColorOperationResult result = operationSupplier.get();

      // Notify successful completion
      notifyProcessingCompleted(result.getProcessedImage(), operationName,
          result.getProcessingTimeMs());

    } catch (IllegalArgumentException ex) {
      notifyInvalidParameters(operationName, ex.getMessage());
    } catch (RuntimeException ex) {
      // Handle service layer exceptions
      Throwable cause = ex.getCause();
      String message = (cause != null) ? cause.getMessage() : ex.getMessage();
      notifyProcessingFailed(operationName, message);
    } catch (Exception ex) {
      String message = ex.getClass().getSimpleName() + " - " + ex.getMessage();
      notifyProcessingFailed(operationName, message);

      // Log the full stack trace for debugging
      ex.printStackTrace();
    }
  }

  /**
   * Notifies callback about processing start if callback is set.
   */
  private void notifyProcessingStarted(String operationName) {
    if (callback != null) {
      callback.onProcessingStarted(operationName);
    }
  }

  /**
   * Notifies callback about successful processing completion if callback is set.
   */
  private void notifyProcessingCompleted(Image processedImage, String operationName, long processingTime) {
    if (callback != null) {
      callback.onProcessingCompleted(processedImage, operationName, processingTime);
    }
  }

  /**
   * Notifies callback about processing failure if callback is set.
   */
  private void notifyProcessingFailed(String operationName, String errorMessage) {
    if (callback != null) {
      callback.onProcessingFailed(operationName, errorMessage);
    }
  }

  /**
   * Notifies callback about invalid parameters if callback is set.
   */
  private void notifyInvalidParameters(String operationName, String errorMessage) {
    if (callback != null) {
      callback.onInvalidParameters(operationName, errorMessage);
    }
  }
}
