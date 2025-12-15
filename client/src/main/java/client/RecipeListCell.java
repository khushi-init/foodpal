package client;

import commons.Recipe;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

// This is the class responsible for displaying a recipe in the list view, making sure
// that the text in the list item is the name of the stored recipe
public class RecipeListCell extends ListCell<Recipe> {
    private List<Long> favIDs;
    private final Image star;
    private final double favIconHeight = 16.0;
    private final double favIconWidth = 16.0;

    /**
     * Constructor for recipeCell, with shiny attributes for favorite cells.
     * @param favIDs List of favorite ID recipes
     * @param star Star icon image
     */
    public RecipeListCell(List<Long> favIDs, Image star) {
        this.favIDs = favIDs;
        this.star = star;
    }


    @Override
    protected void updateItem(Recipe recipe, boolean empty) {
        super.updateItem(recipe, empty);
        if (empty || recipe == null) {
            setText(null);
            setGraphic(null);
            setStyle("");
        } else {
            setText(recipe.getName());
            if(favIDs.contains(recipe.getId())) {
                // Make recipe cell look dope when favorite
                ImageView icon = new ImageView(star);
                icon.setFitHeight(favIconHeight);
                icon.setFitWidth(favIconWidth);
                setGraphic(icon);
            }
            else {
                setGraphic(null);
            }

        }
    }
}
