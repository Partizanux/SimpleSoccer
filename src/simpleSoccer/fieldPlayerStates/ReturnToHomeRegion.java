package simpleSoccer.fieldPlayerStates;

import static simpleSoccer.DEFINE.*;
import simpleSoccer.agents.FieldPlayer;
import static common.debug.DbgConsole.*;
import common.fsm.State;
import common.game.Region;
import common.messaging.Telegram;

public enum ReturnToHomeRegion implements State<FieldPlayer> {
    INSTANCE;

    @Override
    public void enter(FieldPlayer player) {
        player.steering().arriveOn();

        if (!player.homeRegion().inside(player.steering().target(), Region.RegionModifier.HALFSIZE)) {
            player.steering().setTarget(player.homeRegion().center());
        }

        if (def(PLAYER_STATE_INFO_ON)) {
            debug_con.print("Player ").print(player.getId()).print(" enters ReturnToHome state").print("");
        }
    }

    @Override
    public void execute(FieldPlayer player) {
        if (player.pitch().isGameOn()) {
            //if the ball is nearer this player than any other team member  &&
            //there is not an assigned receiver && the goalkeeper does not gave
            //the ball, go chase it
            if (player.isClosestTeamMemberToBall()
                    && (player.team().receiver() == null)
                    && !player.pitch().goalKeeperHasBall()) {
                player.getFSM().changeState(ChaseBall.INSTANCE);

                return;
            }
        }

        //if game is on and close enough to home, change state to wait and set the 
        //player target to his current position.(so that if he gets jostled out of 
        //position he can move back to it)
        if (player.pitch().isGameOn() && player.homeRegion().inside(player.pos(),
                Region.RegionModifier.HALFSIZE)) {
            player.steering().setTarget(player.pos());
            player.getFSM().changeState(Wait.INSTANCE);
        } //if game is not on the player must return much closer to the center of his
        //home region
        else if (!player.pitch().isGameOn() && player.atTarget()) {
            player.getFSM().changeState(Wait.INSTANCE);
        }
    }

    @Override
    public void exit(FieldPlayer player) {
        player.steering().arriveOff();
    }

    @Override
    public boolean onMessage(FieldPlayer e, final Telegram t) {
        return false;
    }
}
