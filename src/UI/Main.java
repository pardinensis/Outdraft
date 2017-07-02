package UI;

import Backend.*;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String WINDOW_NAME = "Outdraft 0.3.4";

    @Override
    public void start(Stage primaryStage) throws Exception{
        OutdraftImpl outdraft = new OutdraftImpl();
        boolean successfulInit = outdraft.init();
        if (!successfulInit) {
            showErrorMessage(primaryStage, "Data files not found. Make sure to place the jar executable next to the data folder!");
            return;
        }

        Heroes.updateCache();

        Team team = new Team();
        team.loadFromCache();

        System.out.println(team.getPlayers());

        outdraft.setTeam(team);
        outdraft.restart();


        WinRateLabel winRateLabel = new WinRateLabel();
        DraftPane draftPane = new DraftPane(outdraft, winRateLabel);
        HeroGrid heroGrid = new HeroGrid(draftPane);
        PredraftPane predraftPane = new PredraftPane(draftPane, heroGrid);
        RecommendationPane pickRecommendationPane = new RecommendationPane(outdraft, draftPane, heroGrid, true);
        RecommendationPane banRecommendationPane = new RecommendationPane(outdraft, draftPane, heroGrid, false);


        GridPane rootPane = new GridPane();
        HBox topPane = new HBox(25);
        topPane.getChildren().addAll(pickRecommendationPane, winRateLabel, banRecommendationPane);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setValignment(VPos.CENTER);

        rootPane.setAlignment(Pos.CENTER);
        topPane.setAlignment(Pos.CENTER);
        heroGrid.setAlignment(Pos.CENTER);

        rootPane.add(predraftPane, 0, 0 );
        rootPane.add(draftPane, 0, 1);
        rootPane.add(topPane, 0, 2);
        rootPane.add(separator, 0, 3);
        rootPane.add(heroGrid, 0, 4);

        draftPane.runUpdateActions();


        Scene scene = new Scene(rootPane, 1600, 900);
        scene.getStylesheets().add(Main.class.getResource("UI.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle(WINDOW_NAME);
//        primaryStage.setFullScreen(true);

        primaryStage.show();
    }

    private void showErrorMessage(Stage primaryStage, String message) {
        Label errorLabel = new Label(message);
        Button okButton = new Button("OK");
        okButton.setOnAction((x) -> System.exit(0));

        VBox rootPane = new VBox(20);
        rootPane.getChildren().addAll(errorLabel, okButton);
        Scene scene = new Scene(rootPane, -1, -1);
        scene.getStylesheets().add(Main.class.getResource("UI.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle(WINDOW_NAME);
        primaryStage.show();
    }

//    private static void printSuggestions(ArrayList<PossiblePick> result) {
//        DecimalFormat df = new DecimalFormat("0.00");
//        DecimalFormat df2 = new DecimalFormat("+0.00;-#");
//        for (int i = 0; i < 12; ++i) {
//            PossiblePick possiblePick = result.get(i);
//            Hero hero = possiblePick.getHero();
//            String wrStr = df.format(possiblePick.getWinRate() * 100);
//            String relWrStr = df2.format(possiblePick.getAdvantage() * 100);
//            String heroStr = hero.getName();
//
//            StringBuilder strb = new StringBuilder();
//            strb.append(wrStr);
//            strb.append(" (");
//            strb.append(relWrStr);
//            strb.append(") ");
//            PickAssignment pickAssignment = possiblePick.getPickAssignment();
//            if (pickAssignment != null) {
//                strb.append("(");
//                String posAddend = df2.format(possiblePick.getPickAssignment().getRating() * 100);
//                strb.append(posAddend);
//                strb.append(") ");
//            }
//            strb.append(heroStr);
//            if (pickAssignment != null) {
//                for (int j = heroStr.length(); j < 20; ++j) strb.append(' ');
//                int pos = pickAssignment.getPositionFor(hero);
//                strb.append(pickAssignment.getPlayers()[pos]);
//            }
//            System.out.println(strb.toString());
//        }
//        System.out.println();
//
//        System.out.println(result.get(0).getPickAssignment());
//    }

    public static void main(String[] args) {
        launch(args);
    }
}
