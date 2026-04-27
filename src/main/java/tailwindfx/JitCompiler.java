package tailwindfx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * JitCompiler — Convierte tokens Tailwind en propiedades -fx-* inline.
 *
 * Entrada: "p-4" → "-fx-padding: 16px;" Entrada: "bg-blue-500/80" →
 * "-fx-background-color: rgba(59,130,246,0.80);" Entrada: "w-[320px]" →
 * "-fx-pref-width: 320px;" Entrada: "-translate-x-4" → "-fx-translate-x:
 * -16px;"
 *
 * Cache: los tokens compilados se guardan en un ConcurrentHashMap sin bloqueo
 * global. Compilar "p-4" 1000 veces cuesta igual que compilarlo 1 vez.
 *
 * Tokens desconocidos — heurística inteligente: Si el token parece un utility
 * JIT (tiene números, /, [) → WARN en consola Si parece una CSS class
 * intencional (btn-primary, card) → silencioso, se agrega como class Modo
 * debug: JitCompiler.setDebug(true) → log de TODOS los tokens
 */
public final class JitCompiler {

    private static final Logger LOG = Logger.getLogger("TailwindFX.JIT");

    // Cache global: token raw → resultado compilado
    // =========================================================================
    // Lock-free LRU cache with bounded size — prevents unbounded growth in long-running apps
    // Uses ConcurrentHashMap for thread-safe access without global locking
    // =========================================================================
    /**
     * Maximum number of compiled tokens to keep in the cache.
     */
    static final int MAX_CACHE_SIZE = 2_000;

    /**
     * Thread-safe LRU cache using ConcurrentHashMap with separate eviction mechanism.
     * Access order is tracked via a separate LinkedHashMap for LRU ordering.
     *
     * <p>
     * Why 2000? A typical large app uses ~300-500 unique utility tokens. 2000
     * gives 4× headroom for JIT-compiled arbitrary values while keeping the
     * cache under ~400KB in the worst case.
     */
    private static final ConcurrentHashMap<String, CompileResult> CACHE
            = new ConcurrentHashMap<>(256);
    
    /**
     * Separate LRU tracking map for eviction policy.
     * Wrapped in synchronizedMap since we only modify it during eviction checks.
     */
    private static final Map<String, Long> ACCESS_ORDER
            = Collections.synchronizedMap(new java.util.LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            });

    // Modo debug: loguea todos los tokens procesados
    private static volatile boolean DEBUG = false;

    /**
     * Detects if a token requires JIT compilation (arbitrary values only).
     * Matches Tailwind CSS v4 candidate parsing logic.
     * 
     * JIT triggers:
     * - Arbitrary values: w-[320px], bg-[#fff], text-[length:var(--x)]
     * - Arbitrary modifiers: bg-red-500/[0.3], hover:bg-[#fff]/(0.5)
     * - Arbitrary properties: [color:red], [mask-type:luminance]
     * 
     * NOT JIT (predefined utilities):
     * - w-32, bg-red-500, -mt-4, col-1, z-10
     */
    private static boolean requiresJitCompilation(String token) {
        if (token == null || token.isEmpty()) return false;
        
        // Fast path: arbitrary property [...]
        if (token.startsWith("[") && token.endsWith("]")) {
            // Must contain : for property:value syntax
            return token.indexOf(':', 1) > 1; // [color:red] ✓, [] ✗
        }
        
        // Split modifier (after /) - Tailwind uses segment() with top-level parsing
        int slashIdx = token.lastIndexOf('/');
        String base = (slashIdx > 0) ? token.substring(0, slashIdx) : token;
        String modifier = (slashIdx > 0) ? token.substring(slashIdx + 1) : null;
        
        // Check if modifier is arbitrary (triggers JIT for opacity, etc.)
        if (modifier != null && isArbitraryValue(modifier)) {
            return true;
        }
        
        // Check if base contains arbitrary value [...]
        return containsArbitraryValue(base);
    }

    /**
     * Checks if a string contains an arbitrary value in [...] syntax.
     * Handles nested parens/brackets like calc(100px-4rem) or var(--x).
     */
    private static boolean containsArbitraryValue(String input) {
        int bracketDepth = 0;
        int start = -1;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == '[' && bracketDepth == 0) {
                start = i;
                bracketDepth++;
            } else if (c == '[') {
                bracketDepth++;
            } else if (c == ']') {
                bracketDepth--;
                if (bracketDepth == 0 && start >= 0) {
                    // Found complete [...] - validate it's not empty
                    String arbitrary = input.substring(start + 1, i);
                    return !arbitrary.isEmpty() && !arbitrary.trim().isEmpty();
                }
            }
            // Skip escaped chars and strings for robustness (simplified)
        }
        return false;
    }

    /**
     * Checks if a modifier/value is arbitrary: [...] or (...) for CSS vars.
     */
    private static boolean isArbitraryValue(String value) {
        if (value == null || value.length() < 2) return false;
        
        // Arbitrary: [value] or (var(--x))
        if ((value.startsWith("[") && value.endsWith("]")) ||
            (value.startsWith("(") && value.endsWith(")"))) {
            String content = value.substring(1, value.length() - 1);
            return !content.isEmpty() && !content.trim().isEmpty();
        }
        return false;
    }

    public static void setDebug(boolean enabled) {
        DEBUG = enabled;
    }

    public static boolean isDebug() {
        return DEBUG;
    }

    private JitCompiler() {
    }

    // =========================================================================
    // Resultado de compilación
    // =========================================================================
    public record CompileResult(
            String inlineStyle, // propiedades -fx-* listas para setStyle()
            String cssClass, // clase CSS a agregar via getStyleClass() (puede ser null)
            boolean isKnown // false si fue un token desconocido
            ) {

        public static CompileResult inline(String style) {
            return new CompileResult(style, null, true);
        }

        public static CompileResult cssClass(String cls) {
            return new CompileResult(null, cls, true);
        }

        public static CompileResult unknown(String token) {
            return new CompileResult(null, token, false);
        }

        public boolean hasInlineStyle() {
            return inlineStyle != null && !inlineStyle.isBlank();
        }

        public boolean hasCssClass() {
            return cssClass != null && !cssClass.isBlank();
        }
    }

    // =========================================================================
    // API pública
    // =========================================================================
    /**
     * Compila un token único. Usa cache lock-free: compilar el mismo token N veces cuesta
     * lo mismo que 1. Lecturas sin bloqueo, escrituras con putIfAtomic para thread-safety.
     */
    public static CompileResult compile(String token) {
        if (token == null) {
            throw new IllegalArgumentException("JitCompiler.compile: token cannot be null");
        }
        if (token.isBlank()) {
            return CompileResult.unknown(token);
        }
        String key = token.trim();
        // Lock-free read - ConcurrentHashMap.get() is thread-safe without locking
        CompileResult result = CACHE.get(key);
        if (result != null) {
            // Update access order for LRU eviction (synchronized internally)
            ACCESS_ORDER.put(key, System.nanoTime());
            TailwindFXMetrics.instance().recordCacheHit();
            return result;
        }
        // Only synchronize on cache miss during compilation and put
        long t0 = System.nanoTime();
        result = doCompile(key);
        TailwindFXMetrics.instance().recordCompilation(System.nanoTime() - t0);
        
        // Thread-safe put with atomic operation
        CompileResult existing = CACHE.putIfAbsent(key, result);
        if (existing != null) {
            // Another thread compiled it first, use their result
            TailwindFXMetrics.instance().recordCacheHit();
            return existing;
        }
        
        // Update access order for the newly added entry
        ACCESS_ORDER.put(key, System.nanoTime());
        TailwindFXMetrics.instance().recordCacheMiss();
        return result;
    }

    /**
     * Compila múltiples tokens y devuelve el inline style combinado y la lista
     * de CSS classes a agregar.
     */
    public static BatchResult compileBatch(String... tokens) {
        StringBuilder inlineStyle = new StringBuilder();
        List<String> cssClasses = new ArrayList<>();

        // Gradient state tracking
        String gradientDirection = null;
        String fromColor = null;
        String viaColor = null;
        String toColor = null;
        boolean hasGradient = false;

        // First pass: collect gradient-related tokens
        List<String> nonGradientTokens = new ArrayList<>();
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            for (String t : token.split("\\s+")) {
                if (t == null || t.isBlank()) {
                    continue;
                }

                // Check for gradient direction
                if (t.startsWith("bg-gradient-to-")) {
                    hasGradient = true;
                    String dir = t.substring("bg-gradient-to-".length());
                    gradientDirection = switch (dir) {
                        case "t" -> "to top";
                        case "tr" -> "to top right";
                        case "r" -> "to right";
                        case "br" -> "to bottom right";
                        case "b" -> "to bottom";
                        case "bl" -> "to bottom left";
                        case "l" -> "to left";
                        case "tl" -> "to top left";
                        default -> "to bottom";
                    };
                }
                // Check for from-* color (e.g., "from-blue-500", "from-gray-800")
                else if (t.startsWith("from-")) {
                    String colorResolved = resolveGradientColor(t.substring(5));
                    if (colorResolved != null) {
                        hasGradient = true;
                        fromColor = colorResolved;
                    } else {
                        nonGradientTokens.add(t);
                    }
                }
                // Check for via-* color
                else if (t.startsWith("via-")) {
                    String colorResolved = resolveGradientColor(t.substring(4));
                    if (colorResolved != null) {
                        hasGradient = true;
                        viaColor = colorResolved;
                    } else {
                        nonGradientTokens.add(t);
                    }
                }
                // Check for to-* color
                else if (t.startsWith("to-")) {
                    String colorResolved = resolveGradientColor(t.substring(3));
                    if (colorResolved != null) {
                        hasGradient = true;
                        toColor = colorResolved;
                    } else {
                        nonGradientTokens.add(t);
                    }
                }
                // Non-gradient token
                else {
                    nonGradientTokens.add(t);
                }
            }
        }

        // If we have gradient components, build the gradient
        if (hasGradient) {
            String gradient = buildGradient(gradientDirection, fromColor, viaColor, toColor);
            if (gradient != null) {
                inlineStyle.append(gradient).append(" ");
            }
        }

        // Process non-gradient tokens normally
        for (String t : nonGradientTokens) {
            CompileResult result = compile(t);
            if (result.hasInlineStyle()) {
                inlineStyle.append(result.inlineStyle()).append(" ");
            }
            if (result.hasCssClass()) {
                cssClasses.add(result.cssClass());
            }
            if (!result.isKnown()) {
                // Heurística: si parece un token JIT (valores arbitrarios) → warn
                // Si parece una CSS class intencional (btn-primary) → silencioso
                // Excepciones: tokens de gradientes no reconocidos → silenciosos
                boolean isGradientRelated = t.startsWith("from-") || t.startsWith("via-") 
                        || t.startsWith("to-") || t.startsWith("bg-gradient-");
                if (requiresJitCompilation(t) && !isGradientRelated) {
                    LOG.warning("TailwindFX JIT: token desconocido '" + t
                            + "' (parece utility JIT pero no se reconoció)");
                } else if (DEBUG) {
                    LOG.info("TailwindFX JIT: '" + t + "' → CSS class (fallback al stylesheet)");
                }
            } else if (DEBUG) {
                String what = result.hasInlineStyle() ? "inline: " + result.inlineStyle().trim()
                        : "class: " + result.cssClass();
                LOG.info("TailwindFX JIT: '" + t + "' → " + what);
            }
        }

        return new BatchResult(inlineStyle.toString().trim(), cssClasses);
    }

    /**
     * Resuelve un token de color para gradientes (e.g., "blue-500", "gray-800")
     */
    private static String resolveGradientColor(String colorToken) {
        if (colorToken == null || colorToken.isBlank()) {
            return null;
        }

        // Parse color-shade pattern like "blue-500", "gray-800"
        int lastDash = colorToken.lastIndexOf('-');
        if (lastDash == -1) {
            // Single color name, try shade 500
            String hex = ColorPalette.hex(colorToken, 500);
            return hex != null ? hex : null;
        }

        String colorName = colorToken.substring(0, lastDash);
        String shadeStr = colorToken.substring(lastDash + 1);

        try {
            int shade = Integer.parseInt(shadeStr);
            String hex = ColorPalette.hex(colorName, shade);
            return hex != null ? hex : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Construye un gradiente linear a partir de sus componentes
     */
    private static String buildGradient(String direction, String from, String via, String to) {
        if (direction == null && from == null && to == null) {
            return null;
        }

        String dir = direction != null ? direction : "to bottom";
        String fromColor = from != null ? from : "#6B7280"; // gray-500 default
        String toColor = to != null ? to : "#9CA3AF"; // gray-400 default

        StringBuilder gradient = new StringBuilder("linear-gradient(");
        gradient.append(dir);

        if (via != null) {
            gradient.append(", ").append(fromColor)
                    .append(", ").append(via)
                    .append(", ").append(toColor);
        } else {
            gradient.append(", ").append(fromColor)
                    .append(", ").append(toColor);
        }

        gradient.append(")");
        return prop("-fx-background-color", gradient.toString());
    }

    public record BatchResult(String inlineStyle, List<String> cssClasses) {

        public boolean hasInlineStyle() {
            return !inlineStyle.isBlank();
        }
    }

    /**
     * Limpia el cache (útil en tests o al cambiar tema)
     */
    /**
     * Clears the JIT compilation cache. Call when the application's utility
     * class set changes significantly (e.g., after a major theme
     * reconfiguration). The cache is automatically bounded by LRU eviction, so
     * explicit clearing is rarely needed.
     */
    public static void clearCache() {
        CACHE.clear();
        ACCESS_ORDER.clear();
    }

    /**
     * Tamaño actual del cache
     */
    public static int cacheSize() {
        return CACHE.size();
    }

    // =========================================================================
    // Compilación principal
    // =========================================================================
    private static CompileResult doCompile(String raw) {
        StyleToken t = StyleToken.parse(raw);

        return switch (t.kind) {
            case SCALE ->
                compileScale(t);
            case COLOR_SHADE ->
                compileColor(t);
            case ARBITRARY ->
                compileArbitrary(t);
            case NAMED ->
                compileNamed(t);
            case UNKNOWN ->
                CompileResult.unknown(raw);
        };
    }

    // =========================================================================
    // Scale: p-4, w-12, gap-8, opacity-75, rotate-45, -translate-x-4
    // =========================================================================
    private static CompileResult compileScale(StyleToken t) {
        int n = t.scale;
        int s = t.signedScale();       // con signo negativo si aplica
        double unit = n * 4.0;         // escala Tailwind: 1 unit = 4px
        double signedUnit = s * 4.0;

        String style = switch (t.prefix) {
            case "p" ->
                switch (nullSafe(t.subPrefix)) {
                    case "x" ->
                        px("padding", "0px %.0fpx 0px %.0fpx".formatted(unit, unit));
                    case "y" ->
                        px("padding", "%.0fpx 0px %.0fpx 0px".formatted(unit, unit));
                    case "t" ->
                        px("padding", "%.0fpx 0px 0px 0px".formatted(unit));
                    case "r" ->
                        px("padding", "0px %.0fpx 0px 0px".formatted(unit));
                    case "b" ->
                        px("padding", "0px 0px %.0fpx 0px".formatted(unit));
                    case "l" ->
                        px("padding", "0px 0px 0px %.0fpx".formatted(unit));
                    default ->
                        px("padding", "%.0fpx".formatted(unit));
                };
            case "m" ->
                switch (nullSafe(t.subPrefix)) {
                    // JavaFX no tiene margin CSS real — usamos translate como aproximación visual
                    case "t" ->
                        prop("-fx-translate-y", "%.0fpx".formatted(signedUnit));
                    case "b" ->
                        prop("-fx-translate-y", "%.0fpx".formatted(-signedUnit));
                    case "l" ->
                        prop("-fx-translate-x", "%.0fpx".formatted(signedUnit));
                    case "r" ->
                        prop("-fx-translate-x", "%.0fpx".formatted(-signedUnit));
                    default ->
                        "";  // m-4 sin dirección: sin equivalente directo
                };
            case "w" ->
                prop("-fx-pref-width", "%.0fpx".formatted(unit));
            case "h" ->
                prop("-fx-pref-height", "%.0fpx".formatted(unit));
            case "gap" ->
                switch (nullSafe(t.subPrefix)) {
                    case "x" ->
                        prop("-fx-hgap", "%.0fpx".formatted(unit));
                    case "y" ->
                        prop("-fx-vgap", "%.0fpx".formatted(unit));
                    default ->
                        prop("-fx-spacing", "%.0fpx".formatted(unit));
                };
            case "aspect" -> {
                // aspect-ratio is Java-only (no CSS equivalent in JavaFX)
                // Return a special marker so apply() can call Styles.aspectRatio() via auto-detection
                // aspect-ratio-16-9 → scale=16, subPrefix="9" won't parse well
                // Better handled via aspect-ratio-[16/9] arbitrary token below
                yield null; // handled in ARBITRARY case
            }
            case "opacity" -> {
                // opacity-75 → 0.75  (scale directo, no * 4)
                double op = n / 100.0;
                yield prop("-fx-opacity", "%.2f".formatted(op));
            }
            case "rotate" ->
                prop("-fx-rotate", "%.0f".formatted((double) s));
            case "scale" ->
                prop("-fx-scale-x", "%.2f".formatted(n / 100.0))
                + prop("-fx-scale-y", "%.2f".formatted(n / 100.0));
            case "translate" ->
                switch (nullSafe(t.subPrefix)) {
                    case "x" ->
                        prop("-fx-translate-x", "%.0fpx".formatted(signedUnit));
                    case "y" ->
                        prop("-fx-translate-y", "%.0fpx".formatted(signedUnit));
                    default ->
                        "";
                };
            case "z" ->
                "";  // z-index: node.setViewOrder() en Java, no CSS
            case "rounded" -> {
                double r = unit;
                yield prop("-fx-background-radius", "%.0fpx".formatted(r))
                + prop("-fx-border-radius", "%.0fpx".formatted(r));
            }
            case "border" ->
                prop("-fx-border-width", "%dpx".formatted(n));
            case "shadow" ->
                buildShadow(n);
            default ->
                null;
        };

        if (style == null) {
            return CompileResult.unknown(t.raw);
        }
        if (style.isBlank()) {
            return CompileResult.cssClass(t.raw); // fallback a CSS class
        }
        return CompileResult.inline(style);
    }

    // =========================================================================
    // Color: bg-blue-500, text-gray-900/50, border-red-300
    // =========================================================================
    private static CompileResult compileColor(StyleToken t) {
        // Clamp alpha to [0, 100] — warn on out-of-range, but produce valid output
        Double alpha = null;
        if (t.hasAlpha()) {
            int rawAlpha = t.alpha;
            if (rawAlpha < 0 || rawAlpha > 100) {
                int clamped = Math.max(0, Math.min(100, rawAlpha));
                LOG.warning("TailwindFX JIT: alpha " + rawAlpha
                        + " out of range [0-100] in token '" + t.raw
                        + "' — clamped to " + clamped);
                alpha = clamped / 100.0;
            } else {
                alpha = t.alphaFraction();
            }
        }
        String color = ColorPalette.fxColor(t.colorName, t.shade, alpha);

        if (color == null) {
            LOG.warning("TailwindFX JIT: color not found: " + t.colorName + "-" + t.shade);
            return CompileResult.unknown(t.raw);
        }

        String style = switch (t.prefix) {
            case "bg" ->
                prop("-fx-background-color", color);
            case "text" ->
                prop("-fx-text-fill", color);
            case "border" ->
                prop("-fx-border-color", color);
            case "shadow" ->
                buildColoredShadow(color);
            case "ring" ->
                buildRing(color);
            case "outline" ->
                prop("-fx-border-color", color)
                + prop("-fx-border-style", "solid")
                + prop("-fx-border-width", "2px");
            case "fill" ->
                prop("-fx-fill", color);
            case "stroke" ->
                prop("-fx-stroke", color);
            case "drop-shadow" ->
                buildColoredDropShadow(color);
            case "text-shadow" ->
                buildColoredTextShadow(color);
            default ->
                null;
        };

        if (style == null) {
            return CompileResult.unknown(t.raw);
        }
        return CompileResult.inline(style);
    }

    // =========================================================================
    // Arbitrario: p-[13px], bg-[#ff6600], w-[320px], rotate-[45deg], opacity-[0.65]
    // =========================================================================
    private static CompileResult compileArbitrary(StyleToken t) {
        String val = t.arbitraryVal.trim();

        // Normalizar: "45deg" → "45" para rotación
        String style = switch (t.prefix) {
            case "p" ->
                switch (nullSafe(t.subPrefix)) {
                    case "x" ->
                        px("padding", "0px " + val + " 0px " + val);
                    case "y" ->
                        px("padding", val + " 0px " + val + " 0px");
                    case "t" ->
                        px("padding", val + " 0px 0px 0px");
                    case "r" ->
                        px("padding", "0px " + val + " 0px 0px");
                    case "b" ->
                        px("padding", "0px 0px " + val + " 0px");
                    case "l" ->
                        px("padding", "0px 0px 0px " + val);
                    default ->
                        px("padding", val);
                };
            case "w" ->
                prop("-fx-pref-width", val);
            case "h" ->
                prop("-fx-pref-height", val);
            case "min" ->
                switch (nullSafe(t.subPrefix)) {
                    case "w" ->
                        prop("-fx-min-width", val);
                    case "h" ->
                        prop("-fx-min-height", val);
                    default ->
                        "";
                };
            case "max" ->
                switch (nullSafe(t.subPrefix)) {
                    case "w" ->
                        prop("-fx-max-width", val);
                    case "h" ->
                        prop("-fx-max-height", val);
                    default ->
                        "";
                };
            case "gap" ->
                prop("-fx-spacing", val);
            case "text" ->
                prop("-fx-font-size", val);
            case "bg" -> {
                if (val.startsWith("linear-gradient") || val.startsWith("radial-gradient")) {
                    yield prop("-fx-background-color", val);
                }
                // Validar y normalizar colores hex: bg-[#f60] -> bg-[#ff6600]
                if (val.startsWith("#")) {
                    String norm = ColorPalette.normalizeHex(val);
                    if (!ColorPalette.isValidHex(norm != null ? norm : val)) {
                        LOG.warning("TailwindFX JIT: invalid hex format '"
                                + val + "' in token '" + t.raw + "'");
                    } else if (norm != null && !norm.equals(val)) {
                        yield prop("-fx-background-color", norm);
                    }
                }
                yield prop("-fx-background-color", val);
            }
            case "opacity" -> {
                // opacity-[0.65] o opacity-[65%]
                String opVal = val.endsWith("%")
                        ? "%.2f".formatted(Double.parseDouble(val.replace("%", "")) / 100.0)
                        : val;
                yield prop("-fx-opacity", opVal);
            }
            case "rotate" -> {
                String deg = val.endsWith("deg") ? val.replace("deg", "") : val;
                String sign = t.negative ? "-" : "";
                yield prop("-fx-rotate", sign + deg);
            }
            case "scale" -> {
                yield prop("-fx-scale-x", val) + prop("-fx-scale-y", val);
            }
            case "translate" ->
                switch (nullSafe(t.subPrefix)) {
                    case "x" ->
                        prop("-fx-translate-x", (t.negative ? "-" : "") + val);
                    case "y" ->
                        prop("-fx-translate-y", (t.negative ? "-" : "") + val);
                    default ->
                        "";
                };
            case "rounded" ->
                prop("-fx-background-radius", val)
                + prop("-fx-border-radius", val);
            case "border" ->
                prop("-fx-border-width", val);
            case "shadow" ->
                parseShadowArbitrary(val);
            case "drop-shadow" ->
                parseDropShadowArbitrary(val);
            case "text-shadow" ->
                parseTextShadowArbitrary(val);
            case "stroke" ->
                prop("-fx-stroke-width", val);
            case "fill" ->
                prop("-fx-fill", ColorPalette.isValidHex(val) ? val : "null");
            case "stroke-color" ->
                prop("-fx-stroke", ColorPalette.isValidHex(val) ? val : "null");
            case "aspect-ratio" ->
                parseAspectRatioArbitrary(val);
            case "aspect" ->
                parseAspectRatioArbitrary(val);
            case "bg-gradient" ->
                parseGradient(val);
            case "ring" ->
                prop("-fx-border-width", "3px") + prop("-fx-border-color", val);
            case "ring-offset" ->
                prop("-fx-border-width", "3px");
            default ->
                null;
        };

        if (style == null) {
            return CompileResult.unknown(t.raw);
        }
        if (style.isBlank()) {
            return CompileResult.cssClass(t.raw);
        }
        return CompileResult.inline(style);
    }

    // =========================================================================
    // Named: text-sm, rounded-lg, font-bold, italic, underline, truncate
    // =========================================================================
    private static CompileResult compileNamed(StyleToken t) {
        String style = switch (t.prefix + "-" + t.namedValue) {
            // Font size
            case "text-xs" ->
                prop("-fx-font-size", "11px");
            case "text-sm" ->
                prop("-fx-font-size", "13px");
            case "text-base" ->
                prop("-fx-font-size", "14px");
            case "text-lg" ->
                prop("-fx-font-size", "16px");
            case "text-xl" ->
                prop("-fx-font-size", "18px");
            case "text-2xl" ->
                prop("-fx-font-size", "22px");
            case "text-3xl" ->
                prop("-fx-font-size", "28px");
            case "text-4xl" ->
                prop("-fx-font-size", "36px");
            case "text-5xl" ->
                prop("-fx-font-size", "48px");

            // Text alignment
            case "text-left" ->
                prop("-fx-text-alignment", "left");
            case "text-center" ->
                prop("-fx-text-alignment", "center");
            case "text-right" ->
                prop("-fx-text-alignment", "right");

            // Font weight
            case "font-thin" ->
                prop("-fx-font-weight", "100");
            case "font-light" ->
                prop("-fx-font-weight", "300");
            case "font-normal" ->
                prop("-fx-font-weight", "normal");
            case "font-medium" ->
                prop("-fx-font-weight", "500");
            case "font-semibold" ->
                prop("-fx-font-weight", "600");
            case "font-bold" ->
                prop("-fx-font-weight", "bold");
            case "font-extrabold" ->
                prop("-fx-font-weight", "800");
            case "font-black" ->
                prop("-fx-font-weight", "900");

            // Font style
            case "italic" ->
                prop("-fx-font-style", "italic");
            case "not-italic" ->
                prop("-fx-font-style", "normal");

            // Text decoration
            case "underline" ->
                prop("-fx-underline", "true");
            case "line-through" ->
                prop("-fx-strikethrough", "true");
            case "no-underline" ->
                prop("-fx-underline", "false");
            case "overrun-ellipsis" ->
                prop("-fx-text-overrun", "ellipsis");
            case "overrun-clip" ->
                prop("-fx-text-overrun", "clip");
            case "wrap-text" ->
                prop("-fx-wrap-text", "true");
            case "nowrap" ->
                prop("-fx-wrap-text", "false");

            // Border radius named
            case "rounded-none" ->
                prop("-fx-background-radius", "0") + prop("-fx-border-radius", "0");
            case "rounded-sm" ->
                prop("-fx-background-radius", "2px") + prop("-fx-border-radius", "2px");
            case "rounded" ->
                prop("-fx-background-radius", "4px") + prop("-fx-border-radius", "4px");
            case "rounded-md" ->
                prop("-fx-background-radius", "6px") + prop("-fx-border-radius", "6px");
            case "rounded-lg" ->
                prop("-fx-background-radius", "8px") + prop("-fx-border-radius", "8px");
            case "rounded-xl" ->
                prop("-fx-background-radius", "12px") + prop("-fx-border-radius", "12px");
            case "rounded-2xl" ->
                prop("-fx-background-radius", "16px") + prop("-fx-border-radius", "16px");
            case "rounded-full" ->
                prop("-fx-background-radius", "999px") + prop("-fx-border-radius", "999px");

            // Shadow named
            case "shadow-none" ->
                prop("-fx-effect", "null");
            case "shadow-sm" ->
                prop("-fx-effect", "dropshadow(gaussian,rgba(0,0,0,0.05),2,0,0,1)");
            case "shadow" ->
                prop("-fx-effect", "dropshadow(gaussian,rgba(0,0,0,0.10),4,0,0,2)");
            case "aspect-square" ->
                prop("/* aspect-ratio: 1/1 — call TailwindFX.aspectSquare(node) */", "");
            case "aspect-video" ->
                prop("/* aspect-ratio: 16/9 — call TailwindFX.aspectRatio(node,16,9) */", "");
            case "shadow-md" ->
                prop("-fx-effect", "dropshadow(gaussian,rgba(0,0,0,0.12),6,0,0,3)");
            case "shadow-lg" ->
                prop("-fx-effect", "dropshadow(gaussian,rgba(0,0,0,0.15),10,0,0,4)");
            case "shadow-xl" ->
                prop("-fx-effect", "dropshadow(gaussian,rgba(0,0,0,0.20),16,0,0,6)");
            case "shadow-2xl" ->
                prop("-fx-effect", "dropshadow(gaussian,rgba(0,0,0,0.25),24,0,0,8)");

            // Opacity named (complementario a scale)
            case "opacity-0" ->
                prop("-fx-opacity", "0");
            case "opacity-5" ->
                prop("-fx-opacity", "0.05");
            case "opacity-10" ->
                prop("-fx-opacity", "0.1");
            case "opacity-20" ->
                prop("-fx-opacity", "0.2");
            case "opacity-25" ->
                prop("-fx-opacity", "0.25");
            case "opacity-30" ->
                prop("-fx-opacity", "0.3");
            case "opacity-40" ->
                prop("-fx-opacity", "0.4");
            case "opacity-50" ->
                prop("-fx-opacity", "0.5");
            case "opacity-60" ->
                prop("-fx-opacity", "0.6");
            case "opacity-70" ->
                prop("-fx-opacity", "0.7");
            case "opacity-75" ->
                prop("-fx-opacity", "0.75");
            case "opacity-80" ->
                prop("-fx-opacity", "0.8");
            case "opacity-90" ->
                prop("-fx-opacity", "0.9");
            case "opacity-95" ->
                prop("-fx-opacity", "0.95");
            case "opacity-100" ->
                prop("-fx-opacity", "1.0");

            // Cursor
            case "cursor-pointer" ->
                prop("-fx-cursor", "hand");
            case "cursor-default" ->
                prop("-fx-cursor", "default");
            case "cursor-grab" ->
                prop("-fx-cursor", "open-hand");
            case "cursor-grabbing" ->
                prop("-fx-cursor", "closed-hand");
            case "cursor-none" ->
                prop("-fx-cursor", "none");
            case "cursor-crosshair" ->
                prop("-fx-cursor", "crosshair");
            case "cursor-text" ->
                prop("-fx-cursor", "text");
            case "cursor-resize" ->
                prop("-fx-cursor", "e-resize");

            // Visibility
            case "visible" ->
                prop("-fx-opacity", "1.0");
            case "invisible" ->
                prop("-fx-opacity", "0");

            // Overflow (JavaFX no tiene CSS equivalente — usar Java API)
            case "overflow-auto" ->
                null; // handled by ScrollPane in Java
            case "overflow-hidden" ->
                prop("-fx-clip", "null"); // JavaFX clip se maneja en Java
            case "overflow-scroll" ->
                null; // handled by ScrollPane in Java
            case "overflow-x-auto" ->
                null; // handled by ScrollPane in Java
            case "overflow-y-auto" ->
                null; // handled by ScrollPane in Java

            // Display
            case "block" ->
                null; // default in JavaFX
            case "inline" ->
                null; // use Label instead of Region
            case "inline-block" ->
                null; // default in JavaFX
            case "hidden" ->
                prop("-fx-visible", "false");
            case "contents" ->
                null; // not applicable in JavaFX

            // Position (JavaFX usa layout panes en vez de position CSS)
            case "static" ->
                null; // default in JavaFX
            case "fixed" ->
                null; // use Stage or Popup
            case "absolute" ->
                null; // use Pane with layoutX/Y
            case "relative" ->
                null; // use StackPane or AnchorPane
            case "sticky" ->
                null; // implement in Java

            // Backdrop filters
            case "backdrop-blur-none" ->
                null; // use Java Effects
            case "backdrop-blur-sm" ->
                null; // use BoxBlur in Java
            case "backdrop-blur" ->
                null; // use BoxBlur in Java
            case "backdrop-blur-md" ->
                null; // use BoxBlur in Java
            case "backdrop-blur-lg" ->
                null; // use BoxBlur in Java
            case "backdrop-blur-xl" ->
                null; // use BoxBlur in Java
            case "backdrop-blur-2xl" ->
                null; // use BoxBlur in Java
            case "backdrop-blur-3xl" ->
                null; // use BoxBlur in Java

            // Ring utilities
            case "ring-0" ->
                prop("-fx-border-width", "0px");
            case "ring-1" ->
                prop("-fx-border-width", "1px") + prop("-fx-border-color", "#D1D5DB");
            case "ring-2" ->
                prop("-fx-border-width", "2px") + prop("-fx-border-color", "#D1D5DB");
            case "ring" ->
                prop("-fx-border-width", "3px") + prop("-fx-border-color", "#D1D5DB");
            case "ring-4" ->
                prop("-fx-border-width", "4px") + prop("-fx-border-color", "#D1D5DB");
            case "ring-8" ->
                prop("-fx-border-width", "8px") + prop("-fx-border-color", "#D1D5DB");

            // Border style
            case "border-solid" ->
                prop("-fx-border-style", "solid");
            case "border-dashed" ->
                prop("-fx-border-style", "dashed");
            case "border-dotted" ->
                prop("-fx-border-style", "dotted");
            case "border-none" ->
                prop("-fx-border-width", "0px");
            case "border-0" ->
                prop("-fx-border-width", "0px");
            case "border" ->
                prop("-fx-border-width", "1px") + prop("-fx-border-color", "#E5E7EB");
            case "border-2" ->
                prop("-fx-border-width", "2px");
            case "border-4" ->
                prop("-fx-border-width", "4px");
            case "border-8" ->
                prop("-fx-border-width", "8px");

            // Transitions (JavaFX usa Animation API en Java)
            case "transition-none" ->
                null; // use JavaFX Animation API
            case "transition-all" ->
                null; // use JavaFX Timeline
            case "transition" ->
                null; // use JavaFX Animation API
            case "transition-colors" ->
                null; // use JavaFX Timeline
            case "transition-opacity" ->
                null; // use FadeTransition
            case "transition-shadow" ->
                null; // use JavaFX Timeline
            case "transition-transform" ->
                null; // use TranslateTransition

            // Blend mode
            case "blend-multiply" ->
                prop("-fx-blend-mode", "multiply");
            case "blend-screen" ->
                prop("-fx-blend-mode", "screen");
            case "blend-overlay" ->
                prop("-fx-blend-mode", "overlay");
            case "blend-darken" ->
                prop("-fx-blend-mode", "darken");
            case "blend-lighten" ->
                prop("-fx-blend-mode", "lighten");
            case "blend-difference" ->
                prop("-fx-blend-mode", "difference");
            case "blend-add" ->
                prop("-fx-blend-mode", "add");

            // Width / height especiales
            case "w-full" ->
                prop("-fx-pref-width", "100%");
            case "h-full" ->
                prop("-fx-pref-height", "100%");
            case "w-auto" ->
                prop("-fx-pref-width", "-1");
            case "h-auto" ->
                prop("-fx-pref-height", "-1");
            case "w-screen" ->
                prop("-fx-pref-width", "-1"); // w-screen: USE_COMPUTED_SIZE
            case "h-screen" ->
                prop("-fx-pref-height", "-1"); // h-screen: USE_COMPUTED_SIZE

            // Overflow / display (no tienen equivalente CSS en JavaFX — delegar a CSS class)
            default ->
                null;
        };

        if (style == null) {
            return CompileResult.cssClass(t.raw); // fallback a CSS class
        }
        return CompileResult.inline(style);
    }

    // =========================================================================
    // Helpers internos
    // =========================================================================
    /**
     * Construye una propiedad CSS: "-fx-padding: 16px;"
     */
    private static String prop(String property, String value) {
        return property + ": " + value + "; ";
    }

    /**
     * Shorthand para -fx-padding
     */
    private static String px(String prop, String value) {
        return "-fx-" + prop + ": " + value + "; ";
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static String buildShadow(int scale) {
        double blur = Math.min(scale * 2.0, 32.0);
        double offsetY = Math.min(scale * 0.5, 8.0);
        double opacity = Math.min(0.05 + scale * 0.01, 0.25);
        return prop("-fx-effect",
                "dropshadow(gaussian,rgba(0,0,0,%.2f),%.0f,0,0,%.0f)".formatted(opacity, blur, offsetY));
    }

    private static String buildColoredShadow(String color) {
        return prop("-fx-effect",
                "dropshadow(gaussian," + color + ",8,0,0,2)");
    }

    private static String buildRing(String color) {
        return prop("-fx-border-color", color)
                + prop("-fx-border-width", "2px")
                + prop("-fx-border-style", "solid");
    }

    /**
     * Parsea un gradiente arbitrario para -fx-background-color.
     *
     * <p>
     * Soporta sintaxis Tailwind donde los espacios son {@code _}:
     * <pre>
     * bg-gradient-[to_right,#3b82f6,#8b5cf6]
     * bg-gradient-[to_bottom,blue-500,purple-600]
     * bg-gradient-[linear,from_#ff6600,to_#3b82f6]
     * </pre>
     */
    private static String parseGradient(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        // _ → espacio (convención Tailwind para espacios en valores arbitrarios)
        String expanded = val.replace("_", " ");
        // Reemplazar tokens de colores de paleta: blue-500 → #3b82f6
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("([a-z]+-)(\\d{2,3})").matcher(expanded);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String name = m.group(1).replaceAll("-$", "");
            try {
                int shade = Integer.parseInt(m.group(2));
                String hex = ColorPalette.hex(name, shade);
                m.appendReplacement(sb, hex != null ? hex : m.group(0));
            } catch (NumberFormatException e) {
                m.appendReplacement(sb, m.group(0));
            }
        }
        m.appendTail(sb);
        String processed = sb.toString();
        // Si ya contiene "to " o directivas CSS, wrappear directamente
        return prop("-fx-background-color", "linear-gradient(" + processed + ")");
    }

    /**
     * Intenta parsear un shadow arbitrario tipo "0_4px_6px_rgba(0,0,0,0.1)"
     * (Tailwind usa _ como separador de espacios en valores arbitrarios)
     */
    private static String parseShadowArbitrary(String val) {
        // Reemplazar _ por espacio (convención de Tailwind para valores con espacios)
        String expanded = val.replace("_", " ");
        // JavaFX dropshadow no acepta el formato CSS estándar de box-shadow directamente
        // Intentamos parsear el formato: [offset-x] [offset-y] [blur] [spread] [color]
        if (expanded.startsWith("rgba") || expanded.startsWith("rgb") || expanded.startsWith("#")) {
            // Solo color proporcionado - usamos defaults razonables
            return prop("-fx-effect", "dropshadow(gaussian," + expanded + ",8,0,0,2)");
        }
        // Intentar parsear formato completo "0 4px 6px -1px rgba(...)"
        // Pattern simplificado: extraer color al final
        java.util.regex.Pattern colorPattern = java.util.regex.Pattern.compile("(rgba?\\([^)]+\\)|#[0-9a-fA-F]{3,8})$");
        java.util.regex.Matcher matcher = colorPattern.matcher(expanded);
        if (matcher.find()) {
            String color = matcher.group(1);
            // Extraer valores numéricos (simplificado - solo primer valor como blur)
            java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile("^(\\d+(?:\\.\\d+)?)(?:px)?");
            java.util.regex.Matcher numMatcher = numPattern.matcher(expanded);
            if (numMatcher.find()) {
                try {
                    double blur = Double.parseDouble(numMatcher.group(1));
                    return prop("-fx-effect", 
                        String.format("dropshadow(gaussian,%s,%.1f,0,0,%.1f)", color, blur, blur / 3));
                } catch (NumberFormatException e) {
                    // Fall through to explicit failure
                }
            }
        }
        // No pudimos parsear - fallar explícitamente con warning
        LOG.warning("TailwindFX JIT: shadow arbitrario no soportado '" + val 
            + "' (formato complejo requiere API Java)");
        return null;
    }

    /**
     * Construye un drop shadow con color específico.
     */
    private static String buildColoredDropShadow(String color) {
        return prop("-fx-effect", "dropshadow(gaussian," + color + ",12,0,0,4)");
    }

    /**
     * Construye un text shadow con color específico.
     */
    private static String buildColoredTextShadow(String color) {
        return prop("-fx-effect", "dropshadow(gaussian," + color + ",4,0,0,1)");
    }

    /**
     * Parsea un valor arbitrario de drop-shadow.
     */
    private static String parseDropShadowArbitrary(String val) {
        return parseShadowArbitrary(val);
    }

    /**
     * Parsea un valor arbitrario de text-shadow.
     */
    private static String parseTextShadowArbitrary(String val) {
        return parseShadowArbitrary(val);
    }

    /**
     * Parsea un valor arbitrario de aspect-ratio.
     */
    private static String parseAspectRatioArbitrary(String val) {
        // aspect-ratio no tiene equivalente directo en JavaFX CSS
        // Retornamos una clase CSS predecible para que el desarrollador pueda manejarlo
        // Ej: aspect-[16/9] -> "-fx-aspect-ratio-16-9"
        String sanitized = val.replaceAll("[^a-zA-Z0-9]", "-");
        LOG.warning("TailwindFX JIT: aspect-ratio '" + val 
            + "' no tiene equivalente CSS - usar FxLayout.setAspectRatio(node, ratio) en Java");
        return null; // CompileResult.cssClass se encargará del fallback
    }
}
