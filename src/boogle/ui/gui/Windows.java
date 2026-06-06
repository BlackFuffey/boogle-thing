/*
 * File: Windows.java
 * Author: Eric
 * Description: Provides Swing window, panel, and component helpers used by the graphical Boogle interface.
 */

package boogle.ui.gui;

import javax.swing.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Factory methods for Swing components used by {@link Windows.Panels}.
 */
class Components {
    /**
     * Creates a button and installs its action listener.
     *
     * @return configured button
     */
    public static JButton addButton(String word, ActionListener action) {
        JButton button = new JButton(word);
        button.addActionListener(action);
        return button;
    }
    /**
     * Creates a text label.
     *
     * @return configured label
     */
    public static JLabel addLabel(String text) {
        JLabel label = new JLabel(text);
        return label;
    }
    /**
     * Creates a single-line text field with the requested column count.
     *
     * @return configured text field
     */
    public static JTextField addTextField(int length) {
        JTextField textField = new JTextField(length);
        return textField;
    }
    /**
     * Creates a string combo box from a list of options.
     *
     * @return configured combo box
     */
    public static JComboBox<String> addComboBox(String[] options) {
        JComboBox<String> comboBox = new JComboBox<String>(options);  
        return comboBox;
    }    
}



/**
 * Thin {@link JFrame} wrapper that stores named panels and named components.
 *
 * <p>The GUI implementation uses this class to build screens incrementally and
 * later retrieve components by string key when event listeners need to update
 * labels, read fields, or clear panels.</p>
 */
public class Windows extends JFrame{
    //panel inner class (for organization)
    /**
     * Named panel that owns a map of named child components.
     */
    class Panels extends JPanel{
        //constructor
        /**
         * Creates a panel and registers it in a window-level panel map.
         *
         * @param name panel key
         * @param panelList map that should contain the new panel
         */
        public Panels(String name, HashMap<String, Panels> panelList) {
            panelList.put(name, this);
        }

        private HashMap<String, JComponent> items = new HashMap<>();

        /**
         * Removes all components and clears this panel's component map.
         */
        public void Clear() {
            items = new HashMap<String, JComponent>();
            this.removeAll();
        }

        /**
         * Adds a named button to this panel.
         */
        public void AddButton(String name, String text, ActionListener action) {
            items.put(name, Components.addButton(text, action));
            this.add(items.get(name));
        }

        /**
         * Adds a named label to this panel.
         */
        public void AddText(String name, String text) {
            items.put(name, Components.addLabel(text));
            this.add(items.get(name));
        }

        /**
         * Adds a named text field to this panel.
         */
        public void AddTextField(String name, int length) {
            items.put(name, Components.addTextField(length));
            this.add(items.get(name));
        }

        /**
         * Adds a named combo box to this panel and selects the first option.
         */
        public void AddComboBox(String name, String[] options) {
            items.put(name, Components.addComboBox(options));
            ((JComboBox<?>) items.get(name)).setSelectedIndex(0);
            this.add(items.get(name));
        }

        //accessor
        /**
         * Returns a named child component.
         */
        public JComponent GetItem(String component) {
            return items.get(component);
        }

        /**
         * Reads text from a named text field or selected value from a named combo box.
         */
        public String GetItemText(String component) {
            if (items.get(component) instanceof JTextField) {
                return ((JTextField) items.get(component)).getText();
            } else if (items.get(component) instanceof JComboBox) {
                return ((JComboBox<?>) items.get(component)).getSelectedItem().toString();
            }
            return null;
        }


        //gridbaglayout constraints
        /*
        defaults for copy paste:
        gridx = RELATIVE;
        gridy = RELATIVE;
        gridwidth = 1;
        gridheight = 1;

        weightx = 0;
        weighty = 0;
        anchor = CENTER;
        fill = NONE;

        insets = new Insets(0, 0, 0, 0);
        ipadx = 0;
        ipady = 0; 
        */

        /**
         * Applies GridBagLayout constraints when this panel uses GridBagLayout.
         */
        public void SetConstraint(JComponent component,GridBagConstraints constraints){
            if(this.getLayout() instanceof GridBagLayout){
                if(constraints == null){
                    ((GridBagLayout)this.getLayout()).setConstraints(component, new GridBagConstraints());
                }else{
                    ((GridBagLayout)this.getLayout()).setConstraints(component, constraints);
                }
            }
        }
    }

//MAIN CLASS STUFF
    /** Whether this window's one-time component tree has been built. */
    private boolean created; 

    /** Registered panels keyed by caller-provided names. */
    private HashMap<String, Panels> panelList = new HashMap<String, Panels>();

    
    /**
     * Creates a named window.
     *
     * @param title frame title
     */
    public Windows(String title) {
        this.setTitle(title); 
        this.created = false;  
    }

    /**
     * Marks the window as having had its one-time component tree created.
     */
    public void Created(){
        created = true;
    }

    /**
     * Clears the registered panel map and removes all frame contents.
     */
    public void Clear(){
        panelList = new HashMap<String,Panels>();
        this.removeAll();
    }
    
    /**
     * Maximizes the window when requested.
     *
     * @param fullscreen whether to maximize the frame to the full screen size
     */
    public void windowSize(boolean fullscreen){
        if(fullscreen){
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }
    /**
     * Sets an explicit window size.
     *
     * @param x window width in pixels
     * @param y window height in pixels
     */
    public void windowSize(int x, int y){
        this.setSize(x,y);
    } 
    /**
     * Creates a named panel and attaches it to either the frame or another panel.
     *
     * @param parent parent panel key, or {@code MAIN} to attach directly to the frame
     * @param name key for the new panel
     * @param layout layout manager to use for the new panel
     */
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
        if(parent.toLowerCase().equals("main")){
            this.add(panel);
        }
        else{
            panelList.get(parent).add(panel);
        }
    }

    /**
     * Shows an informational, warning, or error dialog.
     *
     * @param parent parent frame for the modal dialog; may be {@code null}
     * @param type dialog icon/category to display
     * @param title dialog title
     * @param text dialog body text
     */
    public static void CreateDialog(Windows parent, SubwindowOption type, String title, String text){
        switch(type){
            case WARNING:
                JOptionPane.showMessageDialog(parent, text, title, JOptionPane.WARNING_MESSAGE);
                break;
            case INFO:
                JOptionPane.showMessageDialog(parent, text, title, JOptionPane.INFORMATION_MESSAGE);
                break;
            case ERROR:
                JOptionPane.showMessageDialog(parent, text, title, JOptionPane.ERROR_MESSAGE);
                break;
            default:
                break;
                
            }
    }


    /**
     * Dialog message categories supported by {@link #CreateDialog(Windows, SubwindowOption, String, String)}.
     */
    enum SubwindowOption{
        WARNING,
        INFO,
        ERROR
    }

    //accessor
    /**
     * Returns a named panel registered in this window.
     *
     * @param name panel key
     * @return matching panel, or {@code null} if no panel has that key
     */
    public Panels Panel(String name){
        return panelList.get(name);
    }

 

    /**
     * Reports whether the window has completed its one-time setup.
     *
     * @return {@code true} after {@link #Created()} has been called
     */
    public boolean isCreated(){
        return created;
    }
    /**
     * Compass-style component alignment choices.
     */
    enum direct{
        CENTER,
        NORTH,
        SOUTH,
        EAST,
        WEST,
        NORTHWEST,
        NORTHEAST,
        SOUTHWEST,
        SOUTHEAST,
    }

    //TODO: outdated doc
    /**
     * Sets Swing alignment hints for a component according to a compass direction.
     *
     * @param comp component whose alignment hints should be changed
     * @param alignment requested compass-style alignment
     */
        public static void setAnchor(JComponent comp, direct alignment) {
            switch (alignment) {
                case CENTER:
                    comp.setAlignmentX(CENTER_ALIGNMENT);
                    comp.setAlignmentY(CENTER_ALIGNMENT);
                    break;
                case NORTHWEST:
                    comp.setAlignmentX(TOP_ALIGNMENT);
                    comp.setAlignmentY(RIGHT_ALIGNMENT);
                    break;
                case NORTH:
                    comp.setAlignmentX(CENTER_ALIGNMENT);
                    comp.setAlignmentY(TOP_ALIGNMENT);
                    break;
                case NORTHEAST:
                    comp.setAlignmentX(TOP_ALIGNMENT);
                    comp.setAlignmentY(LEFT_ALIGNMENT);
                    break;
                case EAST:
                    comp.setAlignmentX(RIGHT_ALIGNMENT);
                    comp.setAlignmentY(CENTER_ALIGNMENT);
                case SOUTHEAST:
                case SOUTH:
                    comp.setAlignmentX(CENTER_ALIGNMENT);
                    comp.setAlignmentY(BOTTOM_ALIGNMENT);
                    break;
                case SOUTHWEST:
                case WEST:
                    comp.setAlignmentX(LEFT_ALIGNMENT);
                    comp.setAlignmentY(CENTER_ALIGNMENT);
                    break;
            }
        }

}
