package UI;

import Backend.Heroes;
import Backend.PickAssignment;
import Backend.Player;
import Backend.Outdraft;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PickPane extends GridPane {
    private static final String RANDOM_PLAYER_STRING = "RANDOM";

    private Outdraft outdraft;
    private int id;
    private String heroName;
    private Label advantageLabel;
    private HeroButton heroButton;
    private ComboBox<String> playerBox;
    private ComboBox<String> positionBox;
    private ChangeListener playerBoxListener;
    private ChangeListener positionBoxListener;

    public PickPane(Outdraft outdraft, int id, Runnable updateFunction) {
        this.outdraft = outdraft;
        this.id = id;
        heroName = null;

        advantageLabel = new Label("");
        advantageLabel.getStyleClass().addAll("pick-label");
        add(advantageLabel, 0, 0);

        heroButton = new HeroButton("empty", HeroButton.ImageType.LARGE, false);
        add(heroButton, 0, 1);

        getStyleClass().add("pick-pane");

        playerBox = new ComboBox<>();
        positionBox = new ComboBox<>();

        playerBoxListener = (observableValue, o, t1) -> {
            if (t1 == null)
                return;

            Player player = null;
            if (t1.equals(RANDOM_PLAYER_STRING)) {
                player = Player.RANDOM_PLAYER;
            }
            else {
                ArrayList<Player> players = outdraft.getTeam().getActivePlayers();
                for (Player p : players) {
                    if (p.getName().equals(t1)) {
                        player = p;
                        break;
                    }
                }
            }
            final Player playerArg = player;
            Platform.runLater(() -> {
                outdraft.setPlayerAssignment(id, playerArg);
                if (updateFunction != null) {
                    updateFunction.run();
                }
            });
            playerBox.setId((player != null) ? "user-set" : "");
        };

        positionBoxListener = (observableValue, o, t1) -> {
            if (t1 == null)
                return;

            int position = -1;
            String str = (String)t1;
            if (!str.isEmpty()) {
                char c = str.charAt(str.length() - 1);
                position = c - '1';
            }
            final int positionArg = position;
            Platform.runLater(() -> {
                outdraft.setPositionAssignment(id, positionArg);
                if (updateFunction != null) {
                    updateFunction.run();
                }
            });
            positionBox.setId((position != -1) ? "user-set" : "");
        };

        if (id >= 0) {
            positionBox.getItems().addAll(
                    "", "Position 1", "Position 2", "Position 3", "Position 4", "Position 5"
            );

            playerBox.valueProperty().addListener(playerBoxListener);
            positionBox.valueProperty().addListener(positionBoxListener);
        }

        playerBox.setDisable(true);
        positionBox.setDisable(true);
        add(playerBox, 0, 2);
        add(positionBox, 0, 3);
    }

    public void resetPlayers() {
        playerBox.getItems().clear();

        ArrayList<Player> players = outdraft.getTeam().getActivePlayers();
        if (id >= 0) {
            playerBox.getItems().add("");
            for (Player player : players) {
                playerBox.getItems().add(player.getName());
            }
            if (players.size() < 5) {
                playerBox.getItems().add(RANDOM_PLAYER_STRING);
            }
        }
    }

    private void setPlayerBoxValue(String value) {
        playerBox.valueProperty().removeListener(playerBoxListener);
        playerBox.setValue(value);
        playerBox.valueProperty().addListener(playerBoxListener);
    }

    private void setPositionBoxValue(String value) {
        positionBox.valueProperty().removeListener(positionBoxListener);
        positionBox.setValue(value);
        positionBox.valueProperty().addListener(positionBoxListener);
    }

    public void setHeroName(String heroName) {
        this.heroName = heroName;
        if (heroName != null) {
            heroButton.setHeroName(heroName);
        }
        else {
            heroButton.setHeroName("empty");
            advantageLabel.setText("");
            setPlayerBoxValue("");
            setPositionBoxValue("");
            playerBox.setId("");
            positionBox.setId("");
        }
        playerBox.setDisable(id == -1 || heroName == null);
        positionBox.setDisable(id == -1 || heroName == null);
    }

    public String getHeroName() {
        return heroName;
    }

    public void setPickAssignment(PickAssignment pickAssignment) {
        if (heroName != null) {
            int position = pickAssignment.getPositionFor(Heroes.getInstance().getHeroByName(heroName));
            if (position >= 0) {
                Player player = pickAssignment.getPlayers()[position];
                if (player != null) {
                    setPlayerBoxValue(player.getName() + " ");
                }
                else {
                    setPlayerBoxValue(RANDOM_PLAYER_STRING + " ");
                }
                setPositionBoxValue("Position " + (position + 1) + " ");
            }
        }
    }

    public void setAdvantage(double advantage) {
        DecimalFormat df = new DecimalFormat("00.00");
        String text = (advantage >= 0) ? "+" : "";
        text += df.format(advantage * 100) + "%";
        advantageLabel.setText(text);
        advantageLabel.setTextFill(Tools.ratingToColor(advantage, -0.03, 0.03));
    }

    public HeroButton getHeroButton() {
        return heroButton;
    }

    public void resetPlayerHighlighting() {
        playerBox.setId("");
    }

    public void resetPositionHighlighting() {
        positionBox.setId("");
    }
}
