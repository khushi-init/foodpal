package client.scenes;

import client.Main;
import client.RecipeListCell;
import client.utils.RecipeIngredientUICtrl;
import client.utils.RecipeInstructionUICtrl;
import client.utils.ServerUtils;
import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;


public class RecipesWindowCtrl {

    private final Insets lineMargin = new Insets(0, 15, 0, 15);

    // DUMMY DATA TO AID DEVELOPMENT DO NOT PUT ON RELEASE BRANCH
    private final Double debugDummyQuantity = 2.0;

    private final ServerUtils server = new ServerUtils();

    @FXML
    private ListView<Recipe> sidebarRecipeNamesList;

    @FXML
    private VBox recipeView;

    @FXML
    private Label recipeNameLabel;

    private Recipe currentRecipe;

    private boolean newInstructionAdded = false;

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

        recipeNameLabel.setText(recipe.getName());

    }
    private final int fontSize = 16;
    /**
     * Loads the ingredients within a list to the recipeView UI element
     * @param recipeIngredients - A list of RecipeIngredients
     */
    public void loadIngredients(List<RecipeIngredient> recipeIngredients){
        Label ingredientsLabel = new Label("Ingredients:");
        ingredientsLabel.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        recipeView.getChildren().add(ingredientsLabel);
        for (int i = 0; i < recipeIngredients.size(); ++i) {
            RecipeIngredient ri = recipeIngredients.get(i);
            Pair<RecipeIngredientUICtrl, Node> ing = Main.FXML.loadNode(RecipeIngredientUICtrl.class,"client", "modules", "RecipeIngredient.fxml");
            RecipeIngredientUICtrl ingCtrl = ing.getKey();
            Node ingNode = ing.getValue();

            ingCtrl.setText("• " + ri.getIngredient().getName() + " " + ri.getQuantity());
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
        stepsLabel.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        recipeView.getChildren().add(stepsLabel);
        for (int i = 0; i < recipeInstructions.size(); ++i) {
            String instruction = recipeInstructions.get(i);
            Pair<RecipeInstructionUICtrl, Node> ing = Main.FXML.loadNode(RecipeInstructionUICtrl.class,"client", "modules", "RecipeInstruction.fxml");
            RecipeInstructionUICtrl instCtrl = ing.getKey();
            Node ingNode = ing.getValue();
            int currentIndex = i;
            // Delete logic
            instCtrl.setDeleteCheck(() -> {
                // This code runs when .run() is called on click in deleteCheck runnable
                // Not sure if this is a proper solution to the callback though
                recipeInstructions.remove(currentIndex);
                openRecipe(currentRecipe);
                System.out.println("Instruction removed");
            });
            // Edit Logic
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
            // Check if an instruction was added, and if it we are in the new instruction
            // To trigger immediate editing.
            if(newInstructionAdded && (i == recipeInstructions.size() - 1)) {
                instCtrl.handleEditButton();
                newInstructionAdded = false;
            }
        }
        // Add logic
        Button addButton = new Button("Add Instruction");
        recipeView.getChildren().add(addButton);
        addButton.setOnAction(e -> {
            recipeInstructions.add("New Instruction");
            newInstructionAdded = true;
            openRecipe(currentRecipe);
        });
        recipeView.requestLayout();
    }

    /**
     * Event handler for "Duplicate" button.
     * Clones currently selected recipe and adds it a new name(i)
     */
    @FXML
    public void onCloneRecipe(){
        Recipe selected = sidebarRecipeNamesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        String newName = createCopyName(selected.getName());
        Recipe clone = cloneRecipe(selected, newName);

        recipes.add(clone);

        sidebarRecipeNamesList.getSelectionModel().select(clone);
    }

    /**
     * Creates unique name for a recipe based on from which recipe its duplicated.
     * @param baseName the name of recipe that is being duplicated
     * @return a new unique name of the clone
     */
    private String createCopyName(String baseName){
        int i = 1;
        while(true){
            String candidate = baseName + "(" + i +")";
            boolean exists = recipes.stream().anyMatch(r -> r.getName().equals(candidate));
            if(!exists){
                return candidate;
            }
            i++;
        }
    }

    /**
     * Creates a deep copy of the given recipe but with new name.
     * @param original the recipe we want duplicate
     * @param newName name assigned to this duplicate of the recipe
     * @return a new Recipe that is clone of the original
     */
    private Recipe cloneRecipe(Recipe original, String newName){
        List<String> stepsCopy = new ArrayList<>(original.getPreparationSteps());

        Recipe clone = new Recipe(newName, null, stepsCopy);

        List<RecipeIngredient>  ingredientsCopy = new ArrayList<>();
        for(RecipeIngredient ri : original.getIngredients()){
            RecipeIngredient newRi = new RecipeIngredient(clone, ri.getIngredient(), ri.getQuantity());
            ingredientsCopy.add(newRi);
        }
        clone.setIngredients(ingredientsCopy);

        return clone;



    }

    /**
     * Event handler for the '+' button in the sidebar
     * Creates a new placeholder recipe object
     * Sends the object to the serve via post to be saved
     * Also selects the new recipe on the sidebar
     */
    @FXML
    private void onAddRecipe() {
        Recipe newRecipe = new Recipe(
                "New Recipe",
                new ArrayList<>(),
                new ArrayList<>()
        );
        //calls the fixed ServerUtils method addRecipe
        Recipe savedRecipe = server.addRecipe(newRecipe); // Assuming 'server' is initialized

        if (savedRecipe != null) {
            // add the server-returned object
            recipes.add(savedRecipe);

            // select and open the new recipe in the sidebar
            sidebarRecipeNamesList.getSelectionModel().select(savedRecipe);
        } else {
            // error handling
            System.err.println("Recipe creation failed. Check server console for details.");
        }
    }
}
