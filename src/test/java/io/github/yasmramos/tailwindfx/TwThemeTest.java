package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.theme.ThemeScopeManager;
import org.junit.jupiter.api.Test;

/** Unit tests for TwTheme facade class. */
class TwThemeTest {

  @Test
  void testOfThrowsExceptionForNullScene() {
    // Test that method throws IllegalArgumentException for null scene
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          TwTheme.of(null);
        },
        "TwTheme.of(null) should throw IllegalArgumentException");
  }

  @Test
  void testForSceneThrowsExceptionForNullScene() {
    // Test that method throws IllegalArgumentException for null scene
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          TwTheme.forScene(null);
        },
        "TwTheme.forScene(null) should throw IllegalArgumentException");
  }

  @Test
  void testScopeReturnsBuilderForNullPane() {
    // Test that method returns a builder even for null pane (validation happens on apply)
    ThemeScopeManager.ScopeBuilder builder = TwTheme.scope(null);
    assertNotNull(builder, "TwTheme.scope(null) should return a non-null builder");
  }

  @Test
  void testSaveThemeThrowsExceptionWithNullScene() {
    // This test ensures the method validates null scene parameter
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          TwTheme.saveTheme(null, "test-theme");
        },
        "TwTheme.saveTheme(null, ...) should throw IllegalArgumentException");
  }

  @Test
  void testLoadThemeThrowsExceptionWithNullScene() {
    // Load with null scene should throw IllegalArgumentException
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          TwTheme.loadTheme(null, "non-existent-theme");
        },
        "TwTheme.loadTheme(null, ...) should throw IllegalArgumentException");
  }

  @Test
  void testDeleteThemeDoesNotThrowException() {
    // This test ensures the method exists and doesn't throw
    assertDoesNotThrow(
        () -> {
          TwTheme.deleteTheme("test-theme-to-delete");
        },
        "TwTheme.deleteTheme() should not throw exception");
  }

  @Test
  void testTwThemeCannotBeInstantiated() throws Exception {
    // Verify that the constructor is private
    try {
      TwTheme.class.getDeclaredConstructor().setAccessible(true);
      Object instance = TwTheme.class.getDeclaredConstructor().newInstance();
      fail("TwTheme should not be instantiable");
    } catch (InstantiationException | IllegalAccessException e) {
      // Expected - constructor is private
    }
  }
}
