package common.misc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Intefrace witch represents console panel.
 *
 * There are 3 implemenations: TEditorPane,CEditorPane and GPane (see below).
 */
interface ConsolePanel {
    public void setFont(Font f);

    public void setColor(Color c);
    
    public void setBGColor(Color color);

    public void add(String s);
    
    public void clearText();
    
    //JEditorPanel methods

    public void setBackground(Color c);

    public void setForeground(Color c);

    public void setEditable(boolean e);
    //Component methods

    public void addKeyListener(KeyListener l);

    public void requestFocus();
}
//</editor-fold>

/**
 * Implementation of ConsolePanel using JTextArea
 * Easy to program, repaint quickly but doesn't support colors
 */
// <editor-fold defaultstate="collapsed" desc="TEditorPane">
class TEditorPane extends JTextArea implements ConsolePanel {

    public TEditorPane() {
        super();
        this.setForeground(Color.WHITE);
        this.setBackground(Color.BLACK);
    }

    @Override
    public void add(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setText(getText() + s);
            }
        });
    }
    
    @Override
    public void clearText() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setText("");
                }
            });
    }

    @Override
    public void setColor(Color c) {
        // JTextArea do not implement colors
    }

    @Override
    public void setBGColor(Color color) {
        // JTextArea do not implement colors
    }
}
// </editor-fold>

/**
 * Implementation of ConsolePanel using JEditorPane
 * Easy to program, support colors but repaint very slowly
 */
// <editor-fold defaultstate="collapsed" desc="CEditorPane">
class CEditorPane extends JEditorPane implements ConsolePanel {

    private List<String> ss = new LinkedList<String>();
    private Color color = Color.WHITE;
    private Color bgcolor = Color.BLACK;

    public CEditorPane() {
        super();
        setContentType("text/html");
        /** 
         * automatic move scrollbar to the end
         * Not needed thanks to EndScrollPane
        DefaultCaret caret = (DefaultCaret) this.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
         */
    }

    @Override
    public void add(String s) {
        String c = "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
        String b = "rgb(" + bgcolor.getRed() + "," + bgcolor.getGreen() + "," + bgcolor.getBlue() + ")";
        ss.add("<font color=\"" + c + "\" bgcolor=\""+b+"\">" + s.replace("\n", "<br>") + "</font>");
        showStrings(ss);
    }
    @Override
    public void clearText() {
        ss.clear();
    }

    private void showStrings(List<String> ss) {
        StringBuilder bs = new StringBuilder();
        bs.append("<html><body>");
        Iterator<String> it = ss.iterator();
        while (it.hasNext()) {
            bs.append(it.next());
        }
        bs.append("</body></html>");
        final String s = bs.toString();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setText(s);
            }
        });
    }

    @Override
    public void setColor(Color c) {
        this.color = c;
    }

    @Override
    public void setBGColor(Color c) {
        this.bgcolor = c;
    }
}
// </editor-fold>

/**
 * Implementation of ConsolePanel using JPanel
 * Hard to program, cannot select text by mouse,
 * repaint quickly, support colors
 */
// <editor-fold defaultstate="collapsed" desc="GPane">
class GPane extends JPanel implements ConsolePanel, Runnable {
    /**
    /* Struct for storing text with it's color and font
     */
    private class SC {

        final public String s;
        final public Color c;
        final public Color bg;
        final public Font f;

        public SC(String s, Color c, Color bg, Font f) {
            this.s = s;
            this.c = c;
            this.bg = bg;
            this.f = f;
        }
    }
    final private List<SC> ss = new LinkedList<SC>();
    private Color color = Color.WHITE;
    private Color bgcolor = Color.BLACK;
    private Font font = this.getFont();
    private int height = 20;

    public GPane() {
        super();
    }

    @Override
    public void add(String s) {
        synchronized (ss) {
            ss.add(new SC(s, color, bgcolor, font));
        }
        SwingUtilities.invokeLater(this);
    }
    
    @Override
    public void clearText() {
        synchronized (ss) {
            ss.clear();
        }
        SwingUtilities.invokeLater(this);
    }    

    @Override
    public void run() {
        Dimension pDim = this.getParent().getSize();
        setPreferredSize(new Dimension((int) pDim.getWidth(), height));
        // update jsrcollpane size
        revalidate();
        // repaint
        repaint();
    }

    final private int paddingX = 5;
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        final int lineHeight = g.getFontMetrics().getHeight();
        int x = paddingX;
        int y = 0;
        synchronized (ss) { 
            for (Iterator<SC> it = ss.iterator(); it.hasNext();) {
                SC sc = it.next();
                g.setFont(sc.f);
                String[] lines = sc.s.split("(?<=[\n])");
                for (int i = 0; i < lines.length; i++) {
                    Point p = drawString(g,sc.c, sc.bg, lines[i], x, y, this.getWidth() - paddingX - 5);
                    x = p.x;
                    y = p.y;
                    if (lines[i].endsWith("\n")) {
                        y += lineHeight;
                        x = paddingX;
                    }
                }
            }
        }
        Dimension pDim = this.getParent().getSize();
        height = y + lineHeight;
        setPreferredSize(new Dimension((int) pDim.getWidth(), height + 2));
    }

    public Point drawString(Graphics g, Color fg, Color bg, String s, int x, int y, int width) {
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        int curX = x;
        int curY = y;

        String[] words = s.split(" ");
        int i = 0;
        for (String word : words) {
            i++;
            int wordWidth = fm.stringWidth(word + " ");
            if (curX + wordWidth >= paddingX + width) {
                if (i > 1 || (wordWidth < width)) {
                    curY += lineHeight;
                    curX = paddingX;
                } else {
                    int j = 1;
                    while (curX + fm.stringWidth(word.substring(0, j) + " ") < paddingX + width) {
                        j++;
                    }
                    Point p = drawString(g,fg,bg, word.substring(0, j - 1), curX, curY, width);
                    curY = p.y;
                    curY += lineHeight;
                    p = drawString(g,fg,bg, word.substring(j - 1), paddingX, curY, width);
                    curY = p.y;
                    curX = p.x;
                    continue;
                }
            }
            if(!word.equals("\n")) {
                g.setColor(bg);
                g.fillRect(curX, curY-fm.getAscent()+fm.getDescent(),
                        fm.stringWidth(word+" "), fm.getAscent());
            }
            g.setColor(fg);
            g.drawString(word, curX, curY);
            curX += wordWidth;
        }
        return new Point(curX,curY);
    }

    @Override
    public void setColor(Color c) {
        this.color = c;
    }
    @Override
    public void setBGColor(Color c) {
        this.bgcolor = c;
    }
    @Override
    public void setFont(Font f) {
        this.font = f;
    }

    @Override
    public void setEditable(boolean e) {
    }
}
// </editor-fold>

/**
 * Auxiliary class extending JScrollPane
 * Helps keep vertical scrollbar down
 * 
 * If you change vertical scrollbar position, it stops to move down
 * If you move it down, it keeps down again
 */
// <editor-fold defaultstate="collapsed" desc="EndScrollPane">
class EndScrollPane extends JScrollPane {

    final JScrollBar jsb;
    boolean moveToEnd = true;
    boolean move = false;
    int lastValue = 0;

    public EndScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
        jsb = this.getVerticalScrollBar();

        jsb.addAdjustmentListener(new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                synchronized (jsb) {
                    int value = moveToEnd ? e.getAdjustable().getMaximum() : lastValue;
                    if (!move) {
                        e.getAdjustable().setValue(value);
                    }
                }
            }
        });
        jsb.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                moveToEnd = false;
                move = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                synchronized (jsb) {
                    lastValue = jsb.getValue();
                    int up = (jsb.getModel().getExtent() + jsb.getValue() - jsb.getMaximum());
                    if (up > -5) {
                        moveToEnd = true;
                    }
                }
                move = false;
            }
        });
    }
}
//</editor-fold>

/**
 * Swing simulation of console
 */
public class JConsole extends JFrame {
    /* Here select ConsolePanel implementation */
    private ConsolePanel editorPane = new TEditorPane();
    //private ConsolePanel editorPane = new CEditorPane();
    //private ConsolePanel editorPane = new GPane();
    
    JScrollPane scroll;
    String color = "white";

    public JConsole(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(680, 355));
        this.setSize(680, 355);
        editorPane.setEditable(false);
        editorPane.setBackground(Color.BLACK);
        editorPane.setForeground(Color.WHITE);
        scroll = new EndScrollPane((Component) editorPane,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scroll);
    }

    public void setColor(Color color) {
        editorPane.setColor(color);
    }

    public void setBGColor(Color color) {
       editorPane.setBGColor(color);
    }

    @Override
    public void setFont(Font f) {
        editorPane.setFont(f);
    }

    public void addString(String s) {
        editorPane.add(s);
    }
    
    
    public void clearText() {
        editorPane.clearText();
    }

    public void closeOnType() {
        editorPane.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                System.exit(0);
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        editorPane.requestFocus();
    }
}