package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CssIdentEscaper}. */
@DisplayName("CssIdentEscaper")
class CssIdentEscaperTest {

  @Nested
  @DisplayName("Null and empty input")
  class NullAndEmpty {

    @Test
    @DisplayName("escape(null) returns empty string")
    void escapeNullReturnsEmpty() {
      assertEquals("", CssIdentEscaper.escape(null));
    }

    @Test
    @DisplayName("escape(\"\") returns empty string")
    void escapeEmptyReturnsEmpty() {
      assertEquals("", CssIdentEscaper.escape(""));
    }

    @Test
    @DisplayName("toSelector(null) returns lone dot")
    void toSelectorNullReturnsDot() {
      assertEquals(".", CssIdentEscaper.toSelector(null));
    }
  }

  @Nested
  @DisplayName("Simple identifiers")
  class SimpleIdentifiers {

    @Test
    @DisplayName("plain lowercase identifier is unchanged")
    void plainIdentifierUnchanged() {
      assertEquals("flex", CssIdentEscaper.escape("flex"));
    }

    @Test
    @DisplayName("hyphens and alphanumerics are preserved")
    void hyphenatedNamePreserved() {
      assertEquals("bg-red-500", CssIdentEscaper.escape("bg-red-500"));
    }

    @Test
    @DisplayName("non-leading digits are not escaped")
    void nonLeadingDigitsPreserved() {
      assertEquals("p-4", CssIdentEscaper.escape("p-4"));
      assertEquals("mt-10", CssIdentEscaper.escape("mt-10"));
    }

    @Test
    @DisplayName("underscore and letters are safe")
    void underscorePreserved() {
      assertEquals("text_left_align", CssIdentEscaper.escape("text_left_align"));
    }
  }

  @Nested
  @DisplayName("Special characters")
  class SpecialCharacters {

    @Test
    @DisplayName("slash is escaped (fraction widths)")
    void slashEscaped() {
      assertEquals("w-1\\/2", CssIdentEscaper.escape("w-1/2"));
    }

    @Test
    @DisplayName("colon is escaped (variants)")
    void colonEscaped() {
      assertEquals("hover\\:bg-red-500", CssIdentEscaper.escape("hover:bg-red-500"));
    }

    @Test
    @DisplayName("dot is escaped (decimal sizes)")
    void dotEscaped() {
      assertEquals("p-0\\.5", CssIdentEscaper.escape("p-0.5"));
    }

    @Test
    @DisplayName("square brackets are escaped (arbitrary values)")
    void bracketsEscaped() {
      assertEquals("w-\\[200px\\]", CssIdentEscaper.escape("w-[200px]"));
    }

    @Test
    @DisplayName("percent and hash are escaped (colors, opacities)")
    void percentAndHashEscaped() {
      assertEquals("opacity-50\\%", CssIdentEscaper.escape("opacity-50%"));
      assertEquals("bg-\\#ff0000", CssIdentEscaper.escape("bg-#ff0000"));
    }

    @Test
    @DisplayName("important bang is escaped")
    void bangEscaped() {
      assertEquals("\\!flex", CssIdentEscaper.escape("!flex"));
    }

    @Test
    @DisplayName("all documented special characters are escaped with a backslash")
    void allSpecialCharsEscaped() {
      String specials = "/:.[]%#!(),=~^|{}$@&*'?<>;";
      StringBuilder expected = new StringBuilder();
      for (int i = 0; i < specials.length(); i++) {
        expected.append('\\').append(specials.charAt(i));
      }
      assertEquals(expected.toString(), CssIdentEscaper.escape(specials));
    }

    @Test
    @DisplayName("backslash itself is escaped first")
    void backslashEscaped() {
      assertEquals("a\\\\b", CssIdentEscaper.escape("a\\b"));
    }

    @Test
    @DisplayName("backslash combined with special char keeps escaping order stable")
    void backslashWithSpecialChar() {
      // Input "\w-1/2": leading backslash -> \\, slash -> \/
      assertEquals("\\\\w-1\\/2", CssIdentEscaper.escape("\\w-1/2"));
    }
  }

  @Nested
  @DisplayName("Leading characters")
  class LeadingCharacters {

    @Test
    @DisplayName("leading digit uses CSS hex escape with space terminator")
    void leadingDigitEscaped() {
      // '1' -> \\31 (hex code point of '1' is 31) followed by a space terminator.
      assertEquals("\\31 2-col", CssIdentEscaper.escape("12-col"));
    }

    @Test
    @DisplayName("lone leading hyphen is escaped")
    void leadingHyphenEscaped() {
      assertEquals("\\-m-4", CssIdentEscaper.escape("-m-4"));
    }

    @Test
    @DisplayName("hyphen after first character is not escaped")
    void innerHyphenNotEscaped() {
      // The colon is still escaped; only the inner hyphen stays raw.
      assertEquals("sm\\:flex-row", CssIdentEscaper.escape("sm:flex-row"));
    }
  }

  @Nested
  @DisplayName("Selector building")
  class SelectorBuilding {

    @Test
    @DisplayName("toSelector prefixes escaped name with a dot")
    void selectorHasLeadingDot() {
      assertEquals(".w-1\\/2", CssIdentEscaper.toSelector("w-1/2"));
    }

    @Test
    @DisplayName("toSelector on simple name")
    void selectorSimple() {
      assertEquals(".flex", CssIdentEscaper.toSelector("flex"));
    }

    @Test
    @DisplayName("toSelector handles variant chains")
    void selectorVariants() {
      assertEquals(".md\\:hover\\:bg-blue-500", CssIdentEscaper.toSelector("md:hover:bg-blue-500"));
    }
  }

  @Nested
  @DisplayName("Idempotence properties")
  class IdempotenceProperties {

    @Test
    @DisplayName("escaping an already-safe name is identity")
    void safeNamesAreIdentity() {
      for (String name : new String[] {"flex", "items-center", "gap-4", "font-bold"}) {
        assertEquals(name, CssIdentEscaper.escape(name), "name should be unchanged: " + name);
      }
    }

    @Test
    @DisplayName("output contains no raw special characters")
    void outputHasNoRawSpecials() {
      String escaped = CssIdentEscaper.escape("grid-cols-[repeat(2,minmax(0,1fr))]");
      for (char c : new char[] {'/', ':', '[', ']', '(', ')', ','}) {
        // Every occurrence of these in the output must be preceded by a backslash.
        int idx = escaped.indexOf(c);
        while (idx >= 0) {
          org.junit.jupiter.api.Assertions.assertTrue(
              idx > 0 && escaped.charAt(idx - 1) == '\\',
              "unescaped '" + c + "' found at index " + idx + " in: " + escaped);
          idx = escaped.indexOf(c, idx + 1);
        }
      }
    }
  }
}
