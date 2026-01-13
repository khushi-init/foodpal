package client.scenes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import client.popups.ShoppingListIngredientPopUpCtrl;
import client.popups.ShoppingListIngredientUICtrl;
import commons.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

public class ToBeAddedCtrl {
    @FXML
    private VBox ingredientsBox;
    private String sourceRecipeName;
    private final List<ShoppingListIngredient> toBeAddedList = new ArrayList<>();
    private boolean okClicked = false;
    private ShoppingList shoppingList;
    private Runnable openShoppingList;

    @FXML
    private void handleOk() {
        okClicked = true;
        close();
    }

    @FXML
    private void handleCancel() {
        okClicked = false;
        close();
    }

    private void close() {
        ingredientsBox.getScene().getWindow().hide();
    }

    /**
     * Loading ingredients from recipe and adding their quantity
     * @param ingredients list of ingredients from the selected recipe
     */
    public void loadFromRecipe(List<RecipeIngredient> ingredients) {
        toBeAddedList.clear();
        for (RecipeIngredient recipeIngredient : ingredients) {
            String text = recipeIngredient.getIngredient().getName() + " (" + recipeIngredient.getQuantity() + ")";
            toBeAddedList.add(new ShoppingListIngredient(text));
        }

        showRecipeIngredients();
        System.out.println("Loaded recipe \"" + sourceRecipeName + "\" ingredients");
    }


    /**
     * Display the ingredients in the "To Be Added" list
     */
    public void showRecipeIngredients() {
        ingredientsBox.getChildren().clear();

        for (int i = 0; i < toBeAddedList.size(); i++) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/client/modules/ShoppingListIngredient.fxml")
                );
                Parent row = loader.load();
                ShoppingListIngredientUICtrl ctrl = loader.getController();
                ctrl.setCheckboxEnabled(false);

                int index = i;
                String displayText = toBeAddedList.get(i).getNameAmount();

                ctrl.setText("• " + displayText);
                ctrl.setIndex(index);

                ctrl.setDeleteCheck(idx -> {
                    toBeAddedList.remove(index);
                    showRecipeIngredients();
                });

                ctrl.setEditInstruction(newText -> {
                    toBeAddedList.get(index).setNameAmount(newText);
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
                    String ingredientText = amount.isEmpty() ? name : name + " (" + amount + ")";
                    toBeAddedList.add(new ShoppingListIngredient(ingredientText));
                    showRecipeIngredients();
                    System.out.println("Ingredient \"" + name + "\" added");
                }
            });
        });
    }

    /**
     * show pop up for user to enter a nema/quantity
     * @return name and quantity if inserted
     */
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

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }
    public void setOpenShoppingList(Runnable openShoppingList) {
        this.openShoppingList = openShoppingList;
    }

    @FXML
    private void addToShoppingList() {
        if (shoppingList == null) return;
        for (ShoppingListIngredient ing : toBeAddedList) {
            shoppingList.addIngredient(ing.getNameAmount().trim(), sourceRecipeName);
        }

        toBeAddedList.clear();
        close();
        if (openShoppingList != null) {
            openShoppingList.run();
            System.out.println("Added to shopping list");
        }
    }

    /**
     * getting name of the source recipe
     * @param sourceRecipeName source recipe name
     */
    public void setSourceRecipeName(String sourceRecipeName) {
        this.sourceRecipeName = sourceRecipeName;
    }
}
