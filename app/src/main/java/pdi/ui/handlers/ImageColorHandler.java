package pdi.ui.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pdi.lib.core.domain.Image;
import pdi.ui.commands.ColorCommand;
import pdi.ui.commands.Command;
import pdi.ui.commands.CommandManager;
import pdi.ui.commands.CommandResult;
import pdi.lib.color.application.ColorProcessorService;
import pdi.lib.color.domain.ColorOperation;
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

  private CommandManager commandManager;

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
   * Creates a new ImageColorHandler with command manager for undo/redo support.
   * 
   * @param colorProcessorService Service for performing color operations
   * @param commandManager        Manager for handling command history
   */
  public ImageColorHandler(ColorProcessorService colorProcessorService,
      CommandManager commandManager) {
    this.colorProcessorService = Objects.requireNonNull(colorProcessorService,
        "ColorProcessorService cannot be null");
    this.commandManager = Objects.requireNonNull(commandManager,
        "CommandManager cannot be null");
  }

  /**
   * Sets the command manager for undo/redo functionality.
   * 
   * @param commandManager Command manager to use
   */
  public void setCommandManager(CommandManager commandManager) {
    this.commandManager = commandManager;
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
   * Applies grayscale conversion to the specified image using command pattern.
   * 
   * @param image Image to process
   */
  public void applyGrayscale(Image image) {
    if (image == null) {
      notifyInvalidParameters("Grayscale Conversion", "No image provided");
      return;
    }

    if (commandManager != null) {
      // Use command pattern for undo/redo support
      executeColorCommand(image, new GrayscaleOperation(), new HashMap<>(), "Grayscale Conversion");
    } else {
      // Fallback to direct processing (backward compatibility)
      processColorOperation("Grayscale Conversion", () -> {
        GrayscaleOperation grayscaleOperation = new GrayscaleOperation();
        Map<String, Object> parameters = new HashMap<>();
        return colorProcessorService.processImage(image, grayscaleOperation, parameters);
      });
    }
  }

  // Replace the existing applyBrightness method with this version
  /**
   * Applies brightness adjustment to the specified image using command pattern.
   * 
   * @param image           Image to process
   * @param brightnessLevel Brightness level (-255 to +255)
   */
  public void applyBrightness(Image image, int brightnessLevel) {
    if (image == null) {
      notifyInvalidParameters("Brightness Adjustment", "No image provided");
      return;
    }

    if (commandManager != null) {
      // Use command pattern for undo/redo support
      Map<String, Object> parameters = BrightnessOperation.createParameters(brightnessLevel);
      executeColorCommand(image, new BrightnessOperation(), parameters,
          "Brightness Adjustment (" + brightnessLevel + ")");
    } else {
      // Fallback to direct processing (backward compatibility)
      processColorOperation("Brightness Adjustment", () -> {
        BrightnessOperation brightnessOperation = new BrightnessOperation();
        Map<String, Object> parameters = BrightnessOperation.createParameters(brightnessLevel);
        return colorProcessorService.processImage(image, brightnessOperation, parameters);
      });
    }
  }

  // Replace the existing applyContrast method with this version
  /**
   * Applies contrast adjustment to the specified image using command pattern.
   * 
   * @param image          Image to process
   * @param contrastFactor Contrast factor (0.0 to 3.0)
   */
  public void applyContrast(Image image, double contrastFactor) {
    if (image == null) {
      notifyInvalidParameters("Contrast Adjustment", "No image provided");
      return;
    }

    if (commandManager != null) {
      // Use command pattern for undo/redo support
      Map<String, Object> parameters = ContrastOperation.createParameters(contrastFactor);
      executeColorCommand(image, new ContrastOperation(), parameters,
          "Contrast Adjustment (" + contrastFactor + ")");
    } else {
      // Fallback to direct processing (backward compatibility)
      processColorOperation("Contrast Adjustment", () -> {
        ContrastOperation contrastOperation = new ContrastOperation();
        Map<String, Object> parameters = ContrastOperation.createParameters(contrastFactor);
        return colorProcessorService.processImage(image, contrastOperation, parameters);
      });
    }
  }

  // Replace the existing applyRGBExtraction method with this version
  /**
   * Applies RGB channel extraction to the specified image using command pattern.
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

    if (commandManager != null) {
      // Use command pattern for undo/redo support
      Map<String, Object> parameters = RGBExtractionOperation.createParameters(channel);
      executeColorCommand(image, new RGBExtractionOperation(), parameters, operationName);
    } else {
      // Fallback to direct processing (backward compatibility)
      processColorOperation(operationName, () -> {
        RGBExtractionOperation rgbOperation = new RGBExtractionOperation();
        Map<String, Object> parameters = RGBExtractionOperation.createParameters(channel);
        return colorProcessorService.processImage(image, rgbOperation, parameters);
      });
    }
  }

  /**
   * Executes a color operation as a command for undo/redo support.
   * 
   * @param image       Image to process
   * @param operation   Color operation to apply
   * @param parameters  Operation parameters
   * @param description Human-readable description
   */
  private void executeColorCommand(Image image, ColorOperation operation,
      Map<String, Object> parameters, String description) {

    notifyProcessingStarted(description);

    try {
      // Create and execute command
      Command command = new ColorCommand(image, operation, parameters,
          colorProcessorService, description);

      CommandResult result = commandManager.executeCommand(command);

      if (result.isSuccess()) {
        notifyProcessingCompleted(result.getResultImage(), description,
            result.getExecutionTimeMs());
      } else {
        notifyProcessingFailed(description, result.getErrorMessage());
      }

    } catch (IllegalArgumentException ex) {
      notifyInvalidParameters(description, ex.getMessage());
    } catch (Exception ex) {
      String message = ex.getClass().getSimpleName() + " - " + ex.getMessage();
      notifyProcessingFailed(description, message);
      ex.printStackTrace();
    }
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
