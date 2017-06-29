package UI;

import javafx.scene.control.Label;

import java.text.DecimalFormat;

public class WinRateLabel extends Label {

    public WinRateLabel() {
        setText(" 00.00% ");
        getStyleClass().add("win-rate-label");
    }

    public void setWinRate(double winRate) {
        DecimalFormat df = new DecimalFormat("00.00");
        setText(" " + df.format(winRate * 100) + "% ");
    }
}
