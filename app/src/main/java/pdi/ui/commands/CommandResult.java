package pdi.ui.commands;

import pdi.lib.core.domain.Image;

/**
 * Represents the result of executing a command.
 * 
 * This class encapsulates both successful and failed command executions,
 * providing a consistent way to handle command results throughout the
 * application.
 */
public class CommandResult {

  private final boolean success;
  private final Image resultImage;
  private final String errorMessage;
  private final long executionTimeMs;

  /**
   * Creates a successful command result.
   * 
   * @param resultImage     The image resulting from the command execution
   * @param executionTimeMs Time taken to execute the command in milliseconds
   */
  public CommandResult(Image resultImage, long executionTimeMs) {
    this.success = true;
    this.resultImage = resultImage;
    this.errorMessage = null;
    this.executionTimeMs = executionTimeMs;
  }

  /**
   * Creates a failed command result.
   * 
   * @param errorMessage Description of what went wrong
   */
  public CommandResult(String errorMessage) {
    this.success = false;
    this.resultImage = null;
    this.errorMessage = errorMessage;
    this.executionTimeMs = 0;
  }

  /**
   * Checks if the command execution was successful.
   * 
   * @return true if successful, false otherwise
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * Gets the resulting image from the command execution.
   * 
   * @return The result image, or null if the command failed
   */
  public Image getResultImage() {
    return resultImage;
  }

  /**
   * Gets the error message if the command failed.
   * 
   * @return Error message, or null if the command succeeded
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Gets the execution time of the command.
   * 
   * @return Execution time in milliseconds
   */
  public long getExecutionTimeMs() {
    return executionTimeMs;
  }
}
