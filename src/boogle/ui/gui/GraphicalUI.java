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
import java.util.concurrent.Flow;


//i have no idea what im doing
public class GraphicalUI implements GameUI{
    private boolean ready=false;
    private boolean playing = true;
    private Clip audio;

    /*
    naming convention:
        Window - upper
        PANEL - all caps
        component - lower
    */
    Window MainMenu = new Window("lobby");
    Window Game = new Window("Game");
    Window Settings = new Window("Settings");
    Window Results = new Window("Results");

    private Boolean startCheck(GameOptions options){
        if (options.playerlist.size() != 0) {
            //audio.stop();
            //audio = GameSound.ingame();
            //GameSound.ok();
            return true;
        }
        Window.CreateWarning(null, "Settings Missing", "Players Missing\nPlease Open Settings to Add Players");
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
    //TODO: settings option
    private void OpenSettings(GameOptions options){
        //layout
        Settings.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Settings.windowSize((int)Window.getScreenSize().getWidth()/2,(int)Window.getScreenSize().getHeight());
        //title
        Settings.AddPanel("MAIN", "TITLE", new BoxLayout(Settings,BoxLayout.X_AXIS));
        Settings.PanelAddText("TITLE","title", "Settings");
        Settings.GetComponent("title").setFont(new Font("Ariel",Font.PLAIN,10));
        //settings
        Settings.AddPanel("TITLE", "SETTINGS",new BoxLayout(Settings, BoxLayout.X_AXIS));
        Settings.AddPanel("MAIN", "PLAYERLIST", new BoxLayout(Settings, BoxLayout.X_AXIS));


        //playerlist
        for(Player players: options.playerlist){
            String name = players.getName();
            Settings.AddPanel("PLAYERLIST", players.getName(), new FlowLayout());
            Settings.PanelAddText(name,"number",Integer.toString(options.playerlist.indexOf(players)));
            Settings.PanelAddText(name,"name",players.getName());
            Settings.PanelAddText(name, "type", getPlayerType(players));
            Settings.PanelAddText(name, "level", getAILevel(players));
        }
        
        Settings.AddPanel("PLAYERLIST", "PLAYERSETTINGS", new BoxLayout(Settings,BoxLayout.X_AXIS));


        Settings.setVisible(true);
    }

    public boolean lobby(GameOptions options) {
        ready=false;
        //title screen
        MainMenu.windowSize(true);
        MainMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        MainMenu.AddPanel("MAIN","TITLE",new FlowLayout());
        MainMenu.setAnchor("TITLE", "C");
        MainMenu.PanelAddText("TITLE","title", "BOOGLE");
        MainMenu.GetComponent("title").setFont(new Font("Ariel",Font.BOLD,500));
        MainMenu.AddPanel("MAIN", "SETTINGS", new BoxLayout(Game, 2));
        MainMenu.setAnchor("SETTINGS","C");
        MainMenu.PanelAddButton("SETTINGS", "settings","Settings",e->{OpenSettings(options);});
        MainMenu.PanelAddButton("SETTINGS", "start", "Start", e ->{ready = startCheck(options);});
        MainMenu.PanelAddButton("SETTINGS", "quit","Quit",e ->{System.exit(0);});
        MainMenu.setVisible(true);
        return false; //TODO: this
    }
    @Override
    public void close() throws Exception {
        
    }
    @Override
    public void startTurn(Gameboard board, List<Entry<String, Integer>> leaderboard, ArrayList<String> playedWords,
            String currentPlayerName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'startTurn'");
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