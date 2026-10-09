package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

/**
 * Feeds the generated TailwindFX stylesheet through the real JavaFX CSS parser.
 *
 * <p>Regression guard for the generated CSS. Previously the utilities were emitted with a literal
 * {@code \n} sequence instead of a real line break, so the parser logged "Expected RBRACE" and
 * silently dropped every rule after the first broken block. Declarations that are not valid JavaFX
 * CSS (for example a web-style {@code -fx-effect: 0 1px 3px ...}) produced the same class of
 * warning.
 */
@ExtendWith(ApplicationExtension.class)
public class CssParseIT {

  /** Collects the warnings and errors logged by the JavaFX CSS parser. */
  private static final class ParserWarnings extends Handler {
    private final List<String> messages = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
      if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
        messages.add(record.getMessage());
      }
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}
  }

  private static List<String> parseWarnings(String css) throws Exception {
    File file = File.createTempFile("tailwindfx-css-parse", ".css");
    file.deleteOnExit();
    Files.writeString(file.toPath(), css);

    Logger logger = Logger.getLogger("javafx.css");
    boolean previousUseParentHandlers = logger.getUseParentHandlers();
    logger.setUseParentHandlers(false);
    logger.setLevel(Level.ALL);
    ParserWarnings handler = new ParserWarnings();
    logger.addHandler(handler);
    try {
      StackPane root = new StackPane();
      Scene scene = new Scene(root, 100, 100);
      scene.getStylesheets().add(file.toURI().toURL().toExternalForm());
      // Adding a stylesheet is lazy; applying CSS forces the parser to run.
      root.applyCss();
      root.layout();
    } finally {
      logger.removeHandler(handler);
      logger.setUseParentHandlers(previousUseParentHandlers);
    }
    return handler.messages;
  }

  @Test
  public void generatedStylesheetUsesRealLineBreaks() {
    String css = TwCatalog.generateFullCss(ThemeConfig.defaultConfig());
    assertFalse(css.contains("\\n"), "generated CSS must use real line breaks, not literal \\n");
    assertTrue(css.contains("\n"), "generated CSS must contain real line breaks");
  }

  @Test
  public void generatedStylesheetHasBalancedBraces() {
    String css = TwCatalog.generateFullCss(ThemeConfig.defaultConfig());
    assertEquals(
        css.chars().filter(c -> c == '{').count(),
        css.chars().filter(c -> c == '}').count(),
        "every rule must be closed");
  }

  @Test
  public void generatedStylesheetParsesWithoutJavaFxWarnings() throws Exception {
    String css = TwCatalog.generateFullCss(ThemeConfig.defaultConfig());
    List<String> warnings = parseWarnings(css);
    assertEquals(List.of(), warnings, () -> "JavaFX CSS parser reported: " + warnings);
  }

  @Test
  public void parserWarningsDetectionIsNotVacuous() throws Exception {
    // Sanity check: the harness must flag invalid CSS, otherwise the zero-warning assertion above
    // would pass even if nothing was actually parsed.
    List<String> warnings =
        parseWarnings(".broken { -fx-effect: 0 1px 3px 0 rgba(0, 0, 0, 0.1); }");
    assertFalse(warnings.isEmpty(), "invalid -fx-effect must be reported by the parser");
  }
}
