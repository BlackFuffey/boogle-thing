package boogle.ui.tui;

import boogle.core.*;
import boogle.core.Launcher.GameOptions;
import boogle.player.*;
import boogle.util.*;
import boogle.sound.GameSound;

import javax.sound.sampled.Clip;

import java.util.*;
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
        System.out.println(getAsset("asset/menu/tutorial.txt"));
    }

    private static void printTitleScreen() throws IOException {
        System.out.println(getAsset("asset/logo.txt"));

        System.out.println("\n\t\t\t-- Press enter to continue --\n");
    }

    private static void printMenuScreen(GameOptions options) throws IOException {
        String template = getAsset("asset/menu/head.txt");

        System.out.printf(
            template,
            options.winScore == 0 ? "Endless" : ""+options.winScore,
            options.minWordLength == 0 ? "No minimum word length limit" : ""+options.minWordLength,
            options.wordlistPath,
            options.customBoard == null ? "Generated" : "Custom"
        );

        String humanTemplate = getAsset("asset/menu/player_human.txt");
        String aiTemplate = getAsset("asset/menu/player_ai.txt");

        int i = 0;
        for (Player player : options.playerlist) {

            if (player instanceof UIPlayer)
                System.out.printf(humanTemplate, (i+1), player.getName());
            else
                System.out.printf(aiTemplate, (i+1), player.getName(), ((AIPlayer) player).getLevel().getValue());

            i++;
        }

        if (i == 0)
            System.out.println(getAsset("asset/menu/noplayer.txt"));

        System.out.println(getAsset("asset/menu/tail.txt"));

        System.out.print("\n[38;2;255;176;0m[1mEnter command:[0m ");
    }

    private String currentPlayerName;

    public void startTurn(Gameboard gameboard, List<Map.Entry<String, Integer>> leaderboard, ArrayList<String> playedWords, String currentPlayerName) {
        Terminal.hideCursor();
        StringBuilder boardDisplayBuilder = new StringBuilder();

        char[][] board = gameboard.board;

        /* Head */ {
            String body = getAsset("asset/game/board/y_head/body.txt");
            String head = getAsset("asset/game/board/y_head/head.txt");
            String tail = getAsset("asset/game/board/y_head/tail.txt");
            boardDisplayBuilder.append(head);

            for (char ch : board[0]) {
                boardDisplayBuilder.append(body);
            } 

            boardDisplayBuilder.append(tail);
        }

        /* Body */ {
            String die = getAsset("asset/game/board/die.txt");
            String yDivBody = getAsset("asset/game/board/y_div/body.txt");
            String yDivHead = getAsset("asset/game/board/y_div/head.txt");
            String yDivTail = getAsset("asset/game/board/y_div/tail.txt");
            String xDiv= getAsset("asset/game/board/x_div.txt");
            String xHead= getAsset("asset/game/board/x_head.txt");
            String xTail= getAsset("asset/game/board/x_tail.txt");
            for (char[] row : board) {
                boardDisplayBuilder.append('\n');
                boardDisplayBuilder.append(xHead);
                for (int i = 0; i < row.length; i++) {
                    boardDisplayBuilder.append(String.format(die, row[i]));
                    if (i != row.length-1) {
                        boardDisplayBuilder.append(xDiv);
                    }
                }

                boardDisplayBuilder.append(xTail);
                boardDisplayBuilder.append('\n');
                boardDisplayBuilder.append(yDivHead);
                for (char letter : row) {
                    boardDisplayBuilder.append(yDivBody);
                }
                boardDisplayBuilder.append(yDivTail);
            }
        }
        
        boardDisplayBuilder.append("\r\033[K");

        /* Tail */ {
            String body = getAsset("asset/game/board/y_tail/body.txt");
            String head = getAsset("asset/game/board/y_tail/head.txt");
            String tail = getAsset("asset/game/board/y_tail/tail.txt");
            boardDisplayBuilder.append(head);

            for (char ch : board[0]) {
                boardDisplayBuilder.append(body);
            } 

            boardDisplayBuilder.append(tail);
        }

        boardDisplayBuilder.append('\n');
        String boardDisplay = boardDisplayBuilder.toString();

        String scoreDisplay;
        /* Scores */ {
            StringBuilder scoreboardBuilder = new StringBuilder();

            for (Map.Entry<String, Integer> player: leaderboard){
                scoreboardBuilder.append(String.format(
                    getAsset("asset/game/scores_body.txt"),
                    player.getKey(), player.getValue()
                ));
                scoreboardBuilder.append('\n');
            }

            scoreDisplay = scoreboardBuilder.toString();
        }

        String wordDisplay;
        /* Played Words */ {
            StringBuilder wordsBuilder = new StringBuilder();

            int lineLength = 0;
            StringBuilder lineBuilder = new StringBuilder();
            String wordLine = getAsset("asset/game/played_body.txt");
            for (String word : playedWords) {
                if (lineLength / 90 >= 1) {
                    wordsBuilder.append(String.format(wordLine, lineBuilder.toString()));
                    wordsBuilder.append('\n');
                    lineLength = 0;
                    lineBuilder = new StringBuilder();
                }
                lineBuilder.append(word);
                lineBuilder.append(' ');
                lineLength += word.length()+1;
            }

            if (playedWords.size() == 0) 
                lineBuilder.append("(No played words yet)");

            wordsBuilder.append(String.format(wordLine, lineBuilder.toString()));
            wordsBuilder.append('\n');
            wordDisplay = wordsBuilder.toString();
        }

        Terminal.clearScreen();
        System.out.printf(
            getAsset("asset/game/layout.txt"),
            boardDisplay, scoreDisplay, wordDisplay, currentPlayerName
        );

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
        audio.stop();
        audio = GameSound.results();

        Terminal.clearScreen();
        Terminal.hideCursor();

        System.out.println(getAsset("asset/results/head.txt"));

        System.out.println(
            "┃    Calories" +
            StringUtils.padStart(
                String.format("%d    ┃", skips*10),
                31, ' '
            )
        );

        System.out.println(getAsset("asset/results/body.txt"));

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

        System.out.println(getAsset("asset/results/tail.txt"));
    }

    public void confirm() {
        console.nextLine();
    }

    private static HashMap<String, String> assetCache = new HashMap<>();

    private static String getAsset(String path) {
        try {
            String result = assetCache.get(path);

            if (result != null)
            return result;

            InputStream stream = TextUI.class.getResourceAsStream(path);

            if (stream == null) {
                throw new IOException("No asset found on path "+path);
            }

            result = new String(
                stream.readAllBytes(),
                StandardCharsets.UTF_8
            ).replaceFirst("\\n$", "");

            stream.close();

            assetCache.put(path, result);
            return result;
        } catch (IOException e) {
            System.err.println("Unable to load asset "+path);
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }
}

