package client.utils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class RecipeInstructionUICtrl {

    @FXML
    private VBox recipeView;

    @FXML
    private Label instructionText;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editButton;

    private Runnable clickCheck;

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

    public void setClickCheck(Runnable clickCheck) {
        this.clickCheck = clickCheck;
    }

    @FXML
    private void handleDeleteButton() {
        if (clickCheck != null) {
            clickCheck.run();
        }
    }
}
