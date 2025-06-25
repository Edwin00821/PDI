package pdi.ui.views;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import pdi.lib.color.application.ColorProcessorService;
import pdi.lib.core.application.ImageLoaderService;
import pdi.lib.core.domain.Image;
import pdi.ui.components.DialogManager;
import pdi.ui.components.ImageCanvas;
import pdi.ui.components.MenuManager;
import pdi.ui.controllers.ColorOperationsController;
import pdi.ui.handlers.FileOperationHandler;

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
 */
public class MainWindow extends JFrame {

  // UI Components
  private ImageCanvas imageCanvas;
  private DialogManager dialogManager;
  private MenuManager menuManager;
  private JLabel statusLabel;

  // Application services
  private final ImageLoaderService imageLoaderService;
  private final ColorProcessorService colorProcessorService;

  // Handlers
  private FileOperationHandler fileOperationHandler;

  // Controllers
  private ColorOperationsController colorOperationsController;

  /**
   * Creates the main application window.
   * 
   * @param imageLoaderService Service for loading and managing images
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
   * Creates and initializes handlers.
   */
  private void createComponents() {
    // Create main image canvas
    imageCanvas = new ImageCanvas();
    dialogManager = new DialogManager(this);

    // Create menu manager
    menuManager = new MenuManager();
    setJMenuBar(menuManager.getMenuBar());

    // Create status bar
    statusLabel = new JLabel("Ready");
    statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
  }

  /**
   * Creates and initializes UI components.
   */
  private void setupHandlers() {
    // Initialize file operation handler
    fileOperationHandler = new FileOperationHandler(imageLoaderService, this);

    fileOperationHandler.setStatusUpdateCallback(this::updateStatus);
    fileOperationHandler.setImageLoadedCallback(this::handleImageLoaded);
    fileOperationHandler.setErrorCallback(this::handleError);
    fileOperationHandler.setImageClosedCallback(this::handleImageClosed);

    // Initialize color operations controller
    colorOperationsController = new ColorOperationsController(dialogManager, colorProcessorService);

    colorOperationsController.setImageUpdateCallback(this::handleColorProcessedImage);
    colorOperationsController.setStatusUpdateCallback(this::updateStatus);
    colorOperationsController.setTitleUpdateCallback(this::setTitle);

  }

  /**
   * Sets up callbacks for menu actions.
   */
  private void setupMenuCallbacks() {
    menuManager.setOnOpenImage(fileOperationHandler::openImage);
    menuManager.setOnCloseImage(fileOperationHandler::closeCurrentImage);

    menuManager.setOnAbout(dialogManager::showAboutDialog);
    menuManager.setOnExit(this::exitApplication);

    menuManager.setOnGrayscale(() -> applyColorOperation("grayscale"));
    menuManager.setOnBrightness(() -> applyColorOperation("brightness"));
    menuManager.setOnContrast(() -> applyColorOperation("contrast"));
    menuManager.setOnRedChannel(() -> applyColorOperation("rgb_red"));
    menuManager.setOnGreenChannel(() -> applyColorOperation("rgb_green"));
    menuManager.setOnBlueChannel(() -> applyColorOperation("rgb_blue"));

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

  private void handleError(String errorMessage) {
    dialogManager.showErrorDialog("Error", errorMessage);
  }

  private void handleImageLoaded(Image image) {
    imageCanvas.setImage(image);
    setTitle("PDI - " + image.getOriginalFileName());

    menuManager.setImageLoaded(true);
  }

  private void handleImageClosed() {
    imageCanvas.clearImage();
    setTitle("PDI - Digital Image Processing");

    menuManager.setImageLoaded(false);
  }

  /**
   * Handles color-processed image updates.
   * 
   * @param processedImage The processed image to display
   */
  private void handleColorProcessedImage(Image processedImage) {
    imageCanvas.setImage(processedImage);
  }

  /**
   * Applies a color operation to the currently loaded image.
   * 
   * @param operationType Type of operation to apply
   */
  private void applyColorOperation(String operationType) {
    Image currentImage = getCurrentImage();
    if (currentImage != null) {
      colorOperationsController.applyColorOperation(currentImage, operationType);
    }
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
    // In the future, we might want to ask about saving unsaved work
    // For now, just clean exit
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
   * Handles application exit.
   */
  private void exitApplication() {
    handleWindowClosing();
  }
}
