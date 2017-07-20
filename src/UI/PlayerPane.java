package UI;

import Backend.Player;
import Backend.Team;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;


public class PlayerPane {
    private Team team;
    private PlayerPane[] playerPanes;
    private Player player;

    private CheckBox checkBox;
    private boolean selectable;
    private TextField idField;
    private Label nameLabel;
    private PositionButton[] positions;

    public PlayerPane(Team team, PlayerPane[] playerPanes, Player player, GridPane grid, int row) {
        this.team = team;
        this.playerPanes = playerPanes;
        this.player = player;

        checkBox = new CheckBox();
        grid.add(checkBox, 0, row);
        checkBox.setDisable(player == null);
        checkBox.setOnAction((ActionEvent) -> saveCallback());

        idField = new TextField();
        grid.add(idField, 1, row);
        idField.setOnAction((ActionEvent) -> parseId());
        idField.focusedProperty().addListener((observableValue, oldProp, newProp) -> {
            if (!newProp) {
                parseId();
            }
        });

        nameLabel = new Label("");
        grid.add(nameLabel, 2, row);
        nameLabel.getStyleClass().add("name-label");

        positions = new PositionButton[5];
        for (int i = 0; i < 5; ++i) {
            int familiarity = 5;
            if (player != null) {
                familiarity = player.getPositionsString().charAt(i) - 'a';
            }
            positions[i] = new PositionButton(i, familiarity, this::saveCallback);
            grid.add(positions[i], 3 + i, row);
        }

        update();
    }

    private void saveCallback() {
        char[] cs = new char[5];
        for (int i = 0; i < 5; ++i) {
            cs[i] = (char)('a' + positions[i].getFamiliarity());
        }
        player.setPlaying(checkBox.isSelected());
        player.setPositionsString(new String(cs));

        updateCheckBoxes();
    }

    private void updateCheckBoxes() {
        int nSelected = 0;
        for (PlayerPane playerPane : playerPanes) {
            if (playerPane.checkBox.isSelected()) {
                ++nSelected;
            }
        }
        for (PlayerPane playerPane : playerPanes) {
            boolean selectable = (nSelected < 5 && playerPane.player != null) || playerPane.checkBox.isSelected();
            playerPane.checkBox.setDisable(!selectable);
        }
    }


    private void update() {
        if (player == null) {
            checkBox.setSelected(false);
            idField.setText("");
            nameLabel.setText("");
            for (int i = 0; i < 5; ++i) {
                positions[i].setFamiliarity(5);
            }
            return;
        }

        checkBox.setSelected(player.isPlaying());
        idField.setText("" + player.getId());
        nameLabel.setText(player.getName());
        String positionStr = player.getPositionsString();
        for (int i = 0; i < 5; ++i) {
            positions[i].setFamiliarity(positionStr.charAt(i) - 'a');
        }
    }

    private void parseId() {
        String input = idField.getText();

        if (input.equals("")) {
            player = null;
            update();
        }
        else {
            try {
                long id = Long.parseLong(input);
                if (id > 0) {
                    player = new Player(id);
                    update();
                }
            } catch (NumberFormatException nfex) {
                // do nothing
            }
        }

        updateTeam();
        updateCheckBoxes();
    }


    private void updateTeam() {
        ArrayList<Player> players = new ArrayList<>();
        for (PlayerPane playerPane : playerPanes) {
            Player player = playerPane.getPlayer();
            if (player != null) {
                players.add(player);
            }
        }
        team.setPlayers(players);
        team.writeToCache();
    }

    public Player getPlayer() {
        return player;
    }
}
