package pdi.lib.core.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import pdi.lib.core.domain.Image;
import pdi.lib.core.domain.ImageMetadata;
import pdi.lib.core.domain.ImageRepository;

/**
 * Unit tests for ImageLoaderService.
 * 
 * These tests focus on the service's orchestration logic and interaction
 * with its dependencies, using mocks to isolate the unit under test.
 */
class ImageLoaderServiceTest {

  @Mock
  private ImageLoader mockImageLoader;

  @Mock
  private ImageRepository mockImageRepository;

  private ImageLoaderService imageLoaderService;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    imageLoaderService = new ImageLoaderService(mockImageLoader, mockImageRepository);
  }

  @Test
  @DisplayName("Should successfully load and save image")
  void shouldSuccessfullyLoadAndSaveImage() throws IOException {
    // Given
    File testFile = createTestFile("test.jpg");
    Image mockImage = createMockImage();
    Image savedImage = createMockImage();

    when(mockImageLoader.loadFromFile(testFile)).thenReturn(Optional.of(mockImage));
    when(mockImageRepository.save(mockImage)).thenReturn(savedImage);

    // When
    LoadImageResult result = imageLoaderService.loadImage(testFile);

    // Then
    assertTrue(result.isSuccess());
    assertEquals(savedImage, result.getImage());
    assertNull(result.getErrorMessage());

    verify(mockImageLoader).loadFromFile(testFile);
    verify(mockImageRepository).save(mockImage);
  }

  @Test
  @DisplayName("Should return failure when file does not exist")
  void shouldReturnFailureWhenFileDoesNotExist() {
    // Given
    File nonExistentFile = new File("non-existent.jpg");

    // When
    LoadImageResult result = imageLoaderService.loadImage(nonExistentFile);

    // Then
    assertTrue(result.isFailure());
    assertNull(result.getImage());
    assertTrue(result.getErrorMessage().contains("does not exist"));

    verifyNoInteractions(mockImageLoader);
    verifyNoInteractions(mockImageRepository);
  }

  @Test
  @DisplayName("Should return failure when image loader fails")
  void shouldReturnFailureWhenImageLoaderFails() throws IOException {
    // Given
    File testFile = createTestFile("test.jpg");

    when(mockImageLoader.loadFromFile(testFile)).thenReturn(Optional.empty());

    // When
    LoadImageResult result = imageLoaderService.loadImage(testFile);

    // Then
    assertTrue(result.isFailure());
    assertNull(result.getImage());
    assertTrue(result.getErrorMessage().contains("Failed to load image"));

    verify(mockImageLoader).loadFromFile(testFile);
    verifyNoInteractions(mockImageRepository);
  }

  @Test
  @DisplayName("Should handle unexpected exceptions gracefully")
  void shouldHandleUnexpectedExceptionsGracefully() throws IOException {
    // Given
    File testFile = createTestFile("test.jpg");

    when(mockImageLoader.loadFromFile(testFile)).thenThrow(new RuntimeException("Unexpected error"));

    // When
    LoadImageResult result = imageLoaderService.loadImage(testFile);

    // Then
    assertTrue(result.isFailure());
    assertNull(result.getImage());
    assertTrue(result.getErrorMessage().contains("Unexpected error"));
  }

  @Test
  @DisplayName("Should retrieve image from repository")
  void shouldRetrieveImageFromRepository() {
    // Given
    String imageId = "test-id";
    Image mockImage = createMockImage();

    when(mockImageRepository.findById(imageId)).thenReturn(Optional.of(mockImage));

    // When
    Optional<Image> result = imageLoaderService.getImage(imageId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(mockImage, result.get());

    verify(mockImageRepository).findById(imageId);
  }

  @Test
  @DisplayName("Should remove image from repository")
  void shouldRemoveImageFromRepository() {
    // Given
    String imageId = "test-id";

    when(mockImageRepository.deleteById(imageId)).thenReturn(true);

    // When
    boolean result = imageLoaderService.removeImage(imageId);

    // Then
    assertTrue(result);

    verify(mockImageRepository).deleteById(imageId);
  }

  // Helper methods
  private File createTestFile(String fileName) throws IOException {
    Path filePath = tempDir.resolve(fileName);
    Files.createFile(filePath);
    return filePath.toFile();
  }

  private Image createMockImage() {
    BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    ImageMetadata metadata = new ImageMetadata("JPEG", 1024L, 24, LocalDateTime.now(), "RGB");
    return new Image("test-id", "test.jpg", bufferedImage, metadata);
  }
}
