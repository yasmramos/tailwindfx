package io.github.yasmramos.tailwindfx.core;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;

/**
 * StyleCache - Centralized cache management for TailwindFX styles.
 *
 * <p>This class handles cache invalidation and cleanup operations for nodes, providing a single
 * point of control for style cache properties.
 *
 * <p>Usage:
 *
 * <pre>
 * StyleCache.invalidate(node);
 * StyleCache.invalidateCategory(node, "spacing");
 * StyleCache.removeCleanupListener(node);
 * </pre>
 */
public final class StyleCache {

  /** Property key for category cache storage. */
  private static final String CATEGORY_CACHE_KEY = "tailwindfx.category.cache";

  /** Property key for cleanup listener storage. */
  private static final String CLEANUP_LISTENER_KEY = "tailwindfx.cleanup-listener";

  private StyleCache() {
    // Prevent instantiation
  }

  /**
   * Invalidates the entire style cache for a node.
   *
   * @param node the node whose cache should be invalidated
   */
  public static void invalidate(Node node) {
    Preconditions.requireNode(node, "StyleCache.invalidate");
    node.getProperties().remove(CATEGORY_CACHE_KEY);
    // Detach the change listener as well: dropping only the property would leave the listener
    // attached to sceneProperty(), keeping the node (and its callback) reachable.
    removeCleanupListener(node);
  }

  /**
   * Removes all TailwindFX styles from a node (cleanup). Alias for invalidate for backward
   * compatibility.
   *
   * @param node the node to clean up
   */
  public static void cleanup(Node node) {
    invalidate(node);
  }

  /**
   * Invalidates a specific category from the style cache for a node.
   *
   * @param node the node whose cache should be updated
   * @param category the category to remove from cache
   */
  public static void invalidateCategory(Node node, String category) {
    Preconditions.requireNode(node, "StyleCache.invalidateCategory");
    Preconditions.requireNonBlank(category, "StyleCache.invalidateCategory", "category");

    @SuppressWarnings("unchecked")
    java.util.Map<String, String> cache =
        (java.util.Map<String, String>) node.getProperties().get(CATEGORY_CACHE_KEY);
    if (cache != null) {
      cache.remove(category);
    }
  }

  /**
   * Gets or creates the category cache map for a node.
   *
   * @param node the node whose cache to retrieve
   * @return the category cache map
   */
  @SuppressWarnings("unchecked")
  public static java.util.Map<String, String> getCategoryCache(Node node) {
    Preconditions.requireNode(node, "StyleCache.getCategoryCache");

    java.util.Map<String, String> cache =
        (java.util.Map<String, String>) node.getProperties().get(CATEGORY_CACHE_KEY);
    if (cache == null) {
      cache = new java.util.HashMap<>();
      node.getProperties().put(CATEGORY_CACHE_KEY, cache);
    }
    return cache;
  }

  /**
   * Registers a cleanup listener that is run when the node leaves the scene graph.
   *
   * <p>The listener is attached to {@link Node#sceneProperty()}: when the node is removed from a
   * scene (detached, or its window closed) the action runs and the registration is dropped. Passing
   * {@code null} clears the registration without running anything.
   *
   * <p>Re-registering on the same node replaces both the stored action and the change listener.
   *
   * @param node the node to attach the listener to
   * @param listener the action to run on detach, or {@code null} to clear the registration
   */
  public static void setCleanupListener(Node node, Runnable listener) {
    Preconditions.requireNode(node, "StyleCache.setCleanupListener");
    Object existing = node.getProperties().get(CLEANUP_LISTENER_KEY);
    if (existing != null) {
      node.sceneProperty().removeListener((ChangeListener<Scene>) existing);
      node.getProperties().remove(CLEANUP_LISTENER_KEY);
    }
    if (listener == null) return;

    ChangeListener<Scene> onDetach =
        (obs, oldScene, newScene) -> {
          // Only a real detach (scene -> null) triggers cleanup; re-parenting within a scene or
          // moving between scenes must not.
          if (newScene != null) return;
          try {
            listener.run();
          } catch (RuntimeException e) {
            // Never let a user callback break the scene graph transition that triggered it.
            Preconditions.LOG.log(
                java.util.logging.Level.WARNING, "StyleCache cleanup listener failed.", e);
          }
        };
    node.getProperties().put(CLEANUP_LISTENER_KEY, onDetach);
    node.sceneProperty().addListener(onDetach);
  }

  /**
   * Removes the cleanup listener reference from a node without running it.
   *
   * @param node the node whose listener should be removed
   */
  public static void removeCleanupListener(Node node) {
    Preconditions.requireNode(node, "StyleCache.removeCleanupListener");
    Object existing = node.getProperties().get(CLEANUP_LISTENER_KEY);
    if (existing != null) {
      node.sceneProperty().removeListener((ChangeListener<Scene>) existing);
      node.getProperties().remove(CLEANUP_LISTENER_KEY);
    }
  }

  /**
   * Checks if a node has a cleanup listener registered.
   *
   * @param node the node to check
   * @return true if a cleanup listener is registered
   */
  public static boolean hasCleanupListener(Node node) {
    Preconditions.requireNode(node, "StyleCache.hasCleanupListener");
    return node.getProperties().containsKey(CLEANUP_LISTENER_KEY);
  }
}
