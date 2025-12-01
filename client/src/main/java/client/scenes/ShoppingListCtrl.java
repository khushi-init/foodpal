package client.scenes;

import client.Main;
import client.utils.ShoppingListIngredientUICtrl;
import commons.ShoppingList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

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

            ctrl.setDeleteCheck(() -> {
                shoppingListView.getChildren().remove(node);
            });
            shoppingListView.requestLayout();
        }

        Button addButton = new Button("Add Ingredient");
        shoppingListView.getChildren().add(addButton);
        addButton.setOnAction(e -> {
            TextInputDialog ingredientNameDialog = new TextInputDialog();
            ingredientNameDialog.setTitle("Add ingredient to shopping list");
            ingredientNameDialog.setHeaderText("Add a new ingredient");
            ingredientNameDialog.setContentText("Ingredient name:");

            Optional<String> result = ingredientNameDialog.showAndWait();
            result.ifPresent(name -> {
                if(!name.trim().isEmpty()){
                    Optional<String> amount = getAmount(name);

                    amount.ifPresentOrElse(x -> {
                        if (!x.isEmpty()) {
                            shoppingList.addIngredient(name.trim() + " (" + x + ")");
                        } else {
                            shoppingList.addIngredient(name.trim());
                        }
                    }, () -> {
                        shoppingList.addIngredient(name.trim());
                    });

                    showShoppingList();
                }
            });
        });
    }

    private Optional<String> getAmount(String name) {
        TextInputDialog ingredientAmountDialog = new TextInputDialog();
        ingredientAmountDialog.setTitle("Add ingredient to shopping list");
        ingredientAmountDialog.setTitle("Add a new ingredient");
        ingredientAmountDialog.setContentText("Amount (e.g. 50g):");
        Optional<String> amount = ingredientAmountDialog.showAndWait();

        return amount;
    }
}
