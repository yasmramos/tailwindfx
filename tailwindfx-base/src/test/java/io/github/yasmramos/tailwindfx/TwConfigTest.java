package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for TwConfig global configuration. */
@DisplayName("TwConfig Tests")
class TwConfigTest {

  private double originalUnit;
  private boolean originalDebug;

  @BeforeEach
  void setUp() {
    // Save original values to restore after tests
    originalUnit = TwConfig.unit();
    originalDebug = TwConfig.isDebug();
  }

  @AfterEach
  void tearDown() {
    // Restore original values
    TwConfig.unit(originalUnit);
    TwConfig.debug(originalDebug);
  }

  @Test
  @DisplayName("Should have default unit size of 4.0")
  void shouldHaveDefaultUnitSize() {
    // Reset to default first
    TwConfig.unit(4.0);
    assertEquals(4.0, TwConfig.unit(), "Default unit size should be 4.0");
  }

  @Test
  @DisplayName("Should have debug mode disabled by default")
  void shouldHaveDebugDisabledByDefault() {
    TwConfig.debug(false);
    assertFalse(TwConfig.isDebug(), "Debug mode should be disabled by default");
  }

  @Test
  @DisplayName("Should set and get custom unit size")
  void shouldSetAndGetCustomUnitSize() {
    TwConfig.unit(8.0);
    assertEquals(8.0, TwConfig.unit(), "Unit size should be 8.0");

    TwConfig.unit(16.0);
    assertEquals(16.0, TwConfig.unit(), "Unit size should be 16.0");
  }

  @Test
  @DisplayName("Should enable and disable debug mode")
  void shouldEnableAndDisableDebugMode() {
    TwConfig.debug(true);
    assertTrue(TwConfig.isDebug(), "Debug mode should be enabled");

    TwConfig.debug(false);
    assertFalse(TwConfig.isDebug(), "Debug mode should be disabled");
  }

  @Test
  @DisplayName("Should throw exception when setting negative unit size")
  void shouldThrowExceptionForNegativeUnitSize() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          TwConfig.unit(-1.0);
        },
        "Should throw exception for negative unit size");
  }

  @Test
  @DisplayName("Should throw exception when setting zero unit size")
  void shouldThrowExceptionForZeroUnitSize() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          TwConfig.unit(0.0);
        },
        "Should throw exception for zero unit size");
  }

  @Test
  @DisplayName("Should accept very small positive unit size")
  void shouldAcceptSmallPositiveUnitSize() {
    TwConfig.unit(0.1);
    assertEquals(0.1, TwConfig.unit(), "Should accept very small positive unit size");
  }

  @Test
  @DisplayName("Should accept large unit size")
  void shouldAcceptLargeUnitSize() {
    TwConfig.unit(1000.0);
    assertEquals(1000.0, TwConfig.unit(), "Should accept large unit size");
  }

  @Test
  @DisplayName("Should maintain state across multiple calls")
  void shouldMaintainStateAcrossMultipleCalls() {
    TwConfig.unit(12.0);
    TwConfig.debug(true);

    assertEquals(12.0, TwConfig.unit());
    assertTrue(TwConfig.isDebug());

    // Change again
    TwConfig.unit(6.0);
    TwConfig.debug(false);

    assertEquals(6.0, TwConfig.unit());
    assertFalse(TwConfig.isDebug());
  }

  @Test
  @DisplayName("Should allow chaining of configuration changes")
  void shouldAllowChainingOfConfigurationChanges() {
    // Configure multiple settings
    TwConfig.unit(10.0);
    assertEquals(10.0, TwConfig.unit());

    TwConfig.debug(true);
    assertTrue(TwConfig.isDebug());

    // Verify both are set correctly
    assertEquals(10.0, TwConfig.unit());
    assertTrue(TwConfig.isDebug());
  }
}
