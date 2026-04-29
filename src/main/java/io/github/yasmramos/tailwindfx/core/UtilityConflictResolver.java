package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import javafx.scene.Node;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * UtilityConflictResolver — Resuelve conflictos entre utility classes del mismo tipo.
 *
 * Problema: apply(node,"w-4") + apply(node,"w-8") deja ambas clases en styleClass.
 * La que "gana" depende del orden en el CSS, no de cuál se aplicó último.
 *
 * Solución: antes de agregar una clase nueva, detectar si pertenece a una categoría
 * conocida y eliminar las clases anteriores de esa misma categoría.
 *
 * Resultado:
 *   apply(node, "w-4")  → [w-4]
 *   apply(node, "w-8")  → [w-8]          ← w-4 eliminada
 *   apply(node, "p-2")  → [w-8, p-2]
 *   apply(node, "px-4") → [w-8, p-2, px-4]  ← px no conflicta con p
 *
 * Nota: solo resuelve conflictos de CSS classes (apply/remove).
 * Los inline styles JIT (jit()) se gestionan por StyleMerger que ya
 * sobrescribe por propiedad, sin necesidad de este resolver.
 */
public final class UtilityConflictResolver {

    private UtilityConflictResolver() {}

    // =========================================================================
    // Mapa de categorías — prefijo de clase → categoría de conflicto
    // Clases de la misma categoría se excluyen mutuamente.
    // =========================================================================

    // Mapa inverso: prefijo → nombre de categoría
    private static final Map<String, String> PREFIX_TO_CATEGORY = new LinkedHashMap<>(128);
    // Mapa de categoría → todos los prefijos en esa categoría (para búsqueda inversa)
    private static final Map<String, List<String>> CATEGORY_TO_PREFIXES = new LinkedHashMap<>(64);

    static {
        // Cada entrada: categoría → prefijos que pertenecen a ella
        Map<String, String[]> definitions = new LinkedHashMap<>();

        // Sizing
        definitions.put("w",        new String[]{"w-"});
        definitions.put("min-w",    new String[]{"min-w-"});
        definitions.put("max-w",    new String[]{"max-w-"});
        definitions.put("h",        new String[]{"h-"});
        definitions.put("min-h",    new String[]{"min-h-"});
        definitions.put("max-h",    new String[]{"max-h-"});

        // Padding — all padding utilities conflict with each other.
        // Applying a shorthand (p-) removes specific sides (px-, py-, etc.)
        // and applying a specific side removes the shorthand.
        definitions.put("padding", new String[]{"p-", "px-", "py-", "pt-", "pr-", "pb-", "pl-"});

        // Colores de fondo
        definitions.put("bg-color", new String[]{"bg-slate-","bg-gray-","bg-red-","bg-orange-","bg-amber-",
            "bg-yellow-","bg-lime-","bg-green-","bg-emerald-","bg-teal-","bg-cyan-","bg-sky-",
            "bg-blue-","bg-indigo-","bg-violet-","bg-purple-","bg-fuchsia-","bg-pink-","bg-rose-",
            "bg-white","bg-black","bg-transparent"});

        // Color de texto
        definitions.put("text-color", new String[]{"text-slate-","text-gray-","text-red-","text-orange-",
            "text-amber-","text-yellow-","text-lime-","text-green-","text-emerald-","text-teal-",
            "text-cyan-","text-sky-","text-blue-","text-indigo-","text-violet-","text-purple-",
            "text-fuchsia-","text-pink-","text-rose-","text-white","text-black","text-transparent"});

        // Tipografía
        definitions.put("font-size",   new String[]{"text-xs","text-sm","text-base","text-lg","text-xl",
            "text-2xl","text-3xl","text-4xl","text-5xl","text-6xl","text-7xl","text-8xl","text-9xl"});
        definitions.put("font-weight", new String[]{"font-thin","font-extralight","font-light",
            "font-normal","font-medium","font-semibold","font-bold","font-extrabold","font-black"});
        definitions.put("font-style",  new String[]{"italic","not-italic","oblique"});
        definitions.put("text-align",  new String[]{"text-left","text-center","text-right","text-justify"});
        definitions.put("text-decoration", new String[]{"underline","overline","line-through","no-underline"});
        definitions.put("text-transform",  new String[]{"uppercase","lowercase","capitalize","normal-case"});
        definitions.put("text-overflow",   new String[]{"truncate","text-ellipsis","text-clip",
            "overrun-ellipsis","overrun-clip","overrun-word-ellipsis"});
        definitions.put("whitespace",  new String[]{"whitespace-normal","whitespace-nowrap",
            "whitespace-pre","whitespace-pre-wrap","whitespace-pre-line","text-wrap","text-nowrap"});

        // Bordes
        definitions.put("border-width",  new String[]{"border-0","border","border-2","border-4","border-8"});
        definitions.put("border-color",  new String[]{"border-slate-","border-gray-","border-red-","border-orange-",
            "border-amber-","border-yellow-","border-lime-","border-green-","border-emerald-","border-teal-",
            "border-cyan-","border-sky-","border-blue-","border-indigo-","border-violet-","border-purple-",
            "border-fuchsia-","border-pink-","border-rose-","border-white","border-black","border-transparent"});
        definitions.put("border-style",  new String[]{"border-solid","border-dashed","border-dotted",
            "border-double","border-none"});
        definitions.put("border-radius", new String[]{"rounded-none","rounded-sm","rounded","rounded-md",
            "rounded-lg","rounded-xl","rounded-2xl","rounded-3xl","rounded-full"});

        // Sombras y efectos
        definitions.put("shadow",   new String[]{"shadow-none","shadow-sm","shadow","shadow-md",
            "shadow-lg","shadow-xl","shadow-2xl","shadow-",
            "drop-shadow-none","drop-shadow-sm","drop-shadow","drop-shadow-md",
            "drop-shadow-lg","drop-shadow-xl","drop-shadow-2xl","drop-shadow-"});
        definitions.put("opacity",  new String[]{"opacity-"});
        definitions.put("blur",     new String[]{"blur-none","blur-sm","blur","blur-md","blur-lg","blur-xl"});

        // Transforms
        definitions.put("rotate",      new String[]{"rotate-","-rotate-"});
        definitions.put("scale",       new String[]{"scale-"});
        definitions.put("translate-x", new String[]{"translate-x-","-translate-x-"});
        definitions.put("translate-y", new String[]{"translate-y-","-translate-y-"});

        // Visibilidad
        definitions.put("visibility", new String[]{"visible","invisible","hidden-node"});
        definitions.put("cursor",     new String[]{"cursor-"});

        // Gap — all gap utilities conflict with each other.
        // Applying a shorthand (gap-) removes specific axes (gap-x-, gap-y-)
        // and applying a specific axis removes the shorthand.
        definitions.put("gap", new String[]{"gap-", "gap-x-", "gap-y-"});

        // Alineación
        definitions.put("alignment", new String[]{"items-start","items-center","items-end","items-stretch",
            "items-baseline","justify-start","justify-center","justify-end","justify-between",
            "justify-around","justify-evenly","content-start","content-center","content-end"});

        // Overflow (content-display)
        definitions.put("content-display", new String[]{"icon-left","icon-right","icon-top",
            "icon-bottom","icon-center","icon-only","text-only"});

        // Tailwind v4.1 additions
        definitions.put("skew-x",       new String[]{"skew-x-","-skew-x-"});
        definitions.put("skew-y",       new String[]{"skew-y-","-skew-y-"});
        definitions.put("aspect",       new String[]{"aspect-ratio-","aspect-square","aspect-video","aspect-auto"});
        definitions.put("perspective",  new String[]{"perspective-"});
        definitions.put("rotate-x",     new String[]{"rotate-x-","-rotate-x-"});
        definitions.put("rotate-y",     new String[]{"rotate-y-","-rotate-y-"});
        definitions.put("translate-z",  new String[]{"translate-z-","-translate-z-"});
        definitions.put("text-shadow",  new String[]{"text-shadow-"});
        definitions.put("drop-shadow",  new String[]{"drop-shadow-"});
        definitions.put("fill",         new String[]{"fill-"});
        definitions.put("stroke",       new String[]{"stroke-"});
        definitions.put("stroke-width", new String[]{"stroke-0","stroke-1","stroke-2","stroke-4","stroke-8"});
        definitions.put("clip",         new String[]{"clip-"});
        definitions.put("break",        new String[]{"break-","overflow-wrap-","whitespace-"});

        // Component presets
        definitions.put("card",     new String[]{"card","card-flat","card-elevated","card-dark"});
        definitions.put("badge",    new String[]{"badge","badge-primary","badge-secondary",
            "badge-success","badge-warning","badge-danger","badge-info"});
        definitions.put("glass",    new String[]{"glass","glass-dark"});
        definitions.put("neumorph", new String[]{"neumorph","neumorph-inset","neumorph-dark"});

        // Build lookup maps
        for (Map.Entry<String, String[]> e : definitions.entrySet()) {
            String cat = e.getKey();
            List<String> prefixes = Arrays.asList(e.getValue());
            CATEGORY_TO_PREFIXES.put(cat, prefixes);
            for (String prefix : prefixes) {
                PREFIX_TO_CATEGORY.put(prefix, cat);
            }
        }
    }

    // =========================================================================
    // Cache de categorías por nodo — evita re-escanear styleClass completa
    // =========================================================================

    /**
     * Clave usada en Node.getProperties() para el cache de categorías activas.
     * El cache mapea: nombre_categoría → clase_CSS_actualmente_activa_en_esa_categoría
     *
     * Beneficio: apply() en dashboards con cientos de nodos no re-escanea
     * el styleClass list completo — usa el cache O(1) en lugar de O(n).
     */
    private static final String CACHE_KEY = "tailwindfx.category.cache";

    @SuppressWarnings("unchecked")
    private static java.util.Map<String, String> getCache(Node node) {
        return (java.util.Map<String, String>) node.getProperties()
            .computeIfAbsent(CACHE_KEY, k -> new java.util.HashMap<String, String>(8));
    }

    /**
     * Removes the entire category cache for a node.
     *
     * <p>Call this if you modify a node's {@code styleClass} list externally
     * (without going through {@link #apply}), to prevent stale cache entries
     * from causing incorrect conflict resolution on the next apply.
     *
     * @param node the node whose cache to invalidate (null-safe)
     */
    public static void invalidateCache(Node node) {
        if (node == null) return;
        node.getProperties().remove(CACHE_KEY);
    }

    /**
     * Removes a single category entry from a node's cache.
     *
     * <p>More surgical than {@link #invalidateCache} — useful when you know
     * exactly which category was modified externally. For example, if you
     * manually toggle a {@code w-*} class on a node, invalidate only {@code "w"}:
     *
     * <pre>
     * node.getStyleClass().remove("w-4");          // external modification
     * UtilityConflictResolver.invalidateCategoryCache(node, "w"); // sync cache
     * </pre>
     *
     * <p>Has no effect if the node has no cache entry for the given category.
     *
     * @param node     the node whose cache to partially invalidate (null-safe)
     * @param category the conflict category to remove (e.g. {@code "w"}, {@code "p"}, {@code "shadow"})
     */
    public static void invalidateCategoryCache(Node node, String category) {
        if (node == null || category == null || category.isBlank()) return;
        getCache(node).remove(category);
    }

    /**
     * Removes all TailwindFX metadata from a node's properties map and
     * stops any active animations. Call this when permanently removing a
     * node from the scene to release all framework-held resources.
     *
     * <p>Cleans up:
     * <ul>
     *   <li>Category cache ({@code tailwindfx.category.cache})</li>
     *   <li>StyleDiff hash ({@code tailwindfx.style.hash})</li>
     *   <li>Hover handler references ({@code tailwindfx.hover.handlers})</li>
     *   <li>Active animations ({@code tailwindfx.animations}) — stopped first</li>
     *   <li>Flex grow value ({@code tailwindfx.flex.grow})</li>
     * </ul>
     *
     * <p>Note: {@link Node#getProperties()} is tied to the node's lifetime —
     * if the node is GC-eligible (no strong references from user code), its
     * properties map is also collected automatically. This method is only
     * needed when the app holds a reference to the node after removal.
     *
     * <pre>
     * // Permanent removal pattern:
     * parent.getChildren().remove(card);
     * UtilityConflictResolver.cleanupNode(card); // release framework resources
     * cardRef = null;                             // allow GC
     * </pre>
     *
     * @param node the node to clean up (null-safe — does nothing if null)
     */
    public static void cleanupNode(Node node) {
        if (node == null) return;

        // 1. Stop and remove active animations first (breaks Timeline → node ref chain)
        @SuppressWarnings("unchecked")
        var animations = (java.util.Map<String, javafx.animation.Animation>)
            node.getProperties().get("tailwindfx.animations");
        if (animations != null) {
            animations.values().forEach(javafx.animation.Animation::stop);
            animations.clear();
        }

        // 2. Remove all TailwindFX property keys
        var props = node.getProperties();
        props.remove(CACHE_KEY);
        props.remove("tailwindfx.style.hash");
        props.remove("tailwindfx.animations");
        props.remove("tailwindfx.anim.paused");
        props.remove("tailwindfx.anim.scene-listener");
        props.remove("tailwindfx.hover.handlers");
        props.remove("tailwindfx.flex.grow");
    }

    /**
     * Installs a one-time scene listener that automatically calls
     * {@link #cleanupNode} when the node is permanently removed from the scene.
     *
     * <p>Use this for long-lived containers that frequently add and remove child
     * nodes (e.g., virtual lists, tab panes, carousels):
     *
     * <pre>
     * // In a reusable cell factory:
     * UtilityConflictResolver.autoCleanup(cell);
     * </pre>
     *
     * <p>The listener fires when {@code node.getScene()} transitions from
     * non-null to null. It is installed at most once per node.
     *
     * @param node the node to auto-cleanup on scene removal
     */
    public static void autoCleanup(Node node) {
        if (node == null) return;
        final String KEY = "tailwindfx.cleanup-listener";
        if (node.getProperties().containsKey(KEY)) return; // already installed

        node.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                cleanupNode(node);
                Preconditions.LOG.fine(
                    "UtilityConflictResolver: auto-cleanup on scene removal — "
                    + node.getClass().getSimpleName());
            }
        });
        node.getProperties().put(KEY, Boolean.TRUE);
    }

    // =========================================================================
    // API pública
    // =========================================================================

    /**
     * Aplica una utility class al nodo, eliminando clases previas del mismo tipo.
     * Usa cache O(1) por nodo para máximo rendimiento en dashboards grandes.
     *
     * apply(node, "w-8") cuando el nodo ya tiene "w-4" → elimina "w-4" y agrega "w-8".
     */
    public static void apply(Node node, String cssClass) {
        Preconditions.requireNode(node, "UtilityConflictResolver.apply");
        if (cssClass == null || cssClass.isBlank()) return;
        if (cssClass.length() > 200) {
            Preconditions.LOG.warning("UtilityConflictResolver.apply: unusually long class name (" + cssClass.length() + " chars) — is this a JIT token? Use TailwindFX.apply() instead for auto-detection.");
        }
        String category = findCategory(cssClass);
        if (category != null) {
            var cache = getCache(node);
            String prev = cache.get(category);
            if (prev != null && !prev.equals(cssClass)) {
                // Cache hit: remove exactly the previous class without list scan
                node.getStyleClass().remove(prev);
                TailwindFXMetrics.instance().recordConflictResolution(category);
            } else if (prev == null) {
                // Cache miss: first time this category on this node — defensive cleanup
                removeCategory(node, category, cssClass);
            }
            cache.put(category, cssClass);
        }
        if (!node.getStyleClass().contains(cssClass)) {
            node.getStyleClass().add(cssClass);
        }
    }

    /**
     * Aplica múltiples classes, resolviendo conflictos para cada una.
     * Acepta varargs o strings con espacios.
     */
    public static void applyAll(Node node, String... classes) {
        for (String c : classes) {
            if (c == null || c.isBlank()) continue;
            for (String part : c.split("\\s+")) {
                if (!part.isBlank()) apply(node, part);
            }
        }
    }

    /**
     * Reemplaza la clase de una categoría, independientemente de qué clase
     * de esa categoría esté actualmente aplicada.
     *
     * replaceCategory(node, "w-12") elimina cualquier w-* y pone w-12.
     */
    public static void replaceCategory(Node node, String newClass) {
        apply(node, newClass); // apply ya hace esto
    }

    /**
     * Elimina todas las clases de la categoría a la que pertenece cssClass.
     * Invalida el cache para esa categoría.
     *
     * removeCategory(node, "w-4") elimina todos los w-* del nodo.
     */
    public static void removeCategory(Node node, String cssClass) {
        String category = findCategory(cssClass);
        if (category != null) {
            getCache(node).remove(category);
            removeCategory(node, category, null);
        }
    }

    /**
     * Devuelve la categoría de conflicto de una clase, o null si no está mapeada.
     */
    public static String categoryOf(String cssClass) {
        return findCategory(cssClass);
    }

    /**
     * Lista las clases del nodo que pertenecen a una categoría dada.
     */
    public static List<String> classesInCategory(Node node, String category) {
        List<String> prefixes = CATEGORY_TO_PREFIXES.getOrDefault(category, List.of());
        return node.getStyleClass().stream()
            .filter(cls -> prefixes.stream().anyMatch(p -> matchesPrefix(cls, p)))
            .toList();
    }

    // =========================================================================
    // Internos
    // =========================================================================

    // Breakpoint prefixes supported by TailwindFX responsive engine
    private static final java.util.regex.Pattern BP_PREFIX =
        java.util.regex.Pattern.compile("^(sm:|md:|lg:|xl:|2xl:|dark:)(.+)$");

    /**
     * Finds the conflict category for a CSS class, with responsive prefix support.
     *
     * <p>Responsive classes like {@code md:w-4} are treated as a separate
     * category ({@code md:w}) from their base class ({@code w}), so they do not
     * conflict across breakpoints but DO conflict within the same breakpoint:
     * <pre>
     * apply(node, "w-4")     → category "w"
     * apply(node, "md:w-4")  → category "md:w"  (different — no conflict with w-4)
     * apply(node, "md:w-8")  → category "md:w"  (conflicts with md:w-4 → replaces it)
     * </pre>
     *
     * @param cssClass the CSS class to categorize
     * @return the conflict category string, or {@code null} if not categorized
     */
    static String findCategory(String cssClass) {
        java.util.regex.Matcher m = BP_PREFIX.matcher(cssClass);
        if (m.matches()) {
            // Responsive or dark-mode class: e.g. "md:w-4"
            String bpPrefix  = m.group(1); // "md:"
            String baseClass = m.group(2); // "w-4"
            String baseCat   = findBaseCategory(baseClass);
            // Category is scoped: "md:w" — conflicts only within same breakpoint
            return baseCat != null ? bpPrefix + baseCat : null;
        }
        return findBaseCategory(cssClass);
    }

    /** Finds the category for a plain (non-prefixed) class. */
    private static String findBaseCategory(String cssClass) {
        if (PREFIX_TO_CATEGORY.containsKey(cssClass)) {
            return PREFIX_TO_CATEGORY.get(cssClass);
        }
        String best = null;
        int bestLen = 0;
        for (Map.Entry<String, String> entry : PREFIX_TO_CATEGORY.entrySet()) {
            String prefix = entry.getKey();
            if (prefix.endsWith("-") && cssClass.startsWith(prefix)) {
                if (prefix.length() > bestLen) {
                    bestLen = prefix.length();
                    best = entry.getValue();
                }
            }
        }
        return best;
    }

    /** Elimina todas las clases del nodo que pertenecen a una categoría */
    private static void removeCategory(Node node, String category, String except) {
        List<String> prefixes = CATEGORY_TO_PREFIXES.getOrDefault(category, List.of());
        node.getStyleClass().removeIf(cls ->
            !cls.equals(except) &&
            prefixes.stream().anyMatch(p -> matchesPrefix(cls, p))
        );
    }

    /** Si una clase coincide con un prefijo (exacto o como inicio) */
    private static boolean matchesPrefix(String cssClass, String prefix) {
        if (!prefix.endsWith("-")) return cssClass.equals(prefix);
        return cssClass.startsWith(prefix);
    }
}
