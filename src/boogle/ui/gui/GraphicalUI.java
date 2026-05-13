package boogle.ui.gui;

import boogle.core.*;
import boogle.core.Launcher.GameOptions;
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
        Window - upper
        PANEL - all caps
        component - lower
    */
    Window MainMenu = new Window("lobby");
    Window Game = new Window("Game");
    Window Settings = new Window("Settings");

    private Boolean startCheck(GameOptions options){
        if (options.playerlist.size() != 0) {
            //audio.stop();
            //audio = GameSound.ingame();
            GameSound.ok();
            return true;
        }
        Window.CreateWarning(null, "Settings Missing", "Players Missing\nPlease Open Settings to Add Players");
        return false;
    }

    private void OpenSettings(GameOptions options){

    }

    public boolean lobby(GameOptions options) {
        ready=false;
        while(!ready){
            MainMenu.AddPanel("MAIN","TITLE",new FlowLayout());
            MainMenu.PanelAddText("TITLE","title", "BOOGLE");
            MainMenu.GetComponent("title").setFont(new Font("Ariel",Font.BOLD,20));
            MainMenu.AddPanel("MAIN", "SETTINGS", new BoxLayout(Game, 2));
            MainMenu.PanelAddButton("SETTINGS", "settings","Settings", null);
            MainMenu.PanelAddButton("SETTINGS", "start", "Start", e ->{ready = startCheck(options);});
        }
        return true;
    }
    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'close'");
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