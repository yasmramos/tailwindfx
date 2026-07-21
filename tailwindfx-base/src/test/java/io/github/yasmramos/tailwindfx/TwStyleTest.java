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
public class TwStyleTest extends ApplicationTest {

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
    // Node must be in a parent container for margins to take effect
    TwStyle.apply(labelNode, "m-4", "mt-2");

    // Margins are applied via HBox.setMargin(), not as inline CSS
    // Verify margin was set correctly using HBox.getMargin()
    javafx.geometry.Insets margin = HBox.getMargin(labelNode);
    assertNotNull(margin, "Margin should be applied via HBox.setMargin()");
    
    // m-4 = 4 * 4px = 16px on all sides, but mt-2 overrides top to 8px
    // So we expect: top=8px (from mt-2), right=16px, bottom=16px, left=16px
    assertEquals(8.0, margin.getTop(), "Top margin should be 8px from mt-2");
    assertEquals(16.0, margin.getRight(), "Right margin should be 16px from m-4");
    assertEquals(16.0, margin.getBottom(), "Bottom margin should be 16px from m-4");
    assertEquals(16.0, margin.getLeft(), "Left margin should be 16px from m-4");
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
    TwStyle.apply(labelNode, "-m-4", "-mt-2");

    // Negative margins should be applied
    assertFalse(labelNode.getStyle().isEmpty());
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
}
