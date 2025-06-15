package pdi.lib.core.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import pdi.lib.core.domain.Image;
import pdi.lib.core.domain.ImageMetadata;

/**
 * Unit tests for InMemoryImageRepository.
 * 
 * Tests the repository implementation to ensure it correctly
 * manages image storage and retrieval operations.
 */
class InMemoryImageRepositoryTest {

  private InMemoryImageRepository repository;
  private Image testImage;

  @BeforeEach
  void setUp() {
    repository = new InMemoryImageRepository();
    testImage = createTestImage("test-id", "test.jpg");
  }

  @Test
  @DisplayName("Should save and retrieve image by ID")
  void shouldSaveAndRetrieveImageById() {
    // When
    Image savedImage = repository.save(testImage);
    Optional<Image> retrievedImage = repository.findById("test-id");

    // Then
    assertEquals(testImage, savedImage);
    assertTrue(retrievedImage.isPresent());
    assertEquals(testImage, retrievedImage.get());
  }

  @Test
  @DisplayName("Should return empty optional for non-existent ID")
  void shouldReturnEmptyOptionalForNonExistentId() {
    // When
    Optional<Image> result = repository.findById("non-existent");

    // Then
    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Should find all saved images")
  void shouldFindAllSavedImages() {
    // Given
    Image image1 = createTestImage("id1", "file1.jpg");
    Image image2 = createTestImage("id2", "file2.jpg");

    repository.save(image1);
    repository.save(image2);

    // When
    List<Image> allImages = repository.findAll();

    // Then
    assertEquals(2, allImages.size());
    assertTrue(allImages.contains(image1));
    assertTrue(allImages.contains(image2));
  }

  @Test
  @DisplayName("Should return empty list when no images exist")
  void shouldReturnEmptyListWhenNoImagesExist() {
    // When
    List<Image> allImages = repository.findAll();

    // Then
    assertTrue(allImages.isEmpty());
  }

  @Test
  @DisplayName("Should delete image by ID")
  void shouldDeleteImageById() {
    // Given
    repository.save(testImage);
    assertTrue(repository.existsById("test-id"));

    // When
    boolean deleted = repository.deleteById("test-id");

    // Then
    assertTrue(deleted);
    assertFalse(repository.existsById("test-id"));
    assertTrue(repository.findById("test-id").isEmpty());
  }

  @Test
  @DisplayName("Should return false when deleting non-existent image")
  void shouldReturnFalseWhenDeletingNonExistentImage() {
    // When
    boolean deleted = repository.deleteById("non-existent");

    // Then
    assertFalse(deleted);
  }

  @Test
  @DisplayName("Should check if image exists by ID")
  void shouldCheckIfImageExistsById() {
    // Given
    repository.save(testImage);

    // When & Then
    assertTrue(repository.existsById("test-id"));
    assertFalse(repository.existsById("non-existent"));
  }

  @Test
  @DisplayName("Should count stored images")
  void shouldCountStoredImages() {
    // Given
    assertEquals(0, repository.count());

    repository.save(testImage);
    assertEquals(1, repository.count());

    repository.save(createTestImage("id2", "file2.jpg"));
    assertEquals(2, repository.count());

    repository.deleteById("test-id");
    assertEquals(1, repository.count());
  }

  @Test
  @DisplayName("Should clear all images")
  void shouldClearAllImages() {
    // Given
    repository.save(testImage);
    repository.save(createTestImage("id2", "file2.jpg"));
    assertEquals(2, repository.count());

    // When
    repository.clear();

    // Then
    assertEquals(0, repository.count());
    assertTrue(repository.findAll().isEmpty());
  }

  @Test
  @DisplayName("Should throw exception when saving null image")
  void shouldThrowExceptionWhenSavingNullImage() {
    // When & Then
    assertThrows(NullPointerException.class, () -> repository.save(null));
  }

  @Test
  @DisplayName("Should throw exception when finding by null ID")
  void shouldThrowExceptionWhenFindingByNullId() {
    // When & Then
    assertThrows(NullPointerException.class, () -> repository.findById(null));
  }

  @Test
  @DisplayName("Should update existing image when saving with same ID")
  void shouldUpdateExistingImageWhenSavingWithSameId() {
    // Given
    repository.save(testImage);
    Image updatedImage = createTestImage("test-id", "updated.jpg");

    // When
    repository.save(updatedImage);

    // Then
    assertEquals(1, repository.count());
    Optional<Image> retrieved = repository.findById("test-id");
    assertTrue(retrieved.isPresent());
    assertEquals("updated.jpg", retrieved.get().getOriginalFileName());
  }

  // Helper method
  private Image createTestImage(String id, String filename) {
    BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    ImageMetadata metadata = new ImageMetadata("JPEG", 1024L, 24, LocalDateTime.now(), "RGB");
    return new Image(id, filename, bufferedImage, metadata);
  }
}
