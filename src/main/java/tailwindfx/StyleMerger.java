package tailwindfx;

import javafx.scene.Node;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StyleMerger — Aplica inline styles JIT a nodos JavaFX sin destruir estilos
 * previos.
 *
 * Problema: node.setStyle() sobrescribe TODO el inline style existente.
 * Solución: parsear el estilo actual, mergear por propiedad, y reescribir.
 *
 * Reglas de merge: - JIT gana sobre estilos previos de la misma propiedad (es
 * la intención del dev) - Propiedades no afectadas por el JIT se preservan
 * intactas - Las CSS classes del nodo NO se tocan aquí (eso lo hace
 * TailwindFX.apply)
 */
public final class StyleMerger {

    private StyleMerger() {
    }

    // Regex para parsear "property: value;" de un inline style
    private static final Pattern PROP_PATTERN
            = Pattern.compile("(-fx-[a-z-]+)\\s*:\\s*([^;]+);?");

    // =========================================================================
    // API pública
    // =========================================================================
    /**
     * Aplica tokens JIT a un nodo. Procesa las CSS classes fallback y el inline
     * style merged.
     *
     * Ejemplo: StyleMerger.applyJit(button, "p-4", "bg-blue-500/80",
     * "rounded-lg", "font-bold");
     */
    public static void applyJit(Node node, String... tokens) {
        JitCompiler.BatchResult result = JitCompiler.compileBatch(tokens);

        // 1. Inline styles: merge no destructivo
        if (result.hasInlineStyle()) {
            String merged = merge(node.getStyle(), result.inlineStyle());
            node.setStyle(merged);
        }

        // 2. CSS classes fallback (tokens desconocidos o que mapean a clases)
        for (String cls : result.cssClasses()) {
            if (!node.getStyleClass().contains(cls)) {
                node.getStyleClass().add(cls);
            }
        }
    }

    /**
     * Elimina propiedades JIT del inline style de un nodo. Útil para deshacer
     * estilos aplicados dinámicamente.
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

    /**
     * Reemplaza completamente el inline style JIT (elimina el previo y aplica
     * el nuevo).
     */
    public static void replaceJit(Node node, String... tokens) {
        node.setStyle("");
        node.getStyleClass().removeIf(cls -> !cls.isBlank());
        applyJit(node, tokens);
    }

    // =========================================================================
    // Merge de inline styles
    // =========================================================================
    /**
     * Mergea dos bloques de inline style. Las propiedades del bloque 'incoming'
     * sobreescriben las del 'existing'. Las propiedades en 'existing' que no
     * están en 'incoming' se preservan.
     *
     * merge("-fx-padding: 8px; -fx-opacity: 0.5;", "-fx-padding: 16px;
     * -fx-font-size: 14px;") → "-fx-font-size: 14px; -fx-opacity: 0.5;
     * -fx-padding: 16px;"
     */
    static String merge(String existing, String incoming) {
        Map<String, String> props = parseStyle(existing);
        props.putAll(parseStyle(incoming));   // incoming gana en conflictos
        return buildStyle(props);
    }

    /**
     * Elimina del 'existing' todas las propiedades presentes en 'toRemove'.
     */
    static String removeProperties(String existing, String toRemove) {
        Map<String, String> props = parseStyle(existing);
        Set<String> keysToRemove = parseStyle(toRemove).keySet();
        props.keySet().removeAll(keysToRemove);
        return buildStyle(props);
    }

    // =========================================================================
    // Parse y build de inline style string
    // =========================================================================
    /**
     * Parsea "-fx-padding: 16px; -fx-opacity: 0.5;" → {"fx-padding":"16px",
     * ...}
     */
    static Map<String, String> parseStyle(String style) {
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

    /**
     * {"fx-padding":"16px", "-fx-opacity":"0.5"} → "-fx-opacity: 0.5;
     * -fx-padding: 16px;"
     */
    static String buildStyle(Map<String, String> props) {
        if (props.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // Orden consistente para facilitar debugging
        new TreeMap<>(props).forEach((k, v)
                -> sb.append(k).append(": ").append(v).append("; ")
        );
        return sb.toString().trim();
    }
}
