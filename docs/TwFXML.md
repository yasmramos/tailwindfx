# TwFXML Documentation

The `TwFXML` class provides utilities for processing JavaFX scene graphs loaded from FXML files with Tailwind CSS classes, including support for automatic JIT compilation.

## Overview

`TwFXML` bridges the gap between FXML-based UI definition and TailwindCSS styling. It processes scene graphs after loading from FXML, applying Tailwind utility classes defined in the FXML and optionally enabling automatic JIT compilation for arbitrary values.

## Key Features

- **Scene Graph Processing**: Process entire FXML-loaded scene graphs
- **Auto JIT Support**: Enable/disable automatic JIT compilation for nodes
- **Class Application**: Apply Tailwind classes defined in FXML
- **Recursive Processing**: Handles nested parent-child structures

## Basic Usage

### Process FXML Scene Graph

```java
// Load FXML
Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));

// Process the scene graph to apply Tailwind classes
TwFXML.process(root);

// Create scene and install Tailwind
Scene scene = new Scene(root);
TwInstall.install(scene);
```

### Enable Auto JIT

```java
// Load and process FXML
Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));
TwFXML.process(root);

// Enable automatic JIT compilation for arbitrary values
TwFXML.enableAutoJit(root);

Scene scene = new Scene(root);
TwInstall.install(scene);
```

### Disable Auto JIT

```java
// Disable JIT compilation (use only predefined classes)
TwFXML.disableAutoJit(root);
```

## Advanced Usage

### FXML with Tailwind Classes

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.layout.VBox?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>

<VBox xmlns="http://javafx.com/javafx/17" 
      xmlns:fx="http://javafx.com/fxml/1"
      styleClass="p-6"
      fx:id="container">
    
    <Label text="Welcome" 
           styleClass="text-2xl,font-bold,text-blue-500"/>
    
    <Button text="Click Me"
            styleClass="bg-blue-500,text-white,px-4,py-2,rounded"
            fx:id="actionButton"/>
    
</VBox>
```

Then in your controller or application:

```java
@Override
public void start(Stage stage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));
    
    // Process Tailwind classes from FXML
    TwFXML.process(root);
    
    Scene scene = new Scene(root);
    TwInstall.install(scene);
    
    stage.setScene(scene);
    stage.show();
}
```

### Controller Integration

```java
public class MainController {
    
    @FXML
    private VBox container;
    
    @FXML
    private Button actionButton;
    
    @FXML
    private void initialize() {
        // Additional processing after FXML load
        TwFXML.process(container);
        
        // Or enable JIT for dynamic values
        TwFXML.enableAutoJit(container);
    }
}
```

### Custom Processing

```java
public class FXMLProcessor {
    
    public static Parent loadAndProcess(String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(
            FXMLProcessor.class.getResource(fxmlPath)
        );
        
        // Process Tailwind classes
        TwFXML.process(root);
        
        // Enable JIT if needed
        TwFXML.enableAutoJit(root);
        
        return root;
    }
    
    public static void applyTheme(Parent root, String theme) {
        // Apply theme-specific classes
        root.lookupAll("." + theme).forEach(node -> {
            if (node instanceof Region) {
                TwStyle.apply((Region) node, theme + "-styles");
            }
        });
    }
}
```

## Style Token Processing

`TwFXML` processes style tokens from FXML `styleClass` attributes:

### Comma-Separated Classes

```xml
<Button styleClass="bg-blue-500,text-white,px-4,py-2"/>
```

### Space-Separated Classes

```xml
<Button styleClass="bg-blue-500 text-white px-4 py-2"/>
```

### Mixed Approach

```xml
<VBox styleClass="flex,gap-4">
    <children>
        <Label styleClass="font-bold"/>
        <Button styleClass="rounded,shadow"/>
    </children>
</VBox>
```

## Common Patterns

### Form Layout

```xml
<VBox styleClass="vbox-gap-4,p-6,max-w-md">
    <Label text="Username" styleClass="font-semibold"/>
    <TextField styleClass="w-full,p-2,border,rounded"/>
    
    <Label text="Password" styleClass="font-semibold"/>
    <PasswordField styleClass="w-full,p-2,border,rounded"/>
    
    <Button text="Login" 
            styleClass="w-full,py-2,bg-blue-500,text-white,rounded"/>
</VBox>
```

### Card Component

```xml
<VBox styleClass="p-4,rounded-lg,shadow,bg-white">
    <Label text="Card Title" 
           styleClass="font-bold,text-lg,text-gray-900"/>
    
    <Label text="Card content goes here."
           styleClass="text-gray-600,mt-2"/>
    
    <HBox styleClass="hbox-gap-2,mt-4">
        <Button text="Action" styleClass="px-4,py-2,bg-blue-500,text-white"/>
        <Button text="Cancel" styleClass="px-4,py-2,bg-gray-200"/>
    </HBox>
</VBox>
```

### Navigation Bar

```xml
<HBox styleClass="hbox-gap-4,p-4,bg-gray-800">
    <Label text="Logo" styleClass="text-white,font-bold,text-xl"/>
    
    <Region HBox.hgrow="ALWAYS"/>
    
    <Button text="Home" styleClass="text-gray-300,hover:text-white"/>
    <Button text="About" styleClass="text-gray-300,hover:text-white"/>
    <Button text="Contact" styleClass="text-gray-300,hover:text-white"/>
</HBox>
```

## Best Practices

1. **Process After Load**: Always call `TwFXML.process()` after loading FXML
2. **Enable JIT Selectively**: Only enable JIT if you need arbitrary values
3. **Use Consistent Separators**: Stick to comma or space separation consistently
4. **Validate Classes**: Ensure classes exist in your catalog or enable JIT
5. **Install CSS**: Remember to call `TwInstall.install()` after processing

## Performance Considerations

- **Processing Overhead**: `process()` traverses the entire scene graph
- **JIT Compilation**: Auto JIT adds runtime compilation overhead
- **Cache Benefits**: Processed classes are cached for subsequent use
- **Large Graphs**: Consider processing only visible portions for large scenes

## Troubleshooting

### Classes Not Applied

Ensure you're calling `process()` after loading:

```java
Parent root = FXMLLoader.load(...);
TwFXML.process(root); // Must be after load
```

### JIT Not Working

Verify JIT is enabled:

```java
TwFXML.enableAutoJit(root);
```

### Performance Issues

For large scene graphs, consider selective processing:

```java
// Process only main container
TwFXML.process(mainContainer);

// Skip already processed sections
if (!node.hasProperty("tw-processed")) {
    TwFXML.process(node);
}
```

## See Also

- `TwStyle` - Apply utility classes programmatically
- `TwInstall` - Install TailwindCSS in scenes
- `JitCompiler` - Just-In-Time compilation
- JavaFX `FXMLLoader` - FXML loading mechanism
