/*
 * Copyright 2026 Yasmany Ramos García (yasmramos).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.yasmramos.tailwindfx.animation;

import io.github.yasmramos.tailwindfx.TwConfig;
import io.github.yasmramos.tailwindfx.core.TokenRegistry;
import javafx.scene.Node;

/**
 * AnimationApplier — Plays Tailwind {@code animate-*} utilities as JavaFX animations.
 *
 * <p>JavaFX has no CSS animation engine: there is no {@code @keyframes} equivalent, and inline
 * styles cannot express a running animation. Tailwind's animation utilities therefore cannot be
 * compiled into {@code -fx-*} properties. This class bridges that gap by mapping each supported
 * {@code animate-*} token to the equivalent {@link TwAnimation} timeline and playing it on the
 * node.
 *
 * <p>Animations are registered in the {@link TwAnimation.AnimationRegistry} under the {@code
 * "loop"} slot so re-applying the same token cancels the previous run instead of stacking two
 * timelines on the same properties. Animations that are inherently one-shot (shake, flash) still go
 * through the registry, which cleans the slot up on completion.
 *
 * <pre>
 * // Apply an animation token
 * AnimationApplier.applyAnimationToken(node, "animate-spin");
 * AnimationApplier.applyAnimationToken(node, "animate-pulse");
 * </pre>
 *
 * <p>Recognition is delegated to {@link TokenRegistry#isAnimationToken(String)} so this class and
 * the token parser agree on what counts as an animation utility.
 *
 * @author yasmramos
 * @since 1.0
 */
public final class AnimationApplier {

  /** Registry slot used for tokens applied through this class. */
  private static final String SLOT = "loop";

  private AnimationApplier() {
    // Utility class
  }

  /**
   * Applies an animation token to a node, starting the corresponding timeline.
   *
   * <p>Unsupported or unrecognized tokens are ignored silently: an unknown {@code animate-*} name is
   * a typo, not a programming error, and there is nothing sensible to render. When debug mode is
   * enabled the ignored token is logged instead.
   *
   * @param node the node to animate (must not be null)
   * @param token the animation token (e.g. {@code "animate-spin"}, {@code "animate-pulse"})
   * @throws IllegalArgumentException if node is null
   */
  public static void applyAnimationToken(Node node, String token) {
    if (node == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }
    if (token == null || token.isBlank()) {
      return;
    }

    if (!TokenRegistry.isAnimationToken(token)) {
      if (TwConfig.isDebug()) {
        System.out.println("[TailwindFX Warning] Not an animation token: " + token);
      }
      return;
    }

    TwAnimation animation = buildAnimation(node, animationName(token));
    if (animation == null) {
      if (TwConfig.isDebug()) {
        System.out.println("[TailwindFX Warning] No animation available for: " + token);
      }
      return;
    }

    animation.register(node, SLOT).play();
  }

  /**
   * Extracts the animation name from an {@code animate-*} token.
   *
   * @param token the animation token (e.g. {@code "animate-spin"})
   * @return the animation name without the prefix (e.g. {@code "spin"})
   */
  private static String animationName(String token) {
    return token.substring("animate-".length());
  }

  /**
   * Maps an animation name to the matching {@link TwAnimation} factory.
   *
   * <p>The {@code -slow} variants mirror Tailwind's slower cycle durations by slowing playback
   * rather than rebuilding the timeline.
   *
   * @param node the node to animate
   * @param name the animation name without the prefix (e.g. {@code "spin"})
   * @return the animation, or null when the name has no JavaFX equivalent
   */
  private static TwAnimation buildAnimation(Node node, String name) {
    return switch (name) {
      case "spin" -> TwAnimation.spin(node);
      case "spin-slow" -> TwAnimation.spin(node).speed(0.5);
      case "pulse" -> TwAnimation.pulse(node);
      case "pulse-slow" -> TwAnimation.pulse(node).speed(0.5);
      case "bounce" -> TwAnimation.bounce(node).loop();
      case "bounce-slow" -> TwAnimation.bounce(node).loop().speed(0.5);
      case "ping" -> TwAnimation.pulse(node).speed(2.0);
      case "flash" -> TwAnimation.flash(node);
      case "shake", "shake-x" -> TwAnimation.shake(node);
      // "shake-y" has no JavaFX equivalent in TwAnimation yet: the existing shake() only offsets
      // translateX. Returning null keeps the token recognized but inert instead of playing a
      // horizontally-shaking animation that contradicts its name.
      default -> null;
    };
  }
}