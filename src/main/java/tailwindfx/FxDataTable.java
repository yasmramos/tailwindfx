package tailwindfx;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * FxDataTable — a styled, sortable, filterable data table for TailwindFX.
 *
 * <p>Wraps JavaFX's {@link TableView} with a declarative builder API,
 * built-in sorting, search filtering, pagination, and TailwindFX utility classes.
 *
 * <h3>Usage</h3>
 * <pre>
 * // Basic table with column definitions:
 * record User(String name, String email, int age) {}
 *
 * FxDataTable&lt;User&gt; table = FxDataTable.of(User.class)
 *     .column("Name",  User::name)
 *     .column("Email", User::email)
 *     .column("Age",   u -> String.valueOf(u.age()))
 *     .searchable(true)
 *     .pageSize(20)
 *     .style("table-striped", "table-hover")
 *     .build();
 *
 * table.setItems(userList);
 *
 * // Access the underlying TableView:
 * TableView&lt;User&gt; tv = table.tableView();
 *
 * // Get the full VBox (search bar + table + pagination):
 * VBox container = table.container();
 * </pre>
 *
 * <h3>Styles applied automatically</h3>
 * <ul>
 *   <li>{@code .table-view} — base table style (from tailwindfx.css)</li>
 *   <li>{@code .table-striped} — alternating row colors</li>
 *   <li>{@code .table-hover}   — hover highlight on rows</li>
 *   <li>{@code .table-compact} — reduced row height</li>
 *   <li>{@code .table-bordered}— cell borders</li>
 * </ul>
 *
 * @param <T> the data item type
 */
public final class FxDataTable<T> {

    // =========================================================================
    // Builder
    // =========================================================================

    /** Creates a new builder for type {@code T}. */
    public static <T> Builder<T> of(Class<T> type) {
        return new Builder<>();
    }

    public static final class Builder<T> {

        private final List<ColDef<T>> cols = new ArrayList<>();
        private boolean searchable  = false;
        private boolean paginated   = false;
        private int     pageSize    = 25;
        private String  placeholder = "No data";
        private String[] styleClasses = new String[0];
        private Callback<TableView<T>, TableRow<T>> rowFactory = null;

        private Builder() {}

        /**
         * Adds a column with a string value extractor.
         *
         * @param header column header text
         * @param value  function extracting the display string from T
         */
        public Builder<T> column(String header, Function<T, String> value) {
            cols.add(new ColDef<>(header, value, true));
            return this;
        }

        /**
         * Adds a column with a string extractor and configures sortability.
         *
         * @param header   column header text
         * @param value    string extractor
         * @param sortable whether this column is sortable
         */
        public Builder<T> column(String header, Function<T, String> value, boolean sortable) {
            cols.add(new ColDef<>(header, value, sortable));
            return this;
        }

        /** Enables or disables the search bar above the table. */
        public Builder<T> searchable(boolean on) { this.searchable = on; return this; }

        /** Enables pagination with the given page size. */
        public Builder<T> pageSize(int size) {
            if (size < 1) throw new IllegalArgumentException(
                "FxDataTable.pageSize: must be >= 1, got: " + size);
            this.paginated = true;
            this.pageSize  = size;
            return this;
        }

        /** Sets the placeholder text when the table is empty. */
        public Builder<T> placeholder(String text) { this.placeholder = text; return this; }

        /** Applies additional TailwindFX CSS classes to the TableView. */
        public Builder<T> style(String... classes) { this.styleClasses = classes; return this; }

        /**
         * Sets a custom row factory (e.g. for row-level styling or context menus).
         *
         * @param factory row factory callback
         */
        public Builder<T> rowFactory(Callback<TableView<T>, TableRow<T>> factory) {
            this.rowFactory = factory;
            return this;
        }

        /** Builds the {@link FxDataTable}. */
        public FxDataTable<T> build() {
            return new FxDataTable<>(this);
        }
    }

    // =========================================================================
    // Column definition
    // =========================================================================

    private record ColDef<T>(String header, Function<T, String> value, boolean sortable) {}

    // =========================================================================
    // State
    // =========================================================================

    private final ObservableList<T>  source    = FXCollections.observableArrayList();
    private final FilteredList<T>    filtered;
    private final SortedList<T>      sorted;
    private final TableView<T>       table;
    private final VBox               container;

    // Pagination state
    private final boolean paginated;
    private final int     pageSize;
    private int           currentPage = 0;

    // Search
    private final TextField searchField;

    // =========================================================================
    // Construction
    // =========================================================================

    private FxDataTable(Builder<T> b) {
        this.paginated   = b.paginated;
        this.pageSize    = b.pageSize;
        this.filtered    = new FilteredList<>(source, p -> true);
        this.sorted      = new SortedList<>(filtered);
        this.table       = new TableView<>();
        this.searchField = b.searchable ? new TextField() : null;

        buildColumns(b.cols);
        configureTable(b);
        this.container = buildContainer(b);
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Sets the data items displayed in the table.
     * Replaces all existing items.
     *
     * @param items the new data list
     */
    public void setItems(List<T> items) {
        source.setAll(items);
        if (paginated) { currentPage = 0; applyPage(); }
    }

    /**
     * Adds items to the existing dataset.
     *
     * @param items items to add
     */
    public void addItems(List<T> items) {
        source.addAll(items);
        if (paginated) applyPage();
    }

    /** Returns the currently selected item, or {@code null} if none. */
    public T selectedItem() {
        return table.getSelectionModel().getSelectedItem();
    }

    /** Clears the current selection. */
    public void clearSelection() {
        table.getSelectionModel().clearSelection();
    }

    /**
     * Sets a programmatic filter. Combines with the search bar filter if present.
     *
     * @param predicate the filter condition (null = show all)
     */
    public void setFilter(Predicate<T> predicate) {
        filtered.setPredicate(predicate != null ? predicate : p -> true);
        if (paginated) { currentPage = 0; applyPage(); }
    }

    /** Clears any programmatic filter. */
    public void clearFilter() { setFilter(null); }

    /** Returns the total number of items after filtering. */
    public int filteredSize() { return filtered.size(); }

    /** Returns the total number of source items. */
    public int totalSize() { return source.size(); }

    /**
     * Navigates to a specific page (0-indexed).
     *
     * @param page page index
     */
    public void goToPage(int page) {
        if (!paginated) return;
        int maxPage = Math.max(0, (filtered.size() - 1) / pageSize);
        currentPage = Math.max(0, Math.min(page, maxPage));
        applyPage();
    }

    /** Navigates to the next page. */
    public void nextPage() { goToPage(currentPage + 1); }

    /** Navigates to the previous page. */
    public void prevPage() { goToPage(currentPage - 1); }

    /** Returns the underlying {@link TableView}. */
    public TableView<T> tableView() { return table; }

    /**
     * Returns the full container (search bar + table + pagination controls).
     * Add this to your scene.
     */
    public VBox container() { return container; }

    /**
     * Returns the current page index (0-based).
     * Always returns 0 if pagination is disabled.
     */
    public int currentPage() { return currentPage; }

    /**
     * Returns the total number of pages.
     * Returns 1 if pagination is disabled.
     */
    public int pageCount() {
        if (!paginated || filtered.isEmpty()) return 1;
        return (int) Math.ceil((double) filtered.size() / pageSize);
    }

    // =========================================================================
    // Internal construction
    // =========================================================================

    private void buildColumns(List<ColDef<T>> defs) {
        for (ColDef<T> def : defs) {
            TableColumn<T, String> col = new TableColumn<>(def.header());
            col.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(def.value().apply(data.getValue())));
            col.setSortable(def.sortable());
            if (def.sortable()) {
                col.setComparator(Comparator.naturalOrder());
            }
            table.getColumns().add(col);
        }
        sorted.comparatorProperty().bind(table.comparatorProperty());
    }

    private void configureTable(Builder<T> b) {
        table.setItems(paginated ? FXCollections.observableArrayList() : sorted);
        table.setPlaceholder(new Label(b.placeholder));
        table.getStyleClass().addAll("table-view");
        if (b.styleClasses.length > 0)
            table.getStyleClass().addAll(b.styleClasses);
        if (b.rowFactory != null)
            table.setRowFactory(b.rowFactory);
        else
            table.setRowFactory(tv -> buildStyledRow());
        VBox.setVgrow(table, Priority.ALWAYS);

        if (paginated) {
            filtered.addListener((javafx.collections.ListChangeListener<T>) c -> {
                currentPage = 0; applyPage();
            });
            applyPage();
        }
    }

    private TableRow<T> buildStyledRow() {
        return new TableRow<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeIf(s -> s.startsWith("row-"));
                if (!empty && item != null) {
                    if (getIndex() % 2 == 0)
                        getStyleClass().add("row-even");
                    else
                        getStyleClass().add("row-odd");
                }
            }
        };
    }

    private VBox buildContainer(Builder<T> b) {
        List<javafx.scene.Node> nodes = new ArrayList<>();

        if (b.searchable && searchField != null) {
            searchField.setPromptText("Search…");
            searchField.getStyleClass().add("search-field");
            HBox.setHgrow(searchField, Priority.ALWAYS);
            HBox searchBar = new HBox(searchField);
            searchBar.getStyleClass().add("search-bar");
            searchBar.setStyle("-fx-padding: 0 0 8 0;");
            nodes.add(searchBar);

            // Wire search to filter
            searchField.textProperty().addListener((obs, ov, nv) -> {
                String lower = nv == null ? "" : nv.toLowerCase();
                filtered.setPredicate(item -> {
                    if (lower.isBlank()) return true;
                    // Search across all column values
                    return b.cols.stream()
                        .map(col -> col.value().apply(item))
                        .anyMatch(val -> val != null && val.toLowerCase().contains(lower));
                });
                if (paginated) { currentPage = 0; applyPage(); }
            });
        }

        nodes.add(table);

        if (b.paginated) {
            nodes.add(buildPaginationBar());
        }

        VBox box = new VBox(nodes.toArray(new javafx.scene.Node[0]));
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private HBox buildPaginationBar() {
        Label pageLabel = new Label();
        Button prev = new Button("‹ Prev");
        Button next = new Button("Next ›");

        prev.getStyleClass().addAll("btn", "btn-secondary");
        next.getStyleClass().addAll("btn", "btn-secondary");

        prev.setOnAction(e -> { prevPage(); updatePaginationLabel(pageLabel, prev, next); });
        next.setOnAction(e -> { nextPage(); updatePaginationLabel(pageLabel, prev, next); });

        updatePaginationLabel(pageLabel, prev, next);
        filtered.addListener((javafx.collections.ListChangeListener<T>)
            c -> updatePaginationLabel(pageLabel, prev, next));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(8, prev, pageLabel, spacer, next);
        bar.setStyle("-fx-padding: 8 0 0 0; -fx-alignment: center-left;");
        return bar;
    }

    private void updatePaginationLabel(Label label, Button prev, Button next) {
        int total = pageCount();
        label.setText("Page " + (currentPage + 1) + " of " + total
            + " (" + filteredSize() + " items)");
        prev.setDisable(currentPage == 0);
        next.setDisable(currentPage >= total - 1);
    }

    private void applyPage() {
        if (!paginated) return;
        int from = currentPage * pageSize;
        int to   = Math.min(from + pageSize, filtered.size());
        ObservableList<T> page = FXCollections.observableArrayList(
            from < filtered.size() ? sorted.subList(from, to) : List.of());
        table.setItems(page);
    }
}
