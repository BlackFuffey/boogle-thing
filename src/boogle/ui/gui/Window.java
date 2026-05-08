package boogle.ui.gui;

import javax.swing.*;

import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;

class Components{
    public static JButton addButton(String name, ActionListener action){
        JButton button = new JButton(name);
        button.addActionListener(action);;
        return button;
    }
    public static Label addLabel(String text){
        Label label = new Label(text);
        return label;
    }
    public static TextField addTextField(int length){
        TextField textField = new TextField(length);
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
    /**
     * add button
     * index selects the panel, name is text of button, action is action listener
    */
    public void PanelListAddComponent(int index,String name,ActionListener action){
        panels.get(index).add(Components.addButton(name, action));
    }
    /**
     * add text
     * index selects the panel, add text
     * */
    public void PanelListAddComponent(int index,String text){
        panels.get(index).add(Components.addLabel(text));
    }
    /**
     * add textfield
     * index selects the panel, length is length of textfield
     * */
    public void PanelListAddComponent(int index,int length){
        panels.get(index).add(Components.addTextField(length));
    }

}
