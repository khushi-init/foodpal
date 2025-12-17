package client.utils;

import client.Main;
import commons.Ingredient;
import commons.RecipeIngredient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.util.Pair;

import java.io.IOException;
import java.util.List;

public class ToBeAddedCtrl {
    @FXML
    private VBox ingredientsBox;

    private Stage stage;

    private boolean okClicked = false;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleOk(){
        okClicked = true;

        if (stage != null){
            stage.close();
        }
    }

    @FXML
    private void handleCancel(){
        okClicked = false;

        if (stage != null) {
            stage.close();
        }
    }

    public boolean isOkClicked(){
        return okClicked;
    }

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

            ingredientsBox.getChildren().add(row);
        }
    }

    @FXML
    private void onDone() {
        stage.close();
    }
}
