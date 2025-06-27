package pdi.ui.views;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import pdi.lib.core.domain.Image;
import pdi.lib.core.application.ImageLoaderService;

import pdi.lib.color.application.ColorProcessorService;
import pdi.lib.color.infrastructure.RGBExtractionOperation;

import pdi.ui.commands.CommandManager;
import pdi.ui.commands.CommandResult;
import pdi.ui.components.DialogManager;
import pdi.ui.components.ImageCanvas;
import pdi.ui.components.MenuManager;

import pdi.ui.controllers.ColorOperationsController;

import pdi.ui.handlers.FileOperationHandler;
import pdi.ui.handlers.ImageColorHandler;
import pdi.ui.handlers.ImageColorHandlerCallback;

/**
 * Main application window for the PDI (Digital Image Processing) application.
 * 
 * This window provides the primary user interface for the application,
 * including the menu system and the main canvas for image display.
 * It follows the MVC pattern where this acts as the View/Controller,
 * delegating business logic to the application services.
 * 
 * The window is designed to be the main entry point for user interactions
 * and coordinates between UI components and the application layer.
 * 
 * Now includes undo/redo functionality through the CommandManager.
 */
public class MainWindow extends JFrame implements ImageColorHandlerCallback {

  // UI Components
  private ImageCanvas imageCanvas;
  private DialogManager dialogManager;
  private MenuManager menuManager;
  private JLabel statusLabel;

  // Application services
  private final ImageLoaderService imageLoaderService;
  private final ColorProcessorService colorProcessorService;

  // Command management
  private CommandManager commandManager;

  // Handlers
  private FileOperationHandler fileOperationHandler;
  private ImageColorHandler imageColorHandler;

  // Controllers
  private ColorOperationsController colorOperationsController;

  /**
   * Creates the main application window.
   * 
   * @param imageLoaderService    Service for loading and managing images
   * @param colorProcessorService Service for color processing operations
   */
  public MainWindow(ImageLoaderService imageLoaderService, ColorProcessorService colorProcessorService) {
    this.imageLoaderService = Objects.requireNonNull(imageLoaderService,
        "ImageLoaderService cannot be null");

    this.colorProcessorService = Objects.requireNonNull(colorProcessorService,
        "ColorProcessorService cannot be null");

    initializeWindow();
    createComponents();
    setupLayout();
    setupHandlers();
    setupMenuCallbacks();
    setupEventHandlers();
  }

  /**
   * Initializes the main window properties.
   */
  private void initializeWindow() {
    setTitle("PDI - Digital Image Processing");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1000, 700);
    setLocationRelativeTo(null); // Center on screen

    // Set minimum size to prevent overly small windows
    setMinimumSize(new Dimension(600, 400));

    // Set application icon if available
    // setIconImage(loadApplicationIcon());
  }

  /**
   * Sets up the main layout of the window.
   */
  private void setupLayout() {
    setLayout(new BorderLayout());

    // Add canvas to center (main area)
    add(imageCanvas, BorderLayout.CENTER);

    // Add status bar to bottom
    add(statusLabel, BorderLayout.SOUTH);
  }

  /**
   * Creates and initializes UI components including command manager.
   */
  private void createComponents() {
    // Create main image canvas
    imageCanvas = new ImageCanvas();
    dialogManager = new DialogManager(this);

    // Create command manager for undo/redo
    commandManager = new CommandManager(50); // Keep 50 operations in history
    commandManager.setOnHistoryChanged(this::updateUndoRedoState);

    // Create menu manager
    menuManager = new MenuManager();
    setJMenuBar(menuManager.getMenuBar());

    // Create status bar
    statusLabel = new JLabel("Ready");
    statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
  }

  /**
   * Creates and initializes handlers with command support.
   */
  private void setupHandlers() {
    // Initialize file operation handler
    fileOperationHandler = new FileOperationHandler(imageLoaderService, this);

    fileOperationHandler.setStatusUpdateCallback(this::updateStatus);
    fileOperationHandler.setImageLoadedCallback(this::handleImageLoaded);
    fileOperationHandler.setErrorCallback(this::handleError);
    fileOperationHandler.setImageClosedCallback(this::handleImageClosed);

    // Initialize image color handler with command manager
    imageColorHandler = new ImageColorHandler(colorProcessorService, commandManager);
    imageColorHandler.setCallback(this);

    // Initialize color operations controller (keep for compatibility)
    colorOperationsController = new ColorOperationsController(dialogManager, colorProcessorService);

    colorOperationsController.setImageUpdateCallback(this::handleColorProcessedImage);
    colorOperationsController.setStatusUpdateCallback(this::updateStatus);
    colorOperationsController.setTitleUpdateCallback(this::setTitle);
  }

  /**
   * Sets up callbacks for menu actions including undo/redo.
   */
  private void setupMenuCallbacks() {
    // File operations
    menuManager.setOnOpenImage(fileOperationHandler::openImage);
    menuManager.setOnCloseImage(this::handleCloseImageWithHistoryReset);

    // Undo/Redo operations
    menuManager.setOnUndo(this::performUndo);
    menuManager.setOnRedo(this::performRedo);

    // Dialog operations
    menuManager.setOnAbout(dialogManager::showAboutDialog);
    menuManager.setOnExit(this::exitApplication);

    // Color operations - now using ImageColorHandler with command support
    menuManager.setOnGrayscale(() -> applyColorOperationWithCommands("grayscale"));
    menuManager.setOnBrightness(() -> applyColorOperationWithCommands("brightness"));
    menuManager.setOnContrast(() -> applyColorOperationWithCommands("contrast"));
    menuManager.setOnRedChannel(() -> applyColorOperationWithCommands("rgb_red"));
    menuManager.setOnGreenChannel(() -> applyColorOperationWithCommands("rgb_green"));
    menuManager.setOnBlueChannel(() -> applyColorOperationWithCommands("rgb_blue"));
  }

  /**
   * Sets up event handlers for the window.
   */
  private void setupEventHandlers() {
    // Window closing event
    addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        handleWindowClosing();
      }
    });

    // Context menu for image canvas
    imageCanvas.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showContextMenu(e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showContextMenu(e);
        }
      }
    });
  }

  /**
   * Handles close image operation with command history reset.
   */
  private void handleCloseImageWithHistoryReset() {
    fileOperationHandler.closeCurrentImage();
    commandManager.clearHistory(); // Clear history when closing image
    updateUndoRedoState(); // Update menu states
  }

  /**
   * Performs undo operation.
   */
  private void performUndo() {
    if (!commandManager.canUndo()) {
      updateStatus("Nothing to undo");
      return;
    }

    try {
      updateStatus("Undoing " + commandManager.getUndoDescription() + "...");

      CommandResult result = commandManager.undo();

      if (result.isSuccess()) {
        imageCanvas.setImage(result.getResultImage());
        updateStatus("Undone: " + commandManager.getRedoDescription());
      } else {
        updateStatus("Undo failed: " + result.getErrorMessage());
        dialogManager.showErrorDialog("Undo Failed", result.getErrorMessage());
      }

    } catch (Exception e) {
      String errorMsg = "Failed to undo operation: " + e.getMessage();
      updateStatus(errorMsg);
      dialogManager.showErrorDialog("Undo Error", errorMsg);
      e.printStackTrace();
    }
  }

  /**
   * Performs redo operation.
   */
  private void performRedo() {
    if (!commandManager.canRedo()) {
      updateStatus("Nothing to redo");
      return;
    }

    try {
      updateStatus("Redoing " + commandManager.getRedoDescription() + "...");

      CommandResult result = commandManager.redo();

      if (result.isSuccess()) {
        imageCanvas.setImage(result.getResultImage());
        updateStatus("Redone: " + commandManager.getUndoDescription());
      } else {
        updateStatus("Redo failed: " + result.getErrorMessage());
        dialogManager.showErrorDialog("Redo Failed", result.getErrorMessage());
      }

    } catch (Exception e) {
      String errorMsg = "Failed to redo operation: " + e.getMessage();
      updateStatus(errorMsg);
      dialogManager.showErrorDialog("Redo Error", errorMsg);
      e.printStackTrace();
    }
  }

  /**
   * Updates the undo/redo menu states based on command manager state.
   */
  private void updateUndoRedoState() {
    boolean canUndo = commandManager.canUndo();
    boolean canRedo = commandManager.canRedo();

    menuManager.setUndoRedoState(canUndo, canRedo);
    menuManager.setUndoDescription(commandManager.getUndoDescription());
    menuManager.setRedoDescription(commandManager.getRedoDescription());
  }

  /**
   * Applies color operations using the command pattern for undo/redo support.
   * 
   * @param operationType Type of operation to apply
   */
  private void applyColorOperationWithCommands(String operationType) {
    Image currentImage = getCurrentImage();
    if (currentImage == null) {
      updateStatus("No image loaded");
      return;
    }

    switch (operationType) {
      case "grayscale":
        imageColorHandler.applyGrayscale(currentImage);
        break;
      case "brightness":
        // Show brightness dialog and apply
        colorOperationsController.applyColorOperation(currentImage, operationType);
        break;
      case "contrast":
        // Show contrast dialog and apply
        colorOperationsController.applyColorOperation(currentImage, operationType);
        break;
      case "rgb_red":
        imageColorHandler.applyRGBExtraction(currentImage, RGBExtractionOperation.Channel.RED);
        break;
      case "rgb_green":
        imageColorHandler.applyRGBExtraction(currentImage, RGBExtractionOperation.Channel.GREEN);
        break;
      case "rgb_blue":
        imageColorHandler.applyRGBExtraction(currentImage, RGBExtractionOperation.Channel.BLUE);
        break;
      default:
        updateStatus("Unknown operation: " + operationType);
    }
  }

  // ImageColorHandlerCallback implementation

  @Override
  public void onProcessingStarted(String operationName) {
    updateStatus("Processing: " + operationName + "...");
  }

  @Override
  public void onProcessingCompleted(Image processedImage, String operationName, long processingTime) {
    imageCanvas.setImage(processedImage);
    updateStatus(operationName + " completed in " + processingTime + "ms");

    // Update title with operation indicator
    String currentTitle = getTitle();
    if (!currentTitle.contains("*")) {
      setTitle(currentTitle + " *");
    }
  }

  @Override
  public void onProcessingFailed(String operationName, String errorMessage) {
    updateStatus(operationName + " failed: " + errorMessage);
    dialogManager.showErrorDialog(operationName + " Failed", errorMessage);
  }

  @Override
  public void onInvalidParameters(String operationName, String errorMessage) {
    updateStatus(operationName + " - Invalid parameters: " + errorMessage);
    dialogManager.showErrorDialog("Invalid Parameters",
        operationName + " failed due to invalid parameters:\n" + errorMessage);
  }

  // Existing methods (unchanged)

  private void handleError(String errorMessage) {
    dialogManager.showErrorDialog("Error", errorMessage);
  }

  private void handleImageLoaded(Image image) {
    imageCanvas.setImage(image);
    setTitle("PDI - " + image.getOriginalFileName());

    menuManager.setImageLoaded(true);

    // Clear command history when loading a new image
    commandManager.clearHistory();
    updateUndoRedoState();
  }

  private void handleImageClosed() {
    imageCanvas.clearImage();
    setTitle("PDI - Digital Image Processing");

    menuManager.setImageLoaded(false);

    // Clear command history when closing image
    commandManager.clearHistory();
    updateUndoRedoState();
  }

  /**
   * Handles color-processed image updates from the old controller.
   * This maintains backward compatibility.
   * 
   * @param processedImage The processed image to display
   */
  private void handleColorProcessedImage(Image processedImage) {
    imageCanvas.setImage(processedImage);
  }

  /**
   * Gets the currently loaded image from the canvas.
   * 
   * @return Current image or null if none loaded
   */
  private Image getCurrentImage() {
    return imageCanvas.getCurrentImage();
  }

  /**
   * Shows the context menu at the specified location.
   */
  private void showContextMenu(MouseEvent e) {
    menuManager.getContextMenu().show(e.getComponent(), e.getX(), e.getY());
  }

  /**
   * Handles window closing event - cleanup if needed.
   */
  private void handleWindowClosing() {
    // Clear command history before exit
    commandManager.clearHistory();
    System.exit(0);
  }

  /**
   * Updates the status bar with a message.
   * 
   * @param message Message to display
   */
  private void updateStatus(String message) {
    statusLabel.setText(message);
  }

  /**
   * Gets the current image canvas for external access if needed.
   * 
   * @return The image canvas component
   */
  public ImageCanvas getImageCanvas() {
    return imageCanvas;
  }

  /**
   * Gets the command manager for external access if needed.
   * 
   * @return The command manager
   */
  public CommandManager getCommandManager() {
    return commandManager;
  }

  /**
   * Handles application exit.
   */
  private void exitApplication() {
    handleWindowClosing();
  }
}
