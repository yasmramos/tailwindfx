package io.github.yasmramos.tailwindfx.core;

import java.util.*;
import java.util.regex.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parser de variantes compatible con Tailwind CSS v4.
 * 
 * Soporta todas las variantes oficiales:
 * - State: hover:, focus:, active:, disabled:, etc.
 * - Logical: first:, last:, odd:, even:, etc.
 * - Form: checked:, invalid:, required:, etc.
 * - Content: empty:, only-child:, etc.
 * - Focus: focus-visible:, focus-within:
 * - Group: group-hover:, group-focus:, etc.
 * - Responsive: sm:, md:, lg:, xl:, 2xl:
 * - Theme: dark:, light:
 * - Arbitrary: [@media(min-width:768px)]:, [&:hover]:
 */
public class VariantParser {
    
    // Variantes de estado (state variants)
    private static final Set<String> STATE_VARIANTS = Set.of(
        "hover", "focus", "active", "visited", "link",
        "disabled", "enabled", "checked", "indeterminate",
        "default", "target", "open"
    );
    
    // Variantes lógicas (logical variants)
    private static final Set<String> LOGICAL_VARIANTS = Set.of(
        "first", "last", "only", "odd", "even",
        "first-of-type", "last-of-type", "only-of-type",
        "nth-1", "nth-2", "nth-3"
    );
    
    // Variantes de formulario (form variants)
    private static final Set<String> FORM_VARIANTS = Set.of(
        "valid", "invalid", "required", "optional",
        "in-range", "out-of-range", "read-only", "read-write"
    );
    
    // Variantes de contenido (content variants)
    private static final Set<String> CONTENT_VARIANTS = Set.of(
        "empty", "placeholder-shown", "autofill"
    );
    
    // Variantes de foco (focus variants)
    private static final Set<String> FOCUS_VARIANTS = Set.of(
        "focus-visible", "focus-within"
    );
    
    // Variantes de grupo (group variants)
    private static final Set<String> GROUP_VARIANTS = Set.of(
        "group-hover", "group-focus", "group-active",
        "group-focus-visible", "group-focus-within"
    );
    
    // Variantes responsivas (screen variants)
    private static final Set<String> SCREEN_VARIANTS = Set.of(
        "sm", "md", "lg", "xl", "2xl",
        "min-sm", "max-sm",
        "min-md", "max-md",
        "min-lg", "max-lg",
        "min-xl", "max-xl",
        "min-2xl", "max-2xl"
    );
    
    // Variantes de tema (theme variants)
    private static final Set<String> THEME_VARIANTS = Set.of(
        "dark", "light"
    );
    
    // Todas las variantes conocidas
    private static final Set<String> ALL_VARIANTS = new HashSet<>();
    
    static {
        ALL_VARIANTS.addAll(STATE_VARIANTS);
        ALL_VARIANTS.addAll(LOGICAL_VARIANTS);
        ALL_VARIANTS.addAll(FORM_VARIANTS);
        ALL_VARIANTS.addAll(CONTENT_VARIANTS);
        ALL_VARIANTS.addAll(FOCUS_VARIANTS);
        ALL_VARIANTS.addAll(GROUP_VARIANTS);
        ALL_VARIANTS.addAll(SCREEN_VARIANTS);
        ALL_VARIANTS.addAll(THEME_VARIANTS);
    }
    
    /**
     * Resultado del parseo de un token con variante.
     */
    public static class VariantResult {
        private final List<String> variants;
        private final String utility;
        private final boolean hasVariant;
        
        public VariantResult(List<String> variants, String utility) {
            this.variants = Collections.unmodifiableList(new ArrayList<>(variants));
            this.utility = utility;
            this.hasVariant = !variants.isEmpty();
        }
        
        public List<String> getVariants() {
            return variants;
        }
        
        public String getUtility() {
            return utility;
        }
        
        public boolean hasVariant() {
            return hasVariant;
        }
        
        public String getFirstVariant() {
            return variants.isEmpty() ? null : variants.get(0);
        }
        
        @Override
        public String toString() {
            return "VariantResult{variants=" + variants + ", utility='" + utility + "'}";
        }
    }
    
    /**
     * Parsea un token para extraer variantes y la utilidad base.
     * 
     * Ejemplos:
     * - "hover:bg-blue-500" → variants=[hover], utility=bg-blue-500
     * - "md:hover:focus:bg-blue-700" → variants=[md, hover, focus], utility=bg-blue-700
     * - "bg-blue-500" → variants=[], utility=bg-blue-500
     * - "[@media(min-width:768px)]:w-full" → variants=[@media(min-width:768px)], utility=w-full
     * 
     * @param token El token a parsear
     * @return Resultado con variantes y utilidad
     */
    public static VariantResult parse(String token) {
        if (token == null || token.trim().isEmpty()) {
            return new VariantResult(Collections.emptyList(), token);
        }
        
        token = token.trim();
        List<String> variants = new ArrayList<>();
        int pos = 0;
        
        while (pos < token.length()) {
            // Buscar el siguiente ':'
            int colonPos = token.indexOf(':', pos);
            
            if (colonPos == -1) {
                // No hay más ':', el resto es la utilidad
                break;
            }
            
            // Extraer la parte antes del ':'
            String potentialVariant = token.substring(pos, colonPos);
            
            // Verificar si es una variante válida o una variante arbitraria
            if (isValidVariant(potentialVariant)) {
                variants.add(potentialVariant);
                pos = colonPos + 1;
            } else {
                // No es una variante válida, parar aquí
                break;
            }
        }
        
        // El resto del token es la utilidad
        String utility = (pos < token.length()) ? token.substring(pos) : "";
        
        return new VariantResult(variants, utility);
    }
    
    /**
     * Verifica si una cadena es una variante válida.
     */
    private static boolean isValidVariant(String variant) {
        if (variant == null || variant.isEmpty()) {
            return false;
        }
        
        // Variante arbitraria: empieza con '[' y termina con ']'
        if (variant.startsWith("[") && variant.endsWith("]")) {
            return true;
        }
        
        // Variante conocida
        return ALL_VARIANTS.contains(variant);
    }
    
    /**
     * Extrae solo las variantes de un token.
     */
    public static List<String> extractVariants(String token) {
        return parse(token).getVariants();
    }
    
    /**
     * Extrae solo la utilidad base de un token.
     */
    public static String extractUtility(String token) {
        return parse(token).getUtility();
    }
    
    /**
     * Reconstruye un token completo a partir de variantes y utilidad.
     */
    public static String reconstruct(List<String> variants, String utility) {
        if (variants == null || variants.isEmpty()) {
            return utility;
        }
        
        StringBuilder sb = new StringBuilder();
        for (String variant : variants) {
            sb.append(variant).append(':');
        }
        sb.append(utility);
        return sb.toString();
    }
    
    /**
     * Verifica si un token tiene alguna variante.
     */
    public static boolean hasVariant(String token) {
        return parse(token).hasVariant();
    }
    
    /**
     * Verifica si un token tiene una variante específica.
     */
    public static boolean hasVariant(String token, String variantName) {
        VariantResult result = parse(token);
        return result.getVariants().contains(variantName);
    }
    
    /**
     * Obtiene el tipo de variante (state, screen, theme, etc.)
     */
    public static String getVariantType(String variant) {
        if (variant == null) return "unknown";
        
        if (STATE_VARIANTS.contains(variant)) return "state";
        if (LOGICAL_VARIANTS.contains(variant)) return "logical";
        if (FORM_VARIANTS.contains(variant)) return "form";
        if (CONTENT_VARIANTS.contains(variant)) return "content";
        if (FOCUS_VARIANTS.contains(variant)) return "focus";
        if (GROUP_VARIANTS.contains(variant)) return "group";
        if (SCREEN_VARIANTS.contains(variant)) return "screen";
        if (THEME_VARIANTS.contains(variant)) return "theme";
        if (variant.startsWith("[") && variant.endsWith("]")) return "arbitrary";
        
        return "unknown";
    }
    
    /**
     * Convierte una variante a su selector CSS equivalente.
     */
    public static String toCssSelector(String variant) {
        if (variant == null) return "";
        
        // Variante arbitraria
        if (variant.startsWith("[") && variant.endsWith("]")) {
            String content = variant.substring(1, variant.length() - 1);
            // Si es una media query, devolverla directamente
            if (content.startsWith("@media") || content.startsWith("@container")) {
                return content;
            }
            // Si es un selector, devolverlo
            return content;
        }
        
        // Mapeo de variantes a selectores CSS
        switch (variant) {
            case "hover": return ":hover";
            case "focus": return ":focus";
            case "active": return ":active";
            case "visited": return ":visited";
            case "link": return ":link";
            case "disabled": return ":disabled";
            case "enabled": return ":enabled";
            case "checked": return ":checked";
            case "indeterminate": return ":indeterminate";
            case "default": return ":default";
            case "target": return ":target";
            case "open": return "[open]";
            
            case "first": return ":first-child";
            case "last": return ":last-child";
            case "only": return ":only-child";
            case "odd": return ":nth-child(odd)";
            case "even": return ":nth-child(even)";
            case "first-of-type": return ":first-of-type";
            case "last-of-type": return ":last-of-type";
            case "only-of-type": return ":only-of-type";
            
            case "valid": return ":valid";
            case "invalid": return ":invalid";
            case "required": return ":required";
            case "optional": return ":optional";
            case "in-range": return ":in-range";
            case "out-of-range": return ":out-of-range";
            case "read-only": return ":read-only";
            case "read-write": return ":read-write";
            
            case "empty": return ":empty";
            case "placeholder-shown": return ":placeholder-shown";
            case "autofill": return ":-webkit-autofill";
            
            case "focus-visible": return ":focus-visible";
            case "focus-within": return ":focus-within";
            
            case "group-hover": return ".group:hover &";
            case "group-focus": return ".group:focus &";
            case "group-active": return ".group:active &";
            case "group-focus-visible": return ".group:focus-visible &";
            case "group-focus-within": return ".group:focus-within &";
            
            case "dark": return "@media (prefers-color-scheme: dark)";
            case "light": return "@media (prefers-color-scheme: light)";
            
            default:
                // Breakpoints
                if (SCREEN_VARIANTS.contains(variant)) {
                    return getBreakpointMediaQuery(variant);
                }
                
                return ":" + variant;
        }
    }
    
    /**
     * Obtiene la media query para un breakpoint.
     */
    private static String getBreakpointMediaQuery(String breakpoint) {
        Map<String, String> breakpoints = new HashMap<>();
        breakpoints.put("sm", "(min-width: 640px)");
        breakpoints.put("md", "(min-width: 768px)");
        breakpoints.put("lg", "(min-width: 1024px)");
        breakpoints.put("xl", "(min-width: 1280px)");
        breakpoints.put("2xl", "(min-width: 1536px)");
        
        // Min variants
        breakpoints.put("min-sm", "(min-width: 640px)");
        breakpoints.put("min-md", "(min-width: 768px)");
        breakpoints.put("min-lg", "(min-width: 1024px)");
        breakpoints.put("min-xl", "(min-width: 1280px)");
        breakpoints.put("min-2xl", "(min-width: 1536px)");
        
        // Max variants
        breakpoints.put("max-sm", "(max-width: 639px)");
        breakpoints.put("max-md", "(max-width: 767px)");
        breakpoints.put("max-lg", "(max-width: 1023px)");
        breakpoints.put("max-xl", "(max-width: 1279px)");
        breakpoints.put("max-2xl", "(max-width: 1535px)");
        
        return breakpoints.getOrDefault(breakpoint, "(min-width: 0px)");
    }
}
