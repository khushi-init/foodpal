package client.popups;

import client.data.TranslationManager;
import com.google.inject.Inject;
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

    private TranslationManager tm;

    @Inject
    public ServerDisconnectPopupCtrl(TranslationManager tm) {
        this.tm = tm;
    }

    public void initialize() {
        titleLabel.setText(tm.tr("warning.server.disconnect.title"));
        reconnectionAttemptLabel.setText(tm.tr("warning.server.disconnect.subLabel"));
        abortButton.setText(tm.tr("warning.server.abortButton"));
    }

    /**
     * Functionality of the "Abort" button. Kills the application
     */
    @FXML
    public void killApplication() {
        Platform.exit();
        System.exit(0);
    }

}
