package simpleSoccer.goalKeeperStates;

import simpleSoccer.agents.GoalKeeper;
import static simpleSoccer.ParamLoader.Prm;
import static simpleSoccer.MessageTypes.*;
import simpleSoccer.agents.PlayerBase;
import common.D2.Vector2D;
import static common.D2.Vector2D.*;
import common.fsm.State;
import common.messaging.Telegram;
import static common.messaging.MessageDispatcher.*;
import common.misc.CppToJava.ObjectRef;

public enum PutBallBackInPlay implements State<GoalKeeper> {
    INSTANCE;

    @Override
    public void enter(GoalKeeper keeper) {
        //let the team know that the keeper is in control
        keeper.team().setControllingPlayer(keeper);

        //send all the players home
        keeper.team().opponents().returnAllFieldPlayersToHome();
        keeper.team().returnAllFieldPlayersToHome();
    }

    @Override
    public void execute(GoalKeeper keeper) {
        PlayerBase receiver = null;
        Vector2D ballTarget = new Vector2D();

        ObjectRef<PlayerBase> receiverRef = new ObjectRef<PlayerBase>(receiver);

        //test if there are players further forward on the field we might
        //be able to pass to. If so, make a pass.
        if (keeper.team().findPass(keeper, receiverRef, ballTarget, Prm.MaxPassingForce, Prm.GoalkeeperMinPassDist)) {
            receiver = receiverRef.get();
            //make the pass   
            keeper.ball().kick(Vec2DNormalize(sub(ballTarget, keeper.ball().pos())), Prm.MaxPassingForce);

            //goalkeeper no longer has ball 
            keeper.pitch().setGoalKeeperHasBall(false);

            //let the receiving player know the ball's comin' at him
            Dispatcher.DispatchMsg(SEND_MSG_IMMEDIATELY,
                    keeper.getId(),
                    receiver.getId(),
                    Msg_ReceiveBall,
                    ballTarget);

            //go back to tending the goal   
            keeper.getFSM().changeState(TendGoal.INSTANCE);

            return;
        }

        keeper.setVelocity(new Vector2D());
    }

    @Override
    public void exit(GoalKeeper keeper) {
    }

    @Override
    public boolean onMessage(GoalKeeper e, final Telegram t) {
        return false;
    }
}
