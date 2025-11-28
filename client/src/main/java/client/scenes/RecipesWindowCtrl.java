package client.scenes;

import client.Main;
import client.RecipeListCell;
import client.utils.ErrorCtrl;
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
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.MultipleSelectionModel;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;


public class RecipesWindowCtrl {

    private final Insets lineMargin = new Insets(0, 15, 0, 15);

    private final ServerUtils server = Main.INJECTOR.getInstance(ServerUtils.class);

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
    private ObservableList<Recipe> recipes;

    private PrimaryCtrl primaryCtrl;
    private ErrorCtrl errorCtrl;

    /**
     * Injectable constructor for RecipesWindowCtrl
     * @param p PrimaryCtrl instance to be injected
     */
    @Inject
    public RecipesWindowCtrl(PrimaryCtrl p, ErrorCtrl c){
        this.primaryCtrl = p;
        this.errorCtrl = c;
    }

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
     * Clears recipe view, making it look the same as when the app launches
     */
    public void clearRecipeView(){
        recipeView.getChildren().clear();
        recipeNameLabel.setText("");
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

        Recipe savedRecipe = server.addRecipe(clone); // Assuming 'server' is initialized
        if (savedRecipe != null) {
            recipes.add(savedRecipe);
            sidebarRecipeNamesList.getSelectionModel().select(savedRecipe);
        } else {
            System.err.println("Recipe creation failed. Check server console for details.");
        }
    }

    /**
     * Event handler for "Download" button.
     * Downloads the currently selected recipe.
     */
    @FXML
    public void onDownloadRecipe() {
        server.downloadRecipe(currentRecipe.getId());
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

    /**
     * Fetches the current recipe data from the server and loads it in the local storage of the recipes
     */
    @FXML
    public void refreshLocalRecipes(){
        boolean serverAvailable = server.isServerAvailable();
        if(!serverAvailable){
            errorCtrl.showServerUnavailableError();
            return;
        }
        try{
            clearRecipeView();
            List<Recipe> serverResponse = server.getRecipes();
            this.recipes.setAll(serverResponse);
            Recipe selectedRecipe = getSelectedRecipe();

            //update the recipe UI to contain the new contents of the previously selected recipe:
            if(selectedRecipe == null) return;
            boolean recipeStillExists = false;
            //In the case that the selected recipe was deleted on the server by a different client, make sure nothing is selected after refreshing
            for(Recipe recipe: serverResponse){
                if(recipe.getId() == selectedRecipe.getId()){
                    selectedRecipe = recipe;
                    openRecipe(recipe);
                    recipeStillExists = true;
                    break;
                }
            }
            if(!recipeStillExists) return;
            setSelectedRecipe(selectedRecipe);
            
        } catch (Exception e){
            errorCtrl.showGenericError(e);
        }
        
    }

    /**
     * Retrieves the selected recipe
     * @return The selected recipe item, null if no recipe is selected
     */
    public Recipe getSelectedRecipe(){
        MultipleSelectionModel<Recipe> selectionModel = sidebarRecipeNamesList.getSelectionModel();
        ObservableList<Recipe> selectedRecipes = selectionModel.getSelectedItems();
        if(selectedRecipes.size() == 0){
            return null; //return null if there are no selected recipes (i.e. the list of selected recipes is empty)
        }
        return selectedRecipes.get(0);
    }

    /**
     * Selects the specified recipe in the ListView (the left bar), does not do anything when recipe is null or not in the ListView
     * @param recipe The recipe to be selected
     */
    public void setSelectedRecipe(Recipe recipe){
        if(!recipes.contains(recipe)) return; //if the recipe is not in the list, do nothing
        MultipleSelectionModel<Recipe> selectionModel = sidebarRecipeNamesList.getSelectionModel();
        selectionModel.select(recipe);
    }
}
