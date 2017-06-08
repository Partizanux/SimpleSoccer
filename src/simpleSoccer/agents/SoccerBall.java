package simpleSoccer.agents;

import common.D2.Wall2D;
import java.util.List;
import static java.lang.Math.sqrt;
import common.D2.Vector2D;
import static simpleSoccer.ParamLoader.Prm;
import static common.D2.geometry.*;
import static common.D2.geometry.span_type.*;
import static common.D2.Transformation.Vec2DRotateAroundOrigin;
import static common.D2.Vector2D.*;
import common.messaging.Telegram;
import static common.misc.Utils.*;
import static common.misc.Cgdi.gdi;

public class SoccerBall extends MovingEntity {
    /**
     * keeps a record of the ball's position at the last update
     */
    private Vector2D oldPos;

    /**
     * a local reference to the walls that make up the pitch boundary
     */
    private final List<Wall2D> pitchBoundary;

    public SoccerBall(Vector2D pos, double ballSize, double mass, List<Wall2D> pitchBoundary) {
        super(pos, ballSize, new Vector2D(0, 0), -1.0, new Vector2D(0, 1), mass,
                new Vector2D(1.0, 1.0), 0, 0);
        this.pitchBoundary = pitchBoundary;
    }

    /**
     * tests to see if the ball has collided with a ball and reflects 
     * the ball's velocity accordingly
     */
    void testCollisionWithWalls(final List<Wall2D> walls) {
        //test ball against each wall, find out which is closest
        int idxClosest = -1;

        Vector2D VelNormal = Vec2DNormalize(velocity);

        Vector2D IntersectionPoint,
                CollisionPoint = new Vector2D();

        double DistToIntersection = Float.MAX_VALUE;

        //iterate through each wall and calculate if the ball intersects.
        //if it does then store the index into the closest intersecting wall
        for (int w = 0; w < walls.size(); ++w) {
            //assuming a collision if the ball continued on its current heading 
            //calculate the point on the ball that would hit the wall. This is 
            //simply the wall's normal(inversed) multiplied by the ball's radius
            //and added to the balls center (its position)
            Vector2D ThisCollisionPoint = sub(pos(), (mul(walls.get(w).Normal(), getBRadius())));

            //calculate exactly where the collision point will hit the plane    
            if (WhereIsPoint(ThisCollisionPoint,
                    walls.get(w).From(),
                    walls.get(w).Normal()) == plane_backside) {
                double DistToWall = DistanceToRayPlaneIntersection(ThisCollisionPoint,
                        walls.get(w).Normal(),
                        walls.get(w).From(),
                        walls.get(w).Normal());

                IntersectionPoint = add(ThisCollisionPoint, (mul(DistToWall, walls.get(w).Normal())));

            } else {
                double DistToWall = DistanceToRayPlaneIntersection(ThisCollisionPoint,
                        VelNormal,
                        walls.get(w).From(),
                        walls.get(w).Normal());

                IntersectionPoint = add(ThisCollisionPoint, (mul(DistToWall, VelNormal)));
            }

            //check to make sure the intersection point is actually on the line
            //segment
            boolean OnLineSegment = false;

            if (LineIntersection2D(walls.get(w).From(),
                    walls.get(w).To(),
                    sub(ThisCollisionPoint, mul(walls.get(w).Normal(), 20.0)),
                    add(ThisCollisionPoint, mul(walls.get(w).Normal(), 20.0)))) {

                OnLineSegment = true;
            }


            //Note, there is no test for collision with the end of a line segment

            //now check to see if the collision point is within range of the
            //velocity vector. [work in distance squared to avoid sqrt] and if it
            //is the closest hit found so far. 
            //If it is that means the ball will collide with the wall sometime
            //between this time step and the next one.
            double distSq = Vec2DDistanceSq(ThisCollisionPoint, IntersectionPoint);

            if ((distSq <= velocity.LengthSq()) && (distSq < DistToIntersection) && OnLineSegment) {
                DistToIntersection = distSq;
                idxClosest = w;
                CollisionPoint = IntersectionPoint;
            }
        }

        //to prevent having to calculate the exact time of collision we
        //can just check if the velocity is opposite to the wall normal
        //before reflecting it. This prevents the case where there is overshoot
        //and the ball gets reflected back over the line before it has completely
        //reentered the playing area.
        if ((idxClosest >= 0) && VelNormal.Dot(walls.get(idxClosest).Normal()) < 0) {
            velocity.Reflect(walls.get(idxClosest).Normal());
        }
    }

    /**
     * updates the ball physics, tests for any collisions and adjusts
     * the ball's velocity accordingly
     */
    @Override
    public void update() {
        //keep a record of the old position so the goal::scored method
        //can utilize it for goal testing
        oldPos = new Vector2D(pos);

        //Test for collisions
        testCollisionWithWalls(pitchBoundary);

        //Simulate Prm.Friction. Make sure the speed is positive 
        //first though
        if (velocity.LengthSq() > Prm.Friction * Prm.Friction) {
            velocity.add(mul(Vec2DNormalize(velocity), Prm.Friction));
            pos.add(velocity);


            //update heading
            heading = Vec2DNormalize(velocity);
        }
    }

    @Override
    public void render() {
        gdi.BlackBrush();

        gdi.Circle(pos, boundingRadius);

        /*
        gdi.GreenBrush();
        for (int i=0; i<IPPoints.size(); ++i)
        {
        gdi.Circle(IPPoints[i], 3);
        }
         */
    }

    @Override
    public boolean handleMessage(final Telegram msg) {
        return false;
    }

    /**
     * apply a force to the ball in the direction of heading. Truncates
     * the new velocity to make sure it doesn't exceed the max allowable.
     */
    public void kick(Vector2D direction, double force) {
        //ensure direction is normalized
        direction.Normalize();

        //calculate the acceleration
        Vector2D acceleration = div(mul(direction, force), mass);

        //update the velocity
        velocity = acceleration;
    }

    /**
     * Given a force and a distance to cover given by two vectors, this
     * method calculates how long it will take the ball to travel between
     * the two points
     */
    public double timeToCoverDistance(Vector2D A, Vector2D B, double force) {
        //this will be the velocity of the ball in the next time step *if*
        //the player was to make the pass. 
        double speed = force / mass;

        //calculate the velocity at B using the equation v^2 = u^2 + 2as

        //first calculate s (the distance between the two positions)
        double distanceToCover = Vec2DDistance(A, B);

        double term = speed * speed + 2.0 * distanceToCover * Prm.Friction;

        //if  (u^2 + 2as) is negative it means the ball cannot reach point B.
        if (term <= 0.0) {
            return -1.0;
        }

        double v = sqrt(term);

        // it is possible for the ball to reach B and we know its speed when it
        // gets there, so now it's easy to calculate the time using the equation
        // t = (v-u)/a
        return (v - speed) / Prm.Friction;
    }

    /**
     * given a time this method returns the ball position at that time in the
     * future using the equation s = ut + 1/2at^2, where s = distance, a = friction, u = start velocity
     */
    public Vector2D futurePosition(double time) {
        //calculate the ut term, which is a vector
        Vector2D ut = mul(velocity, time);

        //calculate the 1/2at^2 term, which is scalar
        double half_a_t_squared = 0.5 * Prm.Friction * time * time;

        //turn the scalar quantity into a vector by multiplying the value with
        //the normalized velocity vector (because that gives the direction)
        Vector2D ScalarToVector = mul(half_a_t_squared, Vec2DNormalize(velocity));

        //the predicted position is the balls position plus these two terms
        return add(pos(), ut).add(ScalarToVector);
    }

    /**
     * this is used by players and goalkeepers to 'trap' a ball -- to stop
     * it dead. That player is then assumed to be in possession of the ball
     * and m_pOwner is adjusted accordingly
     */
    public void trap() {
        velocity.Zero();
    }

    public Vector2D oldPos() {
        return new Vector2D(oldPos);
    }

    /**
     * positions the ball at the desired location and sets the ball's velocity to
     *  zero
     */
    public void placeAtPosition(Vector2D NewPos) {
        pos = new Vector2D(NewPos);
        oldPos = new Vector2D(pos);
        velocity.Zero();
    }

    /**
     *  this can be used to vary the accuracy of a player's kick. Just call it 
     *  prior to kicking the ball using the ball's position and the ball target as
     *  parameters.
     */
    public static Vector2D addNoiseToKick(Vector2D ballPos, Vector2D ballTarget) {
        double displacement = (Pi - Pi * Prm.PlayerKickingAccuracy) * randomClamped();
        Vector2D toTarget = sub(ballTarget, ballPos);
        Vec2DRotateAroundOrigin(toTarget, displacement);
        return add(toTarget, ballPos);
    }
}
