package pdi.lib.core.domain;

import org.junit.jupiter.api.Test;


import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;


/**
 * Unit tests for the ImageMetadata value object.
 * 
 * Tests focus on ensuring the value object behaves correctly and
 * maintains immutability principles.
 */
class ImageMetadataTest {

  @Test
  @DisplayName("Should create metadata with all properties")
  void shouldCreateMetadataWithAllProperties() {
    // Given
    String format = "JPEG";
    long fileSize = 2048L;
    int bitDepth = 24;
    LocalDateTime loadedAt = LocalDateTime.now();
    String colorSpace = "RGB";

    // When
    ImageMetadata metadata = new ImageMetadata(format, fileSize, bitDepth, loadedAt, colorSpace);

    // Then
    assertEquals(format, metadata.getFormat());
    assertEquals(fileSize, metadata.getFileSizeBytes());
    assertEquals(bitDepth, metadata.getBitDepth());
    assertEquals(loadedAt, metadata.getLoadedAt());
    assertEquals(colorSpace, metadata.getColorSpace());
  }

  @Test
  @DisplayName("Should provide meaningful toString representation")
  void shouldProvideMeaningfulToStringRepresentation() {
    // Given
    ImageMetadata metadata = new ImageMetadata("PNG", 1024L, 32, LocalDateTime.now(), "ARGB");

    // When
    String result = metadata.toString();

    // Then
    assertTrue(result.contains("PNG"));
    assertTrue(result.contains("1024 bytes"));
    assertTrue(result.contains("32"));
    assertTrue(result.contains("ARGB"));
  }
}
