package boogle.util;

public class StringUtils {
    public static String padStart(String s, int length, char pad) {
        if (s.length() >= length) return s;
        return String.valueOf(pad).repeat(length - s.length()) + s;
    }

    public static String padEnd(String s, int length, char pad) {
        if (s.length() >= length) return s;
        return s + String.valueOf(pad).repeat(length - s.length());
    }

    public static String truncateWithEllipsis(String s, int maxLength) {
        if (s == null || s.length() <= maxLength) {
            return s;
        }
        return s.substring(0, maxLength - 3) + "...";
    }
}
