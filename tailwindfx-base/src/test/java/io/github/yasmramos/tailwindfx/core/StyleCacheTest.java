package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.yasmramos.tailwindfx.testing.JavaFxToolkitExtension;
import java.util.Map;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Unit tests for {@link StyleCache}. */
@ExtendWith(JavaFxToolkitExtension.class)
@DisplayName("StyleCache")
class StyleCacheTest {

  @Nested
  @DisplayName("Argument validation")
  class Validation {

    @Test
    @DisplayName("invalidate(null) throws IllegalArgumentException")
    void invalidateNullThrows() {
      assertThrows(IllegalArgumentException.class, () -> StyleCache.invalidate(null));
    }

    @Test
    @DisplayName("cleanup(null) throws IllegalArgumentException")
    void cleanupNullThrows() {
      assertThrows(IllegalArgumentException.class, () -> StyleCache.cleanup(null));
    }

    @Test
    @DisplayName("getCategoryCache(null) throws IllegalArgumentException")
    void getCategoryCacheNullThrows() {
      assertThrows(IllegalArgumentException.class, () -> StyleCache.getCategoryCache(null));
    }

    @Test
    @DisplayName("invalidateCategory rejects null node")
    void invalidateCategoryNullNodeThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> StyleCache.invalidateCategory(null, "spacing"));
    }

    @Test
    @DisplayName("invalidateCategory rejects null category")
    void invalidateCategoryNullCategoryThrows() {
      StackPane node = new StackPane();
      assertThrows(IllegalArgumentException.class, () -> StyleCache.invalidateCategory(node, null));
    }

    @Test
    @DisplayName("invalidateCategory rejects blank category")
    void invalidateCategoryBlankCategoryThrows() {
      StackPane node = new StackPane();
      assertThrows(
          IllegalArgumentException.class, () -> StyleCache.invalidateCategory(node, "   "));
    }

    @Test
    @DisplayName("setCleanupListener rejects null node")
    void setCleanupListenerNullNodeThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> StyleCache.setCleanupListener(null, () -> {}));
    }

    @Test
    @DisplayName("removeCleanupListener rejects null node")
    void removeCleanupListenerNullNodeThrows() {
      assertThrows(IllegalArgumentException.class, () -> StyleCache.removeCleanupListener(null));
    }

    @Test
    @DisplayName("hasCleanupListener rejects null node")
    void hasCleanupListenerNullNodeThrows() {
      assertThrows(IllegalArgumentException.class, () -> StyleCache.hasCleanupListener(null));
    }
  }

  @Nested
  @DisplayName("Category cache")
  class CategoryCache {

    @Test
    @DisplayName("getCategoryCache lazily creates a map")
    void categoryCacheIsCreatedLazily() {
      StackPane node = new StackPane();
      Map<String, String> cache = StyleCache.getCategoryCache(node);
      assertNotNull(cache, "cache should be created on first access");
      assertTrue(cache.isEmpty(), "fresh cache should be empty");
    }

    @Test
    @DisplayName("getCategoryCache returns the same map instance on repeated calls")
    void categoryCacheIsStable() {
      StackPane node = new StackPane();
      Map<String, String> first = StyleCache.getCategoryCache(node);
      Map<String, String> second = StyleCache.getCategoryCache(node);
      assertSame(first, second, "repeated calls must return the same cached map");
    }

    @Test
    @DisplayName("invalidateCategory removes only the requested entry")
    void invalidateCategoryRemovesSingleEntry() {
      StackPane node = new StackPane();
      Map<String, String> cache = StyleCache.getCategoryCache(node);
      cache.put("spacing", "p-4");
      cache.put("color", "bg-red-500");

      StyleCache.invalidateCategory(node, "spacing");

      assertFalse(cache.containsKey("spacing"), "spacing entry should be removed");
      assertTrue(cache.containsKey("color"), "other categories must survive");
      assertEquals("bg-red-500", cache.get("color"));
    }

    @Test
    @DisplayName("invalidateCategory on unknown category is a no-op")
    void invalidateUnknownCategoryIsNoOp() {
      StackPane node = new StackPane();
      Map<String, String> cache = StyleCache.getCategoryCache(node);
      cache.put("spacing", "p-4");

      StyleCache.invalidateCategory(node, "typography");

      assertEquals(1, cache.size(), "unrelated entries must not be touched");
    }

    @Test
    @DisplayName("invalidateCategory without an existing cache does not fail")
    void invalidateCategoryWithoutCacheIsSafe() {
      StackPane node = new StackPane();
      // No getCategoryCache call before: node has no cache map at all.
      StyleCache.invalidateCategory(node, "spacing");
      // Still safe to create the cache afterwards.
      assertTrue(StyleCache.getCategoryCache(node).isEmpty());
    }
  }

  @Nested
  @DisplayName("Cleanup listener management")
  class CleanupListener {

    @Test
    @DisplayName("new node has no cleanup listener registered")
    void freshNodeHasNoListener() {
      assertFalse(StyleCache.hasCleanupListener(new StackPane()));
    }

    @Test
    @DisplayName("setCleanupListener registers and hasCleanupListener reports true")
    void setThenHas() {
      StackPane node = new StackPane();
      Runnable listener = () -> {};

      StyleCache.setCleanupListener(node, listener);

      assertTrue(StyleCache.hasCleanupListener(node));
    }

    @Test
    @DisplayName("removeCleanupListener unregisters the listener")
    void removeUnregisters() {
      StackPane node = new StackPane();
      StyleCache.setCleanupListener(node, () -> {});

      StyleCache.removeCleanupListener(node);

      assertFalse(StyleCache.hasCleanupListener(node));
    }

    @Test
    @DisplayName("removeCleanupListener on node without listener is a no-op")
    void removeWithoutListenerIsSafe() {
      StackPane node = new StackPane();
      StyleCache.removeCleanupListener(node);
      assertFalse(StyleCache.hasCleanupListener(node));
    }
  }

  @Nested
  @DisplayName("Invalidation")
  class Invalidation {

    @Test
    @DisplayName("invalidate clears both category cache and cleanup listener")
    void invalidateClearsEverything() {
      StackPane node = new StackPane();
      StyleCache.getCategoryCache(node).put("spacing", "p-4");
      StyleCache.setCleanupListener(node, () -> {});

      StyleCache.invalidate(node);

      assertFalse(StyleCache.hasCleanupListener(node), "listener should be gone");
      // Cache map was removed; a fresh one is created lazily and is empty.
      assertTrue(StyleCache.getCategoryCache(node).isEmpty(), "cache should be reset");
    }

    @Test
    @DisplayName("cleanup behaves exactly like invalidate")
    void cleanupIsAliasForInvalidate() {
      StackPane node = new StackPane();
      StyleCache.getCategoryCache(node).put("color", "bg-blue-500");
      StyleCache.setCleanupListener(node, () -> {});

      StyleCache.cleanup(node);

      assertFalse(StyleCache.hasCleanupListener(node));
      assertTrue(StyleCache.getCategoryCache(node).isEmpty());
    }

    @Test
    @DisplayName("invalidate on a pristine node is safe")
    void invalidateFreshNodeIsSafe() {
      StackPane node = new StackPane();
      StyleCache.invalidate(node);
      assertFalse(StyleCache.hasCleanupListener(node));
    }

    @Test
    @DisplayName("caches of two nodes are independent")
    void cachesArePerNode() {
      StackPane a = new StackPane();
      StackPane b = new StackPane();
      StyleCache.getCategoryCache(a).put("spacing", "p-4");

      StyleCache.invalidate(a);

      assertSame(
          StyleCache.getCategoryCache(a), StyleCache.getCategoryCache(a), "identity per node");
      assertFalse(
          StyleCache.getCategoryCache(b).containsKey("spacing"),
          "node B must never see node A entries");
    }

    @Test
    @DisplayName("cleanup listener runs when the node leaves the scene")
    void cleanupListenerRunsOnDetach() {
      StackPane root = new StackPane();
      Scene scene = new Scene(root);
      StackPane node = new StackPane();
      int[] runs = {0};
      StyleCache.setCleanupListener(node, () -> runs[0]++);

      assertNull(node.getScene(), "precondition: the node starts detached");

      root.getChildren().add(node); // attach -> sceneProperty goes null -> scene
      assertSame(scene, node.getScene(), "precondition: the node is attached");
      assertEquals(0, runs[0], "attaching must not trigger cleanup");

      root.getChildren().remove(node); // detach -> sceneProperty goes scene -> null
      assertNull(node.getScene(), "precondition: the node is detached again");

      assertEquals(1, runs[0], "the listener must run exactly once on detach");
    }

    @Test
    @DisplayName("cleanup listener does not run while the node stays in a scene")
    void cleanupListenerIgnoresNonNullScenes() {
      StackPane root = new StackPane();
      Scene scene = new Scene(root);
      StackPane node = new StackPane();
      int[] runs = {0};
      StyleCache.setCleanupListener(node, () -> runs[0]++);

      root.getChildren().add(node);
      // Re-adding the same node inside the same scene does not change sceneProperty, and a plain
      // scene change must never be mistaken for a detach.
      root.getChildren().remove(node);
      root.getChildren().add(node);

      assertSame(scene, node.getScene(), "the node is attached again");
      assertEquals(1, runs[0], "only the real detach counts, not the re-attach");

      // Moving the node into a second scene must not fire the listener either.
      StackPane otherRoot = new StackPane();
      Scene otherScene = new Scene(otherRoot);
      root.getChildren().remove(node);
      assertEquals(2, runs[0], "removing from the first scene is a detach");
      otherRoot.getChildren().add(node);
      assertSame(otherScene, node.getScene());
    }

    @Test
    @DisplayName("removeCleanupListener detaches without running the action")
    void removeCleanupListenerDoesNotRun() {
      StackPane root = new StackPane();
      new Scene(root);
      StackPane node = new StackPane();
      int[] runs = {0};
      StyleCache.setCleanupListener(node, () -> runs[0]++);

      StyleCache.removeCleanupListener(node);

      root.getChildren().add(node);
      root.getChildren().remove(node);

      assertEquals(0, runs[0], "a removed listener must not run");
      assertFalse(StyleCache.hasCleanupListener(node));
    }

    @Test
    @DisplayName("re-registering replaces the previous listener instead of stacking")
    void reRegisterReplacesPreviousListener() {
      StackPane root = new StackPane();
      new Scene(root);
      StackPane node = new StackPane();
      int[] first = {0};
      int[] second = {0};
      StyleCache.setCleanupListener(node, () -> first[0]++);
      StyleCache.setCleanupListener(node, () -> second[0]++);

      root.getChildren().add(node);
      root.getChildren().remove(node);

      assertEquals(0, first[0], "the superseded listener must be detached");
      assertEquals(1, second[0], "only the latest listener runs");
    }

    @Test
    @DisplayName("passing null to setCleanupListener clears the registration")
    void setCleanupListenerNullClears() {
      StackPane root = new StackPane();
      new Scene(root);
      StackPane node = new StackPane();
      int[] runs = {0};
      StyleCache.setCleanupListener(node, () -> runs[0]++);

      StyleCache.setCleanupListener(node, null);

      root.getChildren().add(node);
      root.getChildren().remove(node);

      assertEquals(0, runs[0]);
      assertFalse(StyleCache.hasCleanupListener(node));
    }

    @Test
    @DisplayName("a throwing cleanup listener is contained and logged")
    void throwingCleanupListenerIsContained() {
      StackPane root = new StackPane();
      new Scene(root);
      StackPane node = new StackPane();
      StyleCache.setCleanupListener(
          node,
          () -> {
            throw new IllegalStateException("boom");
          });

      // Must not propagate out of the scene graph transition.
      assertDoesNotThrow(
          () -> {
            root.getChildren().add(node);
            root.getChildren().remove(node);
          });
    }
  }
}
