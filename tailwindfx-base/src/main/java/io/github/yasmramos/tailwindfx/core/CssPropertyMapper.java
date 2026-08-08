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

        // Margin properties are NOT mapped to CSS because JavaFX doesn't support -fx-margin.
        // They are handled by Styles.java via HBox.setMargin(), VBox.setMargin(),
        // GridPane.setMargin()
        // when using TwStyle.apply(). Return null to prevent JIT compilation.
      case "m", "mx", "my", "mt", "mr", "mb", "ml" -> null;

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

        // JavaFX spells visibility without the -fx- prefix.
      case "visible", "hidden", "invisible" -> "visibility";

      case "gap" -> "-fx-hgap";
      case "gap-x" -> "-fx-hgap";
      case "gap-y" -> "-fx-vgap";

      case "cursor" -> "-fx-cursor";

        // Utilities without a JavaFX CSS counterpart. Emitting invented -fx-* properties made
        // JavaFX log parse warnings and discard the whole declaration block, so they are left to
        // the stylesheet/Java API path instead (Styles.z, Styles.blur, TwEffect, ...).
      case "z",
          "overflow",
          "resize",
          "blur",
          "brightness",
          "contrast",
          "grayscale",
          "invert",
          "sepia",
          "skew" -> null;

      default -> null;
    };
  }

  /**
   * Resuelve valores nombrados como sm, md, lg, bold, solid, dashed, etc.
   *
   * @param prefix Prefijo del token
   * @param namedValue Valor nominal (sm, md, lg, bold, solid, dashed, etc.)
   * @return Valor CSS o null si no hay mapeo
   */
  public String resolveNamedValue(String prefix, String namedValue) {
    // Handle background colors: bg-white, bg-black, bg-transparent
    if ("bg".equals(prefix)) {
      String color = resolveNamedColor(namedValue);
      if (color != null) {
        return color;
      }
    }

    // Handle text colors: text-white, text-black, text-transparent
    if ("text".equals(prefix)) {
      String color = resolveNamedColor(namedValue);
      if (color != null) {
        return color;
      }
    }

    // Handle border colors: border-white, border-black, border-transparent
    if ("border".equals(prefix)) {
      String color = resolveNamedColor(namedValue);
      if (color != null) {
        return color;
      }
    }

    // Handle border styles: border-solid, border-dashed, border-dotted, border-none
    if ("border".equals(prefix)) {
      return resolveBorderStyle(namedValue);
    }

    // Handle font families: font-sans, font-serif, font-mono
    if ("font".equals(prefix)) {
      String family = resolveFontFamily(namedValue);
      if (family != null) {
        return family;
      }
    }

    // Handle text alignment: text-left, text-center, text-right, text-justify
    if ("text".equals(prefix)) {
      String alignment = resolveTextAlignment(namedValue);
      if (alignment != null) {
        return alignment;
      }
    }

    // Handle cursor styles: cursor-pointer, cursor-default, etc.
    if ("cursor".equals(prefix)) {
      return resolveCursor(namedValue);
    }

    // Handle overflow styles: overflow-hidden, overflow-visible, etc.
    if ("overflow".equals(prefix)) {
      return resolveOverflow(namedValue);
    }

    // Handle visibility: visible, hidden, invisible
    if ("visible".equals(prefix) || "hidden".equals(prefix) || "invisible".equals(prefix)) {
      return resolveVisibility(prefix);
    }

    // Handle resize: resize-none, resize-y, etc.
    if ("resize".equals(prefix)) {
      return resolveResize(namedValue);
    }

    // Handle width/height special values: w-auto, w-min, w-max, h-auto, h-min, h-max
    if ("w".equals(prefix) || "h".equals(prefix)) {
      return resolveDimension(namedValue);
    }

    // Handle max-width named values: max-w-xs, max-w-sm, etc.
    if ("max-w".equals(prefix)) {
      return resolveMaxWidth(namedValue);
    }

    // Handle effects: blur, brightness, contrast, grayscale, invert, sepia, skew
    if ("blur".equals(prefix)
        || "brightness".equals(prefix)
        || "contrast".equals(prefix)
        || "grayscale".equals(prefix)
        || "invert".equals(prefix)
        || "sepia".equals(prefix)
        || "skew-x".equals(prefix)
        || "skew-y".equals(prefix)) {
      return resolveEffectValue(prefix, namedValue);
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

  /** Resuelve colores nombrados como white, black, transparent */
  private String resolveNamedColor(String colorName) {
    return switch (colorName) {
      case "white" -> "#ffffff";
      case "black" -> "#000000";
      case "transparent" -> "transparent";
      default -> null;
    };
  }

  /** Resuelve un cursor a su valor JavaFX (ver javafx.scene.Cursor). */
  private String resolveCursor(String cursor) {
    return switch (cursor) {
      case "default", "context-menu" -> "default";
      case "pointer", "alias" -> "hand";
      case "text", "vertical-text" -> "text";
      case "move", "all-scroll" -> "move";
      case "wait", "help" -> "wait";
      case "crosshair" -> "crosshair";
      case "not-allowed" -> "disappear";
      case "grab" -> "open-hand";
      case "grabbing" -> "closed-hand";
      case "col-resize" -> "h-resize";
      case "row-resize" -> "v-resize";
      case "n-resize" -> "n-resize";
      case "e-resize" -> "e-resize";
      case "s-resize" -> "s-resize";
      case "w-resize" -> "w-resize";
      case "ne-resize", "nesw-resize" -> "ne-resize";
      case "nw-resize", "nwse-resize" -> "nw-resize";
      case "se-resize" -> "se-resize";
      case "sw-resize" -> "sw-resize";
      case "none" -> "none";
      default -> null;
    };
  }

  /** Resuelve overflow a su valor JavaFX. */
  private String resolveOverflow(String overflow) {
    return switch (overflow) {
      case "visible" -> "visible";
      case "hidden" -> "hidden";
      case "scroll" -> "scroll";
      case "auto" -> "auto";
      default -> null;
    };
  }

  /** Resuelve visibilidad. */
  private String resolveVisibility(String visibility) {
    return switch (visibility) {
      case "visible" -> "visible";
      case "hidden", "invisible" -> "hidden";
      default -> null;
    };
  }

  /** Resuelve resize a su valor JavaFX. */
  private String resolveResize(String resize) {
    return switch (resize) {
      case "none" -> "none";
      case "x" -> "horizontal";
      case "y" -> "vertical";
      case "both" -> "both";
      default -> null;
    };
  }

  /** Resuelve valores especiales de dimensión para JavaFX. */
  private String resolveDimension(String value) {
    return switch (value) {
      case "auto" -> "-1"; // JavaFX: -1 means USE_COMPUTED_SIZE (default behavior)
      case "min" -> "-1"; // JavaFX doesn't support min-content, use computed size
      case "max" -> "-1"; // JavaFX doesn't support max-content, use computed size
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

    // border-0, border-2, border-[3px] son anchos de borde, no colores
    if ("border".equals(token.prefix)
        && (token.kind == StyleToken.Kind.SCALE
            || (token.kind == StyleToken.Kind.ARBITRARY && isLength(resolvedValue)))) {
      return prop("-fx-border-width", resolvedValue);
    }

    // text-* puede ser color, tamaño de fuente o alineación
    if ("text".equals(token.prefix)) {
      return prop(textProperty(token, resolvedValue), resolvedValue);
    }

    // font-* puede ser peso o familia tipográfica
    if ("font".equals(token.prefix)) {
      return prop(
          isFontWeight(resolvedValue) ? "-fx-font-weight" : "-fx-font-family", resolvedValue);
    }

    // JavaFX no deriva el radio del borde del fondo: hay que fijar ambos
    if ("rounded".equals(token.prefix)) {
      return prop("-fx-background-radius", resolvedValue)
          + " "
          + prop("-fx-border-radius", resolvedValue);
    }

    // Manejo especial para w-auto, w-min, w-max, h-auto, h-min, h-max
    if (("w".equals(token.prefix) || "h".equals(token.prefix))
        && ("auto".equals(token.namedValue)
            || "min".equals(token.namedValue)
            || "max".equals(token.namedValue))) {
      String property = mapToCssProperty(token.prefix);
      return prop(property, resolvedValue);
    }

    // Manejo especial para max-w-*
    if ("max-w".equals(token.prefix) && resolvedValue != null) {
      return prop("-fx-max-width", resolvedValue);
    }

    // translate-x-4 / scale-y-95: StyleToken separa el eje en subPrefix, hay que recomponerlo
    if (token.subPrefix != null
        && ("translate".equals(token.prefix) || "scale".equals(token.prefix))) {
      String axisProperty = mapToCssProperty(token.prefix + "-" + token.subPrefix);
      if (axisProperty != null) {
        return prop(axisProperty, resolvedValue);
      }
    }

    // scale-95 escala ambos ejes
    if ("scale".equals(token.prefix) && token.subPrefix == null) {
      return prop("-fx-scale-x", resolvedValue) + " " + prop("-fx-scale-y", resolvedValue);
    }

    String property = mapToCssProperty(token.prefix);
    if (property == null) {
      return null;
    }

    // Manejo especial para propiedades compuestas
    if ("p".equals(token.prefix) && token.subPrefix != null) {
      String[] sides = paddingSides(token.subPrefix, withUnit(resolvedValue));
      if (sides == null) {
        return prop(property, resolvedValue);
      }
      return prop("-fx-padding", formatPadding(sides));
    }

    return prop(property, resolvedValue);
  }

  /**
   * Devuelve los cuatro lados (top, right, bottom, left) de un padding direccional, usando {@code
   * null} para los lados que el token no define.
   *
   * @param subPrefix x, y, t, r, b o l
   * @param value valor con unidad ya aplicada
   * @return arreglo de 4 lados o null si el subPrefix no es direccional
   */
  public static String[] paddingSides(String subPrefix, String value) {
    return switch (subPrefix) {
      case "x" -> new String[] {null, value, null, value};
      case "y" -> new String[] {value, null, value, null};
      case "t" -> new String[] {value, null, null, null};
      case "r" -> new String[] {null, value, null, null};
      case "b" -> new String[] {null, null, value, null};
      case "l" -> new String[] {null, null, null, value};
      default -> null;
    };
  }

  /** Formatea los cuatro lados como shorthand de -fx-padding, con 0px para los lados sin valor. */
  public static String formatPadding(String[] sides) {
    StringBuilder sb = new StringBuilder();
    for (String side : sides) {
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(side == null ? "0px" : side);
    }
    return sb.toString();
  }

  /** Elige la propiedad JavaFX para un token text-*: color, tamaño o alineación. */
  private static String textProperty(StyleToken token, String resolvedValue) {
    if (isTextAlignment(resolvedValue)) {
      return "-fx-text-alignment";
    }
    if (token.kind == StyleToken.Kind.COLOR_SHADE || isColor(resolvedValue)) {
      return "-fx-text-fill";
    }
    return isLength(resolvedValue) ? "-fx-font-size" : "-fx-text-fill";
  }

  private static boolean isTextAlignment(String value) {
    return "left".equals(value)
        || "center".equals(value)
        || "right".equals(value)
        || "justify".equals(value);
  }

  private static boolean isColor(String value) {
    return value.startsWith("#")
        || value.startsWith("rgb")
        || value.startsWith("hsl")
        || "transparent".equals(value);
  }

  private static boolean isLength(String value) {
    return value.matches("-?\\d+(\\.\\d+)?(px|pt|em|rem|%)?");
  }

  private static boolean isFontWeight(String value) {
    return value.matches("[1-9]00") || "normal".equals(value) || "bold".equals(value);
  }

  private String resolveFontFamily(String name) {
    return switch (name) {
      case "sans" -> "System";
      case "serif" -> "Serif";
      case "mono" -> "Monospaced";
      default -> null;
    };
  }

  private String resolveTextAlignment(String value) {
    return switch (value) {
      case "left", "center", "right", "justify" -> value;
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

  /** Agrega 'px' a valores numéricos sin unidad. */
  public static String withUnit(String value) {
    return value.matches("-?\\d+(\\.\\d+)?") ? value + "px" : value;
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

  /** Resuelve valores de efectos: blur, brightness, contrast, grayscale, invert, sepia, skew. */
  private String resolveEffectValue(String prefix, String value) {
    if ("blur".equals(prefix)) {
      return resolveBlur(value);
    }
    if ("brightness".equals(prefix)) {
      return resolveBrightness(value);
    }
    if ("contrast".equals(prefix)) {
      return resolveContrast(value);
    }
    if ("grayscale".equals(prefix)) {
      return resolveGrayscale(value);
    }
    if ("invert".equals(prefix)) {
      return resolveInvert(value);
    }
    if ("sepia".equals(prefix)) {
      return resolveSepia(value);
    }
    if ("skew-x".equals(prefix)) {
      return resolveSkewX(value);
    }
    if ("skew-y".equals(prefix)) {
      return resolveSkewY(value);
    }
    return null;
  }

  private String resolveBlur(String value) {
    return switch (value) {
      case "none" -> "0px";
      case "sm" -> "2px";
      case "default" -> "4px";
      case "md" -> "8px";
      case "lg" -> "12px";
      case "xl" -> "16px";
      case "2xl" -> "24px";
      case "3xl" -> "32px";
      default -> {
        // Handle numeric values like blur-10, blur-20
        try {
          int px = Integer.parseInt(value);
          yield px + "px";
        } catch (NumberFormatException e) {
          yield null;
        }
      }
    };
  }

  private String resolveBrightness(String value) {
    return switch (value) {
      case "0" -> "0%";
      case "50" -> "50%";
      case "75" -> "75%";
      case "90" -> "90%";
      case "95" -> "95%";
      case "100" -> "100%";
      case "105" -> "105%";
      case "110" -> "110%";
      case "125" -> "125%";
      case "150" -> "150%";
      case "200" -> "200%";
      default -> value + "%";
    };
  }

  private String resolveContrast(String value) {
    return switch (value) {
      case "0" -> "0%";
      case "50" -> "50%";
      case "75" -> "75%";
      case "100" -> "100%";
      case "125" -> "125%";
      case "150" -> "150%";
      case "200" -> "200%";
      default -> value + "%";
    };
  }

  private String resolveGrayscale(String value) {
    return switch (value) {
      case "0" -> "0%";
      case "100" -> "100%";
      default -> {
        try {
          int pct = Integer.parseInt(value);
          yield pct + "%";
        } catch (NumberFormatException e) {
          yield null;
        }
      }
    };
  }

  private String resolveInvert(String value) {
    return switch (value) {
      case "0" -> "0%";
      case "100" -> "100%";
      default -> {
        try {
          int pct = Integer.parseInt(value);
          yield pct + "%";
        } catch (NumberFormatException e) {
          yield null;
        }
      }
    };
  }

  private String resolveSepia(String value) {
    return switch (value) {
      case "0" -> "0%";
      case "100" -> "100%";
      default -> {
        try {
          int pct = Integer.parseInt(value);
          yield pct + "%";
        } catch (NumberFormatException e) {
          yield null;
        }
      }
    };
  }

  private String resolveSkewX(String value) {
    return switch (value) {
      case "0" -> "0";
      case "1" -> "0.0175";
      case "2" -> "0.0349";
      case "3" -> "0.0524";
      case "6" -> "0.1051";
      case "12" -> "0.2126";
      default -> {
        try {
          double degrees = Double.parseDouble(value);
          yield String.valueOf(Math.tan(Math.toRadians(degrees)));
        } catch (NumberFormatException e) {
          yield null;
        }
      }
    };
  }

  private String resolveSkewY(String value) {
    return switch (value) {
      case "0" -> "0";
      case "1" -> "0.0175";
      case "2" -> "0.0349";
      case "3" -> "0.0524";
      case "6" -> "0.1051";
      case "12" -> "0.2126";
      default -> {
        try {
          double degrees = Double.parseDouble(value);
          yield String.valueOf(Math.tan(Math.toRadians(degrees)));
        } catch (NumberFormatException e) {
          yield null;
        }
      }
    };
  }
}
