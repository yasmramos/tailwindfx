/**
 * TailwindFX — Utility-first UI framework for JavaFX.
 *
 * <p>Provides CSS utility classes, JIT compilation, responsive primitives,
 * component library, animations, and theme management for JavaFX applications.
 */
module tailwindfx {
    requires javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires java.logging;
    requires java.prefs;

    exports tailwindfx;
}
