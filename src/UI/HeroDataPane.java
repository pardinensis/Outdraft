package UI;

import Backend.Hero;
import Backend.Heroes;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class HeroDataPane {

    private Hero hero;
    private HeroImage image;
    private Label nameLabel;
    private PositionButton[] positions;

    public HeroDataPane(Hero hero, GridPane grid, int row) {
        this.hero = hero;

        image = new HeroButton(hero.getName(), HeroButton.ImageType.HORIZONTAL);
        grid.add(image, 0, row);

//        nameLabel = new Label(hero.getName());
        //grid.add(nameLabel, 1, row);
//        nameLabel.getStyleClass().add("name-label");

        positions = new PositionButton[5];
        String positionsStr = hero.getPostionsString();
        for (int i = 0; i < 5; ++i) {
            int familiarity = positionsStr.charAt(i) - 'a';
            positions[i] = new PositionButton(i, familiarity, this::saveCallback);
            grid.add(positions[i], 2 + i, row);
        }
    }

    public void saveCallback() {
        char[] cs = new char[5];
        for (int i = 0; i < 5; ++i) {
            cs[i] = (char)('a' + positions[i].getFamiliarity());
        }
        hero.setPositionsString(new String(cs));
        Heroes.getInstance().writePositions();
    }
}
