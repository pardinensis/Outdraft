package UI;

import javafx.scene.layout.GridPane;

import java.util.ArrayList;

public class HeroGrid extends GridPane {

    private DraftPane draftPane;
    private ArrayList<HeroButton> buttons;

    public HeroGrid(DraftPane draftPane) {
        this.draftPane = draftPane;
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

        addHero(strengthHeroes, "Abaddon");
        addHero(strengthHeroes, "Alchemist");
        addHero(strengthHeroes, "Axe");
        addHero(strengthHeroes, "Beastmaster");
        addHero(strengthHeroes, "Brewmaster");
        addHero(strengthHeroes, "Bristleback");
        addHero(strengthHeroes, "Centaur Warrunner");
        addHero(strengthHeroes, "Chaos Knight");
        addHero(strengthHeroes, "Clockwerk");
        addHero(strengthHeroes, "Doom");
        addHero(strengthHeroes, "Dragon Knight");
        addHero(strengthHeroes, "Earth Spirit");
        addHero(strengthHeroes, "Earthshaker");
        addHero(strengthHeroes, "Elder Titan");
        addHero(strengthHeroes, "Huskar");
        addHero(strengthHeroes, "Io");
        addHero(strengthHeroes, "Kunkka");
        addHero(strengthHeroes, "Legion Commander");
        addHero(strengthHeroes, "Lifestealer");
        addHero(strengthHeroes, "Lycan");
        addHero(strengthHeroes, "Magnus");
        addHero(strengthHeroes, "Omniknight");
        addHero(strengthHeroes, "Night Stalker");
        addHero(strengthHeroes, "Phoenix");
        addHero(strengthHeroes, "Pudge");
        addHero(strengthHeroes, "Sand King");
        addHero(strengthHeroes, "Slardar");
        addHero(strengthHeroes, "Spirit Breaker");
        addHero(strengthHeroes, "Sven");
        addHero(strengthHeroes, "Tidehunter");
        addHero(strengthHeroes, "Timbersaw");
        addHero(strengthHeroes, "Tiny");
        addHero(strengthHeroes, "Treant Protector");
        addHero(strengthHeroes, "Tusk");
        addHero(strengthHeroes, "Underlord");
        addHero(strengthHeroes, "Undying");
        addHero(strengthHeroes, "Wraith King");

        addHero(agilityHeroes, "Anti-Mage");
        addHero(agilityHeroes, "Arc Warden");
        addHero(agilityHeroes, "Bloodseeker");
        addHero(agilityHeroes, "Bounty Hunter");
        addHero(agilityHeroes, "Broodmother");
        addHero(agilityHeroes, "Clinkz");
        addHero(agilityHeroes, "Drow Ranger");
        addHero(agilityHeroes, "Ember Spirit");
        addHero(agilityHeroes, "Faceless Void");
        addHero(agilityHeroes, "Gyrocopter");
        addHero(agilityHeroes, "Juggernaut");
        addHero(agilityHeroes, "Lone Druid");
        addHero(agilityHeroes, "Luna");
        addHero(agilityHeroes, "Medusa");
        addHero(agilityHeroes, "Meepo");
        addHero(agilityHeroes, "Mirana");
        addHero(agilityHeroes, "Monkey King");
        addHero(agilityHeroes, "Morphling");
        addHero(agilityHeroes, "Naga Siren");
        addHero(agilityHeroes, "Nyx Assassin");
        addHero(agilityHeroes, "Phantom Assassin");
        addHero(agilityHeroes, "Phantom Lancer");
        addHero(agilityHeroes, "Razor");
        addHero(agilityHeroes, "Riki");
        addHero(agilityHeroes, "Shadow Fiend");
        addHero(agilityHeroes, "Slark");
        addHero(agilityHeroes, "Sniper");
        addHero(agilityHeroes, "Spectre");
        addHero(agilityHeroes, "Templar Assassin");
        addHero(agilityHeroes, "Terrorblade");
        addHero(agilityHeroes, "Troll Warlord");
        addHero(agilityHeroes, "Ursa");
        addHero(agilityHeroes, "Vengeful Spirit");
        addHero(agilityHeroes, "Venomancer");
        addHero(agilityHeroes, "Viper");
        addHero(agilityHeroes, "Weaver");

        addHero(intelligenceHeroes, "Ancient Apparition");
        addHero(intelligenceHeroes, "Bane");
        addHero(intelligenceHeroes, "Batrider");
        addHero(intelligenceHeroes, "Chen");
        addHero(intelligenceHeroes, "Crystal Maiden");
        addHero(intelligenceHeroes, "Dark Seer");
        addHero(intelligenceHeroes, "Dazzle");
        addHero(intelligenceHeroes, "Death Prophet");
        addHero(intelligenceHeroes, "Disruptor");
        addHero(intelligenceHeroes, "Enchantress");
        addHero(intelligenceHeroes, "Enigma");
        addHero(intelligenceHeroes, "Invoker");
        addHero(intelligenceHeroes, "Jakiro");
        addHero(intelligenceHeroes, "Keeper of the Light");
        addHero(intelligenceHeroes, "Leshrac");
        addHero(intelligenceHeroes, "Lich");
        addHero(intelligenceHeroes, "Lina");
        addHero(intelligenceHeroes, "Lion");
        addHero(intelligenceHeroes, "Nature's Prophet");
        addHero(intelligenceHeroes, "Necrophos");
        addHero(intelligenceHeroes, "Ogre Magi");
        addHero(intelligenceHeroes, "Oracle");
        addHero(intelligenceHeroes, "Outworld Devourer");
        addHero(intelligenceHeroes, "Puck");
        addHero(intelligenceHeroes, "Pugna");
        addHero(intelligenceHeroes, "Queen of Pain");
        addHero(intelligenceHeroes, "Rubick");
        addHero(intelligenceHeroes, "Shadow Demon");
        addHero(intelligenceHeroes, "Shadow Shaman");
        addHero(intelligenceHeroes, "Silencer");
        addHero(intelligenceHeroes, "Skywrath Mage");
        addHero(intelligenceHeroes, "Storm Spirit");
        addHero(intelligenceHeroes, "Techies");
        addHero(intelligenceHeroes, "Tinker");
        addHero(intelligenceHeroes, "Visage");
        addHero(intelligenceHeroes, "Warlock");
        addHero(intelligenceHeroes, "Windranger");
        addHero(intelligenceHeroes, "Winter Wyvern");
        addHero(intelligenceHeroes, "Witch Doctor");
        addHero(intelligenceHeroes, "Zeus");

    }

    private void addHero(GridPane pane, String heroName) {
        final int width = 21;
        int index = pane.getChildren().size();
        HeroButton heroButton = new HeroButton(heroName, HeroButton.ImageType.HORIZONTAL);
        heroButton.setClickAction(() -> draftPane.click(heroButton.getHeroName()));
        heroButton.setHoverAction(() -> draftPane.hover(heroName));
        heroButton.setHoverEndAction(draftPane::hoverEnd);
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
