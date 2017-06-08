package simpleSoccer.teamStates;

import simpleSoccer.agents.SoccerTeam;
import common.messaging.Telegram;

public enum PrepareForKickOff implements TeamState {
    INSTANCE;

    @Override
    public void enter(SoccerTeam team) {
        //reset key player pointers
        team.setControllingPlayer(null);
        team.setSupportingPlayer(null);
        team.setReceiver(null);
        team.setPlayerClosestToBall(null);

        //send Msg_GoHome to each player.
        team.returnAllFieldPlayersToHome();
    }

    @Override
    public void execute(SoccerTeam team) {
        //if both teams in position, start the game
        if (team.allPlayersAtHome() && team.opponents().allPlayersAtHome()) {
            team.getFSM().ChangeState(Defending.INSTANCE);
        }
    }

    @Override
    public void exit(SoccerTeam team) {
        team.pitch().gameOn();
    }

    @Override
    public boolean onMessage(SoccerTeam e, final Telegram t) {
        return false;
    }
}