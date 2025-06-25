package pdi;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import pdi.lib.color.application.ColorProcessorService;
import pdi.lib.core.application.ImageLoaderService;
import pdi.lib.core.infrastructure.FileImageLoader;
import pdi.lib.core.infrastructure.InMemoryImageRepository;
import pdi.ui.views.MainWindow;

/**
 * Main application class for the PDI (Digital Image Processing) application.
 * 
 * This class serves as the entry point and bootstrap for the application.
 * It follows Clean Architecture principles by setting up the dependency
 * injection manually (in a more complex app, you might use a DI container).
 * 
 * The class is responsible for:
 * - Configuring the application environment
 * - Setting up the dependency graph
 * - Initializing the UI on the Event Dispatch Thread
 * - Handling any startup errors gracefully
 */
public class App {
  /**
   * Application entry point.
   * 
   * @param args Command line arguments (currently unused)
   */
  public static void main(String[] args) {
    // Configure system properties for better UI experience
    configureSystemProperties();

    // Set up the look and feel
    setupLookAndFeel();

    // Initialize and start the application on the EDT
    SwingUtilities.invokeLater(() -> {
      try {
        startApplication();
      } catch (Exception e) {
        handleStartupError(e);
      }
    });
  }

  /**
   * Configures system properties for optimal UI experience.
   */
  private static void configureSystemProperties() {
    // Enable hardware acceleration if available
    System.setProperty("sun.java2d.opengl", "true");

    // Improve font rendering on various platforms
    System.setProperty("awt.useSystemAAFontSettings", "on");
    System.setProperty("swing.aatext", "true");

    // Set application name for better OS integration
    System.setProperty("apple.awt.application.name", "PDI");
  }

  /**
   * Sets up the Look and Feel for the application.
   * Tries to use the system L&F for better OS integration.
   */
  private static void setupLookAndFeel() {
    try {
      // Try to use the system look and feel
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e) {
      // If system L&F fails, log the issue but continue with default
      System.err.println("Warning: Could not set system Look and Feel: " + e.getMessage());
      System.err.println("Using default Look and Feel instead.");
    }
  }

  /**
   * Initializes and starts the main application.
   * This method sets up the dependency injection manually.
   */
  private static void startApplication() {
    System.out.println("Starting PDI Application...");

    // Create infrastructure layer components
    // These are the "outer" layers that depend on frameworks/libraries
    FileImageLoader imageLoader = new FileImageLoader();
    InMemoryImageRepository imageRepository = new InMemoryImageRepository();

    // Create application layer services
    // These coordinate between UI and domain, implementing use cases
    ImageLoaderService imageLoaderService = new ImageLoaderService(imageLoader, imageRepository);
    ColorProcessorService colorProcessorService = new ColorProcessorService();

    // Create and show the main UI
    // The UI layer depends on application services but not on infrastructure
    // directly
    MainWindow mainWindow = new MainWindow(imageLoaderService, colorProcessorService);

    // Make the window visible
    mainWindow.setVisible(true);

    System.out.println("PDI Application started successfully.");

    // Print some useful information for debugging
    printApplicationInfo();
  }

  /**
   * Prints useful application information for debugging and monitoring.
   */
  private static void printApplicationInfo() {
    System.out.println("=== PDI Application Information ===");
    System.out.println("Java Version: " + System.getProperty("java.version"));
    System.out.println("Operating System: " + System.getProperty("os.name") +
        " " + System.getProperty("os.version"));
    System.out.println("Look and Feel: " + UIManager.getLookAndFeel().getName());
    System.out.println("Available Processors: " + Runtime.getRuntime().availableProcessors());
    System.out.println("Max Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
    System.out.println("================================");
  }

  /**
   * Handles startup errors gracefully.
   * 
   * @param error The exception that occurred during startup
   */
  private static void handleStartupError(Exception error) {
    System.err.println("Fatal error starting PDI Application:");
    error.printStackTrace();

    // Try to show an error dialog if possible
    try {
      javax.swing.JOptionPane.showMessageDialog(
          null,
          "A fatal error occurred while starting the application:\n\n" +
              error.getMessage() + "\n\n" +
              "Please check the console for detailed error information.",
          "PDI Application - Startup Error",
          javax.swing.JOptionPane.ERROR_MESSAGE);
    } catch (Exception dialogError) {
      // If we can't even show a dialog, just print to stderr
      System.err.println("Could not display error dialog: " + dialogError.getMessage());
    }

    // Exit with error code
    System.exit(1);
  }
}
