package io.github.yasmramos.tailwindfx.maven;

import io.github.yasmramos.tailwindfx.core.JitCompiler;
import io.github.yasmramos.tailwindfx.core.ThemeCssGenerator;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Generates optimized TailwindCSS for JavaFX at build time.
 * Scans source files for Tailwind classes and generates a minimal CSS file
 * containing only the used utilities, reducing bundle size.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class TailwindCssMojo extends AbstractMojo {

    /**
     * Directory containing JavaFX source files to scan for Tailwind classes.
     */
    @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true)
    private File sourceDirectory;

    /**
     * Output directory for generated CSS files.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}/css", required = true)
    private File outputDirectory;

    /**
     * Name of the generated CSS file.
     */
    @Parameter(defaultValue = "tailwindfx-generated.css")
    private String outputFileName;

    /**
     * Whether to include base styles (CSS variables, reset).
     */
    @Parameter(defaultValue = "true")
    private boolean includeBase;

    /**
     * Whether to include color palette.
     */
    @Parameter(defaultValue = "true")
    private boolean includeColors;

    /**
     * Whether to minify the generated CSS.
     */
    @Parameter(defaultValue = "false")
    private boolean minify;

    /**
     * Custom ThemeConfig class name (optional).
     */
    @Parameter
    private String themeConfigClass;

    // Regex patterns for matching Tailwind classes
    private static final Pattern CLASS_PATTERN = Pattern.compile(
        "(?<![\\w-])([a-zA-Z0-9]+(?:-[a-zA-Z0-9]+)*(?:\\[[^\\]]+\\])?(?::[a-zA-Z0-9-]+(?:\\[[^\\]]+\\])?)*)"
    );
    
    private static final Pattern FXML_CLASS_PATTERN = Pattern.compile(
        "styleClass=\"([^\"]*)\""
    );

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("TailwindFX: Starting CSS generation...");

        // Validate source directory
        if (sourceDirectory == null || !sourceDirectory.exists()) {
            throw new MojoExecutionException(
                "Source directory does not exist: " + (sourceDirectory != null ? sourceDirectory.getAbsolutePath() : "null")
            );
        }

        // Validate and initialize output directory
        if (outputDirectory == null) {
            outputDirectory = new File(sourceDirectory.getParentFile(), "generated/css");
            getLog().warn("Output directory not set, using default: " + outputDirectory.getAbsolutePath());
        }
        
        // Create output directory
        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {
                throw new MojoExecutionException(
                    "Failed to create output directory: " + outputDirectory.getAbsolutePath()
                );
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
            getLog().info("TailwindFX: Generated CSS size: " + 
                String.format("%.2f KB", generatedCss.length() / 1024.0));

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate CSS", e);
        }
    }

    /**
     * Scans Java and FXML files for Tailwind class usage.
     */
    private Set<String> scanForTailwindClasses(File directory) throws IOException {
        Set<String> tailwindClasses = new HashSet<>();
        
        getLog().debug("Scanning directory: " + directory.getAbsolutePath());
        
        // Find all .java and .fxml files
        List<Path> files = Files.walk(directory.toPath())
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
                }
                
                // Extract classes from Java strings (setStyleClass, addStyleClass, etc.)
                Matcher matcher = CLASS_PATTERN.matcher(content);
                while (matcher.find()) {
                    String potentialClass = matcher.group(1);
                    if (isValidTailwindClass(potentialClass)) {
                        tailwindClasses.add(potentialClass);
                    }
                }
                
            } catch (IOException e) {
                getLog().warn("Failed to read file: " + file, e);
            }
        }
        
        return tailwindClasses;
    }

    /**
     * Validates if a string looks like a valid Tailwind class.
     */
    private boolean isValidTailwindClass(String className) {
        if (className == null || className.isEmpty()) {
            return false;
        }
        
        // Skip common false positives
        if (className.equals("class") || className.equals("style") || 
            className.equals("styleClass") || className.equals("id")) {
            return false;
        }
        
        // Must start with a letter or number
        if (!Character.isLetterOrDigit(className.charAt(0))) {
            return false;
        }
        
        // Should contain at least one hyphen for most Tailwind classes
        // But allow simple ones like 'bold', 'italic' if they exist
        return className.length() > 1;
    }

    /**
     * Generates CSS content based on used classes.
     */
    private String generateCss(Set<String> usedClasses) {
        StringBuilder css = new StringBuilder();

        // Add base styles if requested
        if (includeBase) {
            getLog().info("TailwindFX: Including base styles");
            ThemeConfig config = themeConfigClass != null 
                ? loadCustomThemeConfig(themeConfigClass)
                : ThemeConfig.defaultConfig();
            
            ThemeCssGenerator generator = new ThemeCssGenerator(config);
            String baseCss = generator.generateBaseCss();
            css.append(baseCss).append("\n\n");
        }

        // Add color palette if requested
        if (includeColors) {
            getLog().info("TailwindFX: Including color palette");
            ThemeConfig config = themeConfigClass != null 
                ? loadCustomThemeConfig(themeConfigClass)
                : ThemeConfig.defaultConfig();
            
            ThemeCssGenerator generator = new ThemeCssGenerator(config);
            String colorCss = generator.generateBaseCss(); // Reuse base CSS which includes colors
            css.append(colorCss).append("\n\n");
        }

        // Generate utilities for used classes
        getLog().info("TailwindFX: Generating utilities for " + usedClasses.size() + " classes");
        
        for (String className : usedClasses) {
            try {
                JitCompiler.CompileResult result = JitCompiler.compile(className);
                if (result != null && result.inlineStyle() != null && !result.inlineStyle().isEmpty()) {
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
     * Converts inline style format to class-based CSS.
     */
    private String convertToClassCss(String className, String inlineStyle) {
        // Remove -fx- prefix and convert to standard CSS properties
        StringBuilder classCss = new StringBuilder();
        classCss.append(".").append(className.replace("[", "\\[").replace("]", "\\]").replace(":", "\\:")).append(" {\n");
        
        // Parse inline style and convert to class format
        String[] properties = inlineStyle.split(";");
        for (String prop : properties) {
            if (!prop.trim().isEmpty()) {
                classCss.append("    ").append(prop.trim()).append(";\n");
            }
        }
        
        classCss.append("}");
        return classCss.toString();
    }

    /**
     * Basic CSS minification.
     */
    private String minifyCss(String css) {
        return css
            .replaceAll("/\\*.*?\\*/", "") // Remove comments
            .replaceAll("\\s+", " ")        // Collapse whitespace
            .replaceAll("\\s*\\{\\s*", "{") // Remove spaces around braces
            .replaceAll("\\s*\\}\\s*", "}")
            .replaceAll("\\s*;\\s*", ";")
            .trim();
    }

    /**
     * Loads a custom ThemeConfig class if specified.
     */
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
