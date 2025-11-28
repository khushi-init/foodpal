package client.utils;
import java.io.FileWriter;
import java.io.PrintWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ErrorCtrl {
    /**
    * Can be called to display a proper error message when the server is unavailable
    **/
    public void showServerUnavailableError(){
        showErrorPopup("Error",
                "Server unavailable",
                "The server is offline or dealing with a DDOS attack"
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

        String errorContextText = "The following exception was raised ";
        if(writeStackTraceSucces){
            errorContextText += "(for the full stack trace, see stacktrace.txt)";
        } else {
            errorContextText += "(stack trace unavailable)";
        }
        errorContextText += ":\n";
        errorContextText += e.toString();

        showErrorPopup("Error",
                "Uh oh, that's an error.",
                errorContextText
        );
    }

    /**
     * Initializes an error pupup window
     * @param message The String to be displayed
     */
    public void showGenericError(String message){
        showErrorPopup("Error",
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
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
