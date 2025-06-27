package pdi.ui.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages command execution and maintains history for undo/redo operations.
 * 
 * This class implements the Command pattern to provide undo/redo functionality
 * for image operations. It maintains a history of executed commands and
 * allows navigating through them.
 */
public class CommandManager {

  // Command history storage
  private final List<Command> commandHistory;
  private int currentIndex; // Points to the last executed command

  // Configuration
  private final int maxHistorySize;

  // Callbacks for UI updates
  private Runnable onHistoryChanged;

  /**
   * Creates a new CommandManager with default history size.
   */
  public CommandManager() {
    this(50); // Default to 50 commands in history
  }

  /**
   * Creates a new CommandManager with specified history size.
   * 
   * @param maxHistorySize Maximum number of commands to keep in history
   */
  public CommandManager(int maxHistorySize) {
    if (maxHistorySize <= 0) {
      throw new IllegalArgumentException("Max history size must be positive");
    }

    this.maxHistorySize = maxHistorySize;
    this.commandHistory = new ArrayList<>();
    this.currentIndex = -1; // No commands initially
  }

  /**
   * Executes a command and adds it to the history.
   * 
   * @param command The command to execute
   * @return The result of command execution
   * @throws Exception if command execution fails
   */
  public CommandResult executeCommand(Command command) throws Exception {
    Objects.requireNonNull(command, "Command cannot be null");

    CommandResult result = command.execute();

    if (result.isSuccess()) {
      addCommandToHistory(command);
      notifyHistoryChanged();
    }

    return result;
  }

  /**
   * Undoes the last executed command.
   * 
   * @return The result of the undo operation
   * @throws Exception if undo fails or no command to undo
   */
  public CommandResult undo() throws Exception {
    if (!canUndo()) {
      throw new IllegalStateException("No command to undo");
    }

    Command command = commandHistory.get(currentIndex);
    CommandResult result = command.undo();

    if (result.isSuccess()) {
      currentIndex--;
      notifyHistoryChanged();
    }

    return result;
  }

  /**
   * Redoes the next command in history.
   * 
   * @return The result of the redo operation
   * @throws Exception if redo fails or no command to redo
   */
  public CommandResult redo() throws Exception {
    if (!canRedo()) {
      throw new IllegalStateException("No command to redo");
    }

    Command command = commandHistory.get(currentIndex + 1);
    CommandResult result = command.execute();

    if (result.isSuccess()) {
      currentIndex++;
      notifyHistoryChanged();
    }

    return result;
  }

  /**
   * Checks if undo operation is possible.
   * 
   * @return true if there's a command to undo
   */
  public boolean canUndo() {
    return currentIndex >= 0 &&
        currentIndex < commandHistory.size() &&
        commandHistory.get(currentIndex).canUndo();
  }

  /**
   * Checks if redo operation is possible.
   * 
   * @return true if there's a command to redo
   */
  public boolean canRedo() {
    return currentIndex + 1 < commandHistory.size();
  }

  /**
   * Gets the description of the command that would be undone.
   * 
   * @return Description of the undo command, or null if can't undo
   */
  public String getUndoDescription() {
    if (canUndo()) {
      return commandHistory.get(currentIndex).getDescription();
    }
    return null;
  }

  /**
   * Gets the description of the command that would be redone.
   * 
   * @return Description of the redo command, or null if can't redo
   */
  public String getRedoDescription() {
    if (canRedo()) {
      return commandHistory.get(currentIndex + 1).getDescription();
    }
    return null;
  }

  /**
   * Clears the command history.
   */
  public void clearHistory() {
    commandHistory.clear();
    currentIndex = -1;
    notifyHistoryChanged();
  }

  /**
   * Gets the current number of commands in history.
   * 
   * @return Number of commands in history
   */
  public int getHistorySize() {
    return commandHistory.size();
  }

  /**
   * Gets the current position in the history.
   * 
   * @return Current index (-1 if no commands executed)
   */
  public int getCurrentIndex() {
    return currentIndex;
  }

  /**
   * Sets the callback for history change notifications.
   * 
   * @param callback Callback to invoke when history changes
   */
  public void setOnHistoryChanged(Runnable callback) {
    this.onHistoryChanged = callback;
  }

  /**
   * Adds a command to the history, managing size limits.
   * 
   * @param command Command to add
   */
  private void addCommandToHistory(Command command) {
    // If we're not at the end of history, remove everything after current position
    // This happens when user does undo then executes a new command
    while (commandHistory.size() > currentIndex + 1) {
      commandHistory.remove(commandHistory.size() - 1);
    }

    // Add the new command
    commandHistory.add(command);
    currentIndex++;

    // Maintain history size limit
    while (commandHistory.size() > maxHistorySize) {
      commandHistory.remove(0);
      currentIndex--;
    }
  }

  /**
   * Notifies listeners that the history has changed.
   */
  private void notifyHistoryChanged() {
    if (onHistoryChanged != null) {
      onHistoryChanged.run();
    }
  }
}
