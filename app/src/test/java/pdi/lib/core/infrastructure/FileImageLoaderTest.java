package pdi.lib.core.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import pdi.lib.core.domain.Image;

/**
 * Comprehensive unit tests for FileImageLoader.
 * 
 * This test suite uses real image files created programmatically to ensure
 * proper image loading and metadata extraction. Tests cover both successful
 * loading scenarios and error handling cases.
 * 
 * Key features:
 * - Uses @TempDir for clean test isolation
 * - Creates actual image files for realistic testing
 * - Tests all supported formats with real image data
 * - Validates metadata extraction accuracy
 * - Covers edge cases and error conditions
 */
class FileImageLoaderTest {

  private FileImageLoader fileImageLoader;

  @TempDir
  Path tempDir;

  /**
   * Set up test environment before each test.
   */
  @BeforeEach
  void setUp() {
    fileImageLoader = new FileImageLoader();
  }

  // ========== SUCCESSFUL LOADING TESTS ==========

  @Test
  @DisplayName("Should load valid PNG image file successfully")
  void shouldLoadValidPngImageFileSuccessfully() throws IOException {
    // Given
    File imageFile = createTestImageFile("test.png", 50, 50, Color.BLUE);

    // When
    Optional<Image> result = fileImageLoader.loadFromFile(imageFile);

    // Then
    assertTrue(result.isPresent(), "Image should be loaded successfully");

    Image image = result.get();
    assertNotNull(image.getId(), "Image ID should not be null");
    assertEquals("test.png", image.getOriginalFileName(), "Original filename should match");
    assertEquals(50, image.getWidth(), "Image width should be 50");
    assertEquals(50, image.getHeight(), "Image height should be 50");

    // Validate metadata
    assertNotNull(image.getMetadata(), "Metadata should not be null");
    assertEquals("PNG", image.getMetadata().getFormat(), "Format should be PNG");
    assertTrue(image.getMetadata().getFileSizeBytes() > 0, "File size should be positive");
    assertNotNull(image.getMetadata().getLoadedAt(), "Load timestamp should be set");
    assertNotNull(image.getMetadata().getColorSpace(), "Color space should be set");
  }

  @Test
  @DisplayName("Should load valid JPEG image file successfully")
  void shouldLoadValidJpegImageFileSuccessfully() throws IOException {
    // Given
    File imageFile = createTestImageFile("test.jpg", 100, 75, Color.RED);

    // When
    Optional<Image> result = fileImageLoader.loadFromFile(imageFile);

    // Then
    assertTrue(result.isPresent(), "JPEG image should be loaded successfully");

    Image image = result.get();
    assertEquals("test.jpg", image.getOriginalFileName(), "Original filename should match");
    assertEquals(100, image.getWidth(), "Image width should be 100");
    assertEquals(75, image.getHeight(), "Image height should be 75");
    assertEquals("JPG", image.getMetadata().getFormat(), "Format should be JPG");
  }

  @Test
  @DisplayName("Should load all supported image formats")
  void shouldLoadAllSupportedImageFormats() throws IOException {
    // Given - Create test images for all supported formats
    String[] formats = { "jpg", "jpeg", "png", "gif", "bmp" };

    for (String format : formats) {
      // Given
      File imageFile = createTestImageFile("test." + format, 30, 30, Color.GREEN);

      // When
      Optional<Image> result = fileImageLoader.loadFromFile(imageFile);

      // Then
      assertTrue(result.isPresent(),
          "Should successfully load " + format.toUpperCase() + " format");

      Image image = result.get();
      assertEquals("test." + format, image.getOriginalFileName(),
          "Filename should match for " + format);
      assertEquals(30, image.getWidth(),
          "Width should be correct for " + format);
      assertEquals(30, image.getHeight(),
          "Height should be correct for " + format);

      String expectedFormat = format.equals("jpeg") ? "JPEG" : format.toUpperCase();
      if (format.equals("jpg"))
        expectedFormat = "JPG";
      assertEquals(expectedFormat, image.getMetadata().getFormat(),
          "Metadata format should be correct for " + format);
    }
  }

  // ========== ERROR HANDLING TESTS ==========

  @Test
  @DisplayName("Should return empty optional for non-existent file")
  void shouldReturnEmptyOptionalForNonExistentFile() {
    // Given
    File nonExistentFile = new File(tempDir.toFile(), "non-existent.jpg");

    // When
    Optional<Image> result = fileImageLoader.loadFromFile(nonExistentFile);

    // Then
    assertFalse(result.isPresent(),
        "Should return empty Optional for non-existent file");
  }

  @Test
  @DisplayName("Should return empty optional for unsupported format")
  void shouldReturnEmptyOptionalForUnsupportedFormat() throws IOException {
    // Given
    File unsupportedFile = createTextFile("test.txt", "This is not an image");

    // When
    Optional<Image> result = fileImageLoader.loadFromFile(unsupportedFile);

    // Then
    assertFalse(result.isPresent(),
        "Should return empty Optional for unsupported format");
  }

  @Test
  @DisplayName("Should return empty optional for corrupted image file")
  void shouldReturnEmptyOptionalForCorruptedImageFile() throws IOException {
    // Given - Create a file with image extension but invalid content
    File corruptedFile = createTextFile("corrupted.jpg", "This is not valid image data");

    // When
    Optional<Image> result = fileImageLoader.loadFromFile(corruptedFile);

    // Then
    assertFalse(result.isPresent(),
        "Should return empty Optional for corrupted image file");
  }

  @Test
  @DisplayName("Should throw exception when loading from null file")
  void shouldThrowExceptionWhenLoadingFromNullFile() {
    // When & Then
    assertThrows(NullPointerException.class,
        () -> fileImageLoader.loadFromFile(null),
        "Should throw NullPointerException for null file");
  }

  // ========== FILE SUPPORT VALIDATION TESTS ==========

  @Test
  @DisplayName("Should support common image formats")
  void shouldSupportCommonImageFormats() throws IOException {
    // Given
    File jpgFile = createTestImageFile("test.jpg", 10, 10, Color.BLACK);
    File pngFile = createTestImageFile("test.png", 10, 10, Color.WHITE);
    File gifFile = createTestImageFile("test.gif", 10, 10, Color.YELLOW);
    File bmpFile = createTestImageFile("test.bmp", 10, 10, Color.CYAN);

    // When & Then
    assertTrue(fileImageLoader.supportsFile(jpgFile), "Should support JPG files");
    assertTrue(fileImageLoader.supportsFile(pngFile), "Should support PNG files");
    assertTrue(fileImageLoader.supportsFile(gifFile), "Should support GIF files");
    assertTrue(fileImageLoader.supportsFile(bmpFile), "Should support BMP files");
  }

  @Test
  @DisplayName("Should not support non-image files")
  void shouldNotSupportNonImageFiles() throws IOException {
    // Given
    File textFile = createTextFile("test.txt", "text content");
    File execFile = createTextFile("test.exe", "executable content");
    File pdfFile = createTextFile("document.pdf", "pdf content");
    File docFile = createTextFile("document.doc", "document content");

    // When & Then
    assertFalse(fileImageLoader.supportsFile(textFile), "Should not support TXT files");
    assertFalse(fileImageLoader.supportsFile(execFile), "Should not support EXE files");
    assertFalse(fileImageLoader.supportsFile(pdfFile), "Should not support PDF files");
    assertFalse(fileImageLoader.supportsFile(docFile), "Should not support DOC files");
  }

  @Test
  @DisplayName("Should not support null or non-existent files")
  void shouldNotSupportNullOrNonExistentFiles() {
    // Given
    File nonExistentFile = new File(tempDir.toFile(), "non-existent.jpg");

    // When & Then
    assertFalse(fileImageLoader.supportsFile(null),
        "Should not support null file");
    assertFalse(fileImageLoader.supportsFile(nonExistentFile),
        "Should not support non-existent file");
  }

  @Test
  @DisplayName("Should not support directories")
  void shouldNotSupportDirectories() throws IOException {
    // Given
    File directory = Files.createDirectory(tempDir.resolve("testdir")).toFile();

    // When & Then
    assertFalse(fileImageLoader.supportsFile(directory),
        "Should not support directories");
  }

  @Test
  @DisplayName("Should handle case-insensitive file extensions")
  void shouldHandleCaseInsensitiveFileExtensions() throws IOException {
    // Given
    File upperCaseFile = createTestImageFile("TEST.PNG", 20, 20, Color.MAGENTA);
    File lowerCaseFile = createTestImageFile("test.jpg", 20, 20, Color.ORANGE);
    File mixedCaseFile = createTestImageFile("Test.JpEg", 20, 20, Color.PINK);

    // When & Then
    assertTrue(fileImageLoader.supportsFile(upperCaseFile),
        "Should support uppercase extensions");
    assertTrue(fileImageLoader.supportsFile(lowerCaseFile),
        "Should support lowercase extensions");
    assertTrue(fileImageLoader.supportsFile(mixedCaseFile),
        "Should support mixed case extensions");

    // Also test loading
    assertTrue(fileImageLoader.loadFromFile(upperCaseFile).isPresent(),
        "Should load uppercase extension files");
    assertTrue(fileImageLoader.loadFromFile(mixedCaseFile).isPresent(),
        "Should load mixed case extension files");
  }

  // ========== METADATA VALIDATION TESTS ==========

  @Test
  @DisplayName("Should extract correct metadata from loaded image")
  void shouldExtractCorrectMetadataFromLoadedImage() throws IOException {
    // Given
    File imageFile = createTestImageFile("metadata_test.png", 80, 60, Color.GRAY);
    long expectedFileSize = imageFile.length();

    // When
    Optional<Image> result = fileImageLoader.loadFromFile(imageFile);

    // Then
    assertTrue(result.isPresent(), "Image should load successfully");

    Image image = result.get();
    var metadata = image.getMetadata();

    assertEquals("PNG", metadata.getFormat(), "Format should be PNG");
    assertEquals(expectedFileSize, metadata.getFileSizeBytes(),
        "File size should match actual file size");
    assertNotNull(metadata.getLoadedAt(), "Load timestamp should be set");
    assertNotNull(metadata.getColorSpace(), "Color space should be determined");
    assertTrue(metadata.getBitDepth() > 0, "Bit depth should be positive");
  }

  @Test
  @DisplayName("Should extract different color spaces correctly")
  void shouldExtractDifferentColorSpacesCorrectly() throws IOException {
    // Test RGB image
    File rgbImage = createTestImageFile("rgb.png", 20, 20, Color.RED);
    Optional<Image> rgbResult = fileImageLoader.loadFromFile(rgbImage);
    assertTrue(rgbResult.isPresent());

    // For most created images, this will be RGB or similar
    String colorSpace = rgbResult.get().getMetadata().getColorSpace();
    assertNotNull(colorSpace, "Color space should be detected");
    assertTrue(colorSpace.length() > 0, "Color space should not be empty");
  }

  // ========== SUPPORTED EXTENSIONS TESTS ==========

  @Test
  @DisplayName("Should return supported extensions")
  void shouldReturnSupportedExtensions() {
    // When
    String[] extensions = fileImageLoader.getSupportedExtensions();

    // Then
    assertNotNull(extensions, "Extensions array should not be null");
    assertTrue(extensions.length > 0, "Should have at least one supported extension");

    // Check for common formats
    boolean hasJpg = false, hasPng = false, hasGif = false;
    for (String ext : extensions) {
      if ("jpg".equals(ext) || "jpeg".equals(ext))
        hasJpg = true;
      if ("png".equals(ext))
        hasPng = true;
      if ("gif".equals(ext))
        hasGif = true;
    }

    assertTrue(hasJpg, "Should support JPG/JPEG format");
    assertTrue(hasPng, "Should support PNG format");
    assertTrue(hasGif, "Should support GIF format");
  }

  @Test
  @DisplayName("Should return immutable copy of supported extensions")
  void shouldReturnImmutableCopyOfSupportedExtensions() {
    // When
    String[] extensions1 = fileImageLoader.getSupportedExtensions();
    String[] extensions2 = fileImageLoader.getSupportedExtensions();

    // Modify first array
    if (extensions1.length > 0) {
      String original = extensions1[0];
      extensions1[0] = "modified";

      // Then
      assertEquals(original, extensions2[0],
          "Modifying returned array should not affect subsequent calls");
    }
  }

  // ========== UNIQUE ID GENERATION TESTS ==========

  @Test
  @DisplayName("Should generate unique IDs for different files")
  void shouldGenerateUniqueIdsForDifferentFiles() throws IOException {
    // Given
    File file1 = createTestImageFile("image1.png", 25, 25, Color.BLUE);
    File file2 = createTestImageFile("image2.png", 25, 25, Color.RED);

    // Small delay to ensure different timestamps in ID generation
    try {
      Thread.sleep(2);
    } catch (InterruptedException e) {
      /* ignore */ }

    // When
    Optional<Image> image1 = fileImageLoader.loadFromFile(file1);
    Optional<Image> image2 = fileImageLoader.loadFromFile(file2);

    // Then
    assertTrue(image1.isPresent(), "First image should load");
    assertTrue(image2.isPresent(), "Second image should load");
    assertNotEquals(image1.get().getId(), image2.get().getId(),
        "Generated IDs should be unique");
  }

  @Test
  @DisplayName("Should generate consistent ID format")
  void shouldGenerateConsistentIdFormat() throws IOException {
    // Given
    File imageFile = createTestImageFile("consistent.jpg", 15, 15, Color.BLACK);

    // When
    Optional<Image> result = fileImageLoader.loadFromFile(imageFile);

    // Then
    assertTrue(result.isPresent(), "Image should load");
    String id = result.get().getId();

    assertNotNull(id, "ID should not be null");
    assertFalse(id.trim().isEmpty(), "ID should not be empty");
    assertTrue(id.contains("consistent.jpg"),
        "ID should contain filename for traceability");
  }

  // ========== EDGE CASES ==========

  @Test
  @DisplayName("Should handle files with multiple dots in name")
  void shouldHandleFilesWithMultipleDotsInName() throws IOException {
    // Given
    File complexNameFile = createTestImageFile("my.complex.file.name.png", 10, 10, Color.CYAN);

    // When
    boolean isSupported = fileImageLoader.supportsFile(complexNameFile);
    Optional<Image> result = fileImageLoader.loadFromFile(complexNameFile);

    // Then
    assertTrue(isSupported, "Should support files with multiple dots");
    assertTrue(result.isPresent(), "Should load files with multiple dots");
    assertEquals("my.complex.file.name.png", result.get().getOriginalFileName(),
        "Should preserve complete filename");
  }

  @Test
  @DisplayName("Should handle files without extension")
  void shouldHandleFilesWithoutExtension() throws IOException {
    // Given
    File noExtensionFile = createTextFile("noextension", "not an image");

    // When
    boolean isSupported = fileImageLoader.supportsFile(noExtensionFile);

    // Then
    assertFalse(isSupported, "Should not support files without extension");
  }

  // ========== HELPER METHODS ==========

  /**
   * Creates a test image file with specified properties.
   * 
   * @param fileName  The name of the file to create
   * @param width     Width of the test image
   * @param height    Height of the test image
   * @param fillColor Color to fill the image with
   * @return The created file
   * @throws IOException if file creation fails
   */
  private File createTestImageFile(String fileName, int width, int height, Color fillColor) throws IOException {
    // Create a test image with specified dimensions and color
    BufferedImage testImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = testImage.createGraphics();

    // Fill with base color
    g2d.setColor(fillColor);
    g2d.fillRect(0, 0, width, height);

    // Add some simple pattern to make it more realistic
    g2d.setColor(fillColor.brighter());
    g2d.drawOval(width / 4, height / 4, width / 2, height / 2);

    // Add a small rectangle for variety
    g2d.setColor(fillColor.darker());
    g2d.fillRect(0, 0, width / 4, height / 4);

    g2d.dispose();

    // Determine format from extension
    String format = getFormatFromFileName(fileName);

    // Save to temp directory
    Path filePath = tempDir.resolve(fileName);
    File outputFile = filePath.toFile();

    // Write image to file
    boolean written = ImageIO.write(testImage, format, outputFile);
    if (!written) {
      throw new IOException("Failed to write image in format: " + format);
    }

    return outputFile;
  }

  /**
   * Creates a text file with specified content.
   * 
   * @param fileName Name of the file to create
   * @param content  Content to write to the file
   * @return The created file
   * @throws IOException if file creation fails
   */
  private File createTextFile(String fileName, String content) throws IOException {
    Path filePath = tempDir.resolve(fileName);
    Files.write(filePath, content.getBytes());
    return filePath.toFile();
  }

  /**
   * Extracts the image format from a filename.
   * 
   * @param fileName The filename to analyze
   * @return The image format (e.g., "png", "jpg")
   */
  private String getFormatFromFileName(String fileName) {
    int lastDot = fileName.lastIndexOf('.');
    if (lastDot == -1) {
      return "png"; // Default format
    }

    String extension = fileName.substring(lastDot + 1).toLowerCase();

    // Handle special cases
    switch (extension) {
      case "jpg":
      case "jpeg":
        return "jpg";
      case "png":
        return "png";
      case "gif":
        return "gif";
      case "bmp":
        return "bmp";
      default:
        return "png"; // Default fallback
    }
  }
}
