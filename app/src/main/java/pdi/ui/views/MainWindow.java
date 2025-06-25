package pdi.ui.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Objects;

import pdi.lib.core.application.ImageLoaderService;
import pdi.lib.core.application.LoadImageResult;
import pdi.lib.core.domain.Image;
import pdi.ui.components.DialogManager;
import pdi.ui.components.ImageCanvas;

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
  private JMenuBar menuBar;
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
    setupMenus();
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

    // Create status bar
    statusLabel = new JLabel("Ready");
    statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());

    // Initialize file chooser with image filters
    setupFileChooser();
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
   * Creates and sets up the menu system.
   */
  private void setupMenus() {
    menuBar = new JMenuBar();

    // File menu
    JMenu fileMenu = createFileMenu();
    menuBar.add(fileMenu);

    // Help menu (placeholder for future)
    JMenu helpMenu = createHelpMenu();
    menuBar.add(helpMenu);

    setJMenuBar(menuBar);
  }

  /**
   * Creates the File menu with basic operations.
   * 
   * @return Configured File menu
   */
  private JMenu createFileMenu() {
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('F');

    // Open image
    JMenuItem openItem = new JMenuItem("Open Image...");
    openItem.setMnemonic('O');
    openItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
    openItem.addActionListener(new OpenImageAction());
    fileMenu.add(openItem);

    fileMenu.addSeparator();

    // Close image
    JMenuItem closeItem = new JMenuItem("Close Image");
    closeItem.setMnemonic('C');
    closeItem.addActionListener(new CloseImageAction());
    fileMenu.add(closeItem);

    fileMenu.addSeparator();

    // Exit
    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.setMnemonic('x');
    exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
    exitItem.addActionListener(e -> System.exit(0));
    fileMenu.add(exitItem);

    return fileMenu;
  }

  /**
   * Creates the Help menu.
   * 
   * @return Configured Help menu
   */
  private JMenu createHelpMenu() {
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic('H');

    // About
    JMenuItem aboutItem = new JMenuItem("About");
    aboutItem.setMnemonic('A');
    aboutItem.addActionListener(e -> dialogManager.showAboutDialog());
    helpMenu.add(aboutItem);

    return helpMenu;
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
   * Action handler for opening images.
   */
  private class OpenImageAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      int result = fileChooser.showOpenDialog(MainWindow.this);

      if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        loadImage(selectedFile);
      }
    }
  }

  /**
   * Action handler for closing the current image.
   */
  private class CloseImageAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      closeCurrentImage();
    }
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
}
