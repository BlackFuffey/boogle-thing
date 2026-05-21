package boogle.util;

/**
 * Utility class containing static helper methods for manipulating and
 * formatting strings. These methods provide simple padding and truncation
 * functionality that is commonly used throughout the application. The class
 * cannot be instantiated.
 */

public class StringUtils {
    /**
     * Returns a new string padded on the left with the specified character
     * until it reaches the desired length. If the original string is longer
     * than or equal to {@code length} it is returned unchanged.
     *
     * @param s the original string to pad
     * @param length the desired total length of the output string
     * @param pad the character to insert before {@code s}
     * @return a string of length {@code length} where {@code s} is
     *         right‑aligned and padded on the left with {@code pad}
     */
    public static String padStart(String s, int length, char pad) {
        if (s.length() >= length) return s;
        return String.valueOf(pad).repeat(length - s.length()) + s;
    }

    /**
     * Returns a new string padded on the right with the specified character
     * until it reaches the desired length. If the original string is longer
     * than or equal to {@code length} it is returned unchanged.
     *
     * @param s the original string to pad
     * @param length the desired total length of the output string
     * @param pad the character to append after {@code s}
     * @return a string of length {@code length} where {@code s} is
     *         left‑aligned and padded on the right with {@code pad}
     */
    public static String padEnd(String s, int length, char pad) {
        if (s.length() >= length) return s;
        return s + String.valueOf(pad).repeat(length - s.length());
    }

    /**
     * Truncates a string to at most {@code maxLength} characters. If
     * truncation is necessary the last three characters of the result will be
     * an ellipsis ("...") to indicate that content was removed. Null inputs
     * are returned unchanged.
     *
     * @param s the string to truncate; may be {@code null}
     * @param maxLength the maximum allowed length including the ellipsis
     * @return the original string if its length is within the limit, or a
     *         truncated version suffixed with "..." otherwise
     */
    public static String truncateWithEllipsis(String s, int maxLength) {
        if (s == null || s.length() <= maxLength) {
            return s;
        }
        return s.substring(0, maxLength - 3) + "...";
    }
}
