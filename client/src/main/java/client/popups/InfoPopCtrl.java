package client.popups;

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

    private boolean close = false;


    public boolean isClose () {
        return close;
    }

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
        close = false;
        if (stage != null) {
            stage.close();
        }
    }
}
