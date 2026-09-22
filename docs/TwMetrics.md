# TwMetrics Documentation

The `TwMetrics` class is a metrics facade for performance monitoring in TailwindFX. It provides access to TailwindFX metrics, debug reports, and health checks.

## Overview

`TwMetrics` is a singleton facade that delegates to `TailwindFXMetrics` for collecting and reporting performance data about style applications, cache usage, JIT compilation, and overall system health.

## Key Features

- **Debug Reports**: Generate detailed reports for nodes
- **Health Checks**: Verify system operational status
- **Metrics Toggle**: Enable/disable metrics collection
- **Performance Insights**: Track style application performance

## Basic Usage

### Debug Report

```java
// Generate debug report for a node
String report = TwMetrics.debugReport(node);
System.out.println(report);

// Report includes:
// - Applied styles
// - Cache hits/misses
// - JIT compilations
// - Performance timings
```

### Health Check

```java
// Run health check
TwMetrics.healthCheck();

// Outputs system status:
// - CSS installation status
// - Cache health
// - Memory usage
// - Active animations
```

### Enable/Disable Metrics

```java
// Enable metrics collection
TwMetrics.setEnabled(true);

// Disable metrics collection (for production)
TwMetrics.setEnabled(false);

// Check if enabled
boolean isEnabled = TwMetrics.isEnabled();
```

## Advanced Usage

### Performance Monitoring

```java
// Monitor style application performance
TwMetrics.setEnabled(true);

// Apply styles
TwStyle.apply(node, "bg-blue-500", "p-4");

// Get metrics
String report = TwMetrics.debugReport(node);
if (report.contains("slow")) {
    System.err.println("Performance issue detected!");
}
```

### Debug Mode Integration

```java
// Enable both debug mode and metrics
TwConfig.debug(true);
TwMetrics.setEnabled(true);

// Now all operations are logged with timing information
```

### Custom Reporting

```java
public class PerformanceMonitor {
    
    public static void logNodeMetrics(Node node) {
        if (TwMetrics.isEnabled()) {
            String report = TwMetrics.debugReport(node);
            logger.info("Node metrics: " + report);
        }
    }
    
    public static void checkSystemHealth() {
        try {
            TwMetrics.healthCheck();
            logger.info("System healthy");
        } catch (Exception e) {
            logger.error("Health check failed: " + e.getMessage());
        }
    }
}
```

## Metrics Collected

### Style Application Metrics
- Number of styles applied
- Time per style application
- Cache hit rate
- JIT compilation count

### Cache Metrics
- Cache size
- Eviction count
- Hit/miss ratio
- Memory usage

### Layout Metrics
- Layout pass count
- Layout time
- Affected nodes

### Animation Metrics
- Active animations
- Frame rate
- Dropped frames

## Best Practices

1. **Enable in Development**: Use metrics during development to identify bottlenecks
2. **Disable in Production**: Turn off metrics collection in production for best performance
3. **Regular Health Checks**: Periodically run health checks in long-running applications
4. **Monitor Trends**: Track metrics over time to identify degradation

## Common Patterns

### Development Helper

```java
public class DevTools {
    
    @FXML
    private void showMetrics(ActionEvent event) {
        Node target = getTargetNode(event);
        String report = TwMetrics.debugReport(target);
        
        TextArea area = new TextArea(report);
        area.setPrefSize(600, 400);
        
        new Stage()..setScene(new Scene(area));
    }
    
    @FXML
    private void runHealthCheck(ActionEvent event) {
        TwMetrics.healthCheck();
        showAlert("Health Check", "System is operational");
    }
}
```

### Performance Testing

```java
@Test
public void testStyleApplicationPerformance() {
    TwMetrics.setEnabled(true);
    
    long start = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++) {
        TwStyle.apply(testNode, "bg-blue-500");
    }
    long duration = System.currentTimeMillis() - start;
    
    String report = TwMetrics.debugReport(testNode);
    System.out.println("1000 applications in " + duration + "ms");
    System.out.println(report);
    
    assertTrue(duration < 5000); // Should complete in under 5 seconds
}
```

### Automated Monitoring

```java
public class MetricsWatcher implements Runnable {
    
    private final Node watchedNode;
    private final long intervalMs;
    
    public MetricsWatcher(Node node, long intervalMs) {
        this.watchedNode = node;
        this.intervalMs = intervalMs;
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                String report = TwMetrics.debugReport(watchedNode);
                if (report.contains("warning") || report.contains("error")) {
                    sendAlert(report);
                }
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    private void sendAlert(String report) {
        // Send to monitoring service
    }
}
```

## Troubleshooting

### High Memory Usage

If metrics show high memory usage:
1. Check cache size in report
2. Consider reducing cache capacity
3. Clear unused styles

### Slow Style Application

If style application is slow:
1. Check JIT compilation count
2. Prefer standard utilities over arbitrary values
3. Review conflict resolution overhead

### Health Check Failures

If health check fails:
1. Verify CSS is installed
2. Check for null references
3. Ensure scene graph is valid

## See Also

- `TailwindFXMetrics` - Core metrics implementation
- `TwConfig` - Configuration including debug mode
- `TwStyle` - Style application being measured
- `StyleCache` - Caching system metrics
