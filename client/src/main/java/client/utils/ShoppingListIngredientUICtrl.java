package client.utils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import java.util.function.Consumer;

public class ShoppingListIngredientUICtrl {

    @FXML
    private Label ingredientText;

    @FXML
    private HBox ingredientBox;

    private Runnable deleteCheck;

    private Consumer<String> editInstruction;

    private long index;

    public void setText(String text){
        ingredientText.setText(text);
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getIndex() {
        return this.index;
    }

    public void setDeleteCheck(Runnable deleteCheck){
        this.deleteCheck = deleteCheck;
    }

    public void setEditInstruction(Consumer<String> editInstruction) {
        this.editInstruction = editInstruction;
    }

    @FXML
    private void handleDeleteButton(){
        if(deleteCheck != null){
            deleteCheck.run();
        }
    }

    @FXML
    public void handleEditButton() {
        // TODO
    }
}
