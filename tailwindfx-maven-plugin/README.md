# TailwindFX Maven Plugin

Maven plugin for generating optimized TailwindCSS for JavaFX applications at build time.

## Features

- **Tree-shaking**: Scans your source code and generates only the CSS utilities you actually use
- **Customizable**: Configure which features to include (base styles, colors, utilities)
- **Minification**: Optional CSS minification for production builds
- **Custom Themes**: Support for custom ThemeConfig implementations

## Installation

Add the plugin to your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.yasmramos.tailwindfx</groupId>
            <artifactId>tailwindfx-maven-plugin</artifactId>
            <version>2.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <phase>process-resources</phase>
                    <configuration>
                        <!-- Optional configuration -->
                        <outputFileName>tailwindfx-optimized.css</outputFileName>
                        <includeBase>true</includeBase>
                        <includeColors>true</includeColors>
                        <minify>false</minify>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Configuration

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `sourceDirectory` | File | `${project.build.sourceDirectory}` | Directory containing source files to scan |
| `outputDirectory` | File | `${project.build.outputDirectory}/css` | Output directory for generated CSS |
| `outputFileName` | String | `tailwindfx-generated.css` | Name of the generated CSS file |
| `includeBase` | boolean | `true` | Include base styles (CSS variables, reset) |
| `includeColors` | boolean | `true` | Include color palette |
| `minify` | boolean | `false` | Minify the generated CSS |
| `themeConfigClass` | String | - | Fully qualified name of custom ThemeConfig class |

## Usage

### Basic Usage

Run the plugin:

```bash
mvn tailwindfx:generate
```

Or as part of the build lifecycle:

```bash
mvn clean package
```

### Custom Theme Configuration

Create a custom ThemeConfig class:

```java
package com.example;

import io.github.yasmramos.tailwindfx.config.ThemeConfig;

public class MyThemeConfig extends ThemeConfig {
    @Override
    protected void configure() {
        // Customize colors, spacing, etc.
        addColor("brand", "blue", new String[]{
            "#EFF6FF", "#DBEAFE", "#BFDBFE", "#93C5FD", 
            "#60A5FA", "#3B82F6", "#2563EB", "#1D4ED8", 
            "#1E40AF", "#1E3A8A", "#172554"
        });
    }
}
```

Configure in pom.xml:

```xml
<configuration>
    <themeConfigClass>com.example.MyThemeConfig</themeConfigClass>
</configuration>
```

## How It Works

1. **Scanning**: The plugin recursively scans `.java` and `.fxml` files in your source directory
2. **Extraction**: Extracts Tailwind class names from strings and annotations
3. **Generation**: Uses TailwindFX's JIT compiler to generate only the necessary CSS
4. **Output**: Writes optimized CSS to the specified output directory

## Benefits

- **Reduced Bundle Size**: Only includes CSS you actually use
- **Faster Load Times**: Smaller CSS files load faster
- **Production Ready**: Minification support for production builds
- **Maintainable**: Automatic updates when you add/remove Tailwind classes

## Requirements

- Maven 3.6+
- Java 17+
- TailwindFX 2.0.0+

## License

MIT License
