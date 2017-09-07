package UI;

import Backend.Outdraft;
import Backend.PickAssignment;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;

import java.text.DecimalFormat;
import java.util.function.DoubleConsumer;

public class SettingsTab extends GridPane {

    private Slider positionSlider;
    private Label positionLabel;
    private Slider heroSlider;
    private Label heroLabel;
    private Slider winRateSlider;
    private Label winRateLabel;

    private Pair<Slider, Label> create01Slider(DoubleConsumer callback) {
        Slider slider = new Slider(0, 1, 1);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(3);
        slider.setBlockIncrement(0.25);
//        slider.setSnapToTicks(true);

        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        Label label = new Label(decimalFormat.format(slider.getValue()));
        slider.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            label.setText(decimalFormat.format(slider.getValue()));
            callback.accept(slider.getValue());
        });

        return new Pair(slider, label);
    }

    public SettingsTab(Outdraft outdraft) {
        getStyleClass().add("player-pane");


        add(new Label("Position Familiarity"), 0, 0);
        Pair<Slider, Label> positionPair = create01Slider((double value) -> PickAssignment.PLAYER_POSITION_FACTOR = value);
        positionSlider = positionPair.a;
        positionLabel = positionPair.b;
        add(positionSlider, 0, 1);
        add(positionLabel, 1, 1);

        add(new Label("Hero Familiarity"), 0, 2);
        Pair<Slider, Label> heroPair = create01Slider((double value) -> PickAssignment.PLAYER_HERO_FACTOR = value);
        heroSlider = heroPair.a;
        heroLabel = heroPair.b;
        add(heroSlider, 0, 3);
        add(heroLabel, 1, 3);

        add(new Label("Public Win Rates"), 0, 4);
        Pair<Slider, Label> winRatePair = create01Slider((double value) -> PickAssignment.HERO_WIN_RATE_FACTOR = value);
        winRateSlider = winRatePair.a;
        winRateLabel = winRatePair.b;
        add(winRateSlider, 0, 5);
        add(winRateLabel, 1, 5);

        for (Node node : getChildren()) {
            GridPane.setHalignment(node, HPos.CENTER);
        }
        setAlignment(Pos.CENTER);
    }
}
