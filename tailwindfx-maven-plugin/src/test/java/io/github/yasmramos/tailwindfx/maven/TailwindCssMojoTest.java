package io.github.yasmramos.tailwindfx.maven;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TailwindCssMojoTest {

  @TempDir Path tempDir;

  @Test
  public void testMojoExecution() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    // Configurar directorios temporales usando reflexión (como hace Maven)
    File sourceDir = tempDir.resolve("src").toFile();
    sourceDir.mkdirs();
    File outputDir = tempDir.resolve("output").toFile();

    java.lang.reflect.Field sourceField = TailwindCssMojo.class.getDeclaredField("sourceDirectory");
    sourceField.setAccessible(true);
    sourceField.set(mojo, sourceDir);

    java.lang.reflect.Field outputField = TailwindCssMojo.class.getDeclaredField("outputDirectory");
    outputField.setAccessible(true);
    outputField.set(mojo, outputDir);

    // Ejecutar el mojo
    assertDoesNotThrow(() -> mojo.execute());

    // Verificar que se creó el archivo CSS
    File cssFile = new File(outputDir, "tailwindfx-generated.css");
    assertTrue(cssFile.exists(), "El archivo CSS debería existir");
  }

  @Test
  public void testOutputDirectoryCreation() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    File sourceDir = tempDir.resolve("src").toFile();
    sourceDir.mkdirs();
    File outputDir = tempDir.resolve("new-output").toFile();

    java.lang.reflect.Field sourceField = TailwindCssMojo.class.getDeclaredField("sourceDirectory");
    sourceField.setAccessible(true);
    sourceField.set(mojo, sourceDir);

    java.lang.reflect.Field outputField = TailwindCssMojo.class.getDeclaredField("outputDirectory");
    outputField.setAccessible(true);
    outputField.set(mojo, outputDir);

    mojo.execute();

    assertTrue(outputDir.exists(), "El directorio de salida debería crearse");
    assertTrue(outputDir.isDirectory(), "Debería ser un directorio");
  }

  @Test
  public void testConvertToClassCssWithHoverVariant() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    // Use reflection to access private method
    java.lang.reflect.Method convertMethod =
        TailwindCssMojo.class.getDeclaredMethod("convertToClassCss", String.class, String.class);
    convertMethod.setAccessible(true);

    // Test hover variant translation: hover:bg-blue-500 → .bg-blue-500:hover
    String result =
        (String) convertMethod.invoke(mojo, "hover:bg-blue-500", "-fx-background-color: #3b82f6;");

    assertTrue(
        result.contains(".bg-blue-500:hover"),
        "Should translate hover: variant to :hover pseudo-class");
    assertTrue(
        result.contains("-fx-background-color: #3b82f6;"), "Should contain the CSS property");
  }

  @Test
  public void testConvertToClassCssWithFocusVariant() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    java.lang.reflect.Method convertMethod =
        TailwindCssMojo.class.getDeclaredMethod("convertToClassCss", String.class, String.class);
    convertMethod.setAccessible(true);

    // Test focus variant translation: focus:ring-2 → .ring-2:focused
    String result =
        (String)
            convertMethod.invoke(
                mojo, "focus:ring-2", "-fx-border-color: #3b82f6; -fx-border-width: 2px;");

    assertTrue(
        result.contains(".ring-2:focused"),
        "Should translate focus: variant to :focused pseudo-class");
  }

  @Test
  public void testConvertToClassCssWithPressedVariant() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    java.lang.reflect.Method convertMethod =
        TailwindCssMojo.class.getDeclaredMethod("convertToClassCss", String.class, String.class);
    convertMethod.setAccessible(true);

    // Test pressed/active variant translation: pressed:bg-red-500 → .bg-red-500:pressed
    String result1 =
        (String) convertMethod.invoke(mojo, "pressed:bg-red-500", "-fx-background-color: #ef4444;");

    assertTrue(
        result1.contains(".bg-red-500:pressed"),
        "Should translate pressed: variant to :pressed pseudo-class");

    String result2 =
        (String) convertMethod.invoke(mojo, "active:bg-red-500", "-fx-background-color: #ef4444;");

    assertTrue(
        result2.contains(".bg-red-500:pressed"),
        "Should translate active: variant to :pressed pseudo-class");
  }

  @Test
  public void testConvertToClassCssWithDisabledVariant() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    java.lang.reflect.Method convertMethod =
        TailwindCssMojo.class.getDeclaredMethod("convertToClassCss", String.class, String.class);
    convertMethod.setAccessible(true);

    // Test disabled variant translation: disabled:opacity-50 → .opacity-50:disabled
    String result = (String) convertMethod.invoke(mojo, "disabled:opacity-50", "-fx-opacity: 0.5;");

    assertTrue(
        result.contains(".opacity-50:disabled"),
        "Should translate disabled: variant to :disabled pseudo-class");
  }

  @Test
  public void testConvertToClassCssWithBreakpointVariant() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    java.lang.reflect.Method convertMethod =
        TailwindCssMojo.class.getDeclaredMethod("convertToClassCss", String.class, String.class);
    convertMethod.setAccessible(true);

    // Test breakpoint variant translation: md:p-4 → .bp-md .p-4
    String result = (String) convertMethod.invoke(mojo, "md:p-4", "-fx-padding: 16px;");

    assertTrue(
        result.contains(".bp-md .p-4"), "Should translate md: variant to .bp-md context selector");
  }

  @Test
  public void testConvertToClassCssWithDarkVariant() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    java.lang.reflect.Method convertMethod =
        TailwindCssMojo.class.getDeclaredMethod("convertToClassCss", String.class, String.class);
    convertMethod.setAccessible(true);

    // Test dark variant translation: dark:text-white → .dark .text-white
    String result =
        (String) convertMethod.invoke(mojo, "dark:text-white", "-fx-text-fill: #ffffff;");

    assertTrue(
        result.contains(".dark .text-white"),
        "Should translate dark: variant to .dark context selector");
  }

  @Test
  public void testConvertToClassCssFiltersImportant() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    java.lang.reflect.Method convertMethod =
        TailwindCssMojo.class.getDeclaredMethod("convertToClassCss", String.class, String.class);
    convertMethod.setAccessible(true);

    // Test !important filtering
    String result =
        (String) convertMethod.invoke(mojo, "text-red-500", "-fx-text-fill: #ef4444 !important;");

    assertFalse(
        result.contains("!important"), "Should filter out !important as JavaFX doesn't support it");
    assertFalse(
        result.contains("-fx-text-fill: #ef4444 !important;"),
        "Property with !important should be filtered");
  }

  @Test
  public void testConvertToClassCssWithMultipleVariants() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    java.lang.reflect.Method convertMethod =
        TailwindCssMojo.class.getDeclaredMethod("convertToClassCss", String.class, String.class);
    convertMethod.setAccessible(true);

    // Test multiple variants: lg:hover:w-full → .bp-lg .w-full:hover
    String result = (String) convertMethod.invoke(mojo, "lg:hover:w-full", "-fx-pref-width: 100%;");

    assertTrue(result.contains(".bp-lg"), "Should include breakpoint context");
    assertTrue(result.contains(":hover"), "Should include state pseudo-class");
  }

  @Test
  public void testConvertToClassCssEscapesSpecialCharacters() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    java.lang.reflect.Method convertMethod =
        TailwindCssMojo.class.getDeclaredMethod("convertToClassCss", String.class, String.class);
    convertMethod.setAccessible(true);

    // Test class with special characters (e.g., arbitrary values in class name)
    String result =
        (String) convertMethod.invoke(mojo, "bg-[#3b82f6]", "-fx-background-color: #3b82f6;");

    // Should handle special characters properly
    assertNotNull(result);
    assertTrue(result.length() > 0, "Should generate valid CSS");
  }
}
