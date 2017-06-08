package simpleSoccer.agents;

import common.D2.Vector2D;
import static common.D2.Vector2D.*;
import static common.D2.geometry.*;

/**
 * Desc:  class to define a goal for a soccer pitch. The goal is defined
 *        by two 2D vectors representing the left and right posts.
 *
 *        Each time-step the method Scored should be called to determine
 *        if a goal has been scored.
 *
 */
public class Goal {

    private Vector2D m_vLeftPost;
    private Vector2D m_vRightPost;
    //a vector representing the facing direction of the goal
    private Vector2D m_vFacing;
    //the position of the center of the goal line
    private Vector2D m_vCenter;
    //each time Scored() detects a goal this is incremented
    private int m_iNumGoalsScored;

    public Goal(Vector2D left, Vector2D right, Vector2D facing) {
        m_vLeftPost = left;
        m_vRightPost = right;
        m_vCenter = div(add(left, right), 2.0);
        m_iNumGoalsScored = 0;
        m_vFacing = facing;
    }

    /**
     * Given the current ball position and the previous ball position,
     * this method returns true if the ball has crossed the goal line 
     * and increments m_iNumGoalsScored
     */
    public boolean Scored(final SoccerBall ball) {
        if (LineIntersection2D(ball.pos(), ball.oldPos(), m_vLeftPost, m_vRightPost)) {
            ++m_iNumGoalsScored;

            return true;
        }

        return false;
    }

    //-----------------------------------------------------accessor methods
    public Vector2D Center() {
        return new Vector2D(m_vCenter);
    }

    public Vector2D Facing() {
        return new Vector2D(m_vFacing);
    }

    public Vector2D LeftPost() {
        return new Vector2D(m_vLeftPost);
    }

    public Vector2D RightPost() {
        return new Vector2D(m_vRightPost);
    }

    public int NumGoalsScored() {
        return m_iNumGoalsScored;
    }

    public void ResetGoalsScored() {
        m_iNumGoalsScored = 0;
    }
}