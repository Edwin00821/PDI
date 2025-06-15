package pdi.lib.core.infrastructure;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import pdi.lib.core.domain.Image;
import pdi.lib.core.domain.ImageRepository;

/**
 * In-memory implementation of ImageRepository.
 * 
 * This implementation stores images in memory using a thread-safe map.
 * Suitable for applications where images don't need to persist between
 * sessions.
 * For production use, consider implementing a database-backed repository.
 */
public class InMemoryImageRepository implements ImageRepository {

  // Using ConcurrentHashMap for thread safety
  private final Map<String, Image> images = new ConcurrentHashMap<>();

  @Override
  public Image save(Image image) {
    Objects.requireNonNull(image, "Image cannot be null");
    images.put(image.getId(), image);
    return image;
  }

  @Override
  public Optional<Image> findById(String id) {
    Objects.requireNonNull(id, "Image ID cannot be null");
    return Optional.ofNullable(images.get(id));
  }

  @Override
  public List<Image> findAll() {
    return new ArrayList<>(images.values());
  }

  @Override
  public boolean deleteById(String id) {
    Objects.requireNonNull(id, "Image ID cannot be null");
    return images.remove(id) != null;
  }

  @Override
  public boolean existsById(String id) {
    Objects.requireNonNull(id, "Image ID cannot be null");
    return images.containsKey(id);
  }

  @Override
  public int count() {
    return images.size();
  }

  /**
   * Clears all images from the repository.
   * Useful for testing or resetting the application state.
   */
  public void clear() {
    images.clear();
  }
}
