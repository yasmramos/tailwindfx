package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.style.TypeHint;

/**
 * TypeHintProcessor — Advanced type hint detection and processing for Tailwind CSS v4.
 *
 * <p>This processor handles arbitrary values with explicit type hints to disambiguate
 * utility classes that could accept multiple types of values.</p>
 *
 * <p>Supported type hints:</p>
 * <ul>
 *   <li>{@code length} - for lengths (px, rem, em, etc.)</li>
 *   <li>{@code percentage} - for percentages (%)</li>
 *   <li>{@code number} - for unitless numbers</li>
 *   <li>{@code color} - for colors (hex, rgb, rgba, named colors)</li>
 *   <li>{@code angle} - for angles (deg, rad, turn)</li>
 *   <li>{@code url} - for URLs</li>
 *   <li>{@code image} - for image functions</li>
 *   <li>{@code family-name} - for font families</li>
 *   <li>{@code line-width} - for border widths</li>
 *   <li>{@code shape} - for shape functions</li>
 *   <li>{@code position} - for background positions</li>
 *   <li>{@code bg-size} - for background sizes</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>{@code w-[length:320px]} → width: 320px</li>
 *   <li>{@code text-[percentage:50%]} → opacity: 0.5</li>
 *   <li>{@code rotate-[angle:45deg]} → rotate: 45deg</li>
 *   <li>{@code bg-[color:#ff0000]} → background-color: #ff0000</li>
 *   <li>{@code opacity-[number:0.5]} → opacity: 0.5</li>
 *   <li>{@code font-['Custom_Font']} → font-family: 'Custom Font'</li>
 * </ul>
 *
 * @author yasmramos
 * @since 1.0.0
 */
public final class TypeHintProcessor {

    private static final String ARBITRARY_START = "[";
    private static final String ARBITRARY_END = "]";
    private static final String TYPE_HINT_SEPARATOR = ":";

    private TypeHintProcessor() {
        // Utility class
    }

    /**
     * Checks if a token contains an arbitrary value with or without type hints.
     *
     * @param token the token to check
     * @return true if the token contains an arbitrary value
     */
    public static boolean hasArbitraryValue(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        int openBracket = token.indexOf(ARBITRARY_START);
        int closeBracket = token.lastIndexOf(ARBITRARY_END);
        return openBracket != -1 && closeBracket > openBracket;
    }

    /**
     * Extracts the arbitrary value from a token.
     *
     * @param token the token containing an arbitrary value
     * @return the arbitrary value without brackets, or null if not found
     */
    public static String extractArbitraryValue(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        int openBracket = token.indexOf(ARBITRARY_START);
        int closeBracket = token.lastIndexOf(ARBITRARY_END);

        if (openBracket == -1 || closeBracket <= openBracket) {
            return null;
        }

        return token.substring(openBracket + 1, closeBracket);
    }

    /**
     * Processes a token with type hints and returns the appropriate CSS value.
     *
     * @param token the token to process
     * @return TypeHintResult with parsed type and value, or null if not applicable
     */
    public static TypeHintResult processTypeHint(String token) {
        if (!hasArbitraryValue(token)) {
            return null;
        }

        String arbitraryValue = extractArbitraryValue(token);
        if (arbitraryValue == null || arbitraryValue.isEmpty()) {
            return null;
        }

        // Check for type hint
        TypeHint typeHint = TypeHint.parse(arbitraryValue);
        String actualValue = TypeHint.extractValue(arbitraryValue);

        return new TypeHintResult(typeHint, actualValue, arbitraryValue);
    }

    /**
     * Automatically detects the type of an arbitrary value when no explicit hint is provided.
     *
     * @param value the arbitrary value to analyze
     * @return the detected TypeHint, or null if unable to detect
     */
    public static TypeHint autoDetectType(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // Color detection
        if (isColorValue(value)) {
            return TypeHint.COLOR;
        }

        // Angle detection
        if (isAngleValue(value)) {
            return TypeHint.ANGLE;
        }

        // Percentage detection
        if (value.endsWith("%")) {
            return TypeHint.PERCENTAGE;
        }

        // Length detection
        if (isLengthValue(value)) {
            return TypeHint.LENGTH;
        }

        // Number detection (unitless)
        if (isNumberValue(value)) {
            return TypeHint.NUMBER;
        }

        // URL detection
        if (value.startsWith("url(")) {
            return TypeHint.URL;
        }

        // Image function detection
        if (isImageValue(value)) {
            return TypeHint.IMAGE;
        }

        return null;
    }

    /**
     * Checks if a value looks like a color.
     */
    private static boolean isColorValue(String value) {
        // Hex colors
        if (value.matches("^#[0-9a-fA-F]{3,8}$")) {
            return true;
        }

        // RGB/RGBA functions
        if (value.startsWith("rgb(") || value.startsWith("rgba(")) {
            return true;
        }

        // HSL/HSLA functions
        if (value.startsWith("hsl(") || value.startsWith("hsla(")) {
            return true;
        }

        // Named colors (common ones)
        return switch (value.toLowerCase()) {
            case "transparent", "currentcolor", "inherit" -> true;
            default -> false;
        };
    }

    /**
     * Checks if a value looks like an angle.
     */
    private static boolean isAngleValue(String value) {
        return value.matches("^-?\\d+(\\.\\d+)?(deg|rad|turn|grad)$");
    }

    /**
     * Checks if a value looks like a length.
     */
    private static boolean isLengthValue(String value) {
        return value.matches("^-?\\d+(\\.\\d+)?(px|rem|em|vh|vw|vmin|vmax|pt|pc|in|cm|mm)$");
    }

    /**
     * Checks if a value looks like a unitless number.
     */
    private static boolean isNumberValue(String value) {
        return value.matches("^-?\\d+(\\.\\d+)?$");
    }

    /**
     * Checks if a value looks like an image function.
     */
    private static boolean isImageValue(String value) {
        return value.startsWith("linear-gradient(") ||
               value.startsWith("radial-gradient(") ||
               value.startsWith("conic-gradient(") ||
               value.startsWith("image-set(");
    }

    /**
     * Converts a type-hinted value to a JavaFX-compatible CSS value.
     *
     * @param typeHint the type hint
     * @param value the actual value
     * @return the JavaFX CSS value, or null if not convertible
     */
    public static String toJavaFxValue(TypeHint typeHint, String value) {
        if (typeHint == null || value == null) {
            return null;
        }

        return switch (typeHint) {
            case LENGTH -> convertLength(value);
            case PERCENTAGE -> value; // Percentages work directly in JavaFX
            case NUMBER -> value; // Unitless numbers work directly
            case COLOR -> convertColor(value);
            case ANGLE -> convertAngle(value);
            case URL -> value;
            case IMAGE -> convertImage(value);
            case FAMILY_NAME -> normalizeFontFamily(value);
            case LINE_WIDTH -> convertLength(value);
            case POSITION -> value;
            case BG_SIZE -> convertBackgroundSize(value);
            case SHAPE -> null; // Not supported in JavaFX
        };
    }

    /**
     * Converts a length value to JavaFX format.
     */
    private static String convertLength(String value) {
        // JavaFX supports px, em, but not all CSS units
        if (value.endsWith("px") || value.endsWith("em")) {
            return value;
        }
        
        // Convert rem to em (assuming 1rem = 1em for simplicity)
        if (value.endsWith("rem")) {
            return value.replace("rem", "em");
        }

        // For other units, try to use as-is (JavaFX may not support all)
        return value;
    }

    /**
     * Converts a color value to JavaFX format.
     */
    private static String convertColor(String value) {
        // JavaFX supports hex, rgb, rgba, and named colors
        if (value.startsWith("#") || value.startsWith("rgb") || value.startsWith("hsl")) {
            return value;
        }
        
        // Check if it's a named color
        try {
            javafx.scene.paint.Color.web(value);
            return value;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Converts an angle value to JavaFX format.
     */
    private static String convertAngle(String value) {
        // JavaFX primarily uses degrees
        if (value.endsWith("deg")) {
            return value;
        }
        
        // Convert radians to degrees
        if (value.endsWith("rad")) {
            try {
                double radians = Double.parseDouble(value.substring(0, value.length() - 3));
                double degrees = Math.toDegrees(radians);
                return degrees + "deg";
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Convert turns to degrees
        if (value.endsWith("turn")) {
            try {
                double turns = Double.parseDouble(value.substring(0, value.length() - 4));
                double degrees = turns * 360;
                return degrees + "deg";
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return value;
    }

    /**
     * Converts an image value to JavaFX format.
     */
    private static String convertImage(String value) {
        // JavaFX supports linear-gradient and radial-gradient
        if (value.startsWith("linear-gradient(") || value.startsWith("radial-gradient(")) {
            return value;
        }
        
        // Conic gradients not supported in JavaFX
        if (value.startsWith("conic-gradient(")) {
            return null;
        }

        return value;
    }

    /**
     * Normalizes a font family name for JavaFX.
     */
    private static String normalizeFontFamily(String value) {
        // Remove quotes if present and normalize
        return value.replaceAll("^['\"]|['\"]$", "");
    }

    /**
     * Converts a background size value to JavaFX format.
     */
    private static String convertBackgroundSize(String value) {
        // JavaFX doesn't have direct background-size property
        // This would need custom handling
        return value;
    }

    /**
     * Result of processing a type-hinted value.
     *
     * @param typeHint the detected or explicit type hint
     * @param value the actual value without type hint
     * @param originalValue the original arbitrary value with type hint
     */
    public record TypeHintResult(
        TypeHint typeHint,
        String value,
        String originalValue
    ) {
        /**
         * Gets the auto-detected type if no explicit hint was provided.
         *
         * @return the explicit type hint, or the auto-detected type, or null
         */
        public TypeHint getEffectiveType() {
            if (typeHint != null) {
                return typeHint;
            }
            return TypeHintProcessor.autoDetectType(value);
        }

        /**
         * Converts the value to a JavaFX-compatible CSS value.
         *
         * @return the JavaFX CSS value, or null if not convertible
         */
        public String toJavaFxValue() {
            TypeHint effectiveType = getEffectiveType();
            return TypeHintProcessor.toJavaFxValue(effectiveType, value);
        }
    }
}
