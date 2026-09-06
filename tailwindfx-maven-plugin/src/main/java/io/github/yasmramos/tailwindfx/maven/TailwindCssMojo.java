package io.github.yasmramos.tailwindfx.maven;

import io.github.yasmramos.tailwindfx.core.JitCompiler;
import io.github.yasmramos.tailwindfx.core.ThemeCssGenerator;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Generates optimized TailwindCSS for JavaFX at build time. Scans source files for Tailwind classes
 * and generates a minimal CSS file containing only the used utilities, reducing bundle size.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class TailwindCssMojo extends AbstractMojo {

  /** Directory containing JavaFX source files to scan for Tailwind classes. */
  @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true)
  private File sourceDirectory;

  /** Output directory for generated CSS files. */
  @Parameter(defaultValue = "${project.build.outputDirectory}/css", required = true)
  private File outputDirectory;

  /** Name of the generated CSS file. */
  @Parameter(defaultValue = "tailwindfx-generated.css")
  private String outputFileName;

  /** Whether to include base styles (CSS variables, reset). */
  @Parameter(defaultValue = "true")
  private boolean includeBase;

  /** Whether to include color palette. */
  @Parameter(defaultValue = "true")
  private boolean includeColors;

  /** Whether to minify the generated CSS. */
  @Parameter(defaultValue = "false")
  private boolean minify;

  /** Custom ThemeConfig class name (optional). */
  @Parameter private String themeConfigClass;

  // Regex patterns for matching Tailwind classes in Java source files
  // Only match string literals passed to style application methods
  private static final Pattern METHOD_CALL_PATTERN =
      Pattern.compile(
          "(?:TwStyle|TailwindFX)\\.(?:apply|applyRaw)\\s*\\([^)]*?\"([^\"]*)\"|"
              + "getStyleClass\\(\\)\\.(?:add|addAll)\\s*\\(\\s*\"([^\"]*)\"|"
              + "setStyleClass\\s*\\(\\s*\"([^\"]*)\"");

  // Pattern for FXML styleClass attributes (already correctly bounded)
  private static final Pattern FXML_CLASS_PATTERN = Pattern.compile("styleClass=\"([^\"]*)\"");

  // Pattern to extract individual class names from a string literal
  private static final Pattern CLASS_IN_STRING_PATTERN =
      Pattern.compile(
          "(?<![\\w-])([a-zA-Z0-9]+(?:-[a-zA-Z0-9]+)*(?:\\[[^\\]]+\\])?(?::[a-zA-Z0-9-]+(?:\\[[^\\]]+\\])?)*)");

  @Override
  public void execute() throws MojoExecutionException {
    getLog().info("TailwindFX: Starting CSS generation...");

    // Validate source directory
    if (sourceDirectory == null || !sourceDirectory.exists()) {
      throw new MojoExecutionException(
          "Source directory does not exist: "
              + (sourceDirectory != null ? sourceDirectory.getAbsolutePath() : "null"));
    }

    // Validate and initialize output directory
    if (outputDirectory == null) {
      outputDirectory = new File(sourceDirectory.getParentFile(), "generated/css");
      getLog()
          .warn("Output directory not set, using default: " + outputDirectory.getAbsolutePath());
    }

    // Create output directory
    if (!outputDirectory.exists()) {
      if (!outputDirectory.mkdirs()) {
        throw new MojoExecutionException(
            "Failed to create output directory: " + outputDirectory.getAbsolutePath());
      }
    }

    try {
      // Scan source files for Tailwind classes
      Set<String> usedClasses = scanForTailwindClasses(sourceDirectory);
      getLog().info("TailwindFX: Found " + usedClasses.size() + " unique Tailwind classes");

      if (getLog().isDebugEnabled()) {
        getLog().debug("Found classes: " + String.join(", ", usedClasses));
      }

      // Generate CSS
      String generatedCss = generateCss(usedClasses);

      // Write CSS file
      if (outputFileName == null || outputFileName.trim().isEmpty()) {
        outputFileName = "tailwindfx-generated.css";
      }
      Path outputPath = outputDirectory.toPath().resolve(outputFileName);
      try (FileWriter writer = new FileWriter(outputPath.toFile())) {
        writer.write(generatedCss);
      }

      getLog().info("TailwindFX: CSS generated successfully at " + outputPath.toAbsolutePath());
      getLog()
          .info(
              "TailwindFX: Generated CSS size: "
                  + String.format("%.2f KB", generatedCss.length() / 1024.0));

    } catch (IOException e) {
      throw new MojoExecutionException("Failed to generate CSS", e);
    }
  }

  /** Scans Java and FXML files for Tailwind class usage. */
  private Set<String> scanForTailwindClasses(File directory) throws IOException {
    Set<String> tailwindClasses = new HashSet<>();

    getLog().debug("Scanning directory: " + directory.getAbsolutePath());

    // Find all .java and .fxml files
    List<Path> files =
        Files.walk(directory.toPath())
            .filter(path -> path.toString().endsWith(".java") || path.toString().endsWith(".fxml"))
            .collect(Collectors.toList());

    getLog().debug("Found " + files.size() + " source files to scan");

    for (Path file : files) {
      try {
        String content = Files.readString(file, StandardCharsets.UTF_8);

        if (file.toString().endsWith(".fxml")) {
          // Extract classes from FXML styleClass attributes
          Matcher fxmlMatcher = FXML_CLASS_PATTERN.matcher(content);
          while (fxmlMatcher.find()) {
            String classes = fxmlMatcher.group(1);
            for (String cls : classes.split("\\s+")) {
              if (!cls.isEmpty() && isValidTailwindClass(cls)) {
                tailwindClasses.add(cls);
              }
            }
          }
        } else if (file.toString().endsWith(".java")) {
          // Extract classes only from string literals in style application methods
          // This avoids false positives from Java identifiers like class names, method names, etc.

          // Match method calls: TwStyle.apply(...), TailwindFX.apply(...), TwStyle.applyRaw(...)
          Matcher methodMatcher = METHOD_CALL_PATTERN.matcher(content);
          while (methodMatcher.find()) {
            // Get the first non-null group (the string literal content)
            String stringLiteral = null;
            for (int i = 1; i <= methodMatcher.groupCount(); i++) {
              if (methodMatcher.group(i) != null) {
                stringLiteral = methodMatcher.group(i);
                break;
              }
            }

            if (stringLiteral != null) {
              // Extract individual class names from the string literal
              Matcher classMatcher = CLASS_IN_STRING_PATTERN.matcher(stringLiteral);
              while (classMatcher.find()) {
                String potentialClass = classMatcher.group(1);
                if (isValidTailwindClass(potentialClass)) {
                  tailwindClasses.add(potentialClass);
                }
              }
            }
          }
        }

      } catch (IOException e) {
        getLog().warn("Failed to read file: " + file, e);
      }
    }

    return tailwindClasses;
  }

  /**
   * Validates if a string is a valid Tailwind class by attempting to compile it with JitCompiler.
   * This ensures we only accept classes that actually produce CSS output, eliminating false
   * positives from Java identifiers.
   */
  private boolean isValidTailwindClass(String className) {
    if (className == null || className.isEmpty()) {
      return false;
    }

    // Skip common false positives - Java identifiers and keywords
    if (className.equals("class")
        || className.equals("style")
        || className.equals("styleClass")
        || className.equals("id")
        || className.equals("String")
        || className.equals("Stage")
        || className.equals("Integer")
        || className.equals("Double")
        || className.equals("Boolean")
        || className.equals("Object")
        || className.equals("Void")
        || className.equals("var")
        || className.equals("null")
        || className.equals("true")
        || className.equals("false")) {
      return false;
    }

    // Skip PascalCase identifiers (Java class names)
    if (Character.isUpperCase(className.charAt(0))) {
      return false;
    }

    // Skip purely numeric values
    if (className.matches("\\d+")) {
      return false;
    }

    // Skip method names (typically camelCase starting with lowercase verb)
    if (className.matches("[a-z][a-zA-Z0-9]*")
        && !className.contains("-")
        && className.length() > 2) {
      // Check if it looks like a method name (common patterns)
      if (className.startsWith("get")
          || className.startsWith("set")
          || className.startsWith("is")
          || className.startsWith("has")
          || className.startsWith("add")
          || className.startsWith("remove")
          || className.endsWith("ing") // e.g., "printing", "running"
          || className.endsWith("ed")) { // e.g., "created", "loaded"
        return false;
      }
    }

    // Try to compile the class - if it produces no output, it's not a valid Tailwind class
    try {
      JitCompiler compiler = new JitCompiler();
      JitCompiler.BatchResult result = compiler.compileBatch(className);

      // Valid if it produces either inline style OR cssClasses (for variants like hover:, flex,
      // etc.)
      // We check both because some utilities produce CSS classes instead of inline styles
      boolean hasValidOutput = result.hasInlineStyle() || !result.cssClasses().isEmpty();

      if (!hasValidOutput && getLog().isDebugEnabled()) {
        getLog().debug("Filtered out non-Tailwind class: " + className);
      }

      return hasValidOutput;
    } catch (Exception e) {
      // If compilation fails, it's not a valid Tailwind class
      if (getLog().isDebugEnabled()) {
        getLog().debug("Failed to compile potential class '" + className + "': " + e.getMessage());
      }
      return false;
    }
  }

  /** Generates CSS content based on used classes. */
  private String generateCss(Set<String> usedClasses) {
    StringBuilder css = new StringBuilder();

    // Add base styles if requested
    if (includeBase) {
      getLog().info("TailwindFX: Including base styles");
      ThemeConfig config =
          themeConfigClass != null
              ? loadCustomThemeConfig(themeConfigClass)
              : ThemeConfig.defaultConfig();

      ThemeCssGenerator generator = new ThemeCssGenerator(config);
      String baseCss = generator.generateBaseCss();
      css.append(baseCss).append("\n\n");
    }

    // Add color palette if requested
    if (includeColors) {
      getLog().info("TailwindFX: Including color palette");
      ThemeConfig config =
          themeConfigClass != null
              ? loadCustomThemeConfig(themeConfigClass)
              : ThemeConfig.defaultConfig();

      ThemeCssGenerator generator = new ThemeCssGenerator(config);
      String colorCss = generator.generateBaseCss(); // Reuse base CSS which includes colors
      css.append(colorCss).append("\n\n");
    }

    // Generate utilities for used classes
    getLog().info("TailwindFX: Generating utilities for " + usedClasses.size() + " classes");

    JitCompiler compiler = new JitCompiler();

    for (String className : usedClasses) {
      try {
        // Use compileBatch to capture output from specialized processors (RingProcessor, etc.)
        JitCompiler.BatchResult result = compiler.compileBatch(className);
        if (result.hasInlineStyle()) {
          // Convert inline style to class-based CSS
          String classCss = convertToClassCss(className, result.inlineStyle());
          css.append(classCss).append("\n");
        }
      } catch (Exception e) {
        getLog().warn("Failed to compile class: " + className, e);
      }
    }

    if (minify) {
      getLog().info("TailwindFX: Minifying CSS");
      return minifyCss(css.toString());
    }

    return css.toString();
  }

  /**
   * Converts inline style format to class-based CSS. Translates Tailwind variants to valid JavaFX
   * selectors: - hover:X → .X:hover - focus:X → .X:focused - pressed:X / active:X → .X:pressed -
   * disabled:X → .X:disabled - md:, lg:, etc. → .bp-md .X, .bp-lg .X (based on BreakpointManager
   * classes) - dark: → .dark .X (based on ThemeManager class) Filters !important as JavaFX doesn't
   * support it.
   */
  private String convertToClassCss(String className, String inlineStyle) {
    StringBuilder classCss = new StringBuilder();

    // Parse variants from className
    VariantInfo variantInfo = parseVariants(className);
    String baseClassName = variantInfo.baseClass;
    String selector = buildSelector(baseClassName, variantInfo);

    classCss.append(selector).append(" {\\n");

    // Parse inline style and convert to class format
    String[] properties = inlineStyle.split(";");
    for (String prop : properties) {
      String trimmed = prop.trim();
      if (!trimmed.isEmpty()) {
        // Filter !important as JavaFX doesn't support it
        if (trimmed.contains("!important")) {
          getLog()
              .warn(
                  "TailwindFX: !important is not supported in JavaFX CSS. Property filtered: "
                      + trimmed);
          continue;
        }
        classCss.append("    ").append(trimmed).append(";\\n");
      }
    }

    classCss.append("}");
    return classCss.toString();
  }

  /** Holds information about parsed variants from a Tailwind class name. */
  private static class VariantInfo {
    String baseClass;
    List<String> stateVariants = new ArrayList<>(); // hover, focus, pressed, active, disabled
    List<String> breakpointVariants = new ArrayList<>(); // sm, md, lg, xl, 2xl
    boolean isDark = false;

    VariantInfo(String baseClass) {
      this.baseClass = baseClass;
    }
  }

  /**
   * Parses Tailwind variants from a class name. Examples: - "hover:bg-blue-500" →
   * baseClass="bg-blue-500", stateVariants=["hover"] - "md:p-4" → baseClass="p-4",
   * breakpointVariants=["md"] - "dark:text-white" → baseClass="text-white", isDark=true -
   * "lg:hover:w-full" → baseClass="w-full", breakpointVariants=["lg"], stateVariants=["hover"]
   */
  private VariantInfo parseVariants(String className) {
    VariantInfo info = new VariantInfo(className);
    String remaining = className;

    // Process variants separated by colons
    while (remaining.contains(":")) {
      int colonIndex = remaining.indexOf(':');
      String variant = remaining.substring(0, colonIndex);
      remaining = remaining.substring(colonIndex + 1);

      // Check for arbitrary variant syntax [&:hover], [@media...]
      if (variant.startsWith("[") && variant.endsWith("]")) {
        // Extract content from brackets
        String content = variant.substring(1, variant.length() - 1);
        if (content.startsWith("&:")) {
          // [&:hover] → hover
          variant = content.substring(2);
        } else if (content.startsWith("@media")) {
          // Handle media queries - map to breakpoints
          if (content.contains("min-width:640px")) {
            info.breakpointVariants.add("sm");
          } else if (content.contains("min-width:768px")) {
            info.breakpointVariants.add("md");
          } else if (content.contains("min-width:1024px")) {
            info.breakpointVariants.add("lg");
          } else if (content.contains("min-width:1280px")) {
            info.breakpointVariants.add("xl");
          } else if (content.contains("min-width:1536px")) {
            info.breakpointVariants.add("2xl");
          }
          variant = null; // Already processed
        }
      }

      if (variant == null) continue;

      // Categorize variant
      switch (variant) {
        case "hover":
        case "focus":
        case "pressed":
        case "active":
        case "disabled":
        case "visited":
        case "checked":
          info.stateVariants.add(variant);
          break;
        case "sm":
        case "md":
        case "lg":
        case "xl":
        case "2xl":
          info.breakpointVariants.add(variant);
          break;
        case "dark":
          info.isDark = true;
          break;
        default:
          // Unknown variant, keep as part of base class
          break;
      }
    }

    info.baseClass = remaining;
    return info;
  }

  /**
   * Builds a CSS selector from base class name and variants. Examples: - "bg-blue-500" with hover →
   * ".bg-blue-500:hover" - "p-4" with md → ".bp-md .p-4" - "text-white" with dark → ".dark
   * .text-white" - "w-full" with lg, hover → ".bp-lg .w-full:hover"
   */
  private String buildSelector(String baseClass, VariantInfo info) {
    StringBuilder selector = new StringBuilder();

    // Escape special characters in class name for CSS
    String escapedClass = escapeCssClassName(baseClass);

    // Start with breakpoint context (outer wrapper)
    for (String bp : info.breakpointVariants) {
      selector.append(".bp-").append(bp).append(" ");
    }

    // Add dark mode context
    if (info.isDark) {
      selector.append(".dark ");
    }

    // Base class selector
    selector.append(".").append(escapedClass);

    // Add state pseudo-classes (JavaFX uses :hover, :focused, :pressed, :disabled)
    for (String state : info.stateVariants) {
      switch (state) {
        case "hover":
          selector.append(":hover");
          break;
        case "focus":
          selector.append(":focused");
          break;
        case "pressed":
        case "active":
          selector.append(":pressed");
          break;
        case "disabled":
          selector.append(":disabled");
          break;
        case "visited":
        case "checked":
          // JavaFX doesn't have direct equivalents, skip or use custom handling
          break;
      }
    }

    return selector.toString();
  }

  /** Escapes special CSS characters in class names. */
  private String escapeCssClassName(String className) {
    return className
        .replace("\\", "\\\\")
        .replace("[", "\\[")
        .replace("]", "\\]")
        .replace(":", "\\:")
        .replace(".", "\\.");
  }

  /** Basic CSS minification. */
  private String minifyCss(String css) {
    return css.replaceAll("/\\*.*?\\*/", "") // Remove comments
        .replaceAll("\\s+", " ") // Collapse whitespace
        .replaceAll("\\s*\\{\\s*", "{") // Remove spaces around braces
        .replaceAll("\\s*\\}\\s*", "}")
        .replaceAll("\\s*;\\s*", ";")
        .trim();
  }

  /** Loads a custom ThemeConfig class if specified. */
  private ThemeConfig loadCustomThemeConfig(String className) {
    try {
      Class<?> clazz = Class.forName(className);
      // Try to get instance via static method or constructor
      if (clazz.getMethod("defaultConfig") != null) {
        return (ThemeConfig) clazz.getMethod("defaultConfig").invoke(null);
      }
      getLog().warn("Custom ThemeConfig not yet fully supported, using default");
      return ThemeConfig.defaultConfig();
    } catch (Exception e) {
      getLog().warn("Failed to load custom ThemeConfig class: " + className + ", using default", e);
      return ThemeConfig.defaultConfig();
    }
  }
}
