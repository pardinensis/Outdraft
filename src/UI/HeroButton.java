package UI;

import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

public class HeroButton extends HeroImage {
    enum ImageType {
        SMALL,
        HORIZONTAL,
        LARGE
    }

    private Runnable hoverAction, hoverEndAction;
    private Rectangle overlay;
    private boolean available;
    private String heroName;
    private ImageType imageType;
    private boolean highlighted;

    private static Image getImage(String heroName, ImageType imageType) {
        switch(imageType) {
            case SMALL:
                return ImageLoader.getSmallImage(heroName);
            case HORIZONTAL:
                return ImageLoader.getHorizontalImage(heroName);
            case LARGE:
                return ImageLoader.getLargeImage(heroName);
        }
        return null;
    }

    public HeroButton(String heroName, ImageType imageType) {
        super(getImage(heroName, imageType));

        this.heroName = heroName;
        this.imageType = imageType;
        this.highlighted = false;

        overlay = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
        overlay.getStyleClass().add("available");
        getChildren().add(overlay);

        setOnMouseEntered((MouseEvent me) -> {
            if (available) {
                highlighted = getStyleClass().remove("highlighted");
                getStyleClass().add("hovered");
                if (hoverAction != null) {
                    hoverAction.run();
                }
            }
        });
        setOnMouseExited((MouseEvent me) -> {
            getStyleClass().remove("hovered");
            if (highlighted) {
                getStyleClass().add("highlighted");
                highlighted = false;
            }
            if (hoverEndAction != null) {
                hoverEndAction.run();
            }
        });

        available = true;
    }

    public void setClickAction(Runnable action) {
        setOnMouseClicked((MouseEvent e) -> {
            if (available) {
                highlighted = false;
                setAvailable(false);
                action.run();
                if (hoverAction != null) {
                    hoverAction.run();
                }
            }
        });
    }

    public void setHoverAction(Runnable action) {
        hoverAction = action;
    }

    public void setHoverEndAction(Runnable action) {
        hoverEndAction = action;
    }

    public void setAvailable(boolean available) {
        if (this.available == available)
            return;

        this.available = available;
        if (available) {
            overlay.getStyleClass().remove("unavailable");
            overlay.getStyleClass().add("available");
        }
        else {
            overlay.getStyleClass().remove("available");
            overlay.getStyleClass().add("unavailable");
        }
    }

    public void setHeroName(String heroName) {
        this.heroName = heroName;
        setImage(getImage(heroName, imageType));
    }

    public String getHeroName() {
        return heroName;
    }
}
