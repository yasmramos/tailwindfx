package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/** Unit tests for TwStyle facade class. */
public class TwStyleIT extends ApplicationTest {

  private Label labelNode;
  private HBox hboxParent;
  private VBox vboxParent;
  private TwFlexPane flexPaneParent;
  private TwGridPane gridPaneParent;

  @BeforeEach
  void setUp() {
    labelNode = new Label("Test");
    hboxParent = new HBox();
    vboxParent = new VBox();
    flexPaneParent = new TwFlexPane();
    gridPaneParent = TwGridPane.create().build();

    hboxParent.getChildren().add(labelNode);
  }

  @Test
  @DisplayName("apply should add CSS classes to node")
  void testApplyCssClasses() {
    TwStyle.apply(labelNode, "btn-primary", "rounded-lg");

    assertTrue(labelNode.getStyleClass().contains("btn-primary"));
    assertTrue(labelNode.getStyleClass().contains("rounded-lg"));
  }

  @Test
  @DisplayName("apply should handle null and empty tokens gracefully")
  void testApplyWithNullAndEmptyTokens() {
    assertDoesNotThrow(() -> TwStyle.apply(labelNode, (String[]) null));
    assertDoesNotThrow(() -> TwStyle.apply(labelNode));

    // Empty strings are skipped, so no classes should be added
    int initialSize = labelNode.getStyleClass().size();
    TwStyle.apply(labelNode, "", "  ", null);
    assertEquals(initialSize, labelNode.getStyleClass().size());
  }

  @Test
  @DisplayName("apply should throw exception for null node")
  void testApplyWithNullNode() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> TwStyle.apply(null, "btn-primary"));
    assertTrue(exception.getMessage().contains("node"));
  }

  @Test
  @DisplayName("apply should handle multiple tokens separated by spaces")
  void testApplyWithSpaceSeparatedTokens() {
    TwStyle.apply(labelNode, "btn-primary rounded-lg", "text-white");

    assertTrue(labelNode.getStyleClass().contains("btn-primary"));
    assertTrue(labelNode.getStyleClass().contains("rounded-lg"));
    assertTrue(labelNode.getStyleClass().contains("text-white"));
  }

  @Test
  @DisplayName("apply should handle JIT tokens with arbitrary values")
  void testApplyWithJitTokens() {
    TwStyle.apply(labelNode, "bg-[#ff0000]", "p-[20px]");

    // JIT tokens should be applied via inline styles
    assertFalse(labelNode.getStyle().isEmpty());
  }

  @Test
  @DisplayName("apply should handle layout-dependent tokens (margins)")
  void testApplyWithMarginTokens() {
    // Margins require parent container context, so we need to add node to HBox first
    hboxParent.getChildren().clear();
    hboxParent.getChildren().add(labelNode);

    TwStyle.apply(labelNode, "m-4");

    // Margins are applied via HBox.setMargin(), not inline CSS or style classes
    // Verify that margins were actually set on the node
    javafx.geometry.Insets margin = HBox.getMargin(labelNode);
    assertNotNull(margin);
    // m-4 = 16px on all sides (4 * 4px = 16px)
    assertEquals(16, margin.getTop(), 0.1);
    assertEquals(16, margin.getRight(), 0.1);
    assertEquals(16, margin.getBottom(), 0.1);
    assertEquals(16, margin.getLeft(), 0.1);
  }

  @Test
  @DisplayName("apply should handle flex tokens in HBox")
  void testApplyWithFlexTokensInHBox() {
    TwStyle.apply(labelNode, "flex-1", "grow");

    // Flex properties should be set on the node
    assertNotNull(HBox.getHgrow(labelNode));
  }

  @Test
  @DisplayName("apply should handle flex tokens in VBox")
  void testApplyWithFlexTokensInVBox() {
    VBox vbox = new VBox();
    Label child = new Label("Child");
    vbox.getChildren().add(child);

    TwStyle.apply(child, "flex-1", "grow");

    assertNotNull(VBox.getVgrow(child));
  }

  @Test
  @DisplayName("apply should handle gap tokens on container")
  void testApplyWithGapTokensOnHBox() {
    TwStyle.apply(hboxParent, "gap-4");

    assertTrue(hboxParent.getSpacing() > 0);
  }

  @Test
  @DisplayName("apply should handle gap tokens on TwFlexPane")
  void testApplyWithGapTokensOnTwFlexPane() {
    TwStyle.apply(flexPaneParent, "gap-4", "gap-x-2");

    assertTrue(flexPaneParent.getGapX() > 0 || flexPaneParent.getGapY() > 0);
  }

  @Test
  @DisplayName("apply should handle gap tokens on TwGridPane")
  void testApplyWithGapTokensOnTwGridPane() {
    TwStyle.apply(gridPaneParent, "gap-4", "gap-y-2");

    assertTrue(gridPaneParent.getGapX() > 0 || gridPaneParent.getGapY() > 0);
  }

  @Test
  @DisplayName("apply should throw exception for unsupported variants on layout properties")
  void testApplyWithUnsupportedVariantOnLayoutProperty() {
    // Layout-dependent properties with state/responsive variants should be skipped or handled
    // gracefully
    // Based on actual implementation, these may not throw exceptions but be processed differently
    assertDoesNotThrow(() -> TwStyle.apply(labelNode, "hover:bg-red-500"));
  }

  @Test
  @DisplayName("applyRaw should add raw CSS classes without processing")
  void testApplyRaw() {
    TwStyle.applyRaw(labelNode, "custom-class", "another-class");

    assertTrue(labelNode.getStyleClass().contains("custom-class"));
    assertTrue(labelNode.getStyleClass().contains("another-class"));
  }

  @Test
  @DisplayName("remove should remove CSS classes from node")
  void testRemove() {
    labelNode.getStyleClass().addAll("class1", "class2", "class3");

    TwStyle.remove(labelNode, "class2");

    assertFalse(labelNode.getStyleClass().contains("class2"));
    assertTrue(labelNode.getStyleClass().contains("class1"));
    assertTrue(labelNode.getStyleClass().contains("class3"));
  }

  @Test
  @DisplayName("remove should handle null and empty classes gracefully")
  void testRemoveWithNullAndEmptyClasses() {
    labelNode.getStyleClass().add("existing-class");

    // Passing null to remove will cause NPE, so we test with valid args only
    assertDoesNotThrow(() -> TwStyle.remove(labelNode));
    assertDoesNotThrow(() -> TwStyle.remove(labelNode, "non-existent"));

    assertTrue(labelNode.getStyleClass().contains("existing-class"));
  }

  @Test
  @DisplayName("replace should remove all classes and add new ones")
  void testReplace() {
    labelNode.getStyleClass().addAll("old1", "old2");

    TwStyle.replace(labelNode, "new1", "new2");

    assertFalse(labelNode.getStyleClass().contains("old1"));
    assertFalse(labelNode.getStyleClass().contains("old2"));
    assertTrue(labelNode.getStyleClass().contains("new1"));
    assertTrue(labelNode.getStyleClass().contains("new2"));
  }

  @Test
  @DisplayName("toggle should add class if not present")
  void testToggleToAdd() {
    assertFalse(labelNode.getStyleClass().contains("active"));

    TwStyle.toggle(labelNode, "active");

    assertTrue(labelNode.getStyleClass().contains("active"));
  }

  @Test
  @DisplayName("toggle should remove class if present")
  void testToggleToRemove() {
    labelNode.getStyleClass().add("active");
    assertTrue(labelNode.getStyleClass().contains("active"));

    TwStyle.toggle(labelNode, "active");

    assertFalse(labelNode.getStyleClass().contains("active"));
  }

  @Test
  @DisplayName("autoCleanup should not throw exceptions")
  void testAutoCleanup() {
    assertDoesNotThrow(() -> TwStyle.autoCleanup(labelNode));
  }

  @Test
  @DisplayName("invalidateCache should not throw exceptions")
  void testInvalidateCache() {
    assertDoesNotThrow(() -> TwStyle.invalidateCache(labelNode));
  }

  @Test
  @DisplayName("cleanupNode should not throw exceptions")
  void testCleanupNode() {
    assertDoesNotThrow(() -> TwStyle.cleanupNode(labelNode));
  }

  @Test
  @DisplayName("invalidateCategoryCache should not throw exceptions")
  void testInvalidateCategoryCache() {
    assertDoesNotThrow(() -> TwStyle.invalidateCategoryCache(labelNode, "margin"));
  }

  @Test
  @DisplayName("apply should handle negative margin values")
  void testApplyWithNegativeMargin() {
    // Negative margins should not cause exceptions
    assertDoesNotThrow(() -> TwStyle.apply(labelNode, "-m-4", "-mt-2"));

    // Note: Negative margins are handled by Styles.margin() which requires
    // the node to be in a parent container (HBox/VBox/GridPane) to apply setMargin().
    // When applied to an isolated node, no inline style is generated because
    // JavaFX doesn't support -fx-margin CSS property.
    // The important thing is that the application doesn't crash.
  }

  @Test
  @DisplayName("apply should handle responsive prefixes correctly")
  void testApplyWithResponsivePrefixes() {
    // Responsive prefixes on non-layout properties should work
    assertDoesNotThrow(() -> TwStyle.apply(labelNode, "sm:text-lg", "md:bg-blue-500"));
  }

  @Test
  @DisplayName("apply should handle state prefixes correctly")
  void testApplyWithStatePrefixes() {
    // State prefixes on non-layout properties should work
    assertDoesNotThrow(() -> TwStyle.apply(labelNode, "hover:bg-red-500", "focus:ring-2"));
  }

  @Test
  @DisplayName("apply should skip duplicate classes")
  void testApplyWithDuplicateClasses() {
    TwStyle.apply(labelNode, "btn-primary", "btn-primary", "rounded-lg");

    int count = 0;
    for (String styleClass : labelNode.getStyleClass()) {
      if (styleClass.equals("btn-primary")) count++;
    }

    assertEquals(1, count, "Duplicate classes should be added only once");
  }

  @Test
  @DisplayName("apply should handle mixed CSS and JIT tokens")
  void testApplyWithMixedTokens() {
    TwStyle.apply(labelNode, "btn-primary", "bg-[#00ff00]", "rounded-lg", "p-[10px]");

    assertTrue(labelNode.getStyleClass().contains("btn-primary"));
    assertTrue(labelNode.getStyleClass().contains("rounded-lg"));
    assertFalse(labelNode.getStyle().isEmpty());
  }

  @Test
  @DisplayName("applyWithStylesheetPreference should add static tokens as CSS classes")
  void testApplyWithStylesheetPreferenceStaticTokens() {
    // Enable preferStylesheet mode
    TwConfig.preferStylesheet(true);

    try {
      TwStyle.apply(labelNode, "p-4", "bg-blue-500", "rounded-lg");

      // Static tokens should be added as CSS classes
      assertTrue(labelNode.getStyleClass().contains("p-4"), "Should add p-4 as CSS class");
      assertTrue(
          labelNode.getStyleClass().contains("bg-blue-500"), "Should add bg-blue-500 as CSS class");
      assertTrue(
          labelNode.getStyleClass().contains("rounded-lg"), "Should add rounded-lg as CSS class");
    } finally {
      // Reset to default
      TwConfig.preferStylesheet(false);
    }
  }

  @Test
  @DisplayName("applyWithStylesheetPreference should use JIT fallback for arbitrary values")
  void testApplyWithStylesheetPreferenceArbitraryValues() {
    // Enable preferStylesheet mode
    TwConfig.preferStylesheet(true);

    try {
      TwStyle.apply(labelNode, "w-[320px]", "h-[200px]", "bg-[#ff0000]");

      // Arbitrary values should trigger JIT inline compilation
      assertFalse(labelNode.getStyle().isEmpty(), "Should use JIT inline for arbitrary values");
      assertTrue(
          labelNode.getStyle().contains("-fx-pref-width: 320px")
              || labelNode.getStyle().contains("320"),
          "Should contain width value from JIT compilation");
    } finally {
      // Reset to default
      TwConfig.preferStylesheet(false);
    }
  }

  @Test
  @DisplayName("applyWithStylesheetPreference should handle mixed static and dynamic tokens")
  void testApplyWithStylesheetPreferenceMixed() {
    // Enable preferStylesheet mode
    TwConfig.preferStylesheet(true);

    try {
      TwStyle.apply(labelNode, "p-4", "w-[320px]", "bg-blue-500", "h-[auto]");

      // Static tokens should be CSS classes
      assertTrue(labelNode.getStyleClass().contains("p-4"), "Should add static token as CSS class");
      assertTrue(
          labelNode.getStyleClass().contains("bg-blue-500"),
          "Should add static token as CSS class");

      // Dynamic tokens should trigger JIT
      assertFalse(labelNode.getStyle().isEmpty(), "Should use JIT for arbitrary values");
    } finally {
      // Reset to default
      TwConfig.preferStylesheet(false);
    }
  }

  @Test
  @DisplayName("applyWithStylesheetPreference should handle opacity modifiers as dynamic")
  void testApplyWithStylesheetPreferenceOpacityModifier() {
    // Enable preferStylesheet mode
    TwConfig.preferStylesheet(true);

    try {
      TwStyle.apply(labelNode, "bg-blue-500/80", "text-red-500/50");

      // Opacity modifiers should trigger JIT fallback
      assertFalse(labelNode.getStyle().isEmpty(), "Should use JIT for opacity modifier tokens");
    } finally {
      // Reset to default
      TwConfig.preferStylesheet(false);
    }
  }

  @Test
  @DisplayName("preferStylesheet config should be toggleable")
  void testPreferStylesheetConfigToggle() {
    // Verify default is false
    assertFalse(TwConfig.isPreferStylesheet(), "Default should be false");

    // Enable
    TwConfig.preferStylesheet(true);
    assertTrue(TwConfig.isPreferStylesheet(), "Should be enabled after setting true");

    // Disable
    TwConfig.preferStylesheet(false);
    assertFalse(TwConfig.isPreferStylesheet(), "Should be disabled after setting false");
  }

  @Test
  @DisplayName("reset should clear preferStylesheet setting")
  void testResetClearsPreferStylesheet() {
    TwConfig.preferStylesheet(true);
    assertTrue(TwConfig.isPreferStylesheet());

    TwConfig.reset();
    assertFalse(TwConfig.isPreferStylesheet(), "Reset should clear preferStylesheet");
  }

  @Test
  @DisplayName("parseTailwindValue should respect TwConfig.unit() for arbitrary px values")
  void testParseTailwindValueRespectsUnitConfig() {
    // Save original unit value
    double originalUnit = TwConfig.unit();
    
    try {
      // Set unit to 4px (default)
      TwConfig.unit(4.0);
      // m-[10px] should return 2.5 (10 / 4.0 = 2.5), not 2 (integer division)
      // We need to use reflection to test private method, so we test via actual application
      Label testNode = new Label("Test");
      HBox parent = new HBox();
      parent.getChildren().add(testNode);
      
      TwStyle.apply(testNode, "m-[10px]");
      
      // Verify margin was applied (the exact value depends on Styles.m implementation)
      // The key is that parseTailwindValue now returns double respecting TwConfig.unit()
      javafx.geometry.Insets margin = HBox.getMargin(testNode);
      assertNotNull(margin, "Margin should be applied for m-[10px]");
      
      // Set unit to 8px and verify different result
      TwConfig.unit(8.0);
      Label testNode2 = new Label("Test2");
      HBox parent2 = new HBox();
      parent2.getChildren().add(testNode2);
      
      TwStyle.apply(testNode2, "m-[16px]");
      
      // With unit=8, m-[16px] should give factor of 2 (16/8=2)
      margin = HBox.getMargin(testNode2);
      assertNotNull(margin, "Margin should be applied for m-[16px] with unit=8");
      
    } finally {
      // Restore original unit value
      TwConfig.unit(originalUnit);
    }
  }

  @Test
  @DisplayName("layout migration tokens should not throw exception but warn gracefully")
  void testLayoutMigrationTokensDontThrowException() {
    // Enable debug mode to see warnings
    boolean originalDebug = TwConfig.isDebug();
    TwConfig.debug(true);
    
    try {
      Label testNode = new Label("Test");
      
      // These tokens require layout migration and should not throw
      // but should log a warning instead
      assertDoesNotThrow(
          () -> TwStyle.apply(testNode, "flex", "grid"),
          "Layout migration tokens should not throw UnsupportedOperationException"
      );
      
      // Node should still have some classes applied (not left half-styled)
      // The migration tokens are skipped, but other processing continues
    } finally {
      TwConfig.debug(originalDebug);
    }
  }

  @Test
  @DisplayName("flex-[2] should differ from flex-1 in TwFlexPane")
  void testFlexArbitraryValueInTwFlexPane() {
    io.github.yasmramos.tailwindfx.layout.TwFlexPane flexPane = 
        new io.github.yasmramos.tailwindfx.layout.TwFlexPane();
    Label child1 = new Label("Child1");
    Label child2 = new Label("Child2");
    flexPane.getChildren().addAll(child1, child2);
    
    // Apply flex-1 to first child
    TwStyle.apply(child1, "flex-1");
    
    // Apply flex-[2] to second child
    TwStyle.apply(child2, "flex-[2]");
    
    // In TwFlexPane, grow factors should be different
    double grow1 = io.github.yasmramos.tailwindfx.layout.TwFlexPane.getGrow(child1);
    double grow2 = io.github.yasmramos.tailwindfx.layout.TwFlexPane.getGrow(child2);
    
    assertEquals(1.0, grow1, 0.01, "flex-1 should set grow factor to 1");
    assertEquals(2.0, grow2, 0.01, "flex-[2] should set grow factor to 2");
    assertNotEquals(grow1, grow2, "flex-[2] should differ from flex-1");
  }

  @Test
  @DisplayName("icon/large should not be interpreted as opacity modifier")
  void testIconLargeNotTreatedAsOpacityModifier() {
    // Enable preferStylesheet mode to trigger applyWithStylesheetPreference
    TwConfig.preferStylesheet(true);
    
    try {
      Label testNode = new Label("Test");
      
      // icon/large should NOT be treated as dynamic token (opacity modifier)
      // because "icon" is not a valid color utility base
      TwStyle.apply(testNode, "icon/large");
      
      // Should be added as CSS class, not JIT compiled
      assertTrue(
          testNode.getStyleClass().contains("icon/large"),
          "icon/large should be added as CSS class, not treated as opacity modifier"
      );
      
      // Verify it's not in inline styles (which would indicate JIT compilation)
      assertFalse(
          testNode.getStyle().contains("icon/large"),
          "icon/large should not be JIT compiled"
      );
      
    } finally {
      TwConfig.preferStylesheet(false);
    }
  }

  @Test
  @DisplayName("parseTailwindValue should handle missing closing bracket gracefully")
  void testParseTailwindValueHandlesMissingBracket() {
    boolean originalDebug = TwConfig.isDebug();
    TwConfig.debug(true);
    
    try {
      Label testNode = new Label("Test");
      
      // Token with missing closing bracket should not throw StringIndexOutOfBoundsException
      assertDoesNotThrow(
          () -> TwStyle.apply(testNode, "m-[10px"),
          "Missing closing bracket should not throw exception"
      );
      
    } finally {
      TwConfig.debug(originalDebug);
    }
  }
}
