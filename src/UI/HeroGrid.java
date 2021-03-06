package UI;

import Backend.Hero;
import Backend.Heroes;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

public class HeroGrid extends GridPane {

    private HeroGridActionReceiver receiver;
    private ArrayList<HeroButton> buttons;
    private boolean makeUnavailableOnClick;

    public HeroGrid(HeroGridActionReceiver receiver, boolean makeUnavailableOnClick) {
        this.receiver = receiver;
        this.makeUnavailableOnClick = makeUnavailableOnClick;
        buttons = new ArrayList<>();

        getStyleClass().add("hero-grid");
        GridPane strengthHeroes = new GridPane();
        GridPane agilityHeroes = new GridPane();
        GridPane intelligenceHeroes = new GridPane();
        strengthHeroes.getStyleClass().add("hero-grid-section");
        agilityHeroes.getStyleClass().add("hero-grid-section");
        intelligenceHeroes.getStyleClass().add("hero-grid-section");
        add(strengthHeroes, 0, 0);
        add(agilityHeroes, 0, 1);
        add(intelligenceHeroes, 0, 2);

        ArrayList<Hero> strengthHeroList = Heroes.getInstance().getStrengthHeroes();
        for (Hero hero : strengthHeroList) {
            addHero(strengthHeroes, hero.getName());
        }
        ArrayList<Hero> agilityHeroList = Heroes.getInstance().getAgilityHeroes();
        for (Hero hero : agilityHeroList) {
            addHero(agilityHeroes, hero.getName());
        }
        ArrayList<Hero> intelligenceHeroList = Heroes.getInstance().getIntelligenceHeroes();
        for (Hero hero : intelligenceHeroList) {
            addHero(intelligenceHeroes, hero.getName());
        }

    }

    private void addHero(GridPane pane, String heroName) {
        final int width = 21;
        int index = pane.getChildren().size();
        HeroButton heroButton = new HeroButton(heroName, HeroButton.ImageType.HORIZONTAL, makeUnavailableOnClick);
        heroButton.setClickAction(() -> receiver.click(heroButton.getHeroName()));
        heroButton.setHoverAction(() -> receiver.hover(heroName));
        heroButton.setHoverEndAction(receiver::hoverEnd);
        buttons.add(heroButton);
        pane.add(heroButton, index % width, index / width);
    }

    public void reset() {
        for (HeroButton button : buttons) {
            button.setAvailable(true);
        }
    }

    public void setUnavailable(String heroName) {
        for (HeroButton button : buttons) {
            if (button.getHeroName().equals(heroName)) {
                button.setAvailable(false);
                break;
            }
        }
    }
}
