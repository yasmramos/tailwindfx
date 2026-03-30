package tailwindfx;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StyleToken — Representa un token JIT parseado.
 *
 * Un token es la unidad mínima del parser: "p-4", "bg-blue-500/80", "w-[320px]".
 * Cada token se convierte en una o más propiedades -fx-* inline.
 *
 * Formatos soportados:
 *   Básico:     p-4  bg-blue-500  text-sm  opacity-75
 *   Con escala: text-gray-900  border-red-300  shadow-blue-500
 *   Opacidad:   bg-blue-500/80  text-gray-900/50  (alpha 0-100)
 *   Negativo:   -mt-4  -translate-x-2  -rotate-45
 *   Arbitrario: p-[13px]  bg-[#ff6600]  w-[320px]  text-[16px]  rotate-[45deg]
 */
public final class StyleToken {

    // =========================================================================
    // Regex patterns
    // =========================================================================

    // Arbitrario: prefijo-[valor]  (el valor puede contener #, %, (, ), -, espacio)
    private static final Pattern ARBITRARY =
        Pattern.compile("^(-?)([a-z][a-z-]*)(?:-([a-z]+))?-\\[([^\\]]+)]$");

    // Color con shade y opacidad opcional: bg-blue-500/80
    private static final Pattern COLOR_SHADE_ALPHA =
        Pattern.compile("^(-?)([a-z][a-z-]*)-([a-z]+)-(\\d+)(?:/(\\d+))?$");

    // Escalar básico: p-4, w-12, gap-8
    private static final Pattern SCALE =
        Pattern.compile("^(-?)([a-z][a-z-]*)-(\\d+)$");

    // Named: text-sm, rounded-lg, font-bold
    private static final Pattern NAMED =
        Pattern.compile("^(-?)([a-z][a-z-]*)-([a-z0-9]+)$");

    // =========================================================================
    // Campos del token parseado
    // =========================================================================

    public enum Kind { SCALE, COLOR_SHADE, ARBITRARY, NAMED, UNKNOWN }

    public final String  raw;          // token original: "bg-blue-500/80"
    public final boolean negative;     // -mt-4 → true
    public final String  prefix;       // "bg", "p", "text", "w"
    public final String  colorName;    // "blue", "gray", "red" (solo COLOR_SHADE)
    public final Integer shade;        // 500, 300, 900 (solo COLOR_SHADE)
    public final Integer alpha;        // 0-100, null si no hay /alpha
    public final Integer scale;        // 4, 8, 12 (solo SCALE)
    public final String  namedValue;   // "sm", "lg", "bold" (NAMED/COLOR_SHADE)
    public final String  arbitraryVal; // "13px", "#ff6600" (ARBITRARY)
    public final String  subPrefix;    // "x", "y", "t", "r", "b", "l" de px-4, pt-2
    public final Kind    kind;

    // =========================================================================
    // Factory — parse un string en un StyleToken
    // =========================================================================

    public static StyleToken parse(String raw) {
        if (raw == null || raw.isBlank()) return unknown(raw);

        String token = raw.trim();

        // 1. Arbitrario: prefijo-[valor] o prefijo-subprefijo-[valor]
        Matcher m = ARBITRARY.matcher(token);
        if (m.matches()) {
            // Para "-translate-x-[20px]": regex puede capturar prefix="translate-x", sub=null.
            // splitSubPrefix normaliza a prefix="translate", sub="x".
            String rawPrefix = m.group(2);
            String rawSub    = m.group(3);
            String[] parts   = rawSub == null ? splitSubPrefix(rawPrefix) : new String[]{rawPrefix, rawSub};
            return new StyleToken(raw,
                !m.group(1).isEmpty(),
                parts[0],
                parts[1],
                null, null, null, null, null,
                m.group(4),    // arbitraryVal
                Kind.ARBITRARY
            );
        }

        // 2. Color con shade: bg-blue-500 o bg-blue-500/80
        m = COLOR_SHADE_ALPHA.matcher(token);
        if (m.matches() && isColorName(m.group(3))) {
            Integer alpha = m.group(5) != null ? Integer.parseInt(m.group(5)) : null;
            return new StyleToken(raw,
                !m.group(1).isEmpty(),
                m.group(2),     // prefix
                null,           // subPrefix
                m.group(3),     // colorName
                Integer.parseInt(m.group(4)), // shade
                alpha, null, null, null,
                Kind.COLOR_SHADE
            );
        }

        // 3. Scale numérico: p-4, w-12, -mt-4
        m = SCALE.matcher(token);
        if (m.matches()) {
            // Detectar sub-prefix: px, py, pt, pr, pb, pl, mx, my, etc.
            String full = m.group(2);
            String[] parts = splitSubPrefix(full);
            return new StyleToken(raw,
                !m.group(1).isEmpty(),
                parts[0],       // prefix base
                parts[1],       // subPrefix (x, y, t, r, b, l) o null
                null, null, null,
                Integer.parseInt(m.group(3)), // scale
                null, null,
                Kind.SCALE
            );
        }

        // 4. Named: text-sm, rounded-lg, font-bold, opacity-75 (sin shade de color)
        m = NAMED.matcher(token);
        if (m.matches()) {
            String full = m.group(2);
            String[] parts = splitSubPrefix(full);
            return new StyleToken(raw,
                !m.group(1).isEmpty(),
                parts[0],
                parts[1],
                null, null, null, null,
                m.group(3),    // namedValue
                null,
                Kind.NAMED
            );
        }

        return unknown(raw);
    }

    // =========================================================================
    // Constructor privado
    // =========================================================================

    private StyleToken(String raw, boolean negative, String prefix, String subPrefix,
                       String colorName, Integer shade, Integer alpha,
                       Integer scale, String namedValue, String arbitraryVal, Kind kind) {
        this.raw          = raw;
        this.negative     = negative;
        this.prefix       = prefix;
        this.subPrefix    = subPrefix;
        this.colorName    = colorName;
        this.shade        = shade;
        this.alpha        = alpha;
        this.scale        = scale;
        this.namedValue   = namedValue;
        this.arbitraryVal = arbitraryVal;
        this.kind         = kind;
    }

    private static StyleToken unknown(String raw) {
        return new StyleToken(raw, false, raw, null,
            null, null, null, null, null, null, Kind.UNKNOWN);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Valor numérico del scale, aplicando signo negativo si aplica */
    public int signedScale() {
        return negative ? -(scale != null ? scale : 0) : (scale != null ? scale : 0);
    }

    /** Valor double de un alpha 0-100 → 0.0-1.0 */
    public double alphaFraction() {
        return alpha != null ? alpha / 100.0 : 1.0;
    }

    /** Si este token tiene alpha (opacidad parcial en el color) */
    public boolean hasAlpha() { return alpha != null; }

    @Override
    public String toString() {
        return "StyleToken{" + kind + ", raw=" + raw + "}";
    }

    // =========================================================================
    // Internos
    // =========================================================================

    private static final java.util.Set<String> COLOR_NAMES = java.util.Set.of(
        "slate", "gray", "zinc", "neutral", "stone",
        "red", "orange", "amber", "yellow", "lime",
        "green", "emerald", "teal", "cyan", "sky",
        "blue", "indigo", "violet", "purple", "fuchsia",
        "pink", "rose", "white", "black"
    );

    private static boolean isColorName(String s) {
        return COLOR_NAMES.contains(s);
    }

    /**
     * Separa "px" → ["p","x"], "pt" → ["p","t"], "translate" → ["translate",null]
     * Solo para prefijos de 1-2 letras con sub-direcciones conocidas.
     */
    private static String[] splitSubPrefix(String full) {
        return switch (full) {
            case "px"      -> new String[]{"p", "x"};
            case "py"      -> new String[]{"p", "y"};
            case "pt"      -> new String[]{"p", "t"};
            case "pr"      -> new String[]{"p", "r"};
            case "pb"      -> new String[]{"p", "b"};
            case "pl"      -> new String[]{"p", "l"};
            case "mx"      -> new String[]{"m", "x"};
            case "my"      -> new String[]{"m", "y"};
            case "mt"      -> new String[]{"m", "t"};
            case "mr"      -> new String[]{"m", "r"};
            case "mb"      -> new String[]{"m", "b"};
            case "ml"      -> new String[]{"m", "l"};
            case "gap-x"      -> new String[]{"gap", "x"};
            case "gap-y"      -> new String[]{"gap", "y"};
            case "translate-x"-> new String[]{"translate", "x"};
            case "translate-y"-> new String[]{"translate", "y"};
            case "scale-x"    -> new String[]{"scale", "x"};
            case "scale-y"    -> new String[]{"scale", "y"};
            case "scroll-m"   -> new String[]{"scroll", "m"};
            case "scroll-p"   -> new String[]{"scroll", "p"};
            default           -> new String[]{full, null};
        };
    }
}
