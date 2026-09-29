package io.github.yasmramos.tailwindfx.core;

/**
 * Utility for escaping CSS class-name identifiers so that Tailwind utility strings (which contain
 * characters such as {@code /}, {@code :}, {@code .}, {@code [}, {@code ]}, {@code %}, {@code #},
 * {@code !} and leading digits) produce valid CSS selectors.
 *
 * <p>This is the single source of truth shared by the runtime catalog generator ({@code TwCatalog})
 * and the build-time Maven plugin ({@code TailwindCssMojo}).
 *
 * @see <a href="https://www.w3.org/TR/CSS21/syndata.html#characters">CSS 2.1 - Characters and
 *     case</a>
 */
public final class CssIdentEscaper {

  private CssIdentEscaper() {
    // Utility class, no instantiation
  }

  /**
   * Escapes a raw Tailwind class name into a valid CSS identifier suitable for use after a leading
   * dot in a class selector (e.g. {@code w-1/2 -> w-1\/2}).
   *
   * <p>Rules applied:
   *
   * <ul>
   *   <li>The backslash itself is escaped first ({@code \ -> \\}).
   *   <li>Special characters are escaped with a preceding backslash: {@code / : . [ ] % # ! ( ) , =
   *       + ~ ^ | { } $ @ & * ' " ? < > ;}
   *   <li>A leading digit is escaped in CSS form ({@code 1 -> \31 }), as is a leading hyphen
   *       followed by a digit ({@code -1... -> \-1... } handled via digit escape).
   *   <li>A lone leading hyphen is escaped ({@code -m-4 -> \-m-4}).
   * </ul>
   *
   * @param className the raw class name; may be {@code null}
   * @return the escaped identifier, or an empty string when input is {@code null}/empty
   */
  public static String escape(String className) {
    if (className == null || className.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder(className.length() + 8);
    for (int i = 0; i < className.length(); i++) {
      char c = className.charAt(i);
      boolean atStart = (i == 0);

      if (c == '\\') {
        sb.append("\\\\");
      } else if (isSpecialChar(c)) {
        sb.append('\\').append(c);
      } else if (Character.isDigit(c) && atStart) {
        // Leading digit: CSS escape form "\31 " (hex code point + space terminator).
        sb.append('\\').append(Integer.toHexString(c)).append(' ');
      } else if (c == '-' && atStart) {
        // A leading "-" is only special when followed by nothing or a digit;
        // escaping it unconditionally is safe and keeps identifiers valid.
        sb.append("\\-");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Builds a full CSS class selector (".escaped-name") from a raw class name.
   *
   * @param className the raw class name
   * @return the escaped selector, e.g. {@code ".w-1\/2"}
   */
  public static String toSelector(String className) {
    return "." + escape(className);
  }

  /** Returns whether the character must be escaped inside a CSS identifier. */
  private static boolean isSpecialChar(char c) {
    switch (c) {
      case '/':
      case ':':
      case '.':
      case '[':
      case ']':
      case '%':
      case '#':
      case '!':
      case '(':
      case ')':
      case ',':
      case '=':
      case '+':
      case '~':
      case '^':
      case '|':
      case '{':
      case '}':
      case '$':
      case '@':
      case '&':
      case '*':
      case '\'':
      case '"':
      case '?':
      case '<':
      case '>':
      case ';':
        return true;
      default:
        return false;
    }
  }
}
