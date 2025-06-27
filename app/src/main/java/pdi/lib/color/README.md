# PDI Color Module

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Architecture](https://img.shields.io/badge/Pattern-Strategy-success)

The Color module implements core image processing operations using the Strategy pattern, following Clean Architecture principles.

## Table of Contents
- [Overview](#overview)
- [Key Components](#key-components)
  - [Core Interface](#core-interface)
  - [Operations](#operations)
  - [Service](#service)
- [Usage](#usage)
- [Extending the Module](#extending-the-module)
- [Design Principles](#design-principles)

## Overview

This module provides:
- `ColorOperation` interface for all processing operations
- Ready-to-use implementations (brightness, contrast, etc.)
- `ColorProcessorService` as the main entry point
- Detailed operation results with metadata

## Key Components

### Core Interface

#### `ColorOperation`
```java
public interface ColorOperation {
    Image apply(Image image, Map<String,Object> parameters);
    String getOperationName();
    boolean isValidParameters(Map<String,Object> parameters);
    String[] getExpectedParameters();
}
```

### Operations

| Operation Class           | Description                          | Parameters                     |
|---------------------------|--------------------------------------|--------------------------------|
| `BrightnessOperation`     | Adjusts image brightness             | `level: int` (-255 to 255)     |
| `ContrastOperation`       | Adjusts image contrast               | `factor: double` (0.0 to 3.0)  |
| `GrayscaleOperation`      | Converts to grayscale                | None                           |
| `RGBExtractionOperation`  | Extracts RGB channels                | `channel: String` (RED/GREEN/BLUE) |

### Service

#### `ColorProcessorService`
Main service class that:
- Validates parameters
- Executes operations
- Tracks performance
- Returns detailed results

```java
public ColorOperationResult processImage(
    Image image, 
    ColorOperation operation,
    Map<String,Object> parameters
)
```

#### `ColorOperationResult`
Immutable result containing:
- Original and processed images
- Operation metadata
- Processing time
- Timestamp

## Usage

### Basic Operation
```java
// Initialize dependencies
BrightnessOperation operation = new BrightnessOperation();
ColorProcessorService processor = new ColorProcessorService();

// Prepare parameters
Map<String,Object> params = Map.of("level", 30); // Increase brightness

// Process image
ColorOperationResult result = processor.processImage(
    sourceImage, 
    operation, 
    params
);

// Use result
if (result.isSuccessful()) {
    Image processed = result.getProcessedImage();
    double seconds = result.getProcessingTimeSeconds();
}
```

### Creating Custom Operations
1. Implement `ColorOperation`
2. Register with service:

```java
public class SepiaOperation implements ColorOperation {
    // Implementation...
}

// Usage:
processor.processImage(image, new SepiaOperation(), Map.of());
```

## Extending the Module

To add new operations:
1. Create new class implementing `ColorOperation`
2. Implement required methods
3. Add parameter validation
4. (Optional) Add utility methods for parameter creation

Example template:
```java
public class NewEffectOperation implements ColorOperation {
    // Operation implementation...
    
    public static Map<String,Object> createParameters(int intensity) {
        return Map.of("intensity", intensity);
    }
}
```

## Design Principles

1. **Strategy Pattern**: Each operation encapsulates a specific algorithm
2. **Immutable Results**: All operation results are immutable
3. **Parameter Validation**: Strict validation before processing
4. **Performance Tracking**: Automatic processing time measurement
5. **Clean Architecture**: No UI or infrastructure dependencies
