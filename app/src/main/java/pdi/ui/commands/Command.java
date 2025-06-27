package pdi.ui.commands;

import pdi.lib.core.domain.Image;

/**
 * Command interface for implementing the Command pattern in image operations.
 * 
 * This interface allows encapsulating image operations as objects, enabling
 * features like undo/redo, operation queuing, and operation logging.
 * 
 * Each command should be self-contained and reversible.
 */
public interface Command {

  /**
   * Executes the command and returns the result.
   * 
   * @return The result of executing the command
   * @throws Exception if the command execution fails
   */
  CommandResult execute() throws Exception;

  /**
   * Undoes the command, restoring the previous state.
   * 
   * @return The result of undoing the command (usually the original image)
   * @throws Exception if the undo operation fails
   */
  CommandResult undo() throws Exception;

  /**
   * Gets a description of what this command does.
   * Used for UI display and logging purposes.
   * 
   * @return Human-readable description of the command
   */
  String getDescription();

  /**
   * Checks if this command can be undone.
   * Some commands might not be reversible.
   * 
   * @return true if the command can be undone, false otherwise
   */
  boolean canUndo();

  /**
   * Gets the original image before the command was executed.
   * Used for undo operations.
   * 
   * @return The original image state
   */
  Image getOriginalImage();
}
