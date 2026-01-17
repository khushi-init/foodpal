package client.scenes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import client.MyFXML;
import client.data.TranslationManager;
import client.popups.ShoppingListIngredientPopUpCtrl;
import client.popups.ShoppingListIngredientUICtrl;
import com.google.inject.Inject;
import commons.ShoppingList;
import commons.ShoppingListIngredient;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

public class ShoppingListCtrl {

    private ErrorCtrl errorCtrl;

    private final MyFXML fxml;

    @FXML
    private VBox shoppingListView;

    private ShoppingList shoppingList;

    public TranslationManager tm;

    @FXML
    private Button download;
    @FXML
    private Button reset;
    @FXML
    private Label shoppingListLabel;

    public void setErrorCtrl(ErrorCtrl errorCtrl) {
        this.errorCtrl = errorCtrl;
    }

    /**
     * Constructor for ShoppingListCtrl
     * @param fxml - Injected FXML module
     */
    @Inject
    public ShoppingListCtrl(MyFXML fxml, TranslationManager tm) {
        this.fxml = fxml;
        this.tm = tm;
    }

    /**
     * Show the shopping list.
     */
    public void showShoppingList() {
        shoppingListView.getChildren().clear();
        List<ShoppingListIngredient> shoppingListIngredients = shoppingList.getIngredients();

        // Add label when the list is empty, otherwise fill the list.
        if (shoppingListIngredients.isEmpty()) {
            Label emptyList = new Label(tm.tr("shoppinglist.empty"));
            shoppingListView.getChildren().add(emptyList);
        } else {
            Map<String, Integer> ingredientNameCounts = countIngredientNames(shoppingListIngredients);
            showIngredients(shoppingListIngredients, ingredientNameCounts);
        }

        addButton();
        shoppingListView.requestLayout();
    }

    private Map<String, Integer> countIngredientNames(List<ShoppingListIngredient> ingredients) {
        Map<String, Integer> counts = new HashMap<>();
        for (ShoppingListIngredient ingredient : ingredients) {
            String name = extractIngredientName(ingredient.getNameAmount());
            counts.put(name, counts.getOrDefault(name, 0) + 1);
        }
        return counts;
    }

    private void showIngredients(List<ShoppingListIngredient> ingredients, Map<String, Integer> counts) {
        for (int i = 0; i < ingredients.size(); i++) {
            Pair<ShoppingListIngredientUICtrl, Node> pair =
                    fxml.loadNode(
                            ShoppingListIngredientUICtrl.class,
                            "client", "modules", "ShoppingListIngredient.fxml"
                    );

            ShoppingListIngredientUICtrl ctrl = pair.getKey();
            Node node = pair.getValue();

            String displayText = ingredients.get(i).getNameAmount();
            String ingredientName = extractIngredientName(displayText);
            // show recipe name only when duplicates
            if (counts.get(ingredientName) > 1 && 
                    ingredients.get(i).getRecipeName() != null && 
                    !ingredients.get(i).getRecipeName().isEmpty()) {
                displayText += " - " + ingredients.get(i).getRecipeName();
            }
            
            ctrl.setText(displayText);
            ctrl.setCheckedOff(ingredients.get(i).isCheckedOff());
            ctrl.setIndex(i);

            VBox.setVgrow(node, Priority.NEVER);
            shoppingListView.getChildren().add(node);

            ctrl.setDeleteCheck(index -> {
                shoppingList.removeIngredient(index.intValue());
                showShoppingList();
            });

            ctrl.setEditInstruction(newText -> {
                String nameAmount;
                String recipeName = null;
                
                if (newText.contains(" - ")) {
                    String[] parts = newText.split(" - ", 2);
                    nameAmount = parts[0].trim();
                    recipeName = parts[1].trim();
                } else {
                    nameAmount = newText.trim();
                }
                
                shoppingList.updateIngredients((int) ctrl.getIndex(), nameAmount);
                if (recipeName != null) {
                    shoppingList.getIngredients().get((int) ctrl.getIndex()).setRecipeName(recipeName);
                }
                showShoppingList();
            });

            ctrl.setCheckoffInstruction((index, checked) ->{
                shoppingList.updateCheckedOff((int) ctrl.getIndex(), checked);
            });
        }
    }

    private void addButton() {
        Button addButton = new Button(tm.tr("add.button.tobeadded"));
        shoppingListView.getChildren().add(addButton);
        addButton.setOnAction(e -> {
            Optional<Pair<String, String>> result = showIngredientPopUp();
            result.ifPresent(pair -> {
                String name = pair.getKey().trim();
                String amount = pair.getValue().trim();

                if (!name.isEmpty()) {
                    String ingredientText = amount.isEmpty() ? name : name + " (" + amount + ")";
                    shoppingList.addIngredient(ingredientText);
                    showShoppingList();
                    System.out.println("Ingredient \"" + ingredientText + "\" added to the shopping list");
                }
            });
        });
    }

    /**
     * Show a new popup window for adding an ingredient to the shopping list.
     * @return - An optional of a Pair of 2 strings,
     * the first string containing the ingredient name and the second string containing the amount.
     */
    private Optional<Pair<String, String>> showIngredientPopUp() {
        Pair<ShoppingListIngredientPopUpCtrl, Parent> ingListPair = fxml.load(ShoppingListIngredientPopUpCtrl.class, "client", "modules", "AddIngredientToList.fxml");

        Parent root = ingListPair.getValue();

        ShoppingListIngredientPopUpCtrl ctrl = ingListPair.getKey();

        Stage popUpStage = new Stage();
        popUpStage.initModality(Modality.APPLICATION_MODAL);
        popUpStage.setTitle(tm.tr("button.addtoshoppinglist"));
        popUpStage.setScene(new Scene(root));

        ctrl.setStage(popUpStage);

        popUpStage.showAndWait();

        if (ctrl.isOkClicked()) {
            return Optional.of(new Pair<>(ctrl.getName(), ctrl.getQuantity()));
        } else {
            return Optional.empty();
        }

    }

    /**
     * Downloads a file of the shopping list.
     */
    @FXML
    private void onHandleDownload() {
        String userDownloads = System.getProperty("user.home") + "/Downloads/";
        String fileName = tm.tr("shoppinglist.filename") + ".md";
        Path filePath = Paths.get(userDownloads + fileName);

        String shoppingListMarkdown = shoppingList.toMarkdown();
        byte[] file = shoppingListMarkdown.getBytes();
        
        try {
            Files.write(filePath, file);
            errorCtrl.displayInfo(
                    tm.tr("download.success", filePath),
                    tm.tr("button.close"),
                    tm.tr("status.success")
            );
        }
        catch (IOException ex) {
            System.out.print("Invalid Path");
        }
    }

    /**
     * Resets the list when rest button is pressed.
     */
    @FXML
    private void onHandleReset() {
        shoppingList.resetList();
        showShoppingList();
        System.out.println("Shopping list has been reset");
    }

    /**
     * Set the shopping list to the one created when starting the project.
     * @param shoppingList - The shopping list to use.
     */
    public void setAndShowShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
        showShoppingList();
    }

    /**
     * Extract the ingredient name from a formatted string like "Sugar (100g)" or "Sugar (100g) - Recipe Name"
     * @param text The formatted ingredient text
     * @return The ingredient name, or null if it can't be parsed
     */
    private String extractIngredientName(String text) {
        String withoutRecipe = text.trim();

        //take the name before (
        int quantityStart = withoutRecipe.lastIndexOf(" (");
        if (quantityStart > 0) {
            return withoutRecipe.substring(0, quantityStart).trim();
        }
        return withoutRecipe.trim();
    }

    private void applyTexts() {
        download.setText(tm.tr("button.download"));
        reset.setText(tm.tr("button.reset"));
        shoppingListLabel.setText(tm.tr("label.shoppinglist"));
    }

    @FXML
    public void initialize() {
        applyTexts();
        tm.bundleProperty().addListener((obs, oldBundle, newBundle) -> {
            applyTexts();
        });
    }

}
