# TailwindFX Examples

Example applications demonstrating TailwindFX capabilities.

## Prerequisites

Make sure you have TailwindFX library installed in your local Maven repository:

```bash
cd ../tailwindfx
mvn clean install
```

## Running Examples

### Dashboard App

```bash
# Using Maven
mvn exec:java

# Or specify main class explicitly
mvn exec:java -Dexec.mainClass=tailwindfx.examples.DashboardApp

# Using JavaFX Maven plugin
mvn javafx:run
```

## Examples Included

### DashboardApp

A complete dashboard showing various TailwindFX features:

- ✓ Layout builders (HBox, VBox, GridPane)
- ✓ Color utilities (`bg-*`, `text-*`)
- ✓ Spacing utilities (`p-*`, `m-*`, `gap-*`)
- ✓ Shadows and effects
- ✓ Components (cards, badges, buttons, avatars)
- ✓ Form elements (input, checkbox, choice)
- ✓ Dark mode support

## Project Structure

```
tailwindfx-examples/
├── pom.xml
├── README.md
└── src/main/java/tailwindfx/examples/
    ├── Dashboard.java       # Dashboard component
    ├── DashboardApp.java    # Main application
    └── README.md           # Examples documentation
```

## Creating Your Own Project

1. Copy this `pom.xml` as a template
2. Update the `tailwindfx.version` property
3. Add your example classes
4. Run with `mvn exec:java`

### Example pom.xml

```xml
<project>
    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <tailwindfx.version>1.0-SNAPSHOT</tailwindfx.version>
        <javafx.version>25.0.1</javafx.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>io.github.yasmramos</groupId>
            <artifactId>tailwindfx</artifactId>
            <version>${tailwindfx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
    </dependencies>
</project>
```

## More Information

- Library Documentation: [../tailwindfx/src/main/resources/tailwindfx/MODULOS.md](../tailwindfx/src/main/resources/tailwindfx/MODULOS.md)
- Library README: [../tailwindfx/README.md](../tailwindfx/README.md)
