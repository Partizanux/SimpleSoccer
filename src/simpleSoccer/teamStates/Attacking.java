package simpleSoccer.teamStates;

import simpleSoccer.agents.SoccerTeam;
import static simpleSoccer.DEFINE.*;
import static simpleSoccer.teamStates.TeamState.ChangePlayerHomeRegions;
import static common.debug.DbgConsole.*;

import common.messaging.Telegram;

public enum Attacking implements TeamState {
    INSTANCE;

    @Override
    public void enter(SoccerTeam team) {
        if (def(DEBUG_TEAM_STATES)) {
            debug_con.print(team.Name()).print(" entering Attacking state").print("");
        }

        //these define the home regions for this state of each of the players
        final int BlueRegions[] = {1, 12, 14, 6, 4};
        final int RedRegions[] = {16, 3, 5, 9, 13};

        //set up the player's home regions
        if (team.color() == SoccerTeam.blue) {
            ChangePlayerHomeRegions(team, BlueRegions);
        } else {
            ChangePlayerHomeRegions(team, RedRegions);
        }

        //if a player is in either the Wait or ReturnToHomeRegion states, its
        //steering target must be updated to that of its new home region to enable
        //it to move into the correct position.
        team.updateTargetsOfWaitingPlayers();
    }

    @Override
    public void execute(SoccerTeam team) {
        //if this team is no longer in control change states
        if (!team.inControl()) {
            team.getFSM().changeState(Defending.INSTANCE);
            return;
        }

        //calculate the best position for any supporting ATTACKER to move to
        team.determineBestSupportingPosition();
    }

    @Override
    public void exit(SoccerTeam team) {
        //there is no supporting player for defense
        team.setSupportingPlayer(null);
    }

    @Override
    public boolean onMessage(SoccerTeam e, final Telegram t) {
        return false;
    }
}
