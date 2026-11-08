package io.github.yasmramos.tailwindfx.examples.animations;

import io.github.yasmramos.tailwindfx.TwInstall;
import io.github.yasmramos.tailwindfx.TwStyle;
import io.github.yasmramos.tailwindfx.animation.TwAnimation;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Rotation and other loop/attention animations: {@code spin}, {@code pulse}, {@code breathe},
 * {@code shake}, {@code bounce} and {@code flash}.
 *
 * <p>The infinite animations ({@code spin}, {@code pulse}, {@code breathe}) keep running until they
 * are stopped; the Stop button calls {@link TwAnimation#stop()} on the most recent one. The
 * finite ones ({@code shake}, {@code bounce}, {@code flash}) replay on each click.
 */
public class RotateExamples extends Application {

  private TwAnimation running;

  @Override
  public void start(Stage stage) {
    Rectangle square = new Rectangle(64, 64);
    square.setArcWidth(12);
    square.setArcHeight(12);
    square.setStyle("-fx-fill: #f59e0b;");
    Label caption = new Label("Rotate / loop");
    TwStyle.apply(caption, "text-gray-700", "text-sm");
    StackPane viewport = new StackPane(new VBox(10, square, caption));
    viewport.setPrefSize(320, 180);

    FlowPane buttons = new FlowPane(12, 12);
    buttons.setAlignment(Pos.CENTER);
    buttons.getChildren().addAll(
        button("spin", () -> playLoop(TwAnimation.spin(square, 1200))),
        button("pulse", () -> playLoop(TwAnimation.pulse(square))),
        button("breathe", () -> playLoop(TwAnimation.breathe(square))),
        button("shake", () -> TwAnimation.shake(caption).play()),
        button("bounce", () -> TwAnimation.bounce(caption).play()),
        button("flash", () -> TwAnimation.flash(caption).play()),
        button("stop", this::stopRunning));

    VBox root = new VBox(16, viewport, buttons);
    root.setAlignment(Pos.CENTER);
    TwStyle.apply(root, "bg-gray-50", "p-8");

    Scene scene = new Scene(root, 460, 360);
    TwInstall.install(scene);
    stage.setTitle("Rotate and loop animations");
    stage.setScene(scene);
    stage.show();
  }

  private void playLoop(TwAnimation animation) {
    stopRunning();
    running = animation;
    animation.play();
  }

  private void stopRunning() {
    if (running != null) {
      running.stop();
      running = null;
    }
  }

  private static Button button(String text, Runnable action) {
    Button b = new Button(text);
    b.setOnAction(e -> action.run());
    return b;
  }

  public static void main(String[] args) {
    launch(args);
  }
}
