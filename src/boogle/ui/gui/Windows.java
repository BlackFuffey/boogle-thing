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




public class Windows extends JFrame{
    //panel inner class (for organization)
    class Panels extends JPanel{
        //constructor
        public Panels(String name,HashMap<String,Panels> panelList){
            panelList.put(name, this);
        }

        private HashMap<String,JComponent> items = new HashMap<String,JComponent>();

        public void Clear(){
            items.clear();
            this.removeAll();
        }
  
        public void AddButton(String name,String text,ActionListener action){
            items.put(name, Components.addButton(text, action));
            this.add(items.get(name));
        }
        public void AddText(String name,String text){
            items.put(name, Components.addLabel(text));
            this.add(items.get(name));
        }
        public void AddTextField(String name,int length){
            items.put(name, Components.addTextField(length));
            this.add(items.get(name));
        }
        public void AddComboBox(String name, String[] options){
            items.put(name, Components.addComboBox(options));
            ((JComboBox)items.get(name)).setSelectedIndex(0);
            this.add(items.get(name));
        }

        public void setAnchor( String alignment){
        switch(alignment.toUpperCase()){
            //center + cardinal
            case "C":
                this.setAlignmentX(CENTER_ALIGNMENT);
                this.setAlignmentY(CENTER_ALIGNMENT);
            case "NW":
                this.setAlignmentX(TOP_ALIGNMENT);
                this.setAlignmentY(RIGHT_ALIGNMENT);
                
            case "N":
                this.setAlignmentX(CENTER_ALIGNMENT);
                this.setAlignmentY(TOP_ALIGNMENT);
            case "NE":
                this.setAlignmentX(TOP_ALIGNMENT);
                this.setAlignmentY(LEFT_ALIGNMENT);
            case "E":
            case "SE":
            case "S":
                this.setAlignmentX(CENTER_ALIGNMENT);
                this.setAlignmentY(BOTTOM_ALIGNMENT);
            case "SW":
            case "W":
                this.setAlignmentX(LEFT_ALIGNMENT);
                this.setAlignmentY(CENTER_ALIGNMENT);
        
            default:
                return;
            }
        }
    //accessor
        public JComponent GetItem(String component){
            return items.get(component);
        }

        public String GetItemText(String component){
            if(items.get(component) instanceof JTextField){
                return ((JTextField)items.get(component)).getText();
            }else if(items.get(component) instanceof JComboBox){
                return ((JComboBox)items.get(component)).getSelectedItem().toString();
            }
            return null;
        }
    }

//MAIN CLASS STUFF
    private boolean created; 
    public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private HashMap<String,Panels> panelList = new HashMap<String,Panels>();

    
    public Windows(String title) {
        this.setTitle(title); 
        this.created=false;  
    }

    public void Created(){
        created=true;
    }

    public void Clear(){
        for(String name: panelList.keySet()){
            panelList.remove(name);
        }
        this.removeAll();
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
        Panels panel = new Panels(name,panelList);
        panel.setAlignmentX(CENTER_ALIGNMENT);
        panel.setAlignmentY(CENTER_ALIGNMENT);
        if(layout instanceof BoxLayout){ 
            //boxlayout setup workaround cuz boxlayout constructor is stupid
            //ignore parameter layout parent
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

    public static void CreateWarning(JComponent parent, String title, String text){
        JOptionPane.showMessageDialog(parent, text, title, JOptionPane.WARNING_MESSAGE);
    }

    //accessor
    public Panels Panel(String name){
        return panelList.get(name);
    }

    public static Dimension getScreenSize(){
        return screenSize;
    }

    public boolean isCreated(){
        return created;
    }

}
