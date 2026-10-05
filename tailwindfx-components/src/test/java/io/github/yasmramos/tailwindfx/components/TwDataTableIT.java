package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/** Unit tests for {@link TwDataTable} — builder, sorting, filtering, pagination, search. */
@DisplayName("TwDataTable Component Tests")
public class TwDataTableIT extends ApplicationTest {

  record Person(String name, String email, int age) {}

  @BeforeAll
  static void setupSpec() {
    Platform.setImplicitExit(false);
  }

  private static TwDataTable<Person> buildBasic() {
    return TwDataTable.of(Person.class)
        .column("Name", Person::name)
        .column("Email", Person::email)
        .column("Age", p -> String.valueOf(p.age()))
        .build();
  }

  private static List<Person> sampleData() {
    return List.of(
        new Person("Alice", "alice@example.com", 30),
        new Person("Bob", "bob@example.com", 25),
        new Person("Charlie", "charlie@example.com", 35),
        new Person("Diana", "diana@example.com", 28),
        new Person("Eve", "eve@example.com", 22));
  }

  @Nested
  @DisplayName("Builder Guards")
  class BuilderGuards {

    @Test
    @DisplayName("pageSize(0) throws IllegalArgumentException")
    void testPageSizeZero() {
      assertThrows(IllegalArgumentException.class, () -> TwDataTable.of(Person.class).pageSize(0));
    }

    @Test
    @DisplayName("pageSize(-1) throws IllegalArgumentException")
    void testPageSizeNegative() {
      assertThrows(IllegalArgumentException.class, () -> TwDataTable.of(Person.class).pageSize(-1));
    }
  }

  @Nested
  @DisplayName("Basic Build")
  class BasicBuild {

    @Test
    @DisplayName("Build table with columns")
    void testBasicBuild() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class)
              .column("Name", Person::name)
              .column("Email", Person::email)
              .column("Age", p -> String.valueOf(p.age()))
              .build();
      assertNotNull(t);
      assertNotNull(t.container());
    }

    @Test
    @DisplayName("Column count matches configuration")
    void testColumnCount() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class)
              .column("Name", Person::name)
              .column("Email", Person::email)
              .build();
      assertEquals(2, t.getColumns().size());
    }

    @Test
    @DisplayName("TwDataTable is instance of TableView")
    void testTableViewAccess() {
      TwDataTable<Person> t = buildBasic();
      assertTrue(t instanceof TableView);
    }

    @Test
    @DisplayName("Container has table as child")
    void testContainerAccess() {
      TwDataTable<Person> t = buildBasic();
      assertNotNull(t.container());
      assertTrue(t.container().getChildren().contains(t));
    }

    @Test
    @DisplayName("Style classes applied correctly")
    void testStyleClasses() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class)
              .column("Name", Person::name)
              .style("table-striped", "table-hover")
              .build();
      assertTrue(t.getStyleClass().contains("table-striped"));
      assertTrue(t.getStyleClass().contains("table-hover"));
      assertTrue(t.getStyleClass().contains("table-view"));
    }
  }

  @Nested
  @DisplayName("Data Operations")
  class DataOperations {

    @Test
    @DisplayName("Set items correctly")
    void testSetItems() {
      TwDataTable<Person> t = buildBasic();
      t.setItems(sampleData());
      assertEquals(5, t.totalSize());
    }

    @Test
    @DisplayName("Clear items correctly")
    void testClearItems() {
      TwDataTable<Person> t = buildBasic();
      t.setItems(sampleData());
      t.setItems(List.of()); // clear
      assertEquals(0, t.totalSize());
    }

    @Test
    @DisplayName("Add items correctly")
    void testAddItems() {
      TwDataTable<Person> t = buildBasic();
      t.setItems(List.of(new Person("Alice", "a@x.com", 30)));
      t.addItems(List.of(new Person("Bob", "b@x.com", 25)));
      assertEquals(2, t.totalSize());
    }

    @Test
    @DisplayName("Total size returns correct count")
    void testTotalSize() {
      TwDataTable<Person> t = buildBasic();
      assertEquals(0, t.totalSize());
      t.setItems(sampleData());
      assertEquals(5, t.totalSize());
    }
  }

  @Nested
  @DisplayName("Filtering")
  class Filtering {

    @Test
    @DisplayName("Filter by age")
    void testFilteredSize() {
      TwDataTable<Person> t = buildBasic();
      t.setItems(sampleData());
      t.setFilter(p -> p.age() < 30);
      // Alice(30 excluded), Bob(25), Diana(28), Eve(22) = 3 under 30
      assertEquals(3, t.filteredSize());
    }

    @Test
    @DisplayName("Set and change filter")
    void testSetFilter() {
      TwDataTable<Person> t = buildBasic();
      t.setItems(sampleData());
      t.setFilter(p -> p.name().startsWith("A"));
      assertEquals(1, t.filteredSize());
      t.setFilter(p -> true); // show all
      assertEquals(5, t.filteredSize());
    }

    @Test
    @DisplayName("Clear filter restores all items")
    void testClearFilter() {
      TwDataTable<Person> t = buildBasic();
      t.setItems(sampleData());
      t.setFilter(p -> false); // hide all
      assertEquals(0, t.filteredSize());
      t.clearFilter();
      assertEquals(5, t.filteredSize());
    }

    @Test
    @DisplayName("Programmatic filter survives a search-bar filter (combined with AND)")
    void testSetFilterCombinesWithSearch() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class)
              .column("Name", Person::name)
              .column("Email", Person::email)
              .searchable(true)
              .build();
      t.setItems(sampleData());

      // Programmatic filter alone: age < 30 -> Bob(25), Diana(28), Eve(22)
      t.setFilter(p -> p.age() < 30);
      assertEquals(3, t.filteredSize());

      // Now type a search term that must narrow the SAME programmatic filter instead of
      // replacing it. Before the fix, the search listener overwrote filtered's predicate, so the
      // programmatic filter was silently dropped.
      // "example" contains no 'z', so only the two rows whose *name* has no match survive:
      // rows matching "z" anywhere -> none, so ANDing with age<30 keeps Bob, Diana and Eve.
      searchFieldOf(t).setText("z");
      awaitSearchDebounce();
      assertEquals(
          0,
          t.filteredSize(),
          "a term matching nothing must yield nothing even with a permissive programmatic filter");

      // A term that matches a subset of the programmatically filtered rows.
      // "bo" appears in Bob's name/email only.
      searchFieldOf(t).setText("bo");
      awaitSearchDebounce();
      assertEquals(
          1,
          t.filteredSize(),
          "search must combine with the programmatic filter instead of replacing it");

      // Removing the search term restores the programmatic filter result.
      searchFieldOf(t).setText("");
      awaitSearchDebounce();
      assertEquals(3, t.filteredSize(), "clearing the search restores the programmatic filter");
    }

    @Test
    @DisplayName("Search alone still works when no programmatic filter is set")
    void testSearchWithoutProgrammaticFilter() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class)
              .column("Name", Person::name)
              .searchable(true)
              .build();
      t.setItems(sampleData());
      assertEquals(5, t.filteredSize());

      // "diana" matches exactly one row by name.
      searchFieldOf(t).setText("diana");
      awaitSearchDebounce();
      assertEquals(1, t.filteredSize(), "the search bar alone must filter the table");

      searchFieldOf(t).setText("");
      awaitSearchDebounce();
      assertEquals(5, t.filteredSize(), "clearing the search must restore every row");
    }

    @Test
    @DisplayName("clearFilter drops only the programmatic filter, keeping the search")
    void testClearFilterKeepsSearch() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class)
              .column("Name", Person::name)
              .searchable(true)
              .build();
      t.setItems(sampleData());

      // age < 30 -> Bob(25), Diana(28), Eve(22)
      t.setFilter(p -> p.age() < 30);
      assertEquals(3, t.filteredSize());

      // "a" matches alice, charlie and diana by name. ANDed with age < 30 only Diana survives.
      searchFieldOf(t).setText("a");
      awaitSearchDebounce();
      assertEquals(1, t.filteredSize(), "search and programmatic filter must AND together");

      // Dropping the programmatic filter leaves the search term in force: alice, charlie, diana.
      t.clearFilter();
      assertEquals(
          3,
          t.filteredSize(),
          "clearFilter must remove only the programmatic filter, not the search term");
    }

    /** Reaches the private search field of a searchable table through the public container. */
    private TextField searchFieldOf(TwDataTable<Person> table) {
      return (TextField)
          lookupAll(table.container(), TextField.class)
              .stream()
              .findFirst()
              .orElseThrow(() -> new AssertionError("search field not found in container"));
    }

    /** The search field is debounced with a 250ms PauseTransition; let it fire and settle. */
    private void awaitSearchDebounce() {
      // The PauseTransition advances on the FX pulse, so the FX thread must stay alive while
      // sleeping. waitForFxEvents() blocks until the queued runnables and pulses are drained.
      org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
      try {
        Thread.sleep(600); // > 250ms debounce, then one more pulse
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new AssertionError("interrupted while waiting for the search debounce", e);
      }
      org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
    }
  }

  @Nested
  @DisplayName("Pagination")
  class Pagination {

    @Test
    @DisplayName("Sorting reorders the visible page when pagination is enabled")
    void testSortingWithPaginationReordersVisibleItems() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class)
              .column("Name", Person::name)
              .pageSize(2)
              .build();

      // Unsorted input: Alice, Bob, Charlie, Diana, Eve
      t.setItems(sampleData());
      assertEquals(List.of("Alice", "Bob"), visibleNames(t));

      // Sort ascending the way a header click does. The sample data is already ascending by
      // name, so the visible page must stay "Alice, Bob".
      sortByName(t, TableColumn.SortType.ASCENDING);
      assertEquals(
          List.of("Alice", "Bob"),
          visibleNames(t),
          "ascending sort must keep the alphabetically first pair visible");

      // Now flip to descending. This is the assertion that fails without the fix: the TableView
      // holds a materialized copy of the page, so reordering the sorted view never reached the
      // visible rows and they stayed "Alice, Bob".
      sortByName(t, TableColumn.SortType.DESCENDING);
      assertEquals(
          List.of("Eve", "Diana"),
          visibleNames(t),
          "descending sort must re-slice the visible page");

      // And back to ascending again, to prove the refresh is not a one-shot reaction.
      sortByName(t, TableColumn.SortType.ASCENDING);
      assertEquals(
          List.of("Alice", "Bob"),
          visibleNames(t),
          "toggling back to ascending must re-slice the visible page again");
    }

    /** Reads the rendered column values of the current page. */
    private List<String> visibleNames(TwDataTable<Person> table) {
      return table.getItems().stream().map(Person::name).toList();
    }

    /**
     * Applies a sort the way a header click does: set the column's SortType and then put the column
     * into the sort order. TableView only rebuilds its comparator from the sort order list, so
     * changing the SortType alone leaves the comparator stale.
     */
    private void sortByName(TwDataTable<Person> table, TableColumn.SortType type) {
      interact(
          () -> {
            TableColumn<Person, ?> column = table.getColumns().get(0);
            column.setSortType(type);
            table.getSortOrder().setAll(column);
          });
    }

    @Test
    @DisplayName("Page count calculation")
    void testPaginationPageCount() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class).column("Name", Person::name).pageSize(2).build();
      t.setItems(sampleData()); // 5 items, pageSize=2 → 3 pages
      assertEquals(3, t.pageCount());
    }

    @Test
    @DisplayName("Go to page with clamping")
    void testPaginationGoToPage() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class).column("Name", Person::name).pageSize(2).build();
      t.setItems(sampleData());
      assertEquals(0, t.currentPage());
      t.goToPage(1);
      assertEquals(1, t.currentPage());
      t.goToPage(99); // clamp to max
      assertEquals(2, t.currentPage());
      t.goToPage(-1); // clamp to 0
      assertEquals(0, t.currentPage());
    }

    @Test
    @DisplayName("Next and previous page navigation")
    void testPaginationNextPrev() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class).column("Name", Person::name).pageSize(2).build();
      t.setItems(sampleData());
      assertEquals(0, t.currentPage());
      t.nextPage();
      assertEquals(1, t.currentPage());
      t.prevPage();
      assertEquals(0, t.currentPage());
      t.prevPage(); // at 0, should not go negative
      assertEquals(0, t.currentPage());
    }

    @Test
    @DisplayName("Non-paginated table behavior")
    void testPaginationGuards() {
      // Non-paginated table: pageCount always 1, currentPage always 0
      TwDataTable<Person> t = buildBasic();
      t.setItems(sampleData());
      assertEquals(1, t.pageCount());
      assertEquals(0, t.currentPage());
      t.goToPage(5); // no-op on non-paginated
      assertEquals(0, t.currentPage());
    }

    @Test
    @DisplayName("Page size minimum guard")
    void testPageSizeGuard() {
      // Build with minimum valid page size
      TwDataTable<Person> t =
          TwDataTable.of(Person.class).column("Name", Person::name).pageSize(1).build();
      t.setItems(sampleData());
      assertEquals(5, t.pageCount());
    }
  }

  @Nested
  @DisplayName("Searchable")
  class Searchable {

    @Test
    @DisplayName("Searchable container has search bar")
    void testSearchableContainer() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class)
              .column("Name", Person::name)
              .column("Email", Person::email)
              .searchable(true)
              .build();
      // Container should have search bar (TextField) as first child
      VBox container = t.container();
      boolean hasSearchBar =
          container.getChildren().stream()
              .anyMatch(
                  n ->
                      n.getStyleClass().contains("search-bar")
                          || (n instanceof javafx.scene.layout.HBox hb
                              && hb.getChildren().stream()
                                  .anyMatch(c -> c instanceof javafx.scene.control.TextField)));
      assertTrue(hasSearchBar, "Container should have a search bar");
    }
  }

  @Nested
  @DisplayName("Selection")
  class Selection {

    @Test
    @DisplayName("Selected item returns null when no selection")
    void testSelectedItemNull() {
      TwDataTable<Person> t = buildBasic();
      t.setItems(sampleData());
      assertNull(t.selectedItem());
    }

    @Test
    @DisplayName("Selected item after selection")
    void testSelectedItem() {
      TwDataTable<Person> t = buildBasic();
      t.setItems(sampleData());
      interact(() -> t.getSelectionModel().selectFirst());
      assertNotNull(t.selectedItem());
    }

    @Test
    @DisplayName("Clear selection")
    void testClearSelection() {
      TwDataTable<Person> t = buildBasic();
      t.setItems(sampleData());
      interact(() -> t.getSelectionModel().selectFirst());
      assertNotNull(t.selectedItem());
      t.clearSelection();
      assertNull(t.selectedItem());
    }
  }

  /**
   * Collects every descendant of {@code root} that is an instance of {@code type}, depth first.
   * Used to reach the internal search field, which {@link TwDataTable} does not expose.
   */
  private static <N> List<N> lookupAll(Node root, Class<N> type) {
    List<N> found = new ArrayList<>();
    Deque<Node> pending = new ArrayDeque<>();
    pending.push(root);
    while (!pending.isEmpty()) {
      Node node = pending.pop();
      if (type.isInstance(node)) {
        found.add(type.cast(node));
      }
      if (node instanceof Parent parent) {
        // push() reverses order, so iterate in reverse to keep a natural depth-first traversal
        List<Node> children = new ArrayList<>(parent.getChildrenUnmodifiable());
        for (int i = children.size() - 1; i >= 0; i--) {
          pending.push(children.get(i));
        }
      }
    }
    return found;
  }
}
