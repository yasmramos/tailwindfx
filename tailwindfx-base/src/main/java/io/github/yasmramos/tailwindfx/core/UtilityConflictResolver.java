package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.Node;

/**
 * UtilityConflictResolver — Resolves conflicts between utility classes of the same type.
 *
 * <p>Problem: apply(node,"w-4") + apply(node,"w-8") leaves both classes in styleClass. The "winner"
 * depends on CSS order, not which one was applied last.
 *
 * <p>Solution: before adding a new class, detect if it belongs to a known category and remove
 * previous classes from that same category.
 *
 * <p>Result: apply(node, "w-4") → [w-4] apply(node, "w-8") → [w-8] ← w-4 removed apply(node, "p-2")
 * → [w-8, p-2] apply(node, "px-4") → [w-8, px-4] ← px-4 supersedes the horizontal sides of p-2;
 * vertical sides (pt/pr/pb/pl) of p-2 survive because they cover different sides.
 *
 * <p>Note: only resolves CSS class conflicts (apply/remove). JIT inline styles (jit()) are managed
 * by StyleMerger which already overwrites by property, without need for this resolver.
 */
public final class UtilityConflictResolver {

  private UtilityConflictResolver() {}

  // Category map — class prefix → conflict category
  // Classes in the same category are mutually exclusive.

  // Reverse map: prefix → category name
  private static final Map<String, String> PREFIX_TO_CATEGORY = new LinkedHashMap<>(128);
  // Map of category → all prefixes in that category (for reverse lookup)
  private static final Map<String, List<String>> CATEGORY_TO_PREFIXES = new LinkedHashMap<>(64);

  static {
    // Each entry: category → prefixes belonging to it
    Map<String, String[]> definitions = new LinkedHashMap<>();

    // Sizing
    definitions.put("w", new String[] {"w-"});
    definitions.put("min-w", new String[] {"min-w-"});
    definitions.put("max-w", new String[] {"max-w-"});
    definitions.put("h", new String[] {"h-"});
    definitions.put("min-h", new String[] {"min-h-"});
    definitions.put("max-h", new String[] {"max-h-"});

    // Padding — side-aware conflict categories (Tailwind precedence model).
    // A shorthand only conflicts with classes that cover the SAME sides:
    //   p-   covers all four sides   → conflicts with every other padding class
    //   px-/py- cover two opposite sides → conflict with each axis's specific sides
    //   pt-/pr-/pb-/pl- cover one side  → conflict only on that side
    // This prevents apply(node, "p-4", "px-6") from silently dropping top/bottom padding.
    definitions.put("padding-all", new String[] {"p-"});
    definitions.put("padding-x", new String[] {"px-"});
    definitions.put("padding-y", new String[] {"py-"});
    definitions.put("padding-top", new String[] {"pt-"});
    definitions.put("padding-right", new String[] {"pr-"});
    definitions.put("padding-bottom", new String[] {"pb-"});
    definitions.put("padding-left", new String[] {"pl-"});

    // Background colors
    definitions.put(
        "bg-gradient",
        new String[] {
          "bg-gradient-to-r",
          "bg-gradient-to-l",
          "bg-gradient-to-t",
          "bg-gradient-to-b",
          "bg-gradient-to-tr",
          "bg-gradient-to-tl",
          "bg-gradient-to-br",
          "bg-gradient-to-bl"
        });
    definitions.put(
        "bg-color",
        new String[] {
          "bg-slate-",
          "bg-gray-",
          "bg-red-",
          "bg-orange-",
          "bg-amber-",
          "bg-yellow-",
          "bg-lime-",
          "bg-green-",
          "bg-emerald-",
          "bg-teal-",
          "bg-cyan-",
          "bg-sky-",
          "bg-blue-",
          "bg-indigo-",
          "bg-violet-",
          "bg-purple-",
          "bg-fuchsia-",
          "bg-pink-",
          "bg-rose-",
          "bg-white",
          "bg-black",
          "bg-transparent"
        });

    // Text color
    definitions.put(
        "text-color",
        new String[] {
          "text-slate-",
          "text-gray-",
          "text-red-",
          "text-orange-",
          "text-amber-",
          "text-yellow-",
          "text-lime-",
          "text-green-",
          "text-emerald-",
          "text-teal-",
          "text-cyan-",
          "text-sky-",
          "text-blue-",
          "text-indigo-",
          "text-violet-",
          "text-purple-",
          "text-fuchsia-",
          "text-pink-",
          "text-rose-",
          "text-white",
          "text-black",
          "text-transparent"
        });

    // Typography
    definitions.put(
        "font-size",
        new String[] {
          "text-xs",
          "text-sm",
          "text-base",
          "text-lg",
          "text-xl",
          "text-2xl",
          "text-3xl",
          "text-4xl",
          "text-5xl",
          "text-6xl",
          "text-7xl",
          "text-8xl",
          "text-9xl"
        });
    definitions.put(
        "font-weight",
        new String[] {
          "font-thin",
          "font-extralight",
          "font-light",
          "font-normal",
          "font-medium",
          "font-semibold",
          "font-bold",
          "font-extrabold",
          "font-black"
        });
    definitions.put("font-style", new String[] {"italic", "not-italic", "oblique"});
    definitions.put(
        "text-align", new String[] {"text-left", "text-center", "text-right", "text-justify"});
    definitions.put(
        "text-decoration", new String[] {"underline", "overline", "line-through", "no-underline"});
    definitions.put(
        "text-transform", new String[] {"uppercase", "lowercase", "capitalize", "normal-case"});
    definitions.put(
        "text-overflow",
        new String[] {
          "truncate",
          "text-ellipsis",
          "text-clip",
          "overrun-ellipsis",
          "overrun-clip",
          "overrun-word-ellipsis"
        });
    definitions.put(
        "whitespace",
        new String[] {
          "whitespace-normal",
          "whitespace-nowrap",
          "whitespace-pre",
          "whitespace-pre-wrap",
          "whitespace-pre-line",
          "text-wrap",
          "text-nowrap"
        });

    // Borders
    definitions.put(
        "border-width", new String[] {"border-0", "border", "border-2", "border-4", "border-8"});
    definitions.put(
        "border-color",
        new String[] {
          "border-slate-",
          "border-gray-",
          "border-red-",
          "border-orange-",
          "border-amber-",
          "border-yellow-",
          "border-lime-",
          "border-green-",
          "border-emerald-",
          "border-teal-",
          "border-cyan-",
          "border-sky-",
          "border-blue-",
          "border-indigo-",
          "border-violet-",
          "border-purple-",
          "border-fuchsia-",
          "border-pink-",
          "border-rose-",
          "border-white",
          "border-black",
          "border-transparent"
        });
    definitions.put(
        "border-style",
        new String[] {
          "border-solid", "border-dashed", "border-dotted", "border-double", "border-none"
        });
    definitions.put(
        "border-radius",
        new String[] {
          "rounded-none",
          "rounded-sm",
          "rounded",
          "rounded-md",
          "rounded-lg",
          "rounded-xl",
          "rounded-2xl",
          "rounded-3xl",
          "rounded-full"
        });

    // Shadows and effects
    definitions.put(
        "shadow",
        new String[] {
          "shadow-none",
          "shadow-sm",
          "shadow",
          "shadow-md",
          "shadow-lg",
          "shadow-xl",
          "shadow-2xl",
          "shadow-",
          "drop-shadow-none",
          "drop-shadow-sm",
          "drop-shadow",
          "drop-shadow-md",
          "drop-shadow-lg",
          "drop-shadow-xl",
          "drop-shadow-2xl",
          "drop-shadow-"
        });
    definitions.put("opacity", new String[] {"opacity-"});
    definitions.put(
        "blur", new String[] {"blur-none", "blur-sm", "blur", "blur-md", "blur-lg", "blur-xl"});

    // Transforms
    definitions.put("rotate", new String[] {"rotate-", "-rotate-"});
    definitions.put("scale", new String[] {"scale-"});
    definitions.put("translate-x", new String[] {"translate-x-", "-translate-x-"});
    definitions.put("translate-y", new String[] {"translate-y-", "-translate-y-"});

    // Visibility
    definitions.put("visibility", new String[] {"visible", "invisible", "hidden-node"});
    definitions.put("cursor", new String[] {"cursor-"});

    // Gap — all gap utilities conflict with each other.
    // Applying a shorthand (gap-) removes specific axes (gap-x-, gap-y-)
    // and applying a specific axis removes the shorthand.
    definitions.put("gap", new String[] {"gap-", "gap-x-", "gap-y-"});

    // Alignment
    definitions.put(
        "alignment",
        new String[] {
          "items-start",
          "items-center",
          "items-end",
          "items-stretch",
          "items-baseline",
          "justify-start",
          "justify-center",
          "justify-end",
          "justify-between",
          "justify-around",
          "justify-evenly",
          "content-start",
          "content-center",
          "content-end"
        });

    // Overflow (content-display)
    definitions.put(
        "content-display",
        new String[] {
          "icon-left",
          "icon-right",
          "icon-top",
          "icon-bottom",
          "icon-center",
          "icon-only",
          "text-only"
        });

    // Tailwind v4.1 additions
    definitions.put("skew-x", new String[] {"skew-x-", "-skew-x-"});
    definitions.put("skew-y", new String[] {"skew-y-", "-skew-y-"});
    definitions.put(
        "aspect", new String[] {"aspect-ratio-", "aspect-square", "aspect-video", "aspect-auto"});
    definitions.put("perspective", new String[] {"perspective-"});
    definitions.put("rotate-x", new String[] {"rotate-x-", "-rotate-x-"});
    definitions.put("rotate-y", new String[] {"rotate-y-", "-rotate-y-"});
    definitions.put("translate-z", new String[] {"translate-z-", "-translate-z-"});
    definitions.put("text-shadow", new String[] {"text-shadow-"});
    definitions.put("drop-shadow", new String[] {"drop-shadow-"});
    definitions.put("fill", new String[] {"fill-"});
    definitions.put("stroke", new String[] {"stroke-"});
    definitions.put(
        "stroke-width", new String[] {"stroke-0", "stroke-1", "stroke-2", "stroke-4", "stroke-8"});
    definitions.put("clip", new String[] {"clip-"});
    definitions.put("break", new String[] {"break-", "overflow-wrap-", "whitespace-"});

    // Component presets
    definitions.put("card", new String[] {"card", "card-flat", "card-elevated", "card-dark"});
    definitions.put(
        "badge",
        new String[] {
          "badge",
          "badge-primary",
          "badge-secondary",
          "badge-success",
          "badge-warning",
          "badge-danger",
          "badge-info"
        });
    definitions.put("glass", new String[] {"glass", "glass-dark"});
    definitions.put("neumorph", new String[] {"neumorph", "neumorph-inset", "neumorph-dark"});

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

  // Category cache per node — avoids re-scanning complete styleClass

  /**
   * Key used in Node.getProperties() for caching active categories. The cache maps: category_name →
   * CSS_class_currently_active_in_that_category
   *
   * <p>Benefit: apply() on dashboards with hundreds of nodes does not re-scan the complete
   * styleClass list — uses O(1) cache instead of O(n).
   */
  private static final String CACHE_KEY = "tailwindfx.category.cache";


  @SuppressWarnings("unchecked")
  private static java.util.Map<String, String> getCache(Node node) {
    return (java.util.Map<String, String>)
        node.getProperties()
            .computeIfAbsent(CACHE_KEY, k -> new java.util.HashMap<String, String>(8));
  }

  /**
   * Removes the entire category cache for a node.
   *
   * <p>Call this if you modify a node's {@code styleClass} list externally (without going through
   * {@link #apply}), to prevent stale cache entries from causing incorrect conflict resolution on
   * the next apply.
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
   * <p>More surgical than {@link #invalidateCache} — useful when you know exactly which category
   * was modified externally. For example, if you manually toggle a {@code w-*} class on a node,
   * invalidate only {@code "w"}:
   *
   * <pre>
   * node.getStyleClass().remove("w-4");          // external modification
   * UtilityConflictResolver.invalidateCategoryCache(node, "w"); // sync cache
   * </pre>
   *
   * <p>Has no effect if the node has no cache entry for the given category.
   *
   * @param node the node whose cache to partially invalidate (null-safe)
   * @param category the conflict category to remove (e.g. {@code "w"}, {@code "p"}, {@code
   *     "shadow"})
   */
  public static void invalidateCategoryCache(Node node, String category) {
    if (node == null || category == null || category.isBlank()) return;
    getCache(node).remove(category);
  }

  /**
   * Removes all TailwindFX metadata from a node's properties map and stops any active animations.
   * Call this when permanently removing a node from the scene to release all framework-held
   * resources.
   *
   * <p>Cleans up:
   *
   * <ul>
   *   <li>Category cache ({@code tailwindfx.category.cache})
   *   <li>StyleDiff hash ({@code tailwindfx.style.hash})
   *   <li>Hover handler references ({@code tailwindfx.hover.handlers})
   *   <li>Active animations ({@code tailwindfx.animations}) — stopped first
   *   <li>Flex grow value ({@code tailwindfx.flex.grow})
   * </ul>
   *
   * <p>Note: {@link Node#getProperties()} is tied to the node's lifetime — if the node is
   * GC-eligible (no strong references from user code), its properties map is also collected
   * automatically. This method is only needed when the app holds a reference to the node after
   * removal.
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
    var animations =
        (java.util.Map<String, javafx.animation.Animation>)
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
   * Installs a one-time scene listener that automatically calls {@link #cleanupNode} when the node
   * is permanently removed from the scene.
   *
   * <p>Use this for long-lived containers that frequently add and remove child nodes (e.g., virtual
   * lists, tab panes, carousels):
   *
   * <pre>
   * // In a reusable cell factory:
   * UtilityConflictResolver.autoCleanup(cell);
   * </pre>
   *
   * <p>The listener fires when {@code node.getScene()} transitions from non-null to null. It is
   * installed at most once per node.
   *
   * @param node the node to auto-cleanup on scene removal
   */
  public static void autoCleanup(Node node) {
    if (node == null) return;
    final String KEY = "tailwindfx.cleanup-listener";
    if (node.getProperties().containsKey(KEY)) return; // already installed

    node.sceneProperty()
        .addListener(
            (obs, oldScene, newScene) -> {
              if (newScene == null) {
                cleanupNode(node);
                Preconditions.LOG.fine(
                    "UtilityConflictResolver: auto-cleanup on scene removal — "
                        + node.getClass().getSimpleName());
              }
            });
    node.getProperties().put(KEY, Boolean.TRUE);
  }

  // Public API

  /**
   * Applies a utility class to the node, removing previous classes of the same type. Uses O(1)
   * cache per node for maximum performance on large dashboards.
   *
   * <p>apply(node, "w-8") when node already has "w-4" → removes "w-4" and adds "w-8".
   */
  public static void apply(Node node, String cssClass) {
    Preconditions.requireNode(node, "UtilityConflictResolver.apply");
    if (cssClass == null || cssClass.isBlank()) return;
    if (cssClass.length() > 200) {
      Preconditions.LOG.warning(
          "UtilityConflictResolver.apply: unusually long class name ("
              + cssClass.length()
              + " chars) — is this a JIT token? Use TwStyle.apply() instead for auto-detection.");
    }
    String category = findCategory(cssClass);
    if (category != null) {
      supersede(node, cssClass, category);
    }
    if (!node.getStyleClass().contains(cssClass)) {
      node.getStyleClass().add(cssClass);
    }
  }

  /**
   * Removes classes whose covered sides are fully covered by {@code newClass}, then records the new
   * class in the per-category cache. Shared by {@link #apply} and {@link #applyAll}.
   */
  private static void supersede(Node node, String newClass, String category) {
    java.util.Map<String, String> cache = getCache(node);
    String prev = cache.get(category);
    int removed = 0;
    if (prev != null && !prev.equals(newClass)) {
      // Fast path: the previously applied class of this exact category is superseded
      // (same-category classes always cover identical sides).
      if (node.getStyleClass().remove(prev)) {
        removed++;
      } else {
        // Stale cache entry (class was removed externally) — fall back to defensive scan.
        removed += removeCategory(node, category, newClass);
      }
    } else if (prev == null) {
      // Cache miss: defensive cleanup of same-category leftovers.
      removed += removeCategory(node, category, newClass);
    }
    // Side-aware cleanup for base-level padding classes only. Breakpoint-scoped
    // paddings (e.g. "md:p-4") are never decomposed into side classes, so they must
    // not participate in this logic — removing them would silently drop responsive
    // padding at runtime.
    boolean scoped = BP_PREFIX.matcher(newClass).matches();
    if (!scoped) {
      removed += supersedePadding(node, newClass, cache);
    }
    // Register the newcomer AFTER the side scan so it does not match against itself
    // (a padding class trivially covers its own sides).
    cache.put(category, newClass);
    if (removed > 0) {
      TailwindFXMetrics.instance().recordConflictResolution(category);
    }
  }

  /**
   * Applies multiple classes, resolving conflicts for each one. Accepts varargs or strings with
   * spaces.
   */
  public static void applyAll(Node node, String... classes) {
    java.util.List<String> flat = new java.util.ArrayList<>();
    for (String c : classes) {
      if (c == null || c.isBlank()) continue;
      for (String part : c.split("\\s+")) {
        if (!part.isBlank()) flat.add(part);
      }
    }

    // Deduplicate identical tokens within the batch (last-wins collapses to a single
    // occurrence). Without this, duplicate paddings such as "p-4 p-4" are treated as
    // two independent side-owners by expandPaddingBatch: each removes the other's
    // sides, both end up with an empty remaining set, and every padding class is
    // silently dropped from the node.
    java.util.List<String> unique = new java.util.ArrayList<>();
    java.util.Set<String> seen = new java.util.HashSet<>();
    for (String cls : flat) {
      if (seen.add(cls)) unique.add(cls);
    }
    flat = unique;

    // Phase 1 — intra-batch conflict resolution. Later tokens win over earlier ones in
    // the same batch (last-wins semantics), so losers are dropped before touching the
    // node. This prevents the old two-phase bug where each token superseded only the
    // node's *previous* state: applying "p-4 px-6" resolved both newcomers against an
    // empty cache, neither removed the other, and p-4 silently survived alongside px-6.
    java.util.Map<String, String> batchByCategory = new java.util.HashMap<>(8);
    java.util.Set<String> losers = new java.util.HashSet<>();
    for (String cls : flat) {
      String category = findCategory(cls);
      if (category == null) continue;
      String prev = batchByCategory.put(category, cls);
      // The newcomer supersedes the previous winner of the same category. A token that
      // is already a loser must not "revive" the current winner when it reappears later
      // in the batch ("p-4 p-6 p-4": the trailing p-4 loses to p-6, and the duplicate
      // must not resurrect the first p-4 slot — phase 2's contains() guard makes
      // re-applying the winner idempotent).
      if (prev != null && !losers.contains(cls)) {
        losers.add(prev);
      }
    }
    // Padding side-awareness within the batch: e.g. "p-4 px-6" must keep p-4's vertical
    // sides (decomposed to py-4) instead of treating them as fully independent tokens.
    java.util.List<String> expanded = expandPaddingBatch(flat, losers);

    // Phase 2 — apply surviving tokens sequentially against the node's existing state,
    // reusing the exact same supersede() path as single apply().
    for (String cls : expanded) {
      if (losers.contains(cls)) continue;
      String category = findCategory(cls);
      if (category != null) supersede(node, cls, category);
      if (!node.getStyleClass().contains(cls)) node.getStyleClass().add(cls);
    }
  }

  /**
   * Pre-resolves padding interactions among the tokens of a single {@link #applyAll} batch,
   * returning the effective token list (fully covered paddings removed, partially covered
   * shorthands decomposed into their surviving-side equivalents). Non-padding tokens pass
   * through untouched. Mirrors Tailwind CSS precedence at the class-set level (audit finding
   * #8): "p-4 px-6" behaves like "px-6 py-4", and "px-6 py-2 p-8" collapses to "p-8".
   */
  private static java.util.List<String> expandPaddingBatch(
      java.util.List<String> flat, java.util.Set<String> losers) {
    java.util.List<String> out = new ArrayList<>(flat.size());
    java.util.Set<String> emitted = new java.util.HashSet<>();
    java.util.List<String> paddings = new ArrayList<>();
    for (String cls : flat) {
      if (losers.contains(cls)) continue;
      if (paddingSides(cls).isEmpty() || BP_PREFIX.matcher(cls).matches()) {
        out.add(cls);
      } else {
        paddings.add(cls);
      }
    }
    // For every padding class, compute the sides still owned by a LATER padding token.
    // A token whose remaining set is empty is fully overridden and dropped; a shorthand
    // with leftovers is re-emitted as its uncovered-side equivalents ("p-4" minus the
    // horizontal sides owned by a later "px-6" becomes "py-4").
    int n = paddings.size();
    java.util.List<java.util.Set<String>> remaining = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      java.util.Set<String> sides = new java.util.HashSet<>(paddingSides(paddings.get(i)));
      for (int j = i + 1; j < n; j++) {
        sides.removeAll(paddingSides(paddings.get(j)));
      }
      remaining.add(sides);
    }
    for (int i = 0; i < n; i++) {
      String cls = paddings.get(i);
      java.util.Set<String> sides = remaining.get(i);
      if (sides.isEmpty()) continue; // fully covered by later padding tokens
      String effective;
      if (sides.equals(paddingSides(cls))) {
        effective = cls; // no partial override — keep the original token
      } else {
        String value = cls.substring(paddingPrefix(cls).length());
        List<String> prefixes = decomposeSidePrefixes(sides);
        // Emit every generated side class; the last one replaces the original slot to
        // preserve ordering, the others are appended right after.
        effective = prefixes.isEmpty() ? cls : prefixes.get(0) + value;
        for (int k = 1; k < prefixes.size(); k++) {
          String extra = prefixes.get(k) + value;
          if (emitted.add(extra)) out.add(extra);
        }
      }
      if (emitted.add(effective)) out.add(effective);
    }
    return out;
  }

  /**
   * Replaces the class in a category, regardless of which class from that category is currently
   * applied.
   *
   * <p>replaceCategory(node, "w-12") removes any w-* and applies w-12.
   */
  public static void replaceCategory(Node node, String newClass) {
    apply(node, newClass); // apply already does this
  }

  /**
   * Removes all classes from the category to which cssClass belongs. Invalidates the cache for that
   * category.
   *
   * <p>removeCategory(node, "w-4") removes all w-* from the node.
   */
  public static void removeCategory(Node node, String cssClass) {
    String category = findCategory(cssClass);
    if (category != null) {
      getCache(node).remove(category);
      removeCategory(node, category, null);
    }
  }

  /** Returns the conflict category of a class, or null if not mapped. */
  public static String categoryOf(String cssClass) {
    return findCategory(cssClass);
  }

  /** Lists the node's classes that belong to a given category. */
  public static List<String> classesInCategory(Node node, String category) {
    List<String> prefixes = CATEGORY_TO_PREFIXES.getOrDefault(category, List.of());
    return node.getStyleClass().stream()
        .filter(cls -> prefixes.stream().anyMatch(p -> matchesPrefix(cls, p)))
        .toList();
  }

  // Breakpoint prefixes supported by TailwindFX responsive engine
  private static final java.util.regex.Pattern BP_PREFIX =
      java.util.regex.Pattern.compile("^(sm:|md:|lg:|xl:|2xl:|dark:)(.+)$");

  // Padding side model

  /** The four primitive sides every padding utility is built from. */
  private static final String TOP = "top";
  private static final String RIGHT = "right";
  private static final String BOTTOM = "bottom";
  private static final String LEFT = "left";

  /**
   * Returns the set of sides covered by a padding utility class (base or breakpoint-scoped), or an
   * empty set if the class is not a padding utility. Unknown side names (e.g. {@code pc-}) map to
   * the full set so they conservatively conflict with everything padding-related rather than being
   * silently ignored.
   */
  static java.util.Set<String> paddingSides(String cssClass) {
    if (cssClass == null) return java.util.Set.of();
    java.util.regex.Matcher m = BP_PREFIX.matcher(cssClass);
    String base = m.matches() ? m.group(2) : cssClass;
    // Order matters: "px-" also starts with "p", so axis/side prefixes are tested first.
    if (base.startsWith("px-")) return java.util.Set.of(LEFT, RIGHT);
    if (base.startsWith("py-")) return java.util.Set.of(TOP, BOTTOM);
    if (base.startsWith("pt-")) return java.util.Set.of(TOP);
    if (base.startsWith("pr-")) return java.util.Set.of(RIGHT);
    if (base.startsWith("pb-")) return java.util.Set.of(BOTTOM);
    if (base.startsWith("pl-")) return java.util.Set.of(LEFT);
    if (base.startsWith("p-")) return java.util.Set.of(TOP, RIGHT, BOTTOM, LEFT);
    return java.util.Set.of();
  }

  /** True if {@code outer} covers every side in {@code inner}. */
  private static boolean covers(java.util.Set<String> outer, java.util.Set<String> inner) {
    return !inner.isEmpty() && outer.containsAll(inner);
  }

  /**
   * Applies Tailwind's padding precedence model between {@code newClass} and the padding
   * classes currently on the node:
   *
   * <ul>
   *   <li>A class whose sides are fully covered by the newcomer is removed ("px-6 py-2" →
   *       applying "p-8" removes both).
   *   <li>A shorthand whose sides are NOT fully covered by the newcomer survives as its
   *       uncovered-side equivalents: "p-4" + "px-6" keeps top/bottom via generated
   *       "py-4" (so no vertical padding is silently lost — audit finding #8).
   * </ul>
   *
   * @return number of style classes removed from the node
   */
  private static int supersedePadding(Node node, String newClass, Map<String, String> cache) {
    Set<String> newSides = paddingSides(newClass);
    if (newSides.isEmpty()) return 0; // not a padding utility

    // Collect candidates from the category cache (only padding categories matter).
    Map<String, String> victims = new LinkedHashMap<>();
    for (Map.Entry<String, String> e : new ArrayList<>(cache.entrySet())) {
      String cat = e.getKey();
      if (!cat.startsWith("padding-")) continue;
      String cls = e.getValue();
      if (cls.equals(newClass)) continue;
      Set<String> sides = paddingSides(cls);
      if (sides.isEmpty() || BP_PREFIX.matcher(cls).matches()) continue;
      if (covers(newSides, sides)) {
        victims.put(cat, cls); // fully overridden by the newcomer
      } else if (covers(sides, newSides)) {
        // Existing broader shorthand (e.g. "p-4") vs narrower newcomer (e.g. "px-6"):
        // keep coverage of the remaining sides by decomposing the shorthand, carrying
        // over its value suffix ("p-4" -> "py-4").
        victims.put(cat, cls);
        Set<String> leftover = new LinkedHashSet<>(sides);
        leftover.removeAll(newSides);
        String value = cls.substring(paddingPrefix(cls).length());
        for (String prefix : decomposeSidePrefixes(leftover)) {
          String generated = prefix + value;
          String genCat = findCategory(generated);
          if (genCat == null) continue;
          String existingForGen = cache.get(genCat);
          // A more specific rule already on the node wins on those sides (Tailwind
          // specificity): don't resurrect the shorthand's value there.
          if (existingForGen == null || victims.containsValue(existingForGen)) {
            cache.put(genCat, generated);
            if (!node.getStyleClass().contains(generated)) node.getStyleClass().add(generated);
          }
        }
      }
    }
    int removed = 0;
    for (Map.Entry<String, String> e : victims.entrySet()) {
      cache.remove(e.getKey());
      if (node.getStyleClass().remove(e.getValue())) removed++;
    }
    return removed;
  }

  /** Returns the matched padding prefix of a class (e.g. "p-", "px-"), or "" if none. */
  private static String paddingPrefix(String cssClass) {
    for (String prefix : new String[] {"px-", "py-", "pt-", "pr-", "pb-", "pl-", "p-"}) {
      if (cssClass.startsWith(prefix)) return prefix;
    }
    return "";
  }

  /** Converts a set of primitive sides back into canonical padding utility prefixes. */
  private static List<String> decomposeSidePrefixes(Set<String> sides) {
    List<String> out = new ArrayList<>(2);
    boolean hasTop = sides.contains(TOP);
    boolean hasBottom = sides.contains(BOTTOM);
    boolean hasLeft = sides.contains(LEFT);
    boolean hasRight = sides.contains(RIGHT);
    if (hasTop && hasBottom && !hasLeft && !hasRight) out.add("py-");
    else if (hasLeft && hasRight && !hasTop && !hasBottom) out.add("px-");
    else {
      if (hasTop) out.add("pt-");
      if (hasRight) out.add("pr-");
      if (hasBottom) out.add("pb-");
      if (hasLeft) out.add("pl-");
    }
    return out;
  }

  // Internos

  /**
   * Finds the conflict category for a CSS class, with responsive prefix support.
   *
   * <p>Responsive classes like {@code md:w-4} are treated as a separate category ({@code md:w})
   * from their base class ({@code w}), so they do not conflict across breakpoints but DO conflict
   * within the same breakpoint:
   *
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
      String bpPrefix = m.group(1); // "md:"
      String baseClass = m.group(2); // "w-4"
      String baseCat = findBaseCategory(baseClass);
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

  /**
   * Removes all classes from the node that belong to a category.
   *
   * @return the number of classes actually removed
   */
  private static int removeCategory(Node node, String category, String except) {
    List<String> prefixes = CATEGORY_TO_PREFIXES.getOrDefault(category, List.of());
    int[] count = {0};
    node.getStyleClass()
        .removeIf(
            cls -> {
              boolean hit =
                  !cls.equals(except) && prefixes.stream().anyMatch(p -> matchesPrefix(cls, p));
              if (hit) count[0]++;
              return hit;
            });
    return count[0];
  }

  /** If a class matches a prefix (exact or as start) */
  private static boolean matchesPrefix(String cssClass, String prefix) {
    if (!prefix.endsWith("-")) return cssClass.equals(prefix);
    return cssClass.startsWith(prefix);
  }
}
