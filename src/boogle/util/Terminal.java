package boogle.util;

/**
 * Utility class for low‑level terminal control using ANSI escape sequences.
 * These static methods allow the application to enter and exit the
 * alternative screen buffer, clear the screen and toggle cursor visibility.
 * Each method writes the appropriate control code to {@link System#out}
 * and immediately flushes the stream. Methods do not return until the
 * control code has been emitted.
 */

public class Terminal {
    /**
     * Switches to the terminal’s alternate screen buffer. While active the
     * main screen contents are preserved and will be restored when
     * {@link #exitAltBuffer()} is called. This is commonly used by full‑screen
     * terminal applications to avoid clobbering the user’s shell.
     */
    public static void enterAltBuffer() {
        System.out.print("\u001B[?1049h");
        System.out.flush();
    }

    /**
     * Restores the normal screen buffer after a call to
     * {@link #enterAltBuffer()}. The contents of the alternate buffer are
     * discarded and the previous screen is revealed.
     */
    public static void exitAltBuffer() {
        System.out.print("\u001B[?1049l");
        System.out.flush();
    }

    /**
     * Clears the entire screen and moves the cursor to the home position
     * (row 1, column 1). This method writes the ANSI control sequence
     * {@code ESC[2J} to clear the screen followed by {@code ESC[H} to
     * reposition the cursor.
     */
    public static void clearScreen() {
        System.out.print("\u001B[2J\u001B[H");
        System.out.flush();
    }

    /**
     * Hides the cursor by emitting the ANSI escape sequence {@code ESC[?25l}.
     * The cursor will remain hidden until {@link #showCursor()} is called.
     */
    public static void hideCursor() {
        System.out.print("\u001B[?25l");
        System.out.flush();
    }

    /**
     * Shows the cursor by emitting the ANSI escape sequence {@code ESC[?25h}.
     * If the cursor was already visible this method has no effect.
     */
    public static void showCursor() {
        System.out.print("\u001B[?25h");
        System.out.flush();
    }
}
