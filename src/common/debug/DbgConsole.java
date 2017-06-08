package common.debug;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Dimension;
import java.util.LinkedList;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.swing.JFrame;
import java.io.PrintStream;
import java.util.ListIterator;
import javax.swing.JOptionPane;
import static simpleSoccer.DEFINE.*;
import common.misc.JConsole;

/**
 * Desc:   Creates a resizable console window for recording and displaying
 *         debug info.
 *
 *         use the debug_con macro to send text and types to the console
 *         window via the print method. Flush the
 *         buffer using "" or the flush macro.  eg.
 *
 *        debug_con.print("Hello World!").print("");
 *
 */
public class DbgConsole implements DebugConsole {

    private static JConsole m_hwnd;
    //the string buffer. All input to debug stream is stored here
    private static LinkedList<String> m_Buffer = new LinkedList<String>();
//maximum number of lines shown in console before the buffer is flushed to 
//a file
    public final int MaxBufferSize = 500;
//initial dimensions of the console window
    public final static int DEBUG_WINDOW_WIDTH = 400;
    public final static int DEBUG_WINDOW_HEIGHT = 400;
    public static DebugConsole debug_con;

    static {
        //undefine DEBUG to send all debug messages to hyperspace (a sink - see below)
        //define(DEBUG);
        if (def(DEBUG)) {
            debug_con = DbgConsole.Instance();
        } else {
            debug_con = CSink.Instance();
        }
    }

//use these in your code to toggle output to the console on/off
    public static void debug_on() {
        DbgConsole.On();
    }

    public static void debug_off() {
        DbgConsole.Off();
    }

    /**
     * this little class just acts as a sink for any input. Used in place
     * of the DebugConsole class when the console is not required
     */
    static class CSink implements DebugConsole {

        private CSink() {
        }

        //copy ctor and assignment should be private
        private CSink(final CSink c) {
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException("Cloning not allowed");
        }
        private static final CSink instance = new CSink();

        public static CSink Instance() {
            return instance;
        }

        @Override
        public CSink print(final Object T) {
            return this;
        }
    }
    //if true the next input will be pushed into the buffer. If false,
    //it will be appended.
    private static boolean m_bFlushed = true;
    //set to true if the window is destroyed
    private static boolean m_bDestroyed = false;
    //if false the console will just disregard any input
    private static boolean m_bActive = true;
    //default logging file
    private static PrintStream m_LogOut;

    /**
     * this registers the window class and creates the window(called by the ctor)
     */
    private static boolean Create() {
        instance = new DbgConsole();
        m_hwnd = null;
        m_bFlushed = true;
        try {
            //open log file
            m_LogOut = new PrintStream(new FileOutputStream("DebugLog.txt"));
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Create the info window
        m_hwnd = new JConsole("Debug Console");
        m_hwnd.setPreferredSize(new Dimension(DEBUG_WINDOW_WIDTH, DEBUG_WINDOW_HEIGHT));
        m_hwnd.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    m_hwnd.setVisible(false);
                }
            }
        });

        m_hwnd.setBGColor(Color.DARK_GRAY);
        m_hwnd.setColor(Color.WHITE);
        // Show the window
        m_hwnd.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        m_hwnd.setVisible(true);
        UpdateWindow(m_hwnd);

        return true;

    }

    private static void UpdateWindow(JFrame frame) {
        frame.repaint();
    }

    private DbgConsole() {
    }

    //copy ctor and assignment should be private
    private DbgConsole(final DebugConsole console) {
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning not allowed");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        WriteAndResetBuffer();
    }
    private static DbgConsole instance;
    private static boolean created = false;

    /**    
     * Retrieve a pointer to an instance of this class
     */
    public static DbgConsole Instance() {
        if (!created) {
            Create();
            created = true;
        }

        return instance;
    }

    public void ClearBuffer() {
        m_Buffer.clear();
        flush();
    }

    public static void flush() {
        if (!m_bDestroyed) {
            ListIterator<String> it = m_Buffer.listIterator();
            while (it.hasNext()) {
                m_hwnd.addString(it.next());
            }
            m_Buffer.clear();
            m_bFlushed = true;
        }
    }
    
    /**
     * writes the contents of the buffer to the file "debugLog.txt", clears
     * the buffer and resets the appropriate scroll info
     */
    public void WriteAndResetBuffer() {
        m_bFlushed = true;

        //write out the contents of the buffer to a file
        ListIterator<String> it = m_Buffer.listIterator();

        while (it.hasNext()) {
            m_LogOut.println(it.next());
        }

        m_Buffer.clear();
        //m_hwnd.clearText();
    }

    //use to activate deactivate
    public static void Off() {
        m_bActive = false;
    }

    public static void On() {
        m_bActive = true;
    }

    public boolean Destroyed() {
        return m_bDestroyed;
    }

    @Override
    public DbgConsole print(final Object t) {
        if (!m_bActive || m_bDestroyed) {
            return this;
        }

        //reset buffer and scroll info if it overflows. Write the excess
        //to file
        if (m_Buffer.size() > MaxBufferSize) {
            WriteAndResetBuffer();
        }

        if (t.toString().equals("")) {
            m_Buffer.add("\n");
            flush();
            return this;
        }
        
       m_Buffer.add(t.toString());
        
        return this;
    }
}
