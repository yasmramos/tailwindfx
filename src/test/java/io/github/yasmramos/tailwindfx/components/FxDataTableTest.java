package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.components.FxDataTable;
import javafx.application.Platform;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for {@link FxDataTable} — builder, sorting, filtering, pagination,
 * search.
 */
public final class FxDataTableTest {

    private FxDataTableTest() {
    }

    private static int passed = 0, failed = 0;

    record Person(String name, String email, int age) {

    }

    static void ok(String l) {
        System.out.printf("  ✅ %s%n", l);
        passed++;
    }

    static void fail(String l, String m) {
        System.out.printf("  ❌ %s — %s%n", l, m);
        failed++;
    }

    static void check(String l, boolean v) {
        if (v) {
            ok(l);
        } else {
            fail(l, "false");
    
        }}

    static void eq(String l, Object e, Object a) {
        if (e == null ? a == null : e.equals(a)) {
            ok(l); 
        }else {
            fail(l, "expected <" + e + "> got <" + a + ">");
        }
    }

    static void throws_(String l, Class<? extends Throwable> t, Runnable r) {
        try {
            r.run();
            fail(l, "no throw");
        } catch (Throwable e) {
            if (t.isInstance(e)) {
                ok(l);
            } else {
                fail(l, e.getClass().getSimpleName());
        
            }}
    }

    static void runFx(Runnable w) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                w.run();
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(3, TimeUnit.SECONDS)) {
            throw new RuntimeException("timeout");
        }
        if (err.get() != null) {
            throw new RuntimeException(err.get());
        }
    }

    static List<Person> sampleData() {
        return List.of(
                new Person("Alice", "alice@example.com", 30),
                new Person("Bob", "bob@example.com", 25),
                new Person("Charlie", "charlie@example.com", 35),
                new Person("Diana", "diana@example.com", 28),
                new Person("Eve", "eve@example.com", 22)
        );
    }

    public static boolean runAll() throws Exception {
        passed = 0;
        failed = 0;
        System.out.println("\n── FxDataTable ──");

        testBuilderGuards();
        testBasicBuild();
        testColumnCount();
        testSetItems();
        testClearItems();
        testAddItems();
        testTotalSize();
        testFilteredSize();
        testSetFilter();
        testClearFilter();
        testPaginationPageCount();
        testPaginationGoToPage();
        testPaginationNextPrev();
        testPaginationGuards();
        testSearchableContainer();
        testTableViewAccess();
        testContainerAccess();
        testSelectedItem();
        testClearSelection();
        testStyleClasses();
        testPageSizeGuard();

        System.out.printf("  %d passed, %d failed%n", passed, failed);
        return failed == 0;
    }

    // ── Builder guards ────────────────────────────────────────────────────────
    static void testBuilderGuards() {
        throws_("pageSize(0) throws", IllegalArgumentException.class,
                () -> FxDataTable.of(Person.class).pageSize(0));
        throws_("pageSize(-1) throws", IllegalArgumentException.class,
                () -> FxDataTable.of(Person.class).pageSize(-1));
    }

    static void testPageSizeGuard() throws Exception {
        runFx(() -> {
            // Build with minimum valid page size
            FxDataTable<Person> t = FxDataTable.of(Person.class)
                    .column("Name", Person::name)
                    .pageSize(1)
                    .build();
            t.setItems(sampleData());
            eq("pageSize=1: pageCount=5", 5, t.pageCount());
        });
    }

    // ── Basic build ───────────────────────────────────────────────────────────
    static void testBasicBuild() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = FxDataTable.of(Person.class)
                    .column("Name", Person::name)
                    .column("Email", Person::email)
                    .column("Age", p -> String.valueOf(p.age()))
                    .build();
            check("tableView non-null", t.tableView() != null);
            check("container non-null", t.container() != null);
        });
    }

    static void testColumnCount() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = FxDataTable.of(Person.class)
                    .column("Name", Person::name)
                    .column("Email", Person::email)
                    .build();
            eq("2 columns", 2, t.tableView().getColumns().size());
        });
    }

    // ── Data operations ───────────────────────────────────────────────────────
    static void testSetItems() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = buildBasic();
            t.setItems(sampleData());
            eq("setItems: 5 rows", 5, t.totalSize());
        });
    }

    static void testClearItems() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = buildBasic();
            t.setItems(sampleData());
            t.setItems(List.of()); // clear
            eq("clearItems: 0 rows", 0, t.totalSize());
        });
    }

    static void testAddItems() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = buildBasic();
            t.setItems(List.of(new Person("Alice", "a@x.com", 30)));
            t.addItems(List.of(new Person("Bob", "b@x.com", 25)));
            eq("addItems: 2 total", 2, t.totalSize());
        });
    }

    static void testTotalSize() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = buildBasic();
            eq("empty: totalSize=0", 0, t.totalSize());
            t.setItems(sampleData());
            eq("after set: totalSize=5", 5, t.totalSize());
        });
    }

    // ── Filter ────────────────────────────────────────────────────────────────
    static void testFilteredSize() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = buildBasic();
            t.setItems(sampleData());
            t.setFilter(p -> p.age() < 30);
            // Alice(30 excluded), Bob(25), Diana(28), Eve(22) = 3 under 30
            check("filteredSize=3", t.filteredSize() == 3);
        });
    }

    static void testSetFilter() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = buildBasic();
            t.setItems(sampleData());
            t.setFilter(p -> p.name().startsWith("A"));
            eq("filter A: 1 result", 1, t.filteredSize());
            t.setFilter(p -> true); // show all
            eq("filter all: 5 results", 5, t.filteredSize());
        });
    }

    static void testClearFilter() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = buildBasic();
            t.setItems(sampleData());
            t.setFilter(p -> false); // hide all
            eq("filter none: 0", 0, t.filteredSize());
            t.clearFilter();
            eq("clearFilter: 5", 5, t.filteredSize());
        });
    }

    // ── Pagination ────────────────────────────────────────────────────────────
    static void testPaginationPageCount() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = FxDataTable.of(Person.class)
                    .column("Name", Person::name)
                    .pageSize(2)
                    .build();
            t.setItems(sampleData()); // 5 items, pageSize=2 → 3 pages
            eq("5 items / 2 per page = 3 pages", 3, t.pageCount());
        });
    }

    static void testPaginationGoToPage() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = FxDataTable.of(Person.class)
                    .column("Name", Person::name)
                    .pageSize(2)
                    .build();
            t.setItems(sampleData());
            eq("start at page 0", 0, t.currentPage());
            t.goToPage(1);
            eq("goToPage(1)", 1, t.currentPage());
            t.goToPage(99); // clamp to max
            eq("clamp to maxPage=2", 2, t.currentPage());
            t.goToPage(-1); // clamp to 0
            eq("clamp to 0", 0, t.currentPage());
        });
    }

    static void testPaginationNextPrev() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = FxDataTable.of(Person.class)
                    .column("Name", Person::name)
                    .pageSize(2)
                    .build();
            t.setItems(sampleData());
            eq("start=0", 0, t.currentPage());
            t.nextPage();
            eq("after next=1", 1, t.currentPage());
            t.prevPage();
            eq("after prev=0", 0, t.currentPage());
            t.prevPage(); // at 0, should not go negative
            eq("prevPage at 0 stays 0", 0, t.currentPage());
        });
    }

    static void testPaginationGuards() throws Exception {
        runFx(() -> {
            // Non-paginated table: pageCount always 1, currentPage always 0
            FxDataTable<Person> t = buildBasic();
            t.setItems(sampleData());
            eq("non-paginated pageCount=1", 1, t.pageCount());
            eq("non-paginated currentPage=0", 0, t.currentPage());
            t.goToPage(5); // no-op on non-paginated
            eq("goToPage noop on non-paginated", 0, t.currentPage());
        });
    }

    // ── Searchable ────────────────────────────────────────────────────────────
    static void testSearchableContainer() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = FxDataTable.of(Person.class)
                    .column("Name", Person::name)
                    .column("Email", Person::email)
                    .searchable(true)
                    .build();
            // Container should have search bar (TextField) as first child
            boolean hasTextField = t.container().getChildren().stream()
                    .anyMatch(n -> n.getStyleClass().contains("search-bar")
                    || (n instanceof javafx.scene.layout.HBox hb
                    && hb.getChildren().stream().anyMatch(
                            c -> c instanceof javafx.scene.control.TextField)));
            check("searchable container has search bar", hasTextField);
        });
    }

    // ── Access ────────────────────────────────────────────────────────────────
    static void testTableViewAccess() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = buildBasic();
            TableView<Person> tv = t.tableView();
            check("tableView() non-null", tv != null);
            check("tableView instanceof TableView", tv instanceof TableView);
        });
    }

    static void testContainerAccess() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = buildBasic();
            check("container() non-null", t.container() != null);
            check("container has table as child",
                    t.container().getChildren().contains(t.tableView()));
        });
    }

    static void testSelectedItem() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = buildBasic();
            t.setItems(sampleData());
            check("no selection: null", t.selectedItem() == null);
            t.tableView().getSelectionModel().selectFirst();
            check("after selectFirst: non-null", t.selectedItem() != null);
        });
    }

    static void testClearSelection() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = buildBasic();
            t.setItems(sampleData());
            t.tableView().getSelectionModel().selectFirst();
            check("before clear: selected", t.selectedItem() != null);
            t.clearSelection();
            check("after clear: null", t.selectedItem() == null);
        });
    }

    // ── Style classes ─────────────────────────────────────────────────────────
    static void testStyleClasses() throws Exception {
        runFx(() -> {
            FxDataTable<Person> t = FxDataTable.of(Person.class)
                    .column("Name", Person::name)
                    .style("table-striped", "table-hover")
                    .build();
            check("table-striped applied",
                    t.tableView().getStyleClass().contains("table-striped"));
            check("table-hover applied",
                    t.tableView().getStyleClass().contains("table-hover"));
            check("base table-view applied",
                    t.tableView().getStyleClass().contains("table-view"));
        });
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    static FxDataTable<Person> buildBasic() {
        return FxDataTable.of(Person.class)
                .column("Name", Person::name)
                .column("Email", Person::email)
                .column("Age", p -> String.valueOf(p.age()))
                .build();
    }
}
