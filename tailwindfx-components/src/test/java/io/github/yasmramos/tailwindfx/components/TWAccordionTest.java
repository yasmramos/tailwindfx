package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.TitledPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

/** Unit tests for TWAccordion component. */
@ExtendWith(ApplicationExtension.class)
public class TWAccordionTest {

  @Test
  public void testDefaultConstructor() {
    TWAccordion accordion = new TWAccordion();

    assertNotNull(accordion);
    assertTrue(accordion.getStyleClass().contains("collapse"));
    assertTrue(accordion.getPanes().isEmpty());
  }

  @Test
  public void testConstructorWithTitledPanes() {
    TitledPane pane1 = new TitledPane("Pane 1", new javafx.scene.layout.StackPane());
    TitledPane pane2 = new TitledPane("Pane 2", new javafx.scene.layout.StackPane());

    TWAccordion accordion = new TWAccordion(pane1, pane2);

    assertNotNull(accordion);
    assertTrue(accordion.getStyleClass().contains("collapse"));
    assertEquals(2, accordion.getPanes().size());
    assertTrue(accordion.getPanes().contains(pane1));
    assertTrue(accordion.getPanes().contains(pane2));
  }

  @Test
  public void testEnsureTailwindStyleWithRegularTitledPane() {
    TitledPane pane = new TitledPane("Test", new javafx.scene.layout.StackPane());
    assertFalse(pane instanceof TWTitledPane);

    TWAccordion accordion = new TWAccordion(pane);

    // The ensureTailwindStyle method should add collapse-item class
    assertTrue(pane.getStyleClass().contains("collapse-item"));
  }

  @Test
  public void testEnsureTailwindStyleWithTWTitledPane() {
    TWTitledPane pane = new TWTitledPane("Test", new javafx.scene.layout.StackPane());

    TWAccordion accordion = new TWAccordion(pane);

    // TWTitledPane already has collapse-item from its constructor
    assertTrue(pane.getStyleClass().contains("collapse-item"));
  }

  @Test
  public void testUpdateStateClassesOnExpansion() {
    TitledPane pane1 = new TitledPane("Pane 1", new javafx.scene.layout.StackPane());
    TitledPane pane2 = new TitledPane("Pane 2", new javafx.scene.layout.StackPane());

    TWAccordion accordion = new TWAccordion(pane1, pane2);

    // Initially no pane is expanded (both should be collapse-close)
    assertFalse(pane1.getStyleClass().contains("collapse-open"));
    assertTrue(pane1.getStyleClass().contains("collapse-close"));
    assertFalse(pane2.getStyleClass().contains("collapse-open"));
    assertTrue(pane2.getStyleClass().contains("collapse-close"));

    // Expand first pane
    accordion.setExpandedPane(pane1);

    // Verify state classes are updated
    assertTrue(pane1.getStyleClass().contains("collapse-open"));
    assertFalse(pane1.getStyleClass().contains("collapse-close"));
    assertTrue(pane2.getStyleClass().contains("collapse-close"));
    assertFalse(pane2.getStyleClass().contains("collapse-open"));
  }

  @Test
  public void testUpdateStateClassesOnCollapse() {
    TitledPane pane1 = new TitledPane("Pane 1", new javafx.scene.layout.StackPane());
    TitledPane pane2 = new TitledPane("Pane 2", new javafx.scene.layout.StackPane());

    TWAccordion accordion = new TWAccordion(pane1, pane2);

    // Expand then collapse
    accordion.setExpandedPane(pane1);
    accordion.setExpandedPane(null);

    // Both should be collapsed
    assertTrue(pane1.getStyleClass().contains("collapse-close"));
    assertFalse(pane1.getStyleClass().contains("collapse-open"));
    assertTrue(pane2.getStyleClass().contains("collapse-close"));
    assertFalse(pane2.getStyleClass().contains("collapse-open"));
  }

  @Test
  public void testMultipleExpansionChanges() {
    TitledPane pane1 = new TitledPane("Pane 1", new javafx.scene.layout.StackPane());
    TitledPane pane2 = new TitledPane("Pane 2", new javafx.scene.layout.StackPane());
    TitledPane pane3 = new TitledPane("Pane 3", new javafx.scene.layout.StackPane());

    TWAccordion accordion = new TWAccordion(pane1, pane2, pane3);

    // Expand pane2
    accordion.setExpandedPane(pane2);
    assertTrue(pane2.getStyleClass().contains("collapse-open"));
    assertTrue(pane1.getStyleClass().contains("collapse-close"));
    assertTrue(pane3.getStyleClass().contains("collapse-close"));

    // Switch to pane3
    accordion.setExpandedPane(pane3);
    assertTrue(pane3.getStyleClass().contains("collapse-open"));
    assertTrue(pane1.getStyleClass().contains("collapse-close"));
    assertTrue(pane2.getStyleClass().contains("collapse-close"));
  }

  @Test
  public void testAddPaneAfterConstruction() {
    TWAccordion accordion = new TWAccordion();
    TitledPane pane = new TitledPane("Dynamic Pane", new javafx.scene.layout.StackPane());

    accordion.getPanes().add(pane);

    assertEquals(1, accordion.getPanes().size());
    // Note: ensureTailwindStyle is only called in constructor with varargs
    // so dynamically added panes won't automatically get collapse-item class
  }

  @Test
  public void testRemovePane() {
    TitledPane pane1 = new TitledPane("Pane 1", new javafx.scene.layout.StackPane());
    TitledPane pane2 = new TitledPane("Pane 2", new javafx.scene.layout.StackPane());

    TWAccordion accordion = new TWAccordion(pane1, pane2);
    assertEquals(2, accordion.getPanes().size());

    accordion.getPanes().remove(pane1);
    assertEquals(1, accordion.getPanes().size());
    assertTrue(accordion.getPanes().contains(pane2));
  }

  @Test
  public void testInitialStateClasses() {
    TitledPane pane = new TitledPane("Test", new javafx.scene.layout.StackPane());
    TWAccordion accordion = new TWAccordion(pane);

    // Initially, pane is not expanded so it should have collapse-close
    assertTrue(pane.getStyleClass().contains("collapse-close"));
    assertFalse(pane.getStyleClass().contains("collapse-open"));
  }
}
