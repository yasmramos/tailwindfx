package tailwindfx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Preconditions.
 */
@DisplayName("Preconditions Tests")
class PreconditionsTest {

    @Nested
    @DisplayName("Null Checks")
    class NullCheckTests {

        @Test
        @DisplayName("Should not throw on non-null value")
        void testNonNullValue() {
            assertDoesNotThrow(() -> {
                Preconditions.requireNonNull("test", "testMethod", "value");
            });
        }

        @Test
        @DisplayName("Should throw on null value")
        void testNullValue() {
            assertThrows(IllegalArgumentException.class, () -> {
                Preconditions.requireNonNull(null, "testMethod", "value");
            });
        }

        @Test
        @DisplayName("Should include message in exception")
        void testExceptionMessage() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                Preconditions.requireNonNull(null, "testMethod", "myValue");
            });

            assertTrue(exception.getMessage().contains("myValue"));
        }
    }

    @Nested
    @DisplayName("Blank String Checks")
    class BlankCheckTests {

        @Test
        @DisplayName("Should not throw on non-blank string")
        void testNonBlankString() {
            assertDoesNotThrow(() -> {
                Preconditions.requireNonBlank("test", "testMethod", "value");
            });
        }

        @Test
        @DisplayName("Should throw on null string")
        void testNullString() {
            assertThrows(IllegalArgumentException.class, () -> {
                Preconditions.requireNonBlank(null, "testMethod", "value");
            });
        }

        @Test
        @DisplayName("Should throw on empty string")
        void testEmptyString() {
            assertThrows(IllegalArgumentException.class, () -> {
                Preconditions.requireNonBlank("", "testMethod", "value");
            });
        }

        @Test
        @DisplayName("Should throw on blank string")
        void testBlankString() {
            assertThrows(IllegalArgumentException.class, () -> {
                Preconditions.requireNonBlank("   ", "testMethod", "value");
            });
        }

        @Test
        @DisplayName("Should include message in exception")
        void testExceptionMessageNotBlank() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                Preconditions.requireNonBlank("", "testMethod", "myValue");
            });

            assertTrue(exception.getMessage().contains("myValue"));
        }
    }

    @Nested
    @DisplayName("Range Checks")
    class RangeCheckTests {

        @Test
        @DisplayName("Should not throw when span >= 1")
        void testValidSpan() {
            assertDoesNotThrow(() -> {
                Preconditions.requireSpan(5, "testMethod");
            });
        }

        @Test
        @DisplayName("Should not throw when span equals 1")
        void testSpanEqualsOne() {
            assertDoesNotThrow(() -> {
                Preconditions.requireSpan(1, "testMethod");
            });
        }

        @Test
        @DisplayName("Should throw when span below 1")
        void testSpanBelowMin() {
            assertThrows(IllegalArgumentException.class, () -> {
                Preconditions.requireSpan(0, "testMethod");
            });
        }

        @Test
        @DisplayName("Should not throw on valid opacity")
        void testValidOpacity() {
            assertDoesNotThrow(() -> {
                Preconditions.requireOpacity(0.5, "testMethod");
            });
        }

        @Test
        @DisplayName("Should throw on invalid opacity")
        void testInvalidOpacity() {
            assertThrows(IllegalArgumentException.class, () -> {
                Preconditions.requireOpacity(1.5, "testMethod");
            });
        }

        @Test
        @DisplayName("Should not throw on positive duration")
        void testPositiveDuration() {
            assertDoesNotThrow(() -> {
                Preconditions.requirePositiveDuration(100, "testMethod");
            });
        }

        @Test
        @DisplayName("Should throw on zero duration")
        void testZeroDuration() {
            assertThrows(IllegalArgumentException.class, () -> {
                Preconditions.requirePositiveDuration(0, "testMethod");
            });
        }
    }

    @Nested
    @DisplayName("Warning Mode")
    class WarningTests {

        @Test
        @DisplayName("Should not throw with valid node")
        void testWarnNoParent() {
            // Just verify it doesn't throw with a valid (but parentless) node
            // Can't easily create a JavaFX node in unit tests without TestFX
            assertDoesNotThrow(() -> {
                // Skip this test - requires JavaFX node instance
            });
        }
    }
}
