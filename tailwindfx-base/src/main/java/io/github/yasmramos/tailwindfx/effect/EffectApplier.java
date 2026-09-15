package io.github.yasmramos.tailwindfx.effect;

import io.github.yasmramos.tailwindfx.TwConfig;
import io.github.yasmramos.tailwindfx.TwEffect;
import io.github.yasmramos.tailwindfx.core.TokenRegistry;
import javafx.scene.Node;

/**
 * EffectApplier — Applies visual effects to JavaFX nodes.
 *
 * <p>This class handles detection and application of Tailwind CSS filter/effect tokens (blur,
 * brightness, contrast, grayscale, invert, sepia) by delegating to TwEffect.
 *
 * <p>Detection is delegated to TokenRegistry for centralized token classification. Application uses
 * specific exception handling to avoid silently swallowing bugs.
 *
 * <pre>
 * // Apply effect tokens
 * EffectApplier.applyEffectToken(node, "blur-sm");
 * EffectApplier.applyEffectToken(node, "grayscale");
 * EffectApplier.applyEffectToken(node, "brightness-125");
 * </pre>
 */
public final class EffectApplier {

  private static final EffectApplier INSTANCE = new EffectApplier();

  private EffectApplier() {}

  /**
   * Applies an effect token to a node.
   *
   * <p>This method detects the effect type and delegates to the appropriate TwEffect method.
   * Detection is performed using TokenRegistry.isEffectToken() for consistency.
   *
   * <p>Exception handling: Specific exceptions from TwEffect are re-thrown to surface bugs.
   * Unexpected exceptions are logged at WARN level (in debug mode) and re-thrown to avoid silently
   * hiding real issues.
   *
   * @param node the node to apply the effect to
   * @param token the effect token (e.g., "blur-sm", "brightness-125", "grayscale")
   * @throws IllegalArgumentException if the token is invalid or node is null
   */
  public static void applyEffectToken(Node node, String token) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }
    if (token == null || token.isBlank()) {
      return;
    }

    // Validate this is actually an effect token
    if (!TokenRegistry.isEffectToken(token)) {
      if (TwConfig.isDebug()) {
        System.out.println("[TailwindFX Warning] Not an effect token: " + token);
      }
      return;
    }

    try {
      if (token.startsWith("blur-")) {
        String size = token.substring(5);
        if ("none".equals(size)) {
          TwEffect.blurNone(node);
        } else {
          TwEffect.blurWithSize(node, size);
        }
      } else if (token.equals("blur")) {
        TwEffect.blur(node, 0); // default blur
      } else if (token.startsWith("brightness-")) {
        String percentage = token.substring(11);
        TwEffect.brightnessWithPercentage(node, percentage);
      } else if (token.equals("brightness")) {
        TwEffect.brightness(node, 1.0); // default no change
      } else if (token.startsWith("contrast-")) {
        String percentage = token.substring(9);
        TwEffect.contrastWithPercentage(node, percentage);
      } else if (token.equals("contrast")) {
        TwEffect.contrast(node, 1.0); // default no change
      } else if (token.equals("grayscale")) {
        TwEffect.grayscale(node);
      } else if (token.equals("grayscale-0")) {
        TwEffect.grayscaleNone(node);
      } else if (token.equals("invert")) {
        TwEffect.invert(node);
      } else if (token.equals("invert-0")) {
        TwEffect.invertNone(node);
      } else if (token.equals("sepia")) {
        TwEffect.sepia(node);
      } else if (token.equals("sepia-0")) {
        TwEffect.sepiaNone(node);
      } else if (TwConfig.isDebug()) {
        System.out.println("[TailwindFX Warning] Unsupported effect token: " + token);
      }
    } catch (IllegalArgumentException e) {
      // Re-throw validation errors from TwEffect to surface bugs immediately
      throw e;
    } catch (Exception e) {
      // Log unexpected exceptions at WARN level and re-throw to avoid hiding bugs
      if (TwConfig.isDebug()) {
        System.out.println(
            "[TailwindFX WARN] Failed to apply effect \"" + token + "\": " + e.getMessage());
      }
      throw new RuntimeException("Failed to apply effect: " + token, e);
    }
  }
}
