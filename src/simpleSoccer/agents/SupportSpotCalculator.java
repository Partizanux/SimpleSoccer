package simpleSoccer.agents;

import common.D2.Vector2D;
import static common.D2.Vector2D.Vec2DDistance;
import common.game.Region;
import common.time.Regulator;
import static common.misc.Cgdi.gdi;
import static simpleSoccer.ParamLoader.Prm;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *  Desc:   Class to determine the best spots for a suppoting soccer
 *          player to move to.
 *
 */
public class SupportSpotCalculator {

    //a data structure to hold the values and positions of each spot
    private class SupportSpot {

        Vector2D m_vPos;
        double m_dScore;

        SupportSpot(Vector2D pos, double value) {
            m_vPos = new Vector2D(pos);
            m_dScore = value;
        }
    }
    private SoccerTeam m_pTeam;
    private List<SupportSpot> m_Spots = new ArrayList<SupportSpot>();
    //a pointer to the highest valued spot from the last update
    private SupportSpot m_pBestSupportingSpot;
    //this will regulate how often the spots are calculated (default is
    //one update per second)
    private Regulator m_pRegulator;

    //------------------------------- ctor ----------------------------------------
//-----------------------------------------------------------------------------
    public SupportSpotCalculator(int numX,
            int numY,
            SoccerTeam team) {
        m_pBestSupportingSpot = null;
        m_pTeam = team;
        final Region PlayingField = team.pitch().playingArea();

        //calculate the positions of each sweet spot, create them and 
        //store them in m_Spots
        double HeightOfSSRegion = PlayingField.Height() * 0.8;
        double WidthOfSSRegion = PlayingField.Width() * 0.9;
        double SliceX = WidthOfSSRegion / numX;
        double SliceY = HeightOfSSRegion / numY;

        double left = PlayingField.Left() + (PlayingField.Width() - WidthOfSSRegion) / 2.0 + SliceX / 2.0;
        double right = PlayingField.Right() - (PlayingField.Width() - WidthOfSSRegion) / 2.0 - SliceX / 2.0;
        double top = PlayingField.Top() + (PlayingField.Height() - HeightOfSSRegion) / 2.0 + SliceY / 2.0;

        for (int x = 0; x < (numX / 2) - 1; ++x) {
            for (int y = 0; y < numY; ++y) {
                if (m_pTeam.color() == SoccerTeam.blue) {
                    m_Spots.add(new SupportSpot(new Vector2D(left + x * SliceX, top + y * SliceY), 0.0));
                } else {
                    m_Spots.add(new SupportSpot(new Vector2D(right - x * SliceX, top + y * SliceY), 0.0));
                }
            }
        }

        //create the regulator
        m_pRegulator = new Regulator(Prm.SupportSpotUpdateFreq);
    }

//------------------------------- dtor ----------------------------------------
//-----------------------------------------------------------------------------
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        m_pRegulator = null;
    }

    /**
     * draws the spots to the screen as a hollow circles. The higher the 
     * score, the bigger the circle. The best supporting spot is drawn in
     * bright green.
     */
    public void Render() {
        gdi.HollowBrush();
        gdi.GreyPen();

        for (int spt = 0; spt < m_Spots.size(); ++spt) {
            gdi.Circle(m_Spots.get(spt).m_vPos, m_Spots.get(spt).m_dScore);
        }

        if (m_pBestSupportingSpot != null) {
            gdi.GreenPen();
            gdi.Circle(m_pBestSupportingSpot.m_vPos, m_pBestSupportingSpot.m_dScore);
        }
    }

    /**
     * this method iterates through each possible spot and calculates its
     * score.
     */
    public Vector2D DetermineBestSupportingPosition() {
        //only update the spots every few frames                              
        if (!m_pRegulator.isReady() && m_pBestSupportingSpot != null) {
            return m_pBestSupportingSpot.m_vPos;
        }

        //reset the best supporting spot
        m_pBestSupportingSpot = null;

        double BestScoreSoFar = 0.0;

        ListIterator<SupportSpot> it = m_Spots.listIterator();

        while (it.hasNext()) {
            SupportSpot curSpot = it.next();
            //first remove any previous score. (the score is set to one so that
            //the viewer can see the positions of all the spots if he has the 
            //aids turned on)
            curSpot.m_dScore = 1.0;

            //Test 1. is it possible to make a safe pass from the ball's position 
            //to this position?
            if (m_pTeam.isPassSafeFromAllOpponents(m_pTeam.controllingPlayer().pos(),
                    curSpot.m_vPos,
                    null,
                    Prm.MaxPassingForce)) {
                curSpot.m_dScore += Prm.Spot_PassSafeScore;
            }


            //Test 2. Determine if a goal can be scored from this position.  
            if (m_pTeam.canShoot(curSpot.m_vPos,
                    Prm.MaxShootingForce)) {
                curSpot.m_dScore += Prm.Spot_CanScoreFromPositionScore;
            }


            //Test 3. calculate how far this spot is away from the controlling
            //player. The further away, the higher the score. Any distances further
            //away than OptimalDistance pixels do not receive a score.
            if (m_pTeam.supportingPlayer() != null) { //TODO: nema tu byt team.controllingPlayer()??

                final double OptimalDistance = 200.0;

                double dist = Vec2DDistance(m_pTeam.controllingPlayer().pos(),
                        curSpot.m_vPos);

                double temp = abs(OptimalDistance - dist);

                if (temp < OptimalDistance) {

                    //normalize the distance and add it to the score
                    curSpot.m_dScore += Prm.Spot_DistFromControllingPlayerScore
                            * (OptimalDistance - temp) / OptimalDistance;
                }
            }

            //check to see if this spot has the highest score so far
            if (curSpot.m_dScore > BestScoreSoFar) {
                BestScoreSoFar = curSpot.m_dScore;

                m_pBestSupportingSpot = curSpot;
            }

        }

        return m_pBestSupportingSpot.m_vPos;
    }

    /**
     * returns the best supporting spot if there is one. If one hasn't been
     * calculated yet, this method calls determineBestSupportingPosition and
     * returns the result.
     */
    public Vector2D GetBestSupportingSpot() {
        if (m_pBestSupportingSpot != null) {
            return m_pBestSupportingSpot.m_vPos;
        } else {
            return DetermineBestSupportingPosition();
        }
    }
}
