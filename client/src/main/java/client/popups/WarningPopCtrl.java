package client.popups;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class WarningPopCtrl {

    @FXML
    private TextArea warningLabel;

    @FXML
    private Button buttonAnyway;

    private Stage stage;

    public boolean isAnyway() {
        return anyway;
    }

    private boolean anyway = false;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setButtonAnywayText(String text) {
        buttonAnyway.setText(text);
    }

    public void setWarningLabelText(String warningText) {
        this.warningLabel.setText(warningText);
    }

    /**
     * Initializes the controller by disabling mouse and keyboard interactions on the warning label
     */
    public void initialize() {
        // Block mouse selection
        warningLabel.addEventFilter(MouseEvent.ANY, e -> {
            if (e.getEventType() == MouseEvent.MOUSE_DRAGGED ||
                    e.getEventType() == MouseEvent.MOUSE_PRESSED) {
                e.consume();
            }
        });
        // Block keyboard events
        warningLabel.addEventFilter(KeyEvent.ANY, e -> {
            if (e.isShiftDown() || e.isShortcutDown()) {
                e.consume();
            }
        });
    }

    /**
     * If the 'anyway' button is pressed 'anyway' is set to true and the popup dies
     */
    @FXML
    public void handleAnyway() {
        anyway = true;
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * If the 'cancel' button is pressed 'anyway' is set to false and the popup dies
     */
    @FXML
    public void handleCancel() {
        anyway = false;
        if (stage != null) {
            stage.close();
        }
    }

}
