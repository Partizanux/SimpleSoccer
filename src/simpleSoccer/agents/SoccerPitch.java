package simpleSoccer.agents;

import simpleSoccer.ParamLoader;
import simpleSoccer.teamStates.PrepareForKickOff;
import common.misc.Cgdi;
import common.D2.Vector2D;
import common.game.Region;
import common.D2.Wall2D;
import java.lang.reflect.Array;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import static simpleSoccer.ParamLoader.Prm;
import static common.misc.Cgdi.gdi;
import static common.misc.Stream_Utility_function.ttos;

public class SoccerPitch {
    public static final int NumRegionsHorizontal = 6;
    public static final int NumRegionsVertical = 3;
    private SoccerBall ball;
    private SoccerTeam redTeam;
    private SoccerTeam blueTeam;
    private Goal redGoal;
    private Goal blueGoal;
    //container for the boundary walls
    private List<Wall2D> walls = new ArrayList<Wall2D>();
    //defines the dimensions of the playing area
    private Region playingArea;
    //the playing field is broken up into regions that the team
    //can make use of to implement strategies.
    private List<Region> regions;
    //true if a goal keeper has possession
    private boolean goalKeeperHasBall;
    //true if the game is in play. Set to false whenever the players
    //are getting ready for kickoff
    private boolean gameOn;
    //set true to pause the motion
    private boolean paused;
    //local copy of client window dimensions
    private int cx, cy;

    /**
     ** Instantiates the regions the players utilize to  position
     ** themselves
     */
    private void CreateRegions(double width, double height) {
        //index into the vector
        int idx = regions.size() - 1;

        for (int col = 0; col < NumRegionsHorizontal; ++col) {
            for (int row = 0; row < NumRegionsVertical; ++row) {
                regions.set(idx, new Region(playingArea().Left() + col * width,
                        playingArea().Top() + row * height,
                        playingArea().Left() + (col + 1) * width,
                        playingArea().Top() + (row + 1) * height,
                        idx));
                --idx;
            }
        }
    }

    public SoccerPitch(int cx, int cy) {
        this.cx = cx;
        this.cy = cy;
        paused = false;
        goalKeeperHasBall = false;
        regions = Arrays.asList((Region[]) Array.newInstance(Region.class, NumRegionsHorizontal * NumRegionsVertical));
        gameOn = true;
        //define the playing area
        playingArea = new Region(20, 20, cx - 20, cy - 20);

        //create the regions  
        CreateRegions(playingArea().Width() / (double) NumRegionsHorizontal,
                playingArea().Height() / (double) NumRegionsVertical);

        //create the goals
        redGoal = new Goal(new Vector2D(playingArea.Left(), (cy - Prm.GoalWidth) / 2),
                new Vector2D(playingArea.Left(), cy - (cy - Prm.GoalWidth) / 2),
                new Vector2D(1, 0));



        blueGoal = new Goal(new Vector2D(playingArea.Right(), (cy - Prm.GoalWidth) / 2),
                new Vector2D(playingArea.Right(), cy - (cy - Prm.GoalWidth) / 2),
                new Vector2D(-1, 0));


        //create the soccer ball
        ball = new SoccerBall(new Vector2D((double) this.cx / 2.0, (double) this.cy / 2.0),
                Prm.BallSize,
                Prm.BallMass,
                walls);


        //create the teams 
        redTeam = new SoccerTeam(redGoal, blueGoal, this, SoccerTeam.red);
        blueTeam = new SoccerTeam(blueGoal, redGoal, this, SoccerTeam.blue);

        //make sure each team knows who their opponents are
        redTeam.setOpponents(blueTeam);
        blueTeam.setOpponents(redTeam);

        //create the walls
        Vector2D TopLeft = new Vector2D(playingArea.Left(), playingArea.Top());
        Vector2D TopRight = new Vector2D(playingArea.Right(), playingArea.Top());
        Vector2D BottomRight = new Vector2D(playingArea.Right(), playingArea.Bottom());
        Vector2D BottomLeft = new Vector2D(playingArea.Left(), playingArea.Bottom());

        walls.add(new Wall2D(BottomLeft, redGoal.RightPost()));
        walls.add(new Wall2D(redGoal.LeftPost(), TopLeft));
        walls.add(new Wall2D(TopLeft, TopRight));
        walls.add(new Wall2D(TopRight, blueGoal.LeftPost()));
        walls.add(new Wall2D(blueGoal.RightPost(), BottomRight));
        walls.add(new Wall2D(BottomRight, BottomLeft));

        ParamLoader p = ParamLoader.Instance(); // WTF??
    }

    /**
     *  this demo works on a fixed frame rate (60 by default) so we don't need
     *  to pass a time_elapsed as a parameter to the game entities
     */
    public void Update() {
        if (paused) {
            return;
        }

        //update the balls
        ball.update();

        //update the teams
        redTeam.update();
        blueTeam.update();

        //if a goal has been detected reset the pitch ready for kickoff
        if (blueGoal.Scored(ball) || redGoal.Scored(ball)) {
            gameOn = false;

            //reset the ball                                                      
            ball.placeAtPosition(new Vector2D((double) cx / 2.0, (double) cy / 2.0));

            //get the teams ready for kickoff
            redTeam.getFSM().ChangeState(PrepareForKickOff.INSTANCE);
            blueTeam.getFSM().ChangeState(PrepareForKickOff.INSTANCE);
        }
    }

    public boolean Render() {
        //draw the grass
        gdi.DarkGreenPen();
        gdi.DarkGreenBrush();
        gdi.Rect(0, 0, cx, cy);

        //render regions
        if (Prm.bRegions) {
            for (int r = 0; r < regions.size(); ++r) {
                regions.get(r).Render(true);
            }
        }

        //render the goals
        gdi.HollowBrush();
        gdi.RedPen();
        gdi.Rect(playingArea.Left(), (cy - Prm.GoalWidth) / 2, playingArea.Left() + 40,
                cy - (cy - Prm.GoalWidth) / 2);

        gdi.BluePen();
        gdi.Rect(playingArea.Right(), (cy - Prm.GoalWidth) / 2, playingArea.Right() - 40,
                cy - (cy - Prm.GoalWidth) / 2);

        //render the pitch markings
        gdi.WhitePen();
        gdi.Circle(playingArea.Center(), playingArea.Width() * 0.125);
        gdi.Line(playingArea.Center().x, playingArea.Top(), playingArea.Center().x, playingArea.Bottom());
        gdi.WhiteBrush();
        gdi.Circle(playingArea.Center(), 2.0);


        //the ball
        gdi.WhitePen();
        gdi.WhiteBrush();
        ball.render();

        //render the teams
        redTeam.render();
        blueTeam.render();

        //render the walls
        gdi.WhitePen();
        for (int w = 0; w < walls.size(); ++w) {
            walls.get(w).Render();
        }

        //show the score
        gdi.TextColor(Cgdi.red);
        gdi.TextAtPos((cx / 2) - 50, cy - 18,
                "Red: " + ttos(blueGoal.NumGoalsScored()));

        gdi.TextColor(Cgdi.blue);
        gdi.TextAtPos((cx / 2) + 10, cy - 18,
                "Blue: " + ttos(redGoal.NumGoalsScored()));

        return true;
    }

    public void togglePause() {
        paused = !paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public int cx() {
        return cx;
    }

    public int cy() {
        return cy;
    }

    public boolean goalKeeperHasBall() {
        return goalKeeperHasBall;
    }

    public void setGoalKeeperHasBall(boolean b) {
        goalKeeperHasBall = b;
    }

    public Region playingArea() {
        return playingArea;
    }

    public List<Wall2D> walls() {
        return walls;
    }

    SoccerBall ball() {
        return ball;
    }

    public Region getRegionFromIndex(int idx) {
        assert ((idx >= 0) && (idx < (int) regions.size()));
        return regions.get(idx);
    }

    public boolean isGameOn() {
        return gameOn;
    }

    public void gameOn() {
        gameOn = true;
    }

    public void gameOff() {
        gameOn = false;
    }
}
