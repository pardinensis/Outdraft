package UI;

import Backend.Outdraft;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class DraftTab extends GridPane {
    public DraftTab(Outdraft outdraft) {
        getStyleClass().add("tab-content");

        WinRateLabel winRateLabel = new WinRateLabel();
        DraftPane draftPane = new DraftPane(outdraft, winRateLabel);
        HeroGrid heroGrid = new HeroGrid(draftPane, true);
        PredraftPane predraftPane = new PredraftPane(draftPane, heroGrid);
        RecommendationPane pickRecommendationPane = new RecommendationPane(outdraft, draftPane, heroGrid, true);
        RecommendationPane banRecommendationPane = new RecommendationPane(outdraft, draftPane, heroGrid, false);


        HBox topPane = new HBox(25);
        topPane.getChildren().addAll(pickRecommendationPane, winRateLabel, banRecommendationPane);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setValignment(VPos.CENTER);

        setAlignment(Pos.CENTER);
        topPane.setAlignment(Pos.CENTER);
        heroGrid.setAlignment(Pos.CENTER);

        add(predraftPane, 0, 0 );
        add(draftPane, 0, 1);
        add(topPane, 0, 2);
        add(separator, 0, 3);
        add(heroGrid, 0, 4);

        draftPane.runUpdateActions();
    }
}
