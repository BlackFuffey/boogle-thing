import java.util.*;

import boogle.core.Launcher.GameOptions;
import boogle.core.GameUI;
import boogle.core.Launcher;
import boogle.ui.tui.TextUI;
import boogle.ui.gui.GraphicalUI;

/**
 * Command-line entry point for Boogle.
 *
 * <p>The first command-line argument selects the user-interface backend. The
 * text UI is the stable terminal implementation, while the GUI path starts the
 * Swing-based implementation that is marked as work in progress by the
 * application itself. After the UI is chosen, this class builds the default
 * {@link boogle.core.Launcher.GameOptions} object and hands control to
 * {@link boogle.core.Launcher}.</p>
 */
public class Main {
    /**
     * Prevents construction of the command-line entry point.
     */
    private Main() {
    }
    
    static final Scanner console = new Scanner(System.in);

    /**
     * Starts Boogle with either the text or graphical UI.
     *
     * <p>Valid UI names are {@code tui} and {@code gui}. The launcher is
     * initialized with no players, no winning score, no minimum word length, the
     * default {@code wordlist.txt}, and a generated board unless the user changes
     * those settings in the lobby.</p>
     *
     * @param args command-line arguments; {@code args[0]} must be the UI type
     * @throws Exception if UI construction or launcher startup fails before the
     *         application's top-level error handler can process the error
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
              System.out.println("NOTICE: testing only (WIP)");
              ui = new GraphicalUI();
                //throw new UnsupportedOperationException("GUI doesnt compile so i disabled it for now");
            }break;

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


