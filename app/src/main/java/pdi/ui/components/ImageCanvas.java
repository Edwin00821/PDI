package pdi.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

import pdi.lib.core.domain.Image;

/***
 * Reusable canvas component for displaying images.**This component provides a
 * simple,centered display of an image with*automatic sizing and basic rendering
 * capabilities.It'sdesigned to be*easily extensible for future features like
 * zoom,pan,and overlays.**The component follows Swing best practices and can be
 * embedded in any*container or layout manager.
 */
public class ImageCanvas extends JPanel {

  private Image currentImage;
  private BufferedImage displayImage;

  // Canvas configuration
  private Color backgroundColor;
  private boolean centerImage;
  private boolean maintainAspectRatio;

  /**
   * Creates a new ImageCanvas with default settings.
   * Default: gray background, centered image, maintain aspect ratio.
   */
  public ImageCanvas() {
    this(Color.LIGHT_GRAY, true, true);
  }

  /**
   * Creates a new ImageCanvas with custom settings.
   * 
   * @param backgroundColor     Color to use for canvas background
   * @param centerImage         Whether to center the image in the canvas
   * @param maintainAspectRatio Whether to maintain image aspect ratio when
   *                            scaling
   */
  public ImageCanvas(Color backgroundColor, boolean centerImage, boolean maintainAspectRatio) {
    this.backgroundColor = Objects.requireNonNull(backgroundColor, "Background color cannot be null");
    this.centerImage = centerImage;
    this.maintainAspectRatio = maintainAspectRatio;

    initializeComponent();
  }

  /**
   * Initializes the canvas component with default settings.
   */
  private void initializeComponent() {
    setBackground(backgroundColor);
    setPreferredSize(new Dimension(800, 600));
    setMinimumSize(new Dimension(400, 300));

    // Enable double buffering for smooth rendering
    setDoubleBuffered(true);

    // Set focus traversal to allow keyboard events if needed later
    setFocusable(true);
  }

  /**
   * Sets the image to display on the canvas.
   * 
   * @param image The image to display, or null to clear the canvas
   */
  public void setImage(Image image) {
    this.currentImage = image;
    this.displayImage = (image != null) ? image.getImageData() : null;

    // Trigger repaint to show the new image
    repaint();
  }

  /**
   * Gets the currently displayed image.
   * 
   * @return The current image, or null if no image is displayed
   */
  public Image getCurrentImage() {
    return currentImage;
  }

  /**
   * Checks if an image is currently loaded.
   * 
   * @return true if an image is displayed, false otherwise
   */
  public boolean hasImage() {
    return currentImage != null;
  }

  /**
   * Clears the current image from the canvas.
   */
  public void clearImage() {
    setImage(null);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (displayImage == null) {
      drawEmptyState(g);
      return;
    }

    // Enable anti-aliasing for better image quality
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    drawImage(g2d);

    g2d.dispose();
  }

  /**
   * Draws the current image on the canvas.
   * 
   * @param g2d Graphics context for drawing
   */
  private void drawImage(Graphics2D g2d) {
    int canvasWidth = getWidth();
    int canvasHeight = getHeight();
    int imageWidth = displayImage.getWidth();
    int imageHeight = displayImage.getHeight();

    // Calculate drawing dimensions and position
    Rectangle drawingRect = calculateDrawingRectangle(
        canvasWidth, canvasHeight, imageWidth, imageHeight);

    // Draw the image
    g2d.drawImage(displayImage,
        drawingRect.x, drawingRect.y,
        drawingRect.width, drawingRect.height,
        this);

    // Draw image border for better visual separation
    drawImageBorder(g2d, drawingRect);
  }

  /**
   * Calculates the rectangle where the image should be drawn.
   * 
   * @param canvasWidth  Available canvas width
   * @param canvasHeight Available canvas height
   * @param imageWidth   Original image width
   * @param imageHeight  Original image height
   * @return Rectangle defining where to draw the image
   */
  private Rectangle calculateDrawingRectangle(int canvasWidth, int canvasHeight,
      int imageWidth, int imageHeight) {
    int drawWidth = imageWidth;
    int drawHeight = imageHeight;

    // Scale image to fit canvas if it's too large
    if (imageWidth > canvasWidth || imageHeight > canvasHeight) {
      if (maintainAspectRatio) {
        double scaleX = (double) canvasWidth / imageWidth;
        double scaleY = (double) canvasHeight / imageHeight;
        double scale = Math.min(scaleX, scaleY);

        drawWidth = (int) (imageWidth * scale);
        drawHeight = (int) (imageHeight * scale);
      } else {
        drawWidth = canvasWidth;
        drawHeight = canvasHeight;
      }
    }

    // Calculate position (center if requested)
    int x = 0;
    int y = 0;

    if (centerImage) {
      x = (canvasWidth - drawWidth) / 2;
      y = (canvasHeight - drawHeight) / 2;
    }

    return new Rectangle(x, y, drawWidth, drawHeight);
  }

  /**
   * Draws a subtle border around the image for better visual definition.
   * 
   * @param g2d       Graphics context
   * @param imageRect Rectangle where the image is drawn
   */
  private void drawImageBorder(Graphics2D g2d, Rectangle imageRect) {
    g2d.setColor(Color.GRAY);
    g2d.setStroke(new BasicStroke(1.0f));
    g2d.drawRect(imageRect.x - 1, imageRect.y - 1,
        imageRect.width + 1, imageRect.height + 1);
  }

  /**
   * Draws the empty state when no image is loaded.
   * 
   * @param g Graphics context
   */
  private void drawEmptyState(Graphics g) {
    String message = "No image loaded";
    FontMetrics fm = g.getFontMetrics();
    int messageWidth = fm.stringWidth(message);
    int messageHeight = fm.getHeight();

    g.setColor(Color.GRAY);
    g.drawString(message,
        (getWidth() - messageWidth) / 2,
        (getHeight() + messageHeight) / 2);
  }

  // Configuration setters for customization

  /**
   * Sets the background color of the canvas.
   * 
   * @param color New background color
   */
  public void setCanvasBackgroundColor(Color color) {
    this.backgroundColor = Objects.requireNonNull(color, "Color cannot be null");
    setBackground(color);
    repaint();
  }

  /**
   * Sets whether images should be centered on the canvas.
   * 
   * @param center true to center images, false to align to top-left
   */
  public void setCenterImage(boolean center) {
    this.centerImage = center;
    repaint();
  }

  /**
   * Sets whether to maintain aspect ratio when scaling images.
   * 
   * @param maintain true to maintain aspect ratio, false to stretch to fit
   */
  public void setMaintainAspectRatio(boolean maintain) {
    this.maintainAspectRatio = maintain;
    repaint();
  }
}
