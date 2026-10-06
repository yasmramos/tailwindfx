package io.github.yasmramos.tailwindfx.style;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that "rounded" without a suffix is parsed as a valid NAMED token
 * with namedValue="default", so that resolveBorderRadius("default") returns
 * the default border radius value.
 */
class RoundedBaseTokenTest {

  @Test
  @DisplayName("rounded without suffix should parse as NAMED with namedValue=default")
  void testRoundedBaseParsesAsNamed() {
    StyleToken token = StyleToken.parse("rounded");
    assertEquals(StyleToken.Kind.NAMED, token.kind, "rounded should be NAMED kind");
    assertEquals("rounded", token.prefix, "prefix should be 'rounded'");
    assertEquals("default", token.namedValue, "namedValue should be 'default'");
  }

  @Test
  @DisplayName("rounded-lg should still parse as NAMED with namedValue=lg")
  void testRoundedLgStillWorks() {
    StyleToken token = StyleToken.parse("rounded-lg");
    assertEquals(StyleToken.Kind.NAMED, token.kind, "rounded-lg should be NAMED kind");
    assertEquals("rounded", token.prefix, "prefix should be 'rounded'");
    assertEquals("lg", token.namedValue, "namedValue should be 'lg'");
  }

  @Test
  @DisplayName("rounded-full should still parse as NAMED with namedValue=full")
  void testRoundedFullStillWorks() {
    StyleToken token = StyleToken.parse("rounded-full");
    assertEquals(StyleToken.Kind.NAMED, token.kind, "rounded-full should be NAMED kind");
    assertEquals("rounded", token.prefix, "prefix should be 'rounded'");
    assertEquals("full", token.namedValue, "namedValue should be 'full'");
  }
}
