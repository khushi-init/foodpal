package client.scenes;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

public class PrimaryCtrl {

    private Stage primaryStage;

    // All new scenes go here
    private Scene recipesWindowScene;

    /**
     * Initializes the primary control scene
     * @param primaryStage - The primary stage which gets injected
     * @param recipesWindow - The window containing the main recipe overview and sidebar list
     * @param errorCtrl - The error controller which gets injected
     */
    public void init(Stage primaryStage, Pair<RecipesWindowCtrl, Parent> recipesWindow) {
        this.primaryStage = primaryStage;
        this.recipesWindowScene = new Scene(recipesWindow.getValue());
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

}
