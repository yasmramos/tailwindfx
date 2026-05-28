# Ejemplo de uso del plugin TailwindFX Maven

Este directorio contiene un ejemplo básico de cómo usar el plugin `tailwindfx-maven-plugin` en un proyecto JavaFX.

## Estructura del proyecto ejemplo

```
example-project/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── App.java
│   │   └── resources/
│   │       └── css/
│   │           └── tailwindfx-generated.css (generado por el plugin)
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── AppTest.java
```

## Configuración del pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>tailwindfx-example</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <javafx.version>17.0.2</javafx.version>
    </properties>

    <dependencies>
        <!-- TailwindFX -->
        <dependency>
            <groupId>io.github.yasmramos</groupId>
            <artifactId>tailwindfx</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <!-- JavaFX -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Plugin de TailwindFX -->
            <plugin>
                <groupId>io.github.yasmramos</groupId>
                <artifactId>tailwindfx-maven-plugin</artifactId>
                <version>2.0.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                        <phase>process-resources</phase>
                        <configuration>
                            <outputFileName>tailwindfx-optimized.css</outputFileName>
                            <includeBase>true</includeBase>
                            <includeColors>true</includeColors>
                            <minify>false</minify>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- JavaFX Maven Plugin -->
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>com.example.App</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Uso en código Java

```java
package com.example;

import io.github.yasmramos.tailwindfx.TailwindFX;
import io.github.yasmramos.tailwindfx.TwInstall;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Instalar TailwindFX con generación JIT
        TwInstall.installMinimal(stage.getScene());

        // Crear UI con clases Tailwind
        VBox root = new VBox(20);
        root.getStyleClass().add("p-8");
        root.getStyleClass().add("gap-4");

        Button button = new Button("Click Me");
        button.getStyleClass().addAll(
            "px-6",
            "py-3",
            "bg-blue-500",
            "text-white",
            "rounded-lg",
            "hover:bg-blue-600"
        );

        root.getChildren().add(button);

        Scene scene = new Scene(root, 400, 300);
        
        // Asegurar que TailwindFX esté instalado
        TailwindFX.install(scene);

        stage.setTitle("TailwindFX Example");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

## Ejecución

1. **Construir el proyecto:**
   ```bash
   mvn clean package
   ```

   Esto generará el archivo CSS optimizado en `target/classes/css/tailwindfx-optimized.css`

2. **Ejecutar la aplicación:**
   ```bash
   mvn javafx:run
   ```

3. **Ejecutar solo la generación de CSS:**
   ```bash
   mvn tailwindfx:generate
   ```

## Beneficios

- **CSS optimizado**: Solo se incluyen las clases utilizadas en tu código
- **Build automático**: El CSS se genera en cada compilación
- **Tamaño reducido**: Menor tamaño del bundle final
- **Mantenimiento automático**: Al agregar/quitar clases, el CSS se actualiza automáticamente

## Notas

- El plugin escanea archivos `.java` y `.fxml` en busca de clases Tailwind
- Las clases construidas dinámicamente pueden no ser detectadas
- Para producción, considera activar la opción `minify=true`
