package simpleSoccer.goalKeeperStates;

import simpleSoccer.agents.GoalKeeper;
import static simpleSoccer.DEFINE.*;
import static common.debug.DbgConsole.*;
import common.fsm.State;
import common.messaging.Telegram;

public enum InterceptBall implements State<GoalKeeper> {
    INSTANCE;

    @Override
    public void enter(GoalKeeper keeper) {
        keeper.steering().pursuitOn();
        if (def(GOALY_STATE_INFO_ON)) {
            debug_con.print("Goaly ").print(keeper.getId()).print(" enters InterceptBall").print("");
        }
    }

    @Override
    public void execute(GoalKeeper keeper) {
        //if the goalkeeper moves to far away from the goal he should return to his
        //home region UNLESS he is the closest player to the ball, in which case,
        //he should keep trying to intercept it.
        if (keeper.TooFarFromGoalMouth() && !keeper.isClosestPlayerOnPitchToBall()) {
            keeper.GetFSM().ChangeState(ReturnHome.INSTANCE);
            return;
        }

        //if the ball becomes in range of the goalkeeper's hands he traps the 
        //ball and puts it back in play
        if (keeper.ballWithinKeeperRange()) {
            keeper.ball().trap();
            keeper.pitch().setGoalKeeperHasBall(true);
            keeper.GetFSM().ChangeState(PutBallBackInPlay.INSTANCE);
            return;
        }
    }

    @Override
    public void exit(GoalKeeper keeper) {
        keeper.steering().pursuitOff();
    }

    @Override
    public boolean onMessage(GoalKeeper e, final Telegram t) {
        return false;
    }
}
