package Backend;

import java.util.ArrayList;

public interface Outdraft {
    void setTeam(Team team);
    Team getTeam();

    void restart();

    void pickParty(String heroName);
    void pickEnemy(String heroName);

    void ban(String heroName);

    void setPlayerAssignment(int pickId, Player player);
    void setPositionAssignment(int pickId, int position);

    void undo();

    ArrayList<PossiblePick> ratePicks();
    ArrayList<PossiblePick> rateBans();

    PossiblePick getCurrentState();
    PossiblePick getPossiblePick(String heroName);
    PossiblePick getPossibleBan(String heroName);

    boolean isAvailable(String heroName);

    void clearCache();
}
