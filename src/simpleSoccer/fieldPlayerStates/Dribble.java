package simpleSoccer.fieldPlayerStates;

import common.messaging.Telegram;
import static simpleSoccer.DEFINE.*;
import simpleSoccer.agents.FieldPlayer;
import static simpleSoccer.ParamLoader.Prm;
import common.D2.Vector2D;

import static common.D2.Transformation.Vec2DRotateAroundOrigin;
import static common.debug.DbgConsole.*;
import common.fsm.State;
import static common.misc.Utils.QuarterPi;

public enum Dribble implements State<FieldPlayer> {
    INSTANCE;

    @Override
    public void enter(FieldPlayer player) {
        //let the team know this player is controlling
        player.team().setControllingPlayer(player);

        if (def(PLAYER_STATE_INFO_ON)) {
            debug_con.print("Player ").print(player.getId()).print(" enters dribble state").print("");
        }
    }

    @Override
    public void execute(FieldPlayer player) {
        double dot = player.team().homeGoal().facing().Dot(player.heading());

        //if the ball is between the player and the home goal, it needs to swivel
        // the ball around by doing multiple small kicks and turns until the player 
        //is facing in the correct direction
        if (dot < 0) {
            //the player's heading is going to be rotated by a small amount (Pi/4) 
            //and then the ball will be kicked in that direction
            Vector2D direction = player.heading();

            //calculate the sign (+/-) of the angle between the player heading and the 
            //facing direction of the goal so that the player rotates around in the 
            //correct direction
            double angle = QuarterPi * -1
                    * player.team().homeGoal().facing().Sign(player.heading());

            Vec2DRotateAroundOrigin(direction, angle);

            //this value works well whjen the player is attempting to control the
            //ball and turn at the same time
            final double KickingForce = 0.8;

            player.ball().kick(direction, KickingForce);
        } //kick the ball down the field
        else {
            player.ball().kick(player.team().homeGoal().facing(),
                    Prm.MaxDribbleForce);
        }

        //the player has kicked the ball so he must now change state to follow it
        player.getFSM().ChangeState(ChaseBall.INSTANCE);

        return;
    }

    @Override
    public void exit(FieldPlayer player) {
    }

    @Override
    public boolean onMessage(FieldPlayer e, final Telegram t) {
        return false;
    }
}
