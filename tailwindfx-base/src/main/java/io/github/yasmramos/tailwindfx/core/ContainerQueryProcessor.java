package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.util.Map;

/**
 * ContainerQueryProcessor - Handles container query utilities for Tailwind CSS v4 compatibility.
 *
 * <p>Container queries allow styling elements based on the size of their container rather than
 * the viewport. This processor detects and handles container-related tokens.</p>
 *
 * <p>Supported tokens:</p>
 * <ul>
 *   <li>{@code @[<breakpoint>]} - Container query breakpoints</li>
 *   <li>{@code @min-*} - Minimum container size queries</li>
 *   <li>{@code @max-*} - Maximum container size queries</li>
 * </ul>
 *
 * <p>Note: JavaFX does not natively support container queries. This processor provides
 * detection and logging for future implementation or CSS export scenarios.</p>
 *
 * @author yasmramos
 * @since 1.0.0
 */
public class ContainerQueryProcessor {

    private static final String CONTAINER_QUERY_PREFIX = "@";
    private static final String MIN_PREFIX = "@min-";
    private static final String MAX_PREFIX = "@max-";
    
    /**
     * Centralized breakpoints from ThemeConfig (matching Tailwind CSS v4).
     * sm: 640px, md: 768px, lg: 1024px, xl: 1280px, 2xl: 1536px
     * Using ConcurrentHashMap to allow dynamic additions while maintaining thread safety.
     */
    private static final Map<String, Integer> BREAKPOINTS = new java.util.concurrent.ConcurrentHashMap<>(
        ThemeConfig.defaultConfig().breakpoints()
    );

    /**
     * Result of processing a container query token.
     *
     * @param isContainerQuery whether the token is a container query
     * @param queryType the type of query (MIN, MAX, BREAKPOINT)
     * @param value the query value in pixels or breakpoint name
     * @param cssEquivalent equivalent CSS container query syntax (for export)
     */
    public record ContainerQueryResult(
        boolean isContainerQuery,
        QueryType queryType,
        String value,
        String cssEquivalent
    ) {}

    /**
     * Types of container queries supported.
     */
    public enum QueryType {
        MIN,      // @min-*
        MAX,      // @max-*
        BREAKPOINT, // @[breakpoint]
        UNKNOWN
    }

    /**
     * Checks if a token is a container query.
     *
     * @param token the token to check
     * @return true if the token is a container query, false otherwise
     */
    public static boolean isContainerQuery(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return token.startsWith(CONTAINER_QUERY_PREFIX);
    }

    /**
     * Processes a container query token and returns detailed information.
     *
     * @param token the container query token to process
     * @return a ContainerQueryResult with processing details
     */
    public static ContainerQueryResult processContainerQuery(String token) {
        if (!isContainerQuery(token)) {
            return new ContainerQueryResult(false, QueryType.UNKNOWN, null, null);
        }

        // Handle @min-* queries
        if (token.startsWith(MIN_PREFIX)) {
            String value = token.substring(MIN_PREFIX.length());
            int pixels = parseSizeValue(value);
            String css = String.format("@container (min-width: %dpx)", pixels);
            return new ContainerQueryResult(true, QueryType.MIN, value, css);
        }

        // Handle @max-* queries
        if (token.startsWith(MAX_PREFIX)) {
            String value = token.substring(MAX_PREFIX.length());
            int pixels = parseSizeValue(value);
            String css = String.format("@container (max-width: %dpx)", pixels);
            return new ContainerQueryResult(true, QueryType.MAX, value, css);
        }

        // Handle @[breakpoint] queries (token already starts with @)
        if (token.length() > 2 && token.charAt(1) == '[' && token.endsWith("]")) {
            String breakpoint = token.substring(2, token.length() - 1);
            Integer pixels = BREAKPOINTS.get(breakpoint);
            if (pixels != null) {
                String css = String.format("@container (min-width: %dpx)", pixels);
                return new ContainerQueryResult(true, QueryType.BREAKPOINT, breakpoint, css);
            }
            // Custom breakpoint
            int customPixels = parseSizeValue(breakpoint);
            String css = String.format("@container (min-width: %dpx)", customPixels);
            return new ContainerQueryResult(true, QueryType.BREAKPOINT, breakpoint, css);
        }

        // Unknown container query format
        return new ContainerQueryResult(true, QueryType.UNKNOWN, token, null);
    }

    /**
     * Parses a size value string into pixels.
     *
     * @param value the size value string (e.g., "320", "20rem", "50%")
     * @return the size in pixels
     */
    private static int parseSizeValue(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }

        // Handle pixel values
        if (value.endsWith("px")) {
            try {
                return Integer.parseInt(value.substring(0, value.length() - 2));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        // Handle rem values (1rem = 16px by default)
        if (value.endsWith("rem")) {
            try {
                double rems = Double.parseDouble(value.substring(0, value.length() - 3));
                return (int) Math.round(rems * 16);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        // Handle em values (1em = 16px by default)
        if (value.endsWith("em")) {
            try {
                double ems = Double.parseDouble(value.substring(0, value.length() - 2));
                return (int) Math.round(ems * 16);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        // Try parsing as plain number (assumes pixels)
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Gets the standard breakpoints map.
     *
     * @return unmodifiable map of breakpoint names to pixel values
     */
    public static Map<String, Integer> getBreakpoints() {
        return Map.copyOf(BREAKPOINTS);
    }

    /**
     * Adds a custom breakpoint.
     *
     * @param name the breakpoint name
     * @param pixels the breakpoint width in pixels
     */
    public static void addBreakpoint(String name, int pixels) {
        BREAKPOINTS.put(name, pixels);
    }

    /**
     * Removes a custom breakpoint.
     *
     * @param name the breakpoint name to remove
     * @return the previous value associated with the name, or null if none
     */
    public static Integer removeBreakpoint(String name) {
        return BREAKPOINTS.remove(name);
    }
}
