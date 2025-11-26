package client.utils;
import java.io.FileWriter;
import java.io.PrintWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

// import client.utils.ServerUtils;

public class ErrorCtrl {
    /**
    * Can be called to display a proper error message when the server is unavailable
    **/
    public void showServerUnavailableError(){
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Server unavailable");
        alert.setContentText("The server is offline or dealing with a DDOS attack");
        alert.showAndWait();
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

        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Uh oh, that's an error.");

        String errorContextText = "The following exception was raised ";
        if(writeStackTraceSucces){
            errorContextText += "(for the full stack trace, see stacktrace.txt)";
        } else {
            errorContextText += "(stack trace unavailable)";
        }
        errorContextText += ":\n";
        errorContextText += e.toString();

        alert.setContentText(errorContextText);
        alert.showAndWait();
    }

    /**
     * Initializes an error pupup window
     * @param message The String to be displayed
     */
    public void showGenericError(String message){
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("");
        alert.setContentText(message);
        alert.showAndWait();
        showErrorPopup("Error",
                "",
                message
        );
    }

    public void showErrorPopup(String title, String headerText, String message){
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
