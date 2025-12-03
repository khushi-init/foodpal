package client.utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
public class RecipeIngredientUICtrl {

    @FXML
    private VBox recipeView;

    @FXML
    private Label ingredientText;

    private long index; // Someone will probably need this to handle deletes
    public void setText(String text) {
        ingredientText.setText(text);
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getIndex() {
        return this.index;
    }

    private Runnable deleteIngredient;

    private Runnable editIngredient;

    public void setDeleteIngredient(Runnable deleteIngredient) {
        this.deleteIngredient = deleteIngredient;
    }

    public void setEditIngredient(Runnable editIngredient) {
        this.editIngredient = editIngredient;
    }

    @FXML
    private void handleDeleteButton() {
        if(deleteIngredient != null) {
            deleteIngredient.run();
        }
    }
    @FXML
    private void handleEditButton() {
        if(editIngredient != null) {
            editIngredient.run();
        }
    }
}
