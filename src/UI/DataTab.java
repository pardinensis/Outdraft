package UI;

import Backend.Hero;
import Backend.Heroes;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;


public class DataTab extends GridPane {
    private ArrayList<HeroDataPane> heroDataPanes;

    public DataTab() {
        for (int col = 0; col < 3; ++col) {
            ScrollPane scrollPane = new ScrollPane();
            add(scrollPane, col, 0);
            GridPane contentPane = new GridPane();
            scrollPane.setContent(contentPane);
            scrollPane.getStyleClass().clear();
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            contentPane.getStyleClass().add("player-pane");

            int row = 0;
            contentPane.add(new Label("Hero"), 0, row);
            contentPane.add(new Label("Preferred Positions"), 2, row, 5, 1);
            contentPane.add(new Separator(), 0, ++row, 7, 1);

            ArrayList<Hero> heroes;
            switch (col) {
                case 0: heroes = Heroes.getStrengthHeroes(); break;
                case 1: heroes = Heroes.getAgilityHeroes(); break;
                case 2: heroes = Heroes.getIntelligenceHeroes(); break;
                default: return;
            }
            heroDataPanes = new ArrayList<>();
            for (int i = 0; i < heroes.size(); ++i) {
                ++row;
                heroDataPanes.add(new HeroDataPane(heroes.get(i), contentPane, row));
            }
            contentPane.add(new Separator(), 0, ++row, 7, 1);

            for (Node node : contentPane.getChildren()) {
                GridPane.setHalignment(node, HPos.CENTER);
            }
            contentPane.setAlignment(Pos.CENTER);
        }
        setHgap(50);
        setAlignment(Pos.CENTER);
    }
}
