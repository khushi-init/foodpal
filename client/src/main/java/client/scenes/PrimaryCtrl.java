package client.scenes;

import client.utils.searchUtils.Proposition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

public class PrimaryCtrl {

    private Stage primaryStage;

    // All new scenes go here
    private Scene recipesWindowScene;
    private Scene shoppingListWindow;
    private Scene ingredientsWindowScene;
    private Scene searchWindow;

    private RecipesWindowCtrl recipesWindowCtrl;

    /**
     * Initializes the primary control scene
     * @param primaryStage - The primary stage which gets injected
     * @param recipesWindow - The window containing the main recipe overview and sidebar list
     * @param shoppingList  - The window containing the shopping list
     * @param ingredientsWindow - The window containing the ingredients
     * @param searchWindow  - The window containing the advanced query generator
     */
    public void init(Stage primaryStage, Pair<RecipesWindowCtrl, Parent> recipesWindow,
                     Pair<ShoppingListCtrl, Parent> shoppingList,
                     Pair<IngredientsWindowCtrl, Parent> ingredientsWindow,
                     Pair<SearchWindowCtrl, Parent> searchWindow) {
        this.primaryStage = primaryStage;
        this.recipesWindowScene = new Scene(recipesWindow.getValue());
        this.shoppingListWindow = new Scene(shoppingList.getValue());
        this.ingredientsWindowScene = new Scene(ingredientsWindow.getValue());
        this.searchWindow = new Scene(searchWindow.getValue());

        this.recipesWindowCtrl = recipesWindow.getKey();

        recipesWindowScene.getStylesheets().add(
                getClass().getResource("/styles/styles.css").toExternalForm()
        );

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
        recipesWindowCtrl.startup();
        primaryStage.setScene(recipesWindowScene);
    }

    /**
     * Show method for the Shopping List scene.
     */
    public void showShoppingList() {
        primaryStage.setTitle("Shopping List"); // Subject to change
        primaryStage.setScene(shoppingListWindow);
    }

    /**
     * Show method for Ingredient window scene
     */
    public void showIngredientsWindow() {
        primaryStage.setTitle("Ingredients List");
        recipesWindowCtrl.shutdown();
        primaryStage.setScene(ingredientsWindowScene);
    }

    /**
     * Shows the search scene in a new window
     */
    public void showSearchWindow(){
        Stage newStage = new Stage();
        newStage.setTitle("Search Foodpal");
        newStage.setScene(searchWindow);
        newStage.show();
    }

    /**
     * Wrapper for performing a query and displaying the result in the recipes window
     * @param prop The query
     */
    public void applySearchToRecipesWindow(Proposition prop){
        recipesWindowCtrl.applyExternalSearch(prop);
    }

}
