package io.github.yasmramos.tailwindfx.color;

import io.github.yasmramos.tailwindfx.core.Preconditions;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * ColorPalette — Paleta de colores Tailwind como valores RGB.
 *
 * <p>Convierte "blue-500" → "#3b82f6" (hex) o "59,130,246" (componentes RGB sin #) para poder construir
 * rgba(r,g,b,alpha) al aplicar opacidad /80.
 *
 * <p>Esta clase es la ÚNICA fuente de verdad para los valores de color en TailwindFX.
 * Las familias con escala tienen 11 shades (50-950), los colores planos tienen valor único.
 */
public final class ColorPalette {

  /** Array ordenado de shades estándar de TailwindCSS. */
  public static final int[] SHADES = {50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950};

  /** Lista ordenada de familias de colores con escala (excluye colores planos). */
  public static final String[] FAMILIES;

  /** Colores planos (sin escala de shades) según TailwindCSS. */
  public static final Map<String, String> NAMED_COLORS;

  static {
    // Inicializar NAMED_COLORS primero
    NAMED_COLORS = new HashMap<>();
    NAMED_COLORS.put("white", "#ffffff");
    NAMED_COLORS.put("black", "#000000");
    NAMED_COLORS.put("transparent", "transparent");

    // Construir FAMILIES en el orden de inserción del bloque estático
    Set<String> familiesSet = new LinkedHashSet<>();
    // Las familias se añaden al conjunto durante la inicialización del PALETTE
    // Se asignará después de que el bloque estático principal se ejecute
    FAMILIES = initializeFamilies(familiesSet);
  }

  private static String[] initializeFamilies(Set<String> familiesSet) {
    // Orden explícito consistente con el bloque estático
    familiesSet.add("slate");
    familiesSet.add("gray");
    familiesSet.add("red");
    familiesSet.add("orange");
    familiesSet.add("amber");
    familiesSet.add("yellow");
    familiesSet.add("lime");
    familiesSet.add("green");
    familiesSet.add("emerald");
    familiesSet.add("teal");
    familiesSet.add("cyan");
    familiesSet.add("sky");
    familiesSet.add("blue");
    familiesSet.add("indigo");
    familiesSet.add("violet");
    familiesSet.add("purple");
    familiesSet.add("fuchsia");
    familiesSet.add("pink");
    familiesSet.add("rose");
    familiesSet.add("zinc");
    familiesSet.add("neutral");
    familiesSet.add("stone");
    familiesSet.add("mauve");
    familiesSet.add("olive");
    familiesSet.add("mist");
    familiesSet.add("taupe");
    return familiesSet.toArray(new String[0]);
  }

  private ColorPalette() {}

  /** Devuelve el conjunto ordenado de familias de colores con escala. */
  public static String[] families() {
    return FAMILIES;
  }

  /** Devuelve el mapa de colores planos (nombre → hex/valor). */
  public static Map<String, String> namedColors() {
    return NAMED_COLORS;
  }

  /**
   * Devuelve el array de hex para una familia específica en el orden de SHADES.
   * @return array de 11 elementos o null si la familia no existe
   */
  public static String[] shadesOf(String family) {
    if (!isFamily(family)) {
      return null;
    }
    String[] result = new String[SHADES.length];
    for (int i = 0; i < SHADES.length; i++) {
      result[i] = hex(family, SHADES[i]);
    }
    return result;
  }

  /** Verifica si un nombre corresponde a una familia con escala. */
  public static boolean isFamily(String name) {
    for (String f : FAMILIES) {
      if (f.equals(name)) {
        return true;
      }
    }
    return false;
  }

  /** Verifica si un nombre corresponde a un color plano. */
  public static boolean isNamedColor(String name) {
    return NAMED_COLORS.containsKey(name);
  }

  // Public API

  /**
   * Devuelve el hex de un color: resolve("blue", 500) → "#3b82f6" Devuelve null si el color/shade
   * no existe.
   */
  public static String hex(String colorName, int shade) {
    String key = colorName + "-" + shade;
    return PALETTE.get(key);
  }

  /**
   * Devuelve componentes RGB separados por coma: rgb("blue", 500) → "59,130,246" Para construir
   * rgba(r,g,b,alpha).
   */
  public static String rgb(String colorName, int shade) {
    String h = hex(colorName, shade);
    if (h == null) return null;
    return hexToRgb(h);
  }

  /**
   * Construye el valor CSS completo para -fx-background-color / -fx-text-fill: - Sin alpha:
   * devuelve hex "#3b82f6" - Con alpha: devuelve "rgba(59,130,246,0.80)"
   */
  public static String fxColor(String colorName, int shade, Double alphaFraction) {
    // Fallback a gray-500 si el color no existe en la paleta
    if (!exists(colorName, shade)) {
      Preconditions.LOG.warning(
          "ColorPalette: color not found '"
              + colorName
              + "-"
              + shade
              + "' — usando gray-500 como fallback");
      colorName = "gray";
      shade = 500;
    }
    // Clamp alpha a [0.0, 1.0]
    if (alphaFraction != null && (alphaFraction < 0.0 || alphaFraction > 1.0)) {
      double clamped = Math.max(0.0, Math.min(1.0, alphaFraction));
      Preconditions.LOG.warning(
          "ColorPalette: alpha " + alphaFraction + " fuera de [0,1] — ajustado a " + clamped);
      alphaFraction = clamped;
    }
    if (alphaFraction == null || alphaFraction >= 1.0) {
      return hex(colorName, shade);
    }
    String rgb = rgb(colorName, shade);
    if (rgb == null) return null;
    return String.format("rgba(%s,%.2f)", rgb, alphaFraction);
  }

  public static boolean exists(String colorName, int shade) {
    return PALETTE.containsKey(colorName + "-" + shade);
  }

  // Validación y normalización de colores hexadecimales arbitrarios

  /** Verifica si un string es un color hex CSS válido. Acepta #RRGGBB y #RGB. */
  public static boolean isValidHex(String hex) {
    if (hex == null) return false;
    return hex.matches("^#[0-9A-Fa-f]{6}$") || hex.matches("^#[0-9A-Fa-f]{3}$");
  }

  /**
   * Normaliza un hex corto a 6 dígitos: #f60 → #ff6600. Si ya tiene 6 dígitos o es null/inválido lo
   * devuelve sin cambios.
   */
  public static String normalizeHex(String hex) {
    if (hex == null) return null;
    if (hex.matches("^#[0-9A-Fa-f]{3}$")) {
      char r = hex.charAt(1), g = hex.charAt(2), b = hex.charAt(3);
      return String.format("#%c%c%c%c%c%c", r, r, g, g, b, b);
    }
    return hex;
  }

  /**
   * Convierte un hex arbitrario a "r,g,b" para construir rgba(). Acepta #RGB y #RRGGBB. Devuelve
   * null si el hex es inválido.
   */
  public static String hexToRgbString(String hex) {
    String n = normalizeHex(hex);
    if (!isValidHex(n)) return null;
    return hexToRgb(n);
  }

  // Conversión hex → RGB

  private static String hexToRgb(String hex) {
    String h = hex.startsWith("#") ? hex.substring(1) : hex;
    int r = Integer.parseInt(h.substring(0, 2), 16);
    int g = Integer.parseInt(h.substring(2, 4), 16);
    int b = Integer.parseInt(h.substring(4, 6), 16);
    return r + "," + g + "," + b;
  }

  // Paleta completa Tailwind (mismos valores que en tailwindfx.css)

  private static final Map<String, String> PALETTE = new HashMap<>(600);

  static {
    // Slate
    put("slate", 50, "#f8fafc");
    put("slate", 100, "#f1f5f9");
    put("slate", 200, "#e2e8f0");
    put("slate", 300, "#cbd5e1");
    put("slate", 400, "#94a3b8");
    put("slate", 500, "#64748b");
    put("slate", 600, "#475569");
    put("slate", 700, "#334155");
    put("slate", 800, "#1e293b");
    put("slate", 900, "#0f172a");
    put("slate", 950, "#020617");

    // Gray
    put("gray", 50, "#f9fafb");
    put("gray", 100, "#f3f4f6");
    put("gray", 200, "#e5e7eb");
    put("gray", 300, "#d1d5db");
    put("gray", 400, "#9ca3af");
    put("gray", 500, "#6b7280");
    put("gray", 600, "#4b5563");
    put("gray", 700, "#374151");
    put("gray", 800, "#1f2937");
    put("gray", 900, "#111827");
    put("gray", 950, "#030712");

    // Red
    put("red", 50, "#fef2f2");
    put("red", 100, "#fee2e2");
    put("red", 200, "#fecaca");
    put("red", 300, "#fca5a5");
    put("red", 400, "#f87171");
    put("red", 500, "#ef4444");
    put("red", 600, "#dc2626");
    put("red", 700, "#b91c1c");
    put("red", 800, "#991b1b");
    put("red", 900, "#7f1d1d");
    put("red", 950, "#450a0a");

    // Orange
    put("orange", 50, "#fff7ed");
    put("orange", 100, "#ffedd5");
    put("orange", 200, "#fed7aa");
    put("orange", 300, "#fdba74");
    put("orange", 400, "#fb923c");
    put("orange", 500, "#f97316");
    put("orange", 600, "#ea580c");
    put("orange", 700, "#c2410c");
    put("orange", 800, "#9a3412");
    put("orange", 900, "#7c2d12");
    put("orange", 950, "#431407");

    // Amber
    put("amber", 50, "#fffbeb");
    put("amber", 100, "#fef3c7");
    put("amber", 200, "#fde68a");
    put("amber", 300, "#fcd34d");
    put("amber", 400, "#fbbf24");
    put("amber", 500, "#f59e0b");
    put("amber", 600, "#d97706");
    put("amber", 700, "#b45309");
    put("amber", 800, "#92400e");
    put("amber", 900, "#78350f");
    put("amber", 950, "#451a03");

    // Yellow
    put("yellow", 50, "#fefce8");
    put("yellow", 100, "#fef9c3");
    put("yellow", 200, "#fef08a");
    put("yellow", 300, "#fde047");
    put("yellow", 400, "#facc15");
    put("yellow", 500, "#eab308");
    put("yellow", 600, "#ca8a04");
    put("yellow", 700, "#a16207");
    put("yellow", 800, "#854d0e");
    put("yellow", 900, "#713f12");
    put("yellow", 950, "#422006");

    // Lime
    put("lime", 50, "#f7fee7");
    put("lime", 100, "#ecfccb");
    put("lime", 200, "#d9f99d");
    put("lime", 300, "#bef264");
    put("lime", 400, "#a3e635");
    put("lime", 500, "#84cc16");
    put("lime", 600, "#65a30d");
    put("lime", 700, "#4d7c0f");
    put("lime", 800, "#3f6212");
    put("lime", 900, "#365314");
    put("lime", 950, "#1a2e05");

    // Green
    put("green", 50, "#f0fdf4");
    put("green", 100, "#dcfce7");
    put("green", 200, "#bbf7d0");
    put("green", 300, "#86efac");
    put("green", 400, "#4ade80");
    put("green", 500, "#22c55e");
    put("green", 600, "#16a34a");
    put("green", 700, "#15803d");
    put("green", 800, "#166534");
    put("green", 900, "#14532d");
    put("green", 950, "#052e16");

    // Emerald
    put("emerald", 50, "#ecfdf5");
    put("emerald", 100, "#d1fae5");
    put("emerald", 200, "#a7f3d0");
    put("emerald", 300, "#6ee7b7");
    put("emerald", 400, "#34d399");
    put("emerald", 500, "#10b981");
    put("emerald", 600, "#059669");
    put("emerald", 700, "#047857");
    put("emerald", 800, "#065f46");
    put("emerald", 900, "#064e3b");
    put("emerald", 950, "#022c22");

    // Teal
    put("teal", 50, "#f0fdfa");
    put("teal", 100, "#ccfbf1");
    put("teal", 200, "#99f6e4");
    put("teal", 300, "#5eead4");
    put("teal", 400, "#2dd4bf");
    put("teal", 500, "#14b8a6");
    put("teal", 600, "#0d9488");
    put("teal", 700, "#0f766e");
    put("teal", 800, "#115e59");
    put("teal", 900, "#134e4a");
    put("teal", 950, "#042f2e");

    // Cyan
    put("cyan", 50, "#ecfeff");
    put("cyan", 100, "#cffafe");
    put("cyan", 200, "#a5f3fc");
    put("cyan", 300, "#67e8f9");
    put("cyan", 400, "#22d3ee");
    put("cyan", 500, "#06b6d4");
    put("cyan", 600, "#0891b2");
    put("cyan", 700, "#0e7490");
    put("cyan", 800, "#155e75");
    put("cyan", 900, "#164e63");
    put("cyan", 950, "#083344");

    // Sky
    put("sky", 50, "#f0f9ff");
    put("sky", 100, "#e0f2fe");
    put("sky", 200, "#bae6fd");
    put("sky", 300, "#7dd3fc");
    put("sky", 400, "#38bdf8");
    put("sky", 500, "#0ea5e9");
    put("sky", 600, "#0284c7");
    put("sky", 700, "#0369a1");
    put("sky", 800, "#075985");
    put("sky", 900, "#0c4a6e");
    put("sky", 950, "#082f49");

    // Blue
    put("blue", 50, "#eff6ff");
    put("blue", 100, "#dbeafe");
    put("blue", 200, "#bfdbfe");
    put("blue", 300, "#93c5fd");
    put("blue", 400, "#60a5fa");
    put("blue", 500, "#3b82f6");
    put("blue", 600, "#2563eb");
    put("blue", 700, "#1d4ed8");
    put("blue", 800, "#1e40af");
    put("blue", 900, "#1e3a8a");
    put("blue", 950, "#172554");

    // Indigo
    put("indigo", 50, "#eef2ff");
    put("indigo", 100, "#e0e7ff");
    put("indigo", 200, "#c7d2fe");
    put("indigo", 300, "#a5b4fc");
    put("indigo", 400, "#818cf8");
    put("indigo", 500, "#6366f1");
    put("indigo", 600, "#4f46e5");
    put("indigo", 700, "#4338ca");
    put("indigo", 800, "#3730a3");
    put("indigo", 900, "#312e81");
    put("indigo", 950, "#1e1b4b");

    // Violet
    put("violet", 50, "#f5f3ff");
    put("violet", 100, "#ede9fe");
    put("violet", 200, "#ddd6fe");
    put("violet", 300, "#c4b5fd");
    put("violet", 400, "#a78bfa");
    put("violet", 500, "#8b5cf6");
    put("violet", 600, "#7c3aed");
    put("violet", 700, "#6d28d9");
    put("violet", 800, "#5b21b6");
    put("violet", 900, "#4c1d95");
    put("violet", 950, "#2e1065");

    // Purple
    put("purple", 50, "#faf5ff");
    put("purple", 100, "#f3e8ff");
    put("purple", 200, "#e9d5ff");
    put("purple", 300, "#d8b4fe");
    put("purple", 400, "#c084fc");
    put("purple", 500, "#a855f7");
    put("purple", 600, "#9333ea");
    put("purple", 700, "#7e22ce");
    put("purple", 800, "#6b21a8");
    put("purple", 900, "#581c87");
    put("purple", 950, "#3b0764");

    // Fuchsia
    put("fuchsia", 50, "#fdf4ff");
    put("fuchsia", 100, "#fae8ff");
    put("fuchsia", 200, "#f5d0fe");
    put("fuchsia", 300, "#f0abfc");
    put("fuchsia", 400, "#e879f9");
    put("fuchsia", 500, "#d946ef");
    put("fuchsia", 600, "#c026d3");
    put("fuchsia", 700, "#a21caf");
    put("fuchsia", 800, "#86198f");
    put("fuchsia", 900, "#701a75");
    put("fuchsia", 950, "#4a044e");

    // Pink
    put("pink", 50, "#fdf2f8");
    put("pink", 100, "#fce7f3");
    put("pink", 200, "#fbcfe8");
    put("pink", 300, "#f9a8d4");
    put("pink", 400, "#f472b6");
    put("pink", 500, "#ec4899");
    put("pink", 600, "#db2777");
    put("pink", 700, "#be185d");
    put("pink", 800, "#9d174d");
    put("pink", 900, "#831843");
    put("pink", 950, "#500724");

    // Rose
    put("rose", 50, "#fff1f2");
    put("rose", 100, "#ffe4e6");
    put("rose", 200, "#fecdd3");
    put("rose", 300, "#fda4af");
    put("rose", 400, "#fb7185");
    put("rose", 500, "#f43f5e");
    put("rose", 600, "#e11d48");
    put("rose", 700, "#be123c");
    put("rose", 800, "#9f1239");
    put("rose", 900, "#881337");
    put("rose", 950, "#4c0519");

    // Zinc (Tailwind standard neutral gray)
    put("zinc", 50, "#fafafa");
    put("zinc", 100, "#f4f4f5");
    put("zinc", 200, "#e4e4e7");
    put("zinc", 300, "#d4d4d8");
    put("zinc", 400, "#a1a1aa");
    put("zinc", 500, "#71717a");
    put("zinc", 600, "#52525b");
    put("zinc", 700, "#3f3f46");
    put("zinc", 800, "#27272a");
    put("zinc", 900, "#18181b");
    put("zinc", 950, "#09090b");

    // Neutral (Tailwind standard neutral gray)
    put("neutral", 50, "#fafafa");
    put("neutral", 100, "#f5f5f5");
    put("neutral", 200, "#e5e5e5");
    put("neutral", 300, "#d4d4d4");
    put("neutral", 400, "#a3a3a3");
    put("neutral", 500, "#737373");
    put("neutral", 600, "#525252");
    put("neutral", 700, "#404040");
    put("neutral", 800, "#262626");
    put("neutral", 900, "#171717");
    put("neutral", 950, "#0a0a0a");

    // Stone (Tailwind standard neutral gray)
    put("stone", 50, "#fafaf9");
    put("stone", 100, "#f5f5f4");
    put("stone", 200, "#e7e5e4");
    put("stone", 300, "#d6d3d1");
    put("stone", 400, "#a8a29e");
    put("stone", 500, "#78716c");
    put("stone", 600, "#57534e");
    put("stone", 700, "#44403c");
    put("stone", 800, "#292524");
    put("stone", 900, "#1c1917");
    put("stone", 950, "#0c0a09");

    // Mauve (custom purple-gray tones)
    put("mauve", 50, "#f7f5fa");
    put("mauve", 100, "#efeaf5");
    put("mauve", 200, "#ded6e9");
    put("mauve", 300, "#cbc0db");
    put("mauve", 400, "#b3a5c9");
    put("mauve", 500, "#9a88b7");
    put("mauve", 600, "#846fa6");
    put("mauve", 700, "#715991");
    put("mauve", 800, "#5f487a");
    put("mauve", 900, "#4d3a63");
    put("mauve", 950, "#2a1f3a");

    // Olive (custom green-yellow muted tones)
    put("olive", 50, "#fcfce8");
    put("olive", 100, "#f8f8d1");
    put("olive", 200, "#efefad");
    put("olive", 300, "#e6e68a");
    put("olive", 400, "#dddd60");
    put("olive", 500, "#d4d438");
    put("olive", 600, "#bfbf2e");
    put("olive", 700, "#a9a927");
    put("olive", 800, "#8f8f23");
    put("olive", 900, "#757520");
    put("olive", 950, "#3d3d12");

    // Mist (custom blue-cyan very light tones)
    put("mist", 50, "#f4fbfc");
    put("mist", 100, "#e8f7f9");
    put("mist", 200, "#d0eff3");
    put("mist", 300, "#b8e7ed");
    put("mist", 400, "#9bdce6");
    put("mist", 500, "#7ed0df");
    put("mist", 600, "#62c3d8");
    put("mist", 700, "#46b5d0");
    put("mist", 800, "#2ba6c7");
    put("mist", 900, "#1298be");
    put("mist", 950, "#0a6f8a");

    // Taupe (custom brown-gray warm tones)
    put("taupe", 50, "#f9f7f5");
    put("taupe", 100, "#f3efeb");
    put("taupe", 200, "#e7e1d9");
    put("taupe", 300, "#dad3c8");
    put("taupe", 400, "#c9bfb0");
    put("taupe", 500, "#b8aba0");
    put("taupe", 600, "#a5968b");
    put("taupe", 700, "#918075");
    put("taupe", 800, "#7a6b61");
    put("taupe", 900, "#63574e");
    put("taupe", 950, "#3a322d");
  }

  private static void put(String name, int shade, String hex) {
    PALETTE.put(name + "-" + shade, hex);
  }
}
