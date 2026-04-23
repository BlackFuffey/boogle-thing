import java.util.*;
import java.io.*;

import boogle.*;

public class Main {
    
    static final Scanner console = new Scanner(System.in);

    public static void main(String[] args) throws FileNotFoundException, IOException {
        try {
            Terminal.enterAltBuffer();
            GameOptions options = menu();
        } finally {
            Terminal.exitAltBuffer();
            Terminal.showCursor();
        }
    }

    private static class GameOptions {
        LinkedList<Player> playerlist;
        int minWordLength;
        int winScore;
    }
    static GameOptions menu() throws FileNotFoundException, IOException{
        GameOptions options = new GameOptions();
        options.playerlist = new LinkedList<>();
        options.winScore = 0;
        options.minWordLength = 0;

        for (;;) {
            Terminal.hideCursor();
            Terminal.clearScreen();
            printTitleScreen();
            console.nextLine();

            menu:for(;;) {
                Terminal.clearScreen();
                printMenuScreen(options);
                Terminal.showCursor();
                System.out.print("\n\nboogle> ");

                String[] cmd = padArray(console.nextLine()
                                            .trim()
                                        .split(" ", 2),
                               2, "");
                Terminal.hideCursor();

                switch(cmd[0].toLowerCase()) {
                    case "add":
                        String[] addArgs = padArray(cmd[1].split(" ", 2), 2, "");

                        if (addArgs[0].toLowerCase().equals("human")) {
                            options.playerlist.add(new ConsolePlayer(addArgs[1], console));
                            System.out.printf("Human player '%s' is now player #%d", addArgs[1], options.playerlist.size());
                            break;
                        }

                        if (addArgs[0].toLowerCase().equals("ai")) {
                            options.playerlist.add(new AIPlayer(addArgs[1], AIPlayer.Level.NORMAL));
                            System.out.printf("AI player '%s' is now player #%d", addArgs[1], options.playerlist.size());
                            break;    
                        }

                        System.out.printf("Invalid player type '%s', use either 'human' or 'AI'", addArgs[0]);
                    break;
                        
                    case "rename":
                        String[] renameArgs = padArray(cmd[1].split(" ", 2), 2, "");

                        Player target = resolvePlayerNum(options.playerlist, renameArgs[0]);

                        if (target == null) {
                            System.out.printf("Got invalid player number '%s'\n", renameArgs[0]);
                            break;
                        }

                        target.setName(renameArgs[1]);
                        System.out.println("Name updated");
                    break;
                        
                    case "start":
                        if (options.playerlist.size() != 0)
                            return options;
                        System.out.println("Cannot start game with 0 players!");
                    break;

                    case "quit": break menu;

                    default:
                        System.out.printf("I don't understand '%s'.\n See the 'Commands' section on top for list of commands.\n", cmd[0]);
                    break;
                }

                System.out.println("\n-- Press enter to continue --");
                console.nextLine();
            }

        }
        
    }

    static <T> T[] padArray(T[] arr, int padLength, T padWith) {
        if (arr.length >= padLength)
            return arr;

        T[] padded = Arrays.copyOf(arr, padLength);

        for (int i = arr.length; i < padded.length; i++) {
            padded[i] = padWith;
        }

        return padded;
    }

    static Player resolvePlayerNum(LinkedList<Player> playerlist, String playerNumStr) {
        playerNumStr = playerNumStr.toLowerCase();

        try {
            int playerNum = Integer.parseInt(playerNumStr.replace("p", ""));

            if (playerNum < 1 || playerNum > playerlist.size())
                throw new Exception();

            return playerlist.get(playerNum-1);
        } catch (Exception e) {
            return null;
        }
    }

    static void printTitleScreen() throws FileNotFoundException, IOException {
        FileInputStream logo = new FileInputStream("logo.txt");
        logo.transferTo(System.out);
        logo.close();

        System.out.println("\n-- Press enter to start a new game, or press Ctrl-C to quit --\n");
    }

    static void printMenuScreen(GameOptions options) {
        System.out.println(
            "==== Commands ====\n" +
            "set <option> <value>\t--\t Modify game option\n" +
            "rename <P#> <name>\t--\t Rename player\n" +
            "level <P#> <level>\t--\t Change AI player level\n" +
            "move <P#> <P#>\t\t--\t Change player turn order\n" +
            "add <human|AI> <name>\t--\t Add player\n" +
            "remove <P#>\t\t--\t Remove player\n" +
            "start\t\t\t--\t Start game\n" +
            "quit\t\t\t--\t Return to title\n" +
            "\n"+
            "==== Game Options ====\n" +
            "[win_score] Winning Score: " + (options.winScore==0 ? "Endless" : options.winScore) + "\n" +
            "[min_word_length] Minimum Word Length: " + (options.minWordLength==0 ? "No minimum word length limit" : options.minWordLength) + "\n" +
            "\n" +
            "==== Players ====\n"

        );

        int i = 0;
        for (Player player : options.playerlist) {
            System.out.printf("\tP%d:", (i+1));

            if (player instanceof ConsolePlayer)
                System.out.printf("\tType: Human\tName: %s\n", player.getName());
            else
                System.out.printf("\tType: AI\tName: %s\tLevel: %d\n", player.getName(), ((AIPlayer) player).getLevel().getValue());

            i++;
        }

        if (i == 0) {
            System.out.println("(No player, use 'add' command to add a player)");
        }
    }

}


