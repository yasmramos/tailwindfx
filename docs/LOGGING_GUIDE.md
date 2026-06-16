# Logging Guide

## Overview

TailwindFX uses Java's built-in `java.util.logging` (JUL) framework for diagnostic output. This guide explains how to configure and use logging effectively.

## Log Levels

| Level | When Used | Example |
|-------|-----------|---------|
| `SEVERE` | Critical errors that prevent operation | Failed to load theme CSS |
| `WARNING` | Suspicious conditions, non-critical errors | Unusually long class name detected |
| `INFO` | Normal operational messages | JIT compilation of unknown tokens |
| `FINE` | Detailed debugging information | Scene listener events |
| `FINER` | Very detailed tracing | Internal cache operations |
| `FINEST` | Most detailed tracing | Step-by-step algorithm execution |

## Configuration

### Programmatic Configuration

```java
import java.util.logging.*;

// Enable debug logging for all TailwindFX components
Logger rootLogger = Logger.getLogger("io.github.yasmramos.tailwindfx");
rootLogger.setLevel(Level.FINE);

ConsoleHandler handler = new ConsoleHandler();
handler.setLevel(Level.FINE);
rootLogger.addHandler(handler);

// Or enable specific components
Logger.getLogger("TailwindFX.JIT").setLevel(Level.INFO);
Logger.getLogger("TailwindFX.Theme").setLevel(Level.WARNING);
```

### Properties File Configuration

Create a `logging.properties` file:

```properties
# Global default level
.level = INFO

# TailwindFX specific levels
io.github.yasmramos.tailwindfx.level = FINE
TailwindFX.JIT.level = INFO
TailwindFX.Theme.level = WARNING
TailwindFX.Animation.level = FINE

# Handler configuration
handlers = java.util.logging.ConsoleHandler
java.util.logging.ConsoleHandler.level = FINE
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
```

Run with:
```bash
java -Djava.util.logging.config.file=logging.properties -jar your-app.jar
```

## Component-Specific Logging

### JIT Compiler (`TailwindFX.JIT`)

Logs token compilation and unknown utilities:

```java
// Enable JIT debug mode
JitCompiler.setDebug(true);

// Output examples:
// INFO: TailwindFX JIT: 'p-4' → inline: -fx-padding: 16px
// INFO: TailwindFX JIT: 'btn-primary' → class: btn-primary
// WARNING: TailwindFX JIT: token desconocido 'custom-util' (looks like a JIT utility but was not recognized)
```

### Theme Manager (`TailwindFX.Theme`)

Logs theme changes and scope operations:

```java
// Logs when themes are applied
ThemeManager.theme(scene).dark().apply();
// Output: Theme changed to dark for scene Scene@xxx

// Logs scoped theme creation
ThemeManager.scope(panel).preset("rose").apply();
// Output: Scoped theme 'rose' applied to panel
```

### Animation System (`TailwindFX.Animation`)

Logs animation lifecycle events:

```java
// Configure animation logger
Logger.getLogger("TailwindFX.Animation").setLevel(Level.FINE);

// Output examples:
// FINE: Animation 'fadeIn' started on node Button@xxx
// FINE: Animation 'pulse' registered in slot 'attention'
// WARNING: Animation cleanup triggered by scene removal
```

### Utility Conflict Resolver

Logs conflict resolution and cache operations:

```java
// Logs when conflicts are detected and resolved
UtilityConflictResolver.apply(node, "w-4");
UtilityConflictResolver.apply(node, "w-8"); // w-4 removed automatically
// Output: Resolved conflict in category 'w': removed w-4, added w-8
```

## Best Practices

### 1. Use Appropriate Log Levels

```java
// ✓ Good: Use appropriate levels
LOG.severe("Failed to load critical resource"); // System-breaking error
LOG.warning("Unusual input detected, proceeding with fallback"); // Recoverable issue
LOG.info("Operation completed successfully"); // Normal flow
LOG.fine("Method entry with params: " + params); // Debug info
```

### 2. Avoid Expensive Log Construction

```java
// ✗ Bad: Always constructs string
LOG.fine("Processing " + items.size() + " items: " + items.toString());

// ✓ Good: Only constructs if level is enabled
if (LOG.isLoggable(Level.FINE)) {
    LOG.fine("Processing " + items.size() + " items: " + items.toString());
}
```

### 3. Include Context in Messages

```java
// ✗ Bad: Missing context
LOG.warning("Cache miss");

// ✓ Good: Includes relevant context
LOG.warning("Cache miss for token '" + token + "' in thread " + 
            Thread.currentThread().getName());
```

### 4. Use Parameterized Logging (Java 9+)

```java
// Java 9+ style
LOG.log(Level.INFO, () -> "Processed " + count + " nodes");

// Pre-Java 9
if (LOG.isLoggable(Level.INFO)) {
    LOG.info("Processed " + count + " nodes");
}
```

## Monitoring in Production

### Performance Metrics Integration

```java
// Log cache statistics periodically
Timer timer = new Timer();
timer.scheduleAtFixedRate(new TimerTask() {
    @Override
    public void run() {
        LruCache.CacheStats stats = JitCompiler.getCacheStats();
        LOG.info(String.format(
            "Cache stats: size=%d/%d, hits=%d, misses=%d, ratio=%.2f%%",
            stats.size(), stats.maxCapacity(),
            stats.hitCount(), stats.missCount(),
            stats.hitRatio()
        ));
    }
}, 0, 60000); // Every minute
```

### Alerting on Warnings

```java
// Add custom handler for alerts
Logger logger = Logger.getLogger("io.github.yasmramos.tailwindfx");
logger.addHandler(new Handler() {
    @Override
    public void publish(LogRecord record) {
        if (record.getLevel() == Level.WARNING || 
            record.getLevel() == Level.SEVERE) {
            // Send to monitoring system
            MonitoringSystem.alert(record.getMessage());
        }
    }
    
    @Override public void flush() {}
    @Override public void close() throws SecurityException {}
});
```

## Troubleshooting

### Enable All Logging

```java
// Maximum verbosity for debugging
Logger root = Logger.getLogger("");
root.setLevel(Level.ALL);
for (Handler h : root.getHandlers()) {
    h.setLevel(Level.ALL);
}
```

### Common Issues

**Issue**: No log output visible  
**Solution**: Ensure handlers are configured and level is set appropriately

**Issue**: Too much log noise  
**Solution**: Increase level to WARNING or SEVERE for production

**Issue**: Performance impact from logging  
**Solution**: Use `isLoggable()` checks and avoid string concatenation in hot paths

## See Also

- [java.util.logging documentation](https://docs.oracle.com/en/java/javase/17/docs/api/java.logging/java/util/logging/package-summary.html)
- [LRU Cache Documentation](CACHE_LRU.md) - Cache performance monitoring
- [API Documentation](API_REFERENCE.md) - Complete API reference
