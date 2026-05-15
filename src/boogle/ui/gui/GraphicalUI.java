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
    private boolean playing = true;
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
                try{
                    options.playerlist.add(new AIPlayer(name, boogle.player.AIPlayer.Level.fromValue(Integer.parseInt(level))));
                }catch(IllegalArgumentException e){
                    Windows.CreateWarning(null,"Faulty Settings","Error creating Player\nPlease change Player settings");
                }
            case "Human":
                options.playerlist.add(new UIPlayer(name));
            }
    }

    //TODO: settings option
    private void OpenSettings(GameOptions options){
        //layout
        Settings.windowSize((int)Windows.getScreenSize().getWidth()/2,(int)Windows.getScreenSize().getHeight());
        //title
        Settings.AddPanel("MAIN","LAYOUT", new BoxLayout(Game,BoxLayout.X_AXIS));
        Settings.AddPanel("LAYOUT", "SETTINGS",new BoxLayout(Settings, BoxLayout.Y_AXIS));
        Settings.AddPanel("LAYOUT", "PLAYERLIST", new BoxLayout(Settings, BoxLayout.Y_AXIS));

        Settings.Panel("SETTINGS").AddText("title", "Settings");
        Settings.Panel("SETTINGS").GetItem("title").setFont(new Font("Ariel",Font.BOLD,30));
        //settings
        //player setting
        Settings.Panel("SETTINGS").AddText( "players", "Players:");
        Settings.AddPanel("SETTINGS", "PLAYERS",new FlowLayout());
        //playerlist

        Settings.Panel("PLAYERS").AddComboBox("PlayerSettingType", new String[]{"Human","AI"});
        Settings.Panel("PLAYERS").AddText("settingsplayernamelabel", "Name");
        Settings.Panel("PLAYERS").AddTextField("PlayerSettingName", 10);
        Settings.Panel("PLAYERS").AddText("settingsailevel", "ai level");
        Settings.Panel("PLAYERS").AddComboBox("PlayerSettingLevel",new String[]{"","1","2","3","4","5"});
        Settings.Panel("PLAYERS").AddButton("PlayerSettingSubmit","Submit",e->{validatePlayer(options,
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
        MainMenu.windowSize(true);
        MainMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MainMenu.AddPanel("MAIN", "LAYOUT", new BoxLayout(Game, BoxLayout.Y_AXIS));
        MainMenu.AddPanel("LAYOUT","TITLE",new FlowLayout());
        MainMenu.Panel("TITLE").AddText("title", "BOOGLE");
        MainMenu.Panel("title").setFont(new Font("Ariel",Font.BOLD,100));
        MainMenu.AddPanel("LAYOUT", "PLAYERLIST", new BoxLayout(Game,BoxLayout.Y_AXIS));
        
        MainMenu.AddPanel("LAYOUT", "SETTINGS", new FlowLayout());
        MainMenu.Panel("SETTINGS").AddButton("settings","Settings",e->{OpenSettings(options);});
        MainMenu.Panel("SETTINGS").AddButton("start", "Start", e ->{ready = startCheck(options);});
        MainMenu.Panel("SETTINGS").AddButton( "quit","Quit",e ->{System.exit(0);});
        MainMenu.setVisible(true);
        while(!ready){
            //TODO: fix gitter
            MainMenu.Panel("PLAYERLIST").removeAll();
            MainMenu.Panel("PLAYERLIST").AddText("players", "Players:");
            for(Player players: options.playerlist){
            MainMenu.Panel("PLAYERLIST").AddText(players.getName(), players.getName());
            MainMenu.Panel("PLAYERLIST").revalidate();
            MainMenu.Panel("PLAYERLIST").repaint();
            }
        }
        return true; //TODO: this
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