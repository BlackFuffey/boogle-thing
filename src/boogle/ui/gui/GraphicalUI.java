package boogle.ui.gui;
import boogle.core.*;
import boogle.core.Launcher.GameOptions;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

//i have no idea what im doing
public class GraphicalUI implements GameUI{
    
    Window menu = new Window("Lobby");
    Window game = new Window("Game");
    private void setup(){
        menu.AddPanel(new FlowLayout());
    }
    public boolean lobby(GameOptions options) {
        setup();
        // TODO: proper implementation
        menu.setVisible(true);
        game.setVisible(false);
        return false;//place holder
        
    }
    public void newTurn(Gameboard board, HashMap<Player, Integer> scoreboard, ArrayList<String> playedWords,
            Player player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newTurn'");
    }
    public void passive() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'passive'");
    }
    public String active() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'active'");
    }
    public void confirm() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'confirm'");
    }
    public void results(List<Entry<String, Integer>> leaderboard, int skips, int maxScore) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'results'");
    }
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
    public void endTurn(TurnStatus status, String move, int scoreGained, int minWordLength) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'endTurn'");
    }
    
}