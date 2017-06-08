package simpleSoccer.agents;

import common.D2.Vector2D;
import static common.D2.Vector2D.*;
import static common.D2.geometry.*;

/**
 * Desc:  class to define a goal for a soccer pitch. The goal is defined
 *        by two 2D vectors representing the left and right posts.
 *
 *        Each time-step the method scored should be called to determine
 *        if a goal has been scored.
 *
 */
public class Goal {
    private Vector2D leftPost;
    private Vector2D rightPost;
    /**
     * a vector representing the facing direction of the goal
     */
    private Vector2D facing;
    /**
     * the position of the center of the goal line
     */
    private Vector2D center;
    /**
     * each time scored() detects a goal this is incremented
     */
    private int goalsScored;

    public Goal(Vector2D left, Vector2D right, Vector2D facing) {
        leftPost = left;
        rightPost = right;
        center = div(add(left, right), 2.0);
        goalsScored = 0;
        this.facing = facing;
    }

    /**
     * Given the current ball position and the previous ball position,
     * this method returns true if the ball has crossed the goal line 
     * and increments goalsScored
     */
    public boolean scored(final SoccerBall ball) {
        if (LineIntersection2D(ball.pos(), ball.oldPos(), leftPost, rightPost)) {
            ++goalsScored;
            return true;
        }
        return false;
    }

    public Vector2D center() {
        return new Vector2D(center);
    }

    public Vector2D facing() {
        return new Vector2D(facing);
    }

    public Vector2D leftPost() {
        return new Vector2D(leftPost);
    }

    public Vector2D rightPost() {
        return new Vector2D(rightPost);
    }

    public int goalsScored() {
        return goalsScored;
    }

    public void resetGoalsScored() {
        goalsScored = 0;
    }
}