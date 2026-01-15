package client.scenes;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class RecipeInstructionUICtrl {

    @FXML
    private HBox instructionBox;

    @FXML
    private VBox recipeView;

    @FXML
    private Label instructionText;

    private Runnable deleteCheck;

    private Consumer<String> editInstruction;

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

    public void setDeleteCheck(Runnable deleteCheck) {
        this.deleteCheck = deleteCheck;
    }
    public void setEditInstruction(Consumer<String> editInstruction) {
        this.editInstruction = editInstruction;
    }

    @FXML
    private void handleDeleteButton() {
        if (deleteCheck != null) {
            deleteCheck.run();
        }
    }

    /**
     * Handles the edit action for instruction.
     */
    @FXML
    public void handleEditButton() {
        TextField textField = new TextField(instructionText.getText().replace("- ", ""));
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setPromptText(instructionText.getText());
        textField.setFocusTraversable(false);
        textField.setOnAction(actionEvent -> {
            editInstruction.accept(textField.getText());
            instructionBox.getChildren().set(0, instructionText);
        });
        instructionBox.getChildren().set(0, textField);
        textField.requestFocus();
    }
}
