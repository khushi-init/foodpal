package client.scenes;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

public class PrimaryCtrl {

    private Stage primaryStage;

    // All new scenes go here
    private Scene recipesWindowScene;

    public void init(Stage primaryStage, Pair<RecipesWindowCtrl, Parent> recipesWindow) {
        this.primaryStage = primaryStage;
        this.recipesWindowScene = new Scene(recipesWindow.getValue());
        showRecipesWindow();
        primaryStage.show();
    }

    // Show methods are required to handle switching to each scene
    // All new scenes must get a show[Scene-name]() method

    public void showRecipesWindow() {
        primaryStage.setTitle("Cool recipe app"); // Subject to change
        primaryStage.setScene(recipesWindowScene);
    }
}
