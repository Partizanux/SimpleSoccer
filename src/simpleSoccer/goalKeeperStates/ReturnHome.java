package simpleSoccer.goalKeeperStates;

import simpleSoccer.agents.GoalKeeper;
import common.fsm.State;
import common.messaging.Telegram;

public enum ReturnHome implements State<GoalKeeper> {
    INSTANCE;

    @Override
    public void enter(GoalKeeper keeper) {
        keeper.steering().arriveOn();
    }

    @Override
    public void execute(GoalKeeper keeper) {
        keeper.steering().setTarget(keeper.homeRegion().Center());

        //if close enough to home or the opponents get control over the ball,
        //change state to tend goal
        if (keeper.inHomeRegion() || !keeper.team().inControl()) {
            keeper.GetFSM().ChangeState(TendGoal.INSTANCE);
        }
    }

    @Override
    public void exit(GoalKeeper keeper) {
        keeper.steering().arriveOff();
    }

    @Override
    public boolean onMessage(GoalKeeper e, final Telegram t) {
        return false;
    }
}