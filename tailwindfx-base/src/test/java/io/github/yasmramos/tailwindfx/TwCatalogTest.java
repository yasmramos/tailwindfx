package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * TwCatalogTest — Tests for the utility class catalog generator.
 *
 * <p>Verifies that the catalog includes representative families of utilities, is not empty, and
 * generates a complete CSS file with all utilities.
 */
class TwCatalogTest {

  @Test
  void testAllUtilityClassesIsNotEmpty() {
    Set<String> allClasses = TwCatalog.allUtilityClasses();
    assertFalse(allClasses.isEmpty(), "Catalog should not be empty");
  }

  @Test
  void testAllUtilityClassesContainsColorUtilities() {
    Set<String> allClasses = TwCatalog.allUtilityClasses();

    // Background colors
    assertTrue(allClasses.contains("bg-blue-500"), "Should contain bg-blue-500");
    assertTrue(allClasses.contains("bg-red-100"), "Should contain bg-red-100");
    assertTrue(allClasses.contains("bg-green-900"), "Should contain bg-green-900");

    // Text colors
    assertTrue(allClasses.contains("text-blue-500"), "Should contain text-blue-500");
    assertTrue(allClasses.contains("text-gray-700"), "Should contain text-gray-700");

    // Border colors
    assertTrue(allClasses.contains("border-blue-500"), "Should contain border-blue-500");

    // Named colors
    assertTrue(allClasses.contains("bg-white"), "Should contain bg-white");
    assertTrue(allClasses.contains("bg-black"), "Should contain bg-black");
    assertTrue(allClasses.contains("bg-transparent"), "Should contain bg-transparent");
  }

  @Test
  void testAllUtilityClassesContainsSpacingUtilities() {
    Set<String> allClasses = TwCatalog.allUtilityClasses();

    // Padding
    assertTrue(allClasses.contains("p-4"), "Should contain p-4");
    assertTrue(allClasses.contains("px-2"), "Should contain px-2");
    assertTrue(allClasses.contains("py-8"), "Should contain py-8");
    assertTrue(allClasses.contains("pt-1"), "Should contain pt-1");
    assertTrue(allClasses.contains("pr-3"), "Should contain pr-3");
    assertTrue(allClasses.contains("pb-6"), "Should contain pb-6");
    assertTrue(allClasses.contains("pl-5"), "Should contain pl-5");

    // Margin
    assertTrue(allClasses.contains("m-4"), "Should contain m-4");
    assertTrue(allClasses.contains("mx-auto"), "Should contain mx-auto");
    assertTrue(allClasses.contains("-m-2"), "Should contain negative margin -m-2");

    // Gap
    assertTrue(allClasses.contains("gap-4"), "Should contain gap-4");
    assertTrue(allClasses.contains("gap-x-2"), "Should contain gap-x-2");
    assertTrue(allClasses.contains("gap-y-3"), "Should contain gap-y-3");

    // Width and height
    assertTrue(allClasses.contains("w-full"), "Should contain w-full");
    assertTrue(allClasses.contains("h-screen"), "Should contain h-screen");
    assertTrue(allClasses.contains("w-1/2"), "Should contain w-1/2");
    assertTrue(allClasses.contains("h-auto"), "Should contain h-auto");
  }

  @Test
  void testAllUtilityClassesContainsTypographyUtilities() {
    Set<String> allClasses = TwCatalog.allUtilityClasses();

    // Text sizes
    assertTrue(allClasses.contains("text-xs"), "Should contain text-xs");
    assertTrue(allClasses.contains("text-xl"), "Should contain text-xl");
    assertTrue(allClasses.contains("text-9xl"), "Should contain text-9xl");

    // Font weights
    assertTrue(allClasses.contains("font-thin"), "Should contain font-thin");
    assertTrue(allClasses.contains("font-bold"), "Should contain font-bold");
    assertTrue(allClasses.contains("font-black"), "Should contain font-black");

    // Text alignment
    assertTrue(allClasses.contains("text-left"), "Should contain text-left");
    assertTrue(allClasses.contains("text-center"), "Should contain text-center");
    assertTrue(allClasses.contains("text-right"), "Should contain text-right");

    // Text decoration
    assertTrue(allClasses.contains("underline"), "Should contain underline");
    assertTrue(allClasses.contains("line-through"), "Should contain line-through");

    // Text transform
    assertTrue(allClasses.contains("uppercase"), "Should contain uppercase");
    assertTrue(allClasses.contains("capitalize"), "Should contain capitalize");
  }

  @Test
  void testAllUtilityClassesContainsBorderUtilities() {
    Set<String> allClasses = TwCatalog.allUtilityClasses();

    // Border radius
    assertTrue(allClasses.contains("rounded-lg"), "Should contain rounded-lg");
    assertTrue(allClasses.contains("rounded-full"), "Should contain rounded-full");
    assertTrue(allClasses.contains("rounded-none"), "Should contain rounded-none");

    // Border widths
    assertTrue(allClasses.contains("border"), "Should contain border");
    assertTrue(allClasses.contains("border-2"), "Should contain border-2");
    assertTrue(allClasses.contains("border-4"), "Should contain border-4");

    // Border styles
    assertTrue(allClasses.contains("border-solid"), "Should contain border-solid");
    assertTrue(allClasses.contains("border-dashed"), "Should contain border-dashed");
  }

  @Test
  void testAllUtilityClassesContainsEffectUtilities() {
    Set<String> allClasses = TwCatalog.allUtilityClasses();

    // Shadows
    assertTrue(allClasses.contains("shadow"), "Should contain shadow");
    assertTrue(allClasses.contains("shadow-md"), "Should contain shadow-md");
    assertTrue(allClasses.contains("shadow-lg"), "Should contain shadow-lg");
    assertTrue(allClasses.contains("shadow-none"), "Should contain shadow-none");

    // Blur - note: standalone "blur" may not exist, only variants like blur-md
    assertTrue(allClasses.contains("blur-md"), "Should contain blur-md");
    assertTrue(allClasses.contains("blur-none"), "Should contain blur-none");
    assertTrue(allClasses.contains("blur-sm"), "Should contain blur-sm");

    // Brightness
    assertTrue(allClasses.contains("brightness-50"), "Should contain brightness-50");
    assertTrue(allClasses.contains("brightness-100"), "Should contain brightness-100");
    assertTrue(allClasses.contains("brightness-200"), "Should contain brightness-200");

    // Grayscale, invert, sepia
    assertTrue(allClasses.contains("grayscale"), "Should contain grayscale");
    assertTrue(allClasses.contains("grayscale-0"), "Should contain grayscale-0");
    assertTrue(allClasses.contains("invert"), "Should contain invert");
    assertTrue(allClasses.contains("invert-0"), "Should contain invert-0");
    assertTrue(allClasses.contains("sepia"), "Should contain sepia");
    assertTrue(allClasses.contains("sepia-0"), "Should contain sepia-0");
  }

  @Test
  void testAllUtilityClassesContainsTransformUtilities() {
    Set<String> allClasses = TwCatalog.allUtilityClasses();

    // Scale
    assertTrue(allClasses.contains("scale-100"), "Should contain scale-100");
    assertTrue(allClasses.contains("scale-150"), "Should contain scale-150");
    assertTrue(allClasses.contains("scale-x-50"), "Should contain scale-x-50");
    assertTrue(allClasses.contains("scale-y-75"), "Should contain scale-y-75");

    // Rotate
    assertTrue(allClasses.contains("rotate-45"), "Should contain rotate-45");
    assertTrue(allClasses.contains("rotate-90"), "Should contain rotate-90");
    assertTrue(allClasses.contains("-rotate-45"), "Should contain -rotate-45");

    // Translate
    assertTrue(allClasses.contains("translate-x-4"), "Should contain translate-x-4");
    assertTrue(allClasses.contains("translate-y-full"), "Should contain translate-y-full");
    assertTrue(allClasses.contains("-translate-x-1/2"), "Should contain -translate-x-1/2");

    // Opacity
    assertTrue(allClasses.contains("opacity-0"), "Should contain opacity-0");
    assertTrue(allClasses.contains("opacity-50"), "Should contain opacity-50");
    assertTrue(allClasses.contains("opacity-100"), "Should contain opacity-100");

    // Z-index
    assertTrue(allClasses.contains("z-0"), "Should contain z-0");
    assertTrue(allClasses.contains("z-50"), "Should contain z-50");
    assertTrue(allClasses.contains("z-auto"), "Should contain z-auto");
  }

  @Test
  void testAllUtilityClassesContainsLayoutUtilities() {
    Set<String> allClasses = TwCatalog.allUtilityClasses();

    // Display
    assertTrue(allClasses.contains("flex"), "Should contain flex");
    assertTrue(allClasses.contains("inline-flex"), "Should contain inline-flex");
    assertTrue(allClasses.contains("grid"), "Should contain grid");
    assertTrue(allClasses.contains("block"), "Should contain block");
    assertTrue(allClasses.contains("hidden"), "Should contain hidden");
    assertTrue(allClasses.contains("contents"), "Should contain contents");

    // Flexbox
    assertTrue(allClasses.contains("flex-row"), "Should contain flex-row");
    assertTrue(allClasses.contains("flex-col"), "Should contain flex-col");
    assertTrue(allClasses.contains("flex-wrap"), "Should contain flex-wrap");
    assertTrue(allClasses.contains("flex-1"), "Should contain flex-1");

    // Grid
    assertTrue(allClasses.contains("grid-cols-1"), "Should contain grid-cols-1");
    assertTrue(allClasses.contains("grid-cols-12"), "Should contain grid-cols-12");

    // Alignment
    assertTrue(allClasses.contains("justify-center"), "Should contain justify-center");
    assertTrue(allClasses.contains("items-center"), "Should contain items-center");
    assertTrue(allClasses.contains("self-start"), "Should contain self-start");
  }

  @Test
  void testAllUtilityClassesContainsPositioningUtilities() {
    Set<String> allClasses = TwCatalog.allUtilityClasses();

    // Position
    assertTrue(allClasses.contains("static"), "Should contain static");
    assertTrue(allClasses.contains("fixed"), "Should contain fixed");
    assertTrue(allClasses.contains("absolute"), "Should contain absolute");
    assertTrue(allClasses.contains("relative"), "Should contain relative");
    assertTrue(allClasses.contains("sticky"), "Should contain sticky");

    // Visibility
    assertTrue(allClasses.contains("visible"), "Should contain visible");
    assertTrue(allClasses.contains("invisible"), "Should contain invisible");

    // Overflow
    assertTrue(allClasses.contains("overflow-auto"), "Should contain overflow-auto");
    assertTrue(allClasses.contains("overflow-hidden"), "Should contain overflow-hidden");
    assertTrue(allClasses.contains("overflow-x-scroll"), "Should contain overflow-x-scroll");
  }

  @Test
  void testAllUtilityClassesContainsInteractivityUtilities() {
    Set<String> allClasses = TwCatalog.allUtilityClasses();

    // Cursor
    assertTrue(allClasses.contains("cursor-pointer"), "Should contain cursor-pointer");
    assertTrue(allClasses.contains("cursor-default"), "Should contain cursor-default");
    assertTrue(allClasses.contains("cursor-not-allowed"), "Should contain cursor-not-allowed");

    // Pointer events
    assertTrue(allClasses.contains("pointer-events-none"), "Should contain pointer-events-none");
    assertTrue(allClasses.contains("pointer-events-auto"), "Should contain pointer-events-auto");

    // Resize
    assertTrue(allClasses.contains("resize"), "Should contain resize");
    assertTrue(allClasses.contains("resize-none"), "Should contain resize-none");

    // User select
    assertTrue(allClasses.contains("select-none"), "Should contain select-none");
    assertTrue(allClasses.contains("select-text"), "Should contain select-text");

    // Accessibility
    assertTrue(allClasses.contains("sr-only"), "Should contain sr-only");
    assertTrue(allClasses.contains("not-sr-only"), "Should contain not-sr-only");
  }

  @Test
  void testAllUtilityClassesWithCustomConfig() {
    ThemeConfig config = ThemeConfig.defaultConfig();
    Set<String> allClasses = TwCatalog.allUtilityClasses(config);

    // Should still contain basic utilities
    assertFalse(allClasses.isEmpty(), "Catalog with custom config should not be empty");
    assertTrue(allClasses.contains("p-4"), "Should contain p-4 with custom config");
    assertTrue(allClasses.contains("bg-blue-500"), "Should contain bg-blue-500 with custom config");
  }

  @Test
  void testGenerateFullCssProducesOutput() {
    ThemeConfig config = ThemeConfig.defaultConfig();
    String css = TwCatalog.generateFullCss(config);

    assertNotNull(css, "Generated CSS should not be null");
    assertFalse(css.isEmpty(), "Generated CSS should not be empty");

    // Check that CSS contains some expected selectors
    assertTrue(css.contains(".p-4") || css.contains("p-4"), "CSS should contain p-4 utility");
    assertTrue(
        css.contains(".bg-blue-500") || css.contains("bg-blue-500"),
        "CSS should contain bg-blue-500 utility");
  }

  @Test
  void testGenerateFullCssWithDefaultConfig() {
    String css = TwCatalog.generateFullCss();

    assertNotNull(css, "Generated CSS should not be null");
    assertFalse(css.isEmpty(), "Generated CSS should not be empty");
  }

  @Test
  void testCatalogSizeIsReasonable() {
    Set<String> allClasses = TwCatalog.allUtilityClasses();

    // We expect thousands of utilities given the combinatorial nature
    // Colors: ~22 colors * 11 shades * 4 types (bg, text, border, ring) = ~968
    // Plus opacity variants, spacing, typography, etc.
    assertTrue(
        allClasses.size() > 1000,
        "Catalog should have more than 1000 utilities, got: " + allClasses.size());
  }

  @Test
  void testGenerateFullCssFileWithAllUtilities() throws IOException {
    // Generate the complete CSS with all utilities
    ThemeConfig config = ThemeConfig.defaultConfig();
    String css = TwCatalog.generateFullCss(config);

    // Verify CSS is generated
    assertNotNull(css, "Generated CSS should not be null");
    assertFalse(css.isEmpty(), "Generated CSS should not be empty");

    // Write CSS to file in target directory
    Path targetDir = Paths.get("target");
    if (!Files.exists(targetDir)) {
      Files.createDirectories(targetDir);
    }

    Path cssFile = targetDir.resolve("tailwindfx-all-utilities.css");
    Files.writeString(cssFile, css);

    // Verify file was created
    assertTrue(Files.exists(cssFile), "CSS file should be created at " + cssFile.toAbsolutePath());
    assertTrue(Files.size(cssFile) > 0, "CSS file should not be empty");

    // Read back and verify content
    String writtenCss = Files.readString(cssFile);
    assertEquals(css, writtenCss, "Written CSS should match generated CSS");

    // Verify CSS contains expected utilities (without prefix)
    assertTrue(writtenCss.contains(".p-4"), "CSS file should contain .p-4 utility");
    assertTrue(writtenCss.contains(".bg-blue-500"), "CSS file should contain .bg-blue-500 utility");
    assertTrue(writtenCss.contains(".rounded-lg"), "CSS file should contain .rounded-lg utility");
    assertTrue(writtenCss.contains(".text-xl"), "CSS file should contain .text-xl utility");

    System.out.println(
        "Generated CSS file with all utilities: "
            + cssFile.toAbsolutePath()
            + " ("
            + Files.size(cssFile)
            + " bytes)");
  }
}
