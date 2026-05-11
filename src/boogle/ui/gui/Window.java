package boogle.ui.gui;

import javax.swing.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

class Components{
    public static JButton addButton(String word, ActionListener action){
        JButton button = new JButton(word);
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
    public static JComboBox addComboBox(String[] options){
        JComboBox comboBox = new JComboBox(options);  
        return comboBox;
    }
    
}

public class Window extends JFrame{
    private HashMap<String,JPanel> panels = new HashMap<String,JPanel>();
    private static HashMap<String,JComponent> items = new HashMap<String,JComponent>();

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
    public void AddPanel(String parent, String name, LayoutManager layout){
        JPanel panel = new JPanel();
        panel.setLayout(layout);
        panels.put(name,panel);
        if(parent.toLowerCase().equals("main")){
            this.add(panel);
        }
        else{
            panels.get(parent).add(panel);
        }
    }
    /**
     * add button
     * index selects the panel, name is text of button, action is action listener
    */
    public void PanelAddButton(String panel,String name,ActionListener action){
        panels.get(panel).add(Components.addButton(name, action));
    }
    /**
     * add text
     * index selects the panel, add text
     * */
    public void PanelAddText(String panel,String text){
        panels.get(panel).add(Components.addLabel(text));
    }
    /**
     * add textfield
     * index selects the panel, length is length of textfield
     * */
    public void PanelAddTextField(String panel,int length){
        panels.get(panel).add(Components.addTextField(length));
    }

    public String GetComponentText(String component){
        if(items.get(component) instanceof JTextField){
            return ((JTextField)items.get(component)).getText();
        }else if(items.get(component) instanceof JComboBox){
            return ((JComboBox)items.get(component)).getSelectedItem().toString();
        }
        return "";
    }
}
