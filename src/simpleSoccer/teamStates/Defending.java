package simpleSoccer.teamStates;

import simpleSoccer.agents.SoccerTeam;
import static simpleSoccer.DEFINE.*;
import static simpleSoccer.teamStates.TeamState.ChangePlayerHomeRegions;
import static common.debug.DbgConsole.*;

import common.messaging.Telegram;

public enum Defending implements TeamState {
    INSTANCE;

    @Override
    public void enter(SoccerTeam team) {
        if (def(DEBUG_TEAM_STATES)) {
            debug_con.print(team.Name()).print(" entering Defending state").print("");
        }

        //these define the home regions for this state of each of the players
        final int BlueRegions[] = {1, 6, 8, 3, 5};
        final int RedRegions[] = {16, 9, 11, 12, 14};

        //set up the player's home regions
        if (team.color() == SoccerTeam.blue) {
            ChangePlayerHomeRegions(team, BlueRegions);
        } else {
            ChangePlayerHomeRegions(team, RedRegions);
        }

        //if a player is in either the Wait or ReturnToHomeRegion states, its
        //steering target must be updated to that of its new home region
        team.updateTargetsOfWaitingPlayers();
    }

    @Override
    public void execute(SoccerTeam team) {
        //if in control change states
        if (team.inControl()) {
            team.getFSM().changeState(Attacking.INSTANCE);
            return;
        }
    }

    @Override
    public void exit(SoccerTeam team) {
    }

    @Override
    public boolean onMessage(SoccerTeam e, final Telegram t) {
        return false;
    }
}
