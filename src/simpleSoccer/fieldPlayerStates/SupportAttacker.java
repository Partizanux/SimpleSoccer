package simpleSoccer.fieldPlayerStates;

import static simpleSoccer.DEFINE.*;
import simpleSoccer.agents.FieldPlayer;
import static simpleSoccer.ParamLoader.Prm;
import common.D2.Vector2D;
import static common.debug.DbgConsole.*;
import common.fsm.State;
import common.messaging.Telegram;

public enum SupportAttacker implements State<FieldPlayer> {
    INSTANCE;

    @Override
    public void enter(FieldPlayer player) {
        player.steering().arriveOn();

        player.steering().setTarget(player.team().getSupportSpot());

        if (def(PLAYER_STATE_INFO_ON)) {
            debug_con.print("Player ").print(player.getId()).print(" enters support state").print("");
        }
    }

    @Override
    public void execute(FieldPlayer player) {
        //if his team loses control go back home
        if (!player.team().inControl()) {
            player.getFSM().ChangeState(ReturnToHomeRegion.INSTANCE);
            return;
        }

        //if the best supporting spot changes, change the steering target
        if (player.team().getSupportSpot() != player.steering().target()) {
            player.steering().setTarget(player.team().getSupportSpot());

            player.steering().arriveOn();
        }

        //if this player has a shot at the goal AND the ATTACKER can pass
        //the ball to him the ATTACKER should pass the ball to this player
        if (player.team().canShoot(player.pos(),
                Prm.MaxShootingForce)) {
            player.team().requestPass(player);
        }


        //if this player is located at the support spot and his team still have
        //possession, he should remain still and turn to face the ball
        if (player.atTarget()) {
            player.steering().arriveOff();

            //the player should keep his eyes on the ball!
            player.trackBall();

            player.setVelocity(new Vector2D(0, 0));

            //if not threatened by another player request a pass
            if (!player.isThreatened()) {
                player.team().requestPass(player);
            }
        }
    }

    @Override
    public void exit(FieldPlayer player) {
        //set supporting player to null so that the team knows it has to 
        //determine a new one.
        player.team().setSupportingPlayer(null);

        player.steering().arriveOff();
    }

    @Override
    public boolean onMessage(FieldPlayer e, final Telegram t) {
        return false;
    }
}
