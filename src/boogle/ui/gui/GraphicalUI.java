/*
 * File: GraphicalUI.java
 * Author: Eric
 * Description: Implements the Swing-based graphical user interface for Boogle lobby and gameplay interactions.
 */

package boogle.ui.gui;

import boogle.core.*;
import boogle.core.Launcher.GameOptions;
import boogle.core.Player.Move;
import boogle.player.AIPlayer;
import boogle.player.UIPlayer;
import boogle.sound.GameSound;

import javax.sound.sampled.Clip;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;


//hi welcome to hell (efficiency is no longer a consideration (mostly))

/**
 * Swing implementation of the Boogle user interface.
 *
 * <p>The GUI builds separate windows for the lobby, settings, game board, and
 * results. It uses named panels from {@link Windows} so event handlers can
 * update player lists, score labels, played words, and active-turn controls as
 * the game engine advances.</p>
 */
public class GraphicalUI implements GameUI{
    /**
     * Creates a graphical UI with default settings and lazily built windows.
     */
    public GraphicalUI() {
    }

    private Clip audio;

    private String CurrentPlayer;

    /**
     * Automatic confirmation timing options for between-turn pauses.
     */
    private enum Speed{
        Fast,
        Normal,
        OFF,
    }
    private Speed autoConfirm = Speed.OFF;
    private boolean playMusic = false;
    private boolean playSfx = false;

    /*
    naming convention:
        Windows - upper
        PANEL - all caps
        component - lower
    */
    private Windows MainMenu = new Windows("lobby");
    private Windows Game = new Windows("Game");
    private Windows Settings = new Windows("Settings");
    private Windows Results = new Windows("Results");



    //button methods
    /**
     * Checks whether the lobby has enough players to start and warns otherwise.
     */
    private Boolean startCheck(GameOptions options){
        if (options.playerlist.size() != 0) {
            //audio.stop();
            //audio = GameSound.ingame();
            //GameSound.ok();
            return true;
        }
        Windows.CreateDialog(null,Windows.SubwindowOption.WARNING, "Settings Missing", "Players Missing\nPlease Open Settings to Add Players");
        return false;
    }


    /**
     * Replaces the first action listener on a button with a new one.
     */
    private void setActionListener(JComponent comp, ActionListener event){
        if(comp instanceof JButton){
            try{
            ((JButton)comp).removeActionListener(((JButton)comp).getActionListeners()[0]);
            }catch(IndexOutOfBoundsException n){
            }
            ((JButton)comp).addActionListener(event);
            
        }
    }

    /**
     * Returns the display type label for a player.
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
     * Returns an AI player's numeric level for settings display.
     */
    private String getAILevel(Player player){
        if(player instanceof AIPlayer){
            return Integer.toString(((AIPlayer)player).getLevel().getValue());
        }
        return null;
    }
    /**
     * Adds a player to the options after validating the selected type and level.
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
     * Creates or refreshes the main lobby window and its start/quit actions.
     */
    private void CreateMenu(GameOptions options,CompletableFuture<Boolean> start){
        MainMenu.windowSize(true);
        MainMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if(!MainMenu.isCreated()){
            MainMenu.AddPanel("MAIN", "LAYOUT", new BoxLayout(Game, BoxLayout.Y_AXIS));
            MainMenu.AddPanel("LAYOUT","TITLE",new FlowLayout());
            MainMenu.Panel("LAYOUT").AddText("welcome", "Welcome to Boggle(yes the typo is on purpose)!");
            MainMenu.Panel("LAYOUT").AddText("warning", "Please Proceed to Settings to set up the Game!");
            Windows.setAnchor(MainMenu.Panel("LAYOUT").GetItem("warning"), Windows.direct.CENTER);
            Windows.setAnchor(MainMenu.Panel("LAYOUT").GetItem("welcome"), Windows.direct.CENTER);
            MainMenu.AddPanel("LAYOUT", "SETTINGS", new FlowLayout());
            MainMenu.Panel("SETTINGS").AddButton("settings","Settings",e->{
                CreateSettings(options,start);
            });
            MainMenu.Panel("TITLE").AddText("title", "BOOGLE");
            MainMenu.Panel("TITLE").GetItem("title").setFont(new Font("Ariel",Font.BOLD,100));
            MainMenu.Panel("SETTINGS").AddButton("start", "Start", null);
            MainMenu.Panel("SETTINGS").AddButton( "quit","Quit",null);
        }
        setActionListener(MainMenu.Panel("SETTINGS").GetItem("quit"),(e->{
                start.complete(false);
                MainMenu.dispose();
        }));
        setActionListener(MainMenu.Panel("SETTINGS").GetItem("start"),(e ->{
            if(startCheck(options)){
                start.complete(true);
                MainMenu.dispose();
        }}));
        
        MainMenu.Created();
    }


    //settings button methods
    //update playerlist
    @SuppressWarnings("unchecked") //combobox warnings got annoying
    /**
     * Rebuilds player-list display panels and player-selection combo boxes.
     */
    private void reEvalPlayerlist(GameOptions options){
        Settings.Panel("PLAYERLIST").Clear();
        ((JComboBox<?>)Settings.Panel("REMOVE").GetItem("Players")).removeAllItems();
        ((JComboBox<?>)Settings.Panel("SWAP").GetItem("FROM")).removeAllItems();
        ((JComboBox<?>)Settings.Panel("SWAP").GetItem("TO")).removeAllItems();
        ((JComboBox<?>)Settings.Panel("RENAME").GetItem("player")).removeAllItems();
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
            ((JComboBox<String>)Settings.Panel("RENAME").GetItem("player")).addItem(Integer.toString(i+1));
            i++;
        }
        if(i==0){
            ((JComboBox<?>)Settings.Panel("REMOVE").GetItem("Players")).addItem(null);
            ((JComboBox<?>)Settings.Panel("SWAP").GetItem("FROM")).addItem(null);
            ((JComboBox<?>)Settings.Panel("SWAP").GetItem("TO")).addItem(null);
            ((JComboBox<?>)Settings.Panel("RENAME").GetItem("player")).addItem(null);
        }
        Settings.Panel("PLAYERLIST").add(Box.createVerticalGlue());
        Settings.Panel("PLAYERLIST").revalidate();
        Settings.Panel("PLAYERLIST").repaint();
    }

    /**
     * Applies game and presentation settings from the settings window.
     */
    private void SettingsChange(GameOptions options,String boardpath,String wordlist, int winScore,int MinLength,String Auto,String Music, String SFX ){    
        try { 
            
            if (options.winScore < 0){
                throw new IllegalArgumentException("Win Score");
            } else{
                options.winScore=winScore;
            }
            
            if (options.minWordLength < 0){
                throw new IllegalArgumentException("Min Score");
            }else{
                options.minWordLength = MinLength;
            }
            
            if (boardpath.isEmpty()) {
                    options.customBoard = null;
            }else{
            char[][] customBoard = Launcher.loadGameboardFile(boardpath);
                if (customBoard != null) {
                    options.customBoard = customBoard;
                }else{throw new IOException(boardpath);}
            }
            if(!wordlist.isEmpty()){
                (new FileReader(wordlist)).close();
                options.wordlistPath = wordlist;
            }

            switch(Auto){
                case "FAST":
                    autoConfirm=Speed.Fast;
                    break;
                case "NORMAL":
                    autoConfirm=Speed.Normal;
                    break;
                case "OFF":
                    autoConfirm=Speed.OFF;
                    break;
            }
            if(Music.equals("ON")){
                if (!this.playMusic) {
                    this.playMusic = true;
                    this.audio = GameSound.lobby();
                }
            }else{
                this.playMusic=false;
                this.audio = GameSound.nothing();
            }    
            if(SFX.equals("ON")){
                if (!this.playSfx) {
                    playSfx = true;
                }
            }else{
                playSfx=false;
            }
            Windows.CreateDialog(Settings,Windows.SubwindowOption.INFO,"Success", "Game Settings Successfully set");
        }catch(IllegalArgumentException e){
            Windows.CreateDialog(Settings,Windows.SubwindowOption.ERROR, "Invalid Argument", "Invalid Argument Detected"
            +"\n{winScore>0}\n{MinWord}\nerror:"+e.getMessage());
        }catch(IOException e){
            Windows.CreateDialog(Settings,Windows.SubwindowOption.ERROR, "File Access Error", "Can't Access file:\n"+e.getMessage());
        }
    }

    //settings window
    /**
     * Creates or refreshes the settings window and all of its action listeners.
     */
    private void CreateSettings(GameOptions options,CompletableFuture<Boolean> start){
        Settings.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //title+layout
        if(!Settings.isCreated()){
            Settings.AddPanel("MAIN","LAYOUT", new FlowLayout());
            Settings.AddPanel("LAYOUT", "SETTINGS",new BoxLayout(Settings, BoxLayout.Y_AXIS));
            Settings.AddPanel("LAYOUT", "PLAYERLIST", new BoxLayout(Settings, BoxLayout.Y_AXIS));
            Settings.Panel("SETTINGS").AddText("title", "Settings");
            Settings.Panel("SETTINGS").AddText("notice", "<html>NOTICE: all settings Sections are saved individually<br>close this window after submitting<html>");
            Settings.Panel("SETTINGS").AddText( "addplayertitle", "Add Player:");
            Settings.AddPanel("SETTINGS", "PLAYERS",new FlowLayout());
        }
        Settings.Panel("SETTINGS").GetItem("title").setFont(new Font("Ariel",Font.BOLD,30));
        Settings.Panel("SETTINGS").GetItem("notice").setFont(new Font("Ariel",Font.ITALIC,15));
        Windows.setAnchor(Settings.Panel("SETTINGS").GetItem("notice"),Windows.direct.CENTER);
        Windows.setAnchor(Settings.Panel("SETTINGS"),Windows.direct.NORTH);
        Windows.setAnchor(Settings.Panel("PLAYERLIST"),Windows.direct.NORTHWEST);
        //settings

        //player 
        if(!Settings.isCreated()){
        Settings.Panel("PLAYERS").AddComboBox("PlayerSettingType", new String[]{"Human","AI"});
        Settings.Panel("PLAYERS").AddText("settingsplayernamelabel", "Name");
        Settings.Panel("PLAYERS").AddTextField("PlayerSettingName", 10);
        Settings.Panel("PLAYERS").AddText("settingsailevel", "ai level (ignored if human):");
        Settings.Panel("PLAYERS").AddComboBox("PlayerSettingLevel",new String[]{"1","2","3","4","5"});
        Settings.Panel("PLAYERS").AddButton("PlayerSettingSubmit","Submit",null);
        }
        setActionListener(Settings.Panel("PLAYERS").GetItem("PlayerSettingSubmit"),(e->{
            validatePlayer(options,
            Settings.Panel("PLAYERS").GetItemText("PlayerSettingType"),
            Settings.Panel("PLAYERS").GetItemText("PlayerSettingName"),
            Settings.Panel("PLAYERS").GetItemText("PlayerSettingLevel")
        );
            reEvalPlayerlist(options);
            if (this.playSfx) GameSound.ok();
        }));
         
        //update players
        if(!Settings.isCreated()){
        Settings.Panel("SETTINGS").AddText( "playersettingstitle", "Player Settings");
        Settings.AddPanel("SETTINGS", "REMOVE",new FlowLayout());
        Settings.Panel("REMOVE").Clear();
        Settings.Panel("REMOVE").AddText("remove player","Remove Player");
        Settings.Panel("REMOVE").AddComboBox("Players",new String[1]);
        Settings.Panel("REMOVE").AddButton("removeConfirm", "REMOVE",null);
        }
        setActionListener(Settings.Panel("REMOVE").GetItem("removeConfirm"),(e->{
            try{
                options.playerlist.remove(Integer.parseInt(Settings.Panel("REMOVE").GetItemText("Players"))-1);
                reEvalPlayerlist(options);
                if (this.playSfx) GameSound.ok();
            }catch(Exception n){
            }
        }));
        //swap player positions
        if(!Settings.isCreated()){
        Settings.AddPanel("SETTINGS","SWAP",new FlowLayout());
        Settings.Panel("SWAP").Clear();
        Settings.Panel("SWAP").AddText("Swap", "Swap:");
        Settings.Panel("SWAP").AddComboBox("FROM", new String[1]);
        Settings.Panel("SWAP").AddText("text", "to");
        Settings.Panel("SWAP").AddComboBox("TO", new String[1]);
        Settings.Panel("SWAP").AddButton("SWAPCONFIRM", "Swap!",null); 
        }
        setActionListener(Settings.Panel("SWAP").GetItem("SWAPCONFIRM"),(e->{
            try{
                int from = Integer.parseInt(Settings.Panel("SWAP").GetItemText("FROM"))-1;
                int to =  Integer.parseInt(Settings.Panel("SWAP").GetItemText("TO"))-1;
                Player target = options.playerlist.get(from);

                options.playerlist.remove(from);
                options.playerlist.add(to, target);
                if (this.playSfx) GameSound.ok();
                reEvalPlayerlist(options);
            }catch(Exception n){
                if (this.playSfx) GameSound.bad();
            }
        }));
        
        //rename
        if(!Settings.isCreated()){
        Settings.AddPanel("SETTINGS", "RENAME", new FlowLayout());
        Settings.Panel("RENAME").Clear();
        Settings.Panel("RENAME").AddText("rename", "Rename: player#");
        Settings.Panel("RENAME").AddComboBox("player", new String[1]);
        Settings.Panel("RENAME").AddTextField("newName", 10);
        Settings.Panel("RENAME").AddButton("confirm", "Rename",null);
        } 
        setActionListener(Settings.Panel("RENAME").GetItem("confirm"),(e->{
            try{
            options.playerlist.get(Integer.parseInt(Settings.Panel("RENAME").GetItemText("player"))-1).setName(
                Settings.Panel("RENAME").GetItemText("newName"));
            reEvalPlayerlist(options);
            if (this.playSfx) GameSound.ok();
        }catch(Exception n){}
        }));

        //game settings
        if(!Settings.isCreated()){
        Settings.Panel("SETTINGS").AddText("GameSettings", "Game Settings");
        Settings.Panel("SETTINGS").AddText("CurrentSettings", "");
        Settings.AddPanel("SETTINGS","BOARD",new FlowLayout());
        Settings.AddPanel("SETTINGS","WORDLIST",new FlowLayout());
        Settings.AddPanel("SETTINGS", "WINSCORE", new FlowLayout());
        Settings.AddPanel("SETTINGS","AUTOCONFIRM",new FlowLayout());
        Settings.AddPanel("SETTINGS","MINWORDLENGTH",new FlowLayout());
        Settings.AddPanel("SETTINGS","MUSIC" , new FlowLayout());
        Settings.AddPanel("SETTINGS", "SFX", new FlowLayout());
        Settings.Panel("SETTINGS").AddButton("GAMEOPTIONSUBMIT", "Submit Game Settings", null);
        Windows.setAnchor(Settings.Panel("SETTINGS").GetItem("GAMEOPTIONSUBMIT"), Windows.direct.CENTER);
        
        Settings.Panel("BOARD").AddText("", "BOARD PATH (leave blank for random):");
        Settings.Panel("BOARD").AddTextField("BOARDPATH", 18);

        Settings.Panel("WORDLIST").AddText("", "Wordlist.txt path:");
        Settings.Panel("WORDLIST").AddTextField("WORDLISTPATH", 18);

        Settings.Panel("WINSCORE").AddText("", "Win Score:");
        Settings.Panel("WINSCORE").AddTextField("WinScore", 4);
        
        Settings.Panel("AUTOCONFIRM").AddText("", "Auto Confirm");
        Settings.Panel("AUTOCONFIRM").AddComboBox("AUTOCONFIRM", new String[]{"OFF","NORMAL","FAST"});

        Settings.Panel("MUSIC").AddText("", "Music:");
        Settings.Panel("MUSIC").AddComboBox("MusicStat", new String[]{"OFF","ON"});

        Settings.Panel("SFX").AddText("", "Sfx: ");
        Settings.Panel("SFX").AddComboBox("SFXStat", new String[]{"OFF","ON"});
        
        Settings.Panel("MINWORDLENGTH").AddText("", "Min Word Length:");
        Settings.Panel("MINWORDLENGTH").AddTextField("MinWord", 4);
        }
        setActionListener(Settings.Panel("SETTINGS").GetItem("GAMEOPTIONSUBMIT"),(e->{
            try{
                int winscore=0;
                int minlength=0;
                if(!Settings.Panel("WINSCORE").GetItemText("WinScore").isEmpty()){
                    winscore=Integer.parseInt(Settings.Panel("WINSCORE").GetItemText("WinScore"));
                }
                if(!Settings.Panel("MINWORDLENGTH").GetItemText("MinWord").isEmpty()){
                    minlength=Integer.parseInt(Settings.Panel("MINWORDLENGTH").GetItemText("MinWord"));
                }
                SettingsChange(options,
                    Settings.Panel("BOARD").GetItemText("BOARDPATH"),
                    Settings.Panel("WORDLIST").GetItemText("WORDLISTPATH"),
                    winscore,minlength,
                    Settings.Panel("AUTOCONFIRM").GetItemText("AUTOCONFIRM"),
                    Settings.Panel("MUSIC").GetItemText("MusicStat"),
                    Settings.Panel("SFX").GetItemText("SFXStat")
                );
            }catch(Exception n){
                Windows.CreateDialog(null,Windows.SubwindowOption.WARNING, "Argument Error", "There was an error Parsing field\nPlease try Again\nError Cause: "+n.getMessage());
            }
        }));

        //loading 
        if(!Settings.isCreated()){
            Settings.Panel("SETTINGS").AddText("LOADTITLE", "Load Game");
            Settings.AddPanel("SETTINGS", "LOAD", new FlowLayout());
            Settings.Panel("LOAD").AddText("loadtitle", "Load File Path:");
            Settings.Panel("LOAD").AddTextField("SAVEPATH", 18);
            Settings.Panel("LOAD").AddButton("LOADBUTTON", "Load", null);
        }
        setActionListener(Settings.Panel("LOAD").GetItem("LOADBUTTON"),(e->{
            try{
                if(Settings.Panel("LOAD").GetItemText("SAVEPATH").isEmpty()){
                    //empty error popup was kinda funny
                    throw new IOException("Field is blank");
                }
                options.replacement = Launcher.fromSerialized(new FileInputStream(
                    Settings.Panel("LOAD").GetItemText("SAVEPATH")), this);
                    Windows.CreateDialog(Settings, Windows.SubwindowOption.INFO, "Success", "Loading Success!");
                    MainMenu.dispose();
                    Settings.dispose();
                    start.complete(true);
                    if (this.playSfx) GameSound.ok();
            }catch(IOException n){
                Windows.CreateDialog(Settings,Windows.SubwindowOption.ERROR,"Unable to load",n.getMessage());
            }catch(ClassNotFoundException n){
                Windows.CreateDialog(Settings,Windows.SubwindowOption.ERROR,"Save File not Compatible",n.getMessage());
            }
        }));
 

        Settings.Created();
        Settings.pack();
        Settings.windowSize(Settings.getWidth()+500,Settings.getHeight());
        reEvalPlayerlist(options);
        Settings.setVisible(true);
    }

    //Game Window
    /**
     * Creates or refreshes the game window for the current board and scoreboard.
     */
    private void CreateGameWindow(Gameboard board, List<Entry<String, Integer>> leaderboard, ArrayList<String> playedWords, String currentPlayerName){
        char[][] boardChar = board.board;
        if(!Game.isCreated()){
            Game.AddPanel("MAIN", "LAYOUT", new GridBagLayout());
            Game.AddPanel("LAYOUT", "BOARD",new GridLayout(boardChar.length,boardChar[0].length,10,10));
            Game.Panel("LAYOUT").AddText("PLAYEDWORDSTITLE", "Played Words");
            Game.AddPanel("LAYOUT", "PLAYEDWORDS",new FlowLayout());
            Game.AddPanel("LAYOUT", "LEADERBOARD",new GridLayout(0,2));
            Game.AddPanel("LAYOUT","TURNSTATUS", new BoxLayout(Game, BoxLayout.Y_AXIS));
            Game.Panel("LAYOUT").AddText("TurnName","");
            Game.AddPanel("LAYOUT","STATUS",new FlowLayout());
        }else{
            Game.Panel("LEADERBOARD").Clear();
            Game.Panel("LEADERBOARD").setLayout(new GridLayout(0,2));
            Game.Panel("BOARD").Clear();
            Game.Panel("BOARD").setLayout(new GridLayout(boardChar.length,boardChar[0].length,10,10));
        }
        Game.windowSize(true);
        Game.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //close behavior
        //yes i copypasted this (i am not figuring this out on my own)
        Game.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int confirmed = JOptionPane.showConfirmDialog(Game, 
                    "Closing Window Will exit program", "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION);
                if (confirmed == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        //gameboard
        for(int row =0;row<boardChar.length;row++){
            for(int col=0;col<boardChar[0].length;col++){
                String id = Integer.toString(row)+Integer.toString(col);
                Game.Panel("BOARD").AddText(id,Character.toString(boardChar[row][col]));
                Windows.setAnchor(Game.Panel("BOARD").GetItem(id),Windows.direct.CENTER);
                Game.Panel("BOARD").GetItem(id).setFont(new Font("Ariel",Font.PLAIN,50));
            }
        }
        
        //leaderboard construction
        Game.Panel("LEADERBOARD").AddText("title", "SCOREBOARD");
        Game.Panel("LEADERBOARD").AddText(null, "");
        for(int i=0;i<leaderboard.size();i++){
            Game.Panel("LEADERBOARD").AddText(leaderboard.get(i).getKey(), leaderboard.get(i).getKey());
            Game.Panel("LEADERBOARD").AddText(leaderboard.get(i).getKey()+"_Score","");
        }

        //constraints
        //board
        Game.Panel("LAYOUT").SetConstraint(Game.Panel("BOARD"), new GridBagConstraints(
            0,0,5,5,0,0,
            GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE,
            new Insets(20, 20, 0, 0),
            0,0));
         //played words
        Game.Panel("LAYOUT").SetConstraint(Game.Panel("LAYOUT").GetItem("PLAYEDWORDSTITLE"), new GridBagConstraints(
            7, 0, 1, 1, 0, 0, 
            GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0),
             0, 0));
        Game.Panel("LAYOUT").SetConstraint(Game.Panel("PLAYEDWORDS"),new GridBagConstraints(
            7, 1, 4, 5, 1, 1, 
            GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(20, 20, 20, 20), 
            0, 0));
        //leaderboard
                Game.Panel("LAYOUT").SetConstraint(Game.Panel("LEADERBOARD"),new GridBagConstraints(
             10,1,2,1,0,0,
             GridBagConstraints.NORTHEAST,GridBagConstraints.VERTICAL,new Insets(0,0,0,0),
             0,0));
        //turn info
        Game.Panel("LAYOUT").SetConstraint(Game.Panel("LAYOUT").GetItem("TurnName"),new GridBagConstraints(
            0,10,12,1,0,0,
            GridBagConstraints.LAST_LINE_START,GridBagConstraints.NONE,new Insets(0,0,0,0),
            0,0));
        Game.Panel("LAYOUT").SetConstraint(Game.Panel("TURNSTATUS"), new GridBagConstraints(
            0,11,12,1,0,0,
            GridBagConstraints.LAST_LINE_START,GridBagConstraints.NONE,new Insets(0,0,0,0),
            0,0));
        Game.Panel("LAYOUT").SetConstraint(Game.Panel("STATUS"),new GridBagConstraints(
            0,12,12,1,0,0,
            GridBagConstraints.LAST_LINE_START,GridBagConstraints.NONE,new Insets(0, 0, 0, 0),
            0,0));

        Game.Created();
    }

    //create result screen
    /**
     * Creates or refreshes the results window and its continue action.
     */
    private void CreateResult(List<Entry<String, Integer>> leaderboard, int skips, int maxScore,CompletableFuture<Void> toLobby){
        Results.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if(!Results.isCreated()){ 
            Results.AddPanel("MAIN", "LAYOUT", new BoxLayout(Game, BoxLayout.Y_AXIS));
            Results.Panel("LAYOUT").AddText("title", "RESULT");
            Windows.setAnchor(Results.Panel("LAYOUT").GetItem("title"), Windows.direct.CENTER);
            Results.Panel("LAYOUT").GetItem("title").setFont(new Font("Ariel",Font.BOLD,50));
            Results.AddPanel("LAYOUT", "LEADERBOARD",new GridLayout(leaderboard.size()+1,3));
            Results.Panel("LAYOUT").AddText("seperator", "================");
            Windows.setAnchor(Results.Panel("LAYOUT").GetItem("seperator"), Windows.direct.CENTER);
            Results.Panel("LAYOUT").AddText("percentComplete", "");
            Results.Panel("LAYOUT").AddText("SkipTotal", "");
        }else{
            Results.Panel("LEADERBOARD").Clear();
            Results.Panel("LEADERBOARD").setLayout(new GridLayout(leaderboard.size()+1,2));
        }
        
        //leaderboard construct
        Results.Panel("LEADERBOARD").AddText("title", "LEADERBOARD:");
        Results.Panel("LEADERBOARD").AddText(null, "");
        Results.Panel("LEADERBOARD").AddText(null, "");
        int totscore=0;
        for(int i=0;i<leaderboard.size();i++){
            int score=leaderboard.get(i).getValue();
            Results.Panel("LEADERBOARD").AddText(leaderboard.get(i).getKey(), leaderboard.get(i).getKey());
            Results.Panel("LEADERBOARD").AddText(leaderboard.get(i).getKey()+"_Score",Integer.toString(score));
            Results.Panel("LEADERBOARD").AddText(leaderboard.get(i).getKey()+"_Completions",Integer.toString(100*score/maxScore)+"%");

            totscore+=leaderboard.get(i).getValue();
        }

        //completion %
        if(maxScore==0){
            ((JLabel)Results.Panel("LAYOUT").GetItem("percentComplete")).setText("Completion: N/A");
        }else{
            ((JLabel)Results.Panel("LAYOUT").GetItem("percentComplete")).setText("Completion: "+(100*totscore/maxScore)+"%");
        }

        //skips
        ((JLabel)Results.Panel("LAYOUT").GetItem("SkipTotal")).setText("Skipped turns: "+skips);

        if(!Results.isCreated()){
            Results.Panel("LAYOUT").AddButton("Lobby", "Continue >", null);
        }
        setActionListener(Results.Panel("LAYOUT").GetItem("Lobby"),(e->{toLobby.complete(null);}));
        Results.Created();
        Results.pack();
    }


    /**
     * Shows the Swing lobby and waits until the user starts, loads, or quits.
     */
    public boolean lobby(GameOptions options) {
        CompletableFuture<Boolean> GameStart = new CompletableFuture<>(); 
        //title screen
        
        CreateMenu(options,GameStart);
        MainMenu.setVisible(true);
        try{
            audio.stop();
        }catch(Exception e){}
        if (this.playMusic){ audio = GameSound.lobby();}
        else{audio = GameSound.nothing();}
        try {
            if(GameStart.get()){
                Settings.dispose();
                audio.stop();
                if (this.playMusic) audio = GameSound.ingame();
            }
            return GameStart.get();
        } catch (Exception e) {
        }
        return false;
    }
    /**
     * Closes this UI; currently no Swing-specific cleanup is required here.
     */
    public void close() throws Exception {
    }

    /**
     * Updates and shows the game window for the beginning of a turn.
     */
    public void startTurn(Gameboard board, List<Entry<String, Integer>> leaderboard, ArrayList<String> playedWords, String currentPlayerName) {
        //game window construction
        CreateGameWindow(board, leaderboard, playedWords, currentPlayerName);
        
        Game.Panel("TURNSTATUS").Clear();

        CurrentPlayer = currentPlayerName;
        ((JLabel)Game.Panel("LAYOUT").GetItem("TurnName")).setText(currentPlayerName+"'s Turn:");
        
        for(int i=0;i<leaderboard.size();i++){
            ((JLabel)Game.Panel("LEADERBOARD").GetItem(leaderboard.get(i).getKey()+"_Score")).setText(Integer.toString(leaderboard.get(i).getValue()));
        }
        //played words
        Game.Panel("PLAYEDWORDS").Clear();
        for(String word:playedWords){
            Game.Panel("PLAYEDWORDS").AddText(word, " "+word+" ");
        }
        
        Game.repaint();
        Game.setVisible(true);
    }

    /**
     * Clears interactive controls while an AI player acts.
     */
    public void passive() {
        Game.Panel("STATUS").Clear();
        Game.repaint();
    }


    /**
     * Shows human turn controls and waits for the selected move.
     */
    public Player.Move active(boolean shakeUpBoard) {
        CompletableFuture<Player.Move> Input = new CompletableFuture<>();
        if(shakeUpBoard){
            Game.Panel("TURNSTATUS").AddText("shake", "Try shaking up the board");
        }

        Game.Panel("STATUS").Clear();
        Game.Panel("STATUS").AddText("Play", "Submit Word");
        Game.Panel("STATUS").AddTextField("UserInput", 18);
        Game.Panel("STATUS").AddButton("Submit","Submit",e->{
            Input.complete(new Move(Player.Move.Type.WORD,Game.Panel("STATUS").GetItemText("UserInput")));
        });
        Game.Panel("STATUS").AddButton("Skip", "Skip", e->{Input.complete(new Move(Move.Type.SKIP));});
        Game.Panel("STATUS").AddButton("ShakeBoard", "Shake up Board", e->{
            Game.Panel("TURNSTATUS").Clear();
            Game.Panel("TURNSTATUS").AddText("shaking", "Shaking board...");
            Input.complete(new Move(Move.Type.SHAKE));
        });
        Game.Panel("STATUS").AddButton("GiveUp", "Give up", e->{Input.complete(new Move(Move.Type.LEAVE));});
        Game.Panel("STATUS").AddText("saveTitle", "Save Game:");
        Game.Panel("STATUS").AddTextField("SavePath", 20);
        Game.Panel("STATUS").AddButton("SaveGame", "Save", e->{Input.complete(new Move(Move.Type.SAVE, Game.Panel("STATUS").GetItemText("SavePath")));});
        Game.Panel("STATUS").AddButton("ExitGame","Exit",e->{Input.complete(new Move(Move.Type.STOP));});

        Game.revalidate();
        Game.repaint();
        try{
            return Input.get();
        }catch(Exception e){
            return new Move(Move.Type.SKIP);
        }
    }
    
    /**
     * Displays the result of the most recent turn in the game window.
     */
    public void endTurn(TurnStatus status, String move, int scoreGained, int minWordLength) {
        Game.Panel("TURNSTATUS").Clear();
        Game.Panel("TURNSTATUS").AddText("TurnStatus", "");
        switch (status) {
            case OK:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText(CurrentPlayer+" played the word '"+move+"' and gained "+scoreGained+" score!");
                if (this.playSfx) GameSound.ok();
            break;
            case SKIPPED:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText(CurrentPlayer+" skipped their turn!");
                if (this.playSfx) GameSound.bad();
            break;
            case TOO_SHORT:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("Word too short! Minimal word length is" + minWordLength);
                if (this.playSfx) GameSound.bad();  
            break;
            case DUPLICATE:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("This word was already played! Try a different one.");
                if (this.playSfx) GameSound.bad();
            break;
            case NOT_IN_DICT:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("Word not in wordlist! Try a different one.");
                if (this.playSfx) GameSound.bad();
            break;
            case NOT_ON_BOARD:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("Word does not exist on board! Try a different one");
                if (this.playSfx) GameSound.bad();
            break;
            case STOPPED:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("Exiting Game...");
                if (this.playSfx) GameSound.bad();
                break;
            case SAVE_ERR:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("Error Saving Game "+move);
                if (this.playSfx) GameSound.bad();
                break;
            case SAVE_OK:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("Save Game Successful!");
                if (this.playSfx) GameSound.ok();
                break;
            case PLAYER_LEFT:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("Player "+ this.CurrentPlayer+" Left the game");
                if (this.playSfx) GameSound.bad();
                break;
            case SHAKE:
                ((JLabel)Game.Panel("TURNSTATUS").GetItem("TurnStatus")).setText("Player "+ this.CurrentPlayer+" Shook up the Board!");
                if (this.playSfx) GameSound.ok();
                break;
            default:
                break;
        }
    }
    
    /**
     * Applies the configured automatic or manual between-turn confirmation.
     */
    public void confirm() {
        int speed=0;
        switch(autoConfirm){
            case Fast:
                speed=50;
                break;
            case Normal:
                speed=1000;
                break;
            default:
                break;
        }
        if (autoConfirm!=Speed.OFF) {
            try { Thread.sleep(speed); }
            catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            CompletableFuture<Void> enter = new CompletableFuture<>();
            Game.Panel("TURNSTATUS").AddButton("ConfirmButton", "Continue >", e->{enter.complete(null);});
            Game.revalidate();
            Game.repaint();
            try{
                enter.get();
            }catch(Exception e){}
        }
    }
    

    /**
     * Performs final acknowledgement after results; the GUI uses the results button instead.
     */
    public void confirmForSure() {        
    }

    
    /**
     * Shows final scores and waits for the user to continue back to the lobby.
     */
    public void results(List<Entry<String, Integer>> leaderboard, int skips, int maxScore) {
        Game.dispose();
        CompletableFuture<Void> toLobby = new CompletableFuture<>();
        audio.stop();
        if (this.playMusic) audio = GameSound.results();
        CreateResult(leaderboard, skips, maxScore,toLobby);
        Results.setVisible(true);
        try{toLobby.get();
            Results.dispose();
        }catch(Exception e){
        }
    }
    
}
