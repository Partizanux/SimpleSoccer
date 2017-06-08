package simpleSoccer.agents;

import simpleSoccer.fieldPlayerStates.GlobalPlayerState;
import common.misc.Cgdi;
import common.messaging.Telegram;
import common.misc.AutoList;
import common.D2.Vector2D;
import static common.D2.Vector2D.*;
import static common.D2.Transformation.*;
import common.fsm.State;
import common.fsm.StateMachine;
import static common.game.EntityFunctionTemplates.EnforceNonPenetrationContraint;
import common.time.Regulator;
import simpleSoccer.fieldPlayerStates.Wait;

import static common.misc.Cgdi.gdi;
import static common.misc.Utils.clamp;
import static common.misc.Stream_Utility_function.ttos;
import static simpleSoccer.ParamLoader.Prm;

public class FieldPlayer extends PlayerBase {
    //TODO move up
    private StateMachine<FieldPlayer> stateMachine;
    //limits the number of kicks a player may take per second
    private Regulator kickLimiter;

    public FieldPlayer(SoccerTeam home_team, int home_region, State<FieldPlayer> start_state, Vector2D heading, Vector2D velocity,
                       double mass, double max_force, double max_speed, double max_turn_rate, double scale, Role role) {
        super(home_team, home_region, heading, velocity, mass, max_force, max_speed, max_turn_rate, scale, role);
        stateMachine = new StateMachine<>(this);

        if (start_state != null) {
            stateMachine.SetCurrentState(start_state);
            stateMachine.SetPreviousState(start_state);
            stateMachine.SetGlobalState(GlobalPlayerState.INSTANCE);

            stateMachine.currentState().enter(this);
        }

        steeringBehaviors.separationOn();
        //set up the kick regulator
        kickLimiter = new Regulator(Prm.PlayerKickFrequency);
    }

    public FieldPlayer(SoccerTeam home_team, int home_region, Role role) {
        this(home_team, home_region, Wait.INSTANCE, new Vector2D(0, 1), new Vector2D(0.0, 0.0),
                Prm.PlayerMass, Prm.PlayerMaxForce, Prm.PlayerMaxSpeedWithoutBall, Prm.PlayerMaxTurnRate, Prm.PlayerScale, role);
    }

    /**
     * Call this to update the player's position and orientation
     */
    public void update() {
        //run the logic for the current state
        stateMachine.Update();

        //calculate the combined steering force
        steeringBehaviors.calculate();

        //if no steering force is produced decelerate the player by applying a
        //braking force
        if (steeringBehaviors.force().isZero()) {
            final double BrakingRate = 0.8;

            velocity.mul(BrakingRate);
        }

        //the steering force's side component is a force that rotates the 
        //player about its axis. We must limit the rotation so that a player
        //can only turn by PlayerMaxTurnRate rads per update.
        double TurningForce = steeringBehaviors.sideComponent();

        TurningForce = clamp(TurningForce, -Prm.PlayerMaxTurnRate, Prm.PlayerMaxTurnRate);

        //rotate the heading vector
        Vec2DRotateAroundOrigin(heading, TurningForce);

        //make sure the velocity vector points in the same direction as
        //the heading vector
        velocity = mul(heading, velocity.Length());
        
        //and recreate side
        side = heading.Perp();

        //now to calculate the acceleration due to the force exerted by
        //the forward component of the steering force in the direction
        //of the player's heading
        Vector2D accel = mul(heading, steeringBehaviors.forwardComponent()/ mass);

        velocity.add(accel);

        //make sure player does not exceed maximum velocity
        velocity.Truncate(maxSpeed);
        //update the position
        pos.add(velocity);

        //enforce a non-penetration constraint if desired
        if (Prm.bNonPenetrationConstraint) {
            EnforceNonPenetrationContraint(this, new AutoList<PlayerBase>().GetAllMembers());
        }
    }

    @Override
    public void render() {
        gdi.TransparentText();
        gdi.TextColor(Cgdi.grey);

        //set appropriate team color
        if (team().color() == SoccerTeam.blue) {
            gdi.BluePen();
        } else {
            gdi.RedPen();
        }

        //render the player's body
        playerVertTrans = WorldTransform(playerVertices,
                pos(),
                heading(),
                side(),
                scale());
        gdi.ClosedShape(playerVertTrans);

        //and 'is 'ead
        gdi.BrownBrush();
        if (Prm.bHighlightIfThreatened && (team().controllingPlayer() == this) && isThreatened()) {
            gdi.YellowBrush();
        }
        gdi.Circle(pos(), 6);


        //render the state
        if (Prm.bStates) {
            gdi.TextColor(0, 170, 0);
            gdi.TextAtPos(pos.x, pos.y - 25,
                    new String(stateMachine.getNameOfCurrentState()));
        }

        //show IDs
        if (Prm.bIDs) {
            gdi.TextColor(0, 170, 0);
            gdi.TextAtPos(pos().x - 20, pos().y - 25, ttos(getId()));
        }


        if (Prm.bViewTargets) {
            gdi.RedBrush();
            gdi.Circle(steering().target(), 3);
            gdi.TextAtPos(steering().target(), ttos(getId()));
        }
    }

    /**
     * routes any messages appropriately
     */
    @Override
    public boolean handleMessage(final Telegram msg) {
        return stateMachine.handleMessage(msg);
    }

    //TODO remove ?
    public StateMachine<FieldPlayer> getFSM() {
        return stateMachine;
    }

    public boolean isReadyForNextKick() {
        return kickLimiter.isReady();
    }

    /**
     * @return true if the ball is within kicking range
     */
    public boolean ballWithinKickingRange() {
        return (Vec2DDistanceSq(ball().pos(), pos()) < Prm.PlayerKickingDistanceSq);
    }
}
