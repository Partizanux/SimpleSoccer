package simpleSoccer.agents;

import simpleSoccer.goalKeeperStates.GlobalKeeperState;
import common.messaging.Telegram;
import common.misc.AutoList;
import common.D2.Vector2D;
import static common.D2.Vector2D.*;
import static common.D2.Transformation.WorldTransform;
import common.fsm.State;
import common.fsm.StateMachine;
import simpleSoccer.goalKeeperStates.TendGoal;

import static common.misc.Cgdi.gdi;
import static common.misc.Stream_Utility_function.ttos;
import static common.game.EntityFunctionTemplates.EnforceNonPenetrationContraint;
import static simpleSoccer.ParamLoader.Prm;

public class GoalKeeper extends PlayerBase {
    //TODO move up
    private StateMachine<GoalKeeper> stateMachine;
    //this vector is updated to point towards the ball and is used when
    //rendering the goalkeeper (instead of the underlying vehicle's heading)
    //to ensure he always appears to be watching the ball
    private Vector2D lookAt = new Vector2D();

    public GoalKeeper(SoccerTeam home_team, int home_region, State<GoalKeeper> start_state, Vector2D heading, Vector2D velocity,
                      double mass, double max_force, double max_speed, double max_turn_rate, double scale) {
        super(home_team, home_region, heading, velocity, mass, max_force, max_speed, max_turn_rate, scale, Role.GOAL_KEEPER);

        stateMachine = new StateMachine<>(this);
        stateMachine.SetCurrentState(start_state);
        stateMachine.SetPreviousState(start_state);
        stateMachine.SetGlobalState(GlobalKeeperState.INSTANCE);
        stateMachine.currentState().enter(this);
    }

    public GoalKeeper(SoccerTeam home_team, int home_region) {
        this(home_team, home_region, TendGoal.INSTANCE,
                new Vector2D(0, 1), new Vector2D(0.0, 0.0),
                Prm.PlayerMass, Prm.PlayerMaxForce, Prm.PlayerMaxSpeedWithoutBall, Prm.PlayerMaxTurnRate, Prm.PlayerScale);
    }

    public void update() {
        //run the logic for the current state
        stateMachine.Update();

        //calculate the combined force from each steering behavior 
        Vector2D SteeringForce = steeringBehaviors.calculate();

        //Acceleration = force/mass
        Vector2D Acceleration = div(SteeringForce, mass);
        //update velocity
        velocity.add(Acceleration);

        //make sure player does not exceed maximum velocity
        velocity.Truncate(maxSpeed);

        //update the position
        pos.add(velocity);


        //enforce a non-penetration constraint if desired
        if (Prm.bNonPenetrationConstraint) {
            EnforceNonPenetrationContraint(this, new AutoList<PlayerBase>().GetAllMembers());
        }

        //update the heading if the player has a non zero velocity
        if (!velocity.isZero()) {
            heading = Vec2DNormalize(velocity);
            side = heading.Perp();
        }

        //look-at vector always points toward the ball
        if (!pitch().goalKeeperHasBall()) {
            lookAt = Vec2DNormalize(sub(ball().pos(), pos()));
        }
    }

    @Override
    public void render() {
        if (team().color() == SoccerTeam.blue) {
            gdi.BluePen();
        } else {
            gdi.RedPen();
        }

        playerVertTrans = WorldTransform(playerVertices,
                pos(),
                lookAt,
                lookAt.Perp(),
                scale());

        gdi.ClosedShape(playerVertTrans);

        //draw the head
        gdi.BrownBrush();
        gdi.Circle(pos(), 6);

        //draw the getId
        if (Prm.bIDs) {
            gdi.TextColor(0, 170, 0);;
            gdi.TextAtPos(pos().x - 20, pos().y - 25, ttos(getId()));
        }

        //draw the state
        if (Prm.bStates) {
            gdi.TextColor(0, 170, 0);
            gdi.TransparentText();
            gdi.TextAtPos(pos.x, pos.y - 25, stateMachine.getNameOfCurrentState());
        }
    }

    /**
     * routes any messages appropriately
     */
    @Override
    public boolean handleMessage(final Telegram msg) {
        return stateMachine.handleMessage(msg);
    }

    /**
     * @return true if the ball can be grabbed by the goalkeeper
     */
    public boolean ballWithinKeeperRange() {
        return (Vec2DDistanceSq(pos(), ball().pos()) < Prm.KeeperInBallRangeSq);
    }

    /**
     * @return true if the ball comes close enough for the keeper to 
     *         consider intercepting
     */
    public boolean ballWithinRangeForIntercept() {
        return (Vec2DDistanceSq(team().homeGoal().center(), ball().pos())
                <= Prm.GoalKeeperInterceptRangeSq);
    }

    /**
     * @return true if the keeper has ventured too far away from the goalmouth
     */
    public boolean tooFarFromGoalMouth() {
        return (Vec2DDistanceSq(pos(), getRearInterposeTarget())
                > Prm.GoalKeeperInterceptRangeSq);
    }

    /**
     * this method is called by the Intercept state to determine the spot
     * along the goalmouth which will act as one of the INTERPOSE targets
     * (the other is the ball).
     * the specific point at the goal line that the keeper is trying to cover
     * is flexible and can move depending on where the ball is on the field.
     * To achieve this we just scale the ball's y value by the ratio of the
     * goal width to pitch width
     */
    public Vector2D getRearInterposeTarget() {
        double x = team().homeGoal().center().x;
        double y = pitch().playingArea().center().y - Prm.GoalWidth * 0.5
                + (ball().pos().y * Prm.GoalWidth) / pitch().playingArea().height();

        return new Vector2D(x, y);
    }

    public StateMachine<GoalKeeper> getFSM() {
        return stateMachine;
    }

    public Vector2D lookAt() {
        return new Vector2D(lookAt);
    }

    public void setLookAt(Vector2D v) {
        lookAt = new Vector2D(v);
    }
}