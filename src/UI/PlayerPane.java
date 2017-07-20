package UI;

import Backend.Player;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;


public class PlayerPane {
    private TextField idField;
    private Label nameLabel;
    private Player player;
    private PositionButton[] positions;

    public PlayerPane(Player player, GridPane grid, int row) {
        this.player = player;

        idField = new TextField();
        grid.add(idField, 0, row);
        idField.setOnAction((ActionEvent) -> parseId());
        idField.focusedProperty().addListener((observableValue, oldProp, newProp) -> {
            if (!newProp) {
                parseId();
            }
        });

        nameLabel = new Label("");
        grid.add(nameLabel, 1, row);
        nameLabel.getStyleClass().add("name-label");

        positions = new PositionButton[5];
        for (int i = 0; i < 5; ++i) {
            int familiarity = 5;
            if (player != null) {
                familiarity = player.getPositionsString().charAt(i) - 'a';
            }
            positions[i] = new PositionButton(i, familiarity, this::saveCallback);
            grid.add(positions[i], 2 + i, row);
        }

        update();
    }

    private void saveCallback() {
        char[] cs = new char[5];
        for (int i = 0; i < 5; ++i) {
            cs[i] = (char)('a' + positions[i].getFamiliarity());
        }
        player.setPositionsString(new String(cs));
    }

    private void update() {
        if (player == null) {
            idField.setText("");
            nameLabel.setText("");
            for (int i = 0; i < 5; ++i) {
                positions[i].setFamiliarity(5);
            }
            return;
        }

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
}
