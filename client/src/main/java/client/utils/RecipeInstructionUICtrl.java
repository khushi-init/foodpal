package client.utils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class RecipeInstructionUICtrl {

    @FXML
    private VBox recipeView;

    @FXML
    private Label instructionText;

    private long index; // Someone will probably need this to handle deletes

    public void setText(String text) {
        instructionText.setText(text);
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getIndex() {
        return this.index;
    }
}
