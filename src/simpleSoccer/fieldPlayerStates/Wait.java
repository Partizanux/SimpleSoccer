package simpleSoccer.fieldPlayerStates;

import simpleSoccer.agents.FieldPlayer;
import static simpleSoccer.DEFINE.*;
import common.D2.Vector2D;
import static common.debug.DbgConsole.*;
import common.fsm.State;
import common.messaging.Telegram;

public enum Wait implements State<FieldPlayer> {
    INSTANCE;

    @Override
    public void enter(FieldPlayer player) {
        if (def(PLAYER_STATE_INFO_ON)) {
                debug_con.print("Player ").print(player.getId()).print(" enters wait state").print("");
        }

        //if the game is not on make sure the target is the center of the player's
        //home region. This is ensure all the players are in the correct positions
        //ready for kick off
        if (!player.pitch().isGameOn()) {
            player.steering().setTarget(player.homeRegion().center());
        }
    }

    @Override
    public void execute(FieldPlayer player) {
        //if the player has been jostled out of position, get back in position  
        if (!player.atTarget()) {
            player.steering().arriveOn();

            return;
        } else {
            player.steering().arriveOff();

            player.setVelocity(new Vector2D(0, 0));

            //the player should keep his eyes on the ball!
            player.trackBall();
        }

        //if this player's team is controlling AND this player is not the ATTACKER
        //AND is further up the field than the ATTACKER he should request a pass.
        if (player.team().inControl()
                && (!player.isControllingPlayer())
                && player.isAheadOfAttacker()) {
            player.team().requestPass(player);

            return;
        }

        if (player.pitch().isGameOn()) {
            //if the ball is nearer this player than any other team member  AND
            //there is not an assigned receiver AND neither goalkeeper has
            //the ball, go chase it
            if (player.isClosestTeamMemberToBall()
                    && player.team().receiver() == null
                    && !player.pitch().goalKeeperHasBall()) {
                player.getFSM().ChangeState(ChaseBall.INSTANCE);

                return;
            }
        }
    }

    @Override
    public void exit(FieldPlayer player) {
    }

    @Override
    public boolean onMessage(FieldPlayer e, final Telegram t) {
        return false;
    }
}