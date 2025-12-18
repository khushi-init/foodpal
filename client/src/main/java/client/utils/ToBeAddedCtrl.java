package client.utils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


import commons.RecipeIngredient;
import commons.ShoppingList;
import commons.ToBeAddedIngredient;
import commons.ToBeAddedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

public class ToBeAddedCtrl {
    @FXML
    private VBox ingredientsBox;

    private ToBeAddedList toBeAddedList = new ToBeAddedList();
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

    public void loadFromRecipe(List<RecipeIngredient> ris) {
        toBeAddedList.clear();

        for (RecipeIngredient ri : ris) {
            String text = ri.getIngredient().getName() + " (" + ri.getQuantity() + ")";
            toBeAddedList.addIngredient(text);
        }

        showRecipeIngredients();
    }


    /**
     * creating to be added list of ingredients from list
     * @throws IOException error
     */
    public void showRecipeIngredients() {
        ingredientsBox.getChildren().clear();

        for (int i = 0; i < toBeAddedList.getIngredients().size(); i++) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/client/modules/ToBeAddedIngredient.fxml")
                );
                Parent row = loader.load();
                ToBeAddedIngredientUICtrl ctrl = loader.getController();

                int index = i;
                String text = toBeAddedList.getIngredients().get(i).getText();

                ctrl.setText("• " + text);
                ctrl.setIndex(index);

                ctrl.setDeleteCheck(idx -> {
                    toBeAddedList.removeIngredient(index);
                    showRecipeIngredients();
                });

                ctrl.setEditInstruction(newText -> {
                    toBeAddedList.updateIngredient(index, newText);
                    showRecipeIngredients();
                });

                ingredientsBox.getChildren().add(row);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Button addButton = new Button("Add Ingredient");
        ingredientsBox.getChildren().add(addButton);

        addButton.setOnAction(e -> {
            Optional<Pair<String, String>> result = showIngredientPopUp();
            result.ifPresent(pair -> {
                String name = pair.getKey().trim();
                String amount = pair.getValue().trim();

                if (!name.isEmpty()) {
                    if (!amount.isEmpty()) {
                        toBeAddedList.addIngredient(name + " (" + amount + ")");
                    } else {
                        toBeAddedList.addIngredient(name);
                    }
                    showRecipeIngredients();
                }
            });
        });

    }

    private Optional<Pair<String, String>> showIngredientPopUp() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/client/modules/AddIngredientToList.fxml")
            );
            Parent root = loader.load();

            ShoppingListIngredientPopUpCtrl ctrl = loader.getController();

            Stage popUpStage = new Stage();
            popUpStage.initModality(Modality.APPLICATION_MODAL);
            popUpStage.setTitle("Add ingredient");
            popUpStage.setScene(new Scene(root));

            ctrl.setStage(popUpStage);

            popUpStage.showAndWait();

            if (ctrl.isOkClicked()) {
                return Optional.of(new Pair<>(ctrl.getName(), ctrl.getQuantity()));
            } else {
                return Optional.empty();
            }

        } catch (IOException e) {
            return Optional.empty();
        }
    }



}
