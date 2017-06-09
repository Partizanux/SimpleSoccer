package simpleSoccer.fieldPlayerStates;

import common.misc.CppToJava.ObjectRef;
import common.messaging.Telegram;
import simpleSoccer.agents.PlayerBase;
import common.D2.Vector2D;
import static common.D2.Vector2D.*;
import static simpleSoccer.DEFINE.*;
import simpleSoccer.agents.FieldPlayer;
import static simpleSoccer.MessageTypes.*;
import static simpleSoccer.ParamLoader.Prm;
import static simpleSoccer.agents.SoccerBall.addNoiseToKick;
import common.fsm.State;
import static common.debug.DbgConsole.*;
import static common.messaging.MessageDispatcher.*;
import static common.misc.Stream_Utility_function.ttos;
import static common.misc.Utils.*;

public enum KickBall implements State<FieldPlayer> {
    INSTANCE;

    @Override
    public void enter(FieldPlayer player) {
        //let the team know this player is controlling
        player.team().setControllingPlayer(player);

        //the player can only make so many kick attempts per second.
        if (!player.isReadyForNextKick()) {
            player.getFSM().changeState(ChaseBall.INSTANCE);
        }

        if (def(PLAYER_STATE_INFO_ON)) {
            debug_con.print("Player ").print(player.getId()).print(" enters kick state").print("");
        }
    }

    @Override
    public void execute(FieldPlayer player) {
        //calculate the dot product of the vector pointing to the ball
        //and the player's heading
        Vector2D toBall = sub(player.ball().pos(), player.pos());
        double dot = player.heading().Dot(Vec2DNormalize(toBall));

        //cannot kick the ball if the goalkeeper is in possession or if it is 
        //behind the player or if there is already an assigned receiver. So just
        //continue chasing the ball
        if (player.team().receiver() != null || player.pitch().goalKeeperHasBall() || dot < 0) {
            if (def(PLAYER_STATE_INFO_ON)) {
                debug_con.print("Goaly has ball / ball behind player").print("");
            }

            player.getFSM().changeState(ChaseBall.INSTANCE);
            return;
        }

        /* Attempt a shot at the goal */

        //the dot product is used to adjust the shooting force. The more
        //directly the ball is ahead, the more forceful the kick
        double power = Prm.MaxShootingForce * dot;

        //if a shot is possible, this vector will hold the position along the
        //opponent's goal line the player should aim for.
        Vector2D ballTarget = new Vector2D();

        //if it is determined that the player could score a goal from this position
        //OR if he should just kick the ball anyway, the player will attempt
        //to make the shot
        if (player.team().canShoot(player.ball().pos(), power, ballTarget) || randFloat() < Prm.ChancePlayerAttemptsPotShot) {
            if (def(PLAYER_STATE_INFO_ON)) {
                debug_con.print("Player ").print(player.getId()).print(" attempts a shot at ").print(ballTarget).print("");
            }

            //TODO move addNoiseToKick to kick method ?
            //add some noise to the kick. We don't want players who are 
            //too accurate! The amount of noise can be adjusted by altering
            //Prm.PlayerKickingAccuracy
            ballTarget = addNoiseToKick(player.ball().pos(), ballTarget);

            //this is the direction the ball will be kicked in
            Vector2D KickDirection = sub(ballTarget, player.ball().pos());

            player.ball().kick(KickDirection, power);

            //change state   
            player.getFSM().changeState(Wait.INSTANCE);

            player.FindSupport();

            return;
        }


        /* Attempt a pass to a player */

        //if a receiver is found this will point to it
        PlayerBase receiver = null;

        power = Prm.MaxPassingForce * dot;

        //TODO remove ?
        ObjectRef<PlayerBase> receiverRef = new ObjectRef<>();
        //test if there are any potential candidates available to receive a pass
        if (player.isThreatened()
                && player.team().findPass(player, receiverRef, ballTarget, power, Prm.MinPassDist)) {
            receiver = receiverRef.get();

            //TODO move addNoiseToKick to kick method ?
            //add some noise to the kick
            ballTarget = addNoiseToKick(player.ball().pos(), ballTarget);

            Vector2D KickDirection = sub(ballTarget, player.ball().pos());

            player.ball().kick(KickDirection, power);

            if (def(PLAYER_STATE_INFO_ON)) {
                debug_con.print("Player ").print(player.getId()).print(" passes the ball with force ").print(ttos(power,3)).print("  to player ").print(receiver.getId()).print("  target is ").print(ballTarget).print("");
            }


            //let the receiver know a pass is coming 
            Dispatcher.DispatchMsg(SEND_MSG_IMMEDIATELY,
                    player.getId(),
                    receiver.getId(),
                    Msg_ReceiveBall,
                    ballTarget);


            //the player should wait at his current position unless instruced
            //otherwise  
            player.getFSM().changeState(Wait.INSTANCE);

            player.FindSupport();
        } else {
            //cannot shoot or pass, so dribble the ball
            player.FindSupport();
            player.getFSM().changeState(Dribble.INSTANCE);
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
