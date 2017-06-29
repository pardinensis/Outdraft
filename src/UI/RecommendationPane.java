package UI;

import Backend.Outdraft;
import Backend.PossiblePick;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;

public class RecommendationPane extends StackPane {
    private static final int N_BUTTONS = 20;
    private static final int N_ROWS = 2;

    ArrayList<HeroButton> heroButtons;
    Outdraft outdraft;
    boolean ally;

    public RecommendationPane(Outdraft outdraft, DraftPane draftPane, HeroGrid heroGrid, boolean ally) {
        this.outdraft = outdraft;
        this.ally = ally;

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("recommendation-pane");
        getChildren().add(gridPane);

        heroButtons = new ArrayList<>();
        for (int i = 0; i < N_BUTTONS; ++i) {
            HeroButton heroButton = new HeroButton("Axe", HeroButton.ImageType.HORIZONTAL);
            heroButton.setClickAction(() -> {
                heroGrid.setUnavailable(heroButton.getHeroName());
                draftPane.click(heroButton.getHeroName());
            });
            heroButton.setHoverAction(() -> {
                String heroName = heroButton.getHeroName();
                draftPane.hover(heroName);
            });
            heroButtons.add(heroButton);
            gridPane.add(heroButton, i % (N_BUTTONS / N_ROWS), i / (N_BUTTONS / N_ROWS));
        }

        draftPane.addUpdateAction(this::update);
    }

    public void update(DraftOrder.State state) {
        ArrayList<PossiblePick> possiblePicks = (this.ally) ? outdraft.ratePicks() : outdraft.rateBans();
        for (int i = 0; i < N_BUTTONS; ++i) {
            HeroButton button = heroButtons.get(i);
            String heroName = possiblePicks.get(i).getHero().getName();
            button.setHeroName(heroName);
            button.setAvailable(true);
        }

        boolean highlighted = (ally && state == DraftOrder.State.PICK_ALLY) || (!ally && state == DraftOrder.State.BAN_ALLY);
        for (HeroButton button : heroButtons) {
            if (highlighted) {
                if (!button.getStyleClass().contains("highlighted")) {
                    button.getStyleClass().add("highlighted");
                }
            } else {
                button.getStyleClass().remove("highlighted");
            }
        }
    }
}