# TailwindFX Example Project

This is an example project demonstrating how to use TailwindFX with the Maven plugin for build-time CSS generation.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- TailwindFX installed locally (`mvn install` in the main project)

## Building the Example

```bash
cd tailwindfx-example
mvn clean compile
```

This will:
1. Scan your Java and FXML files for Tailwind classes
2. Generate an optimized CSS file at `target/classes/css/tailwindfx-generated.css`
3. Compile your application

## Running the Example

```bash
mvn javafx:run
```

## Project Structure

```
tailwindfx-example/
├── pom.xml                          # Maven configuration with TailwindFX plugin
├── src/main/java/com/example/
│   ├── HelloApplication.java        # Main application class
│   └── HelloController.java         # FXML controller
└── src/main/resources/com/example/
    └── hello-view.fxml              # FXML view with Tailwind classes
```

## Using Tailwind Classes

In Java code:
```java
button.getStyleClass().addAll("bg-blue-500", "text-white", "p-4");
```

In FXML:
```xml
<Button text="Click Me" styleClass="bg-blue-500, text-white, p-4"/>
```

## Plugin Configuration

The Maven plugin is configured in `pom.xml`:

```xml
<plugin>
    <groupId>io.github.yasmramos.tailwindfx</groupId>
    <artifactId>tailwindfx-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <includeBase>true</includeBase>
                <includeColors>true</includeColors>
                <minify>false</minify>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `includeBase` | `true` | Include CSS variables and reset |
| `includeColors` | `true` | Include color palette |
| `minify` | `false` | Minify generated CSS |
| `outputDirectory` | `${project.build.outputDirectory}/css` | Output directory |
| `outputFileName` | `tailwindfx-generated.css` | Output file name |

## Generated CSS

After running `mvn compile`, check the generated CSS at:
```
target/classes/css/tailwindfx-generated.css
```

This file contains only the Tailwind classes used in your project, resulting in a smaller bundle size.

## Next Steps

1. Add more Tailwind classes to your FXML/Java files
2. Re-run `mvn compile` to regenerate CSS
3. Run with `mvn javafx:run` to see the changes

## Troubleshooting

**Plugin not found:** Make sure you've installed TailwindFX locally first:
```bash
cd ..
mvn clean install
```

**Classes not detected:** Ensure you're using the correct syntax:
- Java: `getStyleClass().addAll("class1", "class2")` or string literals
- FXML: `styleClass="class1, class2"`

**CSS not applied:** Check that `TailwindFX.install()` is called in your Application class.
