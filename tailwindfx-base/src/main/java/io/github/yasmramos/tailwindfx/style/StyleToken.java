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
package io.github.yasmramos.tailwindfx.style;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StyleToken — Represents a parsed JIT token.
 *
 * <p>A token is the parser's smallest unit: "p-4", "bg-blue-500/80", "w-[320px]". Each token
 * becomes one or more inline -fx-* properties.
 *
 * <p>Supported formats: Basic: p-4 bg-blue-500 text-sm opacity-75 Scaled: text-gray-900
 * border-red-300 shadow-blue-500 Opacity: bg-blue-500/80 text-gray-900/50 (alpha 0-100) Negative:
 * -mt-4 -translate-x-2 -rotate-45 Arbitrary: p-[13px] bg-[#ff6600] w-[320px] text-[16px]
 * rotate-[45deg]
 */
public final class StyleToken {

  // Regex patterns

  // Arbitrary: prefix-[value] or prefix-[value]/alpha (e.g. bg-[#ff0000]/80)
  private static final Pattern ARBITRARY =
      Pattern.compile("^(-?)([a-z][a-z-]*)(?:-([a-z]+))?-\\[([^\\]]+)](?:/(\\d+))?$");

  // Color with shade and optional opacity: bg-blue-500/80
  private static final Pattern COLOR_SHADE_ALPHA =
      Pattern.compile("^(-?)([a-z][a-z-]*)-([a-z]+)-(\\d+)(?:/(\\d+))?$");

  // Basic scale: p-4, w-12, gap-8
  private static final Pattern SCALE = Pattern.compile("^(-?)([a-z][a-z-]*)-(\\d+)$");

  // Named: text-sm, rounded-lg, font-bold
  private static final Pattern NAMED = Pattern.compile("^(-?)([a-z][a-z-]*)-([a-z0-9]+)$");

  // Fields of the parsed token

  public enum Kind {
    SCALE,
    COLOR_SHADE,
    ARBITRARY,
    NAMED,
    UNKNOWN
  }

  public final String raw; // token original: "bg-blue-500/80"
  public final boolean negative; // -mt-4 → true
  public final String prefix; // "bg", "p", "text", "w"
  public final String colorName; // "blue", "gray", "red" (solo COLOR_SHADE)
  public final Integer shade; // 500, 300, 900 (solo COLOR_SHADE)
  public final Integer alpha; // 0-100, null si no hay /alpha
  public final Integer scale; // 4, 8, 12 (solo SCALE)
  public final String namedValue; // "sm", "lg", "bold" (NAMED/COLOR_SHADE)
  public final String arbitraryVal; // "13px", "#ff6600" (ARBITRARY)
  public final String subPrefix; // "x", "y", "t", "r", "b", "l" de px-4, pt-2
  public final Kind kind;

  // Factory — parses a string into a StyleToken

  public static StyleToken parse(String raw) {
    if (raw == null || raw.isBlank()) return unknown(raw);

    String token = raw.trim();

    // 1. Arbitrary: prefix-[value] or prefix-subprefix-[value]
    Matcher m = ARBITRARY.matcher(token);
    if (m.matches()) {
      // For "-translate-x-[20px]": the regex may capture prefix="translate-x", sub=null.
      // splitSubPrefix normalizes it to prefix="translate", sub="x".
      String rawPrefix = m.group(2);
      String rawSub = m.group(3);
      String[] parts =
          rawSub == null ? splitSubPrefix(rawPrefix) : new String[] {rawPrefix, rawSub};
      Integer alpha = m.group(5) != null ? Integer.parseInt(m.group(5)) : null;
      return new StyleToken(
          raw,
          !m.group(1).isEmpty(),
          parts[0],
          parts[1],
          null,
          null,
          alpha,
          null,
          null,
          m.group(4), // arbitraryVal
          Kind.ARBITRARY);
    }

    // 2. Color with shade: bg-blue-500 or bg-blue-500/80
    m = COLOR_SHADE_ALPHA.matcher(token);
    if (m.matches() && isColorName(m.group(3))) {
      Integer alpha = m.group(5) != null ? Integer.parseInt(m.group(5)) : null;
      return new StyleToken(
          raw,
          !m.group(1).isEmpty(),
          m.group(2), // prefix
          null, // subPrefix
          m.group(3), // colorName
          Integer.parseInt(m.group(4)), // shade
          alpha,
          null,
          null,
          null,
          Kind.COLOR_SHADE);
    }

    // 3. Numeric scale: p-4, w-12, -mt-4
    m = SCALE.matcher(token);
    if (m.matches()) {
      // Detectar sub-prefix: px, py, pt, pr, pb, pl, mx, my, etc.
      String full = m.group(2);
      String[] parts = splitSubPrefix(full);
      // Side border widths with an explicit width: border-t-4, border-x-2, ...
      if (parts[0].startsWith("border-") && isBorderSide(parts[0].substring("border-".length()))) {
        parts = new String[] {"border", parts[0].substring("border-".length())};
      }
      return new StyleToken(
          raw,
          !m.group(1).isEmpty(),
          parts[0], // prefix base
          parts[1], // subPrefix (x, y, t, r, b, l) o null
          null,
          null,
          null,
          Integer.parseInt(m.group(3)), // scale
          null,
          null,
          Kind.SCALE);
    }

    // 4. Named: text-sm, rounded-lg, font-bold, opacity-75 (no color shade)
    m = NAMED.matcher(token);
    if (m.matches()) {
      String full = m.group(2);
      String[] parts = splitSubPrefix(full);
      String namedValue = m.group(3);
      // Side border widths: border-t, border-r, border-b, border-l, border-x, border-y
      // default to 1px on the given side(s); model them as SCALE so map() can emit the
      // four-value -fx-border-width declaration.
      if ("border".equals(parts[0]) && parts[1] == null && isBorderSide(namedValue)) {
        return new StyleToken(
            raw, !m.group(1).isEmpty(), "border", namedValue, null, null, null, 1, null, null,
            Kind.SCALE);
      }
      return new StyleToken(
          raw,
          !m.group(1).isEmpty(),
          parts[0],
          parts[1],
          null,
          null,
          null,
          null,
          namedValue,
          null,
          Kind.NAMED);
    }

    // 5. Base prefix without suffix: rounded, border, shadow
    // In Tailwind, "rounded" without a suffix applies the default border radius.
    // Treat it as NAMED with namedValue="default" so resolveBorderRadius("default") works.
    if (token.equals("rounded")) {
      return new StyleToken(raw, false, "rounded", null, null, null, null, null, "default", null, Kind.NAMED);
    }
    // "border" without a suffix is a 1px border width on all sides.
    if (token.equals("border")) {
      return new StyleToken(raw, false, "border", null, null, null, null, 1, null, null, Kind.SCALE);
    }

    return unknown(raw);
  }

  /** Sides accepted by the border-{t,r,b,l,x,y} width utilities. */
  private static boolean isBorderSide(String value) {
    return "t".equals(value)
        || "r".equals(value)
        || "b".equals(value)
        || "l".equals(value)
        || "x".equals(value)
        || "y".equals(value);
  }

  // Constructor privado

  private StyleToken(
      String raw,
      boolean negative,
      String prefix,
      String subPrefix,
      String colorName,
      Integer shade,
      Integer alpha,
      Integer scale,
      String namedValue,
      String arbitraryVal,
      Kind kind) {
    this.raw = raw;
    this.negative = negative;
    this.prefix = prefix;
    this.subPrefix = subPrefix;
    this.colorName = colorName;
    this.shade = shade;
    this.alpha = alpha;
    this.scale = scale;
    this.namedValue = namedValue;
    this.arbitraryVal = arbitraryVal;
    this.kind = kind;
  }

  private static StyleToken unknown(String raw) {
    return new StyleToken(raw, false, raw, null, null, null, null, null, null, null, Kind.UNKNOWN);
  }

  // Helpers

  /** Numeric scale value, applying a negative sign when present */
  public int signedScale() {
    return negative ? -(scale != null ? scale : 0) : (scale != null ? scale : 0);
  }

  /** Alpha as a double, 0-100 → 0.0-1.0 */
  public double alphaFraction() {
    return alpha != null ? alpha / 100.0 : 1.0;
  }

  /** Whether this token carries alpha (partial color opacity) */
  public boolean hasAlpha() {
    return alpha != null;
  }

  @Override
  public String toString() {
    return "StyleToken{" + kind + ", raw=" + raw + "}";
  }

  // Internos

  private static final java.util.Set<String> COLOR_NAMES =
      java.util.Set.of(
          "slate", "gray", "zinc", "neutral", "stone", "red", "orange", "amber", "yellow", "lime",
          "green", "emerald", "teal", "cyan", "sky", "blue", "indigo", "violet", "purple",
          "fuchsia", "pink", "rose", "white", "black");

  private static boolean isColorName(String s) {
    return COLOR_NAMES.contains(s);
  }

  /** Prefixes that are named values without a scale (visible, hidden, overflow-*, cursor-*, etc.) */
  private static final java.util.Set<String> NAMED_PREFIXES =
      java.util.Set.of(
          "visible",
          "hidden",
          "invisible",
          "overflow",
          "cursor",
          "resize",
          "z",
          "gap",
          "gap-x",
          "gap-y",
          "w-auto",
          "w-full",
          "w-min",
          "w-max",
          "h-auto",
          "h-full",
          "h-min",
          "h-max",
          "min-w-0",
          "min-w-full",
          "min-w-max",
          "max-w-xs",
          "max-w-sm",
          "max-w-md",
          "max-w-lg",
          "max-w-xl",
          "max-w-2xl",
          "max-w-3xl",
          "max-w-full",
          "max-h-full",
          "max-h-screen",
          "relative",
          "absolute",
          "sr-only",
          "not-sr-only");

  private static boolean isNamedPrefix(String s) {
    return NAMED_PREFIXES.contains(s);
  }

  /**
   * Splits "px" → ["p","x"], "pt" → ["p","t"], "translate" → ["translate",null]. Only for 1-2
   * letter prefixes with known sub-directions.
   */
  private static String[] splitSubPrefix(String full) {
    return switch (full) {
      case "px" -> new String[] {"p", "x"};
      case "py" -> new String[] {"p", "y"};
      case "pt" -> new String[] {"p", "t"};
      case "pr" -> new String[] {"p", "r"};
      case "pb" -> new String[] {"p", "b"};
      case "pl" -> new String[] {"p", "l"};
      case "mx" -> new String[] {"m", "x"};
      case "my" -> new String[] {"m", "y"};
      case "mt" -> new String[] {"m", "t"};
      case "mr" -> new String[] {"m", "r"};
      case "mb" -> new String[] {"m", "b"};
      case "ml" -> new String[] {"m", "l"};
      case "gap-x" -> new String[] {"gap", "x"};
      case "gap-y" -> new String[] {"gap", "y"};
      case "translate-x" -> new String[] {"translate", "x"};
      case "translate-y" -> new String[] {"translate", "y"};
      case "skew-x" -> new String[] {"skew", "x"};
      case "skew-y" -> new String[] {"skew", "y"};
      case "scale-x" -> new String[] {"scale", "x"};
      case "scale-y" -> new String[] {"scale", "y"};
      case "scroll-m" -> new String[] {"scroll", "m"};
      case "scroll-p" -> new String[] {"scroll", "p"};
      default -> new String[] {full, null};
    };
  }
}
