package pdi.ui.handlers;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

import pdi.lib.core.application.ImageLoaderService;
import pdi.lib.core.application.LoadImageResult;
import pdi.lib.core.domain.Image;

/**
 * Handles file operations for the PDI application.
 * 
 * This class encapsulates all file-related operations including:
 * - File selection through dialogs
 * - Image loading operations
 * - File validation and filtering
 * 
 * It acts as an intermediary between the UI layer and the application services,
 * providing a clean separation of concerns for file operations.
 */
public class FileOperationHandler {

  private final ImageLoaderService imageLoaderService;
  private final JFileChooser fileChooser;
  private final Component parentComponent;

  // Callbacks for communicating with the UI
  private Consumer<String> statusUpdateCallback;
  private Consumer<Image> imageLoadedCallback;
  private Consumer<String> errorCallback;
  private Runnable imageClosedCallback;

  /**
   * Creates a new FileOperationHandler.
   * 
   * @param imageLoaderService Service for loading images
   * @param parentComponent    Parent component for dialogs
   */
  public FileOperationHandler(ImageLoaderService imageLoaderService, Component parentComponent) {
    this.imageLoaderService = Objects.requireNonNull(imageLoaderService,
        "ImageLoaderService cannot be null");
    this.parentComponent = Objects.requireNonNull(parentComponent,
        "Parent component cannot be null");

    this.fileChooser = createFileChooser();
  }

  /**
   * Sets the callback for status updates.
   * 
   * @param callback Callback to handle status messages
   */
  public void setStatusUpdateCallback(Consumer<String> callback) {
    this.statusUpdateCallback = callback;
  }

  /**
   * Sets the callback for when an image is successfully loaded.
   * 
   * @param callback Callback to handle loaded images
   */
  public void setImageLoadedCallback(Consumer<Image> callback) {
    this.imageLoadedCallback = callback;
  }

  /**
   * Sets the callback for error handling.
   * 
   * @param callback Callback to handle error messages
   */
  public void setErrorCallback(Consumer<String> callback) {
    this.errorCallback = callback;
  }

  /**
   * Sets the callback for when an image is closed.
   * 
   * @param callback Callback to handle image closure
   */
  public void setImageClosedCallback(Runnable callback) {
    this.imageClosedCallback = callback;
  }

  /**
   * Opens a file dialog and loads the selected image.
   */
  public void openImage() {
    updateStatus("Opening file dialog...");

    int result = fileChooser.showOpenDialog(parentComponent);

    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      loadImage(selectedFile);
    } else {
      updateStatus("File selection cancelled");
    }
  }

  /**
   * Loads an image from the specified file.
   * 
   * @param file File containing the image to load
   */
  public void loadImage(File file) {
    if (file == null || !file.exists()) {
      handleError("File does not exist or is invalid");
      return;
    }

    if (!isValidImageFile(file)) {
      handleError("Selected file is not a supported image format");
      return;
    }

    updateStatus("Loading image: " + file.getName() + "...");

    try {
      LoadImageResult result = imageLoaderService.loadImage(file);

      if (result.isSuccess()) {
        Image loadedImage = result.getImage();

        if (imageLoadedCallback != null) {
          imageLoadedCallback.accept(loadedImage);
        }

        updateStatus("Image loaded: " + loadedImage.getOriginalFileName() +
            " (" + loadedImage.getWidth() + "x" + loadedImage.getHeight() + ")");

      } else {
        handleError("Error loading image: " + result.getErrorMessage());
        updateStatus("Failed to load image: " + file.getName());
      }

    } catch (Exception ex) {
      handleError("Unexpected error while loading image: " + ex.getMessage());
      updateStatus("Error occurred while loading image");
    }
  }

  /**
   * Closes the currently loaded image.
   */
  public void closeCurrentImage() {
    if (imageClosedCallback != null) {
      imageClosedCallback.run();
    }
    updateStatus("Image closed");
  }

  /**
   * Gets the current directory of the file chooser.
   * 
   * @return Current directory
   */
  public File getCurrentDirectory() {
    return fileChooser.getCurrentDirectory();
  }

  /**
   * Sets the current directory for the file chooser.
   * 
   * @param directory Directory to set as current
   */
  public void setCurrentDirectory(File directory) {
    if (directory != null && directory.isDirectory()) {
      fileChooser.setCurrentDirectory(directory);
    }
  }

  /**
   * Creates and configures the file chooser.
   * 
   * @return Configured JFileChooser
   */
  private JFileChooser createFileChooser() {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    chooser.setFileFilter(createImageFileFilter());
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.setMultiSelectionEnabled(false);

    return chooser;
  }

  /**
   * Creates a file filter for supported image formats.
   * 
   * @return FileFilter for image files
   */
  private FileFilter createImageFileFilter() {
    return new FileFilter() {
      @Override
      public boolean accept(File file) {
        if (file.isDirectory()) {
          return true;
        }
        return isValidImageFile(file);
      }

      @Override
      public String getDescription() {
        return "Image Files (*.jpg, *.jpeg, *.png, *.gif, *.bmp, *.wbmp)";
      }
    };
  }

  /**
   * Validates if a file is a supported image format.
   * 
   * @param file File to validate
   * @return true if the file is a supported image format
   */
  private boolean isValidImageFile(File file) {
    if (file == null || !file.isFile()) {
      return false;
    }

    String fileName = file.getName().toLowerCase();
    return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
        fileName.endsWith(".png") || fileName.endsWith(".gif") ||
        fileName.endsWith(".bmp") || fileName.endsWith(".wbmp");
  }

  /**
   * Updates the status through the callback if available.
   * 
   * @param message Status message
   */
  private void updateStatus(String message) {
    if (statusUpdateCallback != null) {
      statusUpdateCallback.accept(message);
    }
  }

  /**
   * Handles errors through the callback if available.
   * 
   * @param errorMessage Error message
   */
  private void handleError(String errorMessage) {
    if (errorCallback != null) {
      errorCallback.accept(errorMessage);
    }
  }
}
