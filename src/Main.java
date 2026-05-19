import java.util.*;

import boogle.core.Launcher.GameOptions;
import boogle.core.GameUI;
import boogle.core.Launcher;
import boogle.ui.tui.TextUI;
import boogle.ui.gui.GraphicalUI;

public class Main {
    
    static final Scanner console = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: boogle <tui|gui>");
            System.exit(1);
        }

        GameUI ui;

        if (args[0].equalsIgnoreCase("tui")) {
            ui = new TextUI();
        }


      else if (args[0].equalsIgnoreCase("gui")) {//wip
        System.out.println("NOTICE: testing only (WIP)");
        ui = new GraphicalUI();
      }

        else {
            System.err.printf("Unknown UI type '%s'\n", args[0]);
            System.err.println("Usage: boogle <tui|gui>");
            ui = null; // to keep compiler from complaining uninitialized variable
            System.err.flush();
            System.exit(1);
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


