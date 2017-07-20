package UI;

import Backend.Player;
import Backend.Team;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

public class TeamTab extends GridPane {
    public TeamTab(Team team) {
        getStyleClass().add("player-pane");

        ArrayList<Player> players = team.getPlayers();

        for (int i = 0; i < 10; ++i) {
            Player player = null;
            if (i < players.size()) {
                player = players.get(i);
            }
            PlayerPane playerPane = new PlayerPane(player, this, i);
        }
        setAlignment(Pos.CENTER);
    }
}
