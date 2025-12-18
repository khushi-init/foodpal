package client.utils;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ToBeAddedIngredientUICtrl {

    @FXML
    private Label ingredientText;

    @FXML
    private HBox ingredientBox;

    @FXML
    private CheckBox checkoffBox;

    private Consumer<Long> deleteCheck;

    private Consumer<String> editInstruction;

    private BiConsumer<Long, Boolean> checkoffInstruction;

    private long index;

    public void setText(String text) {
        ingredientText.setText(text);
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public void setCheckedOff(boolean checked) {
        checkoffBox.setSelected(checked);
        if (checked) {
            ingredientText.setStyle("-fx-text-fill: #9ca3af;");
        }
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

    public void setCheckoffInstruction(BiConsumer<Long, Boolean> checkoffInstruction) {
        this.checkoffInstruction = checkoffInstruction;
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
        int labelIndex = ingredientBox.getChildren().indexOf(ingredientText);

        TextField textField = new TextField(ingredientText.getText().replace("• ", ""));
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setFocusTraversable(false);

        textField.setOnAction(e -> {
            String newText = textField.getText();
            ingredientText.setText("• " + newText);
            editInstruction.accept(newText);

            ingredientBox.getChildren().set(labelIndex, ingredientText);
        });

        ingredientBox.getChildren().set(labelIndex, textField);
        textField.requestFocus();
    }

}