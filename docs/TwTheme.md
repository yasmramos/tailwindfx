# TwTheme Documentation

The `TwTheme` class is the theme management facade for TailwindFX. It provides access to theme operations including dark/light mode, theme scoping, and preset management.

## Overview

`TwTheme` is a singleton facade that delegates to `ThemeManager` and `ThemeScopeManager` for theme-related operations. It enables switching between dark and light modes, applying theme presets to specific panes, and persisting theme preferences.

## Key Features

- **Dark/Light Mode**: Toggle between dark and light themes
- **Theme Scoping**: Apply different themes to specific panes or regions
- **Preset Support**: Use predefined theme presets (blue, green, red, etc.)
- **Persistence**: Save and load theme preferences
- **Scene-based Management**: Associate themes with specific scenes

## Basic Usage

### Dark/Light Mode

```java
// Enable dark mode
TwTheme.of(scene).dark().apply();

// Enable light mode
TwTheme.of(scene).light().apply();

// Toggle based on condition
if (isDarkMode) {
    TwTheme.of(scene).dark().apply();
} else {
    TwTheme.of(scene).light().apply();
}
```

### Theme Scoping

Apply themes to specific panes rather than the entire scene:

```java
// Apply blue preset to a specific pane
TwTheme.scope(pane).preset("blue").apply();

// Apply green preset
TwTheme.scope(sidebar).preset("green").apply();

// Chain with other options
TwTheme.scope(contentPane)
    .preset("purple")
    .variant("soft")
    .apply();
```

### Save/Load Themes

```java
// Save current theme
TwTheme.saveTheme(scene, "my-preferred-theme");

// Load saved theme
boolean loaded = TwTheme.loadTheme(scene, "my-preferred-theme");

// Delete saved theme
TwTheme.deleteTheme("old-theme");
```

## Advanced Usage

### Theme Manager Operations

Access the underlying `ThemeManager` for advanced operations:

```java
ThemeManager manager = TwTheme.of(scene);

// Check current theme state
boolean isDark = manager.isDarkMode();

// Apply theme changes
manager.setDarkMode(true);
manager.applyChanges();
```

### Theme Scope Operations

Access the underlying `ThemeScopeManager` for scoped theming:

```java
ThemeScopeManager.ScopeBuilder builder = TwTheme.scope(pane);

// Configure scope
builder.preset("blue")
       .variant("solid")
       .intensity(0.8)
       .apply();
```

### Reactive Theme Switching

```java
// Toggle theme with button click
Button toggleBtn = new Button("Toggle Theme");
toggleBtn.setOnAction(e -> {
    if (TwTheme.of(scene).isDarkMode()) {
        TwTheme.of(scene).light().apply();
    } else {
        TwTheme.of(scene).dark().apply();
    }
});

// Listen for theme changes
scene.propertyChangedProperty().addListener((obs, oldVal, newVal) -> {
    if ("theme".equals(newVal.getKey())) {
        System.out.println("Theme changed to: " + newVal.getValue());
    }
});
```

### Multiple Scene Support

```java
// Apply theme to multiple scenes
Scene mainScene = new Scene(mainRoot);
Scene dialogScene = new Scene(dialogRoot);

TwTheme.of(mainScene).dark().apply();
TwTheme.of(dialogScene).dark().apply();

// Or use a helper method
public void applyToAllScenes(Scene... scenes) {
    for (Scene scene : scenes) {
        TwTheme.of(scene).dark().apply();
    }
}
```

## Theme Presets

Available preset themes include:

- **blue** - Blue accent colors
- **green** - Green accent colors
- **red** - Red accent colors
- **purple** - Purple accent colors
- **orange** - Orange accent colors
- **slate** - Slate gray tones
- **zinc** - Zinc gray tones

Usage:
```java
TwTheme.scope(pane).preset("blue").apply();
```

## Best Practices

1. **Apply Early**: Set theme before showing the stage for best visual experience
2. **Consistent Scoping**: Use theme scoping consistently across similar panes
3. **Save Preferences**: Persist user theme preferences for subsequent sessions
4. **Test Both Modes**: Verify UI works correctly in both dark and light modes
5. **Avoid Frequent Toggles**: Minimize theme switching during runtime to prevent flicker

## Common Patterns

### Settings Dialog

```java
public class ThemeSettingsDialog extends Dialog<Void> {
    
    private final Scene ownerScene;
    private final ToggleGroup themeToggle = new ToggleGroup();
    
    public ThemeSettingsDialog(Scene ownerScene) {
        this.ownerScene = ownerScene;
        
        RadioButton darkRadio = new RadioButton("Dark");
        RadioButton lightRadio = new RadioButton("Light");
        
        darkRadio.setToggleGroup(themeToggle);
        lightRadio.setToggleGroup(themeToggle);
        
        // Set current selection
        if (TwTheme.of(ownerScene).isDarkMode()) {
            darkRadio.setSelected(true);
        } else {
            lightRadio.setSelected(true);
        }
        
        // Apply on selection change
        themeToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == darkRadio.getToggle()) {
                TwTheme.of(ownerScene).dark().apply();
            } else {
                TwTheme.of(ownerScene).light().apply();
            }
            TwTheme.saveTheme(ownerScene, 
                newVal == darkRadio.getToggle() ? "dark" : "light");
        });
    }
}
```

### Theme Provider Pattern

```java
public class ThemeProvider {
    
    private static final Map<Scene, Boolean> sceneThemes = new ConcurrentHashMap<>();
    
    public static void setDark(Scene scene, boolean dark) {
        sceneThemes.put(scene, dark);
        if (dark) {
            TwTheme.of(scene).dark().apply();
        } else {
            TwTheme.of(scene).light().apply();
        }
    }
    
    public static boolean isDark(Scene scene) {
        return sceneThemes.getOrDefault(scene, false);
    }
}
```

## Error Handling

```java
try {
    TwTheme.of(scene).dark().apply();
} catch (IllegalStateException e) {
    // Scene may not have required CSS installed
    System.err.println("Theme application failed: " + e.getMessage());
    TwInstall.install(scene); // Ensure CSS is installed
    TwTheme.of(scene).dark().apply(); // Retry
}
```

## See Also

- `TwInstall` - Install theme CSS support
- `TwConfig` - Global configuration
- `ThemeManager` - Core theme management
- `ThemeScopeManager` - Scoped theme management
- `ThemeCssGenerator` - Generate theme CSS variables
