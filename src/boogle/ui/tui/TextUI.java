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
 * Text‑mode implementation of {@link GameUI}. This class renders the Boogle
 * lobby, game board and results in a terminal using ANSI escape codes.
 * Players interact with the game through typed commands. The UI also
 * supports optional sound effects and music via {@link boogle.sound.GameSound}
 * and can automatically advance through confirmation prompts when
 * {@link #autoConfirm} is enabled.
 */
public class TextUI implements GameUI {
    
    /** Scanner used to read lines of input from the console. */
    private Scanner console = new Scanner(System.in);
    /** Currently playing audio clip; used for background music and effects. */
    private Clip audio;

    /**
     * Constructs a new text UI and switches the terminal into an alternate
     * screen buffer. The alternate buffer allows the game to draw over the
     * existing terminal contents without losing them; when the UI exits the
     * original screen will be restored.
     */
    public TextUI() {
        Terminal.enterAltBuffer();
    }
    
    @Override
    /**
     * Cleans up the terminal by leaving the alternate buffer and ensuring
     * that the cursor is visible again. This method should be called when
     * the UI is no longer needed.
     */
    public void close() {
        Terminal.exitAltBuffer();
        Terminal.showCursor();
    }

    /** When {@code true} the UI skips confirmation prompts and sleeps briefly. */
    private boolean autoConfirm = false;
    /** When {@code true} background music is played during the lobby and game. */
    private boolean playMusic = false;
    /** When {@code true} sound effects are played on certain events. */
    private boolean playSfx = false;

    /**
     * Presents the lobby and allows the user to configure players and
     * options. The lobby consists of a title screen followed by a menu of
     * commands. Users can add or remove players, rename them, change AI
     * levels, move players, set the winning score and minimum word length,
     * choose a dictionary file, supply a custom board or toggle auto
     * confirmation, music and sound effects. Once the user starts the game
     * (and at least one player has been added) the method returns {@code true}.
     * Entering the {@code quit} command returns {@code false}.
     *
     * @param options mutable game configuration to populate
     * @return {@code true} if the game should start, {@code false} to quit
     */
    public boolean lobby(GameOptions options) {
        if (audio != null) audio.stop();

        try { for (;;) {
            Terminal.hideCursor();
            Terminal.clearScreen();
            printTitleScreen();
            if (this.playMusic) audio = GameSound.intro();
            else audio = GameSound.nothing();
            console.nextLine();
            audio.stop();

            if (this.playMusic) audio = GameSound.lobby();

            for(;;) {
                Terminal.clearScreen();
                printMenuScreen(options, this);
                Terminal.showCursor();

                String[] cmd = padArray(console.nextLine().trim().split(" ", 2), 2, "");
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
                        String boardPath = console.nextLine();

                        System.out.print("Enter path to wordlist file: ");
                        String wordlistPath = console.nextLine();

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
                console.nextLine();
            }

        } } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns a copy of the provided array padded to the specified length. If
     * {@code arr.length} is less than {@code padLength} then new elements
     * equal to {@code padWith} are appended to the result. Otherwise the
     * original array is returned unchanged.
     *
     * @param <T> component type of the array
     * @param arr the array to pad
     * @param padLength desired minimum length of the returned array
     * @param padWith element value used to pad the array
     * @return an array of length at least {@code padLength}
     */
    static <T> T[] padArray(T[] arr, int padLength, T padWith) {
        if (arr.length >= padLength)
            return arr;

        T[] padded = Arrays.copyOf(arr, padLength);

        for (int i = arr.length; i < padded.length; i++) {
            padded[i] = padWith;
        }

        return padded;
    }

    /**
     * Attempts to resolve a player reference from a string. The string may
     * start with an optional {@code 'p'} followed by a 1‑based index. If the
     * index is invalid or out of bounds {@code null} is returned rather than
     * throwing an exception.
     *
     * @param playerlist list of players
     * @param playerNumStr string representation of a player number (e.g. {@code "p1"})
     * @return the {@link Player} at the specified position, or {@code null} if
     *         the index could not be parsed
     */
    static Player resolvePlayerNum(List<Player> playerlist, String playerNumStr) {
        try {
            return playerlist.get(parsePlayerNum(playerlist, playerNumStr));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses a string of the form {@code "p1"}, {@code "1"}, etc. into a
     * zero‑based player index. The returned index is validated to ensure it
     * refers to a valid element of {@code playerlist}; otherwise an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param playerlist list of players
     * @param playerNumStr string representation of the player number
     * @return zero‑based index into {@code playerlist}
     * @throws IllegalArgumentException if the parsed index is out of range
     */
    static int parsePlayerNum(List<Player> playerlist, String playerNumStr) {
        playerNumStr = playerNumStr.toLowerCase();

        int playerNum = Integer.parseInt(playerNumStr.replace("p", ""))-1;

        if (playerNum < 0 || playerNum >= playerlist.size())
            throw new IllegalArgumentException();

        return playerNum;
    }

    /**
     * Modifies an option based on a key/value pair. Supported keys include
     * {@code win_score}, {@code min_word_length}, {@code wordlist},
     * {@code board}, {@code auto_confirm}, {@code music} and {@code sfx}.
     * Validates the supplied value and updates {@code options} or the
     * UI instance accordingly. If a value is invalid an explanatory message
     * is printed and the method returns {@code false}.
     *
     * @param options game options to update
     * @param ui reference to the UI for toggling behaviours and sound
     * @param key option identifier
     * @param value new value for the option
     * @return {@code true} if the option was successfully updated; {@code false}
     *         if validation failed
     */
    static boolean setOption(GameOptions options, TextUI ui, String key, String value) {
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
     * Prints the tutorial text to standard output. The tutorial describes the
     * available commands and how to play the game. The content is loaded from
     * a text asset bundled with the application.
     *
     * @throws IOException if the asset cannot be read
     */
    private static void printTutorial() throws IOException {
        System.out.println(getAsset("asset/menu/tutorial.txt"));
    }

    /**
     * Displays the title screen logo and waits for the user to press enter.
     * The logo is loaded from an embedded text asset.
     *
     * @throws IOException if the asset cannot be read
     */
    private static void printTitleScreen() throws IOException {
        System.out.println(getAsset("asset/logo.txt"));

        System.out.println("\n\t\t\t-- Press enter to continue --\n");
    }

    /**
     * Renders the main lobby menu. This method prints the current game
     * options, the list of players with their indices, names and AI levels,
     * and a list of available commands. Asset templates are used to lay out
     * the menu consistently.
     *
     * @param options current game options
     * @param ui reference to the current UI used to query flags such as
     *           {@link #autoConfirm}
     * @throws IOException if any of the menu assets cannot be read
     */
    private static void printMenuScreen(GameOptions options, TextUI ui) throws IOException {
        String template = getAsset("asset/menu/head.txt");

        System.out.printf(template,
            options.winScore == 0 ? "Endless" : ""+options.winScore,
            options.minWordLength == 0 ? "No minimum word length limit" : ""+options.minWordLength,
            options.wordlistPath,
            options.customBoard == null ? "Generated" : "Custom",
            ui.autoConfirm ? "Yes" : "No",
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

    /** Name of the player whose turn is currently being displayed. */
    private String currentPlayerName;

    /**
     * Draws the game board, score table and list of played words for the
     * current turn. The display uses unicode box‑drawing characters stored
     * in assets to render a tidy table. This method also records the
     * {@code currentPlayerName} so that subsequent calls to
     * {@link #active()} and {@link #passive()} can reference it.
     *
     * @param gameboard current game board
     * @param leaderboard players and their scores in descending order
     * @param playedWords list of words that have been successfully played
     * @param currentPlayerName name of the player who is about to move
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
     * Prompts the human player to enter a word or to skip their turn. The
     * prompt displays the player’s name and waits for input. The string
     * {@code "-skip"} (case‑insensitive) is interpreted as a skip and
     * causes {@code null} to be returned. All other input is returned
     * verbatim to the game engine.
     *
     * @return the word entered by the user, or {@code null} to skip
     */
    public Player.Move active() {
        Terminal.showCursor();
        System.out.println("\nEnter your word of choice or a command");
        System.out.println("List of commands:");
        System.out.println("    -skip           --      skip this turn");
        System.out.println("    -giveup         --      give up and leave the game");
        System.out.println("    -save <path>    --      Save game");
        System.out.println("    -stop           --      Stop game\n");
        System.out.print(currentPlayerName + ", make your move: ");
    
        String input = console.nextLine().trim().toLowerCase();
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
                return new Player.Move(Player.Move.Type.WORD, input);
        }
    }

    /**
     * Informs spectators that the current player (an AI) is thinking. A simple
     * message is printed to the console. Control returns immediately so the
     * game engine can proceed to ask the AI for its move.
     */
    public void passive() {
        System.out.printf("\n%s is thinking...\n", currentPlayerName);
    }

    /**
     * Reports the result of a player’s move. Depending on {@code status}
     * this method prints an appropriate message (e.g. success, too short,
     * duplicate, not in dictionary or not on board) and plays a sound if
     * effects are enabled. When the turn is successful the gained score is
     * shown. If auto confirmation is disabled the user is prompted to press
     * enter before continuing to the next turn.
     *
     * @param status outcome of the move
     * @param move the word that was played or attempted
     * @param scoreGained number of points awarded for the move
     * @param minWordLength minimum word length enforced by the game
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
        }

        if (!this.autoConfirm) {
            System.out.println("\n==== Press enter to continue ====");
        }
    }

    /**
     * Displays the final scoreboard and a whimsical “calories” summary. Each
     * player’s name is truncated to fit within the results template and their
     * score is shown along with the percentage of the maximum possible score
     * achieved. The number of skipped turns is multiplied by ten to derive a
     * calorie total purely for fun. Music may be played during the results
     * screen if enabled.
     *
     * @param leaderboard final ranking of players by score
     * @param skips total number of skipped turns
     * @param maxScore maximum score obtainable on the generated board
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
     * Blocks until the user presses enter. Used after the results screen to
     * prevent the UI from closing immediately.
     */
    public void confirmForSure() {
        console.nextLine();
    }

    /**
     * Pauses the UI between turns. When {@link #autoConfirm} is enabled
     * execution sleeps for one second; otherwise the user must press enter
     * before the next turn proceeds. Any {@link InterruptedException} is
     * converted into a runtime error.
     */
    public void confirm() {
        if (this.autoConfirm) {
            try { Thread.sleep(1000); }
            catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            console.nextLine();
        }
    }

    /** Cache of loaded asset contents keyed by their classpath location. */
    private static HashMap<String, String> assetCache = new HashMap<>();

    /**
     * Loads a text asset from the classpath and returns its contents as a
     * {@link String}. Assets are cached after the first load to avoid
     * repeated I/O. A trailing newline is removed to facilitate embedding
     * assets into format strings. When loading fails the method prints an
     * error and exits the JVM with a negative status.
     *
     * @param path classpath relative path to the asset
     * @return the contents of the asset as a string
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

