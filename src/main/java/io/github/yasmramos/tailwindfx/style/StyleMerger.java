package io.github.yasmramos.tailwindfx.style;

import io.github.yasmramos.tailwindfx.core.JitCompiler;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.Node;

/**
 * StyleMerger — Applies JIT inline styles to JavaFX nodes without destroying previous styles.
 *
 * <p>Problem: node.setStyle() overwrites ALL existing inline style. Solution: parse current style,
 * merge by property, and rewrite.
 *
 * <p>Merge rules: - JIT wins over previous styles of the same property (developer's intent) -
 * Properties not affected by JIT are preserved intact - Node's CSS classes are NOT touched here
 * (handled by TailwindFX.apply)
 */
public final class StyleMerger {

  private StyleMerger() {}

  // Regex para parsear "property: value;" de un inline style
  private static final Pattern PROP_PATTERN = Pattern.compile("(-fx-[a-z-]+)\\s*:\\s*([^;]+);?");

  // Public API
  /**
   * Aplica tokens JIT a un nodo. Procesa las CSS classes fallback y el inline style merged.
   *
   * <p>Ejemplo: StyleMerger.applyJit(button, "p-4", "bg-blue-500/80", "rounded-lg", "font-bold");
   */
  public static void applyJit(Node node, String... tokens) {
    JitCompiler.BatchResult result = JitCompiler.compileBatch(tokens);

    // 1. Apply non-CSS properties via Styles API (cursor, overflow, resize, z-index, etc.)
    for (String token : tokens) {
      if (token == null || token.isBlank()) continue;
      applyNonCssProperties(node, token);
    }

    // 2. Inline styles: merge no destructivo (solo para propiedades CSS válidas)
    if (result.hasInlineStyle()) {
      String merged = merge(node.getStyle(), result.inlineStyle());
      node.setStyle(merged);
    }

    // 3. CSS classes fallback (tokens desconocidos o que mapean a clases)
    for (String cls : result.cssClasses()) {
      if (!node.getStyleClass().contains(cls)) {
        node.getStyleClass().add(cls);
      }
    }
  }

  /** Detecta y aplica propiedades que no tienen soporte CSS en JavaFX, delegando a Styles.java. */
  private static void applyNonCssProperties(Node node, String token) {
    String baseToken = token;

    // Extraer token base si tiene variante (ej. hover:cursor-pointer → cursor-pointer)
    int lastColon = token.lastIndexOf(':');
    if (lastColon >= 0 && lastColon < token.length() - 1) {
      baseToken = token.substring(lastColon + 1);
      // Para variantes, se aplicará cuando la variante se active
      // Por ahora solo manejamos tokens sin variante directamente
      if (!token.contains(":")) {
        // No es una variante, aplicar directamente
      } else {
        // Es una variante, el manejo se hace en VariantManager
        return;
      }
    }

    // Cursor utilities
    if (baseToken.startsWith("cursor-")) {
      switch (baseToken) {
        case "cursor-default":
          Styles.cursorDefault(node);
          break;
        case "cursor-pointer":
          Styles.cursorPointer(node);
          break;
        case "cursor-text":
          Styles.cursorText(node);
          break;
        case "cursor-wait":
          Styles.cursorWait(node);
          break;
        case "cursor-crosshair":
          Styles.cursorCrosshair(node);
          break;
        case "cursor-move":
          Styles.cursorMove(node);
          break;
        case "cursor-not-allowed":
          Styles.cursorNotAllowed(node);
          break;
        case "cursor-e-resize":
          Styles.cursorEResize(node);
          break;
        case "cursor-n-resize":
          Styles.cursorNResize(node);
          break;
        case "cursor-ne-resize":
          Styles.cursorNEResize(node);
          break;
        case "cursor-nw-resize":
          Styles.cursorNWResize(node);
          break;
        case "cursor-s-resize":
          Styles.cursorSResize(node);
          break;
        case "cursor-se-resize":
          Styles.cursorSEResize(node);
          break;
        case "cursor-sw-resize":
          Styles.cursorSWResize(node);
          break;
        case "cursor-w-resize":
          Styles.cursorWResize(node);
          break;
        case "cursor-help":
          Styles.cursorHelp(node);
          break;
        case "cursor-progress":
          Styles.cursorProgress(node);
          break;
      }
      return;
    }

    // Overflow utilities
    if (baseToken.startsWith("overflow-")) {
      switch (baseToken) {
        case "overflow-hidden":
          Styles.overflowHidden(node);
          break;
        case "overflow-visible":
          Styles.overflowVisible(node);
          break;
        case "overflow-scroll":
          Styles.overflowScroll(node);
          break;
        case "overflow-auto":
          Styles.overflowAuto(node);
          break;
      }
      return;
    }

    // Resize utilities
    if (baseToken.startsWith("resize")) {
      switch (baseToken) {
        case "resize-none":
          Styles.resizeNone(node);
          break;
        case "resize":
          Styles.resize(node);
          break;
        case "resize-x":
          Styles.resizeX(node);
          break;
        case "resize-y":
          Styles.resizeY(node);
          break;
      }
      return;
    }

    // Z-index utilities (z-0, z-10, z-20, etc.)
    if (baseToken.equals("z-auto")) {
      node.setViewOrder(0);
      return;
    }
    if (baseToken.startsWith("z-")) {
      try {
        int zIndex = Integer.parseInt(baseToken.substring(2));
        Styles.z(node, zIndex);
      } catch (NumberFormatException e) {
        // Ignorar si no es un número válido
      }
      return;
    }

    // Skew utilities (skew-x-6, skew-y-3, etc.) - requieren Transform
    if (baseToken.startsWith("skew-x-") || baseToken.startsWith("skew-y-")) {
      // Skew no tiene soporte directo en JavaFX CSS, se maneja vía Transform
      // Se deja como nota para implementación futura con Transforms.shear()
      return;
    }

    // Effect utilities (blur, brightness, contrast, grayscale, invert, sepia)
    if (baseToken.equals("grayscale") || baseToken.equals("grayscale-0")) {
      if (baseToken.equals("grayscale")) {
        Styles.grayscale(node);
      } else {
        Styles.grayscale0(node);
      }
      return;
    }
    if (baseToken.startsWith("brightness-")) {
      try {
        String val = baseToken.substring("brightness-".length());
        double factor = parseTailwindNumber(val);
        Styles.brightness(node, factor);
      } catch (Exception e) {
        // Ignorar si no se puede parsear
      }
      return;
    }
    if (baseToken.startsWith("contrast-")) {
      try {
        String val = baseToken.substring("contrast-".length());
        double factor = parseTailwindNumber(val);
        Styles.contrast(node, factor);
      } catch (Exception e) {
        // Ignorar
      }
      return;
    }
    if (baseToken.startsWith("blur-") || baseToken.equals("blur")) {
      // Blur requiere efecto específico
      return;
    }
    if (baseToken.startsWith("invert-") || baseToken.equals("invert")) {
      // Invert requiere ColorAdjust
      return;
    }
    if (baseToken.startsWith("sepia-") || baseToken.equals("sepia")) {
      // Sepia requiere ColorAdjust
      return;
    }
  }

  /** Parsea valores numéricos de Tailwind (ej. "75" → 0.75, "100" → 1.0, "150" → 1.5). */
  private static double parseTailwindNumber(String val) {
    try {
      int num = Integer.parseInt(val);
      return num / 100.0;
    } catch (NumberFormatException e) {
      return 1.0;
    }
  }

  /**
   * Elimina propiedades JIT del inline style de un nodo. Útil para deshacer estilos aplicados
   * dinámicamente.
   */
  public static void removeJit(Node node, String... tokens) {
    JitCompiler.BatchResult result = JitCompiler.compileBatch(tokens);

    if (result.hasInlineStyle()) {
      String cleaned = removeProperties(node.getStyle(), result.inlineStyle());
      node.setStyle(cleaned);
    }

    for (String cls : result.cssClasses()) {
      node.getStyleClass().remove(cls);
    }
  }

  /** Reemplaza completamente el inline style JIT (elimina el previo y aplica el nuevo). */
  public static void replaceJit(Node node, String... tokens) {
    node.setStyle("");
    node.getStyleClass().removeIf(cls -> !cls.isBlank());
    applyJit(node, tokens);
  }

  // Merge de inline styles
  /**
   * Mergea dos bloques de inline style. Las propiedades del bloque 'incoming' sobreescriben las del
   * 'existing'. Las propiedades en 'existing' que no están en 'incoming' se preservan.
   *
   * <p>merge("-fx-padding: 8px; -fx-opacity: 0.5;", "-fx-padding: 16px; -fx-font-size: 14px;") →
   * "-fx-font-size: 14px; -fx-opacity: 0.5; -fx-padding: 16px;"
   */
  public static String merge(String existing, String incoming) {
    Map<String, String> props = parseStyle(existing);
    props.putAll(parseStyle(incoming)); // incoming gana en conflictos
    return buildStyle(props);
  }

  /** Elimina del 'existing' todas las propiedades presentes en 'toRemove'. */
  static String removeProperties(String existing, String toRemove) {
    Map<String, String> props = parseStyle(existing);
    Set<String> keysToRemove = parseStyle(toRemove).keySet();
    props.keySet().removeAll(keysToRemove);
    return buildStyle(props);
  }

  // Parse y build de inline style string
  /** Parsea "-fx-padding: 16px; -fx-opacity: 0.5;" → {"fx-padding":"16px", ...} */
  public static Map<String, String> parseStyle(String style) {
    Map<String, String> map = new LinkedHashMap<>();
    if (style == null || style.isBlank()) {
      return map;
    }

    Matcher m = PROP_PATTERN.matcher(style);
    while (m.find()) {
      map.put(m.group(1).trim(), m.group(2).trim());
    }
    return map;
  }

  /** {"fx-padding":"16px", "-fx-opacity":"0.5"} → "-fx-opacity: 0.5; -fx-padding: 16px;" */
  public static String buildStyle(Map<String, String> props) {
    if (props.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    // Orden consistente para facilitar debugging
    new TreeMap<>(props).forEach((k, v) -> sb.append(k).append(": ").append(v).append("; "));
    return sb.toString().trim();
  }
}
