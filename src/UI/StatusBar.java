package UI;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class StatusBar extends BorderPane{
    private Label statusLabel;

    public StatusBar() {
        getStyleClass().add("status-label");
        statusLabel = new Label("ready");
        statusLabel.getStyleClass().add("status-label");
        setCenter(statusLabel);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
    }

    public Label getStatusLabel() {
        return statusLabel;
    }
}
