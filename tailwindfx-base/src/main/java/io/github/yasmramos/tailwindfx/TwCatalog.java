package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.core.JitCompiler;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.util.Set;
import java.util.TreeSet;

/**
 * TwCatalog — Generates and enumerates all supported TailwindFX utility classes.
 *
 * <p>This class provides methods to generate the complete catalog of utility class names by
 * expanding each utility family with its concrete values from the theme configuration.
 *
 * <p>Usage:
 *
 * <pre>
 *   // Get all utility classes with default theme
 *   Set&lt;String&gt; allClasses = TwCatalog.allUtilityClasses();
 *
 *   // Get all utility classes with custom theme
 *   ThemeConfig config = ThemeConfig.defaultConfig();
 *   Set&lt;String&gt; customizedClasses = TwCatalog.allUtilityClasses(config);
 *
 *   // Generate full CSS at runtime
 *   String fullCss = TwCatalog.generateFullCss(config);
 * </pre>
 *
 * <p>The catalog includes:
 *
 * <ul>
 *   <li>Colors: bg-{color}-{shade}, text-{color}-{shade}, border-{color}-{shade},
 *       ring-{color}-{shade} using 209 colors and shades 50..950, plus -white/-black/-transparent
 *   <li>Spacing: p-{n}, px/py/pt/pr/pb/pl-{n}, m-*, gap-*, space-*, w-{n}, h-{n}, min/max-w/h-{n},
 *       inset/top/right/bottom/left-{n} using ThemeConfig.spacing(), plus special values (full,
 *       auto, screen, fractions)
 *   <li>Typography: text-{xs..9xl} (from ThemeConfig.fontSize()), font-{thin..black}, alignment,
 *       decoration, transform
 *   <li>Borders/radii: rounded-{none..full} (from ThemeConfig.borderRadius), border-{width}
 *   <li>Effects/filters: shadow-*, blur-*, brightness-*, contrast-*, grayscale, invert, sepia,
 *       saturate-*, hue-rotate-*, drop-shadow-*
 *   <li>Transform: scale-*, rotate-*, translate-*, skew-*, opacity-*, z-*
 *   <li>Layout/display: flex, inline-flex, grid, block, hidden, etc.
 * </ul>
 *
 * @author yasmramos
 */
public final class TwCatalog {

  private TwCatalog() {}

  // Shorthand values for spacing
  private static final int[] SPACING_VALUES = {
    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64
  };

  // Special spacing values
  private static final String[] SPECIAL_SPACING = {
    "full", "auto", "screen", "1/2", "1/3", "2/3", "1/4", "2/4", "3/4", "1/5", "2/5", "3/5", "4/5",
    "1/6", "2/6", "3/6", "4/6", "5/6"
  };

  // Font sizes
  private static final String[] FONT_SIZES = {
    "xs", "sm", "base", "lg", "xl", "2xl", "3xl", "4xl", "5xl", "6xl", "7xl", "8xl", "9xl"
  };

  // Font weights
  private static final String[] FONT_WEIGHTS = {
    "thin", "extralight", "light", "normal", "medium", "semibold", "bold", "extrabold", "black"
  };

  // Border radius values
  private static final String[] BORDER_RADIUS = {
    "none", "sm", "md", "lg", "xl", "2xl", "3xl", "full"
  };

  // Shadow values
  private static final String[] SHADOWS = {"sm", "md", "lg", "xl", "2xl", "inner", "none"};

  // Blur values
  private static final String[] BLURS = {"none", "sm", "md", "lg", "xl", "2xl", "3xl"};

  // Opacity values
  private static final String[] OPACITIES = {
    "0", "5", "10", "20", "25", "30", "40", "50", "60", "70", "75", "80", "90", "95", "100"
  };

  // Color families from ColorPalette
  private static final String[] COLOR_FAMILIES = {
    "slate",
    "gray",
    "red",
    "orange",
    "amber",
    "yellow",
    "lime",
    "green",
    "emerald",
    "teal",
    "cyan",
    "sky",
    "blue",
    "indigo",
    "violet",
    "purple",
    "fuchsia",
    "pink",
    "rose",
    "white",
    "black",
    "transparent"
  };

  // Shades
  private static final int[] SHADES = {50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950};

  /**
   * Generates the complete catalog of all supported utility classes using the default theme
   * configuration.
   *
   * @return a sorted set of all utility class names
   */
  public static Set<String> allUtilityClasses() {
    return allUtilityClasses(ThemeConfig.defaultConfig());
  }

  /**
   * Generates the complete catalog of all supported utility classes using a custom theme
   * configuration.
   *
   * @param config the theme configuration to use for generating utility classes
   * @return a sorted set of all utility class names
   */
  public static Set<String> allUtilityClasses(ThemeConfig config) {
    Set<String> utilities = new TreeSet<>();

    // Add color utilities
    addColorUtilities(utilities);

    // Add spacing utilities
    addSpacingUtilities(utilities, config);

    // Add typography utilities
    addTypographyUtilities(utilities, config);

    // Add border and radius utilities
    addBorderUtilities(utilities, config);

    // Add effect and filter utilities
    addEffectUtilities(utilities);

    // Add transform utilities
    addTransformUtilities(utilities);

    // Add layout and display utilities
    addLayoutUtilities(utilities);

    // Add positioning utilities
    addPositioningUtilities(utilities);

    // Add interactivity utilities
    addInteractivityUtilities(utilities);

    return utilities;
  }

  /**
   * Generates the complete CSS for all utility classes at runtime.
   *
   * <p>This method reuses the same compilation pipeline as the Maven plugin
   * (JitCompiler.compileBatch + conversion to CSS per class) to produce the complete CSS directly
   * at runtime, without depending on the Maven plugin.
   *
   * @param config the theme configuration to use
   * @return the complete CSS string containing all utility class definitions
   */
  public static String generateFullCss(ThemeConfig config) {
    Set<String> allClasses = allUtilityClasses(config);
    StringBuilder css = new StringBuilder();

    // Compile each utility class and append to CSS
    JitCompiler compiler = new JitCompiler();
    for (String utilityClass : allClasses) {
      try {
        JitCompiler.BatchResult result = compiler.compileBatch(utilityClass);
        if (result.hasInlineStyle()) {
          String classCss =
              "." + utilityClass + " {\n  " + result.inlineStyle().replace("; ", ";\n  ") + "\n}\n";
          css.append(classCss);
        }
      } catch (Exception e) {
        // Skip utilities that fail to compile
        System.getLogger(TwCatalog.class.getName())
            .log(System.Logger.Level.DEBUG, "Failed to compile utility: " + utilityClass, e);
      }
    }

    return css.toString();
  }

  /** Overload that uses default theme configuration. */
  public static String generateFullCss() {
    return generateFullCss(ThemeConfig.defaultConfig());
  }

  // ============================================================================
  // Utility generation methods
  // ============================================================================

  private static void addColorUtilities(Set<String> utilities) {
    // Background colors: bg-{color}-{shade}
    for (String color : COLOR_FAMILIES) {
      // Named colors (white, black, transparent) don't use shades
      if ("white".equals(color) || "black".equals(color) || "transparent".equals(color)) {
        utilities.add("bg-" + color);
        utilities.add("text-" + color);
        utilities.add("border-" + color);
        utilities.add("ring-" + color);
      } else {
        for (int shade : SHADES) {
          utilities.add("bg-" + color + "-" + shade);
          utilities.add("text-" + color + "-" + shade);
          utilities.add("border-" + color + "-" + shade);
          utilities.add("ring-" + color + "-" + shade);
        }
      }
    }

    // Background opacity modifiers
    for (String color : COLOR_FAMILIES) {
      if (!"transparent".equals(color)) {
        for (String opacity : OPACITIES) {
          if ("white".equals(color) || "black".equals(color)) {
            utilities.add("bg-" + color + "/" + opacity);
          } else {
            for (int shade : SHADES) {
              utilities.add("bg-" + color + "-" + shade + "/" + opacity);
            }
          }
        }
      }
    }
  }

  private static void addSpacingUtilities(Set<String> utilities, ThemeConfig config) {
    // Padding: p-{n}, px, py, pt, pr, pb, pl
    String[] paddingPrefixes = {"p", "px", "py", "pt", "pr", "pb", "pl"};
    for (String prefix : paddingPrefixes) {
      for (int value : SPACING_VALUES) {
        utilities.add(prefix + "-" + value);
      }
      // Add special values for base p only
      if ("p".equals(prefix)) {
        for (String special : SPECIAL_SPACING) {
          utilities.add(prefix + "-" + special);
        }
      }
    }

    // Margin: m-{n}, mx, my, mt, mr, mb, ml
    String[] marginPrefixes = {"m", "mx", "my", "mt", "mr", "mb", "ml"};
    for (String prefix : marginPrefixes) {
      for (int value : SPACING_VALUES) {
        utilities.add(prefix + "-" + value);
        utilities.add(prefix + "-auto");
      }
      // Negative margins
      for (int value : SPACING_VALUES) {
        if (value > 0) {
          utilities.add("-" + prefix + "-" + value);
        }
      }
    }

    // Gap: gap-{n}, gap-x, gap-y
    utilities.add("gap");
    for (int value : SPACING_VALUES) {
      utilities.add("gap-" + value);
      utilities.add("gap-x-" + value);
      utilities.add("gap-y-" + value);
    }

    // Space: space-x-{n}, space-y-{n}
    for (int value : SPACING_VALUES) {
      utilities.add("space-x-" + value);
      utilities.add("space-y-" + value);
      utilities.add("space-x-reverse");
      utilities.add("space-y-reverse");
    }

    // Width and height: w-{n}, h-{n}
    for (int value : SPACING_VALUES) {
      utilities.add("w-" + value);
      utilities.add("h-" + value);
    }
    for (String special : SPECIAL_SPACING) {
      utilities.add("w-" + special);
      utilities.add("h-" + special);
    }
    utilities.add("w-screen");
    utilities.add("h-screen");
    utilities.add("w-full");
    utilities.add("h-full");
    utilities.add("w-auto");
    utilities.add("h-auto");

    // Min/max width and height
    for (String special : new String[] {"full", "screen", "min", "max", "fit"}) {
      utilities.add("min-w-" + special);
      utilities.add("max-w-" + special);
      utilities.add("min-h-" + special);
      utilities.add("max-h-" + special);
    }
    for (String fraction :
        new String[] {
          "1/2", "1/3", "2/3", "1/4", "2/4", "3/4", "1/5", "2/5", "3/5", "4/5", "1/6", "2/6", "3/6",
          "4/6", "5/6"
        }) {
      utilities.add("min-w-" + fraction);
      utilities.add("max-w-" + fraction);
      utilities.add("min-h-" + fraction);
      utilities.add("max-h-" + fraction);
    }

    // Inset: inset-{n}, top, right, bottom, left
    String[] insetPrefixes = {"inset", "top", "right", "bottom", "left"};
    for (String prefix : insetPrefixes) {
      for (int value : SPACING_VALUES) {
        utilities.add(prefix + "-" + value);
        utilities.add("-" + prefix + "-" + value);
      }
      for (String special :
          new String[] {"full", "auto", "1/2", "1/3", "2/3", "1/4", "2/4", "3/4"}) {
        utilities.add(prefix + "-" + special);
        utilities.add("-" + prefix + "-" + special);
      }
    }
  }

  private static void addTypographyUtilities(Set<String> utilities, ThemeConfig config) {
    // Text sizes: text-{xs..9xl}
    for (String size : FONT_SIZES) {
      utilities.add("text-" + size);
    }

    // Font weights: font-{thin..black}
    for (String weight : FONT_WEIGHTS) {
      utilities.add("font-" + weight);
    }

    // Text alignment
    utilities.add("text-left");
    utilities.add("text-center");
    utilities.add("text-right");
    utilities.add("text-justify");

    // Text decoration
    utilities.add("underline");
    utilities.add("line-through");
    utilities.add("no-underline");

    // Text transform
    utilities.add("uppercase");
    utilities.add("lowercase");
    utilities.add("capitalize");
    utilities.add("normal-case");

    // Font style
    utilities.add("italic");
    utilities.add("not-italic");

    // Font smoothing
    utilities.add("antialiased");
    utilities.add("subpixel-antialiased");

    // Letter spacing
    utilities.add("tracking-tighter");
    utilities.add("tracking-tight");
    utilities.add("tracking-normal");
    utilities.add("tracking-wide");
    utilities.add("tracking-wider");
    utilities.add("tracking-widest");

    // Line height
    utilities.add("leading-none");
    utilities.add("leading-tight");
    utilities.add("leading-snug");
    utilities.add("leading-normal");
    utilities.add("leading-relaxed");
    utilities.add("leading-loose");

    // List style
    utilities.add("list-none");
    utilities.add("list-disc");
    utilities.add("list-decimal");

    // Placeholder
    utilities.add("placeholder-transparent");
    for (String color : new String[] {"white", "black"}) {
      utilities.add("placeholder-" + color);
    }
    for (String color : COLOR_FAMILIES) {
      if (!"white".equals(color) && !"black".equals(color) && !"transparent".equals(color)) {
        for (int shade : SHADES) {
          utilities.add("placeholder-" + color + "-" + shade);
        }
      }
    }
  }

  private static void addBorderUtilities(Set<String> utilities, ThemeConfig config) {
    // Border radius: rounded-{none..full}
    for (String radius : BORDER_RADIUS) {
      utilities.add("rounded-" + radius);
    }
    utilities.add("rounded");
    utilities.add("rounded-t");
    utilities.add("rounded-r");
    utilities.add("rounded-b");
    utilities.add("rounded-l");
    utilities.add("rounded-tl");
    utilities.add("rounded-tr");
    utilities.add("rounded-br");
    utilities.add("rounded-bl");

    // Border widths
    utilities.add("border");
    utilities.add("border-0");
    utilities.add("border-2");
    utilities.add("border-4");
    utilities.add("border-8");
    utilities.add("border-x");
    utilities.add("border-y");
    utilities.add("border-t");
    utilities.add("border-r");
    utilities.add("border-b");
    utilities.add("border-l");

    // Border styles
    utilities.add("border-solid");
    utilities.add("border-dashed");
    utilities.add("border-dotted");
    utilities.add("border-double");
    utilities.add("border-hidden");
    utilities.add("border-none");

    // Divide
    utilities.add("divide-x");
    utilities.add("divide-y");
    utilities.add("divide-x-reverse");
    utilities.add("divide-y-reverse");
    utilities.add("divide-solid");
    utilities.add("divide-dashed");
    utilities.add("divide-dotted");
    utilities.add("divide-double");
    utilities.add("divide-none");
  }

  private static void addEffectUtilities(Set<String> utilities) {
    // Shadows
    for (String shadow : SHADOWS) {
      utilities.add("shadow-" + shadow);
    }
    utilities.add("shadow");

    // Blur
    for (String blur : BLURS) {
      utilities.add("blur-" + blur);
    }

    // Brightness
    utilities.add("brightness-0");
    utilities.add("brightness-50");
    utilities.add("brightness-75");
    utilities.add("brightness-90");
    utilities.add("brightness-95");
    utilities.add("brightness-100");
    utilities.add("brightness-105");
    utilities.add("brightness-110");
    utilities.add("brightness-125");
    utilities.add("brightness-150");
    utilities.add("brightness-200");

    // Contrast
    utilities.add("contrast-0");
    utilities.add("contrast-50");
    utilities.add("contrast-75");
    utilities.add("contrast-100");
    utilities.add("contrast-125");
    utilities.add("contrast-150");
    utilities.add("contrast-200");

    // Grayscale, invert, sepia
    utilities.add("grayscale");
    utilities.add("grayscale-0");
    utilities.add("invert");
    utilities.add("invert-0");
    utilities.add("sepia");
    utilities.add("sepia-0");

    // Saturate
    utilities.add("saturate-0");
    utilities.add("saturate-50");
    utilities.add("saturate-100");
    utilities.add("saturate-150");
    utilities.add("saturate-200");

    // Hue-rotate
    utilities.add("hue-rotate-0");
    utilities.add("hue-rotate-15");
    utilities.add("hue-rotate-30");
    utilities.add("hue-rotate-60");
    utilities.add("hue-rotate-90");
    utilities.add("hue-rotate-180");

    // Drop shadow
    utilities.add("drop-shadow");
    utilities.add("drop-shadow-sm");
    utilities.add("drop-shadow-md");
    utilities.add("drop-shadow-lg");
    utilities.add("drop-shadow-xl");
    utilities.add("drop-shadow-2xl");
    utilities.add("drop-shadow-none");
  }

  private static void addTransformUtilities(Set<String> utilities) {
    // Scale
    utilities.add("scale-0");
    utilities.add("scale-50");
    utilities.add("scale-75");
    utilities.add("scale-90");
    utilities.add("scale-95");
    utilities.add("scale-100");
    utilities.add("scale-105");
    utilities.add("scale-110");
    utilities.add("scale-125");
    utilities.add("scale-150");
    utilities.add("scale-x-0");
    utilities.add("scale-x-50");
    utilities.add("scale-x-75");
    utilities.add("scale-x-90");
    utilities.add("scale-x-95");
    utilities.add("scale-x-100");
    utilities.add("scale-x-105");
    utilities.add("scale-x-110");
    utilities.add("scale-x-125");
    utilities.add("scale-x-150");
    utilities.add("scale-y-0");
    utilities.add("scale-y-50");
    utilities.add("scale-y-75");
    utilities.add("scale-y-90");
    utilities.add("scale-y-95");
    utilities.add("scale-y-100");
    utilities.add("scale-y-105");
    utilities.add("scale-y-110");
    utilities.add("scale-y-125");
    utilities.add("scale-y-150");

    // Rotate
    utilities.add("rotate-0");
    utilities.add("rotate-1");
    utilities.add("rotate-2");
    utilities.add("rotate-3");
    utilities.add("rotate-6");
    utilities.add("rotate-12");
    utilities.add("rotate-45");
    utilities.add("rotate-90");
    utilities.add("rotate-180");
    utilities.add("-rotate-1");
    utilities.add("-rotate-2");
    utilities.add("-rotate-3");
    utilities.add("-rotate-6");
    utilities.add("-rotate-12");
    utilities.add("-rotate-45");
    utilities.add("-rotate-90");
    utilities.add("-rotate-180");

    // Translate
    for (int value :
        new int[] {
          0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56,
          60, 64
        }) {
      utilities.add("translate-x-" + value);
      utilities.add("translate-y-" + value);
      utilities.add("-translate-x-" + value);
      utilities.add("-translate-y-" + value);
    }
    utilities.add("translate-x-full");
    utilities.add("translate-y-full");
    utilities.add("-translate-x-full");
    utilities.add("-translate-y-full");
    utilities.add("translate-x-1/2");
    utilities.add("translate-y-1/2");
    utilities.add("-translate-x-1/2");
    utilities.add("-translate-y-1/2");

    // Skew
    utilities.add("skew-x-0");
    utilities.add("skew-x-1");
    utilities.add("skew-x-2");
    utilities.add("skew-x-3");
    utilities.add("skew-x-6");
    utilities.add("skew-x-12");
    utilities.add("skew-y-0");
    utilities.add("skew-y-1");
    utilities.add("skew-y-2");
    utilities.add("skew-y-3");
    utilities.add("skew-y-6");
    utilities.add("skew-y-12");

    // Transform origin
    utilities.add("origin-center");
    utilities.add("origin-top");
    utilities.add("origin-top-right");
    utilities.add("origin-right");
    utilities.add("origin-bottom-right");
    utilities.add("origin-bottom");
    utilities.add("origin-bottom-left");
    utilities.add("origin-left");
    utilities.add("origin-top-left");

    // Opacity
    for (String opacity : OPACITIES) {
      utilities.add("opacity-" + opacity);
    }

    // Z-index
    utilities.add("z-0");
    utilities.add("z-10");
    utilities.add("z-20");
    utilities.add("z-30");
    utilities.add("z-40");
    utilities.add("z-50");
    utilities.add("z-auto");
  }

  private static void addLayoutUtilities(Set<String> utilities) {
    // Display
    utilities.add("block");
    utilities.add("inline-block");
    utilities.add("inline");
    utilities.add("flex");
    utilities.add("inline-flex");
    utilities.add("grid");
    utilities.add("inline-grid");
    utilities.add("hidden");
    utilities.add("contents");
    utilities.add("flow-root");
    utilities.add("table");
    utilities.add("table-caption");
    utilities.add("table-cell");
    utilities.add("table-column");
    utilities.add("table-column-group");
    utilities.add("table-footer-group");
    utilities.add("table-header-group");
    utilities.add("table-row-group");
    utilities.add("table-row");

    // Flexbox
    utilities.add("flex-row");
    utilities.add("flex-row-reverse");
    utilities.add("flex-col");
    utilities.add("flex-col-reverse");
    utilities.add("flex-wrap");
    utilities.add("flex-wrap-reverse");
    utilities.add("flex-nowrap");
    utilities.add("flex-1");
    utilities.add("flex-auto");
    utilities.add("flex-initial");
    utilities.add("flex-none");
    utilities.add("flex-grow");
    utilities.add("flex-grow-0");
    utilities.add("flex-shrink");
    utilities.add("flex-shrink-0");

    // Grid
    utilities.add("grid-cols-1");
    utilities.add("grid-cols-2");
    utilities.add("grid-cols-3");
    utilities.add("grid-cols-4");
    utilities.add("grid-cols-5");
    utilities.add("grid-cols-6");
    utilities.add("grid-cols-7");
    utilities.add("grid-cols-8");
    utilities.add("grid-cols-9");
    utilities.add("grid-cols-10");
    utilities.add("grid-cols-11");
    utilities.add("grid-cols-12");
    utilities.add("grid-cols-none");
    utilities.add("grid-cols-subgrid");
    utilities.add("grid-rows-1");
    utilities.add("grid-rows-2");
    utilities.add("grid-rows-3");
    utilities.add("grid-rows-4");
    utilities.add("grid-rows-5");
    utilities.add("grid-rows-6");
    utilities.add("grid-rows-none");
    utilities.add("grid-rows-subgrid");

    // Flex/Grid alignment
    utilities.add("justify-start");
    utilities.add("justify-end");
    utilities.add("justify-center");
    utilities.add("justify-between");
    utilities.add("justify-around");
    utilities.add("justify-evenly");
    utilities.add("justify-stretch");
    utilities.add("justify-items-start");
    utilities.add("justify-items-end");
    utilities.add("justify-items-center");
    utilities.add("justify-items-stretch");
    utilities.add("items-start");
    utilities.add("items-end");
    utilities.add("items-center");
    utilities.add("items-baseline");
    utilities.add("items-stretch");
    utilities.add("place-items-start");
    utilities.add("place-items-end");
    utilities.add("place-items-center");
    utilities.add("place-items-stretch");
    utilities.add("self-start");
    utilities.add("self-end");
    utilities.add("self-center");
    utilities.add("self-stretch");
    utilities.add("self-baseline");
    utilities.add("place-content-center");
    utilities.add("place-content-start");
    utilities.add("place-content-end");
    utilities.add("place-content-between");
    utilities.add("place-content-around");
    utilities.add("place-content-evenly");
    utilities.add("place-content-stretch");
    utilities.add("place-self-center");
    utilities.add("place-self-start");
    utilities.add("place-self-end");
    utilities.add("place-self-stretch");

    // Aspect ratio
    utilities.add("aspect-square");
    utilities.add("aspect-video");
    utilities.add("aspect-auto");

    // Object fit
    utilities.add("object-contain");
    utilities.add("object-cover");
    utilities.add("object-fill");
    utilities.add("object-none");
    utilities.add("object-scale-down");
  }

  private static void addPositioningUtilities(Set<String> utilities) {
    // Position
    utilities.add("static");
    utilities.add("fixed");
    utilities.add("absolute");
    utilities.add("relative");
    utilities.add("sticky");

    // Visibility
    utilities.add("visible");
    utilities.add("invisible");
    utilities.add("collapse");

    // Overflow
    utilities.add("overflow-auto");
    utilities.add("overflow-hidden");
    utilities.add("overflow-clip");
    utilities.add("overflow-visible");
    utilities.add("overflow-scroll");
    utilities.add("overflow-x-auto");
    utilities.add("overflow-y-auto");
    utilities.add("overflow-x-hidden");
    utilities.add("overflow-y-hidden");
    utilities.add("overflow-x-visible");
    utilities.add("overflow-y-visible");
    utilities.add("overflow-x-scroll");
    utilities.add("overflow-y-scroll");

    // Overscroll
    utilities.add("overscroll-auto");
    utilities.add("overscroll-contain");
    utilities.add("overscroll-none");
    utilities.add("overscroll-x-auto");
    utilities.add("overscroll-y-auto");
    utilities.add("overscroll-x-contain");
    utilities.add("overscroll-y-contain");
    utilities.add("overscroll-x-none");
    utilities.add("overscroll-y-none");

    // Scroll
    utilities.add("scroll-auto");
    utilities.add("scroll-smooth");
  }

  private static void addInteractivityUtilities(Set<String> utilities) {
    // Cursor
    utilities.add("cursor-auto");
    utilities.add("cursor-default");
    utilities.add("cursor-pointer");
    utilities.add("cursor-wait");
    utilities.add("cursor-text");
    utilities.add("cursor-move");
    utilities.add("cursor-help");
    utilities.add("cursor-not-allowed");
    utilities.add("cursor-none");
    utilities.add("cursor-context-menu");
    utilities.add("cursor-progress");
    utilities.add("cursor-cell");
    utilities.add("cursor-crosshair");
    utilities.add("cursor-vertical-text");
    utilities.add("cursor-alias");
    utilities.add("cursor-copy");
    utilities.add("cursor-no-drop");
    utilities.add("cursor-grab");
    utilities.add("cursor-grabbing");
    utilities.add("cursor-all-scroll");
    utilities.add("cursor-col-resize");
    utilities.add("cursor-row-resize");
    utilities.add("cursor-n-resize");
    utilities.add("cursor-e-resize");
    utilities.add("cursor-s-resize");
    utilities.add("cursor-w-resize");
    utilities.add("cursor-ne-resize");
    utilities.add("cursor-nw-resize");
    utilities.add("cursor-se-resize");
    utilities.add("cursor-sw-resize");
    utilities.add("cursor-ew-resize");
    utilities.add("cursor-ns-resize");
    utilities.add("cursor-nesw-resize");
    utilities.add("cursor-nwse-resize");
    utilities.add("cursor-zoom-in");
    utilities.add("cursor-zoom-out");

    // Pointer events
    utilities.add("pointer-events-none");
    utilities.add("pointer-events-auto");

    // Resize
    utilities.add("resize");
    utilities.add("resize-none");
    utilities.add("resize-x");
    utilities.add("resize-y");

    // User select
    utilities.add("select-none");
    utilities.add("select-text");
    utilities.add("select-all");
    utilities.add("select-auto");

    // Accessibility
    utilities.add("sr-only");
    utilities.add("not-sr-only");
  }
}
