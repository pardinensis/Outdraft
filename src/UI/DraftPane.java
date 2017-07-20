package UI;

import Backend.*;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;

public class DraftPane extends HBox {

    interface UpdateAction {
        void update(DraftOrder.State state);
    }

    private PickPane[] allyPickPanes;
    private PickPane[] enemyPickPanes;
    private HeroImage[] allyBanImages;
    private HeroImage[] enemyBanImages;

    private PickPane highlightedPickPane;
    private HeroImage highlightedImage;

    private WinRateLabel winRateLabel;

    private int allyPickCount;
    private int enemyPickCount;
    private int allyBanCount;
    private int enemyBanCount;

    private Outdraft outdraft;
    private DraftOrder draftOrder;
    private ArrayList<UpdateAction> updateActions;

    public void pick(String heroName, boolean ally) {
        if (ally) {
            allyPickPanes[allyPickCount++].setHeroName(heroName);
            outdraft.pickParty(heroName);
        }
        else {
            enemyPickPanes[enemyPickCount++].setHeroName(heroName);
            outdraft.pickEnemy(heroName);
        }
    }

    public void ban(String heroName, boolean ally) {
        Image image = ImageLoader.getHorizontalImage(heroName);
        if (ally) {
            allyBanImages[allyBanCount++].setImage(image);
        }
        else {
            enemyBanImages[enemyBanCount++].setImage(image);
        }

        outdraft.ban(heroName);
    }

    public void hover(String heroName) {
        setImages(heroName);

        if (heroName == null) {
            return;
        }

        final DraftOrder.State currentState = draftOrder.getCurrentState();
        PickAssignment pickAssignment;
        switch(draftOrder.getCurrentState()) {
            case PICK_ALLY:
            case BAN_ENEMY:
                PossiblePick possiblePick = outdraft.getPossiblePick(heroName);
                pickAssignment = possiblePick.getOwnPickAssignment();
                for (int i = 0; i < 5; ++i) {
                    allyPickPanes[i].setPickAssignment(pickAssignment);
                }
                winRateLabel.setWinRate(possiblePick.getWinRate());
                break;
            case PICK_ENEMY:
            case BAN_ALLY:
                PossiblePick possibleBan = outdraft.getPossibleBan(heroName);
                pickAssignment = possibleBan.getEnemyPickAssignment();
                for (int i = 0; i < 5; ++i) {
                    enemyPickPanes[i].setPickAssignment(pickAssignment);
                }
                winRateLabel.setWinRate(1 - possibleBan.getWinRate());
                break;
        }

        switch (draftOrder.getCurrentState()) {
            case PICK_ALLY:
            case BAN_ENEMY:
                setAdvantages(heroName, true);
                break;
            case PICK_ENEMY:
            case BAN_ALLY:
                setAdvantages(heroName, false);
                break;
        }
    }

    public void hoverEnd() {
        setImages(null);
        PossiblePick pickAssignment = outdraft.getCurrentState();
        winRateLabel.setWinRate(pickAssignment.getWinRate());
    }

    private void setImages(String heroName) {
        switch(draftOrder.getCurrentState()) {
            case PICK_ALLY:
            case PICK_ENEMY:
                highlightedPickPane.setHeroName(heroName);
                break;
            case BAN_ALLY:
            case BAN_ENEMY:
                Image image = ImageLoader.getHorizontalImage(heroName);
                if (highlightedImage != null)
                    highlightedImage.setImage(image);
                break;
        }
    }

    private void setAdvantages(String heroName, boolean ally) {
        if (heroName == null) {
            return;
        }

        Hero hero = Heroes.getHeroByName(heroName);
        if (ally) {
            for (int i = 0; i < 5; ++i) {
                String prevHeroName = allyPickPanes[i].getHeroName();
                if (prevHeroName != null) {
                    allyPickPanes[i].setAdvantage(hero.getSynergy(Heroes.getHeroByName(prevHeroName)));
                }
            }
            for (int i = 0; i < 5; ++i) {
                String prevHeroName = enemyPickPanes[i].getHeroName();
                if (prevHeroName != null) {
                    enemyPickPanes[i].setAdvantage(hero.getMatchup(Heroes.getHeroByName(prevHeroName)));
                }
            }
        }
        else {
            for (int i = 0; i < 5; ++i) {
                String prevHeroName = allyPickPanes[i].getHeroName();
                if (prevHeroName != null) {
                    allyPickPanes[i].setAdvantage(hero.getMatchup(Heroes.getHeroByName(prevHeroName)));
                }
            }
            for (int i = 0; i < 5; ++i) {
                String prevHeroName = enemyPickPanes[i].getHeroName();
                if (prevHeroName != null) {
                    enemyPickPanes[i].setAdvantage(hero.getSynergy(Heroes.getHeroByName(prevHeroName)));
                }
            }
        }
    }

    private void updateHighlighting() {
        if (highlightedImage != null) {
            highlightedImage.getStyleClass().remove("hovered");
            highlightedImage = null;
        }

        DraftOrder.State currentState = draftOrder.getCurrentState();
        switch(currentState) {
            case PICK_ALLY:
                highlightedPickPane = allyPickPanes[allyPickCount];
                highlightedImage = highlightedPickPane.getHeroButton();
                break;
            case PICK_ENEMY:
                highlightedPickPane = enemyPickPanes[enemyPickCount];
                highlightedImage = highlightedPickPane.getHeroButton();
                break;
            case BAN_ALLY:
                highlightedPickPane = null;
                highlightedImage = allyBanImages[allyBanCount];
                break;
            case BAN_ENEMY:
                highlightedPickPane = null;
                highlightedImage = enemyBanImages[enemyBanCount];
                break;
        }

        if (highlightedImage != null) {
            highlightedImage.getStyleClass().add("hovered");
        }
    }

    public void click(String heroName) {
        DraftOrder.State currentState = draftOrder.getCurrentState();
        switch (currentState) {
            case PICK_ALLY:
                pick(heroName, true);
                break;
            case PICK_ENEMY:
                pick(heroName, false);
                break;
            case BAN_ALLY:
                ban(heroName, true);
                break;
            case BAN_ENEMY:
                ban(heroName, false);
                break;
        }

        if (currentState == DraftOrder.State.FINISHED)
            return;

        draftOrder.proceed();
        updateHighlighting();

        runUpdateActions();
    }

    public void runUpdateActions() {
        for (UpdateAction action : updateActions) {
            action.update(draftOrder.getCurrentState());
        }
    }

    public void restart(boolean allPick, boolean allyFirstPick) {
        if (highlightedImage != null) {
            highlightedImage.getStyleClass().remove("hovered");
        }

        allyPickCount = 0;
        enemyPickCount = 0;
        allyBanCount = 0;
        enemyBanCount = 0;
        highlightedImage = null;

        Image banImage = ImageLoader.getHorizontalImage("empty");
        for (int i = 0; i < 5; ++i) {
            allyPickPanes[i].setHeroName(null);
            enemyPickPanes[i].setHeroName(null);
            allyBanImages[i].setImage(banImage);
            enemyBanImages[i].setImage(banImage);

        }

        String gameMode = (allPick) ? "All Pick" : "Captains Mode";
        draftOrder = new DraftOrder(gameMode, allyFirstPick);
        updateHighlighting();
        outdraft.restart();

        winRateLabel.setWinRate(0.5);

        runUpdateActions();
    }


    public DraftPane(Outdraft outdraft, WinRateLabel winRateLabel) {
        this.outdraft = outdraft;
        this.winRateLabel = winRateLabel;

        draftOrder = new DraftOrder("", true);
        updateActions = new ArrayList<>();

        setAlignment(Pos.CENTER);

        allyPickPanes = new PickPane[5];
        enemyPickPanes = new PickPane[5];
        allyBanImages = new HeroImage[5];
        enemyBanImages = new HeroImage[5];

        GridPane radiantPickGrid = new GridPane();
        GridPane direPickGrid = new GridPane();
        GridPane radiantBanGrid = new GridPane();
        GridPane direBanGrid = new GridPane();

        radiantPickGrid.getStyleClass().add("draft-pane");
        direPickGrid.getStyleClass().add("draft-pane");
        radiantBanGrid.getStyleClass().add("draft-pane-bans");
        direBanGrid.getStyleClass().add("draft-pane-bans");

        for (int i = 0; i < 5; ++i) {
            PickPane pickPane = new PickPane();
            radiantPickGrid.add(pickPane, 4 - i, 0);
            pickPane.getHeroButton().setHoverAction(() -> {
                setImages(null);
                setAdvantages(pickPane.getHeroName(), true);
            });
            allyPickPanes[i] = pickPane;
        }
        for (int i = 0; i < 5; ++i) {
            PickPane pickPane = new PickPane();
            direPickGrid.add(pickPane, i, 0);
            pickPane.getHeroButton().setHoverAction(() -> {
                setImages(null);
                setAdvantages(pickPane.getHeroName(), false);
            });
            enemyPickPanes[i] = pickPane;
        }
        for (int i = 0; i < 5; ++i) {
            HeroImage heroImage = new HeroImage(ImageLoader.getHorizontalImage("empty"));
            radiantBanGrid.add(heroImage, 0, i);
            allyBanImages[i] = heroImage;
        }
        for (int i = 0; i < 5; ++i) {
            HeroImage heroImage = new HeroImage(ImageLoader.getHorizontalImage("empty"));
            direBanGrid.add(heroImage, 0, i);
            enemyBanImages[i] = heroImage;
        }

        getChildren().add(radiantBanGrid);
        getChildren().add(radiantPickGrid);
        getChildren().add(direPickGrid);
        getChildren().add(direBanGrid);
    }

    public void addUpdateAction(UpdateAction action) {
        updateActions.add(action);
    }

}
