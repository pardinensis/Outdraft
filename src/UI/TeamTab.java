package UI;

import Backend.Player;
import Backend.Team;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

public class TeamTab extends GridPane {
    private Team team;
    private PlayerPane[] playerPanes;

    public TeamTab(Team team) {
        this.team = team;

        getStyleClass().add("player-pane");

        add(new Label("Playing"), 0, 0);
        add(new Label("Steam ID"), 1, 0);
        add(new Label("Name"), 2, 0);
        add(new Label("Preferred Positions"), 3, 0, 5, 1);
        add(new Separator(), 0, 1, 8, 1);

        ArrayList<Player> players = team.getAllPlayers();
        playerPanes = new PlayerPane[10];
        for (int i = 0; i < playerPanes.length; ++i) {
            Player player = null;
            if (i < players.size()) {
                player = players.get(i);
            }
            playerPanes[i] = new PlayerPane(team, playerPanes, player, this, i + 2);
        }

        for (Node node : getChildren()) {
            GridPane.setHalignment(node, HPos.CENTER);
        }
        setAlignment(Pos.CENTER);
    }
}
