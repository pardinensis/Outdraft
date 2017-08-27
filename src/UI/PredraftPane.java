package UI;

import Backend.Heroes;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class PredraftPane extends GridPane {
    private Label gameMode;
    private Button allPick, captainsMode;

    private Label firstPick;
    private Button allyFirstPick, enemyFirstPick;

    private boolean gameModeIsAllPick;
    private boolean firstPickIsAlly;

    private DraftPane draftPane;
    private HeroGrid heroGrid;

    enum State {
        CHOOSE_GAME_MODE, CHOOSE_FIRST_PICK, READY
    }
    State currentState;

    public PredraftPane(DraftPane draftPane, HeroGrid heroGrid) {
        this.draftPane = draftPane;
        this.heroGrid = heroGrid;

        getStyleClass().add("predraft-pane");

        currentState = State.CHOOSE_GAME_MODE;

        gameMode = new Label("Game Mode");
        allPick = new Button("All Pick");
        captainsMode = new Button("Captains Mode");
        allPick.setOnAction((ActionEvent) -> setGameMode(true));
        captainsMode.setOnAction((ActionEvent) -> setGameMode(false));


        firstPick = new Label("First Pick");
        allyFirstPick = new Button("Our Team");
        enemyFirstPick = new Button("Enemy Team");
        allyFirstPick.setOnAction((ActionEvent) -> setFirstPick(true));
        enemyFirstPick.setOnAction((ActionEvent) -> setFirstPick(false));


        add(allPick, 0, 0);
        add(gameMode, 1, 0);
        add(captainsMode, 2, 0);
    }

    private void setGameMode(boolean gameModeIsAllPick) {
        this.gameModeIsAllPick = gameModeIsAllPick;
        currentState = State.CHOOSE_FIRST_PICK;
        getChildren().removeAll(allPick, gameMode, captainsMode);
        add(allyFirstPick, 0, 0);
        add(firstPick, 1, 0);
        add(enemyFirstPick, 2, 0);
    }

    private void setFirstPick(boolean ally) {
        firstPickIsAlly = ally;
        currentState = State.CHOOSE_GAME_MODE;
        getChildren().removeAll(allyFirstPick, firstPick, enemyFirstPick);
        add(allPick, 0, 0);
        add(gameMode, 1, 0);
        add(captainsMode, 2, 0);

        Heroes.replaceInstanceIfUpdated();
        heroGrid.reset();
        draftPane.restart(gameModeIsAllPick, firstPickIsAlly);
    }
}