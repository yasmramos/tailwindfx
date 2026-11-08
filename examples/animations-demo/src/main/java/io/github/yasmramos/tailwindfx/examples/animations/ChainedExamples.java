package io.github.yasmramos.tailwindfx.examples.animations;

import io.github.yasmramos.tailwindfx.TwInstall;
import io.github.yasmramos.tailwindfx.TwStyle;
import io.github.yasmramos.tailwindfx.animation.TwAnimation;
import javafx.animation.Interpolator;
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
 * Chained/parallel animations and custom duration/easing options.
 *
 * <p>Shows how to compose animations with {@link TwAnimation#chain} and {@link
 * TwAnimation#parallel}, insert delays with {@link TwAnimation#pause}, and tune playback with
 * explicit durations, {@link TwAnimation#withEase}, {@link TwAnimation#speed} and {@link
 * TwAnimation#autoReverse}.
 */
public class ChainedExamples extends Application {

  @Override
  public void start(Stage stage) {
    Label card = new Label("Compose me");
    TwStyle.apply(card, "bg-rose-500", "text-white", "text-lg", "px-8", "py-6", "rounded-lg");
    Label badge = new Label("+1");
    TwStyle.apply(badge, "bg-white", "text-rose-500", "text-sm", "px-2", "py-1", "rounded-full");
    VBox nodes = new VBox(8, card, badge);
    nodes.setAlignment(Pos.CENTER);
    StackPane viewport = new StackPane(nodes);
    viewport.setPrefSize(420, 200);

    FlowPane buttons = new FlowPane(12, 12);
    buttons.setAlignment(Pos.CENTER);
    buttons.getChildren().addAll(
        button("chain", () -> playChain(card)),
        button("parallel", () -> playParallel(card, badge)),
        button("custom easing", () -> playCustomEasing(card)),
        button("half speed", () -> TwAnimation.fadeIn(card, 600).speed(0.5).play()),
        button("ping-pong", () -> playPingPong(card)));

    VBox root = new VBox(16, viewport, buttons);
    root.setAlignment(Pos.CENTER);
    TwStyle.apply(root, "bg-gray-50", "p-8");

    Scene scene = new Scene(root, 480, 360);
    TwInstall.install(scene);
    stage.setTitle("Chained animations and easing");
    stage.setScene(scene);
    stage.show();
  }

  /** Sequence: pop in, hold, then fade out, restoring the node so it can be replayed. */
  private static void playChain(Label card) {
    TwAnimation.chain(
            TwAnimation.scaleIn(card, TwAnimation.NORMAL),
            TwAnimation.pause(400),
            TwAnimation.fadeOut(card, TwAnimation.SLOW))
        .onFinished(
            e -> {
              card.setOpacity(1);
              card.setScaleX(1);
              card.setScaleY(1);
            })
        .play();
  }

  /** Two independent nodes animated at the same time with {@code parallel}. */
  private static void playParallel(Label card, Label badge) {
    card.setOpacity(0);
    badge.setOpacity(0);
    TwAnimation.parallel(
            TwAnimation.fadeIn(card, TwAnimation.SLOW), TwAnimation.slideUp(badge, TwAnimation.SLOW))
        .play();
  }

  /** Explicit duration + custom interpolator via the three-argument factory. */
  private static void playCustomEasing(Label card) {
    card.setOpacity(0);
    TwAnimation.fadeIn(card, 700, Interpolator.EASE_IN).play();
  }

  /** Plays forward then backward using {@code autoReverse} and a limited cycle count. */
  private static void playPingPong(Label card) {
    TwAnimation.scaleIn(card, TwAnimation.NORMAL).autoReverse().cycleCount(2).play();
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
