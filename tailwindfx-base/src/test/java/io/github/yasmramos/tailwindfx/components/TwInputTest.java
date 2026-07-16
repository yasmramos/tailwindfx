package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

/** Unit tests for TwInput component. */
@ExtendWith(ApplicationExtension.class)
public class TwInputTest {

  @Test
  public void testDefaultConstructor() {
    TwInput input = new TwInput();

    assertNotNull(input);
    assertTrue(input.getStyleClass().contains("tw-input"));
    assertFalse(input.isError());
  }

  @Test
  public void testConstructorWithPlaceholder() {
    TwInput input = new TwInput("Enter text");

    assertNotNull(input);
    assertEquals("Enter text", input.getPromptText());
    assertTrue(input.getStyleClass().contains("tw-input"));
  }

  @Test
  public void testSetError() {
    TwInput input = new TwInput();

    assertFalse(input.isError());
    input.setError(true);
    assertTrue(input.isError());
    assertTrue(input.getStyleClass().contains("input-error"));

    input.setError(false);
    assertFalse(input.isError());
    assertFalse(input.getStyleClass().contains("input-error"));
  }

  @Test
  public void testErrorAutoClearsOnTextChange() {
    TwInput input = new TwInput();
    input.setError(true);
    assertTrue(input.isError());

    input.setText("New text");
    assertFalse(input.isError());
  }

  @Test
  public void testStaticWithPlaceholder() {
    TwInput input = TwInput.withPlaceholder("Username");

    assertNotNull(input);
    assertEquals("Username", input.getPromptText());
  }

  @Test
  public void testStaticNumeric() {
    TwInput input = TwInput.numeric();

    assertNotNull(input);
    assertNotNull(input.getTextFormatter());

    input.setText("123");
    assertEquals("123", input.getText());

    input.setText("123abc");
    assertEquals("123", input.getText());
  }

  @Test
  public void testStaticDecimal() {
    TwInput input = TwInput.decimal();

    assertNotNull(input);
    assertNotNull(input.getTextFormatter());

    input.setText("123.45");
    assertEquals("123.45", input.getText());

    input.setText("123.45.67");
    assertEquals("123.45", input.getText());
  }

  @Test
  public void testStaticPassword() {
    TwInput input = TwInput.password();

    assertNotNull(input);
    assertEquals("●", input.getPromptText());
  }

  @Test
  public void testSetEchoChar() {
    TwInput input = new TwInput();
    input.setEchoChar('*');

    assertEquals("*", input.getPromptText());
  }

  @Test
  public void testNumericFormatterRejectsNonDigits() {
    TwInput input = new TwInput();
    input.setNumericOnly();

    input.setText("");
    input.appendText("a");
    assertEquals("", input.getText());

    input.appendText("123");
    assertEquals("123", input.getText());

    input.appendText("b");
    assertEquals("123", input.getText());
  }

  @Test
  public void testDecimalFormatterAcceptsValidDecimals() {
    TwInput input = new TwInput();
    input.setDecimalOnly();

    input.setText("");
    input.appendText("123");
    assertEquals("123", input.getText());

    input.appendText(".");
    assertEquals("123.", input.getText());

    input.appendText("45");
    assertEquals("123.45", input.getText());
  }

  @Test
  public void testDecimalFormatterRejectsMultipleDots() {
    TwInput input = new TwInput();
    input.setDecimalOnly();

    input.setText("123.45");
    input.appendText(".");
    assertEquals("123.45", input.getText());
  }
}
