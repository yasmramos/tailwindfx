package com.example;

import io.github.yasmramos.tailwindfx.TwFXML;
import io.github.yasmramos.tailwindfx.TwInstall;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        // Install TailwindFX base styles and the generated CSS on the scene
        TwInstall.install(scene);
        TwInstall.installGenerated(scene, "/css/tailwindfx-generated.css");

        // Compile FXML-declared tokens (JIT values and variants) at runtime
        TwFXML.process(root);
        
        stage.setTitle("TailwindFX Example");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
