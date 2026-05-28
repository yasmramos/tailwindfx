package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.util.Map;

/**
 * Generates the base CSS variables dynamically from ThemeConfig. Replaces the static
 * tailwindfx-base.css file.
 */
public class ThemeCssGenerator {

  private final ThemeConfig themeConfig;

  public ThemeCssGenerator(ThemeConfig themeConfig) {
    this.themeConfig = themeConfig;
  }

  /** Generates the full CSS content for base variables. */
  public String generateBaseCss() {
    StringBuilder css = new StringBuilder();
    css.append(".root {\n");

    // Generate Color Variables
    generateColorVariables(css);

    // Generate Spacing Variables
    generateSpacingVariables(css);

    // Generate Font Size Variables
    generateFontSizeVariables(css);

    // Generate Border Radius Variables
    generateBorderRadiusVariables(css);

    // Generate Opacity Variables
    generateOpacityVariables(css);

    // Generate Box Shadow Variables (Basic implementation)
    generateShadowVariables(css);

    css.append("}\n");
    
    // Validate CSS structure
    String result = css.toString();
    if (!result.trim().endsWith("}")) {
      throw new IllegalStateException("Generated CSS does not end with closing brace");
    }
    
    long openBraces = result.chars().filter(ch -> ch == '{').count();
    long closeBraces = result.chars().filter(ch -> ch == '}').count();
    if (openBraces != closeBraces) {
      throw new IllegalStateException(
          "CSS brace mismatch: " + openBraces + " opening braces, " + closeBraces + " closing braces");
    }
    
    return result;
  }

  private void generateColorVariables(StringBuilder css) {
    Map<String, String[]> colors = themeConfig.colors();
    for (Map.Entry<String, String[]> entry : colors.entrySet()) {
      String colorName = entry.getKey();
      String[] shades = entry.getValue();

      // Map standard Tailwind shades to array indices
      int[] shadeValues = {50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950};

      for (int i = 0; i < shadeValues.length && i < shades.length; i++) {
        int shade = shadeValues[i];
        String varName = "--color-" + colorName + "-" + shade;
        css.append("    ").append(varName).append(": ").append(shades[i]).append(";\n");
      }
    }
  }

  private void generateSpacingVariables(StringBuilder css) {
    double[] spacing = themeConfig.spacing();
    for (int i = 0; i < spacing.length; i++) {
      String varName = "--spacing-" + i;
      css.append("    ").append(varName).append(": ").append((int) spacing[i]).append("px;\n");
    }
  }

  private void generateFontSizeVariables(StringBuilder css) {
    Map<String, Double> fontSizes = themeConfig.fontSize();
    for (Map.Entry<String, Double> entry : fontSizes.entrySet()) {
      String key = entry.getKey();
      Double value = entry.getValue();
      String varName = "--font-size-" + key;
      css.append("    ").append(varName).append(": ").append(value.intValue()).append("px;\n");
    }
  }

  private void generateBorderRadiusVariables(StringBuilder css) {
    // ThemeConfig doesn't expose borderRadius array directly, use indexed access
    String[] radiusKeys = {"none", "sm", "default", "md", "lg", "xl", "2xl", "3xl", "full"};

    for (int i = 0; i < radiusKeys.length; i++) {
      double value = themeConfig.borderRadius(i);
      String varName = "--radius-" + radiusKeys[i];
      css.append("    ").append(varName).append(": ").append((int) value).append("px;\n");
    }
  }

  private void generateOpacityVariables(StringBuilder css) {
    Map<String, Double> opacity = themeConfig.opacity();
    for (Map.Entry<String, Double> entry : opacity.entrySet()) {
      String key = entry.getKey();
      Double value = entry.getValue();
      String varName = "--opacity-" + key;
      css.append("    ").append(varName).append(": ").append(value).append(";\n");
    }
  }

  private void generateShadowVariables(StringBuilder css) {
    // Basic shadow mapping - can be expanded based on ThemeConfig if it supports custom shadows
    String[] shadows = {"sm", "default", "md", "lg", "xl", "2xl", "none"};
    String[] values = {
      "0 1px 2px 0 rgba(0, 0, 0, 0.05)",
      "0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1)",
      "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1)",
      "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1)",
      "0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)",
      "0 25px 50px -12px rgba(0, 0, 0, 0.25)",
      "none"
    };

    for (int i = 0; i < shadows.length; i++) {
      String varName = "--shadow-" + shadows[i];
      css.append("    ").append(varName).append(": ").append(values[i]).append(";\n");
    }
  }
}
