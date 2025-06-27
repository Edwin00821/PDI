Here's a detailed `README.md` for the Core module of your PDI project:

```markdown
# PDI Core Module

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture-success)

The Core module contains the fundamental domain entities and interfaces that form the backbone of the Digital Image Processing (PDI) system.

## Table of Contents
- [Overview](#overview)
- [Key Components](#key-components)
  - [Domain Entities](#domain-entities)
  - [Interfaces](#interfaces)
- [Usage](#usage)
- [Design Principles](#design-principles)
- [Dependencies](#dependencies)

## Overview

The Core module defines:
- The central `Image` domain entity
- Value objects like `ImageMetadata`
- Repository and loader interfaces
- Application-level data transfer objects

This module has **no dependencies** on specific implementations or frameworks, adhering to Clean Architecture principles.

## Key Components

### Domain Entities

#### `Image` Class
The primary domain entity representing an image with:
- Unique identifier
- Original filename
- Image data (as `BufferedImage`)
- Technical metadata

```java
Image image = new Image(
    "img_123",
    "sample.jpg",
    bufferedImage,
    metadata
);
```

#### `ImageMetadata` Value Object
Immutable technical details about an image:
- Format (JPG, PNG, etc.)
- File size in bytes
- Bit depth
- Color space
- Loading timestamp

### Interfaces

#### `ImageRepository`
Persistence abstraction for image storage:

```java
public interface ImageRepository {
    Image save(Image image);
    Optional<Image> findById(String id);
    List<Image> findAll();
    boolean deleteById(String id);
    boolean existsById(String id);
    int count();
}
```

#### `ImageLoader`
Abstraction for loading images from various sources:

```java
public interface ImageLoader {
    Optional<Image> loadFromFile(File file);
    boolean supportsFile(File file);
    String[] getSupportedExtensions();
}
```

#### `LoadImageResult`
Result object for image loading operations:

```java
// Successful load
LoadImageResult.success(loadedImage);

// Failed load
LoadImageResult.failure("File not found");
```

## Usage

### Basic Image Creation
```java
ImageMetadata metadata = new ImageMetadata(
    "JPEG",
    1024, // filesize
    24,   // bit depth
    LocalDateTime.now(),
    "RGB"
);

Image image = new Image(
    UUID.randomUUID().toString(),
    "example.jpg",
    bufferedImage,
    metadata
);
```

### Working with Repository
```java
// Initialize with concrete implementation
ImageRepository repo = new InMemoryImageRepository();

// Save image
Image saved = repo.save(image);

// Retrieve image
Optional<Image> found = repo.findById(saved.getId());
```

### Loading Images
```java
ImageLoader loader = new FileImageLoader(); // From infrastructure

Optional<Image> image = loader.loadFromFile(new File("path/to/image.jpg"));
if (image.isPresent()) {
    // Process image...
}
```

## Design Principles

1. **Immutability**: 
   - `ImageMetadata` is immutable
   - `Image` is effectively immutable (defensive copies)

2. **Interface Segregation**:
   - Small, focused interfaces
   - Clear separation between loading and persistence

3. **Domain Focus**:
   - No framework dependencies
   - Pure business logic

4. **Explicit Error Handling**:
   - `Optional` return types
   - Detailed result objects

## Dependencies

This module has minimal dependencies:
- Java 17+
- `java.awt.image.BufferedImage` (for image data)
- No external libraries
