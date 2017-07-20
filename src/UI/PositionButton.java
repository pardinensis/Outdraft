package UI;

import Backend.Player;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class PositionButton extends Button {
    public static final Color[] colors = {
            new Color(0.0, 1.0, 0.0, 1.0),
            new Color(0.6, 0.8, 0.2, 1.0),
            new Color(1.0, 1.0, 0.2, 1.0),
            new Color(0.8, 0.5, 0.2, 1.0),
            new Color(0.8, 0.0, 0.2, 1.0),
            new Color(0.2, 0.2, 0.2, 1.0)
    };

    private int familiarity;
    private Runnable saveCallback;

    public PositionButton(int position, int familiarity, Runnable saveCallback) {
        this.saveCallback = saveCallback;
        setText("" + (position + 1));
        getStyleClass().clear();
        getStyleClass().add("position-button");
        setFamiliarity(familiarity);
        setOnAction((ActionEvent) -> onAction());
    }

    private void update() {
        setBackground(new Background(new BackgroundFill(colors[familiarity], new CornerRadii(10), Insets.EMPTY)));
    }

    public void setFamiliarity(int familiarity) {
        this.familiarity = familiarity;
        assert familiarity >= 0 && familiarity < 6;
        update();
    }

    public int getFamiliarity() {
        return familiarity;
    }

    private void onAction() {
        if (familiarity != 5) {
            familiarity = (familiarity + 1) % 5;
            update();
        }

        saveCallback.run();
    }
}
