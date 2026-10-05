package io.github.yasmramos.tailwindfx;

import java.io.File;
import java.net.URL;
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

/** TEMPORARY scratch check: reports JavaFX CSS parser warnings for the generated stylesheet. */
@ExtendWith(ApplicationExtension.class)
public class CssParseIT {

  private static final List<String> CSS_WARNINGS = new ArrayList<>();

  @Test
  public void detectCssParserWarnings() throws Exception {
    File css =
        new File(
            System.getProperty("user.dir")
                + "/../examples/tailwindfx-demo-dashboard/target/classes/css/"
                + "tailwindfx-generated.css");
    System.out.println("[TEMP] css exists=" + css.exists() + " path=" + css.getAbsolutePath());

    Logger parserLogger = Logger.getLogger("javafx.css");
    Handler handler =
        new Handler() {
          @Override
          public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
              CSS_WARNINGS.add(record.getMessage());
            }
          }

          @Override
          public void flush() {}

          @Override
          public void close() {}
        };
    parserLogger.setUseParentHandlers(false);
    parserLogger.setLevel(Level.ALL);
    parserLogger.addHandler(handler);

    StackPane root = new StackPane();
    Scene scene = new Scene(root, 100, 100);
    URL url = css.toURI().toURL();
    scene.getStylesheets().add(url.toExternalForm());
    // Force the stylesheet to be parsed and applied.
    root.applyCss();
    root.layout();

    parserLogger.removeHandler(handler);
    System.out.println("[TEMP] css parser warnings: " + CSS_WARNINGS.size());
    for (String w : CSS_WARNINGS) {
      System.out.println("[TEMP]   " + w);
    }

    // Control group: the same declarations as before the fix must be reported, otherwise the
    // zero-warning result above would prove nothing.
    CSS_WARNINGS.clear();
    parserLogger.setLevel(Level.ALL);
    parserLogger.addHandler(handler);
    StackPane control = new StackPane();
    Scene controlScene = new Scene(control, 100, 100);
    controlScene
        .getStylesheets()
        .add(
            "data:text/css,"
            + java.net.URLEncoder.encode(
                ".text-sm { -fx-text-fill: 14px; }"
                    + ".font-bold { -fx-font-family: 700; }"
                    + ".px-3 { padding: 0px 6px 0px 6px; }"
                    + ".shadow-md { -fx-effect: 0 1px 3px 0 rgba(0, 0, 0, 0.1); }",
                java.nio.charset.StandardCharsets.UTF_8));
    control.applyCss();
    control.layout();
    parserLogger.removeHandler(handler);
    System.out.println("[TEMP] control (pre-fix) warnings: " + CSS_WARNINGS.size());
    for (String w : CSS_WARNINGS) {
      System.out.println("[TEMP]   " + w);
    }
  }
}