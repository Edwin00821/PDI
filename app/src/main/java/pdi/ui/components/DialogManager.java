package pdi.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Manages all dialog interactions in the application.
 * Centralizes dialog creation and display logic for consistency
 * and easier maintenance.
 * 
 * This class handles:
 * - Error dialogs with consistent styling
 * - Input dialogs with validation
 * - Information dialogs (About, etc.)
 * - Common dialog patterns used throughout the application
 */
public class DialogManager {

  private final Component parentComponent;

  /**
   * Creates a new DialogManager for the specified parent component.
   * 
   * @param parentComponent The parent component for modal dialogs
   */
  public DialogManager(Component parentComponent) {
    this.parentComponent = parentComponent;
  }

  /**
   * Shows an error dialog with consistent styling and behavior.
   * 
   * @param title   The dialog title
   * @param message The error message to display
   */
  public void showErrorDialog(String title, String message) {
    JOptionPane.showMessageDialog(
        parentComponent,
        message,
        title,
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Shows the application about dialog with version and feature information.
   */
  public void showAboutDialog() {
    String aboutText = "PDI - Digital Image Processing\n\n" +
        "A simple application for digital image processing\n" +
        "Built with Clean Architecture principles\n\n" +
        "Features:\n" +
        "• Image loading and display\n" +
        "• Grayscale conversion\n" +
        "• Brightness and contrast adjustment\n" +
        "• RGB channel extraction\n" +
        "• Extensible filter system\n\n" +
        "Version: 1.0.0 MVP";

    JOptionPane.showMessageDialog(
        parentComponent,
        aboutText,
        "About PDI",
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Shows input dialog for brightness adjustment with validation.
   * 
   * @return brightness level (-255 to +255) or null if cancelled/invalid
   */
  public Integer showBrightnessInputDialog() {
    String input = JOptionPane.showInputDialog(
        parentComponent,
        "Enter brightness level (-255 to +255):\n" +
            "Positive values make the image brighter\n" +
            "Negative values make the image darker",
        "Brightness Adjustment",
        JOptionPane.QUESTION_MESSAGE);

    if (input == null) {
      return null; // User cancelled
    }

    try {
      int brightnessLevel = Integer.parseInt(input.trim());

      // Validate range
      if (brightnessLevel < -255 || brightnessLevel > 255) {
        showErrorDialog("Invalid Range",
            "Brightness level must be between -255 and +255.");
        return null;
      }

      return brightnessLevel;

    } catch (NumberFormatException e) {
      showErrorDialog("Invalid Input",
          "Please enter a valid integer between -255 and +255.");
      return null;
    }
  }

  /**
   * Shows input dialog for contrast adjustment with validation.
   * 
   * @return contrast factor (0.0 to 3.0) or null if cancelled/invalid
   */
  public Double showContrastInputDialog() {
    String input = JOptionPane.showInputDialog(
        parentComponent,
        "Enter contrast factor (0.0 to 3.0):\n" +
            "1.0 = no change\n" +
            "< 1.0 = decrease contrast\n" +
            "> 1.0 = increase contrast",
        "Contrast Adjustment",
        JOptionPane.QUESTION_MESSAGE);

    if (input == null) {
      return null; // User cancelled
    }

    try {
      double contrastFactor = Double.parseDouble(input.trim());

      // Validate range
      if (contrastFactor < 0.0 || contrastFactor > 3.0) {
        showErrorDialog("Invalid Range",
            "Contrast factor must be between 0.0 and 3.0.");
        return null;
      }

      return contrastFactor;

    } catch (NumberFormatException e) {
      showErrorDialog("Invalid Input",
          "Please enter a valid number between 0.0 and 3.0.");
      return null;
    }
  }

  /**
   * Shows a confirmation dialog for potentially destructive actions.
   * 
   * @param title   The dialog title
   * @param message The confirmation message
   * @return true if user confirmed, false otherwise
   */
  public boolean showConfirmationDialog(String title, String message) {
    int result = JOptionPane.showConfirmDialog(
        parentComponent,
        message,
        title,
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);

    return result == JOptionPane.YES_OPTION;
  }

  /**
   * Shows a generic information dialog.
   * 
   * @param title   The dialog title
   * @param message The information message
   */
  public void showInfoDialog(String title, String message) {
    JOptionPane.showMessageDialog(
        parentComponent,
        message,
        title,
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Shows a warning dialog.
   * 
   * @param title   The dialog title
   * @param message The warning message
   */
  public void showWarningDialog(String title, String message) {
    JOptionPane.showMessageDialog(
        parentComponent,
        message,
        title,
        JOptionPane.WARNING_MESSAGE);
  }

  /**
   * Shows a generic input dialog with validation.
   * 
   * @param title        The dialog title
   * @param message      The input prompt message
   * @param validator    Function to validate the input (null for no validation)
   * @param errorMessage Error message to show if validation fails
   * @return the validated input string or null if cancelled
   */
  public String showInputDialog(String title, String message,
      java.util.function.Predicate<String> validator,
      String errorMessage) {
    String input = JOptionPane.showInputDialog(
        parentComponent,
        message,
        title,
        JOptionPane.QUESTION_MESSAGE);

    if (input == null) {
      return null; // User cancelled
    }

    if (validator != null && !validator.test(input.trim())) {
      showErrorDialog("Invalid Input", errorMessage);
      return null;
    }

    return input.trim();
  }
}
