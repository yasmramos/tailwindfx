package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.style.StyleToken;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.util.Map;

/**
 * CssPropertyMapper — Mapea prefijos y valores nombrados a propiedades CSS JavaFX.
 *
 * <p>Responsabilidad: Traducir tokens Tailwind a propiedades -fx-* específicas. Ej: "p" →
 * "-fx-padding", "bg" → "-fx-background-color", "text" → "-fx-text-fill"
 *
 * <p>No hace parsing ni resolución de valores, solo mapeo de propiedades.
 */
public final class CssPropertyMapper {

  private final ThemeConfig themeConfig;

  public CssPropertyMapper(ThemeConfig themeConfig) {
    this.themeConfig = themeConfig;
  }

  /**
   * Obtiene la propiedad CSS JavaFX para un prefijo dado.
   *
   * @param prefix Prefijo Tailwind (p, bg, text, w, h, etc.)
   * @return Propiedad -fx-* o null si no hay mapeo
   */
  public String mapToCssProperty(String prefix) {
    return switch (prefix) {
      case "p" -> "-fx-padding";
      case "px" -> "-fx-padding";
      case "py" -> "-fx-padding";
      case "pt", "top" -> "-fx-padding";
      case "pr", "right" -> "-fx-padding";
      case "pb", "bottom" -> "-fx-padding";
      case "pl", "left" -> "-fx-padding";

      case "m" -> null; // Margin handled via code (Styles.java) - CSS property doesn't exist
      case "mx" -> null; // Margin handled via code (Styles.java) - CSS property doesn't exist
      case "my" -> null; // Margin handled via code (Styles.java) - CSS property doesn't exist
      case "mt" -> null; // Margin handled via code (Styles.java) - CSS property doesn't exist
      case "mr" -> null; // Margin handled via code (Styles.java) - CSS property doesn't exist
      case "mb" -> null; // Margin handled via code (Styles.java) - CSS property doesn't exist
      case "ml" -> null; // Margin handled via code (Styles.java) - CSS property doesn't exist

      case "bg" -> "-fx-background-color";
      case "border" -> "-fx-border-color";

      case "text" -> "-fx-text-fill";
      case "font" -> "-fx-font-family";

      case "w" -> "-fx-pref-width";
      case "h" -> "-fx-pref-height";
      case "min-w" -> "-fx-min-width";
      case "min-h" -> "-fx-min-height";
      case "max-w" -> "-fx-max-width";
      case "max-h" -> "-fx-max-height";

      case "opacity" -> "-fx-opacity";
      case "rotate" -> "-fx-rotate";
      case "scale" -> "-fx-scale-x";
      case "scale-x" -> "-fx-scale-x";
      case "scale-y" -> "-fx-scale-y";
      case "translate-x" -> "-fx-translate-x";
      case "translate-y" -> "-fx-translate-y";

      case "rounded" -> "-fx-background-radius";
      case "shadow" -> "-fx-effect";

      case "visible" -> "-fx-visibility";
      case "hidden" -> "-fx-visibility";
      case "invisible" -> "-fx-visibility";
      
      case "gap" -> "-fx-hgap";
      case "gap-x" -> "-fx-hgap";
      case "gap-y" -> "-fx-vgap";
      
      case "overflow" -> null; // Not supported in JavaFX CSS - requires code
      case "cursor" -> null; // Cursor handled via code (node.setCursor) - CSS values incompatible
      case "z" -> null; // Z-index not supported in JavaFX CSS - use node.setViewOrder()
      case "resize" -> null; // Resize not supported in JavaFX CSS - requires code
      
      case "skew-x" -> null; // Skew not supported in JavaFX CSS - requires Transform
      case "skew-y" -> null; // Skew not supported in JavaFX CSS - requires Transform
      
      case "blur" -> null; // Effects not supported in JavaFX CSS - use Effect API
      case "brightness" -> null; // Effects not supported in JavaFX CSS - use Effect API
      case "contrast" -> null; // Effects not supported in JavaFX CSS - use Effect API
      case "grayscale" -> null; // Effects not supported in JavaFX CSS - use Effect API
      case "invert" -> null; // Effects not supported in JavaFX CSS - use Effect API
      case "sepia" -> null; // Effects not supported in JavaFX CSS - use Effect API

      default -> null;
    };
  }

  /**
   * Resuelve valores nombrados como sm, md, lg, bold, solid, dashed, etc.
   *
   * @param prefix Prefijo del token
   * @param namedValue Valor nominal (sm, md, lg, bold, solid, dashed, etc.)
   * @return Valor CSS o null si no hay mapeo o la propiedad no es soportada vía CSS
   */
  public String resolveNamedValue(String prefix, String namedValue) {
    // Properties not supported via CSS - must be handled via code (return null immediately)
    if ("cursor".equals(prefix) || "overflow".equals(prefix) || "resize".equals(prefix)
        || "z".equals(prefix) || "skew-x".equals(prefix) || "skew-y".equals(prefix)
        || "blur".equals(prefix) || "brightness".equals(prefix) || "contrast".equals(prefix)
        || "grayscale".equals(prefix) || "invert".equals(prefix) || "sepia".equals(prefix)) {
      return null; // These require JavaFX API calls, not CSS
    }
    
    // Handle border styles: border-solid, border-dashed, border-dotted, border-none
    if ("border".equals(prefix)) {
      return resolveBorderStyle(namedValue);
    }
    
    // Handle visibility: visible, hidden, invisible
    if ("visible".equals(prefix) || "hidden".equals(prefix) || "invisible".equals(prefix)) {
      return resolveVisibility(prefix);
    }
    
    // Handle width/height special values: w-auto, w-min, w-max, h-auto, h-min, h-max
    if ("w".equals(prefix) || "h".equals(prefix)) {
      return resolveDimension(namedValue);
    }
    
    // Handle max-width named values: max-w-xs, max-w-sm, etc.
    if ("max-w".equals(prefix)) {
      return resolveMaxWidth(namedValue);
    }

    return switch (prefix) {
      case "text" -> resolveFontSize(namedValue);
      case "font" -> resolveFontWeight(namedValue);
      case "rounded" -> resolveBorderRadius(namedValue);
      case "shadow" -> resolveShadow(namedValue);
      default -> null;
    };
  }

  /** Resuelve un estilo de borde a su valor CSS. */
  private String resolveBorderStyle(String style) {
    return switch (style) {
      case "solid" -> "solid";
      case "dashed" -> "dashed";
      case "dotted" -> "dotted";
      case "none" -> "none";
      default -> null;
    };
  }
  
  /** Verifica si un valor es un estilo de borde válido. */
  private boolean isBorderStyle(String value) {
    return "solid".equals(value)
        || "dashed".equals(value)
        || "dotted".equals(value)
        || "none".equals(value);
  }

  private static String prop(String name, String value) {
    return name + ": " + value + ";";
  }

  private static String px(String name, String value) {
    // Si el valor ya tiene 'px', usarlo directamente; si no, agregarlo
    String formatted = value.matches("\\d+$") ? value + "px" : value;
    return name + ": " + formatted + ";";
  }

  private String resolveFontSize(String size) {
    Map<String, Double> fontSizes = themeConfig.fontSize();
    Double value = fontSizes.get(size);
    if (value != null) {
      return value.intValue() + "px";
    }

    // Fallback para tamaños estándar
    return switch (size) {
      case "xs" -> "12px";
      case "sm" -> "14px";
      case "base" -> "16px";
      case "lg" -> "18px";
      case "xl" -> "20px";
      case "2xl" -> "24px";
      case "3xl" -> "30px";
      case "4xl" -> "36px";
      default -> null;
    };
  }

  private String resolveFontWeight(String weight) {
    return switch (weight) {
      case "thin" -> "100";
      case "extralight" -> "200";
      case "light" -> "300";
      case "normal" -> "400";
      case "medium" -> "500";
      case "semibold" -> "600";
      case "bold" -> "700";
      case "extrabold" -> "800";
      case "black" -> "900";
      default -> null;
    };
  }

  private String resolveBorderRadius(String radius) {
    String[] radiusKeys = {"none", "sm", "default", "md", "lg", "xl", "2xl", "3xl", "full"};

    for (int i = 0; i < radiusKeys.length; i++) {
      if (radiusKeys[i].equals(radius)) {
        double value = themeConfig.borderRadius(i);
        return (int) value + "px";
      }
    }

    return switch (radius) {
      case "none" -> "0px";
      case "sm" -> "2px";
      case "md" -> "6px";
      case "lg" -> "8px";
      case "xl" -> "12px";
      case "2xl" -> "16px";
      case "3xl" -> "24px";
      case "full" -> "9999px";
      default -> null;
    };
  }

  private String resolveShadow(String shadow) {
    return switch (shadow) {
      case "sm" -> "0 1px 2px 0 rgba(0, 0, 0, 0.05)";
      case "default", "md" -> "0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1)";
      case "lg" -> "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1)";
      case "xl" -> "0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)";
      case "2xl" -> "0 25px 50px -12px rgba(0, 0, 0, 0.25)";
      case "none" -> "none";
      default -> null;
    };
  }

  /** Resuelve valores especiales de dimensión: auto, min, max. */
  private String resolveDimension(String value) {
    return switch (value) {
      case "auto" -> "USE_PREF_SIZE";
      case "min" -> "USE_PREF_SIZE";
      case "max" -> "-1"; // -1 representa USE_COMPUTED_SIZE en JavaFX
      default -> null;
    };
  }
  
  /** Resuelve valores nombrados de max-width. */
  private String resolveMaxWidth(String value) {
    return switch (value) {
      case "xs" -> "320px";
      case "sm" -> "384px";
      case "md" -> "448px";
      case "lg" -> "512px";
      case "xl" -> "576px";
      case "2xl" -> "672px";
      case "3xl" -> "768px";
      case "full" -> "100%";
      default -> null;
    };
  }

  /**
   * Mapea un token completo a una propiedad CSS con su valor.
   *
   * @param token Token parseado
   * @param resolvedValue Valor ya resuelto (ej: "16px", "rgb(...)")
   * @return Propiedad CSS completa o null
   */
  public String map(StyleToken token, String resolvedValue) {
    if (token == null || resolvedValue == null) {
      return null;
    }

    // Manejo especial para border-* styles (solid, dashed, dotted, none)
    if ("border".equals(token.prefix) && isBorderStyle(token.namedValue)) {
      return prop("-fx-border-style", resolvedValue);
    }
    
    // Manejo especial para w-auto, w-min, w-max, h-auto, h-min, h-max
    if (("w".equals(token.prefix) || "h".equals(token.prefix)) 
        && ("auto".equals(token.namedValue) || "min".equals(token.namedValue) || "max".equals(token.namedValue))) {
      String property = mapToCssProperty(token.prefix);
      return prop(property, resolvedValue);
    }
    
    // Manejo especial para max-w-*
    if ("max-w".equals(token.prefix) && resolvedValue != null) {
      return prop("-fx-max-width", resolvedValue);
    }

    String property = mapToCssProperty(token.prefix);
    if (property == null) {
      return null;
    }

    // Manejo especial para propiedades compuestas
    if ("p".equals(token.prefix) && token.subPrefix != null) {
      return switch (token.subPrefix) {
        case "x" -> px("padding", "0px %s 0px %s".formatted(resolvedValue, resolvedValue));
        case "y" -> px("padding", "%s 0px %s 0px".formatted(resolvedValue, resolvedValue));
        case "t" -> px("padding", "%s 0px 0px 0px".formatted(resolvedValue));
        case "r" -> px("padding", "0px %s 0px 0px".formatted(resolvedValue));
        case "b" -> px("padding", "0px 0px %s 0px".formatted(resolvedValue));
        case "l" -> px("padding", "0px 0px 0px %s".formatted(resolvedValue));
        default -> prop(property, resolvedValue);
      };
    }

    return prop(property, resolvedValue);
  }

  /** Resuelve visibilidad. */
  private String resolveVisibility(String visibility) {
    return switch (visibility) {
      case "visible" -> "visible";
      case "hidden", "invisible" -> "hidden";
      default -> null;
    };
  }
}
