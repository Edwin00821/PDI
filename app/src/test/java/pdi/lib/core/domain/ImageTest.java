package pdi.lib.core.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

/**
 * Unit tests for the Image domain entity.
 * 
 * These tests focus on the core business logic and behavior of the Image
 * entity,
 * ensuring it maintains its invariants and provides correct functionality.
 */
class ImageTest {

  private BufferedImage testImageData;
  private ImageMetadata testMetadata;

  @BeforeEach
  void setUp() {
    // Create a simple test image (100x100 RGB)
    testImageData = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

    testMetadata = new ImageMetadata(
        "JPEG",
        1024L,
        24,
        LocalDateTime.now(),
        "RGB");
  }

  @Test
  @DisplayName("Should create image with valid parameters")
  void shouldCreateImageWithValidParameters() {
    // Given
    String id = "test-id";
    String fileName = "test.jpg";

    // When
    Image image = new Image(id, fileName, testImageData, testMetadata);

    // Then
    assertEquals(id, image.getId());
    assertEquals(fileName, image.getOriginalFileName());
    assertEquals(testImageData, image.getImageData());
    assertEquals(testMetadata, image.getMetadata());
  }

  @Test
  @DisplayName("Should provide convenience methods for image dimensions")
  void shouldProvideConvenienceMethodsForImageDimensions() {
    // Given
    Image image = new Image("id", "test.jpg", testImageData, testMetadata);

    // When & Then
    assertEquals(100, image.getWidth());
    assertEquals(100, image.getHeight());
    assertEquals(BufferedImage.TYPE_INT_RGB, image.getType());
  }

  @Test
  @DisplayName("Should throw exception when id is null")
  void shouldThrowExceptionWhenIdIsNull() {
    // When & Then
    assertThrows(NullPointerException.class, () -> new Image(null, "test.jpg", testImageData, testMetadata));
  }

  @Test
  @DisplayName("Should throw exception when filename is null")
  void shouldThrowExceptionWhenFilenameIsNull() {
    // When & Then
    assertThrows(NullPointerException.class, () -> new Image("id", null, testImageData, testMetadata));
  }

  @Test
  @DisplayName("Should throw exception when image data is null")
  void shouldThrowExceptionWhenImageDataIsNull() {
    // When & Then
    assertThrows(NullPointerException.class, () -> new Image("id", "test.jpg", null, testMetadata));
  }

  @Test
  @DisplayName("Should throw exception when metadata is null")
  void shouldThrowExceptionWhenMetadataIsNull() {
    // When & Then
    assertThrows(NullPointerException.class, () -> new Image("id", "test.jpg", testImageData, null));
  }

  @Test
  @DisplayName("Should create deep copy of image")
  void shouldCreateDeepCopyOfImage() {
    // Given
    Image original = new Image("original-id", "test.jpg", testImageData, testMetadata);

    // When
    Image copy = original.copy();

    // Then
    assertNotEquals(original.getId(), copy.getId());
    assertEquals(original.getOriginalFileName(), copy.getOriginalFileName());
    assertEquals(original.getMetadata(), copy.getMetadata());
    assertNotSame(original.getImageData(), copy.getImageData());
    assertEquals(original.getWidth(), copy.getWidth());
    assertEquals(original.getHeight(), copy.getHeight());
  }

  @Test
  @DisplayName("Should implement equals and hashCode based on ID")
  void shouldImplementEqualsAndHashCodeBasedOnId() {
    // Given
    Image image1 = new Image("same-id", "test1.jpg", testImageData, testMetadata);
    Image image2 = new Image("same-id", "test2.jpg", testImageData, testMetadata);
    Image image3 = new Image("different-id", "test1.jpg", testImageData, testMetadata);

    // When & Then
    assertEquals(image1, image2);
    assertNotEquals(image1, image3);
    assertEquals(image1.hashCode(), image2.hashCode());
    assertNotEquals(image1.hashCode(), image3.hashCode());
  }

  @Test
  @DisplayName("Should provide meaningful toString representation")
  void shouldProvideMeaningfulToStringRepresentation() {
    // Given
    Image image = new Image("test-id", "test.jpg", testImageData, testMetadata);

    // When
    String result = image.toString();

    // Then
    assertTrue(result.contains("test-id"));
    assertTrue(result.contains("test.jpg"));
    assertTrue(result.contains("100x100"));
  }
}
