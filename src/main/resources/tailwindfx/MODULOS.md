# TailwindFX - Módulos CSS

TailwindFX proporciona un sistema de estilos modular para JavaFX inspirado en Tailwind CSS.

## Instalación

### Opción 1: Archivo Combinado (Recomendado)

Carga todos los estilos con un solo método:

```java
import tailwindfx.TailwindFX;

public class MyApp extends Application {
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(root, 800, 600);
        
        // Instala todos los estilos de TailwindFX
        TailwindFX.install(scene);
        
        stage.setScene(scene);
        stage.show();
    }
}
```

### Opción 2: Módulos Esenciales

Carga solo lo necesario para la mayoría de aplicaciones:

```java
// Requerido: Variables y reset
TailwindFX.installBase(scene);

// Recomendado: Controles + componentes preset
TailwindFX.installComponents(scene);
TailwindFX.installComponentsPreset(scene);
```

### Opción 3: Todos los Módulos

```java
// Instala todo (equivalente a TailwindFX.install(scene))
TailwindFX.installAll(scene);
```

### Opción 4: Selectiva

Carga solo los módulos que necesitas:

```java
TailwindFX.installBase(scene);              // Requerido
TailwindFX.installComponents(scene);        // Controles JavaFX
TailwindFX.installUtilities(scene);         // Layout, spacing
TailwindFX.installColors(scene);            // Colores y tipografía
TailwindFX.installEffects(scene);           // Sombras, transforms
TailwindFX.installComponentsPreset(scene);  // Cards, badges, buttons
TailwindFX.installDark(scene);              // Modo oscuro (opcional)
```

### Métodos de Instalación Disponibles

| Método | Descripción |
|--------|-------------|
| `TailwindFX.install(scene)` | Todos los módulos (archivo combinado) |
| `TailwindFX.installBase(scene)` | Variables y reset (**requerido**) |
| `TailwindFX.installComponents(scene)` | Controles JavaFX |
| `TailwindFX.installUtilities(scene)` | Layout, spacing, sizing |
| `TailwindFX.installColors(scene)` | Colores y tipografía |
| `TailwindFX.installEffects(scene)` | Sombras, transforms, filtros |
| `TailwindFX.installComponentsPreset(scene)` | Componentes predefinidos |
| `TailwindFX.installDark(scene)` | Modo oscuro |
| `TailwindFX.installEssentials(scene)` | Base + Components + ComponentsPreset |
| `TailwindFX.installAll(scene)` | Todos los módulos individuales |

## Módulos Disponibles

| Módulo | Tamaño | Descripción |
|--------|--------|-------------|
| `tailwindfx.css` | ~300KB | **Archivo combinado** - todos los módulos |
| `tailwindfx-base.css` | ~40KB | Variables CSS y reset (**requerido**) |
| `tailwindfx-components.css` | ~35KB | Controles JavaFX estilizados |
| `tailwindfx-utilities.css` | ~25KB | Layout, spacing, sizing, visibilidad |
| `tailwindfx-colors.css` | ~30KB | Colores (bg, text, border) y tipografía |
| `tailwindfx-effects.css` | ~20KB | Sombras, transforms, filtros |
| `tailwindfx-components-preset.css` | ~25KB | Componentes: cards, badges, buttons |
| `tailwindfx-dark.css` | ~30KB | Modo oscuro |

### tailwindfx-base.css
**Tamaño:** ~40KB  
**Descripción:** Variables CSS y reset base. **Requerido por todos los demás módulos.**

Contiene:
- Paleta completa de colores (Slate, Gray, Red, Orange, Amber, Yellow, Lime, Green, Emerald, Teal, Cyan, Sky, Blue, Indigo, Violet, Purple, Fuchsia, Pink, Rose)
- Variables de espaciado (-sp-0 a -sp-32)
- Variables de radio de borde (-radius-*)
- Variables de tamaño de fuente (-font-size-*)
- Variables de peso de fuente (-font-weight-*)
- Variables de sombras (-shadow-*)
- Variables de opacidad (-opacity-*)
- Variables de cursor (-cursor-*)
- Sistema de theming Modena (-fx-base, -fx-accent, etc.)

### tailwindfx-components.css
**Tamaño:** ~35KB  
**Descripción:** Estilos automáticos para controles JavaFX.

Contiene estilos para:
- Button (primario, hover, pressed, disabled)
- TextField, PasswordField, TextArea
- ComboBox, ChoiceBox, DatePicker, ColorPicker
- CheckBox, RadioButton, ToggleButton
- Slider, ProgressBar, ProgressIndicator
- ListView, TableView, TreeView
- TabPane, MenuBar, ContextMenu, Tooltip
- Separator, ToolBar, SplitPane
- TitledPane, Accordion, Pagination
- ScrollBar, ScrollPane

### tailwindfx-utilities.css
**Tamaño:** ~25KB  
**Descripción:** Clases utilitarias para layout y spacing.

Contiene:
- Visibilidad: `.hidden`, `.visible`, `.opacity-*`
- Padding: `.p-4`, `.px-4`, `.py-4`, `.pt-4`, etc.
- Gap: `.gap-4`, `.gap-x-4`, `.gap-y-4`
- Sizing: `.w-full`, `.h-screen`, `.min-w-0`, `.max-w-md`
- Z-index: `.z-0`, `.z-10`, `.z-50`
- Overflow: `.overflow-hidden`, `.overflow-scroll`
- Cursor: `.cursor-pointer`, `.cursor-text`

### tailwindfx-colors.css
**Tamaño:** ~30KB  
**Descripción:** Clases de color y tipografía.

Contiene:
- Background: `.bg-gray-*`, `.bg-blue-*`, `.bg-red-*`, etc.
- Text: `.text-gray-*`, `.text-blue-*`, `.text-white`
- Border: `.border-*`, `.border-color-*`
- Border radius: `.rounded`, `.rounded-lg`, `.rounded-full`
- Font size: `.text-xs` a `.text-5xl`
- Font weight: `.font-bold`, `.font-medium`, etc.
- Text alignment: `.text-center`, `.text-left`, `.text-right`
- Text decoration: `.underline`, `.line-through`

### tailwindfx-effects.css
**Tamaño:** ~20KB  
**Descripción:** Efectos visuales y transformaciones.

Contiene:
- Shadows: `.shadow`, `.shadow-lg`, `.shadow-xl`
- Rotate: `.rotate-45`, `.rotate-90`, `.rotate-180`
- Scale: `.scale-95`, `.scale-105`, `.scale-110`
- Translate: `.translate-x-4`, `.translate-y-4`
- Blur: `.blur-sm`, `.blur`, `.blur-lg`
- Filters: `.brightness-*`, `.contrast-*`, `.grayscale`, `.invert`, `.sepia`

### tailwindfx-components-preset.css
**Tamaño:** ~25KB  
**Descripción:** Componentes UI predefinidos.

Contiene:
- Buttons: `.btn`, `.btn-primary`, `.btn-secondary`, `.btn-success`, `.btn-danger`
- Cards: `.card`, `.card-header`, `.card-body`, `.card-footer`
- Badges: `.badge`, `.badge-blue`, `.badge-green`, `.badge-red`
- Alerts: `.alert`, `.alert-success`, `.alert-error`, `.alert-warning`
- Inputs: `.input`, `.input-error`, `.input-success`
- Avatars: `.avatar`, `.avatar-sm`, `.avatar-lg`, `.avatar-group`
- Modals: `.modal`, `.modal-overlay`
- Toasts: `.toast`, `.toast-success`, `.toast-error`
- Glassmorphism: `.glass`, `.glass-dark`
- Neumorphism: `.neumorph`, `.neumorph-pressed`

### tailwindfx-dark.css
**Tamaño:** ~30KB  
**Descripción:** Variantes de modo oscuro.

Contiene:
- Selector `.dark` para activar modo oscuro
- Overrides de colores para todos los componentes
- Variables específicas para modo oscuro
- Soporte para componentes preset

## Uso de Clases CSS

Una vez instalados los estilos, puedes usar las clases CSS en tus componentes:

```java
// Botón primario
Button btn = new Button("Guardar");
btn.getStyleClass().add("btn-primary");

// Tarjeta
VBox card = new VBox();
card.getStyleClass().add("card");

// Badge
Label badge = new Label("Nuevo");
badge.getStyleClass().addAll("badge", "badge-blue");

// Input con error
TextField input = new TextField();
input.getStyleClass().add("input-error");

// Modo oscuro
root.getStyleClass().add("dark");
```

## Personalización

### Cambiar Colores del Tema

```java
// Cambiar color de acento global
scene.getRoot().setStyle("-fx-accent: #3b82f6;");

// Cambiar color base (afecta todos los controles)
scene.getRoot().setStyle("-fx-base: #1e293b;");
```

### Usar Temas Predefinidos

```java
import tailwindfx.TailwindFX;

// Tema oscuro
TailwindFX.theme(scene).dark().apply();

// Tema azul
TailwindFX.theme(scene).preset("blue").apply();

// Tema personalizado
TailwindFX.theme(scene)
    .base("#1e293b")
    .accent("#3b82f6")
    .apply();
```

## Estructura de Archivos

```
tailwindfx/
├── tailwindfx.css                    # Archivo combinado (todos los módulos)
├── tailwindfx-base.css               # Variables y reset (requerido)
├── tailwindfx-components.css         # Controles JavaFX
├── tailwindfx-utilities.css          # Layout, spacing, sizing
├── tailwindfx-colors.css             # Colores y tipografía
├── tailwindfx-effects.css            # Sombras, transforms, filtros
├── tailwindfx-components-preset.css  # Componentes predefinidos
├── tailwindfx-dark.css               # Modo oscuro
└── MODULOS.md                        # Esta documentación
```

## Rendimiento

| Configuración | Tamaño Total | Tiempo de Carga |
|--------------|-------------|-----------------|
| Solo base    | ~40KB       | ~50ms           |
| Base + Components | ~75KB  | ~80ms           |
| Todos módulos | ~200KB     | ~150ms          |

*Medido en Intel i7, SSD, JavaFX 21*

## Migración desde tailwindfx.css Único

Si ya usas `tailwindfx.css`, no necesitas cambiar nada. Los archivos modulares son opcionales.

**Configuración recomendada:**

```java
// Opción A: Todo incluido (más simple)
TailwindFX.install(scene);

// Opción B: Módulos esenciales
TailwindFX.installEssentials(scene);

// Opción C: Selectiva
TailwindFX.installBase(scene);
TailwindFX.installComponents(scene);
TailwindFX.installComponentsPreset(scene);
TailwindFX.installDark(scene); // opcional
```

## Soporte

- Documentación completa: [README.md](../README.md)
- Ejemplos: [TailwindFXExample.java](../TailwindFXExample.java)
- Issues: https://github.com/yasmramos/tailwindfx/issues
