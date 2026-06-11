/*
 * File: TextUI.java
 * Author: Ethan Ding
 * Description: Implements the terminal-based Boogle user interface, including lobby menus, turn display, and prompts.
 */

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

/**
 * Terminal implementation of the Boogle user interface.
 *
 * <p>The text UI uses ANSI escape sequences, classpath text-art assets, and
 * blocking console input to run the lobby, game turns, confirmations, and final
 * results. It is also responsible for translating typed commands such as
 * {@code -skip}, {@code -save}, and lobby configuration commands into launcher
 * options or player moves.</p>
 */
public class TextUI implements GameUI {
    
    private UncheckedBufferedReader console = new UncheckedBufferedReader(new InputStreamReader(System.in));
    private Clip audio;

    /**
     * Creates the terminal UI and switches the terminal to the alternate buffer.
     */
    public TextUI() {
        Terminal.enterAltBuffer();
    }
    
    @Override
    /**
     * Restores the terminal buffer and cursor visibility.
     */
    public void close() {
        Terminal.exitAltBuffer();
        Terminal.showCursor();
    }

    private boolean autoConfirm = false;
    private boolean playMusic = false;
    private boolean playSfx = false;

    private long inputTimeout = 0;

    /**
     * Runs the title screen and text lobby until the user starts, loads, or quits.
     *
     * @param options mutable game options edited by lobby commands
     * @return {@code true} to start or resume a game, {@code false} to quit
     */
    public boolean lobby(GameOptions options) {
        if (audio != null) audio.stop();

        try { for (;;) {
            Terminal.hideCursor();
            Terminal.clearScreen();
            printTitleScreen();
            if (this.playMusic) audio = GameSound.intro();
            else audio = GameSound.nothing();
            console.readLine();
            audio.stop();

            if (this.playMusic) audio = GameSound.lobby();

            for(;;) {
                Terminal.clearScreen();
                printMenuScreen(options, this);
                Terminal.showCursor();

                String[] cmd = padArray(console.readLine().trim().split(" ", 2), 2, "");
                Terminal.hideCursor();

                switch(cmd[0].toLowerCase()) {
                    case "set": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");
                        if (setOption(options, this, args[0], args[1])) {
                            System.out.println("Game option updated");
                            if (this.playSfx) GameSound.ok();
                        } else {
                            if (this.playSfx) GameSound.bad();
                        }
                    } break;

                    case "add": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        if (args[0].toLowerCase().equals("human")) {
                            options.playerlist.add(new UIPlayer(args[1]));
                            System.out.printf("Human player '%s' is now player #%d", args[1], options.playerlist.size());
                            if (this.playSfx) GameSound.ok();
                            break;
                        }

                        if (args[0].toLowerCase().equals("ai")) {
                            options.playerlist.add(new AIPlayer(args[1], AIPlayer.Level.NORMAL));
                            System.out.printf("AI player '%s' is now player #%d", args[1], options.playerlist.size());
                            if (this.playSfx) GameSound.ok();
                            break;    
                        }

                        System.out.printf("Invalid player type '%s', use either 'human' or 'AI'", args[0]);
                        if (this.playSfx) GameSound.bad();
                    } break;
                        
                    case "rename": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        Player target = resolvePlayerNum(options.playerlist, args[0]);

                        if (target == null) {
                         System.out.printf("Invalid player number '%s'\n", args[0]);
                            if (this.playSfx) GameSound.bad();
                            break;
                        }

                        target.setName(args[1]);
                        System.out.println("Name updated");
                        if (this.playSfx) GameSound.ok();
                    } break;

                    case "level": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        Player target = resolvePlayerNum(options.playerlist, args[0]);

                        if (target == null) {
                            System.out.printf("Invalid player number '%s'\n", args[0]);
                            if (this.playSfx) GameSound.bad();
                            break;
                        }

                        if (!(target instanceof AIPlayer)) {
                            System.out.println("Specified player is not an AI");
                            if (this.playSfx) GameSound.bad();
                            break;
                        }

                        try {
                            ((AIPlayer) target).setLevel(AIPlayer.Level.fromValue(
                                Integer.parseInt(args[1])
                            ));
                            System.out.println("AI level updated");
                            if (this.playSfx) GameSound.ok();
                        } catch (Exception e) {
                            System.out.println("Invalid AI level, use an integer between 1-5");
                            if (this.playSfx) GameSound.bad();
                        }
                    } break;

                    case "move": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");
                        int from, to;

                        try { from = parsePlayerNum(options.playerlist, args[0]); }
                        catch (Exception e) {
                            System.out.println("Invalid target player number");
                            if (this.playSfx) GameSound.bad();
                            break;
                        }

                        try { to = parsePlayerNum(options.playerlist, args[1]); }
                        catch (Exception e) {
                            System.out.println("Invalid destination player number");
                            if (this.playSfx) GameSound.bad();
                            break;
                        }

                        Player target = options.playerlist.get(from);

                        options.playerlist.remove(from);
                        options.playerlist.add(to, target);
                        System.out.println("Player moved");
                        if (this.playSfx) GameSound.ok();
                    } break;
                        
                    case "remove": {
                        String[] args = padArray(cmd[1].split(" ", 2), 2, "");

                        try {
                            int target = parsePlayerNum(options.playerlist, args[0]);
                            options.playerlist.remove(target);
                            System.out.println("Player removed");
                            if (this.playSfx) GameSound.ok();
                        } catch (Exception e) {
                            System.out.println("Invalid target player number");
                            if (this.playSfx) GameSound.bad();
                        }
                    } break;

                    case "start": {
                        if (options.playerlist.size() != 0) {
                            audio.stop();
                            if (this.playMusic) audio = GameSound.ingame();
                            if (this.playSfx) GameSound.ok();
                            return true;
                        }
                        System.out.println("Cannot start game with 0 players!");
                        if (this.playSfx) GameSound.bad();
                    } break;

                    case "load": {
                        try {
                            options.replacement = Launcher.fromSerialized(
                                new FileInputStream(cmd[1]), this
                            );
                            System.out.println("Save file loaded successfully!");
                            System.out.println("Press enter to continue");
                            this.confirmForSure();
                            return true;
                        } catch (IOException e) {
                            System.err.println("Unable to load save file: "+e.getMessage());
                        } catch (ClassNotFoundException e) {
                            System.err.println("Save file not compatible: "+e.getMessage());
                        }
                    } break;

                    case "quit": { 
                        if (this.playSfx) GameSound.ok();
                        return false; 
                    }

                    case "help": {
                        Terminal.clearScreen();
                        printTutorial();
                        if (this.playSfx) GameSound.ok();
                    } break;

                    case "gen-tnmt": {
                        Terminal.showCursor();
                        System.out.print("Enter path to board file: ");
                        String boardPath = console.readLine();

                        System.out.print("Enter path to wordlist file: ");
                        String wordlistPath = console.readLine();

                        try { 
                            Launcher.writeTournamentFiles(options, boardPath, wordlistPath);
                            System.out.println("OK");
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                            System.out.println("Unable to generate tournament files");
                        }
                        Terminal.hideCursor();
                    } break;

                    default: {
                        System.out.printf("I don't understand '%s'.\nSee the 'Commands' section on top for list of commands.\n", cmd[0]);
                        if (this.playSfx) GameSound.bad();
                    } break;
                }

                System.out.println("\n-- Press enter to continue --");
                console.readLine();
            }

        } } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extends an array with a default value when command parsing produced too few parts.
     *
     * @param arr original array
     * @param padLength required minimum length
     * @param padWith value to place in added slots
     * @param <T> array element type
     * @return original array if long enough, otherwise a padded copy
     */
    private static <T> T[] padArray(T[] arr, int padLength, T padWith) {
        if (arr.length >= padLength)
            return arr;

        T[] padded = Arrays.copyOf(arr, padLength);

        for (int i = arr.length; i < padded.length; i++) {
            padded[i] = padWith;
        }

        return padded;
    }

    /**
     * Resolves a one-based lobby player number to a player object.
     *
     * @param playerlist current ordered player list
     * @param playerNumStr number such as {@code 1} or {@code p1}
     * @return matching player, or {@code null} if the input is invalid
     */
    private static Player resolvePlayerNum(List<Player> playerlist, String playerNumStr) {
        try {
            return playerlist.get(parsePlayerNum(playerlist, playerNumStr));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses a one-based lobby player number.
     *
     * @param playerlist current ordered player list used for bounds checking
     * @param playerNumStr number such as {@code 1} or {@code p1}
     * @return zero-based player index
     * @throws NumberFormatException if the numeric part cannot be parsed
     * @throws IllegalArgumentException if the index is outside the player list
     */
    private static int parsePlayerNum(List<Player> playerlist, String playerNumStr) {
        playerNumStr = playerNumStr.toLowerCase();

        int playerNum = Integer.parseInt(playerNumStr.replace("p", ""))-1;

        if (playerNum < 0 || playerNum >= playerlist.size())
            throw new IllegalArgumentException();

        return playerNum;
    }

    /**
     * Applies one lobby {@code set} command to game or UI options.
     *
     * @param options launcher options to mutate
     * @param ui text UI whose presentation settings may be changed
     * @param key option key entered by the user
     * @param value option value entered by the user
     * @return {@code true} if the option was recognized and applied
     */
    private static boolean setOption(GameOptions options, TextUI ui, String key, String value) {
        switch(key) {
            case "win_score": {
                try { 
                    int input = Integer.parseInt(value);
                    if (input < 0)
                        throw new Exception();
                    options.winScore = input;
                } catch(Exception e) {
                    System.out.println("Invalid winning score value");
                    System.out.println("Enter an integer bigger than 0, or use 0 for endless mode");
                    return false;
                }
            } break;

            case "min_word_length": {
                try { 
                    int input = Integer.parseInt(value);
                    if (input < 0)
                        throw new Exception();
                    options.minWordLength = input;
                } catch(Exception e) {
                    System.out.println("Invalid minimum word length value");
                    System.out.println("Enter an integer bigger than 0, or use 0 for no limit");
                    return false;
                }
            } break;

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

            case "auto_confirm": {
                if (value.equalsIgnoreCase("yes")) {
                    ui.autoConfirm = true;
                    break;
                }

                if (value.equalsIgnoreCase("no")) {
                    ui.autoConfirm = false;
                    break;
                }

                System.out.printf("I'm not sure what you mean by '%s'\n", value);
                System.out.println("Please specify either 'yes' or 'no'");
                return false;
            }

            case "time_limit": {
                try { 
                    int input = Integer.parseInt(value);
                    if (input < 0)
                        throw new Exception();
                    ui.inputTimeout = input * 1_000_000_000L;
                } catch(Exception e) {
                    System.out.println("Invalid time limit value");
                    System.out.println("Enter an integer bigger than 0, or use 0 for no limit");
                    return false;
                }
            } break;

            case "music": {
                if (value.equalsIgnoreCase("yes")) {
                    if (!ui.playMusic) {
                        ui.playMusic = true;
                        ui.audio = GameSound.lobby();
                    }
                    break;
                }

                if (value.equalsIgnoreCase("no")) {
                    ui.playMusic = false;
                    ui.audio.stop();
                    break;
                }

                System.out.printf("I'm not sure what you mean by '%s'\n", value);
                System.out.println("Please specify either 'yes' or 'no'");
                return false;
            }

            case "sfx": {
                if (value.equalsIgnoreCase("yes")) {
                    ui.playSfx = true;
                    break;
                }

                if (value.equalsIgnoreCase("no")) {
                    ui.playSfx = false;
                    break;
                }

                System.out.printf("I'm not sure what you mean by '%s'\n", value);
                System.out.println("Please specify either 'yes' or 'no'");
                return false;
            }
        }

        return true;
    }

    /**
     * Prints the lobby tutorial asset.
     *
     * @throws IOException retained for callers that treat asset loading as I/O
     */
    private static void printTutorial() throws IOException {
        System.out.println(getAsset("asset/menu/tutorial.txt"));
    }

    /**
     * Prints the title screen and initial prompt.
     *
     * @throws IOException retained for callers that treat asset loading as I/O
     */
    private static void printTitleScreen() throws IOException {
        System.out.println(getAsset("asset/logo.txt"));

        System.out.println("\n\t\t\t-- Press enter to continue --\n");
    }

    /**
     * Prints the lobby menu, current option values, and player list.
     *
     * @param options current game options
     * @param ui current UI presentation options
     * @throws IOException retained for callers that treat asset loading as I/O
     */
    private static void printMenuScreen(GameOptions options, TextUI ui) throws IOException {
        String template = getAsset("asset/menu/head.txt");

        System.out.printf(template,
            options.winScore == 0 ? "Endless" : ""+options.winScore,
            options.minWordLength == 0 ? "No minimum word length limit" : ""+options.minWordLength,
            options.wordlistPath,
            options.customBoard == null ? "Generated" : "Custom",
            ui.autoConfirm ? "Yes" : "No",
            ui.inputTimeout==0 ? "No Limit" : (ui.inputTimeout/1_000_000_000L)+"s",
            ui.playMusic ? "Yes" : "No",
            ui.playSfx ? "Yes" : "No"
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

    /**
     * Renders the board, leaderboard, played-word list, and current player.
     *
     * @param gameboard board to display
     * @param leaderboard current sorted scores
     * @param playedWords accepted words so far
     * @param currentPlayerName player whose turn is active
     */
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

    /**
     * Prompts a human player for a word or command.
     *
     * @return move corresponding to the typed word or command
     */
    public Player.Move active() {
        Terminal.showCursor();
        System.out.println("\nEnter your word of choice or a command");
        System.out.println("List of commands:");
        System.out.println("    -skip           --      skip this turn");
        System.out.println("    -giveup         --      give up and leave the game");
        System.out.println("    -save <path>    --      Save game");
        System.out.println("    -stop           --      Stop game\n");
    
        String input;

        int numLength = (int)Math.floor(Math.log10(inputTimeout / 1_000_000_000L)) + 1;

        long timer = 0;
        long lastUIUpdate = -1_000_000_000L;
        long startTime = System.nanoTime();

        if (inputTimeout != 0)
            System.out.printf(">> %s, make your move (%0"+numLength+"ds): ", currentPlayerName, 0);
        else 
            System.out.printf(">> %s, make your move: ", currentPlayerName);

        // polling loop
        // (better than the others doing vibe coded multithreading slop)
        while (inputTimeout != 0 && !console.ready() && inputTimeout-timer > 0) {
            // hopefully this is fast enough to not interfere with typing
            // the reason why I don't have the ANSI codes in Terminal.java
            // is because multiple syscalls are way slower than one syscall
            // pushing one buffer. And we want to minimize ui refresh time
            if (timer - lastUIUpdate >= 1_000_000_000L) {
                System.out.printf(
                    "\033[s\r>> %s, make your move (%0"+numLength+"ds): \033[u",
                    currentPlayerName,
                    // some ceiling division magic
                    Math.max(0, (inputTimeout - timer + 999_999_999L) / 1_000_000_000L)
                );
                System.out.flush();
                lastUIUpdate = timer;
            }

            try { Thread.sleep(125); }   // polls 8 times per second
            catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            timer = System.nanoTime() - startTime;
        }

        if (inputTimeout != 0 && inputTimeout-timer <= 0) {
            System.out.println();
            return new Player.Move(Player.Move.Type.TIMEOUT);
        } 

        input = console.readLine().trim();

        String[] args = padArray(input.split(" ", 2), 2, "");

        switch (args[0]) {
            case "-skip":
                return new Player.Move(Player.Move.Type.SKIP);

            case "-giveup":
                return new Player.Move(Player.Move.Type.LEAVE);

            case "-save":
                return new Player.Move(Player.Move.Type.SAVE, args[1]);

            case "-stop":
                return new Player.Move(Player.Move.Type.STOP);
                
            default:
                return new Player.Move(Player.Move.Type.WORD, input.toLowerCase());
        }
    }

    /**
     * Displays a short waiting message while an AI turn is resolved.
     */
    public void passive() {
        System.out.printf("\n%s is thinking...\n", currentPlayerName);
    }

    /**
     * Prints a textual outcome message for the turn.
     *
     * @param status outcome status
     * @param move word or save error associated with the outcome
     * @param scoreGained points awarded for an accepted word
     * @param minWordLength configured minimum word length
     */
    public void endTurn(TurnStatus status, String move, int scoreGained, int minWordLength) {
        Terminal.hideCursor();
        switch (status) {
            case OK:
                System.out.printf("%s played the word '%s' and gained +%d score!\n", currentPlayerName, move, scoreGained);
                if (this.playSfx) GameSound.ok();
            break;
            case SAVE_OK:
                System.out.println("Game successful saved!");
                if (this.playSfx) GameSound.ok();
            break;
            case SAVE_ERR:
                System.out.println("Error when saving game: "+move);
                if (this.playSfx) GameSound.bad();
            break;
            case SKIPPED:
                System.out.println(currentPlayerName+" skipped their turn!");
                if (this.playSfx) GameSound.bad();
            break;
            case TOO_SHORT:
                System.out.printf("Word too short! Minimal word length is %d\n", minWordLength);
                if (this.playSfx) GameSound.bad();
            break;
            case DUPLICATE:
                System.out.println("This word was already played! Try a different one.");
                if (this.playSfx) GameSound.bad();
            break;
            case NOT_IN_DICT:
                System.out.println("Word not in wordlist! Try a different one.");
                if (this.playSfx) GameSound.bad();
            break;
            case NOT_ON_BOARD:
                System.out.println("Word does not exist on board! Try a different one");
                if (this.playSfx) GameSound.bad();
            break;
            case STOPPED:
                System.out.println("This game of boogle is no more :(");
                if (this.playSfx) GameSound.bad();
            break;
            case PLAYER_LEFT:
                System.out.printf("%s left the game!\n", currentPlayerName);
                if (this.playSfx) GameSound.bad();
            break;
            case TIMEOUT:
                System.out.printf("%s was TAKING TOO LONG!\n", currentPlayerName);
                if (this.playSfx) GameSound.bad();
            break;
        }

        if (!this.autoConfirm) {
            System.out.println("\n==== Press enter to continue ====");
        }
    }

    /**
     * Prints the final results screen.
     *
     * @param leaderboard final sorted scores
     * @param skips total skipped turns
     * @param maxScore maximum possible score on the board
     */
    public void results(List<Map.Entry<String, Integer>> leaderboard, int skips, int maxScore) {
        audio.stop();
        if (this.playMusic) audio = GameSound.results();

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

    /**
     * Waits through a one-second window of enter presses before leaving results.
     */
    public void confirmForSure() {
        long start = System.nanoTime();

        long end = start;

        while (end-start < 1_000_000_000) {
            console.readLine();
            end = System.nanoTime();
        }
    }

    /**
     * Performs the configured between-turn confirmation behavior.
     */
    public void confirm() {
        if (this.autoConfirm) {
            try { Thread.sleep(1000); }
            catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            console.readLine();
        }
    }

    private static HashMap<String, String> assetCache = new HashMap<>();

    /**
     * Loads and caches a text asset from the classpath.
     *
     * @param path resource path relative to this class's package
     * @return asset contents without one trailing line terminator
     */
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
            ).replaceFirst("\\R$", "");

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

