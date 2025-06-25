package pdi.ui.views;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Objects;

import pdi.lib.core.application.ImageLoaderService;
import pdi.lib.core.application.LoadImageResult;
import pdi.lib.core.domain.Image;
import pdi.ui.components.DialogManager;
import pdi.ui.components.ImageCanvas;
import pdi.ui.components.MenuManager;

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

  // File chooser for image selection (reusable)
  private JFileChooser fileChooser;

  /**
   * Creates the main application window.
   * 
   * @param imageLoaderService Service for loading and managing images
   */
  public MainWindow(ImageLoaderService imageLoaderService) {
    this.imageLoaderService = Objects.requireNonNull(imageLoaderService,
        "ImageLoaderService cannot be null");

    initializeWindow();
    createComponents();
    setupLayout();
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
   * Creates and initializes UI components.
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

    // Initialize file chooser with image filters
    setupFileChooser();
  }

  /**
   * Sets up callbacks for menu actions.
   */
  private void setupMenuCallbacks() {
    menuManager.setOnOpenImage(this::openImage);
    menuManager.setOnCloseImage(this::closeCurrentImage);
    menuManager.setOnExit(this::exitApplication);
    menuManager.setOnAbout(dialogManager::showAboutDialog);

  }

  /**
   * Sets up the file chooser with appropriate filters for image files.
   */
  private void setupFileChooser() {
    fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

    // Add file filter for supported image formats
    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
      @Override
      public boolean accept(File file) {
        if (file.isDirectory()) {
          return true;
        }

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
            fileName.endsWith(".png") || fileName.endsWith(".gif") ||
            fileName.endsWith(".bmp") || fileName.endsWith(".wbmp");
      }

      @Override
      public String getDescription() {
        return "Image Files (*.jpg, *.png, *.gif, *.bmp)";
      }
    });
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
   * Handles the open image action.
   */
  private void openImage() {
    int result = fileChooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      loadImage(selectedFile);
    }
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
   * Loads an image from the specified file.
   * 
   * @param file File containing the image to load
   */
  private void loadImage(File file) {
    updateStatus("Loading image: " + file.getName() + "...");

    try {
      // Use the service to load the image
      LoadImageResult result = imageLoaderService.loadImage(file);

      if (result.isSuccess()) {
        Image loadedImage = result.getImage();
        imageCanvas.setImage(loadedImage);

        updateStatus("Image loaded: " + loadedImage.getOriginalFileName() +
            " (" + loadedImage.getWidth() + "x" + loadedImage.getHeight() + ")");

        // Update window title to include filename
        setTitle("PDI - " + loadedImage.getOriginalFileName());

      } else {
        dialogManager.showErrorDialog("Error Loading Image", result.getErrorMessage());
        updateStatus("Failed to load image: " + file.getName());
      }

    } catch (Exception ex) {
      dialogManager.showErrorDialog("Unexpected Error",
          "An unexpected error occurred while loading the image: " + ex.getMessage());
      updateStatus("Error occurred while loading image");
    }
  }

  /**
   * Closes the currently displayed image.
   */
  private void closeCurrentImage() {
    if (imageCanvas.hasImage()) {
      imageCanvas.clearImage();
      setTitle("PDI - Digital Image Processing");
      updateStatus("Image closed");
    }
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
