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


//i have no idea what im doing
public class GraphicalUI implements GameUI{
    private boolean ready=false;
    private Clip audio;

    /*
    naming convention:
        Windows - upper
        PANEL - all caps
        component - lower
    */
    Windows MainMenu = new Windows("lobby");
    Windows Game = new Windows("Game");
    Windows Settings = new Windows("Settings");
    Windows Results = new Windows("Results");


    //button methods
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
    private String getPlayerType(Player player){
        if(player instanceof AIPlayer){
            return "Ai";
        }
        else if(player instanceof UIPlayer){
            return "Human";
        }
        return null;
    }
    private String getAILevel(Player player){
        if(player instanceof AIPlayer){
            return Integer.toString(((AIPlayer)player).getLevel().getValue());
        }
        return null;
    }
    private void validatePlayer(GameOptions options,String type, String name, String level){
        switch(type){
            case "Ai":    
                options.playerlist.add(new AIPlayer(name, boogle.player.AIPlayer.Level.fromValue(Integer.parseInt(level))));
            case "Human":
                options.playerlist.add(new UIPlayer(name));
            }
    }
    private boolean gameStart(){
        return false;
    }

    //windows setup
    private void CreateMenu(GameOptions options){
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
                OpenSettings(options);
            }
        });
        MainMenu.Panel("SETTINGS").AddButton("start", "Start", e ->{ready = startCheck(options);});
        MainMenu.Panel("SETTINGS").AddButton( "quit","Quit",e ->{System.exit(0);});

    }


    //TODO: settings option
    //settings window
    private void OpenSettings(GameOptions options){
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
            String name = players.getName();
            Settings.AddPanel("PLAYERLIST", players.getName(), new FlowLayout());
            Settings.Panel(name).AddText(name+" number",Integer.toString(options.playerlist.indexOf(players)));
            Settings.Panel(name).AddText(name+" name",players.getName());
            Settings.Panel(name).AddText(name+" type", getPlayerType(players));
            Settings.Panel(name).AddText(name+" level", getAILevel(players));

        }
        Settings.Panel("PLAYERLIST").revalidate();
        Settings.Panel("PLAYERLIST").repaint();
    }); 
        
        
        Settings.AddPanel("PLAYERLIST", "PLAYERSETTINGS", new BoxLayout(Settings,BoxLayout.X_AXIS));


        Settings.setVisible(true);
    }

    public boolean lobby(GameOptions options) {
        ready=false;
        //title screen
        if(!MainMenu.isCreated()){
            CreateMenu(options);
        }
        MainMenu.windowSize(true);
        MainMenu.setVisible(true);
        

        System.out.println("exited");
        /*while(!ready){
            //TODO: fix gitter
            MainMenu.Panel("PLAYERLIST").Clear();
            MainMenu.Panel("PLAYERLIST").AddText("players", "Players: ");
            for(Player players: options.playerlist){
                MainMenu.Panel("PLAYERLIST").AddText(players.getName(), players.getName()+", ");
            }
            MainMenu.Panel("PLAYERLIST").revalidate();
            MainMenu.Panel("PLAYERLIST").repaint();
        }*/
        if(ready){
            MainMenu.dispose();
            return true;
        }else{
            System.out.println("bruh");
            return false;
        }

    }
    @Override
    public void close() throws Exception {
        
    }


//no idea why i decided to make this a standalone method
    private void makeGrid(Gameboard board,Windows parent){
        char[][] boardChar = board.board;
        parent.AddPanel("LAYOUT", "BOARD",new GridLayout(boardChar.length,boardChar[0].length));
        for(int row =0;row<boardChar.length;row++){
            for(char letter:boardChar[row]){
                parent.Panel("BOARD").AddText(Character.toString(letter),Character.toString(letter));
            }
        }
    }

    private GridBagConstraints bagLayout(){

        return null;
    }

    @Override
    public void startTurn(Gameboard board, List<Entry<String, Integer>> leaderboard, ArrayList<String> playedWords, String currentPlayerName) {
        //game window construction
        
        Game.AddPanel("MAIN", "LAYOUT", new GridBagLayout());
        makeGrid(board, Game);



        Game.revalidate();
        Game.repaint();
        Game.setVisible(true);

    }
    @Override
    public void passive() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'passive'");
    }
    @Override
    public String active() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'active'");
    }
    @Override
    public void endTurn(TurnStatus status, String move, int scoreGained, int minWordLength) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'endTurn'");
    }
    @Override
    public void confirm() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'confirm'");
    }
    @Override
    public void results(List<Entry<String, Integer>> leaderboard, int skips, int maxScore) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'results'");
    }
    
}