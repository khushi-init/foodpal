package client.popups;

import client.data.TranslationManager;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class WarningPopCtrl {

    @FXML private Label titleLabel;
    @FXML private TextArea warningLabel;
    @FXML private Button buttonAnyway;
    @FXML private Button cancelButton;

    private Stage stage;

    public boolean isAnyway() {
        return anyway;
    }

    private boolean anyway = false;

    private TranslationManager tm;

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
    @FXML
    public void initialize() {
        titleLabel.setText(tm.tr("popup.warning.title"));
        cancelButton.setText(tm.tr("button.cancel"));
        buttonAnyway.setText(tm.tr("button.delete.anyway"));

        warningLabel.addEventFilter(MouseEvent.ANY, e -> {
            if (e.getEventType() == MouseEvent.MOUSE_DRAGGED ||
                    e.getEventType() == MouseEvent.MOUSE_PRESSED) {
                e.consume();
            }
        });
        warningLabel.addEventFilter(KeyEvent.ANY, e -> {
            if (e.isShiftDown() || e.isShortcutDown()) e.consume();
        });

        tm.bundleProperty().addListener((obs, o, n) -> {
            titleLabel.setText(tm.tr("popup.warning.title"));
            cancelButton.setText(tm.tr("button.cancel"));
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

    @Inject
    public WarningPopCtrl(TranslationManager tm) {
        this.tm = tm;
    }

}
