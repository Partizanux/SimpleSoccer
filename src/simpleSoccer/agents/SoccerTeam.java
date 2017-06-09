package simpleSoccer.agents;

import simpleSoccer.fieldPlayerStates.ReturnToHomeRegion;
import simpleSoccer.fieldPlayerStates.Wait;
import simpleSoccer.teamStates.PrepareForKickOff;
import simpleSoccer.teamStates.Attacking;
import simpleSoccer.teamStates.Defending;

import static common.debug.DbgConsole.*;
import common.D2.Vector2D;
import static common.D2.Vector2D.*;
import static common.D2.Transformation.*;
import static common.D2.geometry.*;
import common.fsm.StateMachine;
import static common.game.EntityManager.EntityMgr;
import static common.messaging.MessageDispatcher.*;
import common.misc.Cgdi;
import static common.misc.Cgdi.gdi;
import static common.misc.CppToJava.ObjectRef;
import static common.misc.Utils.*;
import static common.misc.Stream_Utility_function.ttos;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import static simpleSoccer.DEFINE.*;
import static simpleSoccer.ParamLoader.Prm;
import static simpleSoccer.MessageTypes.*;

/**
 * Class that is responsible for each player AI logic related to a team behavior
 */
public class SoccerTeam {
    public enum Color {
        BLUE, RED
    }
    public static Color blue = Color.BLUE;
    public static Color red = Color.RED;

    private StateMachine<SoccerTeam> stateMachine;
    private Color color;
    private List<PlayerBase> players = new ArrayList<PlayerBase>(5);
    private SoccerPitch pitch;
    private Goal opponentsGoal;
    private Goal homeGoal;
    private SoccerTeam opponents;
    private PlayerBase controllingPlayer;
    private PlayerBase supportingPlayer;
    private PlayerBase receivingPlayer;
    private PlayerBase playerClosestToBall;

    /**
     * the squared distance the closest player is from the ball
     */
    private double distSqToBallOfClosestPlayer;

    /**
     * players use this to determine strategic positions on the playing field
     */
    private SupportSpotCalculator supportSpotCalculator;

    public SoccerTeam(Goal home_goal, Goal opponents_goal, SoccerPitch pitch, Color color) {
        opponentsGoal = opponents_goal;
        homeGoal = home_goal;
        opponents = null;
        this.pitch = pitch;
        this.color = color;
        distSqToBallOfClosestPlayer = 0.0;
        supportingPlayer = null;
        receivingPlayer = null;
        controllingPlayer = null;
        playerClosestToBall = null;

        //setup the state machine
        stateMachine = new StateMachine<>(this);

        stateMachine.setCurrentState(Defending.INSTANCE);
        stateMachine.setPreviousState(Defending.INSTANCE);
        stateMachine.setGlobalState(null);

        //create the players and goalkeeper
        createPlayers();

        for (PlayerBase player : players) {
            player.steering().separationOn();
        }

        //create the support spot calculator
        supportSpotCalculator = new SupportSpotCalculator(Prm.NumSupportSpotsX, Prm.NumSupportSpotsY, this);
    }

    /**
     * creates all the players for this team
     */
    private void createPlayers() {
        if (color() == blue) {
            players.add(new GoalKeeper(this, 1));
            players.add(new FieldPlayer(this, 6, PlayerBase.Role.ATTACKER));
            players.add(new FieldPlayer(this, 8, PlayerBase.Role.ATTACKER));
            players.add(new FieldPlayer(this, 3, PlayerBase.Role.DEFENDER));
            players.add(new FieldPlayer(this, 5, PlayerBase.Role.DEFENDER));

        } else {
            players.add(new GoalKeeper(this, 16));
            players.add(new FieldPlayer(this, 9, PlayerBase.Role.ATTACKER));
            players.add(new FieldPlayer(this, 11, PlayerBase.Role.ATTACKER));
            players.add(new FieldPlayer(this, 12, PlayerBase.Role.DEFENDER));
            players.add(new FieldPlayer(this, 14, PlayerBase.Role.DEFENDER));

        }

        for (PlayerBase player : players) {
            EntityMgr.RegisterEntity(player);
        }
    }

    /**
     * Called each frame. Sets m_pClosestPlayerToBall to point to the player
     * closest to the ball. 
     */
    private void calculateClosestPlayerToBall() {
        double ClosestSoFar = Float.MAX_VALUE;

        ListIterator<PlayerBase> it = players.listIterator();

        while (it.hasNext()) {
            PlayerBase cur = it.next();
            //calculate the dist. Use the squared value to avoid sqrt
            double dist = Vec2DDistanceSq(cur.pos(), pitch().ball().pos());

            //keep a record of this value for each player
            cur.setDistToBallSq(dist);

            if (dist < ClosestSoFar) {
                ClosestSoFar = dist;

                playerClosestToBall = cur;
            }
        }

        distSqToBallOfClosestPlayer = ClosestSoFar;
    }

    /**
     *  Renders the players and any team related info
     */
    public void render() {
        for (PlayerBase player : players) {
            player.render();
        }

        //show the controlling team and player at the top of the display
        if (Prm.bShowControllingTeam) {
            gdi.TextColor(Cgdi.white);

            if ((color() == blue) && inControl()) {
                gdi.TextAtPos(20, 3, "Blue in Control");
            } else if ((color() == red) && inControl()) {
                gdi.TextAtPos(20, 3, "Red in Control");
            }
            if (controllingPlayer != null) {
                gdi.TextAtPos(pitch().cx() - 150, 3,
                        "Controlling Player: " + ttos(controllingPlayer.getId()));
            }
        }

        //render the sweet spots
        if (Prm.bSupportSpots && inControl()) {
            supportSpotCalculator.Render();
        }

//define(SHOW_TEAM_STATE);
        if (def(SHOW_TEAM_STATE)) {
            if (color() == red) {
                gdi.TextColor(Cgdi.white);

                if (stateMachine.currentState() == Attacking.INSTANCE) {
                    gdi.TextAtPos(160, 20, "Attacking");
                }
                if (stateMachine.currentState() == Defending.INSTANCE) {
                    gdi.TextAtPos(160, 20, "Defending");
                }
                if (stateMachine.currentState() == PrepareForKickOff.INSTANCE) {
                    gdi.TextAtPos(160, 20, "Kickoff");
                }
            } else {
                if (stateMachine.currentState() == Attacking.INSTANCE) {
                    gdi.TextAtPos(160, pitch().cy() - 40, "Attacking");
                }
                if (stateMachine.currentState() == Defending.INSTANCE) {
                    gdi.TextAtPos(160, pitch().cy() - 40, "Defending");
                }
                if (stateMachine.currentState() == PrepareForKickOff.INSTANCE) {
                    gdi.TextAtPos(160, pitch().cy() - 40, "Kickoff");
                }
            }
        }

// define(SHOW_SUPPORTING_PLAYERS_TARGET)
        if (def(SHOW_SUPPORTING_PLAYERS_TARGET)) {
            if (supportingPlayer != null) {
                gdi.BlueBrush();
                gdi.RedPen();
                gdi.Circle(supportingPlayer.steering().target(), 4);
            }
        }

    }

    /**
     *  iterates through each player's update function and calculates 
     *  frequently accessed info
     */
    public void update() {
        //this information is used frequently so it's more efficient to 
        //calculate it just once each frame
        calculateClosestPlayerToBall();

        //the team state machine switches between attack/defense behavior. It
        //also handles the 'kick off' state where a team must return to their
        //kick off positions before the whistle is blown
        stateMachine.update();

        //now update each player
        ListIterator<PlayerBase> it = players.listIterator();

        while (it.hasNext()) {
            it.next().update();
        }

    }

    /**
     * Calling this changes the state of all field players to that of
     * ReturnToHomeRegion. Mainly used when a goal keeper has
     * possession
     */
    public void returnAllFieldPlayersToHome() {
        ListIterator<PlayerBase> it = players.listIterator();

        while (it.hasNext()) {
            PlayerBase cur = it.next();
            if (cur.role() != PlayerBase.Role.GOAL_KEEPER) {
                Dispatcher.DispatchMsg(SEND_MSG_IMMEDIATELY,
                        1,
                        cur.getId(),
                        Msg_GoHome,
                        null);
            }
        }
    }

    /**
     * Given a ball position, a kicking power and a reference to a vector2D
     * this function will sample random positions along the opponent's goal-
     * mouth and check to see if a goal can be scored if the ball was to be
     * kicked in that direction with the given power. If a possible shot is
     * found, the function will immediately return true, with the target
     * position stored in the vector ShotTarget.
     * Returns true if player has a clean shot at the goal and sets ShotTarget
     * to a normalized vector pointing in the direction the shot should be
     * made. Else returns false and sets heading to a zero vector
     */
    public boolean canShoot(Vector2D ballPos, double power) {
        return canShoot(ballPos, power, new Vector2D());
    }

    public boolean canShoot(Vector2D ballPos, double power, Vector2D shotTarget) {
        //the number of randomly created shot targets this method will test 
        int NumAttempts = Prm.NumAttemptsToFindValidStrike;

        while (NumAttempts-- > 0) {
            //choose a random position along the opponent's goal mouth. (making
            //sure the ball's radius is taken into account)
            shotTarget.set(opponentsGoal().center());

            //the y value of the shot position should lay somewhere between two
            //goalposts (taking into consideration the ball diameter)
            int MinYVal = (int) (opponentsGoal().leftPost().y + pitch().ball().getBRadius());
            int MaxYVal = (int) (opponentsGoal().rightPost().y - pitch().ball().getBRadius());

            shotTarget.y = (double) randInt(MinYVal, MaxYVal);

            //make sure striking the ball with the given power is enough to drive
            //the ball over the goal line.
            double time = pitch().ball().timeToCoverDistance(ballPos,
                    shotTarget,
                    power);

            //if it is, this shot is then tested to see if any of the opponents
            //can intercept it.
            if (time >= 0) {
                if (isPassSafeFromAllOpponents(ballPos, shotTarget, null, power)) {
                    return true;
                }
            }
        }

        return false;
    }

    //TODO refactor, remove ObjectRef, method must return receiver or null
    /**
     * The best pass is considered to be the pass that cannot be intercepted 
     * by an opponent and that is as far forward of the receiver as possible  
     * If a pass is found, the receiver's address is returned in the 
     * reference, 'receiver' and the position the pass will be made to is 
     * returned in the  reference 'PassTarget'
     */
    public boolean findPass(final PlayerBase passer, ObjectRef<PlayerBase> receiver,
                            Vector2D PassTarget, double power, double MinPassingDistance) {
        assert (receiver != null);
        assert (PassTarget != null);
        ListIterator<PlayerBase> it = members().listIterator();

        double ClosestToGoalSoFar = Float.MAX_VALUE;
        Vector2D Target = new Vector2D();

        boolean finded = false;
        //iterate through all this player's team members and calculate which
        //one is in a position to be passed the ball 
        while (it.hasNext()) {
            PlayerBase curPlyr = it.next();
            //make sure the potential receiver being examined is not this player
            //and that it is further away than the minimum pass distance
            if ((curPlyr != passer)
                    && (Vec2DDistanceSq(passer.pos(), curPlyr.pos())
                    > MinPassingDistance * MinPassingDistance)) {
                if (getBestPassToReceiver(passer, curPlyr, Target, power)) {
                    //if the pass target is the closest to the opponent's goal line found
                    // so far, keep a record of it
                    double Dist2Goal = abs(Target.x - opponentsGoal().center().x);

                    if (Dist2Goal < ClosestToGoalSoFar) {
                        ClosestToGoalSoFar = Dist2Goal;

                        //keep a record of this player
                        receiver.set(curPlyr);

                        //and the target
                        PassTarget.set(Target);

                        finded = true;
                    }
                }
            }
        }//next team member

        return finded;
    }

    /**
     *  Three potential passes are calculated. One directly toward the receiver's
     *  current position and two that are the tangents from the ball position
     *  to the circle of radius 'range' from the receiver.
     *  These passes are then tested to see if they can be intercepted by an
     *  opponent and to make sure they terminate within the playing area. If
     *  all the passes are invalidated the function returns false. Otherwise
     *  the function returns the pass that takes the ball closest to the 
     *  opponent's goal area.
     */
    public boolean getBestPassToReceiver(final PlayerBase passer, final PlayerBase receiver,
                                         Vector2D PassTarget, double power) {
        assert (PassTarget != null);
        //first, calculate how much time it will take for the ball to reach 
        //this receiver, if the receiver was to remain motionless 
        double time = pitch().ball().timeToCoverDistance(pitch().ball().pos(),
                receiver.pos(),
                power);

        //return false if ball cannot reach the receiver after having been
        //kicked with the given power
        if (time < 0) {
            return false;
        }

        //the maximum distance the receiver can cover in this time
        double InterceptRange = time * receiver.maxSpeed();

        //scale the intercept range
        final double ScalingFactor = 0.3;
        InterceptRange *= ScalingFactor;

        //now calculate the pass targets which are positioned at the intercepts
        //of the tangents from the ball to the receiver's range circle.
        Vector2D ip1 = new Vector2D(), ip2 = new Vector2D();

        GetTangentPoints(receiver.pos(),
                InterceptRange,
                pitch().ball().pos(),
                ip1,
                ip2);

        Vector2D Passes[] = {ip1, receiver.pos(), ip2};
        final int NumPassesToTry = Passes.length;

        // this pass is the best found so far if it is:
        //
        //  1. Further upfield than the closest valid pass for this receiver
        //     found so far
        //  2. Within the playing area
        //  3. Cannot be intercepted by any opponents

        double ClosestSoFar = Float.MAX_VALUE;
        boolean bResult = false;

        for (int pass = 0; pass < NumPassesToTry; ++pass) {
            double dist = abs(Passes[pass].x - opponentsGoal().center().x);

            if ((dist < ClosestSoFar)
                    && pitch().playingArea().inside(Passes[pass])
                    && isPassSafeFromAllOpponents(pitch().ball().pos(),
                    Passes[pass],
                    receiver,
                    power)) {
                ClosestSoFar = dist;
                PassTarget.set(Passes[pass]);
                bResult = true;
            }
        }

        return bResult;
    }

    /**
     * test if a pass from positions 'from' to 'target' kicked with force 
     * 'PassingForce'can be intercepted by an opposing player
     */
    public boolean isPassSafeFromOpponent(Vector2D from, Vector2D target,
                                          final PlayerBase receiver, final PlayerBase opp, double PassingForce) {
        //move the opponent into local space.
        Vector2D ToTarget = sub(target, from);
        Vector2D ToTargetNormalized = Vec2DNormalize(ToTarget);

        Vector2D LocalPosOpp = PointToLocalSpace(opp.pos(),
                ToTargetNormalized,
                ToTargetNormalized.Perp(),
                from);

        //if opponent is behind the kicker then pass is considered okay(this is 
        //based on the assumption that the ball is going to be kicked with a 
        //velocity greater than the opponent's max velocity)
        if (LocalPosOpp.x < 0) {
            return true;
        }

        //if the opponent is further away than the target we need to consider if
        //the opponent can reach the position before the receiver.
        if (Vec2DDistanceSq(from, target) < Vec2DDistanceSq(opp.pos(), from)) {
            if (receiver != null) {
                if (Vec2DDistanceSq(target, opp.pos())
                        > Vec2DDistanceSq(target, receiver.pos())) {
                    return true;
                } else {
                    return false;
                }

            } else {
                return true;
            }
        }

        //calculate how long it takes the ball to cover the distance to the 
        //position orthogonal to the opponents position
        double TimeForBall =
                pitch().ball().timeToCoverDistance(new Vector2D(0, 0),
                new Vector2D(LocalPosOpp.x, 0),
                PassingForce);

        //now calculate how far the opponent can run in this time
        double reach = opp.maxSpeed() * TimeForBall
                + pitch().ball().getBRadius()
                + opp.getBRadius();

        //if the distance to the opponent's y position is less than his running
        //range plus the radius of the ball and the opponents radius then the
        //ball can be intercepted
        if (abs(LocalPosOpp.y) < reach) {
            return false;
        }

        return true;
    }

    /**
     * tests a pass from position 'from' to position 'target' against each member
     * of the opposing team. Returns true if the pass can be made without
     * getting intercepted
     */
    public boolean isPassSafeFromAllOpponents(Vector2D from, Vector2D target,
                                              final PlayerBase receiver, double PassingForce) {
        ListIterator<PlayerBase> opp = opponents().members().listIterator();

        while (opp.hasNext()) {
            if (!isPassSafeFromOpponent(from, target, receiver, opp.next(), PassingForce)) {
                debug_on();

                return false;
            }
        }

        return true;
    }

    /**
     * returns true if an opposing player is within the radius of the position
     * given as a par ameter
     */
    public boolean isOpponentWithinRadius(Vector2D pos, double rad) {
        ListIterator<PlayerBase> it = opponents().members().listIterator();

        while (it.hasNext()) {
            if (Vec2DDistanceSq(pos, it.next().pos()) < rad * rad) {
                return true;
            }
        }

        return false;
    }

    /**
     * this tests to see if a pass is possible between the requester and
     * the controlling player. If it is possible a message is sent to the
     * controlling player to pass the ball asap.
     */
    public void requestPass(FieldPlayer requester) {
        //maybe put a restriction here
        if (randFloat() > 0.1) {
            return;
        }

        if (isPassSafeFromAllOpponents(controllingPlayer().pos(), requester.pos(), requester, Prm.MaxPassingForce)) {
            //tell the player to make the pass
            //let the receiver know a pass is coming 
            Dispatcher.DispatchMsg(SEND_MSG_IMMEDIATELY, requester.getId(), controllingPlayer().getId(), Msg_PassToMe, requester);
        }
    }

    /**
     * calculate the closest player to the SupportSpot
     */
    public PlayerBase determineBestSupportingAttacker() {
        double ClosestSoFar = Float.MAX_VALUE;

        PlayerBase BestPlayer = null;

        ListIterator<PlayerBase> it = players.listIterator();

        while (it.hasNext()) {
            PlayerBase cur = it.next();
            //only attackers utilize the BestSupportingSpot
            if ((cur.role() == PlayerBase.Role.ATTACKER) && (cur != controllingPlayer)) {
                //calculate the dist. Use the squared value to avoid sqrt
                double dist = Vec2DDistanceSq(cur.pos(), supportSpotCalculator.GetBestSupportingSpot());

                //if the distance is the closest so far and the player is not a
                //goalkeeper and the player is not the one currently controlling
                //the ball, keep a record of this player
                if ((dist < ClosestSoFar)) {
                    ClosestSoFar = dist;
                    BestPlayer = cur;
                }
            }
        }

        return BestPlayer;
    }

    public List<PlayerBase> members() {
        return players;
    }

    public StateMachine<SoccerTeam> getFSM() {
        return stateMachine;
    }

    public Goal homeGoal() {
        return homeGoal;
    }

    public Goal opponentsGoal() {
        return opponentsGoal;
    }

    public SoccerPitch pitch() {
        return pitch;
    }

    public SoccerTeam opponents() {
        return opponents;
    }

    public void setOpponents(SoccerTeam opps) {
        opponents = opps;
    }

    public Color color() {
        return color;
    }

    public void setPlayerClosestToBall(PlayerBase plyr) {
        playerClosestToBall = plyr;
    }

    public PlayerBase playerClosestToBall() {
        return playerClosestToBall;
    }

    public double closestDistToBallSq() {
        return distSqToBallOfClosestPlayer;
    }

    public Vector2D getSupportSpot() {
        return new Vector2D(supportSpotCalculator.GetBestSupportingSpot());
    }

    public PlayerBase supportingPlayer() {
        return supportingPlayer;
    }

    public void setSupportingPlayer(PlayerBase player) {
        supportingPlayer = player;
    }

    public PlayerBase receiver() {
        return receivingPlayer;
    }

    public void setReceiver(PlayerBase player) {
        receivingPlayer = player;
    }

    public PlayerBase controllingPlayer() {
        return controllingPlayer;
    }

    public void setControllingPlayer(PlayerBase player) {
        controllingPlayer = player;

        //rub it in the opponents faces!
        opponents().lostControl();
    }

    public boolean inControl() {
        if (controllingPlayer != null) {
            return true;
        } else {
            return false;
        }
    }

    public void lostControl() {
        controllingPlayer = null;
    }

    public PlayerBase getPlayerByID(int id) {
        ListIterator<PlayerBase> it = players.listIterator();

        while (it.hasNext()) {
            PlayerBase cur = it.next();
            if (cur.getId() == id) {
                return cur;
            }
        }

        return null;
    }

    public void setPlayerHomeRegion(int plyr, int region) {
        assert ((plyr >= 0) && (plyr < (int) players.size()));

        players.get(plyr).setHomeRegion(region);
    }

    public void determineBestSupportingPosition() {
        supportSpotCalculator.DetermineBestSupportingPosition();
    }

    public void updateTargetsOfWaitingPlayers() {
        ListIterator<PlayerBase> it = players.listIterator();

        while (it.hasNext()) {
            PlayerBase cur = it.next();
            if (cur.role() != PlayerBase.Role.GOAL_KEEPER) {
                //cast to a field player
                FieldPlayer plyr = (FieldPlayer) cur;

                if (plyr.getFSM().inState(Wait.INSTANCE)
                        || plyr.getFSM().inState(ReturnToHomeRegion.INSTANCE)) {
                    plyr.steering().setTarget(plyr.homeRegion().center());
                }
            }
        }
    }

    /**
     * @return false if any of the team are not located within their home region
     */
    public boolean allPlayersAtHome() {
        ListIterator<PlayerBase> it = players.listIterator();

        while (it.hasNext()) {
            if (it.next().inHomeRegion() == false) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return Name of the team ("Red" or "Blue")
     */
    public String Name() {
        if (color == blue) {
            return "Blue";
        }
        return "Red";
    }
}
