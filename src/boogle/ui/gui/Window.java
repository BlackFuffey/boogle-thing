package boogle.ui.gui;

import javax.swing.*;

import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;

class Components{
    public JButton addButton(String name, ActionListener action){
        JButton button = new JButton(name);
        button.addActionListener(action);;
        return button;
    }
    public Label addLabel(String text){
        Label label = new Label(text);
        return label;
    }
    public TextField addTextField(ActionListener action, int length){
        TextField textField = new TextField(length);
        textField.addActionListener(action);
        return textField;
    }
    
}

public class Window extends JFrame{
    private ArrayList<JPanel> panels = new ArrayList<JPanel>();

    public Window(String title) {
        this.setTitle(title);   
    }
    
    public void windowSize(boolean fullscreen){
        if(fullscreen){
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }
    public void windowSize(int x, int y){
        this.setSize(x,y);
    } 
    public void AddPanel(FlowLayout layout){
        panels.add(new JPanel(layout));
    }
    public void AddPanel(GridLayout layout){
        panels.add(new JPanel(layout));
    }
    public void AddPanel(BoxLayout layout){
        panels.add(new JPanel(layout));
    }
}
