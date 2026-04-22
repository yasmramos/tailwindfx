package tailwindfx;

import java.lang.reflect.InvocationTargetException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.animation.Interpolator;

/**
 * FxVirtualFlow — Contenedor virtualizado de alto rendimiento con selección,
 * drag & drop y scroll animado configurable.
 *
 * @param <T> Tipo de dato de los items
 */
public class FxVirtualFlow<T> extends Region {

    // =========================================================================
    // Enums & Constants
    // =========================================================================
    public enum SelectionMode { NONE, SINGLE, MULTIPLE }

    // =========================================================================
    // State & Cache
    // =========================================================================
    private final ObservableList<T> items = FXCollections.observableArrayList();
    private Function<T, Node> cellFactory = item -> {
        var label = new javafx.scene.control.Label(String.valueOf(item));
        label.setStyle("-fx-padding: 8;");
        return label;
    };

    private Orientation orientation = Orientation.VERTICAL;
    private final DoubleProperty cellHeight = new SimpleDoubleProperty(48);
    private final DoubleProperty cellWidth = new SimpleDoubleProperty(200);
    private Function<T, Double> cellSizeProvider = null;

    private final ObjectProperty<Insets> viewportPadding = new SimpleObjectProperty<>(Insets.EMPTY);
    private final ObjectProperty<Interpolator> scrollInterpolator = new SimpleObjectProperty<>(Interpolator.EASE_BOTH);
    private final ObjectProperty<SelectionMode> selectionMode = new SimpleObjectProperty<>(SelectionMode.SINGLE);
    private final ObservableList<Integer> selectedIndices = FXCollections.observableArrayList();
    
    // Contenedor interno
    private final Pane cellContainer = new Pane();
    private final ScrollBar scrollBar = new ScrollBar();
    private final Map<Integer, Node> visibleCells = new HashMap<>();
    
    // Drag & Drop
    private final Region dropIndicator = new Region();
    private int dragSourceIndex = -1;
    private int dragTargetIndex = -1;
    
    // Cache de tamaños
    private double[] prefixSums = new double[0];
    private int prefixSumsLength = 0;
    private boolean sizeCacheDirty = true;
    
    // Animación & Posición
    private Timeline scrollAnimation;
    private double scrollPosition = 0;
    private int firstVisibleIndex = 0;
    private int lastVisibleIndex = 0;

    // Callbacks
    private Consumer<Integer> onSelect;
    private Consumer<Integer> onDoubleClick;
    private BiConsumer<Integer, Integer> onItemReorder;
    private Consumer<List<T>> onSelectionChange;

    // =========================================================================
    // Constructor
    // =========================================================================
    public FxVirtualFlow() {
        // Setup container
        dropIndicator.getStyleClass().add("fx-virtualflow-drop-indicator");
        dropIndicator.setStyle("-fx-background-color: #3b82f6; -fx-pref-height: 2;");
        dropIndicator.setVisible(false);
        dropIndicator.setManaged(false);
        dropIndicator.setPickOnBounds(true);
        
        getChildren().addAll(cellContainer, dropIndicator, scrollBar);
        cellContainer.setMouseTransparent(false); // We handle events here
        cellContainer.setManaged(false);
        
        // Scroll config
        scrollBar.setOrientation(orientation);
        scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
            scrollPosition = newVal.doubleValue();
            updateVisibleCells();
        });

        // Data changes
        items.addListener((ListChangeListener<T>) c -> {
            sizeCacheDirty = true;
            updateScrollBar();
            updateVisibleCells();
        });

        // Size changes
        widthProperty().addListener((obs, o, n) -> updateVisibleCells());
        heightProperty().addListener((obs, o, n) -> updateVisibleCells());
        viewportPadding.addListener((obs, o, n) -> { requestLayout(); updateVisibleCells(); });
        
        // =========================================================================
        // EVENT DELEGATION (Container-level)
        // =========================================================================
        cellContainer.setOnMousePressed(e -> handleSelection(e));
        cellContainer.setOnMouseClicked(e -> { if (e.getClickCount() == 2) handleDoubleClick(e); });
        
        // Drag & Drop setup
        cellContainer.setOnDragDetected(e -> handleDragStart(e));
        cellContainer.setOnDragOver(e -> handleDragOver(e));
        cellContainer.setOnDragExited(e -> dropIndicator.setVisible(false));
        cellContainer.setOnDragDropped(e -> handleDragDrop(e));
        cellContainer.setOnDragDone(e -> { dropIndicator.setVisible(false); dragSourceIndex = -1; });
        
        updateScrollBar();
    }

    // =========================================================================
    // Public API - Core
    // =========================================================================
    public void setItems(ObservableList<T> items) {
        Objects.requireNonNull(items, "items cannot be null");
        this.items.setAll(items);
    }

    public ObservableList<T> getItems() { return items; }

    public void setCellFactory(Function<T, Node> factory) {
        Objects.requireNonNull(factory, "cellFactory cannot be null");
        this.cellFactory = factory;
        visibleCells.values().forEach(cellContainer.getChildren()::remove);
        visibleCells.clear();
        updateVisibleCells();
    }

    public void setCellSizeProvider(Function<T, Double> provider) {
        this.cellSizeProvider = provider;
        sizeCacheDirty = true;
        updateScrollBar();
        updateVisibleCells();
    }

    public void setCellHeight(double height) {
        if (height <= 0) throw new IllegalArgumentException("cellHeight must be positive");
        cellHeight.set(height);
        sizeCacheDirty = true;
        updateScrollBar(); updateVisibleCells();
    }
    public double getCellHeight() { return cellHeight.get(); }
    public DoubleProperty cellHeightProperty() { return cellHeight; }

    public void setCellWidth(double width) {
        if (width <= 0) throw new IllegalArgumentException("cellWidth must be positive");
        cellWidth.set(width);
        sizeCacheDirty = true;
        updateScrollBar(); updateVisibleCells();
    }
    public double getCellWidth() { return cellWidth.get(); }
    public DoubleProperty cellWidthProperty() { return cellWidth; }

    public void setOrientation(Orientation orientation) {
        Objects.requireNonNull(orientation, "orientation cannot be null");
        this.orientation = orientation;
        scrollBar.setOrientation(orientation);
        sizeCacheDirty = true;
        updateScrollBar(); updateVisibleCells();
    }
    public Orientation getOrientation() { return orientation; }

    public void setViewportPadding(Insets padding) {
        Objects.requireNonNull(padding, "viewportPadding cannot be null");
        this.viewportPadding.set(padding);
    }
    public Insets getViewportPadding() { return viewportPadding.get(); }
    public ObjectProperty<Insets> viewportPaddingProperty() { return viewportPadding; }

    // =========================================================================
    // Public API - Selection
    // =========================================================================
    public void setSelectionMode(SelectionMode mode) {
        selectionMode.set(mode);
        if (mode == SelectionMode.NONE) selectedIndices.clear();
    }
    public SelectionMode getSelectionMode() { return selectionMode.get(); }
    public ObjectProperty<SelectionMode> selectionModeProperty() { return selectionMode; }

    public ObservableList<Integer> getSelectedIndices() { return selectedIndices; }
    
    public List<T> getSelectedItems() {
        return selectedIndices.stream()
                .filter(i -> i >= 0 && i < items.size())
                .map(items::get)
                .collect(Collectors.toList());
    }

    public void clearSelection() {
        selectedIndices.clear();
        if (onSelectionChange != null) onSelectionChange.accept(getSelectedItems());
    }

    public void selectIndex(int index) {
        if (index < 0 || index >= items.size()) return;
        selectedIndices.setAll(index);
        if (onSelectionChange != null) onSelectionChange.accept(getSelectedItems());
    }

    // =========================================================================
    // Public API - Scrolling & Animation
    // =========================================================================
    public void scrollToIndex(int index) { scrollToIndex(index, Duration.ZERO); }
    
    public void scrollToIndex(int index, Duration duration) {
        if (items.isEmpty() || index < 0 || index >= items.size()) return;
        ensureSizeCache();
        animateScrollTo(prefixSums[index], duration);
    }

    public void scrollBy(double delta, Duration duration) {
        animateScrollTo(Math.max(0, Math.min(scrollPosition + delta, scrollBar.getMax())), duration);
    }

    public Interpolator getScrollInterpolator() { return scrollInterpolator.get(); }
    public ObjectProperty<Interpolator> scrollInterpolatorProperty() { return scrollInterpolator; }

    public int getFirstVisibleIndex() { return firstVisibleIndex; }
    public int getLastVisibleIndex() { return lastVisibleIndex; }
    public double getScrollPosition() { return scrollPosition; }

    // =========================================================================
    // Public API - Callbacks
    // =========================================================================
    public void setOnSelect(Consumer<Integer> handler) { this.onSelect = handler; }
    public void setOnDoubleClick(Consumer<Integer> handler) { this.onDoubleClick = handler; }
    public void setOnItemReorder(BiConsumer<Integer, Integer> handler) { this.onItemReorder = handler; }
    public void setOnSelectionChange(Consumer<List<T>> handler) { this.onSelectionChange = handler; }

    // =========================================================================
    // Layout
    // =========================================================================
    @Override
    protected void layoutChildren() {
        double w = getWidth(), h = getHeight();
        if (orientation == Orientation.VERTICAL) {
            double sbW = scrollBar.prefWidth(-1);
            double vw = Math.max(0, w - sbW);
            cellContainer.resizeRelocate(0, 0, vw, h);
            scrollBar.resizeRelocate(vw, 0, sbW, h);
        } else {
            double sbH = scrollBar.prefHeight(-1);
            double vh = Math.max(0, h - sbH);
            cellContainer.resizeRelocate(0, 0, w, vh);
            scrollBar.resizeRelocate(0, vh, w, sbH);
        }
        updateVisibleCells();
    }

    @Override protected double computePrefWidth(double h) { return orientation == Orientation.VERTICAL ? 400 : 800; }
    @Override protected double computePrefHeight(double w) { return orientation == Orientation.VERTICAL ? 600 : 200; }
    @Override protected double computeMinWidth(double h) { return orientation == Orientation.VERTICAL ? 100 : 200; }
    @Override protected double computeMinHeight(double w) { return orientation == Orientation.VERTICAL ? 100 : 50; }

    // =========================================================================
    // Internal Logic - Size Cache & Binary Search
    // =========================================================================
    private void ensureSizeCache() {
        if (!sizeCacheDirty) return;
        sizeCacheDirty = false;
        int n = items.size();
        if (prefixSums.length != n + 1) prefixSums = new double[n + 1];
        prefixSums[0] = 0;
        double def = orientation == Orientation.VERTICAL ? cellHeight.get() : cellWidth.get();
        for (int i = 0; i < n; i++) {
            double size = (cellSizeProvider != null) ? cellSizeProvider.apply(items.get(i)) : def;
            prefixSums[i + 1] = prefixSums[i] + Math.max(1, size);
        }
        prefixSumsLength = n + 1;
    }

    private double getTotalContentSize() {
        ensureSizeCache();
        return prefixSumsLength > 0 ? prefixSums[prefixSumsLength - 1] : 0;
    }

    private int findCellIndexForPosition(double absolutePos) {
        ensureSizeCache();
        if (prefixSumsLength <= 1) return 0;
        if (absolutePos <= 0) return 0;
        if (absolutePos >= prefixSums[prefixSumsLength - 1]) return items.size() - 1;

        int low = 0, high = prefixSumsLength - 2;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (absolutePos >= prefixSums[mid] && absolutePos < prefixSums[mid + 1]) return mid;
            if (absolutePos < prefixSums[mid]) high = mid - 1;
            else low = mid + 1;
        }
        return Math.max(0, Math.min(low, items.size() - 1));
    }

    // =========================================================================
    // Internal Logic - Scroll & Cells
    // =========================================================================
    private void updateScrollBar() {
        if (items.isEmpty()) {
            scrollBar.setMin(0); scrollBar.setMax(0); scrollBar.setValue(0);
            scrollBar.setVisibleAmount(1); scrollBar.setDisable(true); return;
        }
        double total = getTotalContentSize();
        double vpSize = orientation == Orientation.VERTICAL 
            ? Math.max(0, cellContainer.getHeight()) 
            : Math.max(0, cellContainer.getWidth());
        double max = Math.max(0, total - vpSize);
        scrollBar.setMin(0); scrollBar.setMax(max);
        scrollBar.setVisibleAmount(Math.min(vpSize / total, 1.0));
        scrollBar.setBlockIncrement(vpSize * 0.9);
        scrollBar.setUnitIncrement(orientation == Orientation.VERTICAL ? cellHeight.get() : cellWidth.get());
        scrollBar.setDisable(max <= 0);
        scrollPosition = Math.min(scrollPosition, max);
        scrollBar.setValue(scrollPosition);
    }

    private void animateScrollTo(double target, Duration duration) {
        if (scrollAnimation != null) scrollAnimation.stop();
        
        // Fix 1: Use toMillis() instead of isZero() for compatibility
        if (duration.toMillis() == 0 || duration.toMillis() < 16) {
            scrollBar.setValue(target); 
            return;
        }

        // Fix 2 & 3: Correct Timeline construction and Interpolator usage
        // Timeline takes KeyFrames. Interpolator is applied to KeyValue.
        scrollAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(scrollBar.valueProperty(), scrollPosition, Interpolator.LINEAR)),
            new KeyFrame(duration, new KeyValue(scrollBar.valueProperty(), target, scrollInterpolator.get()))
        );
        
        scrollAnimation.play();
    }

    private void updateVisibleCells() {
        if (items.isEmpty()) {
            cellContainer.getChildren().clear();
            visibleCells.clear();
            firstVisibleIndex = 0; lastVisibleIndex = -1; return;
        }
        ensureSizeCache();
        Insets pad = viewportPadding.get();
        double vpSize = orientation == Orientation.VERTICAL
            ? Math.max(0, cellContainer.getHeight() - pad.getTop() - pad.getBottom())
            : Math.max(0, cellContainer.getWidth() - pad.getLeft() - pad.getRight());
        if (vpSize <= 0) return;

        firstVisibleIndex = findCellIndexForPosition(scrollPosition);
        lastVisibleIndex = firstVisibleIndex;
        double cur = prefixSums[firstVisibleIndex];
        int buffer = 3;
        while (lastVisibleIndex < items.size() - 1 && (cur < scrollPosition + vpSize || buffer > 0)) {
            lastVisibleIndex++; cur = prefixSums[lastVisibleIndex]; buffer--;
        }
        while (firstVisibleIndex > 0 && buffer > 0) { firstVisibleIndex--; buffer--; }

        visibleCells.keySet().removeIf(idx -> {
            if (idx < firstVisibleIndex || idx > lastVisibleIndex) {
                Node n = visibleCells.get(idx);
                if (n != null) cellContainer.getChildren().remove(n);
                return true;
            }
            return false;
        });

        for (int i = firstVisibleIndex; i <= lastVisibleIndex; i++) {
            if (!visibleCells.containsKey(i)) {
                T item = items.get(i);
                Node cell = cellFactory.apply(item);
                if (cell == null) continue;
                // Attach visual selection state if needed
                cell.getStyleClass().add("fx-virtualflow-cell");
                visibleCells.put(i, cell);
                cellContainer.getChildren().add(cell);
            }
            Node cell = visibleCells.get(i);
            if (cell == null) continue;

            double start = prefixSums[i] - scrollPosition;
            double size = prefixSums[i + 1] - prefixSums[i];
            boolean isSelected = selectedIndices.contains(i);
            // Update visual state
            if (isSelected) cell.getStyleClass().add("selected");
            else cell.getStyleClass().remove("selected");

            if (orientation == Orientation.VERTICAL) {
                double aw = Math.max(0, cellContainer.getWidth() - pad.getLeft() - pad.getRight());
                cell.resizeRelocate(pad.getLeft(), start + pad.getTop(), aw, size);
            } else {
                double ah = Math.max(0, cellContainer.getHeight() - pad.getTop() - pad.getBottom());
                cell.resizeRelocate(start + pad.getLeft(), pad.getTop(), size, ah);
            }
        }
    }

    // =========================================================================
    // Internal Logic - Event Delegation
    // =========================================================================
    private int getIndexAtMouseEvent(MouseEvent e) {
        double local = orientation == Orientation.VERTICAL ? e.getY() : e.getX();
        return findCellIndexForPosition(local + scrollPosition);
    }
    
    // Helper for DragEvents which don't extend MouseEvent
    private int getIndexAtDragEvent(DragEvent e) {
        double local = orientation == Orientation.VERTICAL ? e.getY() : e.getX();
        return findCellIndexForPosition(local + scrollPosition);
    }

    private void handleSelection(MouseEvent e) {
        if (selectionMode.get() == SelectionMode.NONE) return;
        int idx = getIndexAtMouseEvent(e);
        if (idx < 0 || idx >= items.size()) { clearSelection(); return; }

        if (selectionMode.get() == SelectionMode.SINGLE) {
            selectIndex(idx);
        } else if (selectionMode.get() == SelectionMode.MULTIPLE) {
            if (e.isShiftDown() && !selectedIndices.isEmpty()) {
                int last = selectedIndices.get(selectedIndices.size() - 1);
                int from = Math.min(last, idx), to = Math.max(last, idx);
                selectedIndices.clear();
                for (int i = from; i <= to; i++) selectedIndices.add(i);
            } else if (e.isShortcutDown()) {
                if (selectedIndices.contains(idx)) selectedIndices.remove(Integer.valueOf(idx));
                else selectedIndices.add(idx);
            } else {
                selectIndex(idx);
            }
        }
        if (onSelect != null) onSelect.accept(idx);
        if (onSelectionChange != null) onSelectionChange.accept(getSelectedItems());
    }

    private void handleDoubleClick(MouseEvent e) {
        int idx = getIndexAtMouseEvent(e);
        if (idx >= 0 && idx < items.size() && onDoubleClick != null) onDoubleClick.accept(idx);
    }
   
    // Corrected Signatures:
    
    private void handleDragStart(MouseEvent e) {
        int idx = getIndexAtMouseEvent(e);
        if (idx < 0 || idx >= items.size()) return;
        dragSourceIndex = idx;
        
        Dragboard db = cellContainer.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString(String.valueOf(idx)); 
        db.setContent(content);
        db.setDragView(cellContainer.snapshot(null, null));
    }

    private void handleDragOver(DragEvent e) {
        if (dragSourceIndex < 0 || !e.getDragboard().hasString()) return;
        e.acceptTransferModes(TransferMode.MOVE);
        
        int idx = getIndexAtDragEvent(e);
        if (idx == dragSourceIndex) { dropIndicator.setVisible(false); return; }
        dragTargetIndex = idx;
        
        double absPos = prefixSums[idx] - scrollPosition;
        if (orientation == Orientation.VERTICAL) {
            dropIndicator.resizeRelocate(0, absPos, cellContainer.getWidth(), 2);
        } else {
            dropIndicator.resizeRelocate(absPos, 0, 2, cellContainer.getHeight());
        }
        dropIndicator.setVisible(true);
    }

    private void handleDragDrop(DragEvent e) {
        e.setDropCompleted(true);
        dropIndicator.setVisible(false);
        if (dragSourceIndex < 0 || dragTargetIndex < 0) return;
        
        int from = dragSourceIndex;
        int to = (dragTargetIndex < from) ? dragTargetIndex : dragTargetIndex - 1;
        if (onItemReorder != null) onItemReorder.accept(from, to);
        dragSourceIndex = -1;
    }

    // =========================================================================
    // Fluent API
    // =========================================================================
    public FxVirtualFlow<T> items(ObservableList<T> i) { setItems(i); return this; }
    public FxVirtualFlow<T> cellFactory(Function<T, Node> f) { setCellFactory(f); return this; }
    public FxVirtualFlow<T> cellHeight(double h) { setCellHeight(h); return this; }
    public FxVirtualFlow<T> cellWidth(double w) { setCellWidth(w); return this; }
    public FxVirtualFlow<T> cellSizeProvider(Function<T, Double> p) { setCellSizeProvider(p); return this; }
    public FxVirtualFlow<T> orientation(Orientation o) { setOrientation(o); return this; }
    public FxVirtualFlow<T> viewportPadding(Insets p) { setViewportPadding(p); return this; }
    public FxVirtualFlow<T> viewportPadding(double px) { return viewportPadding(new Insets(px)); }
    public FxVirtualFlow<T> selectionMode(SelectionMode m) { setSelectionMode(m); return this; }
    public FxVirtualFlow<T> scrollInterpolator(Interpolator i) { scrollInterpolator.set(i); return this; }
    public FxVirtualFlow<T> outerPadding(Insets p) { setPadding(p); return this; }
    public FxVirtualFlow<T> outerPadding(double px) { return outerPadding(new Insets(px)); }

    public FxVirtualFlow<T> onSelect(Consumer<Integer> h) { setOnSelect(h); return this; }
    public FxVirtualFlow<T> onDoubleClick(Consumer<Integer> h) { setOnDoubleClick(h); return this; }
    public FxVirtualFlow<T> onItemReorder(BiConsumer<Integer, Integer> h) { setOnItemReorder(h); return this; }
    public FxVirtualFlow<T> onSelectionChange(Consumer<List<T>> h) { setOnSelectionChange(h); return this; }

    /**
     * Wrapper TailwindFX seguro (fallback a CSS si no está en classpath)
     */
    public FxVirtualFlow<T> withTailwindStyling(Function<T, Node> base, String... classes) {
        Objects.requireNonNull(base);
        return cellFactory(item -> {
            Node n = base.apply(item);
            if (n != null && classes.length > 0) {
                try {
                    Class.forName("tailwindfx.TailwindFX")
                        .getMethod("apply", Node.class, String[].class)
                        .invoke(null, n, (Object) classes);
                } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                    n.getStyleClass().addAll(classes);
                }
            }
            return n;
        });
    }

    public void dispose() {
        if (scrollAnimation != null) scrollAnimation.stop();
        visibleCells.values().forEach(cellContainer.getChildren()::remove);
        visibleCells.clear();
        items.clear();
        selectedIndices.clear();
        prefixSums = new double[0]; prefixSumsLength = 0;
    }
}