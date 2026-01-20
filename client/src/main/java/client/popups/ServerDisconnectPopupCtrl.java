package client.popups;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class ServerDisconnectPopupCtrl {

    @FXML
    private Label titleLabel;

    @FXML
    private Label reconnectionAttemptLabel;

    @FXML
    private Button abortButton;

    /**
     * Functionality of the "Abort" button. Kills the application
     */
    @FXML
    public void killApplication() {
        Platform.exit();
        System.exit(0);
    }

}
