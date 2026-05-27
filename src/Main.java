import java.util.*;

import boogle.core.Launcher.GameOptions;
import boogle.core.GameUI;
import boogle.core.Launcher;
import boogle.ui.tui.TextUI;
//import boogle.ui.gui.GraphicalUI;

/**
 * Entry point for the Boogle application. This class is responsible for
 * parsing the command‑line arguments, constructing the appropriate
 * {@link boogle.core.GameUI} implementation (either the text‑based
 * {@link boogle.ui.tui.TextUI} or the experimental
 * {@link boogle.ui.gui.GraphicalUI}) and then launching the game. It
 * establishes a set of default {@link boogle.core.Launcher.GameOptions}
 * including the list of players, winning score and minimum word length and
 * delegates execution to the {@link boogle.core.Launcher}.
 */
public class Main {
    
    /**
     * Shared {@link Scanner} used to read input from {@code System.in}. This
     * scanner is intentionally static so that both the lobby and game phases
     * share a single instance and avoid accidentally closing standard input.
     */
    static final Scanner console = new Scanner(System.in);

    /**
     * Launches the Boogle game. A single argument is expected to indicate
     * which user interface implementation should be used. Valid values are
     * {@code "tui"} for the terminal (text) interface and {@code "gui"}
     * for the graphical interface. When an invalid argument is supplied the
     * method prints usage information to standard error and terminates. On a
     * valid argument the method constructs a {@link boogle.core.Launcher.GameOptions}
     * instance with sensible defaults (no winning score, no minimum word length,
     * the built‑in word list and a randomised board) and then instantiates a
     * {@link boogle.core.Launcher} with the chosen UI. If any exception is
     * thrown during startup or gameplay it is logged and the JVM exits with a
     * non‑zero status code.
     *
     * @param args command‑line arguments. The first element selects the UI type
     *             ({@code "tui"} or {@code "gui"}). Additional elements are
     *             ignored.
     * @throws Exception if an unexpected error occurs while starting or running
     *                   the game. Checked exceptions from the UI or launcher
     *                   propagate through this method.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: boogle <tui|gui>");
            System.exit(1);
        }

        args[0] = args[0].toLowerCase();

        GameUI ui;

        switch (args[0]) {
            case "tui": {
                ui = new TextUI();
            } break;

            case "gui": {
              //System.out.println("NOTICE: testing only (WIP)");
              //ui = new GraphicalUI();
                throw new UnsupportedOperationException("GUI doesnt compile so i disabled it for now");
            }

            default: {
                System.err.printf("Unknown UI type '%s'\n", args[0]);
                System.err.println("Usage: boogle <tui|gui>");
                ui = null; // to keep compiler from complaining uninitialized variable
                System.err.flush();
                System.exit(1);
            } break;
        }

        try (ui) {
            GameOptions options = new GameOptions();
            options.playerlist = new ArrayList<>();
            options.winScore = 0;
            options.minWordLength = 0;
            options.wordlistPath = "wordlist.txt";
            options.customBoard = null;

            Launcher launcher = new Launcher(ui);

            launcher.start(options);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}


