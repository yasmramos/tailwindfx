package tailwindfx;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Shear;

/**
 * Styles — API Java para todo lo que CSS no puede hacer en JavaFX.
 *
 * Cada método corresponde a una clase Tailwind comentada en tailwindfx.css
 * con "use X in Java". La nomenclatura es idéntica a Tailwind para que
 * el mapping sea inmediato:
 *
 *   CSS class comentada          →  método Java
 *   .col-span-3                  →  Styles.colSpan(node, 3)
 *   .row-span-full               →  Styles.rowSpanFull(node)
 *   .mx-4                        →  Styles.mx(node, 4)
 *   .z-50                        →  Styles.z(node, 50)
 *   .self-center                 →  Styles.selfCenter(node)
 *   .justify-self-end            →  Styles.justifySelfEnd(node)
 *   .flex-1                      →  Styles.flex1(node)
 *   .grayscale                   →  Styles.grayscale(node)
 *   .brightness-75               →  Styles.brightness(node, 0.75)
 *   .skew-x-6                    →  Styles.skewX(node, 6)
 *   .object-cover                →  Styles.objectCover(imageView)
 *   .order-first                 →  Styles.orderFirst(node)
 *
 * Todos los métodos devuelven el nodo recibido para permitir encadenamiento:
 *   Styles.colSpan(Styles.mx(card, 4), 3);
 */
public final class Styles {

    // Escala Tailwind: 1 unidad = 4px
    private static final double UNIT = 4.0;

    private Styles() {}

    // =========================================================================
    // GRIDPANE — col-span-*, row-span-*
    // Corresponde a: grid-column: span N / grid-row: span N en CSS web
    // =========================================================================

    /**
     * Sets the column span of a node in a {@link GridPane} (equivalent to {@code .col-span-N}).
     *
     * @param node the node to span
     * @param span number of columns to span (must be >= 1)
     * @param <T>  node type, returned unchanged for chaining
     * @return the same node
     * @throws IllegalArgumentException if node is null or span < 1
     */
    public static <T extends Node> T colSpan(T node, int span) {
        Preconditions.requireNode(node, "Styles.colSpan");
        Preconditions.requireSpan(span, "Styles.colSpan");
        GridPane.setColumnSpan(node, span);
        return node;
    }

    /** .col-span-full — ocupa todas las columnas del GridPane */
    public static <T extends Node> T colSpanFull(T node) {
        Preconditions.requireNode(node, "Styles.colSpanFull");
        GridPane.setColumnSpan(node, GridPane.REMAINING);
        return node;
    }

    /**
     * Sets the row span of a node in a {@link GridPane} (equivalent to {@code .row-span-N}).
     *
     * @param node the node to span
     * @param span number of rows to span (must be >= 1)
     * @param <T>  node type, returned unchanged for chaining
     * @return the same node
     * @throws IllegalArgumentException if node is null or span < 1
     */
    public static <T extends Node> T rowSpan(T node, int span) {
        Preconditions.requireNode(node, "Styles.rowSpan");
        Preconditions.requireSpan(span, "Styles.rowSpan");
        GridPane.setRowSpan(node, span);
        return node;
    }

    /** .row-span-full — ocupa todas las filas del GridPane */
    public static <T extends Node> T rowSpanFull(T node) {
        Preconditions.requireNode(node, "Styles.rowSpanFull");
        GridPane.setRowSpan(node, GridPane.REMAINING);
        return node;
    }

    /** Atajo: colSpan(2) */
    public static <T extends Node> T colSpan2(T node)  { return colSpan(node, 2); }
    public static <T extends Node> T colSpan3(T node)  { return colSpan(node, 3); }
    public static <T extends Node> T colSpan4(T node)  { return colSpan(node, 4); }
    public static <T extends Node> T colSpan6(T node)  { return colSpan(node, 6); }
    public static <T extends Node> T colSpan8(T node)  { return colSpan(node, 8); }
    public static <T extends Node> T colSpan12(T node) { return colSpan(node, 12); }
    public static <T extends Node> T rowSpan2(T node)  { return rowSpan(node, 2); }
    public static <T extends Node> T rowSpan3(T node)  { return rowSpan(node, 3); }
    public static <T extends Node> T rowSpan4(T node)  { return rowSpan(node, 4); }
    public static <T extends Node> T rowSpan6(T node)  { return rowSpan(node, 6); }

    // =========================================================================
    // GRIDPANE — posición explícita col-start-*, row-start-*
    // =========================================================================

    /**
     * Positions a node at a specific cell in a {@link GridPane}.
     *
     * @param node the node to position
     * @param col  zero-based column index
     * @param row  zero-based row index
     * @param <T>  node type, returned for chaining
     * @return the same node
     * @throws IllegalArgumentException if node is null or col/row are negative
     */
    public static <T extends Node> T gridCell(T node, int col, int row) {
        Preconditions.requireNode(node, "Styles.gridCell");
        if (col < 0) throw new IllegalArgumentException("Styles.gridCell: col must be >= 0, got: " + col);
        if (row < 0) throw new IllegalArgumentException("Styles.gridCell: row must be >= 0, got: " + row);
        GridPane.setColumnIndex(node, col);
        GridPane.setRowIndex(node, row);
        return node;
    }

    public static <T extends Node> T colStart(T node, int col) {
        GridPane.setColumnIndex(node, col);
        return node;
    }

    public static <T extends Node> T rowStart(T node, int row) {
        GridPane.setRowIndex(node, row);
        return node;
    }

    // =========================================================================
    // GROW/SHRINK — flex-1, flex-grow, flex-shrink, flex-none
    // Corresponde a: flex-grow, flex-shrink en CSS web
    // =========================================================================

    /** .flex-1 / .grow — crece para llenar espacio horizontal (HBox) */
    public static <T extends Node> T flex1(T node) {
        Preconditions.requireNode(node, "Styles.flex1");
        Preconditions.warnNoParent(node, "Styles.flex1");
        HBox.setHgrow(node, Priority.ALWAYS);
        return node;
    }

    /** .grow — crece para llenar espacio horizontal */
    public static <T extends Node> T grow(T node) {
        Preconditions.requireNode(node, "Styles.grow");
        Preconditions.warnNoParent(node, "Styles.grow");
        HBox.setHgrow(node, Priority.ALWAYS);
        return node;
    }

    /** .grow-0 / .flex-none — no crece */
    public static <T extends Node> T growNone(T node) {
        HBox.setHgrow(node, Priority.NEVER);
        VBox.setVgrow(node, Priority.NEVER);
        return node;
    }

    /** .flex-auto — crece y encoge según sea necesario */
    public static <T extends Node> T flexAuto(T node) {
        HBox.setHgrow(node, Priority.SOMETIMES);
        return node;
    }

    /** Crece verticalmente en VBox */
    public static <T extends Node> T vgrow(T node) {
        VBox.setVgrow(node, Priority.ALWAYS);
        return node;
    }

    /** .hbox-fill-width — el nodo llena el ancho disponible en HBox */
    public static <T extends Node> T hboxFillWidth(T node) {
        HBox.setHgrow(node, Priority.ALWAYS);
        if (node instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        return node;
    }

    /** .vbox-fill-height — el nodo llena el alto disponible en VBox */
    public static <T extends Node> T vboxFillHeight(T node) {
        VBox.setVgrow(node, Priority.ALWAYS);
        if (node instanceof Region r) r.setMaxHeight(Double.MAX_VALUE);
        return node;
    }

    // =========================================================================
    // MARGIN — mx-*, my-*, mt-*, mr-*, mb-*, ml-*
    // Corresponde a: margin en CSS web
    // NOTA: JavaFX no tiene margin CSS. Se usa setMargin() en el contenedor.
    // =========================================================================

    /**
     * IMPORTANTE — orden de llamada con métodos de margen.
     *
     * <p>JavaFX aplica los constraints ({@code HBox.setMargin}, etc.) en el momento
     * de la llamada. El nodo debe estar ya añadido al padre para que el margen
     * tenga efecto inmediato.
     *
     * <pre>
     * // Correcto — añadir primero, margen después
     * parent.getChildren().add(node);
     * Styles.mx(node, 4);
     *
     * // Incorrecto — el constraint se aplica sin padre, sin efecto visible
     * Styles.mx(node, 4);
     * parent.getChildren().add(node);
     * </pre>
     *
     * <p>Para evitar el problema usa los métodos {@code WithParent} que añaden
     * el nodo y aplican el margen en un único paso:
     * <pre>
     * Styles.mxWithParent(node, hbox, 4);
     * Styles.addWithMargin(hbox, 2, nodeA, nodeB, nodeC);
     * </pre>
     */
    /**
     * Applies uniform margin on all sides (equivalent to {@code .m-N}).
     *
     * <p>Sets {@code HBox.setMargin}, {@code VBox.setMargin} and {@code GridPane.setMargin}.
     * The node must already be added to its parent for the margin to take effect immediately.
     *
     * @param node the node to margin (must be in a parent container)
     * @param n    margin in Tailwind units (n × 4px)
     * @param <T>  node type, returned for chaining
     * @return the same node
     */
    public static <T extends Node> T m(T node, int n) {
        Preconditions.requireNode(node, "Styles.m");
        Preconditions.warnNoParent(node, "Styles.m");
        Insets insets = new Insets(n * UNIT);
        HBox.setMargin(node, insets);
        VBox.setMargin(node, insets);
        GridPane.setMargin(node, insets);
        return node;
    }

    /**
     * Applies horizontal margin (left + right) equivalent to {@code .mx-N}.
     *
     * @param node the node to margin
     * @param n    margin in Tailwind units (n × 4px per side)
     * @param <T>  node type, returned for chaining
     * @return the same node
     */
    public static <T extends Node> T mx(T node, int n) {
        Preconditions.requireNode(node, "Styles.mx");
        Preconditions.warnNoParent(node, "Styles.mx");
        double px = n * UNIT;
        Insets insets = new Insets(0, px, 0, px);
        HBox.setMargin(node, insets);
        VBox.setMargin(node, insets);
        GridPane.setMargin(node, insets);
        return node;
    }

    /**
     * Applies vertical margin (top + bottom) equivalent to {@code .my-N}.
     *
     * @param node the node to margin
     * @param n    margin in Tailwind units (n × 4px per side)
     * @param <T>  node type, returned for chaining
     * @return the same node
     */
    public static <T extends Node> T my(T node, int n) {
        Preconditions.requireNode(node, "Styles.my");
        Preconditions.warnNoParent(node, "Styles.my");
        double px = n * UNIT;
        Insets insets = new Insets(px, 0, px, 0);
        HBox.setMargin(node, insets);
        VBox.setMargin(node, insets);
        GridPane.setMargin(node, insets);
        return node;
    }

    /** .mt-{n} — margen superior */
    public static <T extends Node> T mt(T node, int n) {
        return margin(node, n * UNIT, 0, 0, 0);
    }

    /** .mr-{n} — margen derecho */
    public static <T extends Node> T mr(T node, int n) {
        return margin(node, 0, n * UNIT, 0, 0);
    }

    /** .mb-{n} — margen inferior */
    public static <T extends Node> T mb(T node, int n) {
        return margin(node, 0, 0, n * UNIT, 0);
    }

    /** .ml-{n} — margen izquierdo */
    public static <T extends Node> T ml(T node, int n) {
        return margin(node, 0, 0, 0, n * UNIT);
    }

    /** .mx-auto — centra horizontalmente en HBox usando espaciadores */
    public static <T extends Node> T mxAuto(T node) {
        HBox.setHgrow(node, Priority.ALWAYS);
        return node;
    }

    /** Margen con valores exactos en px */
    public static <T extends Node> T margin(T node, double top, double right, double bottom, double left) {
        Preconditions.requireNode(node, "Styles.margin");
        Preconditions.warnNoParent(node, "Styles.margin");
        Insets insets = new Insets(top, right, bottom, left);
        HBox.setMargin(node, insets);
        VBox.setMargin(node, insets);
        GridPane.setMargin(node, insets);
        return node;
    }

    // =========================================================================
    // MARGIN WithParent — añaden el nodo Y aplican el margen en un solo paso
    // Eliminan el problema del orden de llamada descrito en el javadoc de margin.
    // =========================================================================

    /**
     * Añade {@code node} a {@code parent} y aplica margen en todos los lados.
     *
     * <p>Equivale a:
     * <pre>
     * parent.getChildren().add(node);
     * Styles.m(node, n);
     * </pre>
     */
    public static <T extends Node> T mWithParent(T node, Pane parent, int n) {
        Preconditions.requireNode(node, "Styles.mWithParent");
        Preconditions.requireNonNull(parent, "Styles.mWithParent", "parent");
        if (!parent.getChildren().contains(node)) parent.getChildren().add(node);
        return m(node, n);
    }

    /**
     * Añade {@code node} a {@code parent} y aplica margen horizontal (n × 4px).
     *
     * <p>Equivale a:
     * <pre>
     * parent.getChildren().add(node);
     * Styles.mx(node, n);
     * </pre>
     */
    public static <T extends Node> T mxWithParent(T node, Pane parent, int n) {
        Preconditions.requireNode(node, "Styles.mxWithParent");
        Preconditions.requireNonNull(parent, "Styles.mxWithParent", "parent");
        if (!parent.getChildren().contains(node)) parent.getChildren().add(node);
        return mx(node, n);
    }

    /**
     * Añade {@code node} a {@code parent} y aplica margen vertical (n × 4px).
     */
    public static <T extends Node> T myWithParent(T node, Pane parent, int n) {
        Preconditions.requireNode(node, "Styles.myWithParent");
        Preconditions.requireNonNull(parent, "Styles.myWithParent", "parent");
        if (!parent.getChildren().contains(node)) parent.getChildren().add(node);
        return my(node, n);
    }

    /**
     * Añade {@code node} a {@code parent} y aplica márgenes individuales en px.
     *
     * @param top    margen superior en px
     * @param right  margen derecho en px
     * @param bottom margen inferior en px
     * @param left   margen izquierdo en px
     */
    public static <T extends Node> T marginWithParent(T node, Pane parent,
                                                       double top, double right,
                                                       double bottom, double left) {
        Preconditions.requireNode(node, "Styles.marginWithParent");
        Preconditions.requireNonNull(parent, "Styles.marginWithParent", "parent");
        if (!parent.getChildren().contains(node)) parent.getChildren().add(node);
        return margin(node, top, right, bottom, left);
    }

    /**
     * Añade varios nodos a un padre con el mismo margen (n × 4px) aplicado a cada uno.
     *
     * <pre>
     * // Añade tres cards a un HBox con 8px de margen cada una
     * Styles.addWithMargin(hbox, 2, card1, card2, card3);
     * </pre>
     *
     * @param parent contenedor destino
     * @param n      unidades Tailwind (n × 4px)
     * @param nodes  nodos a añadir
     */
    @SafeVarargs
    public static <T extends Node> void addWithMargin(Pane parent, int n, T... nodes) {
        Preconditions.requireNonNull(parent, "Styles.addWithMargin", "parent");
        for (T node : nodes) {
            if (node == null) continue;
            if (!parent.getChildren().contains(node)) parent.getChildren().add(node);
            m(node, n);
        }
    }

    // =========================================================================
    // Z-ORDER — z-0, z-10, z-20, z-30, z-40, z-50, z-auto
    // Corresponde a: z-index en CSS web
    // NOTA: En JavaFX, viewOrder más BAJO = más adelante (al revés que z-index)
    // =========================================================================

    /**
     * .z-{value} — controla el orden de pintado del nodo.
     * JavaFX viewOrder: 0 = frente, valores positivos = más atrás.
     * Se invierte respecto a z-index web (donde mayor = más adelante).
     */
    public static <T extends Node> T z(T node, int zIndex) {
        Preconditions.requireNode(node, "Styles.z");
        // Invertir: z-50 en Tailwind = más adelante = viewOrder negativo
        node.setViewOrder(-zIndex);
        return node;
    }

    public static <T extends Node> T z0(T node)    { return z(node, 0); }
    public static <T extends Node> T z10(T node)   { return z(node, 10); }
    public static <T extends Node> T z20(T node)   { return z(node, 20); }
    public static <T extends Node> T z30(T node)   { return z(node, 30); }
    public static <T extends Node> T z40(T node)   { return z(node, 40); }
    public static <T extends Node> T z50(T node)   { return z(node, 50); }

    /** .order-first — lleva el nodo al frente */
    public static <T extends Node> T orderFirst(T node) {
        node.toFront();
        return node;
    }

    /** .order-last — lleva el nodo al fondo */
    public static <T extends Node> T orderLast(T node) {
        node.toBack();
        return node;
    }

    // =========================================================================
    // SELF ALIGNMENT — self-start, self-center, self-end, self-stretch
    // Corresponde a: align-self en CSS web (GridPane)
    // =========================================================================

    public static <T extends Node> T selfStart(T node) {
        GridPane.setValignment(node, VPos.TOP);
        return node;
    }

    public static <T extends Node> T selfCenter(T node) {
        GridPane.setValignment(node, VPos.CENTER);
        return node;
    }

    public static <T extends Node> T selfEnd(T node) {
        GridPane.setValignment(node, VPos.BOTTOM);
        return node;
    }

    public static <T extends Node> T selfBaseline(T node) {
        GridPane.setValignment(node, VPos.BASELINE);
        return node;
    }

    // =========================================================================
    // JUSTIFY SELF — justify-self-start, justify-self-center, justify-self-end
    // Corresponde a: justify-self en CSS web (GridPane)
    // =========================================================================

    public static <T extends Node> T justifySelfStart(T node) {
        GridPane.setHalignment(node, HPos.LEFT);
        return node;
    }

    public static <T extends Node> T justifySelfCenter(T node) {
        GridPane.setHalignment(node, HPos.CENTER);
        return node;
    }

    public static <T extends Node> T justifySelfEnd(T node) {
        GridPane.setHalignment(node, HPos.RIGHT);
        return node;
    }

    // =========================================================================
    // COLORADJUST FILTERS — grayscale, brightness, contrast, saturate,
    //                        hueRotate, invert, sepia
    // Corresponde a: filter: grayscale(), brightness(), etc. en CSS web
    // =========================================================================

    /** .grayscale — elimina el color del nodo */
    public static <T extends Node> T grayscale(T node) {
        Preconditions.requireNode(node, "Styles.grayscale");
        node.setEffect(new ColorAdjust(0, -1, 0, 0));
        return node;
    }

    /** .grayscale-0 — elimina el filtro de escala de grises */
    public static <T extends Node> T grayscale0(T node) {
        if (node.getEffect() instanceof ColorAdjust) node.setEffect(null);
        return node;
    }

    /**
     * .brightness-{n} — ajusta el brillo (0=negro, 1=normal, 2=doble)
     * Tailwind usa 0-200, aquí recibe la fracción directamente.
     * brightness(0.5) = oscurecer a la mitad, brightness(1.5) = aclarar 50%
     */
    public static <T extends Node> T brightness(T node, double value) {
        Preconditions.requireNode(node, "Styles.brightness");
        Preconditions.warnBrightnessRange(value, "Styles.brightness");
        // ColorAdjust brightness: -1 (negro) a 1 (blanco), 0 = normal
        double adjusted = value - 1.0;  // 1.0 → 0, 0.5 → -0.5, 1.5 → 0.5
        applyOrMergeColorAdjust(node, adjusted, Double.NaN, Double.NaN, Double.NaN);
        return node;
    }

    /**
     * .contrast-{n} — ajusta el contraste (1 = normal)
     */
    public static <T extends Node> T contrast(T node, double value) {
        Preconditions.requireNode(node, "Styles.contrast");
        double adjusted = value - 1.0;
        applyOrMergeColorAdjust(node, Double.NaN, Double.NaN, adjusted, Double.NaN);
        return node;
    }

    /**
     * .saturate-{n} — ajusta la saturación (0 = gris, 1 = normal, >1 = más vivo)
     */
    public static <T extends Node> T saturate(T node, double value) {
        Preconditions.requireNode(node, "Styles.saturate");
        double adjusted = value - 1.0;
        applyOrMergeColorAdjust(node, Double.NaN, adjusted, Double.NaN, Double.NaN);
        return node;
    }

    /**
     * .hue-rotate-{deg} — rota el matiz en grados (0-360)
     */
    public static <T extends Node> T hueRotate(T node, double degrees) {
        Preconditions.requireNode(node, "Styles.hueRotate");
        // ColorAdjust hue: -1 a 1 (representa -180° a +180°)
        double adjusted = degrees / 180.0;
        applyOrMergeColorAdjust(node, Double.NaN, Double.NaN, Double.NaN, adjusted);
        return node;
    }

    /**
     * .invert — invierte los colores del nodo
     */
    public static <T extends Node> T invert(T node) {
        Preconditions.requireNode(node, "Styles.invert");
        // No hay propiedad directa en ColorAdjust para invert total
        // Se aproxima con hue=1 + brightness=-1
        node.setEffect(new ColorAdjust(1.0, 0, -0.5, 0));
        return node;
    }

    /**
     * .sepia — aplica tono sepia al nodo
     */
    public static <T extends Node> T sepia(T node) {
        Preconditions.requireNode(node, "Styles.sepia");
        // Sepia: desaturar + shift de tono cálido
        node.setEffect(new ColorAdjust(0.1, -0.5, 0.1, 0));
        return node;
    }

    /** Elimina todos los filtros ColorAdjust del nodo */
    public static <T extends Node> T filterNone(T node) {
        if (node.getEffect() instanceof ColorAdjust) node.setEffect(null);
        return node;
    }

    // =========================================================================
    // SKEW — skew-x-*, skew-y-*
    // Corresponde a: transform: skewX(), skewY() en CSS web
    // =========================================================================

    /**
     * .skew-x-{degrees} — inclina el nodo en el eje X
     */
    public static <T extends Node> T skewX(T node, double degrees) {
        Preconditions.requireNode(node, "Styles.skewX");
        double kx = Math.tan(Math.toRadians(degrees));
        applyOrMergeShear(node, kx, 0);
        return node;
    }

    /**
     * .skew-y-{degrees} — inclina el nodo en el eje Y
     */
    public static <T extends Node> T skewY(T node, double degrees) {
        Preconditions.requireNode(node, "Styles.skewY");
        double ky = Math.tan(Math.toRadians(degrees));
        applyOrMergeShear(node, 0, ky);
        return node;
    }

    /** Atajos para valores Tailwind estándar */
    public static <T extends Node> T skewX1(T node)  { return skewX(node, 1); }
    public static <T extends Node> T skewX2(T node)  { return skewX(node, 2); }
    public static <T extends Node> T skewX3(T node)  { return skewX(node, 3); }
    public static <T extends Node> T skewX6(T node)  { return skewX(node, 6); }
    public static <T extends Node> T skewX12(T node) { return skewX(node, 12); }
    public static <T extends Node> T skewY1(T node)  { return skewY(node, 1); }
    public static <T extends Node> T skewY2(T node)  { return skewY(node, 2); }
    public static <T extends Node> T skewY3(T node)  { return skewY(node, 3); }
    public static <T extends Node> T skewY6(T node)  { return skewY(node, 6); }
    public static <T extends Node> T skewY12(T node) { return skewY(node, 12); }

    // =========================================================================
    // IMAGEVIEW — object-fit, object-position, object-cover, object-contain
    // Corresponde a: object-fit, object-position en CSS web
    // =========================================================================

    /** .object-cover — rellena el área recortando si es necesario */
    public static ImageView objectCover(ImageView iv) {
        Preconditions.requireNonNull(iv, "Styles.objectCover", "imageView");
        iv.setPreserveRatio(false);
        return iv;
    }

    /** .object-contain — encaja la imagen completa preservando proporción */
    public static ImageView objectContain(ImageView iv) {
        Preconditions.requireNonNull(iv, "Styles.objectContain", "imageView");
        iv.setPreserveRatio(true);
        return iv;
    }

    /** .object-fill — estira para llenar exactamente (distorsiona) */
    public static ImageView objectFill(ImageView iv) {
        iv.setPreserveRatio(false);
        iv.setSmooth(true);
        return iv;
    }

    /**
     * .object-top — muestra la parte superior de la imagen
     * Requiere que fitWidth y fitHeight estén definidos.
     */
    public static ImageView objectTop(ImageView iv) {
        if (iv.getImage() != null) {
            double w = iv.getFitWidth()  > 0 ? iv.getFitWidth()  : iv.getImage().getWidth();
            double h = iv.getFitHeight() > 0 ? iv.getFitHeight() : iv.getImage().getHeight();
            iv.setViewport(new javafx.geometry.Rectangle2D(0, 0, iv.getImage().getWidth(), h));
        }
        return iv;
    }

    /** .object-center — muestra el centro de la imagen */
    public static ImageView objectCenter(ImageView iv) {
        Preconditions.requireNonNull(iv, "Styles.objectCenter", "imageView");
        Preconditions.warnNoImage(iv, "Styles.objectCenter");
        if (iv.getImage() != null) {
            double iw = iv.getImage().getWidth();
            double ih = iv.getImage().getHeight();
            double fw = iv.getFitWidth()  > 0 ? iv.getFitWidth()  : iw;
            double fh = iv.getFitHeight() > 0 ? iv.getFitHeight() : ih;
            double x  = Math.max(0, (iw - fw) / 2);
            double y  = Math.max(0, (ih - fh) / 2);
            iv.setViewport(new javafx.geometry.Rectangle2D(x, y, Math.min(fw, iw), Math.min(fh, ih)));
        }
        return iv;
    }

    /** Establece tamaño del ImageView y modo de ajuste en una sola llamada */
    public static ImageView imgSize(ImageView iv, double width, double height, boolean preserveRatio) {
        iv.setFitWidth(width);
        iv.setFitHeight(height);
        iv.setPreserveRatio(preserveRatio);
        return iv;
    }

    // =========================================================================
    // NODE VISIBILITY — hidden, visible, managed
    // =========================================================================

    /** .hidden — oculta el nodo pero mantiene su espacio en el layout */
    public static <T extends Node> T invisible(T node) {
        Preconditions.requireNode(node, "Styles.invisible");
        node.setOpacity(0);
        return node;
    }

    /** .hidden-node — oculta el nodo Y elimina su espacio del layout */
    public static <T extends Node> T hiddenNode(T node) {
        Preconditions.requireNode(node, "Styles.hiddenNode");
        node.setVisible(false);
        node.setManaged(false);
        return node;
    }

    /** Restaura visibilidad y espacio en layout */
    public static <T extends Node> T show(T node) {
        Preconditions.requireNode(node, "Styles.show");
        node.setVisible(true);
        node.setManaged(true);
        node.setOpacity(1);
        return node;
    }

    // =========================================================================
    // INTERNOS
    // =========================================================================

    private static void applyOrMergeColorAdjust(Node node, double brightness, double saturation,
                                                  double contrast, double hue) {
        ColorAdjust ca = (node.getEffect() instanceof ColorAdjust existing)
            ? existing : new ColorAdjust();
        if (!Double.isNaN(brightness))  ca.setBrightness(brightness);
        if (!Double.isNaN(saturation))  ca.setSaturation(saturation);
        if (!Double.isNaN(contrast))    ca.setContrast(contrast);
        if (!Double.isNaN(hue))         ca.setHue(hue);
        node.setEffect(ca);
    }

    private static void applyOrMergeShear(Node node, double kx, double ky) {
        // Remove existing Shear if present, then add new one
        node.getTransforms().removeIf(t -> t instanceof Shear);
        if (kx != 0 || ky != 0) {
            node.getTransforms().add(new Shear(kx, ky));
        }
    }
    // =========================================================================
    // TEXT-SHADOW — Java API (DropShadow on Text child of Label/Text node)
    // =========================================================================

    /**
     * Applies a text shadow to a {@link javafx.scene.text.Text} or
     * {@link javafx.scene.control.Label} node (analogous to CSS {@code text-shadow}).
     *
     * <p>Unlike the CSS {@code .text-shadow-*} classes (which apply DropShadow
     * to the whole Label box), this method targets the {@code Text} child inside
     * a {@code Label} for a true text-only shadow.
     *
     * <pre>
     * // text-shadow: 0 2px 4px rgba(0,0,0,0.30)
     * Styles.textShadow(heading, "rgba(0,0,0,0.30)", 4, 0, 2);
     *
     * // Named presets:
     * Styles.textShadowSm(label);
     * Styles.textShadowLg(label);
     * Styles.textShadowColor(label, "#3b82f6", 6, 0, 2); // colored
     * </pre>
     *
     * @param node    the Text or Label node
     * @param color   CSS color string (hex, rgba, color name)
     * @param radius  blur radius in px
     * @param offsetX horizontal offset in px
     * @param offsetY vertical offset in px
     */
    public static void textShadow(Node node, String color, double radius,
                                   double offsetX, double offsetY) {
        Preconditions.requireNonNull(node, "Styles.textShadow", "node");
        Preconditions.requireNonBlank(color, "Styles.textShadow", "color");
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setColor(javafx.scene.paint.Color.web(color));
        ds.setRadius(radius);
        ds.setOffsetX(offsetX);
        ds.setOffsetY(offsetY);
        ds.setSpread(0);
        node.setEffect(ds);
    }

    /** {@code text-shadow-sm} — subtle shadow. */
    public static void textShadowSm(Node node) {
        textShadow(node, "rgba(0,0,0,0.20)", 2, 0, 1);
    }

    /** {@code text-shadow} — standard shadow. */
    public static void textShadowMd(Node node) {
        textShadow(node, "rgba(0,0,0,0.30)", 4, 0, 2);
    }

    /** {@code text-shadow-lg} — large shadow. */
    public static void textShadowLg(Node node) {
        textShadow(node, "rgba(0,0,0,0.40)", 10, 0, 4);
    }

    /** {@code text-shadow-xl} — extra large shadow. */
    public static void textShadowXl(Node node) {
        textShadow(node, "rgba(0,0,0,0.50)", 16, 0, 6);
    }

    /**
     * Colored text shadow — analogous to Tailwind v4.1's arbitrary text-shadow
     * with a named color.
     *
     * @param node   the Text or Label node
     * @param color  color string (hex, rgba)
     * @param radius blur radius
     * @param offsetX horizontal offset
     * @param offsetY vertical offset
     */
    public static void textShadowColor(Node node, String color,
                                        double radius, double offsetX, double offsetY) {
        textShadow(node, color, radius, offsetX, offsetY);
    }

    /** Removes any text shadow (or other effect) from the node. */
    public static void textShadowNone(Node node) {
        Preconditions.requireNonNull(node, "Styles.textShadowNone", "node");
        if (node.getEffect() instanceof javafx.scene.effect.DropShadow)
            node.setEffect(null);
    }

    // =========================================================================
    // COLORED DROP-SHADOW — arbitrary color dropshadow
    // =========================================================================

    /**
     * Applies a colored drop shadow (analogous to Tailwind v4.1's
     * {@code drop-shadow-[color]} arbitrary utility).
     *
     * <pre>
     * // drop-shadow-blue/50 approximation:
     * Styles.dropShadow(card, "#3b82f6", 0.5, 12, 0, 4);
     *
     * // drop-shadow-none:
     * Styles.dropShadowNone(card);
     * </pre>
     *
     * @param node    the node to shadow
     * @param hexColor hex color (e.g. {@code "#3b82f6"})
     * @param alpha   alpha multiplier [0.0, 1.0]
     * @param radius  blur radius
     * @param offsetX horizontal offset
     * @param offsetY vertical offset
     */
    public static void dropShadow(Node node, String hexColor, double alpha,
                                   double radius, double offsetX, double offsetY) {
        Preconditions.requireNonNull(node, "Styles.dropShadow", "node");
        Preconditions.requireNonBlank(hexColor, "Styles.dropShadow", "hexColor");
        alpha = Math.max(0, Math.min(1, alpha));
        javafx.scene.paint.Color base = javafx.scene.paint.Color.web(hexColor);
        javafx.scene.paint.Color colored = new javafx.scene.paint.Color(
            base.getRed(), base.getGreen(), base.getBlue(), alpha);
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow(
            javafx.scene.effect.BlurType.GAUSSIAN, colored, radius, 0, offsetX, offsetY);
        node.setEffect(ds);
    }

    /** Colored drop shadow — presets by semantic color. */
    public static void dropShadowBlue(Node n)   { dropShadow(n, "#3b82f6", 0.4, 12, 0, 4); }
    public static void dropShadowGreen(Node n)  { dropShadow(n, "#22c55e", 0.4, 12, 0, 4); }
    public static void dropShadowRed(Node n)    { dropShadow(n, "#ef4444", 0.4, 12, 0, 4); }
    public static void dropShadowPurple(Node n) { dropShadow(n, "#a855f7", 0.4, 12, 0, 4); }
    public static void dropShadowYellow(Node n) { dropShadow(n, "#eab308", 0.4, 12, 0, 4); }

    /** Removes drop shadow effect from node. */
    public static void dropShadowNone(Node node) { filterNone(node); }

    // =========================================================================
    // MASK / CLIP UTILITIES
    // =========================================================================

    /**
     * Clips a node to a rectangle with optional corner rounding
     * (analogous to CSS {@code clip-path: inset(...)}).
     *
     * @param node   the node to clip
     * @param width  clip width in px
     * @param height clip height in px
     * @param radius corner radius in px (0 = sharp corners)
     * @return the same node, for chaining
     */
    public static <T extends Node> T clipRect(T node, double width, double height, double radius) {
        Preconditions.requireNonNull(node, "Styles.clipRect", "node");
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(width, height);
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        node.setClip(clip);
        return node;
    }

    /**
     * Clips a node to a rectangle without corner rounding.
     *
     * @param node   the node to clip
     * @param width  clip width in px
     * @param height clip height in px
     * @return the same node, for chaining
     */
    public static <T extends Node> T clipRect(T node, double width, double height) {
        return clipRect(node, width, height, 0);
    }

    /**
     * Clips a node to a circle centered in its bounds (analogous to
     * CSS {@code mask-image: radial-gradient(circle, ...)}).
     *
     * <p>Useful for avatar images and circular buttons.
     *
     * <pre>
     * Styles.clipCircle(avatarImageView);
     * // The node is now circular. Re-call after resize to update the clip.
     * </pre>
     *
     * @param node the node to clip
     */
    public static void clipCircle(Node node) {
        Preconditions.requireNonNull(node, "Styles.clipCircle", "node");
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle();
        // Bind radius to half of min(width, height) — updates on resize
        node.layoutBoundsProperty().addListener((obs, ov, nv) -> {
            double r = Math.min(nv.getWidth(), nv.getHeight()) / 2.0;
            clip.setCenterX(nv.getWidth()  / 2.0);
            clip.setCenterY(nv.getHeight() / 2.0);
            clip.setRadius(r);
        });
        node.setClip(clip);
    }

    /**
     * Clips a node to a rounded rectangle (analogous to CSS
     * {@code overflow: hidden} + {@code border-radius}).
     *
     * <pre>
     * Styles.clipRounded(imageView, 12); // 12px radius
     * </pre>
     *
     * @param node   the node to clip
     * @param radius corner radius in px
     */
    public static void clipRounded(Node node, double radius) {
        Preconditions.requireNonNull(node, "Styles.clipRounded", "node");
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        node.layoutBoundsProperty().addListener((obs, ov, nv) -> {
            clip.setWidth(nv.getWidth());
            clip.setHeight(nv.getHeight());
        });
        node.setClip(clip);
    }

    /**
     * Clips a node to an arbitrary {@link javafx.scene.shape.Shape}
     * (analogous to CSS {@code mask-image} / {@code clip-path}).
     *
     * @param node  the node to clip
     * @param shape the clipping shape (coordinates should match node bounds)
     */
    public static void clipMask(Node node, javafx.scene.shape.Shape shape) {
        Preconditions.requireNonNull(node, "Styles.clipMask", "node");
        Preconditions.requireNonNull(shape, "Styles.clipMask", "shape");
        node.setClip(shape);
    }

    /** Removes any clip/mask from the node. */
    public static void clipNone(Node node) {
        Preconditions.requireNonNull(node, "Styles.clipNone", "node");
        node.setClip(null);
    }

    // =========================================================================
    // SVG HELPERS — fill-current, stroke-current
    // =========================================================================

    /**
     * Sets the fill color of a {@link javafx.scene.shape.Shape} node
     * to match the text color of a reference node (analogous to CSS
     * {@code fill: currentColor}).
     *
     * <pre>
     * Label label = new Label("Click me");
     * TailwindFX.apply(label, "text-blue-500");
     * SVGPath icon = loadIcon();
     * Styles.fillCurrent(icon, label); // icon fill = label text color
     * </pre>
     *
     * @param shape     the SVG shape whose fill to set
     * @param reference the node whose text color to read
     */
    public static void fillCurrent(javafx.scene.shape.Shape shape, Node reference) {
        Preconditions.requireNonNull(shape,     "Styles.fillCurrent", "shape");
        Preconditions.requireNonNull(reference, "Styles.fillCurrent", "reference");
        javafx.scene.paint.Paint fill = (javafx.scene.paint.Paint)
            reference.getProperties().getOrDefault(
                "tailwindfx.text.color", javafx.scene.paint.Color.BLACK);
        shape.setFill(fill);
    }

    /**
     * Sets an SVG shape's fill to an explicit color.
     *
     * @param shape the shape to fill
     * @param hex   hex color string (e.g. {@code "#3b82f6"})
     */
    public static void fill(javafx.scene.shape.Shape shape, String hex) {
        Preconditions.requireNonNull(shape, "Styles.fill", "shape");
        Preconditions.requireNonBlank(hex,  "Styles.fill", "hex");
        shape.setFill(javafx.scene.paint.Color.web(hex));
    }

    /** Sets an SVG shape's stroke to an explicit color. */
    public static void stroke(javafx.scene.shape.Shape shape, String hex) {
        Preconditions.requireNonNull(shape, "Styles.stroke", "shape");
        Preconditions.requireNonBlank(hex,  "Styles.stroke", "hex");
        shape.setStroke(javafx.scene.paint.Color.web(hex));
        if (shape.getStrokeWidth() == 0) shape.setStrokeWidth(1);
    }

    /** Sets stroke width. Shorthand for {@code shape.setStrokeWidth(width)}. */
    public static void strokeWidth(javafx.scene.shape.Shape shape, double width) {
        Preconditions.requireNonNull(shape, "Styles.strokeWidth", "shape");
        shape.setStrokeWidth(width);
    }

    // =========================================================================
    // 3D TRANSFORMS (JavaFX Rotate/Translate on 3 axes)
    // =========================================================================

    /**
     * Rotates a node around the X axis (analogous to CSS {@code rotateX(deg)}).
     *
     * @param node    the node to rotate
     * @param degrees angle in degrees
     */
    public static void rotateX(Node node, double degrees) {
        Preconditions.requireNonNull(node, "Styles.rotateX", "node");
        javafx.scene.transform.Rotate r =
            new javafx.scene.transform.Rotate(degrees, javafx.scene.transform.Rotate.X_AXIS);
        node.getTransforms().removeIf(t -> t instanceof javafx.scene.transform.Rotate
            && ((javafx.scene.transform.Rotate)t).getAxis().equals(
                javafx.scene.transform.Rotate.X_AXIS));
        if (degrees != 0) node.getTransforms().add(r);
    }

    /**
     * Rotates a node around the Y axis (analogous to CSS {@code rotateY(deg)}).
     *
     * @param node    the node to rotate
     * @param degrees angle in degrees
     */
    public static void rotateY(Node node, double degrees) {
        Preconditions.requireNonNull(node, "Styles.rotateY", "node");
        javafx.scene.transform.Rotate r =
            new javafx.scene.transform.Rotate(degrees, javafx.scene.transform.Rotate.Y_AXIS);
        node.getTransforms().removeIf(t -> t instanceof javafx.scene.transform.Rotate
            && ((javafx.scene.transform.Rotate)t).getAxis().equals(
                javafx.scene.transform.Rotate.Y_AXIS));
        if (degrees != 0) node.getTransforms().add(r);
    }

    /**
     * Translates a node along the Z axis (analogous to CSS {@code translateZ(px)}).
     * In JavaFX this changes {@link Node#setTranslateZ(double)}.
     *
     * @param node the node to translate
     * @param px   Z offset in px (positive = closer to viewer)
     */
    public static void translateZ(Node node, double px) {
        Preconditions.requireNonNull(node, "Styles.translateZ", "node");
        node.setTranslateZ(px);
    }

    /**
     * Applies a perspective camera effect (depth perception).
     *
     * <p>Enables 3D depth on the node's parent scene by ensuring
     * the scene uses a {@link javafx.scene.PerspectiveCamera}.
     * The {@code focalLength} controls the perspective strength —
     * lower values create more dramatic perspective.
     *
     * @param node        any node in a Scene
     * @param focalLength camera focal length (default ≈ 1000)
     */
    public static void perspective(Node node, double focalLength) {
        Preconditions.requireNonNull(node, "Styles.perspective", "node");
        if (node.getScene() == null) return;
        javafx.scene.PerspectiveCamera cam = new javafx.scene.PerspectiveCamera(false);
        cam.setFieldOfView(Math.toDegrees(2 * Math.atan(node.getScene().getHeight() / 2 / focalLength)));
        node.getScene().setCamera(cam);
    }

    /** Resets all 3D transforms (rotateX, rotateY, translateZ). */
    public static void reset3D(Node node) {
        Preconditions.requireNonNull(node, "Styles.reset3D", "node");
        node.getTransforms().removeIf(t -> t instanceof javafx.scene.transform.Rotate &&
            !((javafx.scene.transform.Rotate)t).getAxis().equals(
                javafx.scene.transform.Rotate.Z_AXIS));
        node.setTranslateZ(0);
    }

    // =========================================================================
    // MOTION-REDUCE — respect prefers-reduced-motion
    // =========================================================================

    /** System property key for reduced motion preference. */
    private static final String REDUCED_MOTION_KEY = "tailwindfx.motion.reduced";

    /**
     * Sets the reduced-motion preference globally. When {@code true},
     * {@link #shouldAnimate()} returns {@code false}, allowing callers to
     * skip or shorten animations (analogous to CSS {@code prefers-reduced-motion}).
     *
     * <pre>
     * // Respect OS reduced-motion setting:
     * Styles.setReducedMotion(Toolkit.getDefaultToolkit()
     *     .getDesktopProperty("gnome.reduceAnimations") != null);
     *
     * // Or force it in tests:
     * Styles.setReducedMotion(true);
     * if (Styles.shouldAnimate()) FxAnimation.fadeIn(node).play();
     * </pre>
     *
     * @param reduced {@code true} to suppress animations
     */
    public static void setReducedMotion(boolean reduced) {
        System.setProperty(REDUCED_MOTION_KEY, String.valueOf(reduced));
    }

    /**
     * Returns {@code true} if animations should run (motion is NOT reduced).
     * Check this before starting non-essential animations.
     *
     * @return {@code false} when reduced-motion is enabled
     */
    public static boolean shouldAnimate() {
        return !"true".equalsIgnoreCase(System.getProperty(REDUCED_MOTION_KEY, "false"));
    }

    /**
     * Plays an animation only if motion is not reduced.
     * If reduced, the final state is applied instantly.
     *
     * <pre>
     * Styles.playIfMotionOk(FxAnimation.fadeIn(node, 300));
     * </pre>
     *
     * @param animation the animation to conditionally play
     */
    public static void playIfMotionOk(FxAnimation animation) {
        Preconditions.requireNonNull(animation, "Styles.playIfMotionOk", "animation");
        if (shouldAnimate()) {
            animation.play();
        } else {
            // Jump to end state: play at very high speed for 1 frame then stop
            animation.speed(100.0).play();
        }
    }

    // =========================================================================
    // GLASSMORPHISM / NEUMORPHISM PRESETS
    // =========================================================================

    /**
     * Applies a glassmorphism effect to a node.
     *
     * <p>Sets semi-transparent background, border, and blur effect.
     * For best results, place the node over a colorful background.
     *
     * <pre>
     * Styles.glass(panel);
     * TailwindFX.backdropBlur(panel, 12); // blur the content behind
     * </pre>
     *
     * @param node the node to style as glass
     */
    public static void glass(Region node) {
        Preconditions.requireNonNull(node, "Styles.glass", "node");
        node.setStyle(node.getStyle()
            + " -fx-background-color: rgba(255,255,255,0.15);"
            + " -fx-background-radius: 12;"
            + " -fx-border-color: rgba(255,255,255,0.30);"
            + " -fx-border-radius: 12;"
            + " -fx-border-width: 1;");
        node.setEffect(new javafx.scene.effect.BoxBlur(12, 12, 3));
    }

    /** Dark-tinted glass variant (for dark backgrounds). */
    public static void glassDark(Region node) {
        Preconditions.requireNonNull(node, "Styles.glassDark", "node");
        node.setStyle(node.getStyle()
            + " -fx-background-color: rgba(0,0,0,0.20);"
            + " -fx-background-radius: 12;"
            + " -fx-border-color: rgba(255,255,255,0.10);"
            + " -fx-border-radius: 12;"
            + " -fx-border-width: 1;");
        node.setEffect(new javafx.scene.effect.BoxBlur(12, 12, 3));
    }

    /**
     * Applies a neumorphism (soft-UI) effect to a node.
     *
     * <p>Creates the characteristic dual-shadow look. Background color
     * of the node should be {@code #e0e5ec} for best results.
     *
     * @param node the Region to style
     */
    public static void neumorph(Region node) {
        Preconditions.requireNonNull(node, "Styles.neumorph", "node");
        node.setStyle(node.getStyle()
            + " -fx-background-color: #e0e5ec;"
            + " -fx-background-radius: 12;");
        node.setEffect(new javafx.scene.effect.DropShadow(
            javafx.scene.effect.BlurType.GAUSSIAN,
            javafx.scene.paint.Color.web("#a3b1c6"), 10, 0, -4, -4));
    }

    /** Inset neumorphism (pressed state). */
    public static void neumorphInset(Region node) {
        Preconditions.requireNonNull(node, "Styles.neumorphInset", "node");
        node.setStyle(node.getStyle()
            + " -fx-background-color: #e0e5ec;"
            + " -fx-background-radius: 12;");
        node.setEffect(new javafx.scene.effect.InnerShadow(
            javafx.scene.effect.BlurType.GAUSSIAN,
            javafx.scene.paint.Color.web("#a3b1c6"), 10, 0, 4, 4));
    }

}