package simpleSoccer;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import javax.swing.JMenuBar;
import java.awt.event.KeyEvent;
import common.misc.CppToJava;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Point;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Cursor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

import common.misc.Utils;
import simpleSoccer.Script1.MyMenuBar;
import common.time.PrecisionTimer;
import simpleSoccer.agents.SoccerPitch;

import static simpleSoccer.ParamLoader.Prm;
import static simpleSoccer.resource.*;
import static common.misc.Cgdi.gdi;
import static common.misc.WindowUtils.*;
import static common.windows.*;

public class Game {
    private static String g_szApplicationName = "Simple Soccer";
    private static SoccerPitch soccerPitch;

    //TODO check carefully
    /**
     * because of game restart soccerPitch may be null for a while
     */
    private static Lock soccerPitchLock = new ReentrantLock();

    private static PrecisionTimer timer = new PrecisionTimer(Prm.FrameRate);

//used when a user clicks on a menu item to ensure the option is 'checked'
//correctly
    static void CheckAllMenuItemsAppropriately(MyMenuBar hwnd) {
        CheckMenuItemAppropriately(hwnd, IDM_SHOW_REGIONS, Prm.bRegions);
        CheckMenuItemAppropriately(hwnd, IDM_SHOW_STATES, Prm.bStates);
        CheckMenuItemAppropriately(hwnd, IDM_SHOW_IDS, Prm.bIDs);
        CheckMenuItemAppropriately(hwnd, IDM_AIDS_SUPPORTSPOTS, Prm.bSupportSpots);
        CheckMenuItemAppropriately(hwnd, ID_AIDS_SHOWTARGETS, Prm.bViewTargets);
        CheckMenuItemAppropriately(hwnd, IDM_AIDS_HIGHLITE, Prm.bHighlightIfThreatened);
    }

    static void HandleMenuItems(int wParam, MyMenuBar hwnd) {
        switch (wParam) {
            case ID_AIDS_NOAIDS:

                Prm.bStates = false;
                Prm.bRegions = false;
                Prm.bIDs = false;
                Prm.bSupportSpots = false;
                Prm.bViewTargets = false;
                Prm.bHighlightIfThreatened = false;

                CheckAllMenuItemsAppropriately(hwnd);

                break;

            case IDM_SHOW_REGIONS:

                Prm.bRegions = !Prm.bRegions;

                CheckAllMenuItemsAppropriately(hwnd);

                break;

            case IDM_SHOW_STATES:

                Prm.bStates = !Prm.bStates;

                CheckAllMenuItemsAppropriately(hwnd);

                break;

            case IDM_SHOW_IDS:

                Prm.bIDs = !Prm.bIDs;

                CheckAllMenuItemsAppropriately(hwnd);

                break;


            case IDM_AIDS_SUPPORTSPOTS:

                Prm.bSupportSpots = !Prm.bSupportSpots;

                CheckAllMenuItemsAppropriately(hwnd);

                break;

            case ID_AIDS_SHOWTARGETS:

                Prm.bViewTargets = !Prm.bViewTargets;

                CheckAllMenuItemsAppropriately(hwnd);

                break;

            case IDM_AIDS_HIGHLITE:

                Prm.bHighlightIfThreatened = !Prm.bHighlightIfThreatened;

                CheckAllMenuItemsAppropriately(hwnd);

                break;

        }//end switch
    }
    static BufferedImage buffer;
    static Graphics2D hdcBackBuffer;
    //these hold the dimensions of the client window area
    static int cxClient;
    static int cyClient;
    static JPanel panel;

    static void createPanel() {
        panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                gdi.StartDrawing(hdcBackBuffer);
                //fill our backbuffer with white
                gdi.fillRect(Color.WHITE, 0, 0, Prm.width, Prm.height);
                soccerPitchLock.lock();
                soccerPitch.render();
                soccerPitchLock.unlock();
                gdi.StopDrawing(hdcBackBuffer);
                g.drawImage(buffer, 0, 0, null);
            }
        };
        panel.setSize(Prm.width, Prm.height);
        panel.setPreferredSize(new Dimension(Prm.width, Prm.height));
    }

    public static void main(String[] args) {
        final Window window = new Window(g_szApplicationName);
        window.setIconImage(LoadIcon("/SimpleSoccer/icon1.png"));
        window.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        buffer = new BufferedImage(Prm.width, Prm.height, BufferedImage.TYPE_INT_RGB);
        hdcBackBuffer = buffer.createGraphics();
        //these hold the dimensions of the client window area
        cxClient = buffer.getWidth();
        cyClient = buffer.getHeight();
        soccerPitch = new SoccerPitch(cxClient, cyClient);

        //seed random number generator
        Utils.setSeed(0);

        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        //Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        window.setResizable(false);

        int y = center.y - window.getHeight() / 2;
        //window.setLocation(center.x - window.getWidth() / 2, y >= 0 ? y : 0);
        window.setLocation(0 ,0);
        Script1.MyMenuBar menu = Script1.createMenu(IDR_MENU1);
        window.setJMenuBar(menu);

        CheckAllMenuItemsAppropriately(menu);

        createPanel();

        window.add(panel);
        window.pack();
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                CppToJava.keyCache.released(e);
                switch (e.getKeyChar()) {
                    case KeyEvent.VK_ESCAPE: {
                        System.exit(0);
                    }
                    break;
                    case 'r':
                    case 'R': {
                        soccerPitchLock.lock();
                        soccerPitch = null;
                        soccerPitch = new SoccerPitch(cxClient, cyClient);
                        soccerPitchLock.unlock();
                        JMenuBar bar = Script1.createMenu(IDR_MENU1);
                        window.setJMenuBar(bar);
                        bar.revalidate();
                    }
                    break;

                    case 'p':
                    case 'P': {
                        soccerPitch.togglePause();
                    }
                    break;

                }//end switch
            }//end switch        }

            @Override
            public void keyPressed(KeyEvent e) {
                CppToJava.keyCache.pressed(e);
            }
        });
        window.addComponentListener(new ComponentAdapter() {
            //has the user resized the client area?
            @Override
            public void componentResized(ComponentEvent e) {
                //if so we need to update our variables so that any drawing
                //we do using cx and cy is scaled accordingly
                cxClient = e.getComponent().getBounds().width;
                cyClient = e.getComponent().getBounds().height;
                //now to resize the backbuffer accordingly.
                buffer = new BufferedImage(cxClient, cyClient, BufferedImage.TYPE_INT_RGB);
                hdcBackBuffer = buffer.createGraphics();
            }
        });
        window.setVisible(true);

        //timer.smoothUpdatesOn();

        timer.start();

        long startPoint = System.nanoTime();
        long endPoint;
        long counter = 0;

        while (true) {
            if (timer.readyForNextFrame()) {
                soccerPitch.update();
                panel.repaint();

                counter++;
                endPoint = System.nanoTime();
                if (endPoint - startPoint > 1000000000L) {
                    System.out.println(counter);
                    counter = 0;
                    startPoint = endPoint;
                }
            }
        }
    }
}