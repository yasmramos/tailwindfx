package io.github.yasmramos.tailwindfx.color;

import io.github.yasmramos.tailwindfx.core.Preconditions;
import java.util.HashMap;
import java.util.Map;

/**
 * ColorPalette — Paleta de colores Tailwind como valores RGB.
 *
 * Convierte "blue-500" → "59,130,246" (componentes RGB sin #)
 * para poder construir rgba(r,g,b,alpha) al aplicar opacidad /80.
 *
 * También convierte shade a hex para casos sin opacidad.
 */
public final class ColorPalette {

    private ColorPalette() {}

    // =========================================================================
    // API pública
    // =========================================================================

    /**
     * Devuelve el hex de un color: resolve("blue", 500) → "#3b82f6"
     * Devuelve null si el color/shade no existe.
     */
    public static String hex(String colorName, int shade) {
        String key = colorName + "-" + shade;
        return PALETTE.get(key);
    }

    /**
     * Devuelve componentes RGB separados por coma: rgb("blue", 500) → "59,130,246"
     * Para construir rgba(r,g,b,alpha).
     */
    public static String rgb(String colorName, int shade) {
        String h = hex(colorName, shade);
        if (h == null) return null;
        return hexToRgb(h);
    }

    /**
     * Construye el valor CSS completo para -fx-background-color / -fx-text-fill:
     *   - Sin alpha: devuelve hex "#3b82f6"
     *   - Con alpha: devuelve "rgba(59,130,246,0.80)"
     */
    public static String fxColor(String colorName, int shade, Double alphaFraction) {
        // Fallback a gray-500 si el color no existe en la paleta
        if (!exists(colorName, shade)) {
            Preconditions.LOG.warning(
                "ColorPalette: color not found '" + colorName + "-" + shade
                + "' — usando gray-500 como fallback");
            colorName = "gray";
            shade = 500;
        }
        // Clamp alpha a [0.0, 1.0]
        if (alphaFraction != null && (alphaFraction < 0.0 || alphaFraction > 1.0)) {
            double clamped = Math.max(0.0, Math.min(1.0, alphaFraction));
            Preconditions.LOG.warning(
                "ColorPalette: alpha " + alphaFraction
                + " fuera de [0,1] — ajustado a " + clamped);
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

    // =========================================================================
    // Validación y normalización de colores hexadecimales arbitrarios
    // =========================================================================

    /**
     * Verifica si un string es un color hex CSS válido.
     * Acepta #RRGGBB y #RGB.
     */
    public static boolean isValidHex(String hex) {
        if (hex == null) return false;
        return hex.matches("^#[0-9A-Fa-f]{6}$") || hex.matches("^#[0-9A-Fa-f]{3}$");
    }

    /**
     * Normaliza un hex corto a 6 dígitos: #f60 → #ff6600.
     * Si ya tiene 6 dígitos o es null/inválido lo devuelve sin cambios.
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
     * Convierte un hex arbitrario a "r,g,b" para construir rgba().
     * Acepta #RGB y #RRGGBB. Devuelve null si el hex es inválido.
     */
    public static String hexToRgbString(String hex) {
        String n = normalizeHex(hex);
        if (!isValidHex(n)) return null;
        return hexToRgb(n);
    }

    // =========================================================================
    // Conversión hex → RGB
    // =========================================================================

    private static String hexToRgb(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        int r = Integer.parseInt(h.substring(0, 2), 16);
        int g = Integer.parseInt(h.substring(2, 4), 16);
        int b = Integer.parseInt(h.substring(4, 6), 16);
        return r + "," + g + "," + b;
    }

    // =========================================================================
    // Paleta completa Tailwind (mismos valores que en tailwindfx.css)
    // =========================================================================

    private static final Map<String, String> PALETTE = new HashMap<>(600);

    static {
        // Slate
        put("slate", 50,"#f8fafc"); put("slate",100,"#f1f5f9"); put("slate",200,"#e2e8f0");
        put("slate",300,"#cbd5e1"); put("slate",400,"#94a3b8"); put("slate",500,"#64748b");
        put("slate",600,"#475569"); put("slate",700,"#334155"); put("slate",800,"#1e293b");
        put("slate",900,"#0f172a"); put("slate",950,"#020617");

        // Gray
        put("gray", 50,"#f9fafb"); put("gray",100,"#f3f4f6"); put("gray",200,"#e5e7eb");
        put("gray",300,"#d1d5db"); put("gray",400,"#9ca3af"); put("gray",500,"#6b7280");
        put("gray",600,"#4b5563"); put("gray",700,"#374151"); put("gray",800,"#1f2937");
        put("gray",900,"#111827"); put("gray",950,"#030712");

        // Red
        put("red", 50,"#fef2f2"); put("red",100,"#fee2e2"); put("red",200,"#fecaca");
        put("red",300,"#fca5a5"); put("red",400,"#f87171"); put("red",500,"#ef4444");
        put("red",600,"#dc2626"); put("red",700,"#b91c1c"); put("red",800,"#991b1b");
        put("red",900,"#7f1d1d"); put("red",950,"#450a0a");

        // Orange
        put("orange", 50,"#fff7ed"); put("orange",100,"#ffedd5"); put("orange",200,"#fed7aa");
        put("orange",300,"#fdba74"); put("orange",400,"#fb923c"); put("orange",500,"#f97316");
        put("orange",600,"#ea580c"); put("orange",700,"#c2410c"); put("orange",800,"#9a3412");
        put("orange",900,"#7c2d12"); put("orange",950,"#431407");

        // Amber
        put("amber", 50,"#fffbeb"); put("amber",100,"#fef3c7"); put("amber",200,"#fde68a");
        put("amber",300,"#fcd34d"); put("amber",400,"#fbbf24"); put("amber",500,"#f59e0b");
        put("amber",600,"#d97706"); put("amber",700,"#b45309"); put("amber",800,"#92400e");
        put("amber",900,"#78350f"); put("amber",950,"#451a03");

        // Yellow
        put("yellow", 50,"#fefce8"); put("yellow",100,"#fef9c3"); put("yellow",200,"#fef08a");
        put("yellow",300,"#fde047"); put("yellow",400,"#facc15"); put("yellow",500,"#eab308");
        put("yellow",600,"#ca8a04"); put("yellow",700,"#a16207"); put("yellow",800,"#854d0e");
        put("yellow",900,"#713f12"); put("yellow",950,"#422006");

        // Lime
        put("lime", 50,"#f7fee7"); put("lime",100,"#ecfccb"); put("lime",200,"#d9f99d");
        put("lime",300,"#bef264"); put("lime",400,"#a3e635"); put("lime",500,"#84cc16");
        put("lime",600,"#65a30d"); put("lime",700,"#4d7c0f"); put("lime",800,"#3f6212");
        put("lime",900,"#365314"); put("lime",950,"#1a2e05");

        // Green
        put("green", 50,"#f0fdf4"); put("green",100,"#dcfce7"); put("green",200,"#bbf7d0");
        put("green",300,"#86efac"); put("green",400,"#4ade80"); put("green",500,"#22c55e");
        put("green",600,"#16a34a"); put("green",700,"#15803d"); put("green",800,"#166534");
        put("green",900,"#14532d"); put("green",950,"#052e16");

        // Emerald
        put("emerald", 50,"#ecfdf5"); put("emerald",100,"#d1fae5"); put("emerald",200,"#a7f3d0");
        put("emerald",300,"#6ee7b7"); put("emerald",400,"#34d399"); put("emerald",500,"#10b981");
        put("emerald",600,"#059669"); put("emerald",700,"#047857"); put("emerald",800,"#065f46");
        put("emerald",900,"#064e3b"); put("emerald",950,"#022c22");

        // Teal
        put("teal", 50,"#f0fdfa"); put("teal",100,"#ccfbf1"); put("teal",200,"#99f6e4");
        put("teal",300,"#5eead4"); put("teal",400,"#2dd4bf"); put("teal",500,"#14b8a6");
        put("teal",600,"#0d9488"); put("teal",700,"#0f766e"); put("teal",800,"#115e59");
        put("teal",900,"#134e4a"); put("teal",950,"#042f2e");

        // Cyan
        put("cyan", 50,"#ecfeff"); put("cyan",100,"#cffafe"); put("cyan",200,"#a5f3fc");
        put("cyan",300,"#67e8f9"); put("cyan",400,"#22d3ee"); put("cyan",500,"#06b6d4");
        put("cyan",600,"#0891b2"); put("cyan",700,"#0e7490"); put("cyan",800,"#155e75");
        put("cyan",900,"#164e63"); put("cyan",950,"#083344");

        // Sky
        put("sky", 50,"#f0f9ff"); put("sky",100,"#e0f2fe"); put("sky",200,"#bae6fd");
        put("sky",300,"#7dd3fc"); put("sky",400,"#38bdf8"); put("sky",500,"#0ea5e9");
        put("sky",600,"#0284c7"); put("sky",700,"#0369a1"); put("sky",800,"#075985");
        put("sky",900,"#0c4a6e"); put("sky",950,"#082f49");

        // Blue
        put("blue", 50,"#eff6ff"); put("blue",100,"#dbeafe"); put("blue",200,"#bfdbfe");
        put("blue",300,"#93c5fd"); put("blue",400,"#60a5fa"); put("blue",500,"#3b82f6");
        put("blue",600,"#2563eb"); put("blue",700,"#1d4ed8"); put("blue",800,"#1e40af");
        put("blue",900,"#1e3a8a"); put("blue",950,"#172554");

        // Indigo
        put("indigo", 50,"#eef2ff"); put("indigo",100,"#e0e7ff"); put("indigo",200,"#c7d2fe");
        put("indigo",300,"#a5b4fc"); put("indigo",400,"#818cf8"); put("indigo",500,"#6366f1");
        put("indigo",600,"#4f46e5"); put("indigo",700,"#4338ca"); put("indigo",800,"#3730a3");
        put("indigo",900,"#312e81"); put("indigo",950,"#1e1b4b");

        // Violet
        put("violet", 50,"#f5f3ff"); put("violet",100,"#ede9fe"); put("violet",200,"#ddd6fe");
        put("violet",300,"#c4b5fd"); put("violet",400,"#a78bfa"); put("violet",500,"#8b5cf6");
        put("violet",600,"#7c3aed"); put("violet",700,"#6d28d9"); put("violet",800,"#5b21b6");
        put("violet",900,"#4c1d95"); put("violet",950,"#2e1065");

        // Purple
        put("purple", 50,"#faf5ff"); put("purple",100,"#f3e8ff"); put("purple",200,"#e9d5ff");
        put("purple",300,"#d8b4fe"); put("purple",400,"#c084fc"); put("purple",500,"#a855f7");
        put("purple",600,"#9333ea"); put("purple",700,"#7e22ce"); put("purple",800,"#6b21a8");
        put("purple",900,"#581c87"); put("purple",950,"#3b0764");

        // Fuchsia
        put("fuchsia", 50,"#fdf4ff"); put("fuchsia",100,"#fae8ff"); put("fuchsia",200,"#f5d0fe");
        put("fuchsia",300,"#f0abfc"); put("fuchsia",400,"#e879f9"); put("fuchsia",500,"#d946ef");
        put("fuchsia",600,"#c026d3"); put("fuchsia",700,"#a21caf"); put("fuchsia",800,"#86198f");
        put("fuchsia",900,"#701a75"); put("fuchsia",950,"#4a044e");

        // Pink
        put("pink", 50,"#fdf2f8"); put("pink",100,"#fce7f3"); put("pink",200,"#fbcfe8");
        put("pink",300,"#f9a8d4"); put("pink",400,"#f472b6"); put("pink",500,"#ec4899");
        put("pink",600,"#db2777"); put("pink",700,"#be185d"); put("pink",800,"#9d174d");
        put("pink",900,"#831843"); put("pink",950,"#500724");

        // Rose
        put("rose", 50,"#fff1f2"); put("rose",100,"#ffe4e6"); put("rose",200,"#fecdd3");
        put("rose",300,"#fda4af"); put("rose",400,"#fb7185"); put("rose",500,"#f43f5e");
        put("rose",600,"#e11d48"); put("rose",700,"#be123c"); put("rose",800,"#9f1239");
        put("rose",900,"#881337"); put("rose",950,"#4c0519");

        // White / Black
        PALETTE.put("white-0",   "#ffffff");
        PALETTE.put("black-0",   "#000000");
    }

    private static void put(String name, int shade, String hex) {
        PALETTE.put(name + "-" + shade, hex);
    }
}
