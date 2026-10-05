package io.github.yasmramos.tailwindfx.theme;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ThemeConfig class. Tests configuration of design system tokens (colors, spacing,
 * breakpoints, etc.).
 */
class ThemeConfigTest {

  @BeforeEach
  void setUp() {
    // Reset singleton before each test to ensure clean state
    ThemeConfig.reset();
  }

  @AfterEach
  void tearDown() {
    ThemeConfig.reset();
  }

  @Test
  void testDefaultConfig_CreatesSingleton() {
    ThemeConfig config1 = ThemeConfig.defaultConfig();
    ThemeConfig config2 = ThemeConfig.defaultConfig();

    assertSame(config1, config2, "Should return same singleton instance");
  }

  @Test
  void testReset_ClearsSingleton() {
    ThemeConfig config1 = ThemeConfig.defaultConfig();
    ThemeConfig.reset();
    ThemeConfig config2 = ThemeConfig.defaultConfig();

    assertNotSame(config1, config2, "Should create new instance after reset");
  }

  @Test
  void testColor_ValidColorAndShade() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    String blue500 = config.color("blue", 500);
    assertEquals("#3b82f6", blue500, "Should return correct blue-500 color");

    String red600 = config.color("red", 600);
    assertEquals("#dc2626", red600, "Should return correct red-600 color");
  }

  @Test
  void testColor_InvalidColorName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    String invalid = config.color("nonexistent", 500);
    assertNull(invalid, "Should return null for invalid color name");
  }

  @Test
  void testColor_InvalidShade() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    String invalid = config.color("blue", 999);
    assertNull(invalid, "Should return null for invalid shade");
  }

  @Test
  void testColor_AllValidShades() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    int[] validShades = {50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950};

    for (int shade : validShades) {
      String color = config.color("blue", shade);
      assertNotNull(color, "Should return color for shade " + shade);
      assertFalse(color.isEmpty(), "Color should not be empty");
    }
  }

  @Test
  void testColorFamily_ReturnsAllShades() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    String[] blueFamily = config.colorFamily("blue");
    assertNotNull(blueFamily, "Should return color family array");
    assertEquals(11, blueFamily.length, "Should have 11 shades (50-950)");
    assertEquals("#eff6ff", blueFamily[0], "First shade should be 50");
    assertEquals("#172554", blueFamily[10], "Last shade should be 950");
  }

  @Test
  void testColorFamily_InvalidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    String[] invalid = config.colorFamily("nonexistent");
    assertNull(invalid, "Should return null for invalid color family");
  }

  @Test
  void testColors_ReturnsAllColorFamilies() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Map<String, String[]> allColors = config.colors();
    assertNotNull(allColors, "Should return colors map");
    assertFalse(allColors.isEmpty(), "Should have color families");
    assertTrue(allColors.containsKey("blue"), "Should contain blue");
    assertTrue(allColors.containsKey("red"), "Should contain red");
    assertTrue(allColors.containsKey("green"), "Should contain green");
  }

  @Test
  void testSpacing_ValidValue() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    assertEquals(0.0, config.spacing(0), "Spacing 0 should be 0");
    assertEquals(2.0, config.spacing(1), "Spacing 1 should be 2px");
    assertEquals(4.0, config.spacing(2), "Spacing 2 should be 4px");
    assertEquals(6.0, config.spacing(3), "Spacing 3 should be 6px");
    assertEquals(16.0, config.spacing(4), "Spacing 4 should be 16px");
    assertEquals(64.0, config.spacing(16), "Spacing 16 should be 64px");
  }

  @Test
  void testSpacing_ValueOutOfBounds() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    double large = config.spacing(100);
    assertEquals(400.0, large, "Should use fallback formula: value * 4.0");
  }

  @Test
  void testSpacing_NegativeValue() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    double negative = config.spacing(-5);
    assertEquals(-20.0, negative, "Should use fallback formula for negative values");
  }

  @Test
  void testSpacingArray_ReturnsCopy() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    double[] spacing = config.spacing();
    assertNotNull(spacing, "Should return spacing array");
    assertEquals(65, spacing.length, "Should have 65 elements (0-64)");

    // Verify it's a copy (modifying it shouldn't affect config)
    double original = spacing[4];
    spacing[4] = 999.0;
    assertEquals(original, config.spacing(4), "Should return copy, not reference");
  }

  @Test
  void testBreakpoint_ValidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    assertEquals(640, config.breakpoint("sm"), "sm breakpoint should be 640");
    assertEquals(768, config.breakpoint("md"), "md breakpoint should be 768");
    assertEquals(1024, config.breakpoint("lg"), "lg breakpoint should be 1024");
    assertEquals(1280, config.breakpoint("xl"), "xl breakpoint should be 1280");
    assertEquals(1536, config.breakpoint("2xl"), "2xl breakpoint should be 1536");
  }

  @Test
  void testBreakpoint_InvalidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Integer invalid = config.breakpoint("nonexistent");
    assertNull(invalid, "Should return null for invalid breakpoint");
  }

  @Test
  void testBreakpoints_ReturnsAllBreakpoints() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Map<String, Integer> breakpoints = config.breakpoints();
    assertNotNull(breakpoints, "Should return breakpoints map");
    assertEquals(5, breakpoints.size(), "Should have 5 breakpoints");
    assertTrue(breakpoints.containsKey("sm"));
    assertTrue(breakpoints.containsKey("md"));
    assertTrue(breakpoints.containsKey("lg"));
  }

  @Test
  void testBorderRadius_ValidValue() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    assertEquals(0.0, config.borderRadius(0), "Border radius 0 should be 0");
    assertEquals(2.0, config.borderRadius(1), "Border radius 1 should be 2px");
    assertEquals(4.0, config.borderRadius(2), "Border radius 2 should be 4px");
    assertEquals(6.0, config.borderRadius(3), "Border radius 3 should be 6px");
    assertEquals(8.0, config.borderRadius(4), "Border radius 4 should be 8px");
    assertEquals(64.0, config.borderRadius(16), "Border radius 16 should be 64px");
  }

  @Test
  void testBorderRadius_ValueOutOfBounds() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    double large = config.borderRadius(100);
    assertEquals(400.0, large, "Should use fallback formula: value * 4.0");
  }

  @Test
  void testShadow_ValidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    String sm = config.shadow("sm");
    assertNotNull(sm, "Should return sm shadow");
    assertTrue(sm.contains("0 1px 2px"), "Should contain sm shadow values");

    String lg = config.shadow("lg");
    assertNotNull(lg, "Should return lg shadow");
    assertTrue(lg.contains("0 10px 15px"), "Should contain lg shadow values");
  }

  @Test
  void testShadow_InvalidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    String invalid = config.shadow("nonexistent");
    assertNull(invalid, "Should return null for invalid shadow");
  }

  @Test
  void testShadows_ReturnsAllShadows() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Map<String, String> shadows = config.shadows();
    assertNotNull(shadows, "Should return shadows map");
    assertFalse(shadows.isEmpty(), "Should have shadows");
    assertTrue(shadows.containsKey("sm"));
    assertTrue(shadows.containsKey("md"));
    assertTrue(shadows.containsKey("lg"));
    assertTrue(shadows.containsKey("none"));
  }

  @Test
  void testOpacity_ValidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    assertEquals(0.0, config.opacity("0"), "Opacity 0 should be 0.0");
    assertEquals(0.5, config.opacity("50"), "Opacity 50 should be 0.5");
    assertEquals(1.0, config.opacity("100"), "Opacity 100 should be 1.0");
  }

  @Test
  void testOpacity_InvalidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Double invalid = config.opacity("nonexistent");
    assertNull(invalid, "Should return null for invalid opacity");
  }

  @Test
  void testOpacity_ReturnsAllOpacities() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Map<String, Double> opacities = config.opacity();
    assertNotNull(opacities, "Should return opacities map");
    assertFalse(opacities.isEmpty(), "Should have opacity values");
    assertTrue(opacities.containsKey("50"));
    assertTrue(opacities.containsKey("100"));
  }

  @Test
  void testFontFamily_ValidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    String sans = config.fontFamily("sans");
    assertNotNull(sans, "Should return sans font family");
    assertTrue(sans.contains("Inter"), "Should contain Inter");

    String mono = config.fontFamily("mono");
    assertNotNull(mono, "Should return mono font family");
    assertTrue(mono.contains("JetBrains Mono"), "Should contain JetBrains Mono");
  }

  @Test
  void testFontFamily_InvalidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    String invalid = config.fontFamily("nonexistent");
    assertNull(invalid, "Should return null for invalid font family");
  }

  @Test
  void testFontSize_ValidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    assertEquals(12.0, config.fontSize("xs"), "Font size xs should be 12.0");
    assertEquals(16.0, config.fontSize("base"), "Font size base should be 16.0");
    assertEquals(24.0, config.fontSize("2xl"), "Font size 2xl should be 24.0");
    assertEquals(72.0, config.fontSize("7xl"), "Font size 7xl should be 72.0");
  }

  @Test
  void testFontSize_InvalidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Double invalid = config.fontSize("nonexistent");
    assertNull(invalid, "Should return null for invalid font size");
  }

  @Test
  void testFontWeight_ValidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    assertEquals("400", config.fontWeight("normal"), "Normal weight should be 400");
    assertEquals("700", config.fontWeight("bold"), "Bold weight should be 700");
    assertEquals("100", config.fontWeight("thin"), "Thin weight should be 100");
  }

  @Test
  void testFontWeight_InvalidName() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    String invalid = config.fontWeight("nonexistent");
    assertNull(invalid, "Should return null for invalid font weight");
  }

  @Test
  void testCustomConfig_WithBuilder() {
    Map<String, Integer> customBreakpoints = new HashMap<>();
    customBreakpoints.put("custom", 500);

    ThemeConfig config = new ThemeConfig.Builder().breakpoints(customBreakpoints).build();

    assertEquals(500, config.breakpoint("custom"), "Should have custom breakpoint");
    assertNull(config.breakpoint("sm"), "Should not have default breakpoints");
  }

  @Test
  void testBuilder_AddBreakpoint() {
    ThemeConfig config = new ThemeConfig.Builder().addBreakpoint("custom", 900).build();

    assertEquals(900, config.breakpoint("custom"), "Should add custom breakpoint");
  }

  @Test
  void testBuilder_AddShadow() {
    ThemeConfig config =
        new ThemeConfig.Builder().addShadow("custom", "0 0 10px rgba(0,0,0,0.5)").build();

    assertEquals("0 0 10px rgba(0,0,0,0.5)", config.shadow("custom"), "Should add custom shadow");
  }

  @Test
  void testBuilder_AddOpacity() {
    ThemeConfig config = new ThemeConfig.Builder().addOpacity("custom", 0.33).build();

    assertEquals(0.33, config.opacity("custom"), "Should add custom opacity");
  }

  @Test
  void testBuilder_AddFontFamily() {
    ThemeConfig config = new ThemeConfig.Builder().addFontFamily("custom", "Custom Font").build();

    assertEquals("Custom Font", config.fontFamily("custom"), "Should add custom font family");
  }

  @Test
  void testBuilder_AddFontSize() {
    ThemeConfig config = new ThemeConfig.Builder().addFontSize("custom", 50.0).build();

    assertEquals(50.0, config.fontSize("custom"), "Should add custom font size");
  }

  @Test
  void testBuilder_AddFontWeight() {
    ThemeConfig config = new ThemeConfig.Builder().addFontWeight("custom", "950").build();

    assertEquals("950", config.fontWeight("custom"), "Should add custom font weight");
  }

  @Test
  void testBuilder_InvalidSpacingLength() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new ThemeConfig.Builder()
              .spacing(new double[10]) // Should be 65
              .build();
        },
        "Should throw exception for invalid spacing array length");
  }

  @Test
  void testBuilder_InvalidBorderRadiusLength() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new ThemeConfig.Builder()
              .borderRadius(new double[10]) // Should be 17
              .build();
        },
        "Should throw exception for invalid border radius array length");
  }

  @Test
  void testColorsAreUnmodifiable() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Map<String, String[]> colors = config.colors();
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          colors.put("test", new String[] {"#000000"});
        },
        "Should return unmodifiable map");
  }

  @Test
  void testBreakpointsAreUnmodifiable() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Map<String, Integer> breakpoints = config.breakpoints();
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          breakpoints.put("test", 100);
        },
        "Should return unmodifiable map");
  }

  @Test
  void testShadowsAreUnmodifiable() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Map<String, String> shadows = config.shadows();
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          shadows.put("test", "0 0 0");
        },
        "Should return unmodifiable map");
  }

  @Test
  void testOpacityAreUnmodifiable() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Map<String, Double> opacities = config.opacity();
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          opacities.put("test", 0.5);
        },
        "Should return unmodifiable map");
  }

  @Test
  void testFontFamilyAreUnmodifiable() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Map<String, String> fonts = config.fontFamily();
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          fonts.put("test", "Test Font");
        },
        "Should return unmodifiable map");
  }

  @Test
  void testFontSizeAreUnmodifiable() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Map<String, Double> sizes = config.fontSize();
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          sizes.put("test", 99.0);
        },
        "Should return unmodifiable map");
  }

  @Test
  void testFontWeightAreUnmodifiable() {
    ThemeConfig config = ThemeConfig.defaultConfig();

    Map<String, String> weights = config.fontWeight();
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          weights.put("test", "999");
        },
        "Should return unmodifiable map");
  }
}
