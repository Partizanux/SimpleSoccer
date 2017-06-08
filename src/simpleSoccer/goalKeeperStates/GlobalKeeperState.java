package simpleSoccer.goalKeeperStates;

import simpleSoccer.agents.GoalKeeper;
import common.fsm.State;
import common.messaging.Telegram;

public enum GlobalKeeperState implements State<GoalKeeper> {
    INSTANCE;

    @Override
    public void enter(GoalKeeper keeper) {
    }

    @Override
    public void execute(GoalKeeper keeper) {
    }

    @Override
    public void exit(GoalKeeper keeper) {
    }

    @Override
    public boolean onMessage(GoalKeeper keeper, final Telegram telegram) {
        switch (telegram.Msg) {
            case Msg_GoHome:
                keeper.setDefaultHomeRegion();
                keeper.GetFSM().ChangeState(ReturnHome.INSTANCE);
                break;

            case Msg_ReceiveBall:
                keeper.GetFSM().ChangeState(InterceptBall.INSTANCE);
                break;
        }

        return false;
    }
}