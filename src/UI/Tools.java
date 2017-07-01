package UI;

import javafx.scene.paint.Color;

/**
 * Created by pardinensis on 01.07.17.
 */
public class Tools {

    public static Color ratingToColor(double value, double badValue, double goodValue) {
        final Color good = new Color(0, 1, 0, 1);
        final Color neutral = new Color(1, 1, 1, 1);
        final Color bad = new Color(1, 0, 0, 1);

        final double intervalHalf = (goodValue - badValue) * 0.5;
        final double neutralValue = badValue + intervalHalf;

        Color mixed;
        if (value < neutralValue) {
            mixed = neutral.interpolate(bad, (neutralValue - value) / intervalHalf);
        }
        else {
            mixed = neutral.interpolate(good, (value - neutralValue) / intervalHalf);
        }
        return mixed;
    }
}
