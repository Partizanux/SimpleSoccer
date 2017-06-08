package simpleSoccer.teamStates;

import simpleSoccer.agents.SoccerTeam;
import common.fsm.State;

public interface TeamState extends State<SoccerTeam> {
    static void ChangePlayerHomeRegions(SoccerTeam team, final int NewRegions[]) {
        for (int i = 0; i < NewRegions.length; ++i) {
            team.setPlayerHomeRegion(i, NewRegions[i]);
        }
    }
}