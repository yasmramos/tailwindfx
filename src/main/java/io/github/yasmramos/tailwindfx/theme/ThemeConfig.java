package io.github.yasmramos.tailwindfx.theme;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * ThemeConfig — Configuración centralizada del sistema de diseño.
 *
 * <p>Equivalente JavaFX de tailwind.config.js. Define colores, spacing, breakpoints, border-radius,
 * sombras y otras tokens de diseño.
 *
 * <p>Uso:
 *
 * <pre>
 *   ThemeConfig config = ThemeConfig.defaultConfig();
 *   String color = config.color("blue", 500);
 *   double spacing = config.spacing(4);
 *   int breakpoint = config.breakpoint("md");
 * </pre>
 */
public final class ThemeConfig {

  private static ThemeConfig instance;

  private final Map<String, String[]> colors;
  private final double[] spacing;
  private final Map<String, Integer> breakpoints;
  private final double[] borderRadius;
  private final Map<String, String> shadows;
  private final Map<String, Double> opacity;
  private final Map<String, String> fontFamily;
  private final Map<String, Double> fontSize;
  private final Map<String, String> fontWeight;

  private ThemeConfig(Builder builder) {
    this.colors = Collections.unmodifiableMap(new HashMap<>(builder.colors));
    this.spacing = builder.spacing.clone();
    this.breakpoints = Collections.unmodifiableMap(new TreeMap<>(builder.breakpoints));
    this.borderRadius = builder.borderRadius.clone();
    this.shadows = Collections.unmodifiableMap(new HashMap<>(builder.shadows));
    this.opacity = Collections.unmodifiableMap(new HashMap<>(builder.opacity));
    this.fontFamily = Collections.unmodifiableMap(new HashMap<>(builder.fontFamily));
    this.fontSize = Collections.unmodifiableMap(new HashMap<>(builder.fontSize));
    this.fontWeight = Collections.unmodifiableMap(new HashMap<>(builder.fontWeight));
  }

  /** Obtiene la instancia singleton de configuración por defecto. */
  public static ThemeConfig defaultConfig() {
    if (instance == null) {
      instance = new Builder().build();
    }
    return instance;
  }

  /** Resetea la instancia singleton (útil para testing). */
  public static void reset() {
    instance = null;
  }

  /** Obtiene un color específico. Ej: color("blue", 500) → "#3b82f6" */
  public String color(String name, int shade) {
    String[] shades = colors.get(name);
    if (shades == null) {
      return null;
    }
    // Mapeo correcto de shade a índice: 50→0, 100→1, 200→2, 300→3, 400→4, 500→5, 600→6, 700→7,
    // 800→8, 900→9, 950→10
    int index;
    if (shade == 50) index = 0;
    else if (shade == 100) index = 1;
    else if (shade == 200) index = 2;
    else if (shade == 300) index = 3;
    else if (shade == 400) index = 4;
    else if (shade == 500) index = 5;
    else if (shade == 600) index = 6;
    else if (shade == 700) index = 7;
    else if (shade == 800) index = 8;
    else if (shade == 900) index = 9;
    else if (shade == 950) index = 10;
    else return null;

    if (index < 0 || index >= shades.length) {
      return null;
    }
    return shades[index];
  }

  /** Obtiene el array completo de colores para una familia. */
  public String[] colorFamily(String name) {
    return colors.get(name);
  }

  /** Obtiene todos los colores configurados. */
  public Map<String, String[]> colors() {
    return colors;
  }

  /** Obtiene un valor de spacing. Ej: spacing(4) → 16.0 (4 * 4px) */
  public double spacing(int value) {
    if (value < 0 || value >= spacing.length) {
      return value * 4.0; // Fallback: multiplier base
    }
    return spacing[value];
  }

  /** Obtiene el array completo de spacing. */
  public double[] spacing() {
    return spacing.clone();
  }

  /** Obtiene un breakpoint. Ej: breakpoint("md") → 768 */
  public Integer breakpoint(String name) {
    return breakpoints.get(name);
  }

  /** Obtiene todos los breakpoints. */
  public Map<String, Integer> breakpoints() {
    return breakpoints;
  }

  /** Obtiene border-radius. Ej: borderRadius(4) → 16.0 */
  public double borderRadius(int value) {
    if (value < 0 || value >= borderRadius.length) {
      return value * 4.0;
    }
    return borderRadius[value];
  }

  /** Obtiene un shadow predefinido. Ej: shadow("md") → "0 4px 6px -1px rgba(0,0,0,0.1)" */
  public String shadow(String name) {
    return shadows.get(name);
  }

  /** Obtiene todos los shadows. */
  public Map<String, String> shadows() {
    return shadows;
  }

  /** Obtiene opacidad. Ej: opacity(50) → 0.5 */
  public Double opacity(String name) {
    return opacity.get(name);
  }

  /** Obtiene todos los valores de opacidad. */
  public Map<String, Double> opacity() {
    return opacity;
  }

  /** Obtiene una fuente. Ej: fontFamily("sans") → "Inter, system-ui, sans-serif" */
  public String fontFamily(String name) {
    return fontFamily.get(name);
  }

  /** Obtiene todas las fuentes. */
  public Map<String, String> fontFamily() {
    return fontFamily;
  }

  /** Obtiene tamaño de fuente. Ej: fontSize("lg") → 18.0 */
  public Double fontSize(String name) {
    return fontSize.get(name);
  }

  /** Obtiene todos los tamaños de fuente. */
  public Map<String, Double> fontSize() {
    return fontSize;
  }

  /** Obtiene peso de fuente. Ej: fontWeight("bold") → "700" */
  public String fontWeight(String name) {
    return fontWeight.get(name);
  }

  /** Obtiene todos los pesos de fuente. */
  public Map<String, String> fontWeight() {
    return fontWeight;
  }

  /** Builder para crear configuraciones personalizadas. */
  public static final class Builder {
    private final Map<String, String[]> colors = new HashMap<>();
    private double[] spacing = new double[65];
    private final Map<String, Integer> breakpoints = new TreeMap<>();
    private double[] borderRadius = new double[17];
    private final Map<String, String> shadows = new HashMap<>();
    private final Map<String, Double> opacity = new HashMap<>();
    private final Map<String, String> fontFamily = new HashMap<>();
    private final Map<String, Double> fontSize = new HashMap<>();
    private final Map<String, String> fontWeight = new HashMap<>();

    public Builder() {
      initDefaults();
    }

    private void initDefaults() {
      // Colores Tailwind CSS por defecto
      colors.put(
          "slate",
          new String[] {
            "#f8fafc", "#f1f5f9", "#e2e8f0", "#cbd5e1", "#94a3b8", "#64748b", "#475569", "#334155",
            "#1e293b", "#0f172a", "#020617"
          });
      colors.put(
          "gray",
          new String[] {
            "#f9fafb", "#f3f4f6", "#e5e7eb", "#d1d5db", "#9ca3af", "#6b7280", "#4b5563", "#374151",
            "#1f2937", "#111827", "#030712"
          });
      colors.put(
          "red",
          new String[] {
            "#fef2f2", "#fee2e2", "#fecaca", "#fca5a5", "#f87171", "#ef4444", "#dc2626", "#b91c1c",
            "#991b1b", "#7f1d1d", "#450a0a"
          });
      colors.put(
          "orange",
          new String[] {
            "#fff7ed", "#ffedd5", "#fed7aa", "#fdba74", "#fb923c", "#f97316", "#ea580c", "#c2410c",
            "#9a3412", "#7c2d12", "#431407"
          });
      colors.put(
          "amber",
          new String[] {
            "#fffbeb", "#fef3c7", "#fde68a", "#fcd34d", "#fbbf24", "#f59e0b", "#d97706", "#b45309",
            "#92400e", "#78350f", "#451a03"
          });
      colors.put(
          "yellow",
          new String[] {
            "#fefce8", "#fef9c3", "#fef08a", "#fde047", "#facc15", "#eab308", "#ca8a04", "#a16207",
            "#854d0e", "#713f12", "#422006"
          });
      colors.put(
          "lime",
          new String[] {
            "#f7fee7", "#ecfccb", "#d9f99d", "#bef264", "#a3e635", "#84cc16", "#65a30d", "#4d7c0f",
            "#3f6212", "#365314", "#1a2e05"
          });
      colors.put(
          "green",
          new String[] {
            "#f0fdf4", "#dcfce7", "#bbf7d0", "#86efac", "#4ade80", "#22c55e", "#16a34a", "#15803d",
            "#166534", "#14532d", "#052e16"
          });
      colors.put(
          "emerald",
          new String[] {
            "#ecfdf5", "#d1fae5", "#a7f3d0", "#6ee7b7", "#34d399", "#10b981", "#059669", "#047857",
            "#065f46", "#064e3b", "#022c22"
          });
      colors.put(
          "teal",
          new String[] {
            "#f0fdfa", "#ccfbf1", "#99f6e4", "#5eead4", "#2dd4bf", "#14b8a6", "#0d9488", "#0f766e",
            "#115e59", "#134e4a", "#042f2e"
          });
      colors.put(
          "cyan",
          new String[] {
            "#ecfeff", "#cffafe", "#a5f3fc", "#67e8f9", "#22d3ee", "#06b6d4", "#0891b2", "#0e7490",
            "#155e75", "#164e63", "#083344"
          });
      colors.put(
          "sky",
          new String[] {
            "#f0f9ff", "#e0f2fe", "#bae6fd", "#7dd3fc", "#38bdf8", "#0ea5e9", "#0284c7", "#0369a1",
            "#075985", "#0c4a6e", "#082f49"
          });
      colors.put(
          "blue",
          new String[] {
            "#eff6ff", "#dbeafe", "#bfdbfe", "#93c5fd", "#60a5fa", "#3b82f6", "#2563eb", "#1d4ed8",
            "#1e40af", "#1e3a8a", "#172554"
          });
      colors.put(
          "indigo",
          new String[] {
            "#eef2ff", "#e0e7ff", "#c7d2fe", "#a5b4fc", "#818cf8", "#6366f1", "#4f46e5", "#4338ca",
            "#3730a3", "#312e81", "#1e1b4b"
          });
      colors.put(
          "violet",
          new String[] {
            "#f5f3ff", "#ede9fe", "#ddd6fe", "#c4b5fd", "#a78bfa", "#8b5cf6", "#7c3aed", "#6d28d9",
            "#5b21b6", "#4c1d95", "#2e1065"
          });
      colors.put(
          "purple",
          new String[] {
            "#faf5ff", "#f3e8ff", "#e9d5ff", "#d8b4fe", "#c084fc", "#a855f7", "#9333ea", "#7e22ce",
            "#6b21a8", "#581c87", "#3b0764"
          });
      colors.put(
          "fuchsia",
          new String[] {
            "#fdf4ff", "#fae8ff", "#f5d0fe", "#f0abfc", "#e879f9", "#d946ef", "#c026d3", "#a21caf",
            "#86198f", "#701a75", "#4a044e"
          });
      colors.put(
          "pink",
          new String[] {
            "#fdf2f8", "#fce7f3", "#fbcfe8", "#f9a8d4", "#f472b6", "#ec4899", "#db2777", "#be185d",
            "#9d174d", "#831843", "#500724"
          });
      colors.put(
          "rose",
          new String[] {
            "#fff1f2", "#ffe4e6", "#fecdd3", "#fda4af", "#fb7185", "#f43f5e", "#e11d48", "#be123c",
            "#9f1239", "#881337", "#4c0519"
          });

      // Named colors: white, black, transparent
      colors.put(
          "white",
          new String[] {
            "#ffffff", "#ffffff", "#ffffff", "#ffffff", "#ffffff", "#ffffff", "#ffffff", "#ffffff",
            "#ffffff", "#ffffff", "#ffffff"
          });
      colors.put(
          "black",
          new String[] {
            "#000000", "#000000", "#000000", "#000000", "#000000", "#000000", "#000000", "#000000",
            "#000000", "#000000", "#000000"
          });
      colors.put(
          "transparent",
          new String[] {
            "transparent",
            "transparent",
            "transparent",
            "transparent",
            "transparent",
            "transparent",
            "transparent",
            "transparent",
            "transparent",
            "transparent",
            "transparent"
          });

      // Spacing: 0-64 (base 4px)
      for (int i = 0; i <= 64; i++) {
        spacing[i] = i * 4.0;
      }
      // Ajustes especiales
      spacing[0] = 0;
      spacing[1] = 2;
      spacing[2] = 4;
      spacing[3] = 6;

      // Breakpoints
      breakpoints.put("sm", 640);
      breakpoints.put("md", 768);
      breakpoints.put("lg", 1024);
      breakpoints.put("xl", 1280);
      breakpoints.put("2xl", 1536);

      // Border radius
      for (int i = 0; i <= 16; i++) {
        borderRadius[i] = i * 4.0;
      }
      borderRadius[0] = 0;
      borderRadius[1] = 2;
      borderRadius[2] = 4;
      borderRadius[3] = 6;
      borderRadius[4] = 8;

      // Shadows
      shadows.put("sm", "0 1px 2px 0 rgba(0,0,0,0.05)");
      shadows.put("md", "0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06)");
      shadows.put("lg", "0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -2px rgba(0,0,0,0.05)");
      shadows.put("xl", "0 20px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04)");
      shadows.put("2xl", "0 25px 50px -12px rgba(0,0,0,0.25)");
      shadows.put("inner", "inset 0 2px 4px 0 rgba(0,0,0,0.06)");
      shadows.put("none", "none");

      // Opacity
      opacity.put("0", 0.0);
      opacity.put("5", 0.05);
      opacity.put("10", 0.1);
      opacity.put("20", 0.2);
      opacity.put("25", 0.25);
      opacity.put("30", 0.3);
      opacity.put("40", 0.4);
      opacity.put("50", 0.5);
      opacity.put("60", 0.6);
      opacity.put("70", 0.7);
      opacity.put("75", 0.75);
      opacity.put("80", 0.8);
      opacity.put("90", 0.9);
      opacity.put("95", 0.95);
      opacity.put("100", 1.0);

      // Font family
      fontFamily.put("sans", "Inter, system-ui, -apple-system, sans-serif");
      fontFamily.put("serif", "Georgia, Cambria, serif");
      fontFamily.put("mono", "JetBrains Mono, Consolas, monospace");

      // Font size (en px)
      fontSize.put("xs", 12.0);
      fontSize.put("sm", 14.0);
      fontSize.put("base", 16.0);
      fontSize.put("lg", 18.0);
      fontSize.put("xl", 20.0);
      fontSize.put("2xl", 24.0);
      fontSize.put("3xl", 30.0);
      fontSize.put("4xl", 36.0);
      fontSize.put("5xl", 48.0);
      fontSize.put("6xl", 60.0);
      fontSize.put("7xl", 72.0);
      fontSize.put("8xl", 96.0);
      fontSize.put("9xl", 128.0);

      // Font weight
      fontWeight.put("thin", "100");
      fontWeight.put("extralight", "200");
      fontWeight.put("light", "300");
      fontWeight.put("normal", "400");
      fontWeight.put("medium", "500");
      fontWeight.put("semibold", "600");
      fontWeight.put("bold", "700");
      fontWeight.put("extrabold", "800");
      fontWeight.put("black", "900");
    }

    public Builder colors(Map<String, String[]> colors) {
      this.colors.clear();
      this.colors.putAll(colors);
      return this;
    }

    public Builder addColor(String name, String[] shades) {
      this.colors.put(name, shades);
      return this;
    }

    public Builder spacing(double[] spacing) {
      if (spacing.length != 65) {
        throw new IllegalArgumentException("Spacing array must have 65 elements (0-64)");
      }
      this.spacing = spacing.clone();
      return this;
    }

    public Builder breakpoints(Map<String, Integer> breakpoints) {
      this.breakpoints.clear();
      this.breakpoints.putAll(breakpoints);
      return this;
    }

    public Builder addBreakpoint(String name, int width) {
      this.breakpoints.put(name, width);
      return this;
    }

    public Builder borderRadius(double[] borderRadius) {
      if (borderRadius.length != 17) {
        throw new IllegalArgumentException("Border radius array must have 17 elements (0-16)");
      }
      this.borderRadius = borderRadius.clone();
      return this;
    }

    public Builder shadows(Map<String, String> shadows) {
      this.shadows.clear();
      this.shadows.putAll(shadows);
      return this;
    }

    public Builder addShadow(String name, String value) {
      this.shadows.put(name, value);
      return this;
    }

    public Builder opacity(Map<String, Double> opacity) {
      this.opacity.clear();
      this.opacity.putAll(opacity);
      return this;
    }

    public Builder addOpacity(String name, double value) {
      this.opacity.put(name, value);
      return this;
    }

    public Builder fontFamily(Map<String, String> fontFamily) {
      this.fontFamily.clear();
      this.fontFamily.putAll(fontFamily);
      return this;
    }

    public Builder addFontFamily(String name, String value) {
      this.fontFamily.put(name, value);
      return this;
    }

    public Builder fontSize(Map<String, Double> fontSize) {
      this.fontSize.clear();
      this.fontSize.putAll(fontSize);
      return this;
    }

    public Builder addFontSize(String name, double value) {
      this.fontSize.put(name, value);
      return this;
    }

    public Builder fontWeight(Map<String, String> fontWeight) {
      this.fontWeight.clear();
      this.fontWeight.putAll(fontWeight);
      return this;
    }

    public Builder addFontWeight(String name, String value) {
      this.fontWeight.put(name, value);
      return this;
    }

    public ThemeConfig build() {
      return new ThemeConfig(this);
    }
  }
}
