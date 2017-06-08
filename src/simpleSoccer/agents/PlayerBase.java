package simpleSoccer.agents;

import java.util.ListIterator;
import common.D2.Vector2D;
import common.misc.AutoList;
import java.util.LinkedList;
import java.util.List;
import static simpleSoccer.ParamLoader.Prm;
import static simpleSoccer.MessageTypes.*;
import static common.D2.Vector2D.*;
import common.game.Region;
import static common.messaging.MessageDispatcher.*;
import static java.lang.Math.abs;

abstract public class PlayerBase extends MovingEntity implements AutoList.Interface {
    public enum Role {
        GOAL_KEEPER, ATTACKER, DEFENDER
    }
    //this player's role in the team
    protected Role role;
    //a pointer to this player's team
    protected SoccerTeam team;
    //the steering behaviors
    protected SteeringBehaviors steeringBehaviors;
    //the region that this player is assigned to.
    protected int homeRegion;
    //the region this player moves to before kickoff
    protected int defaultRegion;
    //the distance to the ball (in squared-space). This value is queried 
    //a lot so it's calculated once each time-step and stored here.
    protected double DistToBallSq;
    //the vertex buffer
    protected List<Vector2D> playerVertices = new LinkedList<Vector2D>();
    //the buffer for the transformed vertices
    protected List<Vector2D> playerVertTrans = new LinkedList<Vector2D>();

    public PlayerBase(SoccerTeam home_team, int home_region, Vector2D heading, Vector2D velocity,
                      double mass, double max_force, double max_speed, double max_turn_rate, double scale,
                      Role role) {
        super(home_team.pitch().getRegionFromIndex(home_region).center(), scale * 10.0, velocity, max_speed,
                heading, mass, new Vector2D(scale, scale), max_turn_rate, max_force);

        team = home_team;
        DistToBallSq = Float.MAX_VALUE;
        homeRegion = home_region;
        defaultRegion = home_region;
        this.role = role;

        //setup the vertex buffers and calculate the bounding radius
        final Vector2D player[] = {
            new Vector2D(-3, 8),
            new Vector2D(3, 10),
            new Vector2D(3, -10),
            new Vector2D(-3, -8)
        };

        for (int vtx = 0; vtx < player.length; ++vtx) {
            playerVertices.add(player[vtx]);

            //set the bounding radius to the length of the 
            //greatest extent
            if (abs(player[vtx].x) > boundingRadius) {
                boundingRadius = abs(player[vtx].x);
            }
            if (abs(player[vtx].y) > boundingRadius) {
                boundingRadius = abs(player[vtx].y);
            }
        }

        //set up the steering behavior class
        steeringBehaviors = new SteeringBehaviors(this, team.pitch(), ball());

        //a player's start target is its start position (because it's just waiting)
        steeringBehaviors.setTarget(home_team.pitch().getRegionFromIndex(home_region).center());
        new AutoList<PlayerBase>().add(this);
    }

    /**
     *  returns true if there is an opponent within this player's 
     *  comfort zone
     */
    public boolean isThreatened() {
        //check against all opponents to make sure non are within this
        //player's comfort zone
        ListIterator<PlayerBase> it;
        it = team().opponents().members().listIterator();

        while (it.hasNext()) {
            PlayerBase curOpp = it.next();
            //calculate distance to the player. if dist is less than our
            //comfort zone, and the opponent is in front of the player, return true
            if (positionInFrontOfPlayer(curOpp.pos())
                    && Vec2DDistanceSq(pos(), curOpp.pos()) < Prm.PlayerComfortZoneSq) {
                return true;
            }

        }

        return false;
    }

    /**
     *  rotates the player to face the ball
     */
    public void trackBall() {
        rotateHeadingToFacePosition(ball().pos());
    }

    /**
     * sets the player's heading to point at the current target
     */
    public void trackTarget() {
        setHeading(Vec2DNormalize(sub(steering().target(), pos())));
    }

    // TODO change to msg pass_was_made, kick_was_made, assist or etc and send it to team (decouple logic) ?
    /**
     * determines the player who is closest to the SupportSpot and messages him
     * to tell him to change state to SupportAttacker
     */
    public void FindSupport() {
        //if there is no support we need to find a suitable player.
        if (team().supportingPlayer() == null) {
            PlayerBase BestSupportPly = team().determineBestSupportingAttacker();
            team().setSupportingPlayer(BestSupportPly);
            Dispatcher.DispatchMsg(SEND_MSG_IMMEDIATELY,
                    getId(),
                    team().supportingPlayer().getId(),
                    Msg_SupportAttacker,
                    null);
        }

        PlayerBase BestSupportPly = team().determineBestSupportingAttacker();

        //if the best player available to support the ATTACKER changes, update
        //the pointers and send messages to the relevant players to update their
        //states
        if (BestSupportPly != null && (BestSupportPly != team().supportingPlayer())) {

            if (team().supportingPlayer() != null) {
                Dispatcher.DispatchMsg(SEND_MSG_IMMEDIATELY,
                        getId(),
                        team().supportingPlayer().getId(),
                        Msg_GoHome,
                        null);
            }

            team().setSupportingPlayer(BestSupportPly);

            Dispatcher.DispatchMsg(SEND_MSG_IMMEDIATELY,
                    getId(),
                    team().supportingPlayer().getId(),
                    Msg_SupportAttacker,
                    null);
        }
    }

    /** 
     * @return true if a ball comes within range of a receiver
     */
    public boolean ballWithinReceivingRange() {
        return (Vec2DDistanceSq(pos(), ball().pos()) < Prm.BallWithinReceivingRangeSq);
    }

    /**
     * @return true if the player is located within the boundaries 
     *        of his home region
     */
    public boolean inHomeRegion() {
        if (role == Role.GOAL_KEEPER) {
            return pitch().getRegionFromIndex(homeRegion).inside(pos(), Region.RegionModifier.NORMAL);
        } else {
            return pitch().getRegionFromIndex(homeRegion).inside(pos(), Region.RegionModifier.HALFSIZE);
        }
    }

    /**
     * 
     * @return true if this player is ahead of the ATTACKER
     */
    public boolean isAheadOfAttacker() {
        return abs(pos().x - team().opponentsGoal().center().x)
                < abs(team().controllingPlayer().pos().x - team().opponentsGoal().center().x);
    }

    /**
     * @return true if the player is located at his steering target
     */
    public boolean atTarget() {
        return (Vec2DDistanceSq(pos(), steering().target()) < Prm.PlayerInTargetRangeSq);
    }

    /**
     * @return true if the player is the closest player in his team to the ball
     */
    public boolean isClosestTeamMemberToBall() {
        return team().playerClosestToBall() == this;
    }

    /**
     * @param position
     * @return true if the point specified by 'position' is located in
     * front of the player
     */
    public boolean positionInFrontOfPlayer(Vector2D position) {
        Vector2D ToSubject = sub(position, pos());

        if (ToSubject.Dot(heading()) > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if the player is the closest player on the pitch to the ball
     */
    public boolean isClosestPlayerOnPitchToBall() {
        return isClosestTeamMemberToBall()
                && (distToBallSq() < team().opponents().closestDistToBallSq());
    }

    /** 
     * @return true if this player is the controlling player
     */
    public boolean isControllingPlayer() {
        return team().controllingPlayer() == this;
    }

    /** 
     * @return true if the player is located in the designated 'hot region' --
     * the area close to the opponent's goal 
     */
    public boolean inHotRegion() {
        return abs(pos().x - team().opponentsGoal().center().x)
                < pitch().playingArea().length() / 3.0;
    }

    Role role() {
        return role;
    }

    public double distToBallSq() {
        return DistToBallSq;
    }

    public void setDistToBallSq(double val) {
        DistToBallSq = val;
    }

    /**
     *  calculate distance to opponent's/home goal. Used frequently by the passing methods
     */
    public double distToOppGoal() {
        return abs(pos().x - team().opponentsGoal().center().x);
    }

    public double distToHomeGoal() {
        return abs(pos().x - team().homeGoal().center().x);
    }

    public void setDefaultHomeRegion() {
        homeRegion = defaultRegion;
    }

    public SoccerBall ball() {
        return team().pitch().ball();
    }

    public SoccerPitch pitch() {
        return team().pitch();
    }

    public SteeringBehaviors steering() {
        return steeringBehaviors;
    }

    public Region homeRegion() {
        return pitch().getRegionFromIndex(homeRegion);
    }

    public void setHomeRegion(int NewRegion) {
        homeRegion = NewRegion;
    }

    public SoccerTeam team() {
        return team;
    }

    /**
     * binary predicates for std::sort (see CanPassForward/Backward)
     */
    static public boolean sortByDistanceToOpponentsGoal(PlayerBase p1, PlayerBase p2) {
        return (p1.distToOppGoal() < p2.distToOppGoal());
    }

    static public boolean sortByReversedDistanceToOpponentsGoal(PlayerBase p1, PlayerBase p2) {
        return (p1.distToOppGoal() > p2.distToOppGoal());
    }
}
