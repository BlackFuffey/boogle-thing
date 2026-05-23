package boogle.ui.gui;

import javax.swing.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Collection of static factory methods used to create Swing components for the
 * graphical user interface. These helpers encapsulate common construction
 * patterns such as attaching an {@link ActionListener} or setting the
 * initial size of a text field. By centralising component creation here the
 * rest of the GUI code remains succinct and focused on layout rather than
 * instantiation details.
 */
class Components {
    /**
     * Creates a new {@link JButton} with the provided label and binds the
     * given action listener to it. Buttons created via this method do not
     * customise any other properties beyond the text and listener.
     *
     * @param word   the text to display on the button
     * @param action the listener invoked when the button is pressed
     * @return a new button with the specified text and action handler
     */
    public static JButton addButton(String word, ActionListener action) {
        JButton button = new JButton(word);
        button.addActionListener(action);;
        return button;
    }
    /**
     * Constructs a new {@link JLabel} initialised with the supplied text. The
     * label has no custom styling applied; callers are free to adjust fonts
     * or alignment after creation.
     *
     * @param text the text to render within the label
     * @return a new label displaying {@code text}
     */
    public static JLabel addLabel(String text) {
        JLabel label = new JLabel(text);
        return label;
    }
    /**
     * Creates a new {@link JTextField} capable of holding a specified number
     * of columns of text. The underlying document model is left at its
     * default; use this helper when you simply need a text field of a given
     * width.
     *
     * @param length the number of columns the text field should accommodate
     * @return a new text field with an initial column count of {@code length}
     */
    public static JTextField addTextField(int length) {
        JTextField textField = new JTextField(length);
        return textField;
    }
    /**
     * Constructs a {@link JComboBox} populated with the provided array of
     * options. No item is pre-selected; the caller may call
     * {@link JComboBox#setSelectedIndex(int)} after construction if a default
     * value is desired.
     *
     * @param options the array of option strings to populate the combo box
     * @return a combo box containing each element of {@code options}
     */
    public static JComboBox<String> addComboBox(String[] options) {
        JComboBox<String> comboBox = new JComboBox<String>(options);  
        return comboBox;
    }    
}



/**
 * Window abstraction used by the graphical interface. A {@code Windows}
 * instance wraps a {@link JFrame} and maintains a hierarchy of named panels
 * for easy lookup and manipulation. Panels can be added to other panels
 * using a {@code parent} name and will automatically be registered in an
 * internal map. This class also exposes helpers for managing window size
 * and for posting warning dialogs.
 */
public class Windows extends JFrame{
    //panel inner class (for organization)
    /**
     * Sub‑panel within a {@link Windows} instance. A {@code Panels}
     * encapsulates its own child components and registers itself in its
     * parent’s panel list. Components can be looked up by name via the
     * {@link #GetItem(String)} method. The class exposes helper methods
     * for adding buttons, labels, text fields and combo boxes, as well as
     * methods for clearing all components and adjusting alignment relative
     * to its container.
     */
    class Panels extends JPanel{
        //constructor
        /**
         * Constructs a {@code Panels} object and registers it with its parent
         * window. The panel becomes addressable via the given {@code name}.
         *
         * @param name      the key used to store this panel in the parent
         * @param panelList the parent’s internal map of panels
         */
        public Panels(String name, HashMap<String, Panels> panelList) {
            panelList.put(name, this);
        }

        /**
         * A lookup table of component names to the actual {@link JComponent}
         * instances contained in this panel. This map is populated by the
         * {@code Add*} helper methods and cleared when {@link #Clear()} is invoked.
         */
        private HashMap<String, JComponent> items = new HashMap<>();

        /**
         * Removes all components from this panel and clears the internal
         * component map. After invocation no components will be displayed and
         * {@link #GetItem(String)} will return {@code null} for all keys.
         */
        public void Clear() {
            items.clear();
            this.removeAll();
        }

        /**
         * Adds a new button to this panel.
         *
         * @param name   key used to store the button
         * @param text   label displayed on the button
         * @param action handler invoked when the button is clicked
         */
        public void AddButton(String name, String text, ActionListener action) {
            items.put(name, Components.addButton(text, action));
            this.add(items.get(name));
        }

        /**
         * Adds a new label to this panel.
         *
         * @param name key used to store the label
         * @param text text displayed by the label
         */
        public void AddText(String name, String text) {
            items.put(name, Components.addLabel(text));
            this.add(items.get(name));
        }

        /**
         * Adds a new text field of a specified length to this panel.
         *
         * @param name   key used to store the text field
         * @param length number of columns in the created text field
         */
        public void AddTextField(String name, int length) {
            items.put(name, Components.addTextField(length));
            this.add(items.get(name));
        }

        /**
         * Adds a new combo box populated with the provided options to this
         * panel. The first option in the array will be selected by default.
         *
         * @param name    key used to store the combo box
         * @param options list of selectable options for the combo box
         */
        public void AddComboBox(String name, String[] options) {
            items.put(name, Components.addComboBox(options));
            ((JComboBox<?>) items.get(name)).setSelectedIndex(0);
            this.add(items.get(name));
        }

        /**
         * Sets the alignment of this panel within its parent using a simple
         * alignment code. See {@link #setAnchor(String)} documentation in
         * {@link Panels} for the mapping between codes and positions.
         *
         * @param alignment cardinal alignment code (e.g. "C", "N", "SW")
         */
        public void setAnchor(String alignment) {
            switch (alignment.toUpperCase()) {
                case "C":
                    this.setAlignmentX(CENTER_ALIGNMENT);
                    this.setAlignmentY(CENTER_ALIGNMENT);
                    break;
                case "NW":
                    this.setAlignmentX(TOP_ALIGNMENT);
                    this.setAlignmentY(RIGHT_ALIGNMENT);
                    break;
                case "N":
                    this.setAlignmentX(CENTER_ALIGNMENT);
                    this.setAlignmentY(TOP_ALIGNMENT);
                    break;
                case "NE":
                    this.setAlignmentX(TOP_ALIGNMENT);
                    this.setAlignmentY(LEFT_ALIGNMENT);
                    break;
                case "E":
                case "SE":
                case "S":
                    this.setAlignmentX(CENTER_ALIGNMENT);
                    this.setAlignmentY(BOTTOM_ALIGNMENT);
                    break;
                case "SW":
                case "W":
                    this.setAlignmentX(LEFT_ALIGNMENT);
                    this.setAlignmentY(CENTER_ALIGNMENT);
                    break;
                default:
                    return;
            }
        }


        //set component anchor too (just use getItem() in parameter)
        public void setAnchor(JComponent item, String alignment){
            try{
            switch (alignment.toUpperCase()) {
                case "C":
                    item.setAlignmentX(CENTER_ALIGNMENT);
                    item.setAlignmentY(CENTER_ALIGNMENT);
                    break;
                case "NW":
                    item.setAlignmentX(TOP_ALIGNMENT);
                    item.setAlignmentY(RIGHT_ALIGNMENT);
                    break;
                case "N":
                    item.setAlignmentX(CENTER_ALIGNMENT);
                    item.setAlignmentY(TOP_ALIGNMENT);
                    break;
                case "NE":
                    item.setAlignmentX(TOP_ALIGNMENT);
                    item.setAlignmentY(LEFT_ALIGNMENT);
                    break;
                case "E":
                case "SE":
                case "S":
                    item.setAlignmentX(CENTER_ALIGNMENT);
                    item.setAlignmentY(BOTTOM_ALIGNMENT);
                    break;
                case "SW":
                case "W":
                    item.setAlignmentX(LEFT_ALIGNMENT);
                    item.setAlignmentY(CENTER_ALIGNMENT);
                    break;
                default:
                    return;
            }}catch(NullPointerException e){}
        }

        //accessor
        /**
         * Retrieves a component stored within this panel by its key.
         *
         * @param component the name used when the component was added
         * @return the stored component, or {@code null} if no component
         *         exists under that name
         */
        public JComponent GetItem(String component) {
            return items.get(component);
        }

        /**
         * Returns the textual value of a supported component. For text
         * fields this returns the current text; for combo boxes it returns
         * the selected item’s string representation. Other component types
         * return {@code null}.
         *
         * @param component the component key
         * @return the current value of a text field or combo box, or
         *         {@code null} if the component is not one of those types
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
        public void SetConstraint(String component,GridBagConstraints constraints){
            if(this.getLayout() instanceof GridBagLayout){
                add(items.get(component),constraints);
            }
            return;
        }
    }

//MAIN CLASS STUFF
    /**
     * Flag indicating whether this window has been initialised and shown at
     * least once. The graphical UI uses this to avoid re‑building windows
     * repeatedly.
     */
    private boolean created; 

    /**
     * Cached copy of the screen resolution as reported by the local
     * {@link Toolkit}. Exposed via {@link #getScreenSize()} for use when
     * calculating window sizes.
     */
    public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    /**
     * Mapping from panel names to their corresponding {@code Panels}
     * instances. This allows nested panels to be looked up and added to
     * other panels by name.
     */
    private HashMap<String, Panels> panelList = new HashMap<>();

    
    /**
     * Constructs a new top‑level window with the specified title. The
     * underlying frame is not yet visible; call {@link #windowSize(boolean)}
     * or {@link #windowSize(int, int)} followed by {@link #setVisible(boolean)}
     * to display it.
     *
     * @param title the title of the window displayed in its decoration
     */
    public Windows(String title) {
        this.setTitle(title); 
        this.created = false;  
    }

    /**
     * Marks this window as having been created. This flag is used to
     * prevent duplicate creation of UI elements.
     */
    public void Created(){
        created = true;
    }

    /**
     * Removes every registered panel from this window and clears the
     * underlying map. This is useful when reconstructing the interface
     * from scratch.
     */
    public void Clear(){
        for(String name: panelList.keySet()){
            panelList.remove(name);
        }
        this.removeAll();
    }
    
    /**
     * Toggles full‑screen mode for this window. When {@code fullscreen} is
     * {@code true} the window is maximised to cover the entire screen. No
     * action is taken when {@code fullscreen} is {@code false}.
     *
     * @param fullscreen whether the window should enter full‑screen mode
     */
    public void windowSize(boolean fullscreen){
        if(fullscreen){
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }
    /**
     * Sets the preferred size of this window to the given width and height
     * in pixels.
     *
     * @param x the width of the window in pixels
     * @param y the height of the window in pixels
     */
    public void windowSize(int x, int y){
        this.setSize(x,y);
    } 
    /**
     * Creates and registers a new panel. The new panel will be added to
     * either the window itself (if {@code parent} equals {@code "main"},
     * case-insensitive) or to the panel identified by {@code parent}. The
     * panel is assigned the specified layout manager. For {@link BoxLayout}
     * instances this method wraps the layout to circumvent the awkward
     * constructor requirements of {@code BoxLayout}.
     *
     * @param parent the name of the panel to which the new panel should be
     *               added, or {@code "main"} to add directly to the window
     * @param name   the key under which to store the new panel
     * @param layout the layout manager governing the new panel’s component
     *               arrangement
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
        panelList.put(name,panel);
        if(parent.toLowerCase().equals("main")){
            this.add(panel);
        }
        else{
            panelList.get(parent).add(panel);
        }
    }

    //this is just for gridconstraints 
    public void AddPanel(String parent, String name, LayoutManager layout, GridBagConstraints constraints){
        if(Panel(parent).getLayout() instanceof GridBagLayout){

            //copypasted from original
            Panels panel = new Panels(name,panelList);
            panel.setAlignmentX(CENTER_ALIGNMENT);
            panel.setAlignmentY(CENTER_ALIGNMENT);

            if(layout instanceof BoxLayout){ 
            panel.setLayout(new BoxLayout(panel, ((BoxLayout)layout).getAxis()));
            }else{
            //default
            panel.setLayout(layout);
            }
            panelList.put(name,panel);
            if(constraints == null){
                //default constraints
                panelList.get(parent).add(panel,new GridBagConstraints());
            }else{
                panelList.get(parent).add(panel,constraints);
            }  
        }else{
            AddPanel(parent, name, layout);
        }
    }


    /**
     * Displays a warning dialog with the specified title and message. This
     * static convenience method delegates to
     * {@link JOptionPane#showMessageDialog(java.awt.Component, Object, String, int)}
     * using the warning message type.
     *
     * @param parent the parent component of the dialog, may be {@code null}
     * @param title  title string for the dialog window
     * @param text   message to display within the dialog
     */
    public static void CreateWarning(JComponent parent, String title, String text){
        JOptionPane.showMessageDialog(parent, text, title, JOptionPane.WARNING_MESSAGE);
    }

    //accessor
    /**
     * Retrieves the panel associated with the given name. Panels are
     * registered via {@link #AddPanel(String, String, LayoutManager)}.
     *
     * @param name key of the panel to retrieve
     * @return the panel corresponding to {@code name} or {@code null} if
     *         none exists
     */
    public Panels Panel(String name){
        return panelList.get(name);
    }

    /**
     * Returns the screen resolution captured when the class was loaded. This
     * value can be used to size windows relative to the user’s display.
     *
     * @return the screen dimensions as a {@link Dimension}
     */
    public static Dimension getScreenSize(){
        return screenSize;
    }

    /**
     * Indicates whether {@link #Created()} has been called on this window.
     *
     * @return {@code true} if the window has been created, {@code false}
     *         otherwise
     */
    public boolean isCreated(){
        return created;
    }

}
