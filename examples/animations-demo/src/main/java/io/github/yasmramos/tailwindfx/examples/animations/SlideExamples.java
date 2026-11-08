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
import javafx.stage.Stage;

/**
 * Slide animations: directional entrances that move a node into place while fading in.
 *
 * <p>Available directions are {@code slideUp}, {@code slideDown}, {@code slideLeft}, {@code
 * slideRight} (relative moves from the current position) and the {@code slideIn*} variants that
 * travel a fixed distance. Each button replays one of them.
 */
public class SlideExamples extends Application {

  @Override
  public void start(Stage stage) {
    Label card = new Label("Slide me");
    TwStyle.apply(card, "bg-emerald-500", "text-white", "text-lg", "px-8", "py-6", "rounded-lg");
    StackPane viewport = new StackPane(card);
    viewport.setPrefSize(420, 180);

    FlowPane buttons = new FlowPane(12, 12);
    buttons.setAlignment(Pos.CENTER);
    buttons.getChildren().addAll(
        button("slideUp", () -> TwAnimation.slideUp(card, TwAnimation.SLOW).play()),
        button("slideDown", () -> TwAnimation.slideDown(card, TwAnimation.SLOW).play()),
        button("slideLeft", () -> TwAnimation.slideLeft(card).play()),
        button("slideRight", () -> TwAnimation.slideRight(card, TwAnimation.SLOW).play()),
        button("slideInLeft", () -> TwAnimation.slideInLeft(card, TwAnimation.SLOW).play()),
        button("slideInRight", () -> TwAnimation.slideInRight(card, TwAnimation.SLOW).play()));

    VBox root = new VBox(16, viewport, buttons);
    root.setAlignment(Pos.CENTER);
    TwStyle.apply(root, "bg-gray-50", "p-8");

    Scene scene = new Scene(root, 480, 340);
    TwInstall.install(scene);
    stage.setTitle("Slide animations");
    stage.setScene(scene);
    stage.show();

    TwAnimation.slideUp(card, TwAnimation.SLOW).play();
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
