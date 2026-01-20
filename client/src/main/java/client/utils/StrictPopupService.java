package client.utils;

import client.MyFXML;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

public class StrictPopupService <C>{

    private Stage lockStage;
    private MyFXML fxml;
    private C controller;

    /**
     * Generic class for creating APPLICATION_MODAL popups
     * @param fxml - Injected MyFXML object
     */
    @Inject
    public StrictPopupService(MyFXML fxml) {
        this.fxml = fxml;
    }

    /**
     * Displays the desired popup
     * @param controllerType - The controller of the popup
     * @param fxmlPath - The path to the fxml file of the popup
     */
    public void showStrictPopup(Class<C> controllerType, String... fxmlPath) {
        if (lockStage != null && lockStage.isShowing()) return;

        Platform.runLater(() -> {
            Pair<C, Parent> popUpPair = fxml.load(controllerType, fxmlPath);

            Parent root = popUpPair.getValue();
            controller = popUpPair.getKey();

            lockStage = new Stage();
            lockStage.initModality(Modality.APPLICATION_MODAL);
            lockStage.setScene(new Scene(root));
            lockStage.show();
        });
    }

    /**
     * If the popup is currently displayed, it hides it
     */
    public void hideStrictPopup() {
        Platform.runLater(() -> {
            if (lockStage != null) {
                lockStage.close();
                lockStage = null;
            }
        });
    }

    public boolean isShowing() {
        return lockStage != null && lockStage.isShowing();
    }

    public C getController() {
        return controller;
    }
}
