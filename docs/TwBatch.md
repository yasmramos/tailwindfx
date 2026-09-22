# TwBatch Documentation

The `TwBatch` class is a batch operations facade for performance optimization in TailwindFX. It enables efficient execution of multiple style operations by batching them together.

## Overview

`TwBatch` provides a simple API for grouping multiple style operations into a single batch, reducing overhead and improving performance when applying styles to many nodes or applying many styles to a single node.

## Key Features

- **Batch Execution**: Group multiple style operations efficiently
- **Async Support**: Execute batches asynchronously
- **Performance Optimization**: Reduces redundant layout passes
- **Simple API**: Single method call to batch operations

## Basic Usage

### Synchronous Batch

```java
// Execute multiple style operations in a batch
TwBatch.run(() -> {
    TwStyle.apply(node1, "p-4", "bg-blue-500");
    TwStyle.apply(node2, "m-2", "text-white");
    TwStyle.apply(node3, "rounded-lg", "shadow");
});
```

### Asynchronous Batch

```java
// Execute batch asynchronously (on JavaFX Application Thread)
TwBatch.runAsync(() -> {
    TwStyle.apply(node1, "bg-red-500");
    TwStyle.apply(node2, "bg-green-500");
    TwStyle.apply(node3, "bg-blue-500");
});
```

## Advanced Usage

### Large-Scale Styling

```java
public void styleManyNodes(List<Node> nodes) {
    TwBatch.run(() -> {
        for (Node node : nodes) {
            TwStyle.apply(node, "p-4", "m-2", "rounded");
        }
    });
}
```

### Complex Layout Setup

```java
public void setupComplexLayout(Pane container) {
    TwBatch.run(() -> {
        // Apply container styles
        TwStyle.apply(container, "flex", "gap-4", "p-6");
        
        // Create and style children
        for (int i = 0; i < 10; i++) {
            Button btn = new Button("Button " + i);
            TwStyle.apply(btn, "flex-1", "py-2", "bg-blue-500", "text-white");
            container.getChildren().add(btn);
        }
        
        // Apply additional modifications
        TwStyle.apply(container, "justify-between");
    });
}
```

### Conditional Styling

```java
public void applyConditionalStyles(Node node, boolean isActive) {
    TwBatch.run(() -> {
        if (isActive) {
            TwStyle.apply(node, "bg-green-500", "text-white");
            TwStyle.remove(node, "bg-gray-500");
        } else {
            TwStyle.apply(node, "bg-gray-500", "text-gray-700");
            TwStyle.remove(node, "bg-green-500", "text-white");
        }
    });
}
```

## Performance Benefits

### Without Batching

```java
// Each call triggers separate processing
for (Node node : nodes) {
    TwStyle.apply(node, "p-4");      // Layout pass 1
    TwStyle.apply(node, "m-2");      // Layout pass 2
    TwStyle.apply(node, "rounded");  // Layout pass 3
}
// Total: 3 × N layout passes
```

### With Batching

```java
// All operations grouped together
TwBatch.run(() -> {
    for (Node node : nodes) {
        TwStyle.apply(node, "p-4", "m-2", "rounded");
    }
});
// Total: 1 layout pass for all operations
```

## Common Patterns

### Form Initialization

```java
public void initializeForm(VBox formContainer) {
    TwBatch.run(() -> {
        // Container styling
        TwStyle.apply(formContainer, "vbox-gap-4", "p-6", "max-w-md");
        
        // Label styling
        for (Label label : formLabels) {
            TwStyle.apply(label, "font-semibold", "text-gray-700");
        }
        
        // Input styling
        for (TextField input : formInputs) {
            TwStyle.apply(input, "w-full", "p-2", "border", "rounded");
        }
        
        // Button styling
        TwStyle.apply(submitBtn, "w-full", "py-2", "bg-blue-500", "text-white");
        TwStyle.apply(cancelBtn, "w-full", "py-2", "bg-gray-200");
    });
}
```

### Table Row Styling

```java
public void addTableRow(TableView<?> table, Object item) {
    TableRow row = createRow(item);
    
    TwBatch.run(() -> {
        TwStyle.apply(row, "hover:bg-gray-100", "transition");
        
        for (Node cell : row.getChildren()) {
            TwStyle.apply(cell, "p-2", "border-b");
        }
    });
    
    table.getItems().add(item);
}
```

### Theme Switching

```java
public void switchTheme(Scene scene, boolean isDark) {
    TwBatch.run(() -> {
        // Update all themed elements
        scene.getRoot().lookupAll(".themed").forEach(node -> {
            if (isDark) {
                TwStyle.apply(node, "dark:bg-gray-800", "dark:text-white");
            } else {
                TwStyle.apply(node, "bg-white", "text-gray-900");
            }
        });
    });
}
```

### Card Grid Population

```java
public void populateCardGrid(GridPane grid, List<CardData> cards) {
    TwBatch.run(() -> {
        int col = 0, row = 0;
        
        for (CardData data : cards) {
            Card card = createCard(data);
            
            // Style card components
            TwStyle.apply(card, "p-4", "rounded-lg", "shadow");
            TwStyle.apply(card.getTitle(), "font-bold", "text-lg");
            TwStyle.apply(card.getContent(), "text-gray-600");
            
            grid.add(card, col, row);
            
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    });
}
```

## Best Practices

1. **Group Related Operations**: Batch operations that affect the same container or related nodes
2. **Avoid Nested Batches**: Don't call `TwBatch.run()` from within another batch
3. **Keep Batches Focused**: Each batch should represent a logical unit of work
4. **Use Async for Non-Critical**: Use `runAsync()` for non-urgent styling operations
5. **Measure Performance**: Profile before and after to verify improvement

## When to Use Batching

### Good Candidates for Batching

- Initializing forms with many fields
- Populating lists or grids
- Applying themes to multiple elements
- Setting up complex layouts
- Bulk style updates

### Less Beneficial for Batching

- Single style applications
- Infrequent style changes
- Already optimized code paths
- JIT compilation of arbitrary values

## Error Handling

```java
try {
    TwBatch.run(() -> {
        TwStyle.apply(node1, "bg-blue-500");
        TwStyle.apply(null, "p-4"); // Will throw
        TwStyle.apply(node3, "m-2");
    });
} catch (NullPointerException e) {
    System.err.println("Batch failed: " + e.getMessage());
    // Handle error appropriately
}
```

## See Also

- `TwStyle` - Style application being batched
- `StylePerf` - Underlying performance optimization
- `TwMetrics` - Monitor batch performance
