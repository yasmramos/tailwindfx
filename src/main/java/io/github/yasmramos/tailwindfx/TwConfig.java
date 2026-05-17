package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.core.Preconditions;

/**
 * TwConfig — Global configuration facade.
 * 
 * <p>Provides access to global TailwindFX settings including
 * unit size, debug mode, and performance options.</p>
 * 
 * <pre>
 * TwConfig.INSTANCE.unit(8.0);
 * TwConfig.INSTANCE.debug(true);
 * double currentUnit = TwConfig.INSTANCE.unit();
 * </pre>
 */
public final class TwConfig {
    
    public static final TwConfig INSTANCE = new TwConfig();
    
    private static double UNIT_SIZE = 4.0;
    private static boolean DEBUG_MODE = false;
    
    private TwConfig() {}
    
    /**
     * Get the current unit size.
     * @return unit size in pixels
     */
    public double unit() {
        return UNIT_SIZE;
    }
    
    /**
     * Set the unit size (base multiplier for spacing utilities).
     * @param value the new unit size (must be positive)
     * @throws IllegalArgumentException if value <= 0
     */
    public void unit(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("unit size must be positive: " + value);
        }
        UNIT_SIZE = value;
    }
    
    /**
     * Check if debug mode is enabled.
     * @return true if debug mode is on
     */
    public boolean isDebug() {
        return DEBUG_MODE;
    }
    
    /**
     * Enable or disable debug mode.
     * @param enabled true to enable debug logging
     */
    public void debug(boolean enabled) {
        DEBUG_MODE = enabled;
    }
}
