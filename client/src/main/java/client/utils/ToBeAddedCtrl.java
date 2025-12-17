package client.utils;

import java.io.IOException;
import java.util.List;

import commons.RecipeIngredient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ToBeAddedCtrl {
    @FXML
    private VBox ingredientsBox;


    private boolean okClicked = false;

    @FXML
    private void handleOk() {
        okClicked = true;
        close();
    }

    @FXML
    private void handleCancel(){
        okClicked = false;
        close();
    }

    private void close() {
        ingredientsBox.getScene().getWindow().hide();
    }

    /**
     * creating to be added list of ingredients from list
     * @param recipeIngredients list of all ingredients from selected recipe
     * @throws IOException error
     */
    public void showRecipeIngredients(List<RecipeIngredient> recipeIngredients) throws IOException {
        ingredientsBox.getChildren().clear();
        for (int i = 0; i < recipeIngredients.size(); i++) {
            RecipeIngredient ri = recipeIngredients.get(i);
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/client/modules/ShoppingListIngredient.fxml")
            );

            Parent row = loader.load();
            ShoppingListIngredientUICtrl rowCtrl = loader.getController();
            rowCtrl.setText("• " + ri.getIngredient().getName() + " " + ri.getQuantity());

            rowCtrl.setIndex(i);

            rowCtrl.setDeleteCheck(idx -> {
                ingredientsBox.getChildren().remove(row);
            });

            rowCtrl.setEditInstruction(newName -> {
                rowCtrl.setText("• " + newName);
            });

            ingredientsBox.getChildren().add(row);
        }
    }

}
