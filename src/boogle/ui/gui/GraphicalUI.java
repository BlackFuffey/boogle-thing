package boogle.ui.gui;

import boogle.core.*;
import boogle.core.Launcher.GameOptions;
import boogle.player.AIPlayer;
import boogle.player.UIPlayer;
import boogle.sound.GameSound;

import javax.sound.sampled.Clip;
import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;


/**
 * Experimental graphical user interface for Boogle. This implementation
 * attempts to provide a Swing‑based lobby and game board but remains a work
 * in progress. Many of the methods defined by {@link GameUI} are not
 * implemented and will throw {@link UnsupportedOperationException} when
 * invoked.
 */
public class GraphicalUI implements GameUI{
    /** Flag set when the user has configured the game and pressed Start. */
    private boolean ready=false;
    /** Currently playing audio clip, used to manage music and sound effects. */
    private Clip audio;

    /*
    naming convention:
        Windows - upper
        PANEL - all caps
        component - lower
    */
    /** Top‑level window for the lobby screen. */
    Windows MainMenu = new Windows("lobby");
    /** Window used during the game to show the board and scores. */
    Windows Game = new Windows("Game");
    /** Dialog for configuring players and options. */
    Windows Settings = new Windows("Settings");
    /** Window for displaying final results. */
    Windows Results = new Windows("Results");


    //button methods
    /**
     * Ensures that the game can begin. In the graphical UI a game may only
     * start when at least one player has been added. If there are no
     * players the method will display a warning dialog and return {@code false}.
     *
     * @param options current options containing the player list
     * @return {@code true} if the game can start, {@code false} otherwise
     */
    private Boolean startCheck(GameOptions options){
        if (options.playerlist.size() != 0) {
            //audio.stop();
            //audio = GameSound.ingame();
            //GameSound.ok();
            return true;
        }
        Windows.CreateWarning(null, "Settings Missing", "Players Missing\nPlease Open Settings to Add Players");
        return false;
    }
    /**
     * Returns a human‑readable description of the supplied player’s type.
     *
     * @param player the player to describe
     * @return {@code "Ai"} if the player is an {@link AIPlayer},
     *         {@code "Human"} if the player is a {@link UIPlayer}, or
     *         {@code null} if unknown
     */
    private String getPlayerType(Player player){
        if(player instanceof AIPlayer){
            return "Ai";
        }
        else if(player instanceof UIPlayer){
            return "Human";
        }
        return null;
    }
    /**
     * Returns the AI level as a string for display purposes. Human players
     * return {@code null}.
     *
     * @param player the player whose level to report
     * @return the integer value of the AI level or {@code null} for humans
     */
    private String getAILevel(Player player){
        if(player instanceof AIPlayer){
            return Integer.toString(((AIPlayer)player).getLevel().getValue());
        }
        return null;
    }
    /**
     * Adds a new player to the game based on UI input. This helper method
     * interprets the selected type and level and appends a new instance to
     * the {@code playerlist} contained within {@code options}.
     *
     * @param options mutable game options
     * @param type player type as returned by the combo box ({@code "Ai"} or
     *             {@code "Human"})
     * @param name name of the new player
     * @param level difficulty level for AI players, ignored for humans
     */
    private void validatePlayer(GameOptions options,String type, String name, String level){
        switch(type){
            case "Ai":    
                options.playerlist.add(new AIPlayer(name, boogle.player.AIPlayer.Level.fromValue(Integer.parseInt(level))));
            case "Human":
                options.playerlist.add(new UIPlayer(name));
            }
    }
    /**
     * Placeholder method for launching the game window. The current
     * implementation always returns {@code false} because the graphical UI is
     * incomplete.
     *
     * @return currently always {@code false}
     */
    private boolean gameStart(){
        return false;
    }

    //windows setup
    /**
     * Builds the initial lobby window including title, player list and
     * settings buttons. This method is called lazily when the lobby is
     * first displayed.
     *
     * @param options configuration options used to populate the player list
     */
    private void CreateMenu(GameOptions options){
        MainMenu.windowSize(true);
        MainMenu.Created();
        MainMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MainMenu.AddPanel("MAIN", "LAYOUT", new BoxLayout(Game, BoxLayout.Y_AXIS));
        MainMenu.AddPanel("LAYOUT","TITLE",new FlowLayout());
        MainMenu.Panel("TITLE").AddText("title", "BOOGLE");
        MainMenu.Panel("TITLE").GetItem("title").setFont(new Font("Ariel",Font.BOLD,100));
        MainMenu.AddPanel("LAYOUT", "PLAYERLIST", new BoxLayout(Game,BoxLayout.Y_AXIS));
        
        MainMenu.AddPanel("LAYOUT", "SETTINGS", new FlowLayout());
        MainMenu.Panel("SETTINGS").AddButton("settings","Settings",e->{
            if(Settings.isCreated()){
                Settings.setVisible(true);
            }else{
                CreateSettings(options);
            }
        });
        MainMenu.Panel("SETTINGS").AddButton("start", "Start", e ->{ready = startCheck(options);});
        MainMenu.Panel("SETTINGS").AddButton( "quit","Quit",e ->{System.exit(0);});

    }

    //TODO: settings option
    //settings window
    /**
     * Opens the settings dialog allowing users to add players and adjust
     * their names and AI levels. Settings are applied directly to the
     * provided {@code options} instance.
     *
     * @param options game options to modify
     */
    private void CreateSettings(GameOptions options){
        Settings.Created();
        Settings.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //layout
        Settings.windowSize((int)(Windows.getScreenSize().getWidth()/1.5),(int)Windows.getScreenSize().getHeight());
        //title
        Settings.AddPanel("MAIN","LAYOUT", new GridBagLayout());
        Settings.AddPanel("LAYOUT", "SETTINGS",new BoxLayout(Settings, BoxLayout.Y_AXIS));
        Settings.AddPanel("LAYOUT", "PLAYERLIST", new BoxLayout(Settings, BoxLayout.Y_AXIS));
       
       
        Settings.Panel("SETTINGS").AddText("title", "Settings");
        Settings.Panel("SETTINGS").GetItem("title").setFont(new Font("Ariel",Font.BOLD,30));
        Settings.Panel("SETTINGS").setAnchor("N");
        Settings.Panel("PLAYERLIST").setAnchor("NW");
        //settings
        //player setting
        Settings.Panel("SETTINGS").AddText( "players", "Players:");
        Settings.AddPanel("SETTINGS", "PLAYERS",new FlowLayout());
        //playerlist

        Settings.Panel("PLAYERS").AddComboBox("PlayerSettingType", new String[]{"Human","AI"});
        Settings.Panel("PLAYERS").AddText("settingsplayernamelabel", "Name");
        Settings.Panel("PLAYERS").AddTextField("PlayerSettingName", 10);
        Settings.Panel("PLAYERS").AddText("settingsailevel", "ai level (ignored if human):");
        Settings.Panel("PLAYERS").AddComboBox("PlayerSettingLevel",new String[]{"1","2","3","4","5"});
        Settings.Panel("PLAYERLIST").AddText("playerlist","Current Players: ");
        Settings.Panel("PLAYERS").AddButton("PlayerSettingSubmit","Submit",e->{
            validatePlayer(options,
            Settings.Panel("PLAYERS").GetItemText("PlayerSettingType"),
            Settings.Panel("PLAYERS").GetItemText("PlayerSettingName"),
            Settings.Panel("PLAYERS").GetItemText("PlayerSettingLevel")
        );
        Settings.Panel("PLAYERLIST").Clear();
        Settings.Panel("PLAYERLIST").AddText("playerlist","Current Players: ");
        for(Player players: options.playerlist){
            //so names will not interfere with other panels
            String name = players.toString();
            Settings.AddPanel("PLAYERLIST", name, new FlowLayout());
            Settings.Panel(name).AddText(name+" number",Integer.toString(options.playerlist.indexOf(players)));
            Settings.Panel(name).AddText(name+" name",players.getName());
            Settings.Panel(name).AddText(name+" type", getPlayerType(players));
            Settings.Panel(name).AddText(name+" level"," lvl "+getAILevel(players));
        }
        Settings.Panel("PLAYERLIST").add(Box.createVerticalGlue());
        Settings.Panel("PLAYERLIST").revalidate();
        Settings.Panel("PLAYERLIST").repaint();
    }); 
        Settings.AddPanel("PLAYERLIST", "PLAYERSETTINGS", new BoxLayout(Settings,BoxLayout.X_AXIS));
        Settings.setVisible(true);
    }

    //Game Window
    private void CreateGameWindow(Gameboard board, List<Entry<String, Integer>> leaderboard, ArrayList<String> playedWords, String currentPlayerName){
        Game.Created();
        Game.AddPanel("MAIN", "LAYOUT", new GridBagLayout());
        char[][] boardChar = board.board;
        Game.AddPanel("LAYOUT", "BOARD",new GridLayout(boardChar.length,boardChar[0].length));
        for(int row =0;row<boardChar.length;row++){
            for(char letter:boardChar[row]){
                Game.Panel("BOARD").AddText(Character.toString(letter),Character.toString(letter));
            }
        }

    }


    /**
     * Displays the lobby window. The graphical lobby shows a title screen
     * followed by the main menu. Because this implementation is still a
     * prototype it simply opens the window and returns based on whether the
     * Start button has been pressed. The text UI should be used for a
     * complete experience.
     *
     * @param options game options to be modified by user input
     * @return {@code true} if Start was pressed and at least one player has
     *         been added; {@code false} otherwise
     */
    public boolean lobby(GameOptions options) {
        ready=false;
        CompletableFuture<Boolean> GameStart = new CompletableFuture<>(); 
        //title screen
        if(!MainMenu.isCreated()){
            CreateMenu(options);
        }
        MainMenu.setVisible(true);
        

        System.out.println("exited");
        if(ready){
            MainMenu.dispose();
            return true;
        }else{
            System.out.println("bruh");
            return false;
        }

    }
    @Override
    /**
     * No‑op for the graphical UI. Resources such as windows and audio clips
     * are disposed elsewhere.
     */
    public void close() throws Exception {
        
    }


//no idea why i decided to make this a standalone method
    /**
     * Populates a panel with a grid of labels representing the letters on the
     * provided {@link Gameboard}. The parent {@link Windows} must already
     * have a layout suitable for the grid. Each character in the board is
     * rendered into its own label.
     *
     * @param board the current game board to display
     * @param parent the parent window in which to add the grid panels
     */
    private void makeGrid(Gameboard board,Windows parent){
        char[][] boardChar = board.board;
        parent.AddPanel("LAYOUT", "BOARD",new GridLayout(boardChar.length,boardChar[0].length));
        for(int row =0;row<boardChar.length;row++){
            for(char letter:boardChar[row]){
                parent.Panel("BOARD").AddText(Character.toString(letter),Character.toString(letter));
            }
        }
    }

    /**
     * Placeholder for creating {@link GridBagConstraints}. Not currently
     * implemented.
     *
     * @return always {@code null}
     */
    private GridBagConstraints bagLayout(){

        return null;
    }

    @Override
    /**
     * Displays the game window for the current turn. In the prototype the
     * method constructs a panel containing the letter grid and shows it.
     * Scoreboard and history display are not yet implemented.
     *
     * @param board the current board
     * @param leaderboard list of players and scores (unused)
     * @param playedWords list of previously played words (unused)
     * @param currentPlayerName name of the player whose turn it is (unused)
     */
    public void startTurn(Gameboard board, List<Entry<String, Integer>> leaderboard, ArrayList<String> playedWords, String currentPlayerName) {
        //game window construction
        if(Game.isCreated()){
            CreateGameWindow(board, leaderboard, playedWords, currentPlayerName);
        }

        Game.revalidate();
        Game.repaint();
        Game.setVisible(true);

    }
    @Override
    /**
     * Called when an AI player is thinking. Not implemented in the graphical
     * UI; throws {@link UnsupportedOperationException}.
     */
    public void passive() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'passive'");
    }
    @Override
    /**
     * Requests input from a human player. Not implemented in the graphical
     * UI; throws {@link UnsupportedOperationException}.
     *
     * @return the entered word (never returns in current implementation)
     */
    public String active() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'active'");
    }
    @Override
    /**
     * Reports the outcome of a player’s move. Not implemented in the
     * graphical UI; throws {@link UnsupportedOperationException}.
     */
    public void endTurn(TurnStatus status, String move, int scoreGained, int minWordLength) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'endTurn'");
    }
    @Override
    /**
     * Waits for the user to acknowledge the end of a turn. Not implemented
     * in the graphical UI; throws {@link UnsupportedOperationException}.
     */
    public void confirm() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'confirm'");
    }
    @Override
    public void confirmForSure() {
        confirm();
    }

    @Override
    /**
     * Displays the final results screen. Not implemented in the graphical UI;
     * throws {@link UnsupportedOperationException}.
     */
    public void results(List<Entry<String, Integer>> leaderboard, int skips, int maxScore) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'results'");
    }
    
}
