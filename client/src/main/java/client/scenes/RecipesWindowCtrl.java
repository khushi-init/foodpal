package client.scenes;

import client.RecipeListCell;
import commons.Recipe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;


public class RecipesWindowCtrl {

    @FXML
    private ListView<Recipe> sidebarRecipeNamesList;

    // This list will store the recipe names, they're automatically displayed in the sidebar
    // A selection listener should be implemented to handle clicks + deletes of recipes later
    private final ObservableList<Recipe> recipes = FXCollections.observableArrayList();

    /**
     * Initializes the sidebar items (Recipe names) to track the ObervableList items
     */
    public void initialize() {
        sidebarRecipeNamesList.setItems(recipes);

        recipes.add(new Recipe("Eggs and bacon", null, null));

        sidebarRecipeNamesList.setCellFactory(lc -> new RecipeListCell());
    }
}
