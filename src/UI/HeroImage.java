package UI;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class HeroImage extends StackPane {
    protected ImageView imageView;

    public HeroImage(Image image) {
        imageView = new ImageView(image);
        imageView.setFitWidth(image.getWidth());
        imageView.setFitHeight(image.getHeight());
        Rectangle clip = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        imageView.setClip(clip);
        getChildren().add(imageView);
        getStyleClass().add("hero-button");
    }

    public void setImage(Image image) {
        imageView.setImage(image);
    }
}
