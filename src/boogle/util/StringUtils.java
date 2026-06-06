/*
 * File: StringUtils.java
 * Author: Ethan Ding
 * Description: Provides string padding and truncation helpers used when formatting terminal output.
 */

package boogle.util;

/**
 * Small string formatting helpers used by the text UI.
 */
public class StringUtils {
    /**
     * Prevents construction of the static string utility class.
     */
    private StringUtils() {
    }
    /**
     * Left-pads a string until it reaches a requested length.
     *
     * @param s source string
     * @param length target minimum length
     * @param pad character to repeat before {@code s}
     * @return padded string, or {@code s} unchanged if it is already long enough
     */
    public static String padStart(String s, int length, char pad) {
        if (s.length() >= length) return s;
        return String.valueOf(pad).repeat(length - s.length()) + s;
    }

    /**
     * Right-pads a string until it reaches a requested length.
     *
     * @param s source string
     * @param length target minimum length
     * @param pad character to repeat after {@code s}
     * @return padded string, or {@code s} unchanged if it is already long enough
     */
    public static String padEnd(String s, int length, char pad) {
        if (s.length() >= length) return s;
        return s + String.valueOf(pad).repeat(length - s.length());
    }

    /**
     * Shortens a string and appends an ellipsis when it exceeds a limit.
     *
     * @param s source string; {@code null} is returned as {@code null}
     * @param maxLength maximum returned length, including the ellipsis
     * @return original or truncated string
     */
    public static String truncateWithEllipsis(String s, int maxLength) {
        if (s == null || s.length() <= maxLength) {
            return s;
        }
        return s.substring(0, maxLength - 3) + "...";
    }
}
