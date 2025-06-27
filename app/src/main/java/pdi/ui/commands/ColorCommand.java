package pdi.ui.commands;

import java.util.Map;
import java.util.Objects;

import pdi.lib.color.application.ColorProcessorService;
import pdi.lib.color.domain.ColorOperation;
import pdi.lib.color.domain.ColorOperationResult;
import pdi.lib.core.domain.Image;

/**
 * Command implementation for color operations on images.
 * 
 * This command encapsulates color processing operations, making them
 * reversible and allowing for undo/redo functionality.
 */
public class ColorCommand implements Command {

  private final Image originalImage;
  private final ColorOperation operation;
  private final Map<String, Object> parameters;
  private final ColorProcessorService colorProcessorService;
  private final String description;

  // State tracking
  private Image processedImage;
  private boolean executed = false;

  /**
   * Creates a new color command.
   * 
   * @param originalImage         The image to process
   * @param operation             The color operation to apply
   * @param parameters            Parameters for the operation
   * @param colorProcessorService Service to perform the operation
   * @param description           Human-readable description of the operation
   */
  public ColorCommand(
      Image originalImage,
      ColorOperation operation,
      Map<String, Object> parameters,
      ColorProcessorService colorProcessorService,
      String description) {

    this.originalImage = Objects.requireNonNull(originalImage, "Original image cannot be null");
    this.operation = Objects.requireNonNull(operation, "Operation cannot be null");
    this.parameters = Objects.requireNonNull(parameters, "Parameters cannot be null");
    this.colorProcessorService = Objects.requireNonNull(colorProcessorService,
        "ColorProcessorService cannot be null");
    this.description = Objects.requireNonNull(description, "Description cannot be null");
  }

  @Override
  public CommandResult execute() throws Exception {
    if (executed) {
      throw new IllegalStateException("Command has already been executed");
    }

    try {
      long startTime = System.currentTimeMillis();

      ColorOperationResult result = colorProcessorService.processImage(
          originalImage, operation, parameters);

      long executionTime = System.currentTimeMillis() - startTime;

      this.processedImage = result.getProcessedImage();
      this.executed = true;

      return new CommandResult(processedImage, executionTime);

    } catch (Exception e) {
      return new CommandResult("Failed to execute " + description + ": " + e.getMessage());
    }
  }

  @Override
  public CommandResult undo() throws Exception {
    if (!executed) {
      throw new IllegalStateException("Cannot undo a command that hasn't been executed");
    }

    if (!canUndo()) {
      throw new UnsupportedOperationException("This command cannot be undone");
    }

    // Reset execution state
    this.processedImage = null;
    this.executed = false;

    // Return the original image
    return new CommandResult(originalImage, 0);
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean canUndo() {
    return true; // All color operations are reversible by restoring original state
  }

  @Override
  public Image getOriginalImage() {
    return originalImage;
  }

  /**
   * Gets the processed image result.
   * Only available after successful execution.
   * 
   * @return The processed image, or null if not yet executed
   */
  public Image getProcessedImage() {
    return processedImage;
  }

  /**
   * Checks if this command has been executed.
   * 
   * @return true if executed, false otherwise
   */
  public boolean isExecuted() {
    return executed;
  }
}
