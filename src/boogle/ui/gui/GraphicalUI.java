package boogle.ui.gui;

import boogle.core.*;
import boogle.core.Launcher.GameOptions;
import boogle.player.AIPlayer;
import boogle.player.UIPlayer;
import boogle.sound.GameSound;

import javax.sound.sampled.Clip;
import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
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

    private String CurrentPlayer;

    boolean autoConfirm;

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

    KeyListener enter = new KeyListener() {
        final int KEY= KeyEvent.VK_ENTER;
        public void keyTyped(KeyEvent e) {
        }
        public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KEY){
               
            }
        }

        public void keyReleased(KeyEvent e) {
            
        }
        
    };


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
            return "AI";
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
            case "AI":    
                options.playerlist.add(new AIPlayer(name, boogle.player.AIPlayer.Level.fromValue(Integer.parseInt(level))));
            break;
            case "Human":
                options.playerlist.add(new UIPlayer(name));
            break;
            }
    }

    //windows setup
    /**
     * Builds the initial lobby window including title, player list and
     * settings buttons. This method is called lazily when the lobby is
     * first displayed.
     *
     * @param options configuration options used to populate the player list
     */
    private void CreateMenu(GameOptions options,CompletableFuture<Boolean> start){
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
        MainMenu.Panel("SETTINGS").AddButton("start", "Start", e ->{ready = startCheck(options);
            start.complete(true);
        });
        MainMenu.Panel("SETTINGS").AddButton( "quit","Quit",e ->{System.exit(0);});

    }



    //TODO: settings option
    //settings button methods
    //update playerlist
    //@SuppressWarnings("unchecked") //combobox warnings got annoying
    private void reEvalPlayerlist(GameOptions options){
        Settings.Panel("PLAYERLIST").Clear();
        ((JComboBox<?>)Settings.Panel("REMOVE").GetItem("Players")).removeAllItems();
        ((JComboBox<?>)Settings.Panel("SWAP").GetItem("FROM")).removeAllItems();
        ((JComboBox<?>)Settings.Panel("SWAP").GetItem("TO")).removeAllItems();
        Settings.Panel("PLAYERLIST").AddText("playerlist","Current Players: ");
        int i=0;
        for(Player players: options.playerlist){
            //so names will not interfere with other panels
            String name = players.toString();
            Settings.AddPanel("PLAYERLIST", name, new FlowLayout());
            Settings.Panel(name).AddText(name+" number",Integer.toString(options.playerlist.indexOf(players)+1));
            Settings.Panel(name).AddText(name+" name",players.getName());
            Settings.Panel(name).AddText(name+" type", getPlayerType(players));
            Settings.Panel(name).AddText(name+" level"," lvl "+getAILevel(players));
            ((JComboBox<String>)Settings.Panel("REMOVE").GetItem("Players")).addItem(Integer.toString(i+1));
            ((JComboBox<String>)Settings.Panel("SWAP").GetItem("FROM")).addItem(Integer.toString(i+1));
            ((JComboBox<String>)Settings.Panel("SWAP").GetItem("TO")).addItem(Integer.toString(i+1));
            i++;
        }
        if(i==0){
            ((JComboBox<?>)Settings.Panel("REMOVE").GetItem("Players")).addItem(null);
            ((JComboBox<String>)Settings.Panel("SWAP").GetItem("FROM")).addItem(null);
            ((JComboBox<String>)Settings.Panel("SWAP").GetItem("TO")).addItem(null);
        }
        ((JComboBox<?>)Settings.Panel("REMOVE").GetItem("Players")).setSelectedIndex(0);
        Settings.Panel("PLAYERLIST").add(Box.createVerticalGlue());
        Settings.Panel("PLAYERLIST").revalidate();
        Settings.Panel("PLAYERLIST").repaint();
    }
    private String[] playlistString(GameOptions options){
        if(options.playerlist.size()>0){
        String[] playerlist = new String[options.playerlist.size()];
        Arrays.setAll(playerlist, i -> Integer.toString(i));
        return playerlist;}
        else{
            return new String[1];
        }
    }    

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
        //Settings.windowSize((int)(Windows.getScreenSize().getWidth()/1.5),(int)Windows.getScreenSize().getHeight());
        //title
        Settings.AddPanel("MAIN","LAYOUT", new GridBagLayout());
        Settings.AddPanel("LAYOUT", "SETTINGS",new BoxLayout(Settings, BoxLayout.Y_AXIS));
        Settings.AddPanel("LAYOUT", "PLAYERLIST", new BoxLayout(Settings, BoxLayout.Y_AXIS));
       
       
        Settings.Panel("SETTINGS").AddText("title", "Settings");
        Settings.Panel("SETTINGS").GetItem("title").setFont(new Font("Ariel",Font.BOLD,30));
        Settings.Panel("SETTINGS").setAnchor("THIS","N");
        Settings.Panel("PLAYERLIST").setAnchor("THIS","NW");
        //settings
        //player 
        Settings.Panel("SETTINGS").AddText( "players", "Players:");
        Settings.AddPanel("SETTINGS", "PLAYERS",new FlowLayout());

        //playerlist
        Settings.Panel("PLAYERS").AddComboBox("PlayerSettingType", new String[]{"Human","AI"});
        Settings.Panel("PLAYERS").AddText("settingsplayernamelabel", "Name");
        Settings.Panel("PLAYERS").AddTextField("PlayerSettingName", 10);
        Settings.Panel("PLAYERS").AddText("settingsailevel", "ai level (ignored if human):");
        Settings.Panel("PLAYERS").AddComboBox("PlayerSettingLevel",new String[]{"1","2","3","4","5"});
        Settings.Panel("PLAYERS").AddButton("PlayerSettingSubmit","Submit",e->{
            validatePlayer(options,
            Settings.Panel("PLAYERS").GetItemText("PlayerSettingType"),
            Settings.Panel("PLAYERS").GetItemText("PlayerSettingName"),
            Settings.Panel("PLAYERS").GetItemText("PlayerSettingLevel")
        );
        reEvalPlayerlist(options);
        }); 

        //update players
        Settings.AddPanel("SETTINGS", "REMOVE",new FlowLayout());
        Settings.Panel("REMOVE").AddText("remove player","Remove Player");
        Settings.Panel("REMOVE").AddComboBox("Players",playlistString(options));
        Settings.Panel("REMOVE").AddButton("removeConfirm", "REMOVE",e->{
            try{
                options.playerlist.remove(Integer.parseInt(Settings.Panel("REMOVE").GetItemText("Players"))-1);
                reEvalPlayerlist(options);
            }catch(NullPointerException n){
            }
        });
        //swap player positions
        Settings.AddPanel("SETTINGS","SWAP",new FlowLayout());
        Settings.Panel("SWAP").AddText("Swap", "Swap:");
        Settings.Panel("SWAP").AddComboBox("FROM", playlistString(options));
        Settings.Panel("SWAP").AddText("text", "to");
        Settings.Panel("SWAP").AddComboBox("TO", playlistString(options));
        Settings.Panel("SWAP").AddButton("SWAPCONFIRM", "Swap!", e->{
            try{
                int from = Integer.parseInt(Settings.Panel("SWAP").GetItemText("FROM"))-1;
                int to =  Integer.parseInt(Settings.Panel("SWAP").GetItemText("TO"))-1;
                Player target = options.playerlist.get(from);

                options.playerlist.remove(from);
                options.playerlist.add(to, target);
                //if (this.playSfx) GameSound.ok();
            reEvalPlayerlist(options);
            }catch(NullPointerException n){
            }
        });
        
        //rename
        Settings.AddPanel("SETTINGS", "RENAME", new FlowLayout());
        Settings.Panel("RENAME").AddText("rename", "Rename: player#");
        Settings.Panel("RENAME").AddComboBox("player", playlistString(options));
        Settings.Panel("RENAME").AddTextField("newName", 10);

        
        Settings.pack();
        Settings.windowSize(Settings.getWidth()+500,Settings.getHeight());
        Settings.setVisible(true);
    }

    //Game Window
    //TODO: FIX LAYOUT
    private void CreateGameWindow(Gameboard board, List<Entry<String, Integer>> leaderboard, ArrayList<String> playedWords, String currentPlayerName){
        Game.Created();
        Game.windowSize(true);
        Game.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //close behavior
        //yes i copypasted this (i am not figuring this out on my own)
        Game.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int confirmed = JOptionPane.showConfirmDialog(Game, 
                    "Closing Window Will exit program\nPlease use exit button to exit Current Game", "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION);

                if (confirmed == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        Game.AddPanel("MAIN", "LAYOUT", new GridBagLayout());
        char[][] boardChar = board.board;
        Game.AddPanel("LAYOUT", "BOARD",new GridLayout(boardChar.length,boardChar[0].length,10,10));
        Game.Panel("LAYOUT").SetConstraint(Game.Panel("BOARD"), new GridBagConstraints(
            0,0,5,5,1,1,
            GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE,
            new Insets(20, 20, 0, 0),
            10,10
        ));
        for(int row =0;row<boardChar.length;row++){
            for(int col=0;col<boardChar[0].length;col++){
                String id = Integer.toString(row)+Integer.toString(col);
                Game.Panel("BOARD").AddText(id,Character.toString(boardChar[row][col]));
                Game.Panel("BOARD").setAnchor(id,"C");;
                Game.Panel("BOARD").GetItem(id).setFont(new Font("Ariel",Font.PLAIN,50));
            }
        }

        //leaderboard construction
        Game.AddPanel("LAYOUT", "LEADERBOARD",new GridLayout(2,leaderboard.size()+1));
        Game.Panel("LEADERBOARD").AddText("title", "SCOREBOARD");
        Game.Panel("LEADERBOARD").AddText(null, "");
        Game.Panel("LAYOUT").SetConstraint(Game.Panel("LEADERBOARD"),new GridBagConstraints(
             0,1,1,1,1,1,GridBagConstraints.NORTHEAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0
            ));
        for(int i=0;i<leaderboard.size();i++){
            Game.Panel("LEADERBOARD").AddText(leaderboard.get(i).getKey(), leaderboard.get(i).getKey());
            Game.Panel("LEADERBOARD").AddText(leaderboard.get(i).getKey()+"_Score","");
        }
        Game.Panel("LAYOUT").AddText("TurnName","");
        Game.Panel("LAYOUT").SetConstraint(Game.Panel("LAYOUT").GetItem("TurnName"),new GridBagConstraints(
            0,6,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0)
        );

        Game.AddPanel("LAYOUT","TURNSTATUS", new BoxLayout(Game, BoxLayout.Y_AXIS));
        Game.Panel("LAYOUT").SetConstraint(Game.Panel("TURNSTATUS"), new GridBagConstraints(
            0,7,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0)
    );

        Game.AddPanel("LAYOUT","STATUS",new FlowLayout());
        Game.Panel("LAYOUT").SetConstraint(Game.Panel("STATUS"),new GridBagConstraints(
            0,8,1,1,1,1,GridBagConstraints.LAST_LINE_START,GridBagConstraints.NONE,new Insets(0, 0, 0, 0),0,0)
        );

    }

    private void CreateResult(List<Entry<String, Integer>> leaderboard, int skips, int maxScore){
        Results.Clear();
        CompletableFuture<Boolean> toLobby = new CompletableFuture<>(); 

        Results.AddPanel("MAIN", "LAYOUT", new BoxLayout(Game, BoxLayout.Y_AXIS));
        Results.AddPanel("LAYOUT", CurrentPlayer, null);

        Results.Panel("LAYOUT").AddButton("Lobby", "Continue >", e->{toLobby.complete(true);});
        Results.pack();
        Results.setVisible(true);

        try{toLobby.get();}catch(Exception e){}
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
            CreateMenu(options,GameStart);
        }
        MainMenu.setVisible(true);
        

        try {
            if(GameStart.get()){
                MainMenu.dispose();
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {

        }
        return false;

    }
    /**
     * No‑op for the graphical UI. Resources such as windows and audio clips
     * are disposed elsewhere.
     */
    public void close() throws Exception {
        
    }

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
        if(!Game.isCreated()){
            CreateGameWindow(board, leaderboard, playedWords, currentPlayerName);
        }
        Game.Panel("TURNSTATUS").Clear();

        CurrentPlayer = currentPlayerName;
        ((JLabel)Game.Panel("LAYOUT").GetItem("TurnName")).setText(currentPlayerName+"'s Turn");
        
        for(int i=0;i<leaderboard.size();i++){
            ((JLabel)Game.Panel("LEADERBOARD").GetItem(leaderboard.get(i).getKey()+"_Score")).setText(Integer.toString(leaderboard.get(i).getValue()));
        }
        Game.repaint();
        Game.setVisible(true);

    }

    /**
     * Called when an AI player is thinking. Not implemented in the graphical
     * UI; throws {@link UnsupportedOperationException}.
     */
    public void passive() {
        Game.Panel("STATUS").Clear();
        Game.Panel("STATUS").AddText("CurrentAI", "Current AI "+CurrentPlayer+" is thinking");

    }


    /**
     * Requests input from a human player. Not implemented in the graphical
     * UI; throws {@link UnsupportedOperationException}.
     *
     * @return the entered word (never returns in current implementation)
     */
    public String active() {
        CompletableFuture<String> Input = new CompletableFuture<>();

        Game.Panel("STATUS").Clear();
        Game.Panel("STATUS").AddText("Play", "Submit Word");
        Game.Panel("STATUS").AddTextField("UserInput", 18);
        Game.Panel("STATUS").AddButton("Submit","Submit",e->{
            Input.complete(Game.Panel("STATUS").GetItemText("UserInput"));
        });
        Game.Panel("STATUS").AddButton("Skip", "Skip", e->{Input.cancel(true);});
        Game.revalidate();
        Game.repaint();
        try{
            return Input.get();
        }catch(Exception e){
            return null;
        }
    }
    
    /**
     * Reports the outcome of a player’s move. Not implemented in the
     * graphical UI; throws {@link UnsupportedOperationException}.
     */
    public void endTurn(TurnStatus status, String move, int scoreGained, int minWordLength) {
        Game.Panel("TURNSTATUS").AddText("TurnStatus", "");
        switch (status) {
            case OK:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText(CurrentPlayer+" played the word '"+move+"' and gained "+scoreGained+" score!");
                //if (this.playSfx) GameSound.ok();
            break;
            case SKIPPED:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText(CurrentPlayer+" skipped their turn!");
                //if (this.playSfx) GameSound.bad();
            break;
            case TOO_SHORT:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("Word too short! Minimal word length is" + minWordLength);
                //if (this.playSfx) GameSound.bad();
                
            break;
            case DUPLICATE:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("This word was already played! Try a different one.");
                //if (this.playSfx) GameSound.bad();
            break;
            case NOT_IN_DICT:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("Word not in wordlist! Try a different one.");
                //if (this.playSfx) GameSound.bad();
            break;
            case NOT_ON_BOARD:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("Word does not exist on board! Try a different one");
                //if (this.playSfx) GameSound.bad();
            break;
        }
    }
    
    /**
     * Waits for the user to acknowledge the end of a turn. Not implemented
     * in the graphical UI; throws {@link UnsupportedOperationException}.
     */
    public void confirm() {
        if (this.autoConfirm) {
            try { Thread.sleep(1000); }
            catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            CompletableFuture<Boolean> enter = new CompletableFuture<>();
            Game.Panel("TURNSTATUS").AddButton("ConfirmButton", "Continue >", e->{enter.complete(true);});
            Game.revalidate();
            Game.repaint();
            try{
                enter.get();
            }catch(Exception e){}
        }
    }
    

    public void confirmForSure() {
        //not needed cuz gui blocking
    }

    
    /**
     * Displays the final results screen. Not implemented in the graphical UI;
     * throws {@link UnsupportedOperationException}.
     */
    public void results(List<Entry<String, Integer>> leaderboard, int skips, int maxScore) {
        // TODO Auto-generated method stub
        CreateResult(leaderboard, skips, maxScore);

        Game.dispose();
        

        throw new UnsupportedOperationException("Unimplemented method 'results'");
    }
    
}
