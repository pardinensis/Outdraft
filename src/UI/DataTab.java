package UI;

import Backend.Hero;
import Backend.Heroes;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class DataTab extends GridPane implements HeroGridActionReceiver {
    private Label heroNameLabel;
    private HeroButton heroImage;
    private Label winRateLabel;

    private static final int N_MATCHUPS = 16;
    private static final int N_MATCHUP_ROWS = 1;
    private HeroButton[] goodAllyImages;
    private HeroButton[] goodEnemyImages;
    private HeroButton[] badEnemyImages;

    private HeroGrid heroGrid;

    public DataTab() {
        getStyleClass().add("tab-content");
        setAlignment(Pos.CENTER);

        GridPane infoPane = new GridPane();
        add(infoPane, 1, 0);

        heroNameLabel = new Label("");
        heroNameLabel.getStyleClass().add("info-label");
        infoPane.add(heroNameLabel, 0, 0);

        infoPane.setAlignment(Pos.CENTER);
        GridPane heroImageContainer = new GridPane();
        heroImage = new HeroButton("empty", HeroButton.ImageType.LARGE, false);
        heroImageContainer.add(heroImage, 0, 0);
        heroImageContainer.setAlignment(Pos.CENTER);
        infoPane.add(heroImageContainer, 0, 1);

        winRateLabel = new Label("");
        winRateLabel.getStyleClass().add("info-label");
        infoPane.add(winRateLabel, 0, 2);

        GridPane matchupPane = new GridPane();
        matchupPane.setAlignment(Pos.CENTER);
        matchupPane.getStyleClass().add("hero-grid-section");
        add(matchupPane, 2, 0);

        Label allyLabel = new Label("best with");
        matchupPane.add(allyLabel, 0, 0, N_MATCHUPS / N_MATCHUP_ROWS, 1);
        Label enemyLabel = new Label("best against");
        matchupPane.add(enemyLabel, 0, 1 + N_MATCHUP_ROWS, N_MATCHUPS / N_MATCHUP_ROWS, 1);
        Label enemyLabel2 = new Label("worst against");
        matchupPane.add(enemyLabel2, 0, 2 + 2 * N_MATCHUP_ROWS, N_MATCHUPS / N_MATCHUP_ROWS, 1);

        goodAllyImages = new HeroButton[N_MATCHUPS];
        goodEnemyImages = new HeroButton[N_MATCHUPS];
        badEnemyImages = new HeroButton[N_MATCHUPS];
        for (int i = 0; i < N_MATCHUPS; ++i) {
            goodAllyImages[i] = new HeroButton("empty", HeroButton.ImageType.HORIZONTAL, false);
            matchupPane.add(goodAllyImages[i], i % (N_MATCHUPS / N_MATCHUP_ROWS), 1 + i / (N_MATCHUPS / N_MATCHUP_ROWS));

            goodEnemyImages[i] = new HeroButton("empty", HeroButton.ImageType.HORIZONTAL, false);
            matchupPane.add(goodEnemyImages[i], i % (N_MATCHUPS / N_MATCHUP_ROWS), 2 + N_MATCHUP_ROWS + i / (N_MATCHUPS / N_MATCHUP_ROWS));

            badEnemyImages[i] = new HeroButton("empty", HeroButton.ImageType.HORIZONTAL, false);
            matchupPane.add(badEnemyImages[i], i % (N_MATCHUPS / N_MATCHUP_ROWS), 3 + 2 * N_MATCHUP_ROWS + i / (N_MATCHUPS / N_MATCHUP_ROWS));
        }

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setValignment(VPos.CENTER);
        add(separator, 0, 3, 4, 1);

        heroGrid = new HeroGrid(this, false);
        add(heroGrid, 0, 4, 4, 1);

    }

    @Override
    public void click(String heroName) {
    }

    @Override
    public void hover(String heroName) {
        heroNameLabel.setText(heroName);
        heroImage.setHeroName(heroName);

        Hero hero = Heroes.getInstance().getHeroByName(heroName);
        double winRate = hero.getWinRate();
        DecimalFormat df = new DecimalFormat("00.00");
        winRateLabel.setText(" " + df.format(winRate * 100) + "% ");
        winRateLabel.setTextFill(Tools.ratingToColor(winRate, 0.4, 0.6));

        ArrayList<Hero> heroes = new ArrayList<>();
        for (Hero h : Heroes.getInstance().getAllHeroes()) {
            if (h != null) {
                heroes.add(h);
            }
        }

        heroes.sort((Hero lhs, Hero rhs) -> Double.compare(hero.getSynergy(rhs), hero.getSynergy(lhs)));
        for (int i = 0; i < N_MATCHUPS; ++i) {
            goodAllyImages[i].setHeroName(heroes.get(i).getName());
        }

        heroes.sort((Hero lhs, Hero rhs) -> Double.compare(hero.getMatchup(rhs), hero.getMatchup(lhs)));
        for (int i = 0; i < N_MATCHUPS; ++i) {
            goodEnemyImages[i].setHeroName(heroes.get(i).getName());
            badEnemyImages[i].setHeroName(heroes.get(heroes.size() - 1 - i).getName());
        }
    }

    @Override
    public void hoverEnd() {

    }
}
