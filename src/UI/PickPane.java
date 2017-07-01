package UI;

import Backend.Heroes;
import Backend.PickAssignment;
import Backend.Player;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.text.DecimalFormat;

public class PickPane extends GridPane {
    private String heroName;
    private Label advantageLabel;
    private HeroButton heroButton;
    private Label playerLabel;
    private Label positionLabel;

    public PickPane() {
        heroName = null;

        advantageLabel = new Label("");
        add(advantageLabel, 0, 0);

        heroButton = new HeroButton("empty", HeroButton.ImageType.LARGE);
        add(heroButton, 0, 1);

        getStyleClass().add("pick-pane");

        playerLabel = new Label("");
        playerLabel.getStyleClass().addAll("label", "label-bold");
        add(playerLabel, 0, 2);

        positionLabel = new Label("");
        positionLabel.getStyleClass().add("label");
        add(positionLabel, 0, 3);
    }

    public void setHeroName(String heroName) {
        this.heroName = heroName;
        if (heroName != null) {
            heroButton.setHeroName(heroName);
        }
        else {
            heroButton.setHeroName("empty");
            advantageLabel.setText("");
            playerLabel.setText("");
            positionLabel.setText("");
        }
    }

    public String getHeroName() {
        return heroName;
    }

    public void setPickAssignment(PickAssignment pickAssignment) {
        if (heroName != null) {
            int position = pickAssignment.getPositionFor(Heroes.getHeroByName(heroName));
            if (position >= 0) {
                Player player = pickAssignment.getPlayers()[position];
                if (player != null) {
                    playerLabel.setText(player.getName());
                }
                else {
                    playerLabel.setText("");
                }
                positionLabel.setText("Position " + (position + 1));
            }
        }
    }

    public void setAdvantage(double advantage) {
        DecimalFormat df = new DecimalFormat("00.00");
        String text = (advantage >= 0) ? "+" : "";
        text += df.format(advantage * 100) + "%";
        advantageLabel.setText(text);

        final Color good = new Color(0, 1, 0, 1);
        final Color neutral = new Color(1, 1, 1, 1);
        final Color bad = new Color(1, 0, 0, 1);

        final double maxValue = 0.03;
        Color mixed;
        if (advantage < 0) {
            mixed = neutral.interpolate(bad, -advantage / maxValue);
        }
        else {
            mixed = neutral.interpolate(good, advantage / maxValue);
        }
        advantageLabel.setTextFill(mixed);
    }

    public HeroButton getHeroButton() {
        return heroButton;
    }

}
