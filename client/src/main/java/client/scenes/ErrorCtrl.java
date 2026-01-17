package client.scenes;
import java.io.FileWriter;
import java.io.PrintWriter;

import client.MyFXML;
import client.data.TranslationManager;
import client.popups.InfoPopCtrl;
import client.popups.WarningPopCtrl;
import com.google.inject.Inject;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.scene.layout.Region;

public class ErrorCtrl {

    private final MyFXML fxml;
    private TranslationManager tm;

    /**
     * Constructor for the ErrorCtrl class
     * @param fxml - Injected fxml module
     *             @param tm the translation manager used to localize UI text
     */
    @Inject
    public ErrorCtrl(MyFXML fxml, TranslationManager tm) {
        this.fxml = fxml;
        this.tm = tm;
    }

    /**
    * Can be called to display a proper error message when the server is unavailable
    **/
    public void showServerUnavailableError() {
        showErrorPopup(
                tm.tr("error.title"),
                tm.tr("error.server.header"),
                tm.tr("error.server.message")
        );
    }

    /**
    * Makes an error popup and writes the stacktrace to stacktrace.txt
    * @param e The exception to be displayed
    **/
    public void showGenericError(Exception e){
        boolean writeStackTraceSucces = true;
        try( FileWriter fileWriter = new FileWriter("stacktrace.txt")){
            PrintWriter printWriter = new PrintWriter(fileWriter);
            e.printStackTrace(printWriter);
        } catch (Exception e2){
            writeStackTraceSucces = false;
        }

        String errorContextText = tm.tr(
                writeStackTraceSucces
                        ? "error.generic.context.withStacktrace"
                        : "error.generic.context.noStacktrace"
        );

        errorContextText += ":\n" + e.toString();

        showErrorPopup(
                tm.tr("error.title"),
                tm.tr("error.generic.header"),
                errorContextText
        );
    }

    /**
     * Initializes an error pupup window
     * @param message The String to be displayed
     */
    public void showGenericError(String message) {
        showErrorPopup(
                tm.tr("error.title"),
                "",
                message
        );
    }

    /**
     * Shows an error popup with the specified contents
     * @param title Title of the window
     * @param headerText The text that comes left of the red cross (well technically it's a red box with a white cross in it)
     * @param message The main message
     */
    public void showErrorPopup(String title, String headerText, String message){
        Alert alert = new Alert(AlertType.ERROR);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a warning popup
     * @param warningText - The description of the warning
     * @param anywayText - The text of the 'Anyway' button, basically the one the user presses if they chose to proceed by
     *                   disregarding the warning
     * @param title - The popup window title
     * @return - Whether 'anyway' was pressed
     */
    public boolean displayWarning(String warningText, String anywayText, String title) {
        Pair<WarningPopCtrl, Parent> warningPopupPair = fxml.load(WarningPopCtrl.class, "client", "modules", "Warning.fxml");

        Parent root = warningPopupPair.getValue();
        WarningPopCtrl popUpCtrl = warningPopupPair.getKey();

        Stage popUpStage = new Stage();
        popUpStage.setTitle(title); // Will change to language thing later
        popUpStage.setScene(new Scene(root));

        popUpCtrl.setStage(popUpStage);
        popUpCtrl.setButtonAnywayText(anywayText);
        popUpCtrl.setWarningLabelText(warningText);

        popUpStage.showAndWait();

        return popUpCtrl.isAnyway();
    }

    /**
     * Display an info message.
     * @param infoText        - The text of info.
     * @param closeButtonText - The text of the button.
     * @param title           - The text of the title.
     */
    public void displayInfo(String infoText, String closeButtonText, String title) {
        Pair<InfoPopCtrl, Parent> infoPopUpPair = fxml.load
                (InfoPopCtrl.class, "client", "modules", "Info.fxml");

        Parent root = infoPopUpPair.getValue();
        InfoPopCtrl popUpCtrl = infoPopUpPair.getKey();

        Stage popUpStage = new Stage();
        popUpStage.setTitle(title);
        popUpStage.setScene(new Scene(root));

        popUpCtrl.setStage(popUpStage);
        popUpCtrl.setInfoLabelText(infoText);
        popUpCtrl.setCloseButtonText(closeButtonText);
        popUpCtrl.setTitleText(title);

        popUpStage.showAndWait();
    }
}
