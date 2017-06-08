package simpleSoccer.goalKeeperStates;

import simpleSoccer.agents.GoalKeeper;
import static simpleSoccer.ParamLoader.Prm;
import common.fsm.State;
import common.messaging.Telegram;

public enum TendGoal implements State<GoalKeeper> {
    INSTANCE;

    public void enter(GoalKeeper keeper) {
        //turn INTERPOSE on
        keeper.steering().interposeOn(Prm.GoalKeeperTendingDistance);

        //INTERPOSE will position the agent between the ball position and a target
        //position situated along the goal mouth. This call sets the target
        keeper.steering().setTarget(keeper.getRearInterposeTarget());
    }

    @Override
    public void execute(GoalKeeper keeper) {
        //the rear INTERPOSE target will change as the ball's position changes
        //so it must be updated each update-step 
        keeper.steering().setTarget(keeper.getRearInterposeTarget());

        //if the ball comes in range the keeper traps it and then changes state
        //to put the ball back in play
        if (keeper.ballWithinKeeperRange()) {
            keeper.ball().trap();
            keeper.pitch().setGoalKeeperHasBall(true);
            keeper.getFSM().ChangeState(PutBallBackInPlay.INSTANCE);
            return;
        }

        //if ball is within a predefined distance, the keeper moves out from
        //position to try and intercept it.
        if (keeper.ballWithinRangeForIntercept() && !keeper.team().inControl()) {
            keeper.getFSM().ChangeState(InterceptBall.INSTANCE);
        }

        //if the keeper has ventured too far away from the goal-line and there
        //is no threat from the opponents he should move back towards it
        if (keeper.tooFarFromGoalMouth() && keeper.team().inControl()) {
            keeper.getFSM().ChangeState(ReturnHome.INSTANCE);
            return;
        }
    }

    @Override
    public void exit(GoalKeeper keeper) {
        keeper.steering().interposeOff();
    }

    public boolean onMessage(GoalKeeper e, final Telegram t) {
        return false;
    }
}
