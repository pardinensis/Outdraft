package UI;

import Backend.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String WINDOW_NAME = "Outdraft";

    @Override
    public void start(Stage primaryStage) throws Exception{
        OutdraftImpl outdraft = new OutdraftImpl();
        boolean successfulInit = outdraft.init();
        if (!successfulInit) {
            showErrorMessage(primaryStage, "Data files not found. Make sure to place the jar executable next to the data folder!");
            return;
        }

        Team team = new Team();
        team.loadFromCache();

        outdraft.setTeam(team);
        outdraft.restart();

        Tab teamTab = new Tab("Team", new TeamTab(team));
        teamTab.setClosable(false);

        Tab draftTab = new Tab("Draft", new DraftTab(outdraft));
        draftTab.setClosable(false);

        Tab dataTab = new Tab("Hero Positions", new DataTab());
        dataTab.setClosable(false);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(teamTab, draftTab, dataTab);

        StatusBar statusBar = new StatusBar();

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(tabPane);
        mainPane.setBottom(statusBar);

        Platform.runLater(() -> {
            final StackPane region = (StackPane) tabPane.lookup(".headers-region");
            final StackPane regionTop = (StackPane) tabPane.lookup(".tab-pane:top *.tab-header-area");
            regionTop.widthProperty().addListener((arg0, arg1, arg2) -> {
                Insets in = regionTop.getPadding();
                regionTop.setPadding(new Insets(
                        in.getTop(),
                        in.getRight(),
                        in.getBottom(),
                        arg2.doubleValue() / 2 - region.getWidth() / 2));
            });
            // force re-layout so the tabs aligned to center at initial state
            primaryStage.setWidth(primaryStage.getWidth() + 1);
        });

        Scene scene = new Scene(mainPane, 1600, 900);
        scene.getStylesheets().add(Main.class.getResource("UI.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle(WINDOW_NAME);
//        primaryStage.setFullScreen(true);

        primaryStage.show();

        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Heroes.updateCache(this::updateMessage);
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        statusBar.getStatusLabel().textProperty().bind(task.messageProperty());
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
