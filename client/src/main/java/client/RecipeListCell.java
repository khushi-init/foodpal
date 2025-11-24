package client;

import commons.Recipe;
import javafx.scene.control.ListCell;

// This is the class responsible for displaying a recipe in the list view, making sure
// that the text in the list item is the name of the stored recipe
public class RecipeListCell extends ListCell<Recipe> {
    @Override
    protected void updateItem(Recipe recipe, boolean empty) {
        super.updateItem(recipe, empty);
        if (empty || recipe == null) {
            setText(null);
        } else {
            setText(recipe.getName());
        }
    }
}
