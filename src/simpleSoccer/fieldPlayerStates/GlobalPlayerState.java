package simpleSoccer.fieldPlayerStates;

import static simpleSoccer.DEFINE.*;
import static simpleSoccer.MessageTypes.*;
import simpleSoccer.agents.FieldPlayer;
import static simpleSoccer.ParamLoader.Prm;
import common.D2.Vector2D;
import static common.D2.Vector2D.*;
import static common.debug.DbgConsole.*;
import common.fsm.State;
import static common.messaging.MessageDispatcher.*;
import common.messaging.Telegram;

public enum GlobalPlayerState implements State<FieldPlayer> {
    INSTANCE;

    @Override
    public void enter(FieldPlayer player) {
    }

    @Override
    public void execute(FieldPlayer player) {
        //if a player is in possession and close to the ball reduce his max speed
        if ((player.ballWithinReceivingRange()) && (player.isControllingPlayer())) {
            player.setMaxSpeed(Prm.PlayerMaxSpeedWithBall);
        } else {
            player.setMaxSpeed(Prm.PlayerMaxSpeedWithoutBall);
        }

    }

    @Override
    public void exit(FieldPlayer player) {
    }

    //TODO simplify
    @Override
    public boolean onMessage(FieldPlayer player, final Telegram telegram) {
        switch (telegram.Msg) {
            case Msg_ReceiveBall: {
                //TODO move to ReceiveBall.enter
                //set the target
                player.steering().setTarget((Vector2D) telegram.ExtraInfo);

                //change state 
                player.getFSM().ChangeState(ReceiveBall.INSTANCE);

                return true;
            }

            case Msg_SupportAttacker: {
                //if already supporting just return
                if (player.getFSM().inState(SupportAttacker.INSTANCE)) {
                    return true;
                }

                //TODO Duplicates SupportAttacker.enter
                //set the target to be the best supporting position
                player.steering().setTarget(player.team().getSupportSpot());

                //change the state
                player.getFSM().ChangeState(SupportAttacker.INSTANCE);

                return true;
            }

            case Msg_Wait: {
                //change the state
                player.getFSM().ChangeState(Wait.INSTANCE);

                return true;
            }

            case Msg_GoHome: {
                player.setDefaultHomeRegion();

                player.getFSM().ChangeState(ReturnToHomeRegion.INSTANCE);

                return true;
            }

            case Msg_PassToMe: {
                //get the position of the player requesting the pass 
                FieldPlayer receiver = (FieldPlayer) telegram.ExtraInfo;

                if (def(PLAYER_STATE_INFO_ON)) {
                    debug_con.print("Player ").print(player.getId()).print(" received request from ").print(receiver.getId()).print(" to make pass").print("");
                }

                //if the ball is not within kicking range or their is already a 
                //receiving player, this player cannot pass the ball to the player
                //making the request.
                if (player.team().receiver() != null
                        || !player.ballWithinKickingRange()) {
                    if (def(PLAYER_STATE_INFO_ON)) {
                        debug_con.print("Player ").print(player.getId()).print(" cannot make requested pass <cannot kick ball>").print("");
                    }

                    return true;
                }

                //make the pass   
                player.ball().kick(sub(receiver.pos(), player.ball().pos()),
                        Prm.MaxPassingForce);


                if (def(PLAYER_STATE_INFO_ON)) {
                    debug_con.print("Player ").print(player.getId()).print(" Passed ball to requesting player").print("");
                }

                //let the receiver know a pass is coming 
                Dispatcher.DispatchMsg(SEND_MSG_IMMEDIATELY,
                        player.getId(),
                        receiver.getId(),
                        Msg_ReceiveBall,
                        receiver.pos());



                //change state   
                player.getFSM().ChangeState(Wait.INSTANCE);

                player.FindSupport();

                return true;
            }

        }

        return false;
    }
}