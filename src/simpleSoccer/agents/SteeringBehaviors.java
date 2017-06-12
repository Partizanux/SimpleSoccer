package simpleSoccer.agents;

import java.util.ListIterator;
import common.misc.AutoList;
import java.util.List;
import common.D2.Vector2D;
import java.lang.reflect.Array;
import java.util.Arrays;
import static common.D2.Vector2D.*;
import static common.misc.Cgdi.gdi;
import static simpleSoccer.ParamLoader.Prm;

public class SteeringBehaviors {
    private PlayerBase player;
    private SoccerBall ball;

    /**
     * the steering force created by the combined effect of allthe selected behaviors
     */
    private Vector2D steeringForce = new Vector2D();

    /**
     * the current target (usually the ball or predicted ball position)
     */
    private Vector2D target = new Vector2D();

    /**
     * the distance the player tries to INTERPOSE from the target
     */
    private double interposeDist;
    private double multSeparation;
    private double viewDistance;

    /**
     * used by group behaviors to tag neighbours
     */
    private boolean tagged;

    /**
     * binary flags to indicate whether or not a Behavior should be active
     */
    private int m_iFlags;


    //TODO refactor
    private enum Behavior {
        NONE(0x0000),
        SEEK(0x0001),
        ARRIVE(0x0002),
        SEPARATION(0x0004),
        PURSUIT(0x0008),
        INTERPOSE(0x0010);
        private int flag;

        Behavior(int flag) {
            this.flag = flag;
        }

        public int flag() {
            return flag;
        }
    }

    //TODO refactor
    /**
     * ARRIVE makes use of these to determine how quickly a vehicle
     * should decelerate to its target
     */
    private enum Deceleration {
        slow(3), normal(2), fast(1);
        private int dec;

        Deceleration(int d) {
            this.dec = d;
        }

        public int value() {
            return dec;
        }
    }

    /**
     * Given a target, this Behavior returns a steering force which will
     * align the agent with the target and move the agent in the desired
     * direction
     */
    private Vector2D seek(Vector2D target) {
        Vector2D DesiredVelocity = mul(sub(target, player.pos()), player.maxSpeed());
        return (sub(DesiredVelocity, player.velocity()));
    }

    /**
     * This Behavior is similar to SEEK but it attempts to ARRIVE at the
     *  target with a zero velocity
     */
    private Vector2D arrive(Vector2D TargetPos, Deceleration deceleration) {
        Vector2D ToTarget = sub(TargetPos, player.pos());

        //calculate the distance to the target
        double dist = ToTarget.Length();

        if (dist > 0) {
            //because Deceleration is enumerated as an int, this value is required
            //to provide fine tweaking of the Deceleration..
            final double DecelerationTweaker = 0.3;

            //calculate the speed required to reach the target given the desired
            //Deceleration
            double speed = dist / ((double) deceleration.value() * DecelerationTweaker);

            //make sure the velocity does not exceed the max
            speed = Math.min(speed, player.maxSpeed());

            //from here proceed just like seek except we don't need to normalize
            //the ToTarget vector because we have already gone to the trouble
            //of calculating its length: dist. 
            Vector2D DesiredVelocity = mul(ToTarget, speed / dist);

            return sub(DesiredVelocity, player.velocity());
        }

        return new Vector2D(0, 0);
    }

    /**
     * This Behavior predicts where its prey will be and seeks
     * to that location
     * This Behavior creates a force that steers the agent towards the
     * ball
     */
    private Vector2D pursuit(final SoccerBall ball) {
        Vector2D ToBall = sub(ball.pos(), player.pos());

        //the lookahead time is proportional to the distance between the ball
        //and the pursuer; 
        double LookAheadTime = 0.0;

        if (ball.speed() != 0.0) {
            LookAheadTime = ToBall.Length() / ball.speed();
        }

        //calculate where the ball will be at this time in the future
        target = ball.futurePosition(LookAheadTime);

        //now SEEK to the predicted future position of the ball
        return arrive(target, Deceleration.fast);
    }

    /**
     *
     * this calculates a force repelling from the other neighbors
     */
    private Vector2D separation() {
        //iterate through all the neighbors and calculate the vector from them
        Vector2D SteeringForce = new Vector2D();

        List<PlayerBase> AllPlayers = new AutoList<PlayerBase>().GetAllMembers();
        ListIterator<PlayerBase> it = AllPlayers.listIterator();
        while (it.hasNext()) {
            PlayerBase curPlyr = it.next();
            //make sure this agent isn't included in the calculations and that
            //the agent is close enough
            if ((curPlyr != player) && curPlyr.steering().isTagged()) {
                Vector2D ToAgent = sub(player.pos(), curPlyr.pos());

                //scale the force inversely proportional to the agents distance
                //from its neighbor.
                SteeringForce.add(div(Vec2DNormalize(ToAgent), ToAgent.Length()));
            }
        }

        return SteeringForce;
    }

    /**
     * Given an opponent and an object position this method returns a 
     * force that attempts to position the agent between them
     */
    private Vector2D interpose(SoccerBall ball, Vector2D target, double interposeDist) {
        Vector2D toBall = sub(ball.pos(), target);
        Vector2D interposeFromTarget = mul(Vec2DNormalize(toBall), interposeDist);
        Vector2D interposePoint = add(target, interposeFromTarget);
        return arrive(interposePoint, Deceleration.normal);
    }

    /**
     *  tags any vehicles within a predefined radius
     */
    private void FindNeighbours() {
        List<PlayerBase> AllPlayers = new AutoList<PlayerBase>().GetAllMembers();
        ListIterator<PlayerBase> it = AllPlayers.listIterator();
        while (it.hasNext()) {
            PlayerBase curPlyr = it.next();

            //first clear any current tag
            curPlyr.steering().unTag();

            //work in distance squared to avoid sqrts
            Vector2D to = sub(curPlyr.pos(), player.pos());

            if (to.LengthSq() < (viewDistance * viewDistance)) {
                curPlyr.steering().tag();
            }
        }//next
    }

    /**
     * this function tests if a specific bit of m_iFlags is set
     */
    private boolean on(Behavior bt) {
        return (m_iFlags & bt.flag()) == bt.flag();
    }

    /**
     *  This function calculates how much of its max steering force the 
     *  vehicle has left to apply and then applies that amount of the
     *  force to add.
     */
    private boolean accumulateForce(Vector2D sf, Vector2D ForceToAdd) {
        //first calculate how much steering force we have left to use
        double MagnitudeSoFar = sf.Length();

        double magnitudeRemaining = player.maxForce() - MagnitudeSoFar;

        //return false if there is no more force left to use
        if (magnitudeRemaining <= 0.0) {
            return false;
        }

        //calculate the magnitude of the force we want to add
        double MagnitudeToAdd = ForceToAdd.Length();

        //now calculate how much of the force we can really add  
        if (MagnitudeToAdd > magnitudeRemaining) {
            MagnitudeToAdd = magnitudeRemaining;
        }

        //add it to the steering force
        sf.add(mul(Vec2DNormalize(ForceToAdd), MagnitudeToAdd));

        return true;
    }

    /**
     * this method calls each active steering Behavior and acumulates their
     *  forces until the max steering force magnitude is reached at which
     *  time the function returns the steering force accumulated to that 
     *  point
     */
    private Vector2D sumForces() {
        Vector2D force = new Vector2D();

        //the soccer players must always tag their neighbors
        FindNeighbours();

        if (on(Behavior.SEPARATION)) {
            force.add(mul(separation(), multSeparation));

            if (!accumulateForce(steeringForce, force)) {
                return steeringForce;
            }
        }

        if (on(Behavior.SEEK)) {
            force.add(seek(target));

            if (!accumulateForce(steeringForce, force)) {
                return steeringForce;
            }
        }

        if (on(Behavior.ARRIVE)) {
            force.add(arrive(target, Deceleration.fast));

            if (!accumulateForce(steeringForce, force)) {
                return steeringForce;
            }
        }

        if (on(Behavior.PURSUIT)) {
            force.add(pursuit(ball));

            if (!accumulateForce(steeringForce, force)) {
                return steeringForce;
            }
        }

        if (on(Behavior.INTERPOSE)) {
            force.add(interpose(ball, target, interposeDist));

            if (!accumulateForce(steeringForce, force)) {
                return steeringForce;
            }
        }

        return steeringForce;
    }
    //a vertex buffer to contain the feelers rqd for dribbling
    private List<Vector2D> m_Antenna;

//------------------------- ctor -----------------------------------------
//
//------------------------------------------------------------------------
    public SteeringBehaviors(PlayerBase agent,
            SoccerPitch world,
            SoccerBall ball) {
        player = agent;
        m_iFlags = 0;
        multSeparation = Prm.SeparationCoefficient;
        tagged = false;
        viewDistance = Prm.ViewDistance;
        this.ball = ball;
        interposeDist = 0.0;
        m_Antenna = Arrays.asList((Vector2D[]) Array.newInstance(Vector2D.class, 5));
    }

    /**
     * calculates the overall steering force based on the currently active
     * steering behaviors. 
     */
    public Vector2D calculate() {
        //reset the force
        steeringForce.Zero();

        //this will hold the value of each individual steering force
        steeringForce = sumForces();

        //make sure the force doesn't exceed the vehicles maximum allowable
        steeringForce.Truncate(player.maxForce());

        return new Vector2D(steeringForce);
    }

    /**
     * calculates the component of the steering force that is parallel
     * with the vehicle heading
     */
    public double forwardComponent() {
        return player.heading().Dot(steeringForce);
    }

    /**
     * calculates the component of the steering force that is perpendicuar
     * with the vehicle heading
     */
    public double sideComponent() {
        return player.side().Dot(steeringForce) * player.maxTurnRate();
    }

    public Vector2D force() {
        return steeringForce;
    }

    /**
     * renders visual aids and info for seeing how each Behavior is
     * calculated
     */
    public void renderAids() {
        //render the steering force
        gdi.RedPen();
        gdi.Line(player.pos(), add(player.pos(), mul(steeringForce, 20)));
    }

    public Vector2D target() {
        return new Vector2D(target);
    }

    public void setTarget(final Vector2D t) {
        target = new Vector2D(t);
    }

    public double interposeDistance() {
        return interposeDist;
    }

    public void setInterposeDistance(double d) {
        interposeDist = d;
    }

    public boolean isTagged() {
        return tagged;
    }

    public void tag() {
        tagged = true;
    }

    public void unTag() {
        tagged = false;
    }

    public void seekOn() {
        m_iFlags |= Behavior.SEEK.flag();
    }

    public void arriveOn() {
        m_iFlags |= Behavior.ARRIVE.flag();
    }

    public void pursuitOn() {
        m_iFlags |= Behavior.PURSUIT.flag();
    }

    public void separationOn() {
        m_iFlags |= Behavior.SEPARATION.flag();
    }

    public void interposeOn(double d) {
        m_iFlags |= Behavior.INTERPOSE.flag();
        interposeDist = d;
    }

    public void seekOff() {
        if (on(Behavior.SEEK)) {
            m_iFlags ^= Behavior.SEEK.flag();
        }
    }

    public void arriveOff() {
        if (on(Behavior.ARRIVE)) {
            m_iFlags ^= Behavior.ARRIVE.flag();
        }
    }

    public void pursuitOff() {
        if (on(Behavior.PURSUIT)) {
            m_iFlags ^= Behavior.PURSUIT.flag();
        }
    }

    public void separationOff() {
        if (on(Behavior.SEPARATION)) {
            m_iFlags ^= Behavior.SEPARATION.flag();
        }
    }

    public void interposeOff() {
        if (on(Behavior.INTERPOSE)) {
            m_iFlags ^= Behavior.INTERPOSE.flag();
        }
    }

    public boolean isSeek() {
        return on(Behavior.SEEK);
    }

    public boolean isArrive() {
        return on(Behavior.ARRIVE);
    }

    public boolean isPursuit() {
        return on(Behavior.PURSUIT);
    }

    public boolean isSeparation() {
        return on(Behavior.SEPARATION);
    }

    public boolean isInterpose() {
        return on(Behavior.INTERPOSE);
    }
}