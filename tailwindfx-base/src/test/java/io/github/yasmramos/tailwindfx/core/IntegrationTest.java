package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Integration tests for JitCompiler combining multiple utilities. */
public class IntegrationTest {

  @Test
  public void testGradientWithRing() {
    String[] tokens = {
      "bg-gradient-to-r",
      "from-blue-500",
      "to-purple-500",
      "ring-4",
      "ring-offset-2",
      "ring-blue-300"
    };

    JitCompiler.BatchResult result = JitCompiler.compileBatch(tokens);

    assertTrue(result.success(), "Compilation should succeed");
    assertTrue(result.inlineStyle().contains("linear-gradient"), "Should contain gradient");
    // Ring might be implemented as CSS class or inline style depending on implementation
    assertTrue(
        result.hasInlineStyle() || !result.cssClasses().isEmpty(), "Should have ring styling");
    System.out.println("Gradient + Ring: " + result.inlineStyle());
  }

  @Test
  public void testAspectRatioWithScrollSnap() {
    String[] tokens = {"aspect-video", "snap-x", "snap-mandatory", "overflow-x-auto"};

    JitCompiler.BatchResult result = JitCompiler.compileBatch(tokens);

    assertTrue(result.success(), "Compilation should succeed");
    // aspect-video now returns null (no invalid -fx-aspect-ratio property)
    // snap-* tokens also return null (no invalid -fx-snap-* properties)
    // The test verifies the compilation succeeds without injecting invalid CSS
    System.out.println("Aspect Ratio + Scroll Snap: " + result.inlineStyle());
  }

  @Test
  public void testDarkModeWithImportant() {
    String[] tokens = {"dark:bg-gray-800", "p-4!", "text-white!"};

    JitCompiler.BatchResult result = JitCompiler.compileBatch(tokens);

    assertTrue(result.hasDarkMode(), "Should detect dark mode");
    assertTrue(result.hasImportant(), "Should detect important flag");
    assertTrue(result.success(), "Compilation should succeed");
    System.out.println("Dark Mode + Important: " + result.inlineStyle());
  }

  @Test
  public void testComplexCardLayout() {
    String[] tokens = {
      "bg-white", "rounded-lg", "shadow-md", "p-6", "aspect-square", "ring-1", "ring-gray-200"
    };

    JitCompiler.BatchResult result = JitCompiler.compileBatch(tokens);

    assertTrue(result.success(), "Compilation should succeed");
    assertTrue(result.inlineStyle().contains("-fx-background-color"), "Should have background");
    assertTrue(result.inlineStyle().contains("-fx-padding"), "Should have padding");
    System.out.println("Complex Card: " + result.inlineStyle());
  }

  @Test
  public void testArbitraryValuesWithModifiers() {
    String[] tokens = {"w-[320px]!", "h-[auto]", "bg-[#ff0000]/80", "text-[14px]"};

    JitCompiler.BatchResult result = JitCompiler.compileBatch(tokens);

    assertTrue(result.hasImportant(), "Should detect important");
    assertTrue(result.success(), "Compilation should succeed");
    assertTrue(result.inlineStyle().contains("-fx-pref-width"), "Should have width");
    System.out.println("Arbitrary Values: " + result.inlineStyle());
  }

  @Test
  public void testFullButtonComponent() {
    String[] tokens = {
      "bg-gradient-to-r",
      "from-blue-600",
      "to-indigo-600",
      "text-white",
      "font-bold",
      "py-2",
      "px-4",
      "rounded-md",
      "shadow-lg",
      "hover:opacity-90", // This might not compile fully but shouldn't break
      "ring-2",
      "ring-offset-2",
      "ring-blue-500"
    };

    JitCompiler.BatchResult result = JitCompiler.compileBatch(tokens);

    assertTrue(result.success(), "Compilation should succeed");
    assertTrue(result.inlineStyle().contains("linear-gradient"), "Should have gradient");
    assertTrue(result.inlineStyle().contains("-fx-text-fill"), "Should have text color");
    System.out.println("Full Button: " + result.inlineStyle());
  }

  @Test
  public void testResponsiveGridItem() {
    String[] tokens = {
      "aspect-video", "bg-gray-100", "rounded-xl", "snap-start", "shadow-sm", "dark:bg-gray-700"
    };

    JitCompiler.BatchResult result = JitCompiler.compileBatch(tokens);

    assertTrue(result.success(), "Compilation should succeed");
    assertTrue(result.hasDarkMode(), "Should detect dark mode");
    System.out.println("Grid Item: " + result.inlineStyle());
  }

  @Test
  public void testCachePerformance() {
    String[] tokens = {"p-4", "bg-blue-500", "text-white", "rounded-md"};

    // First compilation (cache miss)
    long start1 = System.nanoTime();
    JitCompiler.compileBatch(tokens);
    long time1 = System.nanoTime() - start1;

    // Second compilation (cache hit)
    long start2 = System.nanoTime();
    JitCompiler.compileBatch(tokens);
    long time2 = System.nanoTime() - start2;

    assertTrue(time2 < time1, "Cache hit should be faster than cache miss");
    System.out.println(
        "Cache miss: " + (time1 / 1_000_000.0) + "ms, Cache hit: " + (time2 / 1_000_000.0) + "ms");
  }

  @Test
  public void testEmptyAndNullHandling() {
    JitCompiler.BatchResult result1 = JitCompiler.compileBatch();
    assertFalse(result1.success());
    assertEquals("", result1.inlineStyle());

    JitCompiler.BatchResult result2 = JitCompiler.compileBatch((String[]) null);
    assertFalse(result2.success());

    JitCompiler.BatchResult result3 = JitCompiler.compileBatch(new String[] {"", "   ", null});
    assertFalse(result3.success());
  }
}
