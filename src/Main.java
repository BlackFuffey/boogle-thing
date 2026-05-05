import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import boogle.core.Launcher.GameOptions;
import boogle.core.GameUI;
import boogle.core.Launcher;
import boogle.ui.tui.TextUI;
import boogle.ui.gui.GraphicalUI;

public class Main {
    
    static final Scanner console = new Scanner(System.in);

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length < 2) {
            System.err.println("Usage: boogle <tui|gui>");
            System.exit(1);
        }

        GameUI ui;

        if (args[1].equalsIgnoreCase("tui")) {
            ui = new TextUI();
        }

        else if (args[1].equalsIgnoreCase("gui")) {
            ui = new GraphicalUI();
        }

        else {
            System.err.printf("Unknown UI type '%s'\n", args[1]);
            System.err.println("Usage: boogle <tui|gui>");
            ui = null;  // so compiler doesnt complain about uninitialized variable
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
        }
    }
}


