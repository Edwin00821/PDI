package pdi.lib.core.application;

import java.io.File;
import java.util.Optional;

import pdi.lib.core.domain.Image;

/**
 * Interface for loading images from external sources.
 * 
 * This abstraction allows different implementations for loading images
 * (from files, URLs, streams, etc.) while keeping the application service
 * independent of the specific loading mechanism.
 */
public interface ImageLoader {

  /**
   * Loads an image from a file.
   * 
   * @param file The file containing the image data
   * @return Optional containing the loaded Image, empty if loading failed
   */
  Optional<Image> loadFromFile(File file);

  /**
   * Checks if the loader supports the given file type.
   * 
   * @param file The file to check
   * @return true if this loader can handle the file type
   */
  boolean supportsFile(File file);

  /**
   * Gets the supported file extensions for this loader.
   * 
   * @return Array of supported extensions (e.g., ["jpg", "jpeg", "png"])
   */
  String[] getSupportedExtensions();
}
