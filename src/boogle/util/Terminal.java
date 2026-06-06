/*
 * File: Terminal.java
 * Author: Ethan Ding
 * Description: Provides ANSI terminal escape helpers for screen clearing, cursor visibility, and alternate buffer control.
 */

package boogle.util;

/**
 * ANSI escape helpers for the terminal user interface.
 */
public class Terminal {
    /**
     * Prevents construction of the static terminal utility class.
     */
    private Terminal() {
    }
    /**
     * Switches the terminal into the alternate screen buffer.
     */
    public static void enterAltBuffer() {
        System.out.print("\u001B[?1049h");
        System.out.flush();
    }

    /**
     * Returns the terminal from the alternate screen buffer to the normal buffer.
     */
    public static void exitAltBuffer() {
        System.out.print("\u001B[?1049l");
        System.out.flush();
    }

    /**
     * Clears the terminal screen and moves the cursor to the home position.
     */
    public static void clearScreen() {
        System.out.print("\u001B[2J\u001B[H");
        System.out.flush();
    }

    /**
     * Hides the terminal cursor.
     */
    public static void hideCursor() {
        System.out.print("\u001B[?25l");
        System.out.flush();
    }

    /**
     * Shows the terminal cursor.
     */
    public static void showCursor() {
        System.out.print("\u001B[?25h");
        System.out.flush();
    }
}
