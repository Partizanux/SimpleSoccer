package simpleSoccer.fieldPlayerStates;

import static simpleSoccer.DEFINE.*;
import simpleSoccer.agents.FieldPlayer;
import static common.debug.DbgConsole.*;
import common.fsm.State;
import common.messaging.Telegram;

public enum ChaseBall implements State<FieldPlayer> {
    INSTANCE;

    @Override
    public void enter(FieldPlayer player) {
        player.steering().seekOn();
        if (def(PLAYER_STATE_INFO_ON)) {
            debug_con.print("Player ").print(player.getId()).print(" enters chase state").print("");
        }
    }

    @Override
    public void execute(FieldPlayer player) {
        //if the ball is within kicking range the player changes state to KickBall.
        if (player.ballWithinKickingRange()) {
            player.getFSM().changeState(KickBall.INSTANCE);
            return;
        }

        //if the player is the closest player to the ball then he should keep
        //chasing it
        if (player.isClosestTeamMemberToBall()) {
            player.steering().setTarget(player.ball().pos());
            return;
        }

        //if the player is not closest to the ball anymore, he should return back
        //to his home region and wait for another opportunity
        player.getFSM().changeState(ReturnToHomeRegion.INSTANCE);
    }

    @Override
    public void exit(FieldPlayer player) {
        player.steering().seekOff();
    }

    @Override
    public boolean onMessage(FieldPlayer e, final Telegram t) {
        return false;
    }
}
