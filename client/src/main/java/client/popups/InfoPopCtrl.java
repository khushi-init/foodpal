package client.popups;

import client.data.TranslationManager;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class InfoPopCtrl {

    @FXML
    private Label titleLabel;

    @FXML
    private TextArea infoLabel;

    @FXML
    private Button closeButton;

    private Stage stage;

    private TranslationManager tm;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setInfoLabelText(String infoText) {
        this.infoLabel.setText(infoText);
    }

    public void setCloseButtonText(String buttonText) {
        this.closeButton.setText(buttonText);
    }

    public void setTitleText(String titleText) {
        this.titleLabel.setText(titleText);
    }

    /**
     * If close is pressed, close is put to false and the stage is closed.
     */
    @FXML
    public void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }

    @Inject
    public InfoPopCtrl(TranslationManager tm) {
        this.tm= tm;
    }

    @FXML
    public void initialize() {
        titleLabel.setText(tm.tr("popup.info.title"));
        closeButton.setText(tm.tr("button.close"));
        tm.bundleProperty().addListener((obs, o, n) -> {
            titleLabel.setText(tm.tr("popup.info.title"));
            closeButton.setText(tm.tr("button.close"));
        });
    }
}
