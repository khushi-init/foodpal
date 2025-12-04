package client.utils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.Consumer;

public class ShoppingListIngredientUICtrl {

    @FXML
    private Label ingredientText;

    @FXML
    private HBox ingredientBox;

    private Consumer<Long> deleteCheck;

    private Consumer<String> editInstruction;

    private long index;

    public void setText(String text) {
        ingredientText.setText(text);
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getIndex() {
        return this.index;
    }

    public void setDeleteCheck(Consumer<Long> deleteCheck) {
        this.deleteCheck = deleteCheck;
    }

    public void setEditInstruction(Consumer<String> editInstruction) {
        this.editInstruction = editInstruction;
    }

    @FXML
    private void handleDeleteButton() {
        if (deleteCheck != null) {
            deleteCheck.accept(index);
            ingredientBox.getChildren().clear();
        }
    }

    /**
     * method for editing ingredient in the shopping list
     */
    @FXML
    public void handleEditButton() {
        TextField textField = new TextField(ingredientText.getText().replace("- ", ""));
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setPromptText(ingredientText.getText());
        textField.setFocusTraversable(false);
        textField.setOnAction(actionEvent -> {
            editInstruction.accept(textField.getText());
            ingredientBox.getChildren().set(0, ingredientText);
        });
        ingredientBox.getChildren().set(0, textField);
        textField.requestFocus();
    }

}