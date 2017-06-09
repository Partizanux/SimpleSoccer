package simpleSoccer.fieldPlayerStates;

import common.D2.Vector2D;
import static simpleSoccer.DEFINE.*;
import static simpleSoccer.ParamLoader.Prm;
import simpleSoccer.agents.FieldPlayer;
import static common.debug.DbgConsole.*;
import common.fsm.State;
import common.messaging.Telegram;
import static common.misc.Utils.randFloat;

public enum ReceiveBall implements State<FieldPlayer> {
    INSTANCE;

    //TODO move to Prm
    private final double PASS_THREAT_RADIUS = 70.0;

    @Override
    public void enter(FieldPlayer player) {
        //let the team know this player is receiving the ball
        player.team().setReceiver(player);

        //this player is also now the controlling player
        player.team().setControllingPlayer(player);

        //there are two types of receive behavior. One uses ARRIVE to direct
        //the receiver to the position sent by the passer in its telegram. The
        //other uses the PURSUIT behavior to pursue the ball.
        //This statement selects between them dependent on the probability
        //ChanceOfUsingArriveTypeReceiveBehavior, whether or not an opposing
        //player is close to the receiving player, and whether or not the receiving
        //player is in the opponents 'hot region' (the third of the pitch closest
        //to the opponent's goal
        if (player.inHotRegion() || randFloat() < Prm.ChanceOfUsingArriveTypeReceiveBehavior
                && !player.team().isOpponentWithinRadius(player.pos(), PASS_THREAT_RADIUS)) {
            player.steering().arriveOn();
            if (def(PLAYER_STATE_INFO_ON)) {
                debug_con.print("Player ").print(player.getId()).print(" enters receive state (Using Arrive)").print("");
            }
        } else {
            player.steering().pursuitOn();
            if (def(PLAYER_STATE_INFO_ON)) {
                debug_con.print("Player ").print(player.getId()).print(" enters receive state (Using Pursuit)").print("");
            }
        }
    }

    @Override
    public void execute(FieldPlayer player) {
        //if the ball comes close enough to the player or if his team lose control
        //he should change state to chase the ball
        if (player.ballWithinReceivingRange() || !player.team().inControl()) {
            player.getFSM().changeState(ChaseBall.INSTANCE);
            return;
        }

        //TODO remove duplicates SteeringBehaviors.PURSUIT(SoccerBall ball)
        if (player.steering().isPursuit()) {
            player.steering().setTarget(player.ball().pos());
        }

        //if the player has 'arrived' at the steering target he should wait and
        //turn to face the ball
        if (player.atTarget()) {
            player.steering().arriveOff();
            player.steering().pursuitOff();
            player.trackBall();
            player.setVelocity(new Vector2D(0, 0));
        }
    }

    @Override
    public void exit(FieldPlayer player) {
        player.steering().arriveOff();
        player.steering().pursuitOff();
        player.team().setReceiver(null);
    }

    @Override
    public boolean onMessage(FieldPlayer e, final Telegram t) {
        return false;
    }
}