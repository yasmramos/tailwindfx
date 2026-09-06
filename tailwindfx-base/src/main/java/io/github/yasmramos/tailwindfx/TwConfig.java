package io.github.yasmramos.tailwindfx;

/**
 * TwConfig — Global configuration facade.
 *
 * <p>Provides access to global TailwindFX settings including unit size, debug mode, and performance
 * options.
 *
 * <pre>
 * TwConfig.INSTANCE.unit(8.0);
 * TwConfig.INSTANCE.debug(true);
 * TwConfig.INSTANCE.preferStylesheet(true);
 * double currentUnit = TwConfig.INSTANCE.unit();
 * </pre>
 */
public final class TwConfig {

  private static final TwConfig INSTANCE = new TwConfig();

  private static double UNIT_SIZE = 4.0;
  private static boolean DEBUG_MODE = false;
  private static boolean PREFER_STYLESHEET = false;

  private TwConfig() {}

  /**
   * Get the current unit size.
   *
   * @return unit size in pixels
   */
  public static double unit() {
    return UNIT_SIZE;
  }

  /**
   * Set the unit size (base multiplier for spacing utilities).
   *
   * @param value the new unit size (must be positive)
   * @throws IllegalArgumentException if value <= 0
   */
  public static void unit(double value) {
    if (value <= 0) {
      throw new IllegalArgumentException("unit size must be positive: " + value);
    }
    UNIT_SIZE = value;
  }

  /**
   * Check if debug mode is enabled.
   *
   * @return true if debug mode is on
   */
  public static boolean isDebug() {
    return DEBUG_MODE;
  }

  /**
   * Enable or disable debug mode.
   *
   * @param enabled true to enable debug logging
   */
  public static void debug(boolean enabled) {
    DEBUG_MODE = enabled;
  }

  /**
   * Check if stylesheet-based styling is preferred over inline JIT. When enabled, TailwindFX will
   * apply CSS classes from the generated stylesheet instead of compiling inline styles, for tokens
   * that exist in the AOT stylesheet.
   *
   * @return true if preferStylesheet mode is enabled
   */
  public static boolean isPreferStylesheet() {
    return PREFER_STYLESHEET;
  }

  /**
   * Enable or disable preferStylesheet mode. When enabled, TailwindFX applies CSS classes from the
   * build-time generated stylesheet instead of compiling inline JIT styles, for tokens that exist
   * in the AOT stylesheet. Dynamic/arbitrary values still use JIT fallback.
   *
   * @param enabled true to enable stylesheet-based styling
   */
  public static void preferStylesheet(boolean enabled) {
    PREFER_STYLESHEET = enabled;
  }

  /** Reset configuration to default values (useful for testing). */
  public static void reset() {
    UNIT_SIZE = 4.0;
    DEBUG_MODE = false;
    PREFER_STYLESHEET = false;
  }
}
