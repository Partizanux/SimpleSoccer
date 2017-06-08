package simpleSoccer;

import java.util.HashMap;
import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JCheckBoxMenuItem;
import static simpleSoccer.resource.*;
import javax.swing.MenuElement;
import static common.windows.*;

public class Script1 {

    public static class MyMenuBar extends JMenuBar {

        final private ActionListener al;
        private Map<Integer, MyCheckBoxMenuItem> items = new HashMap<Integer, MyCheckBoxMenuItem>();

        public MyMenuBar() {
            super();
            al = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    MyMenuItem source = (MyMenuItem) e.getSource();
                    Main.HandleMenuItems(source.getID(), MyMenuBar.this);
                }
            };
        }

        @Override
        public JMenu add(JMenu c) {
            for (MenuElement elm : c.getSubElements()) {
                for (MenuElement comp : elm.getSubElements()) {
                    if (comp instanceof MyCheckBoxMenuItem) {
                        MyCheckBoxMenuItem myelm = (MyCheckBoxMenuItem) comp;
                        this.items.put(myelm.getID(), myelm);
                    }
                }
            }
            return super.add(c);
        }

        private ActionListener getActionListener() {
            return al;
        }

        /**
         * Swap menu state and do call actionEvent
         * 
         * @param MenuItem getId of MyCheckBoxMenuItem
         */
        public void changeMenuState(int MenuItem) {
            MyCheckBoxMenuItem item = this.items.get(MenuItem);
            if (item != null) {
                item.doClick();
            }
        }

        /**
         * Set menu state and do not call actionEvent
         * 
         * @param MenuItem getId of MyCheckBoxMenuItem
         * @param state New state (MFS_CHECKED or MFS_UNCHECKED)
         */
        public void setMenuState(int MenuItem, final long state) {
            MyCheckBoxMenuItem item = this.items.get(MenuItem);
            if (item == null) {
                return;
            }
            if (state == MFS_CHECKED) {
                item.setSelected(true);
            } else if (state == MFS_UNCHECKED) {
                item.setSelected(false);
            } else {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        }
    }

    public static interface MyMenuItem {

        public int getID();
    }

    public static class MyButtonMenuItem extends JMenuItem implements MyMenuItem {

        private final int id;

        public MyButtonMenuItem(String title, int id, ActionListener al) {
            super(title);
            this.id = id;
            this.addActionListener(al);
        }

        @Override
        public int getID() {
            return id;
        }
    }

    public static class MyCheckBoxMenuItem extends JCheckBoxMenuItem implements MyMenuItem {

        private final int id;

        public MyCheckBoxMenuItem(String title, int id, ActionListener al) {
            this(title, id, al, false);
        }

        public MyCheckBoxMenuItem(String title, int id, ActionListener al, boolean checked) {
            super(title, checked);
            this.id = id;
            this.addActionListener(al);
        }

        @Override
        public int getID() {
            return id;
        }
    }

    public static MyMenuBar createMenu(final int id_menu) {
        MyMenuBar menu = new MyMenuBar();
        ActionListener al = menu.getActionListener();
        JMenu debug = new JMenu("Debug Aids");
        JMenuItem noAids = new MyButtonMenuItem("No Aids", ID_AIDS_NOAIDS, al);
        JMenuItem ids = new MyCheckBoxMenuItem("Show IDs", IDM_SHOW_IDS, al);
        JMenuItem states = new MyCheckBoxMenuItem("Show States", IDM_SHOW_STATES, al);
        JMenuItem regions = new MyCheckBoxMenuItem("Show Regions", IDM_SHOW_REGIONS, al);
        JMenuItem spots = new MyCheckBoxMenuItem("Show Support Spots", IDM_AIDS_SUPPORTSPOTS, al);
        JMenuItem targets = new MyCheckBoxMenuItem("Show Targets", ID_AIDS_SHOWTARGETS, al);
        JMenuItem threat = new MyCheckBoxMenuItem("Highlight if Threatened", IDM_AIDS_HIGHLITE, al);
        debug.add(noAids);
        debug.add(ids);
        debug.add(states);
        debug.add(regions);
        debug.add(spots);
        debug.add(targets);
        debug.add(threat);

        menu.add(debug);

        return menu;
    }
}