package io.github.yasmramos.tailwindfx.examples.animations;

import io.github.yasmramos.tailwindfx.TwInstall;
import io.github.yasmramos.tailwindfx.TwStyle;
import io.github.yasmramos.tailwindfx.animation.TwAnimation;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Fade animations: {@code fadeIn} (entrance) and {@code fadeOut} (exit).
 *
 * <p>Both examples replay on demand. {@code fadeIn} resets opacity to 0 before playing, so it can be
 * triggered repeatedly; {@code fadeOut} fades from the current opacity to 0, so replay it after
 * restoring the node's opacity.
 */
public class FadeExamples extends Application {

  @Override
  public void start(Stage stage) {
    Label card = new Label("Fade me");
    TwStyle.apply(card, "bg-blue-500", "text-white", "text-lg", "px-8", "py-6", "rounded-lg");
    StackPane stage2 = new StackPane(card);
    stage2.setPrefSize(360, 200);

    Button fadeIn = new Button("fadeIn");
    fadeIn.setOnAction(e -> TwAnimation.fadeIn(card, TwAnimation.SLOW).play());

    Button fadeOut = new Button("fadeOut");
    fadeOut.setOnAction(
        e ->
            TwAnimation.fadeOut(card, TwAnimation.SLOW)
                .onFinished(ev -> card.setOpacity(1)) // restore so the demo can replay
                .play());

    VBox root = new VBox(16, stage2, new HBox(12, fadeIn, fadeOut));
    root.setAlignment(Pos.CENTER);
    TwStyle.apply(root, "bg-gray-50", "p-8");

    Scene scene = new Scene(root, 420, 320);
    TwInstall.install(scene);
    stage.setTitle("Fade animations");
    stage.setScene(scene);
    stage.show();

    // Play the entrance once as soon as the window is visible.
    TwAnimation.fadeIn(card, TwAnimation.SLOW).play();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
