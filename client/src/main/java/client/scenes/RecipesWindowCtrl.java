package client.scenes;

import client.Main;
import client.RecipeListCell;
import client.utils.RecipeIngredientUICtrl;
import client.utils.RecipeInstructionUICtrl;
import client.utils.ServerUtils;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;


public class RecipesWindowCtrl {

    private final Insets lineMargin = new Insets(0, 15, 0, 15);

    private final ServerUtils server = Main.INJECTOR.getInstance(ServerUtils.class);

    @FXML
    private ListView<Recipe> sidebarRecipeNamesList;

    @FXML
    private VBox recipeView;

    @FXML
    private Label recipeNameLabel;

    // This list will store the recipe names, they're automatically displayed in the sidebar
    // A selection listener should be implemented to handle clicks + deletes of recipes later
    private ObservableList<Recipe> recipes;

    /**
     * Initializes the sidebar items (Recipe names) to track the ObservableList items
     */
    public void initialize() {
        recipes = FXCollections.observableArrayList(server.getRecipes());
        sidebarRecipeNamesList.setItems(recipes);

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
        recipeView.getChildren().clear(); //cleaning - otherwise would duplicate all steps and ingredients for every child
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

            instCtrl.setText("• " + instruction);
            instCtrl.setIndex(i);
            VBox.setVgrow(ingNode, Priority.ALWAYS);
            recipeView.getChildren().add(ingNode);
        }
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

    /**
     * Event handler for the "-" button on the recipes list
     * Sends delete request to the server for the currently
     * selected recipe. Upon successful deletion from the server
     * it is deleted from the local client list as well.
     */
    @FXML
    private void onDeleteRecipe() {
        Recipe hit = sidebarRecipeNamesList.getSelectionModel().getSelectedItem();
        if (hit == null) {
            return;
        }
        boolean successful = server.deleteRecipe(hit.getId());
        if (!successful) {
            System.err.println("RecipesWindowCtrl: Failed to delete recipe from the server, aborting request!");
            return;
        }
        sidebarRecipeNamesList.getSelectionModel().selectPrevious();
        recipes.remove(hit);
    }
}
