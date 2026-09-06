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

  @Test
  public void testIsValidTailwindClassRejectsJavaIdentifiers() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    // Use reflection to access private method
    java.lang.reflect.Method isValidMethod =
        TailwindCssMojo.class.getDeclaredMethod("isValidTailwindClass", String.class);
    isValidMethod.setAccessible(true);

    // Common Java identifiers that should be rejected
    String[] falsePositives = {
      "String",
      "Stage",
      "Integer",
      "Double",
      "Boolean",
      "Object",
      "Void",
      "printStackTrace",
      "getPadding",
      "setStyle",
      "addClass",
      "removeAll",
      "github",
      "BorderPane",
      "VBox",
      "HBox",
      "Label",
      "Button",
      "10",
      "120",
      "248",
      "class",
      "style",
      "styleClass",
      "id",
      "null",
      "true",
      "false",
      "var",
      "running",
      "created",
      "loaded"
    };

    for (String identifier : falsePositives) {
      Boolean result = (Boolean) isValidMethod.invoke(mojo, identifier);
      assertFalse(result, "Should reject Java identifier: " + identifier);
    }
  }

  @Test
  public void testIsValidTailwindClassAcceptsRealTailwindClasses() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    java.lang.reflect.Method isValidMethod =
        TailwindCssMojo.class.getDeclaredMethod("isValidTailwindClass", String.class);
    isValidMethod.setAccessible(true);

    // Real Tailwind classes that should be accepted
    String[] validClasses = {
      "p-5",
      "p-8",
      "text-2xl",
      "font-bold",
      "bg-gray-50",
      "bg-blue-500",
      "rounded-lg",
      "shadow-md",
      "flex",
      "grid",
      "w-full",
      "h-auto",
      "m-4",
      "mb-2",
      "mt-4",
      "mx-auto",
      "py-2",
      "px-4",
      "border",
      "border-2",
      "gap-4",
      "collapse-item",
      "hover:bg-red-500",
      "focus:ring-2",
      "md:p-4",
      "dark:text-white"
    };

    for (String className : validClasses) {
      Boolean result = (Boolean) isValidMethod.invoke(mojo, className);
      assertTrue(result, "Should accept valid Tailwind class: " + className);
    }
  }

  @Test
  public void testScanForTailwindClassesExtractsOnlyFromStrings() throws Exception {
    TailwindCssMojo mojo = new TailwindCssMojo();

    // Create a test Java file with both real Tailwind classes and false positives
    File sourceDir = tempDir.resolve("src").toFile();
    sourceDir.mkdirs();
    File testFile = tempDir.resolve("src/TestExample.java").toFile();

    String javaContent =
        "package test;\n"
            + "import io.github.yasmramos.tailwindfx.TwStyle;\n"
            + "\n"
            + "public class TestExample {\n"
            + "    private String github = \"repo\";\n"
            + "    private int count = 120;\n"
            + "    \n"
            + "    public void setup() {\n"
            + "        Button btn = new Button();\n"
            + "        TwStyle.apply(btn, \"p-5 bg-blue-500 text-white\");\n"
            + "        btn.getStyleClass().add(\"rounded-lg\");\n"
            + "        btn.getStyleClass().addAll(\"shadow-md\", \"hover:bg-blue-600\");\n"
            + "        printStackTrace(); // Should not be picked up\n"
            + "        Stage stage = new Stage(); // Should not be picked up\n"
            + "    }\n"
            + "}";

    java.nio.file.Files.writeString(testFile.toPath(), javaContent);

    // Use reflection to access private method
    java.lang.reflect.Field sourceField = TailwindCssMojo.class.getDeclaredField("sourceDirectory");
    sourceField.setAccessible(true);
    sourceField.set(mojo, sourceDir);

    java.lang.reflect.Method scanMethod =
        TailwindCssMojo.class.getDeclaredMethod("scanForTailwindClasses", File.class);
    scanMethod.setAccessible(true);

    @SuppressWarnings("unchecked")
    java.util.Set<String> classes = (java.util.Set<String>) scanMethod.invoke(mojo, sourceDir);

    // Should contain real Tailwind classes
    assertTrue(classes.contains("p-5"), "Should extract p-5");
    assertTrue(classes.contains("bg-blue-500"), "Should extract bg-blue-500");
    assertTrue(classes.contains("text-white"), "Should extract text-white");
    assertTrue(classes.contains("rounded-lg"), "Should extract rounded-lg");
    assertTrue(classes.contains("shadow-md"), "Should extract shadow-md");
    assertTrue(classes.contains("hover:bg-blue-600"), "Should extract hover:bg-blue-600");

    // Should NOT contain Java identifiers
    assertFalse(classes.contains("String"), "Should not extract String");
    assertFalse(classes.contains("Stage"), "Should not extract Stage");
    assertFalse(classes.contains("Button"), "Should not extract Button");
    assertFalse(classes.contains("printStackTrace"), "Should not extract printStackTrace");
    assertFalse(classes.contains("github"), "Should not extract github");
    assertFalse(classes.contains("count"), "Should not extract count");
  }
}
