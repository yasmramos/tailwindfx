package io.github.yasmramos.tailwindfx;

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

/** TEMPORARY scratch check: finds which -fx-effect syntax the JavaFX CSS parser accepts. */
@ExtendWith(ApplicationExtension.class)
public class EffectSyntaxIT {

  private static final List<String> WARNINGS = new ArrayList<>();

  private static int warningsFor(String css) {
    WARNINGS.clear();
    Logger logger = Logger.getLogger("javafx.css");
    logger.setUseParentHandlers(false);
    logger.setLevel(Level.ALL);
    Handler handler =
        new Handler() {
          @Override
          public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
              WARNINGS.add(record.getMessage());
            }
          }

          @Override
          public void flush() {}

          @Override
          public void close() {}
        };
    logger.addHandler(handler);
    StackPane root = new StackPane();
    Scene scene = new Scene(root, 50, 50);
    scene
        .getStylesheets()
        .add(
            "data:text/css;base64,"
            + java.util.Base64.getEncoder()
                .encodeToString(css.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    root.applyCss();
    root.layout();
    logger.removeHandler(handler);
    int count = WARNINGS.size();
    if (count > 0) {
      System.out.println("[TEMP]     -> " + WARNINGS.get(0));
    }
    return count;
  }

  @Test
  public void probeEffectSyntax() {
    String[] candidates = {
      ".a { -fx-effect: dropshadow(gaussian, 2, rgba(0, 0, 0, 0.05)); }",
      ".a { -fx-effect: dropshadow(one-pass-box, 2, rgba(0, 0, 0, 0.05)); }",
      ".a { -fx-effect: dropshadow(two-pass-box, 2, rgba(0, 0, 0, 0.05)); }",
      ".a { -fx-effect: dropshadow(0, 1, 2, rgba(0, 0, 0, 0.05)); }",
      ".a { -fx-effect: dropshadow(gaussian, 2, 0, 1, rgba(0, 0, 0, 0.05)); }",
      ".a { -fx-effect: innershadow(0, 1, 2, rgba(0, 0, 0, 0.05)); }",
      ".a { -fx-effect: innershadow(gaussian, 2, rgba(0, 0, 0, 0.05)); }"
    };

    for (String candidate : candidates) {
      int warnings = warningsFor(candidate);
      System.out.println(
          "[TEMP] warnings=" + warnings + (warnings == 0 ? "  OK   " : "  FAIL ") + candidate);
    }
  }
}