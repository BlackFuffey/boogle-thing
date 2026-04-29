import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import boogle.*;
import boogle.util.Terminal;

public class Main {
    
    static final Scanner console = new Scanner(System.in);

    public static void main(String[] args) throws FileNotFoundException, IOException {
        try {
            GameOptions options = new GameOptions();
            options.playerlist = new ArrayList<>();
            options.winScore = 0;
            options.minWordLength = 0;
            options.wordlistPath = "wordlist.txt";
            options.customBoard = null;

            for (;;) {
                for (int i = 0; i < options.playerlist.size(); i++) {
                    Player player = options.playerlist.get(i);
                    if (player instanceof AIPlayer) {
                        AIPlayer oldAI = (AIPlayer) player;
                        AIPlayer newAI = new AIPlayer(oldAI.getName(), oldAI.getLevel());

                        options.playerlist.set(i, newAI);
                    }
                }

                Terminal.enterAltBuffer();
                options = menu(options);

                if (options == null)
                return;

                HashSet<String> dictionary = new HashSet<>();

                Scanner wordlist = new Scanner(new File(options.wordlistPath));
                while (wordlist.hasNext()) {
                    String word = wordlist.nextLine();

                    if (word.length() < options.minWordLength)
                        continue;

                    dictionary.add(word.toUpperCase());
                }
                wordlist.close();

                GameMaster gm = new GameMaster(options.playerlist, dictionary, options.customBoard, options.minWordLength, options.winScore, console);
                gm.begin();
            }
        } finally {
            Terminal.exitAltBuffer();
            Terminal.showCursor();
        }
    }

    private static class GameOptions {
        ArrayList<Player> playerlist;
        String wordlistPath;
        char[][] customBoard;
        int minWordLength;
        int winScore;
    }
    static GameOptions menu(GameOptions options) throws FileNotFoundException, IOException{

        for (;;) {
            Terminal.hideCursor();
            Terminal.clearScreen();
            printTitleScreen();
            console.nextLine();

            for(;;) {
                Terminal.clearScreen();
                printMenuScreen(options);
                Terminal.showCursor();
                System.out.print("\n\nboogle> ");

                String[] cmd = padArray(console.nextLine().trim().split(" ", 2), 2, "");
                Terminal.hideCursor();

                switch(cmd[0].toLowerCase()) {
                    case "set": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");
                        System.out.print(
                            setOption(options, args[0], args[1]) ?
                            "Game option updated\n" : ""
                        );
                    } break;

                    case "add": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        if (args[0].toLowerCase().equals("human")) {
                            options.playerlist.add(new ConsolePlayer(args[1], console));
                            System.out.printf("Human player '%s' is now player #%d", args[1], options.playerlist.size());
                            break;
                        }

                        if (args[0].toLowerCase().equals("ai")) {
                            options.playerlist.add(new AIPlayer(args[1], AIPlayer.Level.NORMAL));
                            System.out.printf("AI player '%s' is now player #%d", args[1], options.playerlist.size());
                            break;    
                        }

                        System.out.printf("Invalid player type '%s', use either 'human' or 'AI'", args[0]);
                    } break;
                        
                    case "rename": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        Player target = resolvePlayerNum(options.playerlist, args[0]);

                        if (target == null) {
                            System.out.printf("Invalid player number '%s'\n", args[0]);
                            break;
                        }

                        target.setName(args[1]);
                        System.out.println("Name updated");
                    } break;

                    case "level": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        Player target = resolvePlayerNum(options.playerlist, args[0]);

                        if (target == null) {
                            System.out.printf("Invalid player number '%s'\n", args[0]);
                            break;
                        }

                        if (!(target instanceof AIPlayer)) {
                            System.out.println("Specified player is not an AI");
                            break;
                        }

                        try {
                            ((AIPlayer) target).setLevel(AIPlayer.Level.fromValue(
                                Integer.parseInt(args[1])
                            ));
                            System.out.println("AI level updated");
                        } catch (Exception e) {
                            System.out.println("Invalid AI level, use an integer between 1-5");
                        }
                    } break;

                    case "move": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");
                        int from, to;

                        try { from = parsePlayerNum(options.playerlist, args[0]); }
                        catch (Exception e) {
                            System.out.println("Invalid target player number");
                            break;
                        }

                        try { to = parsePlayerNum(options.playerlist, args[1]); }
                        catch (Exception e) {
                            System.out.println("Invalid destination player number");
                            break;
                        }

                        Player target = options.playerlist.get(from);

                        options.playerlist.remove(from);
                        options.playerlist.add(to, target);
                        System.out.println("Player moved");
                    } break;
                        
                    case "remove": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        try {
                            int target = parsePlayerNum(options.playerlist, args[0]);
                            options.playerlist.remove(target);
                            System.out.println("Player removed");
                        } catch (Exception e) {
                            System.out.println("Invalid target player number");
                        }
                    } break;

                    case "start": {
                        if (options.playerlist.size() != 0)
                            return options;
                        System.out.println("Cannot start game with 0 players!");
                    } break;

                    case "quit": { return null; }

                    case "help": {
                        Terminal.clearScreen();
                        printTutorial();
                    } break;

                    default: {
                        System.out.printf("I don't understand '%s'.\nSee the 'Commands' section on top for list of commands.\n", cmd[0]);
                    } break;
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

    static Player resolvePlayerNum(List<Player> playerlist, String playerNumStr) {
        try {
            return playerlist.get(parsePlayerNum(playerlist, playerNumStr));
        } catch (Exception e) {
            return null;
        }
    }

    static int parsePlayerNum(List<Player> playerlist, String playerNumStr) {
        playerNumStr = playerNumStr.toLowerCase();

        int playerNum = Integer.parseInt(playerNumStr.replace("p", ""))-1;

        if (playerNum < 0 || playerNum >= playerlist.size())
            throw new IllegalArgumentException();

        return playerNum;
    }

    static boolean setOption(GameOptions options, String key, String value) {
        switch(key) {
            case "win_score":
                try { 
                    options.winScore = Integer.parseInt(value);
                    if (options.winScore < 0)
                        throw new Exception();
                }
                catch(Exception e) {
                    System.out.println("Invalid winning score value");
                    System.out.println("Enter an integer bigger than 0, or use 0 for endless mode");
                    return false;
                }
            break;

            case "min_word_length":
                try { 
                    options.minWordLength = Integer.parseInt(value);
                    if (options.minWordLength < 0)
                        throw new Exception();
                } catch(Exception e) {
                    System.out.println("Invalid minimum word length value");
                    System.out.println("Enter an integer bigger than 0, or use 0 for no limit");
                    return false;
                }
            break;

            case "wordlist":
                try { 
                    (new FileReader(value)).close();
                    options.wordlistPath = value;
                } catch (IOException e) {
                    System.out.println("Unable to use file: "+e.getMessage());
                    return false;
                }
            break;

            case "board": {
                if (value.isEmpty()) {
                    options.customBoard = null;
                    break;
                }

                char[][] customBoard = loadGameboardFile(value);

                if (customBoard != null) {
                    options.customBoard = customBoard;
                    break;
                }

                System.out.println("Unable to use the specified file");
                System.out.println("Leave path empty if you wish for board to be randomly generated");
                return false;
            }

            default:
                System.out.println("I don't know about that option");
                System.out.println("TIP: use the part inside of square bracket in front of the option you wish to modify");
                return false;
        }

        return true;
    }

    static void printTutorial() throws IOException {
        FileInputStream logo = new FileInputStream("ui/tutorial.txt");
        logo.transferTo(System.out);
        logo.close();
    }

    static void printTitleScreen() throws FileNotFoundException, IOException {
        FileInputStream logo = new FileInputStream("ui/logo.txt");
        logo.transferTo(System.out);
        logo.close();

        System.out.println("\n\t\t\t-- Press enter to continue --\n");
    }

    static void printMenuScreen(GameOptions options) throws IOException{
        System.out.printf(
            new String(Files.readAllBytes(Paths.get("ui/menu.txt"))),
            options.winScore == 0 ? "Endless" : ""+options.winScore,
            options.minWordLength == 0 ? "No minimum word length limit" : ""+options.minWordLength,
            options.wordlistPath,
            options.customBoard == null ? "Generated" : "Custom"
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

    static char[][] loadGameboardFile(String path) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));

            char[][] result = new char[lines.size()][];

            int width = -1;
            for (int i = 0; i < lines.size(); i++) {
                String[] chars = lines.get(i).split("\\s+");
                
                if (width == -1)
                    width = chars.length;
                else if (width != chars.length)
                    throw new IOException(
                        String.format(
                            "Malformed gameboard file: row %d is not of the expected length %d (got %d instead)",
                            i+1, width, chars.length
                        )
                    );

                char[] row = new char[chars.length];

                for (int j = 0; j < chars.length; j++) {
                    if (chars[j].length() != 1)
                        throw new IOException(
                            String.format(
                                "Malformed gameboard file: found multiple characters in one grid at (%d, %d)",
                                i+1, j+1
                            )
                        );

                    row[j] = Character.toUpperCase(chars[j].charAt(0));
                }

                result[i] = row;
            }

            return result;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

}


