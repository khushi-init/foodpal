package client;

import commons.Ingredient;
import javafx.scene.control.ListCell;

public class IngredientListCell extends ListCell<Ingredient> {
    @Override
    protected void updateItem(Ingredient ingredient, boolean empty) {
        super.updateItem(ingredient, empty);

        //If the cell is empty or the item is null, set no text/graphic.
        if (empty || ingredient == null) {
            setText(null);
            setGraphic(null);
        }
        else {
            setText(ingredient.getName());
        }
    }
}
