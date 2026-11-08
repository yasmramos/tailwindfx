package io.github.yasmramos.tailwindfx.examples.animations;

import io.github.yasmramos.tailwindfx.TwInstall;
import io.github.yasmramos.tailwindfx.TwStyle;
import io.github.yasmramos.tailwindfx.animation.TwAnimation;
import java.util.function.Consumer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Main animation showcase.
 *
 * <p>Presents every TwAnimation category as a tile: each tile has its own swatch and a Play button
 * that runs the corresponding animation on that swatch, so the effects can be compared at a glance.
 * The left panel launches the focused example application for each group.
 *
 * <p>Run with:
 *
 * <pre>
 * mvn -pl examples/animations-demo -P examples javafx:run
 * </pre>
 */
public class AnimationsDemoApp extends Application {

  @Override
  public void start(Stage stage) {
    BorderPane root = new BorderPane();
    TwStyle.apply(root, "bg-gray-50");

    root.setTop(header());
    root.setLeft(exampleLauncher());
    root.setCenter(gallery());

    Scene scene = new Scene(root, 960, 640);
    TwInstall.install(scene);
    stage.setTitle("TailwindFX - Animation Examples");
    stage.setScene(scene);
    stage.show();

    TwAnimation.fadeIn(root, TwAnimation.SLOW).play();
  }

  private VBox header() {
    Label title = new Label("TailwindFX Animations");
    TwStyle.apply(title, "text-2xl", "font-bold", "text-gray-800");
    Label subtitle = new Label("Every animation below is powered by TwAnimation");
    TwStyle.apply(subtitle, "text-gray-500");
    VBox header = new VBox(4, title, subtitle);
    TwStyle.apply(header, "p-6", "bg-white");
    return header;
  }

  private VBox exampleLauncher() {
    VBox panel = new VBox(10);
    panel.setPrefWidth(240);
    TwStyle.apply(panel, "p-6", "bg-white");

    Label caption = new Label("Focused examples");
    TwStyle.apply(caption, "text-sm", "font-semibold", "text-gray-600");

    panel.getChildren().addAll(
        caption,
        launcher("Fade", FadeExamples.class),
        launcher("Slide", SlideExamples.class),
        launcher("Scale", ScaleExamples.class),
        launcher("Rotate / loop", RotateExamples.class),
        launcher("Chained / easing", ChainedExamples.class));
    return panel;
  }

  private Button launcher(String text, Class<? extends Application> type) {
    Button b = new Button(text);
    b.setMaxWidth(Double.MAX_VALUE);
    TwStyle.apply(b, "w-full", "text-left", "px-4", "py-2", "rounded-lg", "border");
    b.setOnAction(
        e -> {
          try {
            Application app = type.getDeclaredConstructor().newInstance();
            Stage newStage = new Stage();
            app.start(newStage);
            newStage.setTitle(text);
            newStage.show();
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        });
    return b;
  }

  private ScrollPane gallery() {
    FlowPane tiles = new FlowPane(20, 20);
    tiles.setAlignment(Pos.TOP_LEFT);
    TwStyle.apply(tiles, "p-6");

    tiles.getChildren().addAll(
        tile("fadeIn", "Entrance fade", sw -> TwAnimation.fadeIn(sw, TwAnimation.SLOW).play()),
        tile(
            "fadeOut",
            "Exit fade",
            sw ->
                TwAnimation.fadeOut(sw, TwAnimation.SLOW)
                    .onFinished(e -> sw.setOpacity(1))
                    .play()),
        tile("slideUp", "Slide entrance", sw -> TwAnimation.slideUp(sw, TwAnimation.SLOW).play()),
        tile("scaleIn", "Pop-in scale", sw -> TwAnimation.scaleIn(sw, TwAnimation.SLOW).play()),
        tile("spin", "Infinite rotation", sw -> TwAnimation.spin(sw, 1200).play()),
        tile("pulse", "Pulse", sw -> TwAnimation.pulse(sw).cycleCount(4).play()),
        tile("breathe", "Breathe", sw -> TwAnimation.breathe(sw).cycleCount(2).play()),
        tile("shake", "Shake (validation)", sw -> TwAnimation.shake(sw).play()),
        tile("bounce", "Bounce", sw -> TwAnimation.bounce(sw).play()),
        tile("flash", "Flash (toast)", sw -> TwAnimation.flash(sw).play()),
        tile("chain", "Chained + pause", AnimationsDemoApp::playChain));

    ScrollPane scroll = new ScrollPane(tiles);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent;");
    return scroll;
  }

  private VBox tile(String name, String description, Consumer<Node> action) {
    Rectangle swatch = new Rectangle(72, 48);
    swatch.setArcWidth(10);
    swatch.setArcHeight(10);
    swatch.setStyle("-fx-fill: #6366f1;");

    Label nameLabel = new Label(name);
    TwStyle.apply(nameLabel, "font-semibold", "text-gray-800");
    Label descLabel = new Label(description);
    TwStyle.apply(descLabel, "text-xs", "text-gray-500");

    StackPane viewport = new StackPane(swatch);
    viewport.setPrefSize(96, 72);

    Button play = new Button("Play");
    play.setOnAction(e -> action.accept(swatch));

    VBox tile = new VBox(8, viewport, nameLabel, descLabel, play);
    TwStyle.apply(tile, "p-4", "bg-white", "rounded-xl", "border");
    tile.setPrefWidth(180);
    return tile;
  }

  private static void playChain(Node swatch) {
    TwAnimation.chain(
            TwAnimation.scaleIn(swatch, TwAnimation.NORMAL),
            TwAnimation.pause(300),
            TwAnimation.fadeOut(swatch, TwAnimation.SLOW))
        .onFinished(
            e -> {
              swatch.setOpacity(1);
              swatch.setScaleX(1);
              swatch.setScaleY(1);
            })
        .play();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
