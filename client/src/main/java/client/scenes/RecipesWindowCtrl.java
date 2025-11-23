package client.scenes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;


public class RecipesWindowCtrl {

    @FXML
    private ListView<String> sidebarRecipeNamesList;

    // This list will store the recipe names, they're automatically displayed in the sidebar
    // A selection listener should be implemented to handle clicks + deletes of recipes later
    private final ObservableList<String> items = FXCollections.observableArrayList();

    public void initialize() {
        sidebarRecipeNamesList.setItems(items);

        items.addAll("Recipe 1", "Recipe 2", "Recipe 3", "Recipe tree");
    }
}
