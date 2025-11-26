package client.scenes;

import client.Main;
import client.RecipeListCell;
import client.utils.RecipeIngredientUICtrl;
import client.utils.RecipeInstructionUICtrl;
import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;


public class RecipesWindowCtrl {

    private final Insets lineMargin = new Insets(0, 15, 0, 15);

    // DUMMY DATA TO AID DEVELOPMENT DO NOT PUT ON RELEASE BRANCH
    private final Double debugDummyQuantity = 2.0;

    @FXML
    private ListView<Recipe> sidebarRecipeNamesList;

    @FXML
    private VBox recipeView;

    private Recipe currentRecipe;

    // This list will store the recipe names, they're automatically displayed in the sidebar
    // A selection listener should be implemented to handle clicks + deletes of recipes later
    private final ObservableList<Recipe> recipes = FXCollections.observableArrayList();

    /**
     * Initializes the sidebar items (Recipe names) to track the ObervableList items
     */
    public void initialize() {
        sidebarRecipeNamesList.setItems(recipes);

        // TEMPORARY DUMMY DATA TO AID DEVELOPMENT
        Ingredient sample = new Ingredient("Carrot");
        Ingredient sample2 = new Ingredient("Egg");
        Recipe sampler = new Recipe("Eggs and Carrot", null, new ArrayList<>(List.of("Stirff", "fooo", "bar", "Hello World!", "Bruh")));
        RecipeIngredient ri = new RecipeIngredient(sampler, sample, debugDummyQuantity);
        RecipeIngredient ri2 = new RecipeIngredient(sampler, sample2, debugDummyQuantity);
        sampler.setIngredients(List.of(ri, ri2));
        recipes.add(sampler);
        // SECOND RECIPE FOR TESTING
        sample = new Ingredient("Carrot but better");
        sample2 = new Ingredient("Egg but more awesome");
        sampler = new Recipe("Eggs and Carrot+", null, new ArrayList<>(List.of("oops", "I", "dropped", "everything", "fuck")));
        ri = new RecipeIngredient(sampler, sample, debugDummyQuantity);
        ri2 = new RecipeIngredient(sampler, sample2, debugDummyQuantity);
        sampler.setIngredients(List.of(ri, ri2));
        recipes.add(sampler);
        // TEMPORARY DUMMY DATA TO AID DEVELOPMENT

        sidebarRecipeNamesList.setCellFactory(lc -> new RecipeListCell());
        sidebarRecipeNamesList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        openRecipe(newSelection);
                    }
                }
        );

    }

    /**
     * Loads the contents of the provided recipe to the recipeView UI element
     * @param recipe - The recipe to load
     */
    public void openRecipe(Recipe recipe) {
        this.currentRecipe = recipe;
        recipeView.getChildren().clear();
        loadIngredients(recipe.getIngredients());
        Separator sep = new Separator();
        recipeView.getChildren().add(sep);
        VBox.setMargin(sep, lineMargin);
        loadSteps(recipe.getPreparationSteps());
    }

    /**
     * Loads the ingredients within a list to the recipeView UI element
     * @param recipeIngredients - A list of RecipeIngredients
     */
    public void loadIngredients(List<RecipeIngredient> recipeIngredients){
        Label ingredientsLabel = new Label("Ingredients:");
        recipeView.getChildren().add(ingredientsLabel);
        for (int i = 0; i < recipeIngredients.size(); ++i) {
            RecipeIngredient ri = recipeIngredients.get(i);
            Pair<RecipeIngredientUICtrl, Node> ing = Main.FXML.loadNode(RecipeIngredientUICtrl.class,"client", "modules", "RecipeIngredient.fxml");
            RecipeIngredientUICtrl ingCtrl = ing.getKey();
            Node ingNode = ing.getValue();

            ingCtrl.setText("- " + ri.getIngredient().getName() + " " + ri.getQuantity());
            ingCtrl.setIndex(i);
            VBox.setVgrow(ingNode, Priority.ALWAYS);
            recipeView.getChildren().add(ingNode);
        }
        recipeView.requestLayout();
    }

    /**
     * Loads the instructions within a list to the recipeView UI element
     * @param recipeInstructions - A list of Strings (The recipe instructions)
     */
    // This might look like code duplication now, but the way we handle ingredients and steps might change dramatically in the future
    public void loadSteps(List<String> recipeInstructions){
        Label stepsLabel = new Label("Steps:");
        recipeView.getChildren().add(stepsLabel);
        for (int i = 0; i < recipeInstructions.size(); ++i) {
            String instruction = recipeInstructions.get(i);
            Pair<RecipeInstructionUICtrl, Node> ing = Main.FXML.loadNode(RecipeInstructionUICtrl.class,"client", "modules", "RecipeInstruction.fxml");
            RecipeInstructionUICtrl instCtrl = ing.getKey();
            Node ingNode = ing.getValue();
            int currentIndex = i;
            instCtrl.setDeleteCheck(() -> {
                // This code runs when .run() is called on click in deleteCheck runnable
                // Not sure if this is a proper solution to the callback though
                recipeInstructions.remove(currentIndex);
                openRecipe(currentRecipe);
                System.out.println("Instruction removed");
            });
            instCtrl.setEditInstruction(newInstruction -> {
                // This code is run when a string is passed into the editInstruction consumer
                System.out.println("Instruction edit from " + recipeInstructions.get(currentIndex) + " to " + newInstruction);
                recipeInstructions.set(currentIndex, newInstruction);
                openRecipe(currentRecipe);
            });
            instCtrl.setText("- " + instruction);
            instCtrl.setIndex(i);
            VBox.setVgrow(ingNode, Priority.ALWAYS);
            recipeView.getChildren().add(ingNode);
        }
        recipeView.requestLayout();
    }

}
