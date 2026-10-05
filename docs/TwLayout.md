# TwLayout Documentation

The `TwLayout` class provides layout utilities and container management for JavaFX applications using TailwindCSS-style utility classes.

## Overview

`TwLayout` bridges TailwindCSS layout concepts with JavaFX layout panes, providing both utility class application and programmatic layout builders for flexbox, grid, and other layout patterns.

## Key Features

- **Utility Class Application**: Apply layout tokens like `flex`, `grid`, `gap-4`
- **Flexbox Builders**: `TwFlexPane` for flex layouts
- **Grid Builders**: `TwGridPane` for grid layouts
- **Container Management**: Aspect ratio, growth, margins
- **Layout Helpers**: Spacers, anchors, alignment utilities

## Basic Usage

### Applying Layout Tokens

```java
// Flexbox container
TwLayout.apply(container, "flex", "justify-center", "items-center");

// Grid container
TwLayout.apply(grid, "grid", "grid-cols-3", "gap-4");

// Spacing utilities
TwLayout.apply(node, "p-4", "m-2", "gap-2");
```

### Flexbox Layout

```java
// Create horizontal flex container
TwFlexPane flexRow = TwLayout.flexRow();
flexRow.applyClass("gap-4");
flexRow.getChildren().addAll(node1, node2, node3);

// Create vertical flex container
TwFlexPane flexCol = TwLayout.flexCol();
flexCol.applyClass("gap-2", "items-center");
flexCol.getChildren().addAll(header, content, footer);
```

### Grid Layout

```java
// Build grid with 3 columns
TwGridPane grid = TwLayout.grid()
    .cols(3)
    .gap("4")
    .build();

// Add children with spanning
grid.add(node1, 0, 0);
grid.add(node2, 1, 0, 2, 1); // Span 2 columns
```

## Advanced Usage

### Builder Pattern

```java
// Complex flex layout
TwLayout.Builder builder = TwLayout.of(container);
builder.flex()
       .row()
       .justifyCenter()
       .itemsCenter()
       .gap(4)
       .apply();

// Complex grid layout
TwLayout.grid()
    .cols(12)
    .rows("auto")
    .gapX("4")
    .gapY("2")
    .build();
```

### Aspect Ratio

```java
// Set 16:9 aspect ratio
TwLayout.aspectRatio(videoNode, 16, 9);

// Set 1:1 (square)
TwLayout.aspectRatio(imageNode, 1, 1);

// Set 4:3
TwLayout.aspectRatio(photoNode, 4, 3);
```

### Growth and Margins

```java
// Make node grow horizontally
TwLayout.hgrow(expandableNode);

// Make node grow vertically
TwLayout.vgrow(scrollableContent);

// Make node grow in both directions
TwLayout.grow(fillerNode);

// Set uniform margin
TwLayout.margin(node, 16);

// Set custom margins (top, right, bottom, left)
TwLayout.margin(node, 8, 16, 8, 16);
```

### Spacers and Anchors

```java
// Add spacer in flex container
Region spacer = TwLayout.spacer();
hbox.getChildren().addAll(node1, spacer, node2);

// Add sized spacer
Region bigSpacer = TwLayout.spacer(32);

// Fill anchor pane cell
TwLayout.anchorFill(centeredNode);
```

## Layout Tokens Reference

### Display
- `flex` - Flexbox container
- `inline-flex` - Inline flexbox
- `grid` - Grid container
- `block` - Block element
- `hidden` - Hidden element

### Flex Direction
- `flex-row` - Horizontal (default)
- `flex-col` - Vertical
- `flex-row-reverse` - Horizontal reversed
- `flex-col-reverse` - Vertical reversed

### Justify Content
- `justify-start` - Start aligned
- `justify-center` - Centered
- `justify-end` - End aligned
- `justify-between` - Space between
- `justify-around` - Space around
- `justify-evenly` - Evenly spaced

### Align Items
- `items-start` - Top/left aligned
- `items-center` - Centered
- `items-end` - Bottom/right aligned
- `items-stretch` - Stretched
- `items-baseline` - Baseline aligned

### Gap/Spacing
- `gap-0` through `gap-96` - Gap sizes
- `gap-x-*` - Horizontal gap
- `gap-y-*` - Vertical gap

### Grid
- `grid-cols-1` through `grid-cols-12` - Column count
- `grid-rows-1` through `grid-rows-6` - Row count
- `col-span-1` through `col-span-12` - Column span
- `row-span-1` through `row-span-6` - Row span

## Common Patterns

### Centered Content

```java
// Center content both horizontally and vertically
VBox centerLayout = new VBox();
TwLayout.apply(centerLayout, "flex", "justify-center", "items-center");
centerLayout.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
```

### Holy Grail Layout

```java
BorderPane holyGrail = new BorderPane();

// Header
TwLayout.apply(header, "flex", "justify-between", "items-center", "p-4");

// Sidebar
TwLayout.apply(sidebar, "w-64", "p-4");

// Main content
TwLayout.apply(main, "flex-1", "p-4", "overflow-auto");

// Footer
TwLayout.apply(footer, "flex", "justify-center", "p-4");
```

### Card Grid

```java
TwGridPane cardGrid = TwLayout.grid()
    .cols(3)
    .gap("6")
    .build();

for (Card card : cards) {
    int col = index % 3;
    int row = index / 3;
    cardGrid.add(card, col, row);
}
```

### Responsive Container

```java
// Container that adapts to screen size
StackPane responsiveContainer = new StackPane();
TwLayout.apply(responsiveContainer, "w-full", "max-w-7xl", "mx-auto", "px-4");
```

## Performance Tips

1. **Use Appropriate Containers**: Choose the simplest layout pane that meets your needs
2. **Avoid Deep Nesting**: Flatten layout hierarchies when possible
3. **Cache Builders**: Reuse layout builders for similar structures
4. **Lazy Loading**: Add children to containers as needed

## Best Practices

1. **Consistent Spacing**: Use gap utilities instead of individual margins
2. **Semantic Classes**: Name layout containers for clarity
3. **Responsive Design**: Consider different screen sizes in layout choices
4. **Accessibility**: Ensure proper focus order in flex/grid layouts

## See Also

- `TwStyle` - Apply general utility classes
- `TwFlexPane` - Flexbox container implementation
- `TwGridPane` - Grid container implementation
- JavaFX `HBox`, `VBox`, `GridPane`, `FlowPane` - Standard layout panes
