package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.style.StyleToken;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.util.Map;

/**
 * StyleResolver — Resolves Tailwind tokens into style values.
 *
 * <p>Responsibility: Convert StyleTokens into concrete CSS values. Does not handle caching,
 * logging, or metrics. Only pure resolution.
 *
 * <p>Examples: - Token(p-4, scale=4) → "16px" - Token(bg-blue-500) → "rgb(59,130,246)" -
 * Token(w-[320px]) → "320px"
 */
public final class StyleResolver {

  private final ThemeConfig themeConfig;
  private final CssPropertyMapper propertyMapper;

  public StyleResolver(ThemeConfig themeConfig) {
    this.themeConfig = themeConfig;
    this.propertyMapper = new CssPropertyMapper(themeConfig);
  }

  /**
   * Resolves a token into a CSS property value.
   *
   * @return CSS value or null if the token cannot be resolved
   */
  public String resolve(StyleToken token) {
    if (token == null || token.kind == StyleToken.Kind.UNKNOWN) {
      return null;
    }

    return switch (token.kind) {
      case SCALE -> resolveScale(token);
      case COLOR_SHADE -> resolveColor(token);
      case ARBITRARY -> resolveArbitrary(token);
      case NAMED -> resolveNamed(token);
      default -> null;
    };
  }

  private String resolveScale(StyleToken token) {
    double[] spacing = themeConfig.spacing();
    int scale = token.scale;

    if (scale >= 0 && scale < spacing.length) {
      return (int) spacing[scale] + "px";
    }

    // Fallback for scales out of range
    return (scale * 4) + "px";
  }

  private String resolveColor(StyleToken token) {
    Map<String, String[]> colors = themeConfig.colors();
    String[] shades = colors.get(token.colorName);

    if (shades == null || token.shade == null) {
      return null;
    }

    // Mapear shade a índice del array
    int[] shadeValues = {50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950};
    int index = -1;
    for (int i = 0; i < shadeValues.length; i++) {
      if (shadeValues[i] == token.shade) {
        index = i;
        break;
      }
    }

    if (index < 0 || index >= shades.length) {
      return null;
    }

    String colorValue = shades[index];

    // Aplicar opacidad si existe
    if (token.alpha != null) {
      return applyOpacity(colorValue, token.alpha);
    }

    return colorValue;
  }

  private String resolveArbitrary(StyleToken token) {
    return token.arbitraryVal;
  }

  private String resolveNamed(StyleToken token) {
    return propertyMapper.resolveNamedValue(token.prefix, token.namedValue);
  }

  private String applyOpacity(String colorValue, int alpha) {
    // Alpha es 0-100, convertir a 0.0-1.0
    double opacity = alpha / 100.0;

    // Si es rgb(), convertir a rgba()
    if (colorValue.startsWith("rgb(")) {
      String rgbContent = colorValue.substring(4, colorValue.length() - 1);
      return "rgba(" + rgbContent + "," + String.format("%.2f", opacity) + ")";
    }

    // Si es hex, convertir a rgba
    if (colorValue.startsWith("#")) {
      // Simplificado: asumir hex de 6 dígitos
      if (colorValue.length() == 7) {
        int r = Integer.parseInt(colorValue.substring(1, 3), 16);
        int g = Integer.parseInt(colorValue.substring(3, 5), 16);
        int b = Integer.parseInt(colorValue.substring(5, 7), 16);
        return "rgba(" + r + "," + g + "," + b + "," + String.format("%.2f", opacity) + ")";
      }
    }

    return colorValue;
  }
}
