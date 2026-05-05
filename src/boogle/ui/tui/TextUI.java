package boogle.ui.tui;

import boogle.core.*;
import boogle.core.Launcher.GameOptions;
import boogle.player.*;
import boogle.util.Terminal;

import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class TextUI implements GameUI {
    
    private Scanner console = new Scanner(System.in);

    public TextUI() {
        Terminal.enterAltBuffer();
    }
    
    @Override
    public void close() {
        Terminal.exitAltBuffer();
        Terminal.showCursor();
    }

    public boolean lobby(GameOptions options) {
        try { for (;;) {
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
                            return true;
                        System.out.println("Cannot start game with 0 players!");
                    } break;

                    case "quit": { return false; }

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

        } } catch (IOException e) {
            e.printStackTrace();
            return false;
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

                char[][] customBoard = Launcher.loadGameboardFile(value);

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

    private static void printTutorial() throws IOException {
        InputStream asset = TextUI.class.getClassLoader().getResourceAsStream("asset/tutorial.txt");
        asset.transferTo(System.out);
        asset.close();
    }

    private static void printTitleScreen() throws IOException {
        InputStream logo = TextUI.class.getClassLoader().getResourceAsStream("asset/logo.txt");
        logo.transferTo(System.out);
        logo.close();

        System.out.println("\n\t\t\t-- Press enter to continue --\n");
    }

    private static void printMenuScreen(GameOptions options) throws IOException {
        String template = new String(
            TextUI.class.getClassLoader().getResourceAsStream("asset/menu.txt").readAllBytes(),
            StandardCharsets.UTF_8
        );

        System.out.printf(
            template,
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

    public void newTurn()

    public void confirm() {
        console.nextLine();
    }
}
