package boogle.util;

public class Terminal {
    public static void enterAltBuffer() {
        System.out.print("\u001B[?1049h");
        System.out.flush();
    }

    public static void exitAltBuffer() {
        System.out.print("\u001B[?1049l");
        System.out.flush();
    }

    public static void clearScreen() {
        System.out.print("\u001B[2J\u001B[H");
        System.out.flush();
    }

    public static void hideCursor() {
        System.out.print("\u001B[?25l");
        System.out.flush();
    }

    public static void showCursor() {
        System.out.print("\u001B[?25h");
        System.out.flush();
    }
}
