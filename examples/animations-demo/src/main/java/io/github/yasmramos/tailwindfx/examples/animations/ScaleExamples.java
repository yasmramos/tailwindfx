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
 * Scale animations: {@code scaleIn} (pop-in entrance) and {@code scaleOut} (shrink-and-fade exit).
 *
 * <p>{@code scaleIn} starts the node at 85% scale with opacity 0 and grows it to full size;
 * {@code scaleOut} shrinks it back down while fading out. The exit example restores the node so the
 * demo can be replayed.
 */
public class ScaleExamples extends Application {

  @Override
  public void start(Stage stage) {
    Label badge = new Label("Scale me");
    TwStyle.apply(badge, "bg-violet-500", "text-white", "text-lg", "px-8", "py-6", "rounded-lg");
    StackPane viewport = new StackPane(badge);
    viewport.setPrefSize(360, 200);

    Button scaleIn = new Button("scaleIn");
    scaleIn.setOnAction(e -> TwAnimation.scaleIn(badge, TwAnimation.SLOW).play());

    Button scaleOut = new Button("scaleOut");
    scaleOut.setOnAction(
        e ->
            TwAnimation.scaleOut(badge, TwAnimation.SLOW)
                .onFinished(
                    ev -> {
                      badge.setOpacity(1);
                      badge.setScaleX(1);
                      badge.setScaleY(1);
                    })
                .play());

    VBox root = new VBox(16, viewport, new HBox(12, scaleIn, scaleOut));
    root.setAlignment(Pos.CENTER);
    TwStyle.apply(root, "bg-gray-50", "p-8");

    Scene scene = new Scene(root, 420, 320);
    TwInstall.install(scene);
    stage.setTitle("Scale animations");
    stage.setScene(scene);
    stage.show();

    TwAnimation.scaleIn(badge, TwAnimation.SLOW).play();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
