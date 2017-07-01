package UI;

import javafx.scene.image.Image;

import java.io.File;
import java.util.HashMap;

public class ImageLoader {
    private static HashMap<String, Image> small = new HashMap<>();
    private static HashMap<String, Image> large = new HashMap<>();
    private static HashMap<String, Image> horizontal = new HashMap<>();

    private static Image get(String heroName, HashMap<String, Image> map, String directory) {
        if (heroName == null) {
            heroName = "empty";
        }
        Image image = map.get(heroName);
        if (image == null) {
            String newImgName = heroName.replace(' ', '_').toLowerCase();
            File imgFile = new File(directory + newImgName + ".png");
            image = new Image(imgFile.toURI().toString(), false);
            map.put(heroName, image);
        }
        return image;
    }

    public static Image getLargeImage(String heroName) {
//        get(heroName, small, "data/img/");
        return get(heroName, large, "data/img_large/");
    }

    public static Image getSmallImage(String heroName) {
//        get(heroName, large, "data/img_large/");
        return get(heroName, small, "data/img/");
    }

    public static Image getHorizontalImage(String heroName) {
        return get(heroName, horizontal, "data/img_horizontal/");
    }
}
