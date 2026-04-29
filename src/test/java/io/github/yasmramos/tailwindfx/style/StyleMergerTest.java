package io.github.yasmramos.tailwindfx.style;

import io.github.yasmramos.tailwindfx.style.StyleMerger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StyleMerger.
 */
@DisplayName("StyleMerger Tests")
class StyleMergerTest {

    @Nested
    @DisplayName("Basic Merge")
    class BasicMergeTests {

        @Test
        @DisplayName("Should merge two style strings")
        void testBasicMerge() {
            String result = StyleMerger.merge("-fx-padding: 16px;", "-fx-background-color: blue;");
            
            assertNotNull(result);
            assertTrue(result.contains("-fx-padding"));
            assertTrue(result.contains("-fx-background-color"));
        }

        @Test
        @DisplayName("Should handle null first style")
        void testNullFirstStyle() {
            String result = StyleMerger.merge(null, "-fx-background-color: blue;");
            
            assertNotNull(result);
            assertTrue(result.contains("-fx-background-color"));
        }

        @Test
        @DisplayName("Should handle null second style")
        void testNullSecondStyle() {
            String result = StyleMerger.merge("-fx-padding: 16px;", null);
            
            assertNotNull(result);
            assertTrue(result.contains("-fx-padding"));
        }

        @Test
        @DisplayName("Should handle both null styles")
        void testBothNullStyles() {
            String result = StyleMerger.merge(null, null);
            
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Conflict Resolution")
    class ConflictResolutionTests {

        @Test
        @DisplayName("Should resolve same property conflict (last wins)")
        void testSamePropertyConflict() {
            String result = StyleMerger.merge(
                "-fx-padding: 8px;", 
                "-fx-padding: 16px;");
            
            assertNotNull(result);
            // Last value should win
            assertTrue(result.contains("16px"));
        }

        @Test
        @DisplayName("Should preserve different properties")
        void testDifferentProperties() {
            String result = StyleMerger.merge(
                "-fx-padding: 16px;", 
                "-fx-margin: 8px;");
            
            assertNotNull(result);
            assertTrue(result.contains("-fx-padding"));
            assertTrue(result.contains("-fx-margin"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty strings")
        void testEmptyStrings() {
            String result = StyleMerger.merge("", "");
            
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle blank strings")
        void testBlankStrings() {
            String result = StyleMerger.merge("  ", "  ");
            
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle complex multi-property merge")
        void testComplexMerge() {
            String style1 = "-fx-padding: 16px; -fx-margin: 8px;";
            String style2 = "-fx-background-color: blue; -fx-border-width: 2px;";
            
            String result = StyleMerger.merge(style1, style2);
            
            assertNotNull(result);
            assertTrue(result.contains("-fx-padding"));
            assertTrue(result.contains("-fx-margin"));
            assertTrue(result.contains("-fx-background-color"));
            assertTrue(result.contains("-fx-border-width"));
        }
    }
}
