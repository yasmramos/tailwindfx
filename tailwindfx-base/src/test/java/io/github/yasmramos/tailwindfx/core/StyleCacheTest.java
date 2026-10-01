package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.yasmramos.tailwindfx.testing.JavaFxToolkitExtension;
import java.util.Map;
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
          IllegalArgumentException.class, () -> StyleCache.setCleanupListener(null, new Object()));
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
      Object listener = new Object();

      StyleCache.setCleanupListener(node, listener);

      assertTrue(StyleCache.hasCleanupListener(node));
    }

    @Test
    @DisplayName("removeCleanupListener unregisters the listener")
    void removeUnregisters() {
      StackPane node = new StackPane();
      StyleCache.setCleanupListener(node, new Object());

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
      StyleCache.setCleanupListener(node, new Object());

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
      StyleCache.setCleanupListener(node, new Object());

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
  }
}
