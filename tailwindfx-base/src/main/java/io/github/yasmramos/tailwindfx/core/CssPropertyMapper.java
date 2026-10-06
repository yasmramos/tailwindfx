package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.style.StyleToken;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.util.Map;

/**
 * CssPropertyMapper — Maps prefixes and named values to JavaFX CSS properties.
 *
 * <p>Responsibility: Translate Tailwind tokens to specific -fx-* properties. Example: "p" →
 * "-fx-padding", "bg" → "-fx-background-color", "text" → "-fx-text-fill"
 *
 * <p>Does not parse or resolve values, only property mapping.
 */
public final class CssPropertyMapper {

  private final ThemeConfig themeConfig;

  public CssPropertyMapper(ThemeConfig themeConfig) {
    this.themeConfig = themeConfig;
  }

  /**
   * Gets the JavaFX CSS property for a given prefix.
   *
   * @param prefix Tailwind prefix (p, bg, text, w, h, etc.)
   * @return -fx-* property or null if no mapping
   */
  public String mapToCssProperty(String prefix) {
    return switch (prefix) {
      case "p" -> "-fx-padding";
      case "px" -> "-fx-padding";
      case "py" -> "-fx-padding";
        // Note: top/right/bottom/left are NOT mapped to CSS because JavaFX doesn't support
        // -fx-inset or similar properties. Insets must be applied programmatically via
        // Node.setTranslateX/Y() or layout panes. Return null to prevent invalid CSS generation.
      case "pt", "top", "pr", "right", "pb", "bottom", "pl", "left" -> null;

        // Margin properties are NOT mapped to CSS because JavaFX doesn't support -fx-margin.
        // They are handled by Styles.java via HBox.setMargin(), VBox.setMargin(),
        // GridPane.setMargin()
        // when using TwStyle.apply(). Return null to prevent JIT compilation.
      case "m", "mx", "my", "mt", "mr", "mb", "ml" -> null;

      case "bg" -> "-fx-background-color";
        // Border width vs border color: distinguish by token type in CssPropertyMapper.map()
        // For border widths (border, border-0, border-2, etc.), map to -fx-border-width
        // For border colors (border-blue-500), map to -fx-border-color
      case "border" -> "-fx-border-color"; // Default to color; width handled specially in map()

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

        // Visibility is NOT mapped to CSS because JavaFX doesn't support -fx-visibility.
        // It is controlled via Node.setVisible() and Node.setManaged().
        // Return null to prevent JIT compilation of invalid CSS.
      case "visible", "hidden", "invisible" -> null;

      case "gap" -> "-fx-hgap";
      case "gap-x" -> "-fx-hgap";
      case "gap-y" -> "-fx-vgap";

        // Overflow is NOT mapped to CSS because JavaFX doesn't support -fx-overflow.
        // Clipping is controlled via Node.setClip().
        // Return null to prevent JIT compilation of invalid CSS.
      case "overflow" -> null;

      case "cursor" -> "-fx-cursor";

        // Z-index is NOT mapped to CSS because JavaFX doesn't support -fx-z-index.
        // Z-order is controlled via Node.toFront() and Node.toBack().
        // Return null to prevent JIT compilation of invalid CSS.
      case "z" -> null;

        // Resize is NOT mapped to CSS because JavaFX doesn't support -fx-resize.
        // Resizing is controlled programmatically or via layout panes.
        // Return null to prevent JIT compilation of invalid CSS.
      case "resize" -> null;

        // Skew is NOT supported in JavaFX CSS. There is no -fx-she-x or -fx-she-y property.
        // Skew transforms must be applied via Node.getTransforms().add(new Shear(...)).
        // Return null to prevent JIT compilation of invalid CSS.
      case "skew-x", "skew-y" -> null;

        // Blur, brightness, contrast, grayscale, invert, sepia are NOT mapped to CSS
        // because JavaFX doesn't have corresponding -fx-* CSS properties for these filters.
        // These effects are implemented via javafx.scene.effect.* classes (e.g., Blur,
        // ColorAdjust).
        // Return null to prevent JIT compilation of invalid CSS.
      case "blur", "brightness", "contrast", "grayscale", "invert", "sepia" -> null;

      default -> null;
    };
  }

  /**
   * Resolves named values like sm, md, lg, bold, solid, dashed, etc.
   *
   * @param prefix Token prefix
   * @param namedValue Nominal value (sm, md, lg, bold, solid, dashed, etc.)
   * @return CSS value or null if no mapping
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

    // Handle cursor styles: cursor-pointer, cursor-default, etc.
    if ("cursor".equals(prefix)) {
      return resolveCursor(namedValue);
    }

    // Note: overflow is not mapped to CSS (returns null in mapToCssProperty),
    // so this code path will not produce valid inline CSS. Kept for completeness.
    // Handle overflow styles: overflow-hidden, overflow-visible, etc.
    if ("overflow".equals(prefix)) {
      return resolveOverflow(namedValue);
    }

    // Note: visibility is not mapped to CSS (returns null in mapToCssProperty),
    // so this code path will not produce valid inline CSS. Kept for completeness.
    // Handle visibility: visible, hidden, invisible
    if ("visible".equals(prefix) || "hidden".equals(prefix) || "invisible".equals(prefix)) {
      return resolveVisibility(prefix);
    }

    // Note: resize is not mapped to CSS (returns null in mapToCssProperty),
    // so this code path will not produce valid inline CSS. Kept for completeness.
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

    // Note: blur, brightness, contrast, grayscale, invert, sepia, skew are not mapped to CSS
    // (return null in mapToCssProperty), so this code path will not produce valid inline CSS.
    // Kept for completeness.
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

  /** Resolves a border style to its CSS value. */
  private String resolveBorderStyle(String style) {
    return switch (style) {
      case "solid" -> "solid";
      case "dashed" -> "dashed";
      case "dotted" -> "dotted";
      case "none" -> "none";
      default -> null;
    };
  }

  /** Resolves named colors like white, black, transparent. */
  private String resolveNamedColor(String colorName) {
    return switch (colorName) {
      case "white" -> "#ffffff";
      case "black" -> "#000000";
      case "transparent" -> "transparent";
      default -> null;
    };
  }

  /** Resolves a cursor to its JavaFX value. */
  private String resolveCursor(String cursor) {
    return switch (cursor) {
      case "default" -> "default";
      case "pointer" -> "hand";
      case "text" -> "text";
      case "move" -> "move";
      case "wait" -> "wait";
      case "crosshair" -> "crosshair";
      case "help" -> "wait"; // fallback: no direct equivalent in JavaFX
      case "not-allowed" -> "disappear";
      case "context-menu" -> "default"; // fallback: no direct equivalent in JavaFX
      case "vertical-text" -> "text"; // fallback: no direct equivalent in JavaFX
      case "alias" -> "hand"; // fallback: no direct equivalent in JavaFX
      case "all-scroll" -> "move"; // fallback: no direct equivalent in JavaFX
      case "grab" -> "open-hand";
      case "grabbing" -> "closed-hand";
      case "col-resize" -> "h-resize";
      case "row-resize" -> "v-resize";
      case "n-resize" -> "n-resize";
      case "e-resize" -> "e-resize";
      case "s-resize" -> "s-resize";
      case "w-resize" -> "w-resize";
      case "ne-resize" -> "ne-resize";
      case "nw-resize" -> "nw-resize";
      case "se-resize" -> "se-resize";
      case "sw-resize" -> "sw-resize";
      case "nesw-resize" -> "ne-resize"; // fallback to nearest equivalent
      case "nwse-resize" -> "nw-resize"; // fallback to nearest equivalent
      case "none" -> "none";
      default -> null;
    };
  }

  /** Resolves overflow to its JavaFX value. */
  private String resolveOverflow(String overflow) {
    return switch (overflow) {
      case "visible" -> "visible";
      case "hidden" -> "hidden";
      case "scroll" -> "scroll";
      case "auto" -> "auto";
      default -> null;
    };
  }

  /** Resolves visibility. */
  private String resolveVisibility(String visibility) {
    return switch (visibility) {
      case "visible" -> "visible";
      case "hidden", "invisible" -> "hidden";
      default -> null;
    };
  }

  /** Resolves resize to its JavaFX value. */
  private String resolveResize(String resize) {
    return switch (resize) {
      case "none" -> "none";
      case "x" -> "horizontal";
      case "y" -> "vertical";
      case "both" -> "both";
      default -> null;
    };
  }

  /** Resolves special dimension values for JavaFX. */
  private String resolveDimension(String value) {
    return switch (value) {
      case "auto" -> "-1"; // JavaFX: -1 means USE_COMPUTED_SIZE (default behavior)
      case "min" -> "-1"; // JavaFX doesn't support min-content, use computed size
      case "max" -> "-1"; // JavaFX doesn't support max-content, use computed size
      default -> null;
    };
  }

  /** Resolves named max-width values. */
  private String resolveMaxWidth(String value) {
    return switch (value) {
      case "xs" -> "320px";
      case "sm" -> "384px";
      case "md" -> "448px";
      case "lg" -> "512px";
      case "xl" -> "576px";
      case "2xl" -> "672px";
      case "3xl" -> "768px";
        // JavaFX does not support percentage values in -fx-max-width/-fx-pref-width properties.
        // "full" should be handled programmatically via maxWidth(Double.MAX_VALUE) or layout
        // managers.
        // Return null to prevent invalid CSS injection.
      case "full" -> null;
      default -> null;
    };
  }

  /**
   * Maps a complete token to a CSS property with its value.
   *
   * @param token Parsed token
   * @param resolvedValue Already resolved value (e.g., "16px", "rgb(...)")
   * @return Complete CSS property or null
   */
  public String map(StyleToken token, String resolvedValue) {
    if (token == null || resolvedValue == null) {
      return null;
    }

    // Special handling for border widths: border, border-0, border-2, border-4, border-8
    // These should map to -fx-border-width, not -fx-border-color
    // Border width tokens are SCALE type or the base 'border' token without color/shade
    if ("border".equals(token.prefix)) {
      // Check if it's a border style (solid, dashed, dotted, none)
      if (isBorderStyle(token.namedValue)) {
        return prop("-fx-border-style", resolvedValue);
      }
      // Check if it's a border width (SCALE type or base 'border' with numeric scale)
      if (token.kind == StyleToken.Kind.SCALE || (token.scale != null && token.colorName == null)) {
        // Base 'border' without number should be 1px
        if (token.scale == null) {
          return prop("-fx-border-width", "1px");
        }
        return prop("-fx-border-width", resolvedValue);
      }
      // Otherwise it's a border color (COLOR_SHADE type), continue with default mapping
    }

    // Tailwind's text-* utilities are dual-purpose: named sizes (text-sm, text-2xl) set the FONT
    // SIZE, while colors (text-white, text-gray-500) set the text fill. JavaFX needs a different
    // property for each, and a pixel length in -fx-text-fill is not a valid Paint, so the JavaFX
    // CSS parser rejects it. resolveFontSize() returns null for color names, which keeps the
    // -fx-text-fill mapping for them.
    if ("text".equals(token.prefix) && token.namedValue != null) {
      String fontSize = resolveFontSize(token.namedValue);
      if (fontSize != null) {
        return prop("-fx-font-size", fontSize);
      }
    }

    // font-* utilities carry either a weight (font-bold, font-medium) or an arbitrary font family
    // (font-['Inter']). -fx-font-family only accepts a font family name, so weights must go to
    // -fx-font-weight instead of emitting an invalid numeric font family.
    if ("font".equals(token.prefix) && token.namedValue != null) {
      String fontWeight = resolveFontWeight(token.namedValue);
      if (fontWeight != null) {
        return prop("-fx-font-weight", fontWeight);
      }
    }

    // Special handling for rotate: already resolved as degrees without 'px' suffix
    if ("rotate".equals(token.prefix)) {
      String property = mapToCssProperty(token.prefix);
      return prop(property, resolvedValue);
    }

    // Scale utilities carry an optional axis sub-prefix (scale-x-*, scale-y-*). The generic
    // mapping below only sees token.prefix ("scale"), which previously always emitted
    // -fx-scale-x and made scale-y-* wrong. Bare scale-* applies to both axes.
    if ("scale".equals(token.prefix)) {
      if (token.subPrefix != null) {
        String axisProperty =
            switch (token.subPrefix) {
              case "x" -> "-fx-scale-x";
              case "y" -> "-fx-scale-y";
              default -> null;
            };
        if (axisProperty == null) {
          return null;
        }
        return prop(axisProperty, resolvedValue);
      }
      return prop("-fx-scale-x", resolvedValue) + " " + prop("-fx-scale-y", resolvedValue);
    }

    // Gap utilities are dual-axis in JavaFX FlowPane (-fx-hgap / -fx-vgap). The generic mapping
    // only consulted token.prefix, so gap-y-* wrongly emitted -fx-hgap. Bare gap-N sets both.
    if ("gap".equals(token.prefix)) {
      if (token.subPrefix != null) {
        String axisProperty =
            switch (token.subPrefix) {
              case "x" -> "-fx-hgap";
              case "y" -> "-fx-vgap";
              default -> null;
            };
        if (axisProperty == null) {
          return null;
        }
        return prop(axisProperty, resolvedValue);
      }
      return prop("-fx-hgap", resolvedValue) + " " + prop("-fx-vgap", resolvedValue);
    }

    // rounded-* must also round the border: JavaFX draws -fx-border-* independently of the
    // background shape, so without -fx-border-radius a border-* + rounded-* combination keeps
    // sharp corners on the stroke.
    if ("rounded".equals(token.prefix)) {
      return prop("-fx-background-radius", resolvedValue)
          + " "
          + prop("-fx-border-radius", resolvedValue);
    }

    // Aspect-ratio, ring width/color, transitions and animations have no real JavaFX CSS
    // equivalent; they are applied at runtime by AspectRatioProcessor, RingProcessor and
    // TransitionProcessor via JitCompiler.compileBatch. Emitting inert -fx-transition-* or
    // approximate properties here would only confuse consumers of the stylesheet.
    if ("aspect".equals(token.prefix)
        || "transition".equals(token.prefix)
        || "duration".equals(token.prefix)
        || "ease".equals(token.prefix)
        || "animate".equals(token.prefix)) {
      return null;
    }

    // Special handling for translate-x and translate-y: use signed values
    if ("translate".equals(token.prefix) && token.subPrefix != null) {
      String property =
          switch (token.subPrefix) {
            case "x" -> "-fx-translate-x";
            case "y" -> "-fx-translate-y";
            default -> mapToCssProperty(token.prefix + "-" + token.subPrefix);
          };
      if (property != null) {
        return prop(property, resolvedValue);
      }
    }

    // Special handling for w-auto, w-min, w-max, h-auto, h-min, h-max
    if (("w".equals(token.prefix) || "h".equals(token.prefix))
        && ("auto".equals(token.namedValue)
            || "min".equals(token.namedValue)
            || "max".equals(token.namedValue))) {
      String property = mapToCssProperty(token.prefix);
      return prop(property, resolvedValue);
    }

    // Special handling for max-w-*
    if ("max-w".equals(token.prefix) && resolvedValue != null) {
      return prop("-fx-max-width", resolvedValue);
    }

    String property = mapToCssProperty(token.prefix);
    if (property == null) {
      return null;
    }

    // Special handling for composite properties
    if ("p".equals(token.prefix) && token.subPrefix != null) {
      // Composite padding utilities need the full JavaFX property name: "padding" is not a JavaFX
      // CSS property, so the axis forms are emitted as -fx-padding with the four insets
      // (top right bottom left).
      return switch (token.subPrefix) {
        case "x" -> px("-fx-padding", "0px %s 0px %s".formatted(resolvedValue, resolvedValue));
        case "y" -> px("-fx-padding", "%s 0px %s 0px".formatted(resolvedValue, resolvedValue));
        case "t" -> px("-fx-padding", "%s 0px 0px 0px".formatted(resolvedValue));
        case "r" -> px("-fx-padding", "0px %s 0px 0px".formatted(resolvedValue));
        case "b" -> px("-fx-padding", "0px 0px %s 0px".formatted(resolvedValue));
        case "l" -> px("-fx-padding", "0px 0px 0px %s".formatted(resolvedValue));
        default -> prop(property, resolvedValue);
      };
    }

    return prop(property, resolvedValue);
  }

  /** Checks if a value is a valid border style. */
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
    // If value already has 'px', use it directly; otherwise add it
    String formatted = value.matches("\\d+$") ? value + "px" : value;
    return name + ": " + formatted + ";";
  }

  private String resolveFontSize(String size) {
    Map<String, Double> fontSizes = themeConfig.fontSize();
    Double value = fontSizes.get(size);
    if (value != null) {
      return value.intValue() + "px";
    }

    // Fallback for standard sizes
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

  /**
   * Resolves a Tailwind shadow utility to a JavaFX {@code -fx-effect} value.
   *
   * <p>JavaFX has no {@code box-shadow}: {@code -fx-effect} only accepts effect objects, so each
   * Tailwind layer is translated to a {@code dropshadow(offsetX, offsetY, blurRadius, color)}.
   * Emitting the web syntax here (e.g. {@code 0 1px 3px 0 rgba(...)}) is not parseable by the
   * JavaFX CSS parser and made it log a warning for every shadow utility in the stylesheet.
   *
   * @param shadow the shadow size keyword ({@code sm}, {@code md}, {@code lg}, {@code xl}, ...)
   * @return a {@code dropshadow(...)} value, or null for {@code none} / unknown keywords since
   *     JavaFX CSS cannot express "no effect"
   */
  private String resolveShadow(String shadow) {
    // Full JavaFX dropshadow signature:
    //   dropshadow(gaussian, color, radius, spread, offsetX, offsetY)
    // The previous 4-argument form was not valid JavaFX CSS and made the parser reject every
    // shadow utility in the generated stylesheet.
    return switch (shadow) {
      case "sm" -> "dropshadow(gaussian, rgba(0,0,0,0.05), 2, 0, 0, 1)";
      case "default", "md" -> "dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1)";
      case "lg" -> "dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 10)";
      case "xl" -> "dropshadow(gaussian, rgba(0,0,0,0.1), 25, 0, 0, 20)";
      case "2xl" -> "dropshadow(gaussian, rgba(0,0,0,0.25), 50, 0, 0, 25)";
        // JavaFX CSS offers no way to clear an inherited effect from a class rule.
      case "none" -> null;
      default -> null;
    };
  }

  /** Resolves effect values: blur, brightness, contrast, grayscale, invert, sepia, skew. */
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
