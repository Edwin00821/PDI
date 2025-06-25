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

  // Action callbacks - using functional interfaces for clean separation
  private Runnable onOpenImage;
  private Runnable onCloseImage;
  private Runnable onExit;
  private Runnable onAbout;

  // State tracking
  private boolean hasImageLoaded = false;

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

    // Help menu
    JMenu helpMenu = createHelpMenu();
    menuBar.add(helpMenu);
  }

  /**
   * Creates the File menu with basic operations.
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
    closeItem.setName("close-image"); // For state management
    fileMenu.add(closeItem);

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
   * Creates the context menu for right-click operations.
   */
  private void createContextMenu() {
    contextMenu = new JPopupMenu();

    // Close image (if available)
    JMenuItem closeContextItem = new JMenuItem("Close Image");
    closeContextItem.addActionListener(e -> executeCallback(onCloseImage));
    closeContextItem.setName("context-close-image");
    contextMenu.add(closeContextItem);

    contextMenu.addSeparator();
  }

  /**
   * Updates menu item states based on current application state.
   */
  private void updateMenuStates() {
    updateMenuBarStates();
    updateContextMenuStates();
  }

  /**
   * Updates menu bar item states.
   */
  private void updateMenuBarStates() {
    // Enable/disable items that require an image
    setMenuItemEnabled("close-image", hasImageLoaded);
  }

  /**
   * Updates context menu item states.
   */
  private void updateContextMenuStates() {
    setContextMenuItemEnabled("context-close-image", hasImageLoaded);
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
}
