package client.scenes;

import client.Main;
import client.utils.IngredientPopUpCtrl;
import client.utils.ShoppingListIngredientUICtrl;
import commons.ShoppingList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ShoppingListCtrl {

    @FXML
    private VBox shoppingListView;

    private ShoppingList shoppingList;

    /**
     * Initialize the scene.
     */
    public void initialize() {
        shoppingList = new ShoppingList();
        shoppingList.addIngredient("TEST");
        showShoppingList();
    }

    /**
     * Show the shopping list.
     */
    public void showShoppingList() {
        shoppingListView.getChildren().clear();
        List<String> shoppingListIngredients = shoppingList.getIngredients();

        for (int i = 0; i < shoppingListIngredients.size(); i++) {
            Pair<ShoppingListIngredientUICtrl, Node> pair =
                    Main.FXML.loadNode(
                            ShoppingListIngredientUICtrl.class,
                            "client", "modules", "ShoppingListIngredient.fxml"
                    );

            ShoppingListIngredientUICtrl ctrl = pair.getKey();
            Node node = pair.getValue();

            ctrl.setText("- " + shoppingListIngredients.get(i));
            ctrl.setIndex(i);

            VBox.setVgrow(node, Priority.NEVER);
            shoppingListView.getChildren().add(node);

            ctrl.setDeleteCheck(index -> {
                shoppingListView.getChildren().remove(node);
                shoppingList.removeIngredient(index.intValue());
            });
            shoppingListView.requestLayout();
        }

        Button addButton = new Button("Add Ingredient");
        shoppingListView.getChildren().add(addButton);
        addButton.setOnAction(e -> {
            Optional<Pair<String, String>> result = showIngredientPopUp();
            result.ifPresent(pair -> {
                String name = pair.getKey().trim();
                String amount = pair.getValue().trim();

                if (!name.isEmpty()) {
                    if (!amount.isEmpty()) {
                        shoppingList.addIngredient(name + " (" + amount + ")");
                    } else {
                        shoppingList.addIngredient(name);
                    }
                    showShoppingList();
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

            IngredientPopUpCtrl ctrl = loader.getController();

            Stage popUpStage = new Stage();
            popUpStage.initModality(Modality.APPLICATION_MODAL);
            popUpStage.setTitle("Add to shopping list");
            popUpStage.setScene(new Scene(root));

            ctrl.setStage(popUpStage);

            popUpStage.showAndWait();

            if (ctrl.isOkClicked()) {
                return Optional.of(new Pair<>(ctrl.getName(), ctrl.getQuantity()));
            } else {
                return Optional.empty();
            }

        } catch (IOException e) {
            System.out.println(e.toString());
            return Optional.empty();
        }
    }

}
