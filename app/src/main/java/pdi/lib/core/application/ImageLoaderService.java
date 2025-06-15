package pdi.lib.core.application;

import java.io.File;
import java.util.Optional;

import pdi.lib.core.domain.Image;
import pdi.lib.core.domain.ImageRepository;

/**
 * Application service for loading images from external sources.
 * 
 * This service orchestrates the loading process, coordinates with domain
 * objects,
 * and ensures business rules are followed. It acts as a facade for image
 * loading
 * operations and decouples the UI from the underlying implementation.
 */
public class ImageLoaderService {
  private final ImageLoader imageLoader;
  private final ImageRepository imageRepository;

  /**
   * Creates the service with required dependencies.
   * 
   * @param imageLoader     Component responsible for actual file loading
   * @param imageRepository Repository for storing loaded images
   */
  public ImageLoaderService(ImageLoader imageLoader, ImageRepository imageRepository) {
    this.imageLoader = imageLoader;
    this.imageRepository = imageRepository;
  }

  /**
   * Loads an image from a file and stores it in the repository.
   * 
   * @param file The image file to load
   * @return LoadImageResult containing the loaded image or error information
   */
  public LoadImageResult loadImage(File file) {
    try {
      // Validate file exists and is readable
      if (!file.exists() || !file.canRead()) {
        return LoadImageResult.failure("File does not exist or is not readable: " + file.getPath());
      }

      // Delegate actual loading to the loader component
      Optional<Image> loadedImage = imageLoader.loadFromFile(file);

      if (loadedImage.isEmpty()) {
        return LoadImageResult.failure("Failed to load image from file: " + file.getName());
      }

      // Store the loaded image
      Image savedImage = imageRepository.save(loadedImage.get());

      return LoadImageResult.success(savedImage);

    } catch (Exception e) {
      return LoadImageResult.failure("Unexpected error loading image: " + e.getMessage());
    }
  }

  /**
   * Retrieves an image from the repository by ID.
   * 
   * @param imageId The ID of the image to retrieve
   * @return Optional containing the image if found
   */
  public Optional<Image> getImage(String imageId) {
    return imageRepository.findById(imageId);
  }

  /**
   * Removes an image from the repository.
   * 
   * @param imageId The ID of the image to remove
   * @return true if image was removed successfully
   */
  public boolean removeImage(String imageId) {
    return imageRepository.deleteById(imageId);
  }
}
