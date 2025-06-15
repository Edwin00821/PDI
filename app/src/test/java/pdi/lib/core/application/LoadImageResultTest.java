package pdi.lib.core.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

import pdi.lib.core.domain.Image;
import pdi.lib.core.domain.ImageMetadata;

/**
 * Unit tests for LoadImageResult.
 * 
 * Tests the Result pattern implementation to ensure proper
 * success/failure state management.
 */
class LoadImageResultTest {

  @Test
  @DisplayName("Should create successful result with image")
  void shouldCreateSuccessfulResultWithImage() {
    // Given
    Image testImage = createTestImage();

    // When
    LoadImageResult result = LoadImageResult.success(testImage);

    // Then
    assertTrue(result.isSuccess());
    assertFalse(result.isFailure());
    assertEquals(testImage, result.getImage());
    assertNull(result.getErrorMessage());
  }

  @Test
  @DisplayName("Should create failure result with error message")
  void shouldCreateFailureResultWithErrorMessage() {
    // Given
    String errorMessage = "Something went wrong";

    // When
    LoadImageResult result = LoadImageResult.failure(errorMessage);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.isFailure());
    assertNull(result.getImage());
    assertEquals(errorMessage, result.getErrorMessage());
  }

  @Test
  @DisplayName("Should provide meaningful toString for success")
  void shouldProvideMeaningfulToStringForSuccess() {
    // Given
    Image testImage = createTestImage();
    LoadImageResult result = LoadImageResult.success(testImage);

    // When
    String stringRepresentation = result.toString();

    // Then
    assertTrue(stringRepresentation.contains("success=true"));
    assertTrue(stringRepresentation.contains("image="));
  }

  @Test
  @DisplayName("Should provide meaningful toString for failure")
  void shouldProvideMeaningfulToStringForFailure() {
    // Given
    String errorMessage = "Test error";
    LoadImageResult result = LoadImageResult.failure(errorMessage);

    // When
    String stringRepresentation = result.toString();

    // Then
    assertTrue(stringRepresentation.contains("success=false"));
    assertTrue(stringRepresentation.contains("Test error"));
  }

  private Image createTestImage() {
    BufferedImage bufferedImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
    ImageMetadata metadata = new ImageMetadata("PNG", 512L, 24, LocalDateTime.now(), "RGB");
    return new Image("test-id", "test.png", bufferedImage, metadata);
  }
}
