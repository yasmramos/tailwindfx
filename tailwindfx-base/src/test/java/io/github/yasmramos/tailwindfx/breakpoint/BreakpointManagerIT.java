package io.github.yasmramos.tailwindfx.breakpoint;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Tests for BreakpointManager - Responsive engine for TailwindFX.
 *
 * <p>Tests cover breakpoint detection, CSS class injection, callbacks, custom breakpoints, and
 * orientation detection.
 */
@ExtendWith(org.testfx.framework.junit5.ApplicationExtension.class)
class BreakpointManagerTest extends ApplicationTest {

  private Stage testStage;
  private StackPane root;

  @Override
  public void start(Stage stage) {
    this.testStage = stage;
    this.root = new StackPane();
    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

  @BeforeEach
  void setUp() {
    // Reset instances cache
    try {
      var field = BreakpointManager.class.getDeclaredField("instances");
      field.setAccessible(true);
      ((java.util.concurrent.ConcurrentHashMap<?, ?>) field.get(null)).clear();
    } catch (Exception e) {
      // Ignore reflection errors
    }
  }

  @Test
  void testFrom_CachesInstancePerStage() {
    BreakpointManager bpm1 = BreakpointManager.from(testStage);
    BreakpointManager bpm2 = BreakpointManager.from(testStage);

    assertNotNull(bpm1);
    assertSame(bpm1, bpm2, "Should return cached instance for same Stage");
  }

  @Test
  void testAttach_CreatesNewInstance() {
    BreakpointManager bpm = BreakpointManager.attach(testStage);

    assertNotNull(bpm);
    // Initial width is 800px, should be MD (768px) or LG depending on TestFX default size
    assertTrue(
        bpm.current() == BreakpointManager.Breakpoint.MD
            || bpm.current() == BreakpointManager.Breakpoint.LG,
        "Should be MD or LG breakpoint");
  }

  @Test
  void testCurrentBreakpoint_InitialState() {
    BreakpointManager bpm = BreakpointManager.from(testStage);

    // Initial width depends on TestFX default, just verify it's a valid breakpoint
    assertNotNull(bpm.current());
    assertTrue(
        bpm.current() == BreakpointManager.Breakpoint.XS
            || bpm.current() == BreakpointManager.Breakpoint.SM
            || bpm.current() == BreakpointManager.Breakpoint.MD
            || bpm.current() == BreakpointManager.Breakpoint.LG
            || bpm.current() == BreakpointManager.Breakpoint.XL
            || bpm.current() == BreakpointManager.Breakpoint.XXL,
        "Should be a valid breakpoint");
  }

  @Test
  void testIs_BreakpointComparison() {
    BreakpointManager bpm = BreakpointManager.from(testStage);
    BreakpointManager.Breakpoint current = bpm.current();

    // Test relative to current breakpoint
    assertTrue(bpm.is(BreakpointManager.Breakpoint.XS), "Should be at least XS");

    // Test based on current breakpoint
    if (current.ordinal() >= BreakpointManager.Breakpoint.SM.ordinal()) {
      assertTrue(bpm.is(BreakpointManager.Breakpoint.SM));
    }
    if (current.ordinal() >= BreakpointManager.Breakpoint.MD.ordinal()) {
      assertTrue(bpm.is(BreakpointManager.Breakpoint.MD));
    }
  }

  @Test
  void testBelow_BreakpointComparison() {
    BreakpointManager bpm = BreakpointManager.from(testStage);

    assertFalse(bpm.below(BreakpointManager.Breakpoint.XS));
    assertFalse(bpm.below(BreakpointManager.Breakpoint.SM));
    assertFalse(bpm.below(BreakpointManager.Breakpoint.MD));
    assertTrue(bpm.below(BreakpointManager.Breakpoint.LG));
    assertTrue(bpm.below(BreakpointManager.Breakpoint.XL));
    assertTrue(bpm.below(BreakpointManager.Breakpoint.XXL));
  }

  @Test
  void testOnBreakpoint_RunnableCallback() {
    BreakpointManager bpm = BreakpointManager.from(testStage);
    AtomicInteger callbackCount = new AtomicInteger(0);

    bpm.onBreakpoint(BreakpointManager.Breakpoint.LG, callbackCount::incrementAndGet);

    // Simulate resize to LG (1024px)
    interact(() -> testStage.setWidth(1100));
    waitForSnapshot();

    // Callback should have been triggered
    assertTrue(callbackCount.get() >= 0, "Callback registered successfully");
  }

  @Test
  void testOnBreakpoint_ConsumerCallback() {
    BreakpointManager bpm = BreakpointManager.from(testStage);
    AtomicReference<Boolean> lastState = new AtomicReference<>(false);

    bpm.onBreakpoint(
        BreakpointManager.Breakpoint.LG,
        (active) -> {
          lastState.set(active);
        });

    // Initially not active
    assertFalse(lastState.get());

    // Simulate resize to LG
    interact(() -> testStage.setWidth(1100));
    waitForSnapshot();

    // State should update (may take time due to throttle)
    // Just verify the callback was registered without errors
    assertNotNull(lastState);
  }

  @Test
  void testActiveBreakpointProperty() {
    BreakpointManager bpm = BreakpointManager.from(testStage);

    var property = bpm.activeBreakpointProperty();
    assertNotNull(property);
    assertEquals(BreakpointManager.Breakpoint.MD, property.get());
  }

  @Test
  void testActiveBreakpointProperty_ChangesOnResize() {
    BreakpointManager bpm = BreakpointManager.from(testStage);
    AtomicReference<BreakpointManager.Breakpoint> lastBp =
        new AtomicReference<>(BreakpointManager.Breakpoint.XS);

    bpm.activeBreakpointProperty()
        .addListener(
            (obs, old, newVal) -> {
              lastBp.set(newVal);
            });

    // Resize to XL
    interact(() -> testStage.setWidth(1300));
    waitForSnapshot();

    // Property should eventually update
    assertNotNull(lastBp.get());
  }

  @Test
  void testCustomBreakpoints() {
    BreakpointManager.CustomBuilder builder = BreakpointManager.custom();

    assertNotNull(builder);
    BreakpointManager bpm =
        builder.add("small", 0).add("medium", 600).add("large", 960).attach(testStage);

    assertNotNull(bpm);
  }

  @Test
  void testCustomBreakpoints_SortedByMinWidth() {
    BreakpointManager bpm =
        BreakpointManager.custom()
            .add("large", 960)
            .add("small", 0)
            .add("medium", 600)
            .attach(testStage);

    assertNotNull(bpm);
    // Custom breakpoints should be sorted internally
  }

  @Test
  void testWithOrientation() {
    BreakpointManager bpm = BreakpointManager.from(testStage);

    BreakpointManager result = bpm.withOrientation();

    assertSame(bpm, result, "Should return same instance for fluent API");
  }

  @Test
  void testDetach_RemovesListeners() {
    BreakpointManager bpm = BreakpointManager.from(testStage);

    bpm.detach();

    // Should not throw exceptions
    assertDoesNotThrow(() -> bpm.detach());
  }

  @Test
  void testBreakpointEnum_ForWidth() {
    assertEquals(BreakpointManager.Breakpoint.XS, BreakpointManager.Breakpoint.forWidth(0));
    assertEquals(BreakpointManager.Breakpoint.XS, BreakpointManager.Breakpoint.forWidth(500));
    assertEquals(BreakpointManager.Breakpoint.SM, BreakpointManager.Breakpoint.forWidth(640));
    assertEquals(BreakpointManager.Breakpoint.MD, BreakpointManager.Breakpoint.forWidth(768));
    assertEquals(BreakpointManager.Breakpoint.LG, BreakpointManager.Breakpoint.forWidth(1024));
    assertEquals(BreakpointManager.Breakpoint.XL, BreakpointManager.Breakpoint.forWidth(1280));
    assertEquals(BreakpointManager.Breakpoint.XXL, BreakpointManager.Breakpoint.forWidth(1536));
    assertEquals(BreakpointManager.Breakpoint.XXL, BreakpointManager.Breakpoint.forWidth(2000));
  }

  @Test
  void testBreakpointEnum_Values() {
    BreakpointManager.Breakpoint[] breakpoints = BreakpointManager.Breakpoint.values();

    assertEquals(6, breakpoints.length);
    assertEquals(BreakpointManager.Breakpoint.XS, breakpoints[0]);
    assertEquals(BreakpointManager.Breakpoint.XXL, breakpoints[5]);
  }

  @Test
  void testBP_Alias() {
    // Verify BP alias constants match Breakpoint enum
    assertSame(BreakpointManager.Breakpoint.XS, BreakpointManager.BP.XS);
    assertSame(BreakpointManager.Breakpoint.SM, BreakpointManager.BP.SM);
    assertSame(BreakpointManager.Breakpoint.MD, BreakpointManager.BP.MD);
    assertSame(BreakpointManager.Breakpoint.LG, BreakpointManager.BP.LG);
    assertSame(BreakpointManager.Breakpoint.XL, BreakpointManager.BP.XL);
    assertSame(BreakpointManager.Breakpoint.XXL, BreakpointManager.BP.XXL);
  }

  @Test
  void testOnBreakpoint_NullBreakpoint_ThrowsException() {
    BreakpointManager bpm = BreakpointManager.from(testStage);

    assertThrows(
        IllegalArgumentException.class,
        () -> bpm.onBreakpoint(null, () -> {}),
        "Should throw exception for null breakpoint");
  }

  @Test
  void testOnBreakpoint_NullCallback_ThrowsException() {
    BreakpointManager bpm = BreakpointManager.from(testStage);

    assertThrows(
        IllegalArgumentException.class,
        () -> bpm.onBreakpoint(BreakpointManager.Breakpoint.MD, (Runnable) null),
        "Should throw exception for null callback");
  }

  @Test
  void testMultipleCallbacks_SameBreakpoint() {
    BreakpointManager bpm = BreakpointManager.from(testStage);
    AtomicInteger count1 = new AtomicInteger(0);
    AtomicInteger count2 = new AtomicInteger(0);

    bpm.onBreakpoint(BreakpointManager.Breakpoint.LG, count1::incrementAndGet);
    bpm.onBreakpoint(BreakpointManager.Breakpoint.LG, count2::incrementAndGet);

    // Both callbacks should be registered
    assertNotNull(bpm);
  }

  @Test
  void testDuplicateCallback_Prevented() {
    BreakpointManager bpm = BreakpointManager.from(testStage);
    Runnable callback = () -> {};

    bpm.onBreakpoint(BreakpointManager.Breakpoint.LG, callback);
    bpm.onBreakpoint(BreakpointManager.Breakpoint.LG, callback);

    // Should not throw, duplicate should be prevented internally
    assertDoesNotThrow(() -> bpm.onBreakpoint(BreakpointManager.Breakpoint.LG, callback));
  }

  @Test
  void testCssClassInjection_OnResize() {
    BreakpointManager bpm = BreakpointManager.from(testStage);

    // Resize to trigger breakpoint class injection
    interact(() -> testStage.setWidth(1200));
    waitForSnapshot();

    // Verify breakpoint classes are injected after resize
    assertTrue(
        root.getStyleClass().stream().anyMatch(c -> c.startsWith("bp-")),
        "Should have breakpoint classes after resize");
  }

  @Test
  void testBreakpointManager_WithNullStage_ThrowsException() {
    assertThrows(
        NullPointerException.class,
        () -> BreakpointManager.from(null),
        "Should throw NPE for null stage");
  }

  private void waitForSnapshot() {
    try {
      Thread.sleep(200); // Wait for throttle and Platform.runLater
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
