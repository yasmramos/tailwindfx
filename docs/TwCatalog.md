# TwCatalog Documentation

The `TwCatalog` class generates and enumerates all supported TailwindFX utility classes. It provides methods to generate the complete catalog of utility class names by expanding each utility family with its concrete values from the theme configuration.

## Overview

`TwCatalog` is responsible for generating the full set of utility classes available in TailwindFX. It expands utility families (colors, spacing, typography, etc.) with concrete values from `ThemeConfig`, producing a comprehensive catalog that can be used for validation, autocomplete, or CSS generation.

## Key Features

- **Complete Utility Enumeration**: Generates all possible utility class combinations
- **Theme-aware Generation**: Respects custom theme configurations
- **CSS Generation**: Produces full CSS output at runtime
- **Sorted Output**: Returns utilities in a deterministic `TreeSet` order
- **JIT Integration**: Works alongside `JitCompiler` for dynamic values

## Available Utilities

The catalog includes the following utility families:

### Colors
- Background: `bg-{color}-{shade}`, `bg-white`, `bg-black`, `bg-transparent`
- Text: `text-{color}-{shade}`, `text-white`, `text-black`, `text-transparent`
- Border: `border-{color}-{shade}`, `border-white`, `border-black`
- Ring: `ring-{color}-{shade}`, `ring-white`, `ring-black`
- 29 color families: slate, gray, red, orange, amber, yellow, lime, green, emerald, teal, cyan, sky, blue, indigo, violet, purple, fuchsia, pink, rose, zinc, neutral, stone, mauve, olive, mist, taupe
- Shades: 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950

### Spacing
- Padding: `p-{n}`, `px`, `py`, `pt`, `pr`, `pb`, `pl-{n}`
- Margin: `m-{n}`, `mx`, `my`, `mt`, `mr`, `mb`, `ml-{n}`
- Gap: `gap-{n}`, `gap-x`, `gap-y-{n}`
- Space: `space-x`, `space-y-{n}`
- Width/Height: `w-{n}`, `h-{n}`, `min-w-{n}`, `max-w-{n}`, `min-h-{n}`, `max-h-{n}`
- Inset: `inset-{n}`, `top`, `right`, `bottom`, `left-{n}`
- Special values: `full`, `auto`, `screen`, fractions (`1/2`, `1/3`, `1/4`, etc.)

### Typography
- Font size: `text-xs`, `text-sm`, `text-base`, `text-lg`, `text-xl`, `text-2xl` through `text-9xl`
- Font weight: `font-thin`, `font-extralight`, `font-light`, `font-normal`, `font-medium`, `font-semibold`, `font-bold`, `font-extrabold`, `font-black`
- Alignment: `text-left`, `text-center`, `text-right`, `text-justify`
- Decoration: `underline`, `line-through`, `no-underline`
- Transform: `uppercase`, `lowercase`, `capitalize`, `normal-case`

### Borders & Radius
- Border radius: `rounded-none`, `rounded-sm`, `rounded`, `rounded-md`, `rounded-lg`, `rounded-xl`, `rounded-2xl`, `rounded-3xl`, `rounded-full`
- Border width: `border`, `border-0`, `border-2`, `border-4`, `border-8`, `border-x`, `border-y`, `border-t`, `border-r`, `border-b`, `border-l`

### Effects & Filters
- Shadows: `shadow`, `shadow-sm`, `shadow-md`, `shadow-lg`, `shadow-xl`, `shadow-2xl`, `shadow-inner`, `shadow-none`
- Blur: `blur`, `blur-sm`, `blur-md`, `blur-lg`, `blur-xl`, `blur-2xl`, `blur-3xl`, `blur-none`
- Brightness: `brightness-0` through `brightness-200`
- Contrast: `contrast-0` through `contrast-200`
- Grayscale: `grayscale`, `grayscale-0`, `grayscale-50`, `grayscale-100`
- Invert: `invert`, `invert-0`, `invert-50`, `invert-100`
- Sepia: `sepia`, `sepia-0`, `sepia-50`, `sepia-100`
- Saturate: `saturate-0` through `saturate-200`
- Hue Rotate: `hue-rotate-0` through `hue-rotate-90`
- Drop Shadow: `drop-shadow`, `drop-shadow-sm`, `drop-shadow-md`, `drop-shadow-lg`, `drop-shadow-xl`, `drop-shadow-2xl`, `drop-shadow-none`

### Transform
- Scale: `scale-0` through `scale-100`, `scale-x-*`, `scale-y-*`
- Rotate: `rotate-0`, `rotate-45`, `rotate-90`, `rotate-180`, `-rotate-45`, `-rotate-90`
- Translate: `translate-x-*`, `translate-y-*`
- Skew: `skew-x-*`, `skew-y-*`
- Opacity: `opacity-0` through `opacity-100`
- Z-index: `z-0` through `z-50`, `z-auto`

### Layout & Display
- Display: `flex`, `inline-flex`, `grid`, `block`, `inline-block`, `hidden`, `inline`, `table`, `table-row`, `table-cell`
- Flex: `flex-row`, `flex-col`, `flex-wrap`, `flex-nowrap`, `flex-1`, `flex-auto`, `flex-none`
- Grid: `grid-cols-*`, `grid-rows-*`, `col-span-*`, `row-span-*`
- Visibility: `visible`, `invisible`
- Overflow: `overflow-auto`, `overflow-hidden`, `overflow-scroll`, `overflow-x-*`, `overflow-y-*`

## Basic Usage

### Get All Utility Classes

```java
// Get all utility classes with default theme
Set<String> allClasses = TwCatalog.allUtilityClasses();

// Iterate through utilities
for (String utility : allClasses) {
    System.out.println(utility);
}
```

### Custom Theme Configuration

```java
// Get all utility classes with custom theme
ThemeConfig config = ThemeConfig.defaultConfig();
// Customize config as needed
Set<String> customizedClasses = TwCatalog.allUtilityClasses(config);
```

### Generate Full CSS

```java
// Generate full CSS with default theme
String fullCss = TwCatalog.generateFullCss();

// Generate full CSS with custom theme
ThemeConfig config = ThemeConfig.defaultConfig();
String customizedCss = TwCatalog.generateFullCss(config);

// Write to file
Files.writeString(Paths.get("tailwindfx-all-utilities.css"), fullCss);
```

## Advanced Usage

### Filter Utilities by Category

```java
Set<String> allClasses = TwCatalog.allUtilityClasses();

// Filter color utilities
Set<String> colorUtilities = allClasses.stream()
    .filter(c -> c.startsWith("bg-") || c.startsWith("text-"))
    .collect(Collectors.toSet());

// Filter spacing utilities
Set<String> spacingUtilities = allClasses.stream()
    .filter(c -> c.startsWith("p-") || c.startsWith("m-"))
    .collect(Collectors.toSet());
```

### Validate Utility Classes

```java
Set<String> validUtilities = TwCatalog.allUtilityClasses();

boolean isValid = validUtilities.contains("bg-blue-500"); // true
boolean isInvalid = validUtilities.contains("bg-invalid"); // false
```

### Check Utility Existence

```java
public boolean hasUtility(String utility) {
    return TwCatalog.allUtilityClasses().contains(utility);
}
```

## Performance Considerations

- **Lazy Generation**: Utilities are generated on-demand, not at class load time
- **Caching**: Consider caching the result of `allUtilityClasses()` if called frequently
- **TreeSet Overhead**: The sorted `TreeSet` provides deterministic order but has O(log n) insertion cost
- **CSS Generation**: `generateFullCss()` is expensive; call sparingly and cache results

## Integration Points

### With TwInstall

```java
// Generate and install CSS
String css = TwCatalog.generateFullCss();
// Write CSS to resource path
TwInstall.installGenerated(scene, "/css/tailwindfx-generated.css");
```

### With JitCompiler

```java
// Use catalog for known utilities, JIT for arbitrary values
if (TwCatalog.allUtilityClasses().contains(token)) {
    TwStyle.apply(node, token);
} else {
    TwStyle.jit(node, token);
}
```

### With Maven Plugin

The Maven plugin uses `TwCatalog` internally to generate the build-time CSS:

```xml
<plugin>
    <groupId>io.github.yasmramos</groupId>
    <artifactId>tailwindfx-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Best Practices

1. **Cache Results**: Store the result of `allUtilityClasses()` if used repeatedly
2. **Generate Once**: Call `generateFullCss()` once at build time or application startup
3. **Use Default Theme**: Unless customization is required, use the default theme for consistency
4. **Validate Input**: Check user-provided utility classes against the catalog
5. **Combine with JIT**: Use catalog for standard utilities, JIT for arbitrary values

## Example: Complete Setup

```java
public class TailwindFXApp extends Application {
    
    private static final Set<String> UTILITIES = TwCatalog.allUtilityClasses();
    private static final String GENERATED_CSS = TwCatalog.generateFullCss();
    
    @Override
    public void start(Stage stage) throws Exception {
        // Write generated CSS to file
        Path cssPath = Paths.get("target/classes/css/tailwindfx-generated.css");
        Files.createDirectories(cssPath.getParent());
        Files.writeString(cssPath, GENERATED_CSS);
        
        Scene scene = new Scene(root, 800, 600);
        TwInstall.installGenerated(scene);
        
        stage.setScene(scene);
        stage.show();
    }
    
    public static boolean isValidUtility(String utility) {
        return UTILITIES.contains(utility);
    }
}
```

## See Also

- `TwInstall` - Install generated CSS in scenes
- `TwStyle` - Apply utility classes to nodes
- `ThemeConfig` - Theme configuration
- `JitCompiler` - Just-In-Time compilation for arbitrary values
- TailwindFX Maven Plugin - Build-time CSS generation
