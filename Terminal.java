public class Terminal {
    static void enterAltBuffer() {
        System.out.print("\u001B[?1049h");
        System.out.flush();
    }

    static void exitAltBuffer() {
        System.out.print("\u001B[?1049l");
        System.out.flush();
    }

    static void clearScreen() {
        System.out.print("\u001B[2J\u001B[H");
        System.out.flush();
    }

    static void hideCursor() {
        System.out.print("\u001B[?25l");
        System.out.flush();
    }

    static void showCursor() {
        System.out.print("\u001B[?25h");
        System.out.flush();
    }
}
