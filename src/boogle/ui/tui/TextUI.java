package boogle.ui.tui;

import boogle.core.*;
import boogle.core.Launcher.GameOptions;
import boogle.player.*;
import boogle.util.StringUtils;
import boogle.util.Terminal;
import boogle.sound.GameSound;

import java.util.*;

import javax.sound.sampled.Clip;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TextUI implements GameUI {
    
    private Scanner console = new Scanner(System.in);
    private Clip audio;

    public TextUI() {
        Terminal.enterAltBuffer();
    }
    
    @Override
    public void close() {
        Terminal.exitAltBuffer();
        Terminal.showCursor();
    }

    public boolean lobby(GameOptions options) {
        if (audio != null)
            audio.stop();

        try { for (;;) {
            Terminal.hideCursor();
            Terminal.clearScreen();
            printTitleScreen();
            audio = GameSound.intro();
            console.nextLine();
            audio.stop();

            audio = GameSound.lobby();

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
                        if (setOption(options, args[0], args[1])) {
                            System.out.println("Game option updated");
                            GameSound.ok();
                        }
                    } break;

                    case "add": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        if (args[0].toLowerCase().equals("human")) {
                            options.playerlist.add(new UIPlayer(args[1]));
                            System.out.printf("Human player '%s' is now player #%d", args[1], options.playerlist.size());
                            GameSound.ok();
                            break;
                        }

                        if (args[0].toLowerCase().equals("ai")) {
                            options.playerlist.add(new AIPlayer(args[1], AIPlayer.Level.NORMAL));
                            System.out.printf("AI player '%s' is now player #%d", args[1], options.playerlist.size());
                            GameSound.ok();
                            break;    
                        }

                        System.out.printf("Invalid player type '%s', use either 'human' or 'AI'", args[0]);
                        GameSound.bad();
                    } break;
                        
                    case "rename": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        Player target = resolvePlayerNum(options.playerlist, args[0]);

                        if (target == null) {
                         System.out.printf("Invalid player number '%s'\n", args[0]);
                            GameSound.bad();
                            break;
                        }

                        target.setName(args[1]);
                        System.out.println("Name updated");
                        GameSound.ok();
                    } break;

                    case "level": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        Player target = resolvePlayerNum(options.playerlist, args[0]);

                        if (target == null) {
                            System.out.printf("Invalid player number '%s'\n", args[0]);
                            GameSound.bad();
                            break;
                        }

                        if (!(target instanceof AIPlayer)) {
                            System.out.println("Specified player is not an AI");
                            GameSound.bad();
                            break;
                        }

                        try {
                            ((AIPlayer) target).setLevel(AIPlayer.Level.fromValue(
                                Integer.parseInt(args[1])
                            ));
                            System.out.println("AI level updated");
                            GameSound.ok();
                        } catch (Exception e) {
                            System.out.println("Invalid AI level, use an integer between 1-5");
                            GameSound.bad();
                        }
                    } break;

                    case "move": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");
                        int from, to;

                        try { from = parsePlayerNum(options.playerlist, args[0]); }
                        catch (Exception e) {
                            System.out.println("Invalid target player number");
                            GameSound.bad();
                            break;
                        }

                        try { to = parsePlayerNum(options.playerlist, args[1]); }
                        catch (Exception e) {
                            System.out.println("Invalid destination player number");
                            GameSound.bad();
                            break;
                        }

                        Player target = options.playerlist.get(from);

                        options.playerlist.remove(from);
                        options.playerlist.add(to, target);
                        System.out.println("Player moved");
                        GameSound.ok();
                    } break;
                        
                    case "remove": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        try {
                            int target = parsePlayerNum(options.playerlist, args[0]);
                            options.playerlist.remove(target);
                            System.out.println("Player removed");
                            GameSound.ok();
                        } catch (Exception e) {
                            System.out.println("Invalid target player number");
                            GameSound.bad();
                        }
                    } break;

                    case "start": {
                        if (options.playerlist.size() != 0) {
                            audio.stop();
                            audio = GameSound.ingame();
                            GameSound.ok();
                            return true;
                        }
                        System.out.println("Cannot start game with 0 players!");
                        GameSound.bad();
                    } break;

                    case "quit": { GameSound.ok(); return false; }

                    case "help": {
                        Terminal.clearScreen();
                        printTutorial();
                        GameSound.ok();
                    } break;

                    default: {
                        System.out.printf("I don't understand '%s'.\nSee the 'Commands' section on top for list of commands.\n", cmd[0]);
                        GameSound.bad();
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
                    GameSound.bad();
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
                    GameSound.bad();
                    return false;
                }
            break;

            case "wordlist":
                try { 
                    (new FileReader(value)).close();
                    options.wordlistPath = value;
                } catch (IOException e) {
                    System.out.println("Unable to use file: "+e.getMessage());
                    GameSound.bad();
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
                GameSound.bad();
                return false;
            }

            default:
                System.out.println("I don't know about that option");
                System.out.println("TIP: use the part inside of square bracket in front of the option you wish to modify");
                GameSound.bad();
                return false;
        }

        return true;
    }

    private static void printTutorial() throws IOException {
        InputStream asset = TextUI.class.getResourceAsStream("asset/tutorial.txt");
        asset.transferTo(System.out);
        asset.close();
    }

    private static void printTitleScreen() throws IOException {
        InputStream logo = TextUI.class.getResourceAsStream("asset/logo.txt");
        logo.transferTo(System.out);
        logo.close();

        System.out.println("\n\t\t\t-- Press enter to continue --\n");
    }

    private static void printMenuScreen(GameOptions options) throws IOException {
        String template = new String(
            TextUI.class.getResourceAsStream("asset/menu.txt").readAllBytes(),
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

            if (player instanceof UIPlayer)
                System.out.printf("\tType: Human\tName: %s\n", player.getName());
            else
                System.out.printf("\tType: AI\tName: %s\tLevel: %d\n", player.getName(), ((AIPlayer) player).getLevel().getValue());

            i++;
        }

        if (i == 0) {
            System.out.println("(No player, use 'add' command to add a player)");
        }
    }

    private String currentPlayerName;

    public void startTurn(Gameboard gameboard, List<Map.Entry<String, Integer>> leaderboard, ArrayList<String> playedWords, String currentPlayerName) {
        Terminal.clearScreen();
        Terminal.hideCursor();

        char[][] board = gameboard.board;
        for (char[] row : board) {
            System.out.println('\n');
            for (char letter : row) {
                System.out.print("   "+letter);
            }
        }

        System.out.println('\n');
        System.out.println("==== Scores ====");
        for (Map.Entry<String, Integer> player: leaderboard){
            System.out.println(player.getKey()+"\t"+player.getValue());
        }

        System.out.println('\n');

        System.out.println("==== Played Words ====");
        int lineLength = 0;
        for (String word : playedWords) {
            if (lineLength / 60 >= 1) {
                System.out.println();
                lineLength = 0;
            }
            System.out.print(word+" ");
            lineLength += word.length()+1;
        }
        if (playedWords.size() == 0) 
        System.out.println("(No played words yet)");

        System.out.println('\n');

        System.out.printf("It's %s's turn\n\n", currentPlayerName);
        this.currentPlayerName = currentPlayerName;
    }

    public String active() {
        Terminal.showCursor();
        System.out.println("\nEnter your word, or '-skip' to skip this turn");
        System.out.print(currentPlayerName + ", make your move: ");
    
        String input = console.nextLine();

        if (input.equalsIgnoreCase(
        "-skip")) 
            return null;

        return input;
    }

    public void passive() {
        System.out.printf("\n%s is thinking...\n", currentPlayerName);
    }

    public void endTurn(TurnStatus status, String move, int scoreGained, int minWordLength) {
        Terminal.hideCursor();
        switch (status) {
            case OK:
                System.out.printf("%s played the word '%s' and gained +%d score!\n", currentPlayerName, move, scoreGained);
                GameSound.ok();
            break;
            case SKIPPED:
                System.out.println(currentPlayerName+" skipped their turn!");
                GameSound.bad();
            break;
            case TOO_SHORT:
                System.out.printf("Word too short! Minimal word length is %d\n", minWordLength);
                GameSound.bad();
            break;
            case DUPLICATE:
                System.out.println("This word was already played! Try a different one.");
                GameSound.bad();
            break;
            case NOT_IN_DICT:
                System.out.println("Word not in wordlist! Try a different one.");
                GameSound.bad();
            break;
            case NOT_ON_BOARD:
                System.out.println("Word does not exist on board! Try a different one");
                GameSound.bad();
            break;
        }

        System.out.println("\n==== Press enter to continue ====");
    }

    public void results(List<Map.Entry<String, Integer>> leaderboard, int skips, int maxScore) {
        try {
            audio.stop();
            audio = GameSound.results();

            Terminal.clearScreen();
            Terminal.hideCursor();

            InputStream asset = TextUI.class.getResourceAsStream("asset/results_head.txt");
            asset.transferTo(System.out);
            asset.close();

            System.out.println(
                "┃    Calories" +
                StringUtils.padStart(
                    String.format("%d    ┃", skips*10),
                    31, ' '
                )
            );

            asset = TextUI.class.getResourceAsStream("asset/results_body.txt");
            asset.transferTo(System.out);
            asset.close();

            for (Map.Entry<String, Integer> entry : leaderboard) {
                String name = StringUtils.truncateWithEllipsis(entry.getKey(), 16);
                int score = entry.getValue();

                System.out.println(
                    StringUtils.padEnd(String.format("┃    %s (%dc)", name, score), 30, ' ') +
                    StringUtils.padStart(
                        String.format("%d%%    ┃", (score*100)/(maxScore==0 ? Integer.MAX_VALUE : maxScore)),
                        14, ' '
                    )
                );
            }

            asset = TextUI.class.getResourceAsStream("asset/results_tail.txt");
            asset.transferTo(System.out);
            asset.close();

        } catch (IOException e) {
            e.printStackTrace();
            confirm();
            System.exit(-1);
        }
    }

    public void confirm() {
        console.nextLine();
    }
}
