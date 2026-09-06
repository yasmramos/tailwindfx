package io.github.yasmramos.tailwindfx;

import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;

/**
 * TwEffect — Visual effects facade for JavaFX nodes.
 *
 * <p>Provides access to visual effects including blur, brightness, contrast, grayscale, invert,
 * sepia, and shadow utilities using JavaFX's {@code javafx.scene.effect} API.
 *
 * <p>Note: JavaFX does not support CSS filter properties directly. These effects are applied
 * programmatically via {@link Node#setEffect(Effect)} rather than through inline CSS styles.
 *
 * <pre>
 * // Blur effects
 * TwEffect.blur(node, 4);        // Apply Gaussian blur with radius 4
 * TwEffect.blurNone(node);       // Remove blur effect
 *
 * // Brightness/Contrast adjustments
 * TwEffect.brightness(node, 1.25);  // 125% brightness
 * TwEffect.contrast(node, 1.1);     // 110% contrast
 *
 * // Color filters
 * TwEffect.grayscale(node);      // Convert to grayscale
 * TwEffect.invert(node);         // Invert colors
 * TwEffect.sepia(node);          // Apply sepia tone
 * </pre>
 *
 * <p><strong>Limitations:</strong>
 *
 * <ul>
 *   <li>JavaFX only allows one {@code Effect} per node. Multiple effects must be chained using
 *       {@code setInput()} or combined via {@code Blend}.
 *   <li>CSS filter syntax (e.g., {@code filter: blur(4px)}) is not supported in JavaFX CSS. These
 *       methods provide the equivalent functionality programmatically.
 *   <li>{@code backdrop-filter} is not available in JavaFX; use regular {@code blur()} instead.
 * </ul>
 */
public final class TwEffect {

  private static final TwEffect INSTANCE = new TwEffect();

  // Tailwind CSS v4 blur scale mappings (in pixels)
  // Source: https://tailwindcss.com/docs/blur
  private static final double BLUR_NONE = 0;
  private static final double BLUR_SM = 4;
  private static final double BLUR_MD = 8;
  private static final double BLUR_LG = 16;
  private static final double BLUR_XL = 24;
  private static final double BLUR_2XL = 40;
  private static final double BLUR_3XL = 64;

  private TwEffect() {}

  /**
   * Apply backdrop blur effect to a node (legacy method).
   *
   * @param node the node
   * @param radius blur radius in pixels
   * @deprecated Use {@link #blur(Node, double)} instead
   */
  @Deprecated
  public static void backdropBlur(Node node, double radius) {
    blur(node, radius);
  }

  /**
   * Remove backdrop blur effect from a node (legacy method).
   *
   * @param node the node
   * @deprecated Use {@link #blurNone(Node)} instead
   */
  @Deprecated
  public static void backdropBlurNone(Node node) {
    blurNone(node);
  }

  // ============================================================================
  // BLUR EFFECTS
  // ============================================================================

  /**
   * Apply Gaussian blur effect to a node.
   *
   * <p>Uses {@link GaussianBlur} for high-quality blur rendering. The radius determines the blur
   * intensity: larger values produce stronger blur effects.
   *
   * @param node the node to apply the effect to (must not be null)
   * @param radius blur radius in pixels (must be &gt;= 0)
   * @throws IllegalArgumentException if node is null or radius is negative
   */
  public static void blur(Node node, double radius) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }
    if (radius < 0) {
      throw new IllegalArgumentException("Blur radius cannot be negative: " + radius);
    }

    if (radius == 0) {
      node.setEffect(null);
      return;
    }

    GaussianBlur blur = new GaussianBlur(radius);
    applyOrChainEffect(node, blur);
  }

  /**
   * Remove blur effect from a node.
   *
   * @param node the node to remove the effect from (must not be null)
   * @throws IllegalArgumentException if node is null
   */
  public static void blurNone(Node node) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }
    node.setEffect(null);
  }

  /**
   * Apply blur effect using Tailwind CSS naming convention.
   *
   * @param node the node to apply the effect to
   * @param size Tailwind size token: "none", "sm", "md", "lg", "xl", "2xl", "3xl"
   * @throws IllegalArgumentException if size is not recognized
   */
  public static void blurWithSize(Node node, String size) {
    double radius =
        switch (size) {
          case "none" -> BLUR_NONE;
          case "sm" -> BLUR_SM;
          case "md" -> BLUR_MD;
          case "lg" -> BLUR_LG;
          case "xl" -> BLUR_XL;
          case "2xl" -> BLUR_2XL;
          case "3xl" -> BLUR_3XL;
          default -> throw new IllegalArgumentException("Unknown blur size: " + size);
        };
    blur(node, radius);
  }

  // ============================================================================
  // BRIGHTNESS EFFECT
  // ============================================================================

  /**
   * Apply brightness adjustment to a node.
   *
   * <p>Brightness values:
   *
   * <ul>
   *   <li>{@code 0} = black
   *   <li>{@code 1} = original (no change)
   *   <li>{@code >1} = brighter
   *   <li>{@code <1} = darker
   * </ul>
   *
   * <p>Tailwind mapping: {@code brightness-50} → 0.5, {@code brightness-100} → 1.0, {@code
   * brightness-125} → 1.25, {@code brightness-200} → 2.0
   *
   * @param node the node to apply the effect to (must not be null)
   * @param value brightness multiplier (typically 0.0 to 2.0)
   * @throws IllegalArgumentException if node is null
   */
  public static void brightness(Node node, double value) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }

    ColorAdjust adjust = getOrCreateColorAdjust(node);
    adjust.setBrightness(value - 1.0); // ColorAdjust uses offset from 1.0
  }

  /**
   * Remove brightness adjustment from a node.
   *
   * @param node the node to remove the effect from (must not be null)
   * @throws IllegalArgumentException if node is null
   */
  public static void brightnessNone(Node node) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }

    ColorAdjust adjust = getOrCreateColorAdjust(node);
    adjust.setBrightness(0);
  }

  /**
   * Apply brightness effect using Tailwind CSS percentage naming.
   *
   * @param node the node to apply the effect to
   * @param percentage Tailwind percentage: "0", "50", "75", "90", "95", "100", "105", "110", "125",
   *     "150", "200"
   * @throws IllegalArgumentException if percentage is not recognized
   */
  public static void brightnessWithPercentage(Node node, String percentage) {
    double value =
        switch (percentage) {
          case "0" -> 0.0;
          case "50" -> 0.5;
          case "75" -> 0.75;
          case "90" -> 0.9;
          case "95" -> 0.95;
          case "100" -> 1.0;
          case "105" -> 1.05;
          case "110" -> 1.1;
          case "125" -> 1.25;
          case "150" -> 1.5;
          case "200" -> 2.0;
          default -> throw new IllegalArgumentException(
              "Unknown brightness percentage: " + percentage);
        };
    brightness(node, value);
  }

  // ============================================================================
  // CONTRAST EFFECT
  // ============================================================================

  /**
   * Apply contrast adjustment to a node.
   *
   * <p>Contrast values:
   *
   * <ul>
   *   <li>{@code 0} = gray (no contrast)
   *   <li>{@code 1} = original (no change)
   *   <li>{@code >1} = higher contrast
   *   <li>{@code <1} = lower contrast
   * </ul>
   *
   * <p>Tailwind mapping: {@code contrast-50} → 0.5, {@code contrast-100} → 1.0, {@code
   * contrast-125} → 1.25
   *
   * @param node the node to apply the effect to (must not be null)
   * @param value contrast multiplier (typically 0.0 to 2.0)
   * @throws IllegalArgumentException if node is null
   */
  public static void contrast(Node node, double value) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }

    ColorAdjust adjust = getOrCreateColorAdjust(node);
    adjust.setContrast(value - 1.0); // ColorAdjust uses offset from 1.0
  }

  /**
   * Remove contrast adjustment from a node.
   *
   * @param node the node to remove the effect from (must not be null)
   * @throws IllegalArgumentException if node is null
   */
  public static void contrastNone(Node node) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }

    ColorAdjust adjust = getOrCreateColorAdjust(node);
    adjust.setContrast(0);
  }

  /**
   * Apply contrast effect using Tailwind CSS percentage naming.
   *
   * @param node the node to apply the effect to
   * @param percentage Tailwind percentage: "0", "50", "75", "100", "125", "150", "200"
   * @throws IllegalArgumentException if percentage is not recognized
   */
  public static void contrastWithPercentage(Node node, String percentage) {
    double value =
        switch (percentage) {
          case "0" -> 0.0;
          case "50" -> 0.5;
          case "75" -> 0.75;
          case "100" -> 1.0;
          case "125" -> 1.25;
          case "150" -> 1.5;
          case "200" -> 2.0;
          default -> throw new IllegalArgumentException(
              "Unknown contrast percentage: " + percentage);
        };
    contrast(node, value);
  }

  // ============================================================================
  // GRAYSCALE EFFECT
  // ============================================================================

  /**
   * Apply grayscale effect to a node.
   *
   * <p>Converts the node to grayscale by setting saturation to -1.0 (complete desaturation).
   *
   * @param node the node to apply the effect to (must not be null)
   * @throws IllegalArgumentException if node is null
   */
  public static void grayscale(Node node) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }

    ColorAdjust adjust = getOrCreateColorAdjust(node);
    adjust.setSaturation(-1.0); // -1.0 = complete desaturation (grayscale)
  }

  /**
   * Remove grayscale effect from a node (restore full color).
   *
   * @param node the node to remove the effect from (must not be null)
   * @throws IllegalArgumentException if node is null
   */
  public static void grayscaleNone(Node node) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }

    ColorAdjust adjust = getOrCreateColorAdjust(node);
    adjust.setSaturation(0); // 0 = original saturation
  }

  // ============================================================================
  // INVERT EFFECT
  // ============================================================================

  /**
   * Apply color inversion to a node.
   *
   * <p>Inverts colors by adjusting hue by 180 degrees (π radians), creating a negative-like effect.
   * Note: True color inversion requires more complex processing; this provides a close
   * approximation using hue rotation.
   *
   * @param node the node to apply the effect to (must not be null)
   * @throws IllegalArgumentException if node is null
   */
  public static void invert(Node node) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }

    ColorAdjust adjust = getOrCreateColorAdjust(node);
    adjust.setHue(Math.PI); // 180 degrees hue rotation
  }

  /**
   * Remove color inversion from a node.
   *
   * @param node the node to remove the effect from (must not be null)
   * @throws IllegalArgumentException if node is null
   */
  public static void invertNone(Node node) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }

    ColorAdjust adjust = getOrCreateColorAdjust(node);
    adjust.setHue(0); // Reset hue to original
  }

  // ============================================================================
  // SEPIA EFFECT
  // ============================================================================

  /**
   * Apply sepia tone effect to a node.
   *
   * <p>Creates a warm, vintage photograph effect by:
   *
   * <ul>
   *   <li>Reducing saturation to ~0.3 (partial desaturation)
   *   <li>Shifting hue slightly toward orange/brown (~0.15 radians)
   *   <li>Reducing brightness slightly (~0.9)
   * </ul>
   *
   * @param node the node to apply the effect to (must not be null)
   * @throws IllegalArgumentException if node is null
   */
  public static void sepia(Node node) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }

    ColorAdjust adjust = getOrCreateColorAdjust(node);
    adjust.setSaturation(-0.7); // Reduce saturation to ~30%
    adjust.setHue(0.15); // Slight shift toward warm tones
    adjust.setBrightness(-0.1); // Slightly darker
  }

  /**
   * Remove sepia tone effect from a node.
   *
   * @param node the node to remove the effect from (must not be null)
   * @throws IllegalArgumentException if node is null
   */
  public static void sepiaNone(Node node) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }

    ColorAdjust adjust = getOrCreateColorAdjust(node);
    adjust.setSaturation(0);
    adjust.setHue(0);
    adjust.setBrightness(0);
  }

  // ============================================================================
  // HELPER METHODS
  // ============================================================================

  /**
   * Retrieves an existing {@link ColorAdjust} effect from the node or creates a new one.
   *
   * <p>If the node already has a {@code ColorAdjust} effect, it is returned. If the node has a
   * different effect type, a new {@code ColorAdjust} is created and chained via {@code setInput()}.
   * If no effect exists, a new {@code ColorAdjust} is created.
   *
   * @param node the node to get or create the effect for
   * @return the existing or newly created {@code ColorAdjust} effect
   */
  private static ColorAdjust getOrCreateColorAdjust(Node node) {
    Effect currentEffect = node.getEffect();

    if (currentEffect instanceof ColorAdjust) {
      return (ColorAdjust) currentEffect;
    }

    // If there's an existing effect of a different type, chain it
    ColorAdjust adjust = new ColorAdjust();
    if (currentEffect != null) {
      adjust.setInput(currentEffect);
    }
    node.setEffect(adjust);
    return adjust;
  }

  /**
   * Applies an effect to a node, chaining with existing effects if necessary.
   *
   * <p>JavaFX only allows one {@code Effect} per node. This method handles chaining by:
   *
   * <ul>
   *   <li>If no effect exists: sets the new effect directly
   *   <li>If the existing effect is the same type: replaces it
   *   <li>If a different effect exists: wraps the new effect in a Blend or uses setInput on
   *       ColorAdjust/GaussianBlur
   * </ul>
   *
   * @param node the node to apply the effect to
   * @param newEffect the effect to apply
   */
  private static void applyOrChainEffect(Node node, Effect newEffect) {
    Effect currentEffect = node.getEffect();

    if (currentEffect == null || currentEffect.getClass() == newEffect.getClass()) {
      node.setEffect(newEffect);
    } else if (newEffect instanceof GaussianBlur blur
        && currentEffect instanceof ColorAdjust adjust) {
      // Chain: ColorAdjust -> GaussianBlur
      blur.setInput(adjust);
      node.setEffect(blur);
    } else if (newEffect instanceof ColorAdjust newAdjust) {
      // Chain: existing -> ColorAdjust
      newAdjust.setInput(currentEffect);
      node.setEffect(newAdjust);
    } else {
      // For other combinations, just replace (or could use Blend for complex chaining)
      node.setEffect(newEffect);
    }
  }
}
