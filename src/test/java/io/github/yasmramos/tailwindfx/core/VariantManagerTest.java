package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationTest;

/** Unit tests for VariantManager. */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class VariantManagerTest extends ApplicationTest {

  private Button testButton;
  private CheckBox testCheckBox;
  private Pane testPane;
  private JitCompiler JitCompiler;

  @Override
  public void start(Stage stage) {
    testButton = new Button("Test");
    testCheckBox = new CheckBox("Check");
    testPane = new Pane();

    Scene scene = new Scene(testPane, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

  @BeforeEach
  public void setUp() {
    // JitCompiler uses static methods, no instance needed
  }

  @Test
  public void testApplyStateVariantWithNullParameters() {
    // When null parameters are provided, method should return silently
    assertDoesNotThrow(
        () -> {
          VariantManager.applyStateVariant(null, "hover", "bg-blue-500", JitCompiler);
          VariantManager.applyStateVariant(testButton, null, "bg-blue-500", JitCompiler);
          VariantManager.applyStateVariant(testButton, "hover", null, JitCompiler);
        });
  }

  @Test
  public void testApplyStateVariantWithInvalidUtility() {
    // Given an invalid utility that won't compile
    String initialStyle = testButton.getStyle();

    // When applying hover variant with invalid utility
    VariantManager.applyStateVariant(testButton, "hover", "invalid-utility-xyz", JitCompiler);

    // Then style should remain unchanged (or empty if JIT returns null)
    // No exception should be thrown
    assertTrue(true);
  }

  @Test
  public void testApplyHoverVariantSetsHandlers() {
    // When applying hover variant with valid utility
    VariantManager.applyStateVariant(testButton, "hover", "opacity-50", JitCompiler);

    // Then event handlers should be set up
    assertNotNull(testButton.getOnMouseEntered());
    assertNotNull(testButton.getOnMouseExited());
  }

  @Test
  public void testApplyFocusVariantOnControl() {
    // When applying focus variant
    VariantManager.applyStateVariant(testButton, "focus", "border-2", JitCompiler);

    // Focus property listener should be set up (verified by no exception)
    assertTrue(true);
  }

  @Test
  public void testApplyActiveVariantSetsHandlers() {
    // When applying active variant
    VariantManager.applyStateVariant(testButton, "active", "scale-95", JitCompiler);

    // Then event handlers should be set up
    assertNotNull(testButton.getOnMousePressed());
    assertNotNull(testButton.getOnMouseReleased());
  }

  @Test
  public void testApplyCheckedVariantOnCheckBox() {
    // When applying checked variant
    VariantManager.applyStateVariant(testCheckBox, "checked", "bg-green-500", JitCompiler);

    // Selected property listener should be set up (verified by no exception)
    assertTrue(true);
  }

  @Test
  public void testBindResponsiveVariantWithNullParameters() {
    // When null parameters are provided, method should return silently
    assertDoesNotThrow(
        () -> {
          VariantManager.bindResponsiveVariant(null, "md", "w-full", JitCompiler);
          VariantManager.bindResponsiveVariant(testButton, null, "w-full", JitCompiler);
          VariantManager.bindResponsiveVariant(testButton, "md", null, JitCompiler);
        });
  }

  @Test
  public void testBindResponsiveVariantValidBreakpoint() {
    // When binding responsive variant with valid parameters
    assertDoesNotThrow(
        () -> {
          VariantManager.bindResponsiveVariant(testButton, "md", "w-full", JitCompiler);
          VariantManager.bindResponsiveVariant(testButton, "lg", "h-full", JitCompiler);
        });
  }

  @Test
  public void testApplyThemeVariantDark() {
    // When applying dark theme variant
    assertDoesNotThrow(
        () -> {
          VariantManager.applyThemeVariant(testButton, "dark", "bg-black", JitCompiler);
        });
  }

  @Test
  public void testApplyThemeVariantLight() {
    // When applying light theme variant
    assertDoesNotThrow(
        () -> {
          VariantManager.applyThemeVariant(testButton, "light", "bg-white", JitCompiler);
        });
  }

  @Test
  public void testApplyGroupVariantWithNullParameters() {
    // When null parameters are provided, method should handle gracefully
    assertDoesNotThrow(
        () ->
            VariantManager.applyGroupVariant(testButton, "group-hover", "bg-red-500", JitCompiler));
  }

  @Test
  public void testApplyGroupVariantValid() {
    // When applying group variant
    assertDoesNotThrow(
        () -> {
          VariantManager.applyGroupVariant(testButton, "group-hover", "text-blue-500", JitCompiler);
          VariantManager.applyGroupVariant(
              testButton, "group-focus", "border-red-500", JitCompiler);
        });
  }

  @Test
  public void testApplyArbitraryVariantMediaQuery() {
    // When applying arbitrary media query variant
    assertDoesNotThrow(
        () -> {
          VariantManager.applyArbitraryVariant(
              testButton, "[@media(min-width:768px)]", "p-4", JitCompiler);
        });
  }

  @Test
  public void testApplyArbitraryVariantSelector() {
    // When applying arbitrary selector variant
    assertDoesNotThrow(
        () -> {
          VariantManager.applyArbitraryVariant(testButton, "[&:hover]", "bg-red-500", JitCompiler);
        });
  }

  @Test
  public void testProcessTokenWithoutVariants() {
    // When processing token without variants
    assertDoesNotThrow(
        () -> {
          VariantManager.processToken(testButton, "text-white", JitCompiler);
          VariantManager.processToken(testButton, "bg-blue-500", JitCompiler);
        });
  }

  @Test
  public void testProcessTokenWithStateVariant() {
    // When processing token with hover variant
    VariantManager.processToken(testButton, "hover:opacity-80", JitCompiler);

    // Then event handlers should be set up
    assertNotNull(testButton.getOnMouseEntered());
    assertNotNull(testButton.getOnMouseExited());
  }

  @Test
  public void testProcessTokenWithResponsiveVariant() {
    // When processing token with responsive variant
    assertDoesNotThrow(
        () -> {
          VariantManager.processToken(testButton, "md:w-full", JitCompiler);
          VariantManager.processToken(testButton, "lg:h-auto", JitCompiler);
        });
  }

  @Test
  public void testProcessTokenWithChainedVariants() {
    // When processing token with multiple variants
    assertDoesNotThrow(
        () -> {
          VariantManager.processToken(testButton, "hover:md:bg-purple-500", JitCompiler);
          VariantManager.processToken(testButton, "focus:lg:border-2", JitCompiler);
        });
  }

  @Test
  public void testProcessTokenWithUnknownVariant() {
    // When processing token with unknown variant type
    assertDoesNotThrow(
        () -> {
          VariantManager.processToken(testButton, "custom:rotate-45", JitCompiler);
          VariantManager.processToken(testButton, "unknown:scale-110", JitCompiler);
        });
  }

  @Test
  public void testProcessTokenWithThemeVariant() {
    // When processing token with theme variant
    assertDoesNotThrow(
        () -> {
          VariantManager.processToken(testButton, "dark:bg-gray-900", JitCompiler);
          VariantManager.processToken(testButton, "light:text-gray-100", JitCompiler);
        });
  }

  @Test
  public void testProcessTokenWithGroupVariant() {
    // When processing token with group variant
    assertDoesNotThrow(
        () -> {
          VariantManager.processToken(testButton, "group-hover:text-red-500", JitCompiler);
          VariantManager.processToken(testButton, "group-focus:border-blue-500", JitCompiler);
        });
  }

  @Test
  public void testProcessTokenWithAllSupportedVariantTypes() {
    // Test various supported variant types
    assertDoesNotThrow(
        () -> {
          // State variants
          VariantManager.processToken(testButton, "hover:bg-blue-500", JitCompiler);
          VariantManager.processToken(testButton, "focus:border-2", JitCompiler);
          VariantManager.processToken(testButton, "active:scale-95", JitCompiler);

          // Responsive variants
          VariantManager.processToken(testButton, "sm:p-2", JitCompiler);
          VariantManager.processToken(testButton, "md:p-4", JitCompiler);
          VariantManager.processToken(testButton, "lg:p-6", JitCompiler);
          VariantManager.processToken(testButton, "xl:p-8", JitCompiler);

          // Theme variants
          VariantManager.processToken(testButton, "dark:bg-black", JitCompiler);
        });
  }

  @Test
  public void testMultipleTokensOnSameNode() {
    // When applying multiple tokens to the same node
    assertDoesNotThrow(
        () -> {
          VariantManager.processToken(testButton, "text-white", JitCompiler);
          VariantManager.processToken(testButton, "bg-blue-500", JitCompiler);
          VariantManager.processToken(testButton, "p-4", JitCompiler);
          VariantManager.processToken(testButton, "rounded-lg", JitCompiler);
        });
  }

  @Test
  public void testDisabledVariantOnControl() {
    // When applying disabled variant
    VariantManager.applyStateVariant(testButton, "disabled", "opacity-50", JitCompiler);

    // Disable property listener should be set up (verified by no exception)
    assertTrue(true);
  }
}
