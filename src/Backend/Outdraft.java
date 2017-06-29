package Backend;

import java.util.ArrayList;

public interface Outdraft {
    void setTeam(Team team);
    void restart();

    void pickParty(String heroName);
    void pickEnemy(String heroName);

    void ban(String heroName);

    void undo();

    ArrayList<PossiblePick> ratePicks();
    ArrayList<PossiblePick> rateBans();

    PossiblePick getPossiblePick(String heroName);
    PossiblePick getPossibleBan(String heroName);

    boolean isAvailable(String heroName);
}
