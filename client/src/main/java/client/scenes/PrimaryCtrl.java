package client.scenes;

import client.utils.ErrorCtrl;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import client.utils.ErrorCtrl;

public class PrimaryCtrl {

    private Stage primaryStage;

    // All new scenes go here
    private Scene recipesWindowScene;

    private ErrorCtrl errorCtrl;

    /**
     * Initializes the primary control scene
     * @param primaryStage - The primary stage which gets injected
     * @param recipesWindow - The window containing the main recipe overview and sidebar list
     * @param errorCtrl - The error controller which gets injected
     */
    public void init(Stage primaryStage, Pair<RecipesWindowCtrl, Parent> recipesWindow, ErrorCtrl errorCtrl) {
        this.primaryStage = primaryStage;
        this.recipesWindowScene = new Scene(recipesWindow.getValue());
        this.errorCtrl = errorCtrl;
        showRecipesWindow();
        primaryStage.show();
    }

    // Show methods are required to handle switching to each scene
    // All new scenes must get a show[Scene-name]() method

    /**
     * Show method for the recipeWindow. It switches the javafx scene to the recipeWindow Scene
     */
    public void showRecipesWindow() {
        primaryStage.setTitle("Cool recipe app"); // Subject to change
        primaryStage.setScene(recipesWindowScene);
    }

    /**
     * Wraps the ErrorCtrl method with the same name
     */
    public void showServerUnavailableError(){
        errorCtrl.showServerUnavailableError();
    }

    /**
     * Wraps the ErrorCtrl method with the same name
     * @param e The exception to be displayed in the popup
     */
    public void showGenericError(Exception e){
        errorCtrl.showGenericError(e);
    }

    /**
     * Wraps the ErrorCtrl method with the same name
     * @param message Message to be displayed in the error popup
     */
    public void showGenericError(String message){
        errorCtrl.showGenericError(message);
    }

}
