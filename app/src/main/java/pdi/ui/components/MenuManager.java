package pdi.ui.components;

import javax.swing.*;

import java.util.function.Consumer;

/**
 * Manager component for handling all menu-related functionality.
 * 
 * This component encapsulates both the main menu bar and context menus,
 * providing a centralized location for menu management and reducing
 * complexity in the main window.
 * 
 * The MenuManager follows the delegation pattern, allowing the main
 * window to register callbacks for menu actions while keeping menu
 * logic separated and maintainable.
 */
public class MenuManager {

  // Menu components
  private JMenuBar menuBar;
  private JPopupMenu contextMenu;

  // Undo/Redo callbacks
  private Runnable onUndo;
  private Runnable onRedo;

  // Action callbacks - using functional interfaces for clean separation
  private Runnable onOpenImage;
  private Runnable onCloseImage;
  private Runnable onExit;
  private Runnable onAbout;

  // Color operation callbacks
  private Runnable onGrayscale;
  private Runnable onBrightness;
  private Runnable onContrast;
  private Runnable onRedChannel;
  private Runnable onGreenChannel;
  private Runnable onBlueChannel;

  // State tracking
  private boolean hasImageLoaded = false;

  // State tracking for undo/redo
  private boolean canUndo = false;
  private boolean canRedo = false;

  /**
   * Creates a new MenuManager with default configuration.
   */
  public MenuManager() {
    initializeMenus();
  }

  /**
   * Initializes all menu components.
   */
  private void initializeMenus() {
    createMenuBar();
    createContextMenu();
    updateMenuStates();
  }

  /**
   * Creates and configures the main menu bar.
   */
  private void createMenuBar() {
    menuBar = new JMenuBar();

    // File menu
    JMenu fileMenu = createFileMenu();
    menuBar.add(fileMenu);

    // Colors menu
    JMenu colorsMenu = createColorsMenu();
    menuBar.add(colorsMenu);

    // Help menu
    JMenu helpMenu = createHelpMenu();
    menuBar.add(helpMenu);
  }

  /**
   * Updates the File menu to include undo/redo operations.
   * Replace the existing createFileMenu method with this version.
   */
  private JMenu createFileMenu() {
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('F');

    // Open image
    JMenuItem openItem = new JMenuItem("Open Image...");
    openItem.setMnemonic('O');
    openItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
    openItem.addActionListener(e -> executeCallback(onOpenImage));
    fileMenu.add(openItem);

    fileMenu.addSeparator();

    // Close image
    JMenuItem closeItem = new JMenuItem("Close Image");
    closeItem.setMnemonic('C');
    closeItem.setAccelerator(KeyStroke.getKeyStroke("ctrl W"));
    closeItem.addActionListener(e -> executeCallback(onCloseImage));
    closeItem.setName("close-image");
    fileMenu.add(closeItem);

    fileMenu.addSeparator();

    // Undo
    JMenuItem undoItem = new JMenuItem("Undo");
    undoItem.setMnemonic('U');
    undoItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Z"));
    undoItem.addActionListener(e -> executeCallback(onUndo));
    undoItem.setName("undo-item");
    fileMenu.add(undoItem);

    // Redo
    JMenuItem redoItem = new JMenuItem("Redo");
    redoItem.setMnemonic('R');
    redoItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Y"));
    redoItem.addActionListener(e -> executeCallback(onRedo));
    redoItem.setName("redo-item");
    fileMenu.add(redoItem);

    fileMenu.addSeparator();

    // Exit
    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.setMnemonic('x');
    exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
    exitItem.addActionListener(e -> executeCallback(onExit));
    fileMenu.add(exitItem);

    return fileMenu;
  }

  /**
   * Updates the context menu to include undo/redo operations.
   * Replace the existing createContextMenu method with this version.
   */
  private void createContextMenu() {
    contextMenu = new JPopupMenu();

    // Undo
    JMenuItem undoContextItem = new JMenuItem("Undo");
    undoContextItem.addActionListener(e -> executeCallback(onUndo));
    undoContextItem.setName("context-undo");
    contextMenu.add(undoContextItem);

    // Redo
    JMenuItem redoContextItem = new JMenuItem("Redo");
    redoContextItem.addActionListener(e -> executeCallback(onRedo));
    redoContextItem.setName("context-redo");
    contextMenu.add(redoContextItem);

    contextMenu.addSeparator();

    // Close image (if available)
    JMenuItem closeContextItem = new JMenuItem("Close Image");
    closeContextItem.addActionListener(e -> executeCallback(onCloseImage));
    closeContextItem.setName("context-close-image");
    contextMenu.add(closeContextItem);

    contextMenu.addSeparator();

    // Quick color operations
    JMenuItem grayscaleContextItem = new JMenuItem("Grayscale");
    grayscaleContextItem.addActionListener(e -> executeCallback(onGrayscale));
    grayscaleContextItem.setName("context-grayscale");
    contextMenu.add(grayscaleContextItem);
  }

  /**
   * Creates the Colors menu with color processing operations.
   */
  private JMenu createColorsMenu() {
    JMenu colorsMenu = new JMenu("Colors");
    colorsMenu.setMnemonic('C');
    colorsMenu.setName("colors-menu");

    // Basic Operations
    JMenuItem grayscaleItem = new JMenuItem("Grayscale");
    grayscaleItem.setMnemonic('G');
    grayscaleItem.setAccelerator(KeyStroke.getKeyStroke("ctrl G"));
    grayscaleItem.addActionListener(e -> executeCallback(onGrayscale));
    grayscaleItem.setName("grayscale-item");
    colorsMenu.add(grayscaleItem);

    colorsMenu.addSeparator();

    // Adjustments
    JMenuItem brightnessItem = new JMenuItem("Brightness...");
    brightnessItem.setMnemonic('B');
    brightnessItem.setAccelerator(KeyStroke.getKeyStroke("ctrl B"));
    brightnessItem.addActionListener(e -> executeCallback(onBrightness));
    brightnessItem.setName("brightness-item");
    colorsMenu.add(brightnessItem);

    JMenuItem contrastItem = new JMenuItem("Contrast...");
    contrastItem.setMnemonic('n');
    contrastItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift C"));
    contrastItem.addActionListener(e -> executeCallback(onContrast));
    contrastItem.setName("contrast-item");
    colorsMenu.add(contrastItem);

    colorsMenu.addSeparator();

    // RGB Channel Extraction
    JMenu rgbMenu = new JMenu("RGB Channels");
    rgbMenu.setMnemonic('R');

    JMenuItem redChannelItem = new JMenuItem("Red Channel");
    redChannelItem.setMnemonic('R');
    redChannelItem.setAccelerator(KeyStroke.getKeyStroke("ctrl R"));
    redChannelItem.addActionListener(e -> executeCallback(onRedChannel));
    redChannelItem.setName("red-channel-item");
    rgbMenu.add(redChannelItem);

    JMenuItem greenChannelItem = new JMenuItem("Green Channel");
    greenChannelItem.setMnemonic('G');
    greenChannelItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift G"));
    greenChannelItem.addActionListener(e -> executeCallback(onGreenChannel));
    greenChannelItem.setName("green-channel-item");
    rgbMenu.add(greenChannelItem);

    JMenuItem blueChannelItem = new JMenuItem("Blue Channel");
    blueChannelItem.setMnemonic('B');
    blueChannelItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift B"));
    blueChannelItem.addActionListener(e -> executeCallback(onBlueChannel));
    blueChannelItem.setName("blue-channel-item");
    rgbMenu.add(blueChannelItem);

    colorsMenu.add(rgbMenu);

    return colorsMenu;
  }

  /**
   * Creates the Help menu.
   */
  private JMenu createHelpMenu() {
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic('H');

    // About
    JMenuItem aboutItem = new JMenuItem("About");
    aboutItem.setMnemonic('A');
    aboutItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
    aboutItem.addActionListener(e -> executeCallback(onAbout));
    helpMenu.add(aboutItem);

    return helpMenu;
  }

  /**
   * Updates menu item states based on current application state.
   */
  private void updateMenuStates() {
    updateMenuBarStates();
    updateContextMenuStates();
  }

  /**
   * Updates menu bar item states including undo/redo.
   * Replace the existing updateMenuBarStates method with this version.
   */
  private void updateMenuBarStates() {
    // Enable/disable items that require an image
    setMenuItemEnabled("close-image", hasImageLoaded);

    // Enable/disable color operations
    setMenuEnabled("colors-menu", hasImageLoaded);

    // Enable/disable undo/redo
    setMenuItemEnabled("undo-item", canUndo);
    setMenuItemEnabled("redo-item", canRedo);
  }

  /**
   * Updates context menu item states including undo/redo.
   * Replace the existing updateContextMenuStates method with this version.
   */
  private void updateContextMenuStates() {
    setContextMenuItemEnabled("context-close-image", hasImageLoaded);
    setContextMenuItemEnabled("context-grayscale", hasImageLoaded);
    setContextMenuItemEnabled("context-undo", canUndo);
    setContextMenuItemEnabled("context-redo", canRedo);
  }

  /**
   * Updates the undo/redo state and refreshes menu states.
   * 
   * @param canUndo true if undo is possible
   * @param canRedo true if redo is possible
   */
  public void setUndoRedoState(boolean canUndo, boolean canRedo) {
    this.canUndo = canUndo;
    this.canRedo = canRedo;
    updateMenuStates();
  }

  /**
   * Updates the undo menu item text with operation description.
   * 
   * @param description Description of the operation that can be undone,
   *                    or null to use default text
   */
  public void setUndoDescription(String description) {
    String text = (description != null) ? "Undo " + description : "Undo";
    updateMenuItemText("undo-item", text);
    updateContextMenuItemText("context-undo", text);
  }

  /**
   * Updates the redo menu item text with operation description.
   * 
   * @param description Description of the operation that can be redone,
   *                    or null to use default text
   */
  public void setRedoDescription(String description) {
    String text = (description != null) ? "Redo " + description : "Redo";
    updateMenuItemText("redo-item", text);
    updateContextMenuItemText("context-redo", text);
  }

  /**
   * Updates the text of a menu item by name.
   * 
   * @param itemName Name of the menu item
   * @param text     New text for the menu item
   */
  private void updateMenuItemText(String itemName, String text) {
    JMenuItem item = findMenuItemByName(menuBar, itemName);
    if (item != null) {
      item.setText(text);
    }
  }

  /**
   * Updates the text of a context menu item by name.
   * 
   * @param itemName Name of the context menu item
   * @param text     New text for the menu item
   */
  private void updateContextMenuItemText(String itemName, String text) {
    for (int i = 0; i < contextMenu.getComponentCount(); i++) {
      var component = contextMenu.getComponent(i);
      if (component instanceof JMenuItem item && itemName.equals(item.getName())) {
        item.setText(text);
        break;
      }
    }
  }

  // Add these callback setters to MenuManager class

  /**
   * Sets the callback for undo operations.
   * 
   * @param callback Callback to execute when undo is requested
   */
  public void setOnUndo(Runnable callback) {
    this.onUndo = callback;
  }

  /**
   * Sets the callback for redo operations.
   * 
   * @param callback Callback to execute when redo is requested
   */
  public void setOnRedo(Runnable callback) {
    this.onRedo = callback;
  }

  /**
   * Sets the enabled state of a menu item by name.
   */
  private void setMenuItemEnabled(String itemName, boolean enabled) {
    JMenuItem item = findMenuItemByName(menuBar, itemName);
    if (item != null) {
      item.setEnabled(enabled);
    }
  }

  /**
   * Sets the enabled state of a menu by name.
   */
  private void setMenuEnabled(String menuName, boolean enabled) {
    JMenu menu = findMenuByName(menuBar, menuName);
    if (menu != null) {
      menu.setEnabled(enabled);
    }
  }

  /**
   * Sets the enabled state of a context menu item by name.
   */
  private void setContextMenuItemEnabled(String itemName, boolean enabled) {
    for (int i = 0; i < contextMenu.getComponentCount(); i++) {
      var component = contextMenu.getComponent(i);
      if (component instanceof JMenuItem item && itemName.equals(item.getName())) {
        item.setEnabled(enabled);
        break;
      }
    }
  }

  /**
   * Finds a menu item by name recursively.
   */
  private JMenuItem findMenuItemByName(JMenuBar menuBar, String name) {
    for (int i = 0; i < menuBar.getMenuCount(); i++) {
      JMenuItem item = findMenuItemByName(menuBar.getMenu(i), name);
      if (item != null) {
        return item;
      }
    }
    return null;
  }

  /**
   * Finds a menu item by name recursively in a menu.
   */
  private JMenuItem findMenuItemByName(JMenu menu, String name) {
    for (int i = 0; i < menu.getItemCount(); i++) {
      JMenuItem item = menu.getItem(i);
      if (item != null) {
        if (name.equals(item.getName())) {
          return item;
        }
        if (item instanceof JMenu subMenu) {
          JMenuItem found = findMenuItemByName(subMenu, name);
          if (found != null) {
            return found;
          }
        }
      }
    }
    return null;
  }

  /**
   * Finds a menu by name.
   */
  private JMenu findMenuByName(JMenuBar menuBar, String name) {
    for (int i = 0; i < menuBar.getMenuCount(); i++) {
      JMenu menu = menuBar.getMenu(i);
      if (name.equals(menu.getName())) {
        return menu;
      }
    }
    return null;
  }

  /**
   * Safely executes a callback if it's not null.
   */
  private void executeCallback(Runnable callback) {
    if (callback != null) {
      callback.run();
    }
  }

  /**
   * Safely executes a callback with a parameter if it's not null.
   */
  private void executeCallback(Consumer<String> callback, String parameter) {
    if (callback != null) {
      callback.accept(parameter);
    }
  }

  // Public API for configuration and state management

  /**
   * Gets the configured menu bar.
   */
  public JMenuBar getMenuBar() {
    return menuBar;
  }

  /**
   * Gets the context menu for use with components.
   */
  public JPopupMenu getContextMenu() {
    return contextMenu;
  }

  /**
   * Updates the image loaded state and refreshes menu states.
   */
  public void setImageLoaded(boolean loaded) {
    this.hasImageLoaded = loaded;
    updateMenuStates();
  }

  // Callback setters for action handling

  public void setOnOpenImage(Runnable callback) {
    this.onOpenImage = callback;
  }

  public void setOnCloseImage(Runnable callback) {
    this.onCloseImage = callback;
  }

  public void setOnExit(Runnable callback) {
    this.onExit = callback;
  }

  public void setOnAbout(Runnable callback) {
    this.onAbout = callback;
  }

  // Color operation callback setters

  public void setOnGrayscale(Runnable callback) {
    this.onGrayscale = callback;
  }

  public void setOnBrightness(Runnable callback) {
    this.onBrightness = callback;
  }

  public void setOnContrast(Runnable callback) {
    this.onContrast = callback;
  }

  public void setOnRedChannel(Runnable callback) {
    this.onRedChannel = callback;
  }

  public void setOnGreenChannel(Runnable callback) {
    this.onGreenChannel = callback;
  }

  public void setOnBlueChannel(Runnable callback) {
    this.onBlueChannel = callback;
  }
}
