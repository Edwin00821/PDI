package pdi.lib.core.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Image persistence operations.
 * 
 * This interface defines the contract for storing and retrieving images
 * without specifying the underlying storage mechanism (memory, database, file
 * system).
 * Following the Repository pattern from DDD.
 */
public interface ImageRepository {

  /**
   * Saves an image to the repository.
   * 
   * @param image The image to save
   * @return The saved image (may include generated ID if not provided)
   */
  Image save(Image image);

  /**
   * Finds an image by its unique identifier.
   * 
   * @param id The image ID to search for
   * @return Optional containing the image if found, empty otherwise
   */
  Optional<Image> findById(String id);

  /**
   * Retrieves all images currently stored in the repository.
   * 
   * @return List of all images (empty list if none exist)
   */
  List<Image> findAll();

  /**
   * Removes an image from the repository.
   * 
   * @param id The ID of the image to remove
   * @return true if image was removed, false if not found
   */
  boolean deleteById(String id);

  /**
   * Checks if an image with the given ID exists.
   * 
   * @param id The image ID to check
   * @return true if image exists, false otherwise
   */
  boolean existsById(String id);

  /**
   * Gets the total number of images in the repository.
   * 
   * @return Count of stored images
   */
  int count();
}
