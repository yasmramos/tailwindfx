package io.github.yasmramos.tailwindfx.style;

/**
 * TypeHint — Represents type hints for arbitrary values in Tailwind CSS v4.
 *
 * <p>Type hints help disambiguate arbitrary values when the utility could accept multiple types.
 * Format: prefix-[type:value] where type can be: length, percentage, number, color, angle, etc.
 *
 * <p>Examples:
 * <ul>
 *   <li>w-[length:320px] — explicitly a length</li>
 *   <li>text-[percentage:50%] — explicitly a percentage</li>
 *   <li>rotate-[angle:45deg] — explicitly an angle</li>
 *   <li>bg-[color:#ff0000] — explicitly a color</li>
 *   <li>opacity-[number:0.5] — explicitly a number</li>
 * </ul>
 */
public enum TypeHint {
    LENGTH("length"),
    PERCENTAGE("percentage"),
    NUMBER("number"),
    COLOR("color"),
    ANGLE("angle"),
    URL("url"),
    IMAGE("image"),
    FAMILY_NAME("family-name"),
    LINE_WIDTH("line-width"),
    SHAPE("shape"),
    POSITION("position"),
    BG_SIZE("bg-size");

    private final String hint;

    TypeHint(String hint) {
        this.hint = hint;
    }

    public String getHint() {
        return hint;
    }

    /**
     * Parses a type hint from an arbitrary value string.
     * @param arbitraryValue The arbitrary value string (e.g., "length:320px")
     * @return The TypeHint if found, or null if no hint is present
     */
    public static TypeHint parse(String arbitraryValue) {
        if (arbitraryValue == null || arbitraryValue.isBlank()) {
            return null;
        }

        int colonIndex = arbitraryValue.indexOf(':');
        if (colonIndex == -1) {
            return null; // No type hint present
        }

        String hintPart = arbitraryValue.substring(0, colonIndex).trim();
        
        for (TypeHint typeHint : values()) {
            if (typeHint.hint.equalsIgnoreCase(hintPart)) {
                return typeHint;
            }
        }

        return null;
    }

    /**
     * Extracts the actual value from a type-hinted arbitrary value.
     * @param arbitraryValue The arbitrary value string (e.g., "length:320px")
     * @return The value part without the type hint (e.g., "320px"), or the original if no hint
     */
    public static String extractValue(String arbitraryValue) {
        if (arbitraryValue == null || arbitraryValue.isBlank()) {
            return arbitraryValue;
        }

        int colonIndex = arbitraryValue.indexOf(':');
        if (colonIndex == -1) {
            return arbitraryValue; // No type hint present
        }

        return arbitraryValue.substring(colonIndex + 1).trim();
    }

    /**
     * Checks if an arbitrary value contains a type hint.
     * @param arbitraryValue The arbitrary value string
     * @return true if a type hint is present, false otherwise
     */
    public static boolean hasTypeHint(String arbitraryValue) {
        return parse(arbitraryValue) != null;
    }
}
