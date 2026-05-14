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
    public static JLabel addLabel(String text){
        JLabel label = new JLabel(text);
        return label;
    }
    public static JTextField addTextField(int length){
        JTextField textField = new JTextField(length);
        return textField;
    }
    public static JComboBox<String> addComboBox(String[] options){
        JComboBox<String> comboBox = new JComboBox<String>(options);  
        return comboBox;
    }    
}

public class Window extends JFrame{
    public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private HashMap<String,JPanel> panelList = new HashMap<String,JPanel>();
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
        panel.setAlignmentX(CENTER_ALIGNMENT);
        panel.setAlignmentY(CENTER_ALIGNMENT);
        if(layout instanceof BoxLayout){ 
            //boxlayout setup workaround cuz boxlayout constructor is stupid
            panel.setLayout(new BoxLayout(panel, ((BoxLayout)layout).getAxis()));
        }else{
            //default
            panel.setLayout(layout);
        }
        panelList.put(name,panel);
        if(parent.toLowerCase().equals("main")){
            this.add(panel);
        }
        else{
            panelList.get(parent).add(panel);
        }
    }

    //add components to panel
    public void PanelAddButton(String panel, String name,String text,ActionListener action){
        items.put(name, Components.addButton(text, action));
        panelList.get(panel).add(items.get(name));
    }
    public void PanelAddText(String panel ,String name,String text){
        items.put(name, Components.addLabel(text));
        panelList.get(panel).add(items.get(name));
    }
    public void PanelAddTextField(String panel, String name,int length){
        items.put(name, Components.addTextField(length));
        panelList.get(panel).add(items.get(name));
    }
    public void PanelAddComboBox(String panel, String name, String[] options){
        items.put(name, Components.addComboBox(options));
        panelList.get(panel).add(items.get(name));
    }

    public static void CreateWarning(JComponent parent, String title, String text){
        JOptionPane.showMessageDialog(parent, text, title, JOptionPane.WARNING_MESSAGE);
    }

    //accessor
    public JPanel GetPanel(String name){
        return panelList.get(name);
    }
    public JComponent GetComponent(String component){
        return items.get(component);
    }

    public String GetComponentText(String component){
        if(items.get(component) instanceof JTextField){
            return ((JTextField)items.get(component)).getText();
        }else if(items.get(component) instanceof JComboBox){
            return ((JComboBox)items.get(component)).getSelectedItem().toString();
        }
        return null;
    }

    public static Dimension getScreenSize(){
        return screenSize;
    }

    public void setAnchor(String panel, String alignment){
        switch(alignment.toUpperCase()){
            //center + cardinal
            case "C":
                GetPanel(panel).setAlignmentX(CENTER_ALIGNMENT);
                GetPanel(panel).setAlignmentY(CENTER_ALIGNMENT);
            case "NW":
                
            case "N":
                GetPanel(panel).setAlignmentX(CENTER_ALIGNMENT);
                GetPanel(panel).setAlignmentY(TOP_ALIGNMENT);
            case "NE":
            case "E":
            case "SE":
            case "S":
                GetPanel(panel).setAlignmentX(CENTER_ALIGNMENT);
                GetPanel(panel).setAlignmentY(BOTTOM_ALIGNMENT);
            case "SW":
            case "W":
                GetPanel(panel).setAlignmentX(LEFT_ALIGNMENT);
                GetPanel(panel).setAlignmentY(CENTER_ALIGNMENT);
        
            default:
                return;
        }
    }
}
