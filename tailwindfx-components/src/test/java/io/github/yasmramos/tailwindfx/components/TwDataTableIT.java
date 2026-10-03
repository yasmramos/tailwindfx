package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.TableView;
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
      // programmatic filter was silently dropped and all 5 rows matched the term.
      t.container();
      searchFieldOf(t).setText("e");
      awaitSearchDebounce();
      // Rows matching "e" in name or email AND age < 30.
      assertEquals(2, t.filteredSize(), "search must combine with the programmatic filter");

      // Removing the search term restores the programmatic filter result.
      searchFieldOf(t).setText("");
      awaitSearchDebounce();
      assertEquals(3, t.filteredSize(), "clearing the search restores the programmatic filter");
    }

    @Test
    @DisplayName("Programmatic filter still applies after clearFilter with an active search")
    void testClearFilterKeepsSearch() {
      TwDataTable<Person> t =
          TwDataTable.of(Person.class)
              .column("Name", Person::name)
              .searchable(true)
              .build();
      t.setItems(sampleData());

      t.setFilter(p -> p.age() >= 28);
      searchFieldOf(t).setText("a");
      awaitSearchDebounce();
      int combined = t.filteredSize();

      t.clearFilter();
      // After clearing the programmatic filter only the search term remains.
      int searchOnly = t.filteredSize();
      assertTrue(searchOnly >= combined, "clearing the filter cannot narrow the result set");
    }

    /** Reaches the private search field of a searchable table through the public container. */
    private javafx.scene.control.TextField searchFieldOf(TwDataTable<Person> table) {
      javafx.scene.Node searchBar =
          table.container().getChildren().stream()
              .filter(n -> n.getStyleClass().contains("search-bar"))
              .findFirst()
              .orElseThrow(() -> new AssertionError("search bar not found in container"));
      return (javafx.scene.control.TextField) ((javafx.scene.layout.HBox) searchBar).getChildren().get(0);
    }

    /** The search field is debounced with a 250ms PauseTransition; wait for it to fire. */
    private void awaitSearchDebounce() {
      org.testfx.util.WaitForAsyncUtils.waitFor(
          2, java.util.concurrent.TimeUnit.SECONDS);
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

      // Unsorted input: Alice, Bob, Diana, Eve, Frank
      t.setItems(sampleData());
      assertEquals(List.of("Alice", "Bob"), visibleNames(t));

      // Change the comparator the way TableView does when a column header is clicked.
      t.getComparatorProperty().set(String::compareTo);
      // With the fix the page is re-sliced from the sorted view, so the first page is the
      // alphabetically first pair. Before the fix the materialized copy was never refreshed and
      // the visible rows stayed "Alice, Bob" no matter what the comparator was.
      assertEquals(List.of("Alice", "Bob"), visibleNames(t));

      t.getComparatorProperty().set(Comparator.<Person, String>comparing(Person::name).reversed());
      assertEquals(
          List.of("Frank", "Eve"), visibleNames(t), "descending sort must re-slice the visible page");
    }

    /** Reads the rendered column values of the current page. */
    private List<String> visibleNames(TwDataTable<Person> table) {
      return table.getItems().stream().map(Person::name).toList();
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
}
