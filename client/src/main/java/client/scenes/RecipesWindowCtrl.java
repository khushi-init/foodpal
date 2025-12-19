package client.scenes;

import client.Main;
import client.RecipeListCell;
import client.data.DataManipulator;
import client.data.LocalStorage;
import client.utils.*;
import client.utils.searchUtils.Proposition;
import commons.Ingredient;
import commons.NutritionalValue;
import commons.Recipe;
import commons.RecipeIngredient;
import commons.ShoppingList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.MultipleSelectionModel;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;

import com.google.inject.Inject;


public class RecipesWindowCtrl {

    private final Insets lineMargin = new Insets(0, 15, 0, 15);

    private final ServerUtils server = Main.INJECTOR.getInstance(ServerUtils.class);


    @FXML
    private ListView<Recipe> sidebarRecipeNamesList;

    @FXML
    private VBox recipeView;

    // The recipeName is an editable TextField
    @FXML
    private TextField recipeNameField;

    @FXML
    private TextField searchField;

    @FXML
    private Label cancelSearchButton;

    @FXML
    private Button downloadButton;

    @FXML
    private Button duplicateButton;

    @FXML
    private Button favoriteButton;

    @FXML
    private Button toCart;

    @FXML
    private Label advancedSearchButton;

    // Favorite Injections
    @FXML
    private ImageView favoriteImage;
    private Image favorite;
    private Image unFavorite;
    private Recipe currentRecipe;
    @FXML
    private CheckBox favoriteCheck;


    private boolean newInstructionAdded = false;

    // This is the Shopping List data that is used in the session.
    private final ShoppingList shoppingList = new ShoppingList();

    private LocalStorage storage;
    private DataManipulator dataManipulator;

    private final ErrorCtrl errorCtrl;
    private final SearchCtrl searchCtrl;

    private final NutritionalValue defaultNutritionalValue = new NutritionalValue(0, 0, 0);

    private final PrimaryCtrl primaryCtrl;

    /**
     * Injectable constructor for RecipesWindowCtrl
     * @param c ErrorCtrl instance for error
     * @param p Primary Ctrl instance
     * @param storage - The local storage storing recipes and ingredients
     * @param dataManipulator - The data manipulator
     * @param s - The injected search control
     */
    @Inject
    public RecipesWindowCtrl(ErrorCtrl c, PrimaryCtrl p, LocalStorage storage, DataManipulator dataManipulator, SearchCtrl s) {
        this.errorCtrl = c;
        this.primaryCtrl = p;
        this.storage = storage;
        this.dataManipulator = dataManipulator;
        this.searchCtrl = s;
    }

    /**
     * Initializes the sidebar items (Recipe names) to track the ObservableList items
     */
    public void initialize() {
        dataManipulator.loadFavs();
        favorite = new Image(getClass().getResource("/client/images/favorite.png").toExternalForm());
        unFavorite = new Image(getClass().getResource("/client/images/not_favorite.png").toExternalForm());
        sidebarRecipeNamesList.setItems(storage.getRecipes());
        sidebarRecipeNamesList.setCellFactory(lc -> new RecipeListCell(storage.getFavoriteIDs(), favorite));
        sidebarRecipeNamesList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        openRecipe(newSelection);
                    } else {
                        clearRecipeView();
                        updateRecipeSelectionState(false);
                    }
                }
        );
        favoriteCheck.selectedProperty().addListener(
                (obs, oldSelection, newSelection) -> updateToFav());

        // New listener for Recipe TextField
        // Saves the new name of teh recipe once the enter key is pressed
        recipeNameField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                saveRecipeName();
                recipeNameField.getParent().requestFocus();
            }
        });
        // Focused Property Listener, saves when the TextField loses focus
        recipeNameField.focusedProperty().addListener((obs,
                                                       oldFocused, newFocused) -> {
            if (oldFocused && !newFocused) {
                saveRecipeName();
            }
        });

        // Deactivate recipe specific buttons, since nothing is selected at the start.
        updateRecipeSelectionState(false);

        initializeSceneEvents();
        intializeSearchElements();
    }

    /**
     * Sets event handlers for the whole window
     */
    public void initializeSceneEvents(){
        searchField.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if(newScene != null){
                //when ESCAPE is pressed anywhere in the window, the search is canceled:
                newScene.setOnKeyPressed(event -> {
                    if(event.getCode() == KeyCode.ESCAPE){
                        cancelSearch();
                    }
                });
            }
        });
    }

    /**
     * Initializes the search field and the cancel button.
     */
    public void intializeSearchElements(){
        cancelSearchButton.setDisable(true);

        searchField.setOnKeyReleased(event -> {
            cancelSearchButton.setDisable(searchField.getText().isEmpty());

            //the query is executed iff the user presses enter:
            if(event.getCode() == KeyCode.ENTER){
                if(searchField.getText().isEmpty()){
                    cancelSearch(); //if query is empty, return to the normal sidebar.
                    return;
                }
                try{
                    ObservableList<Recipe> searchResults = FXCollections.observableArrayList(
                            favFilter(
                                    searchCtrl.query(
                                    searchField.getText(), storage.getRecipes()
                            ),
                            storage.getFavoriteIDs())
                    );
                    sidebarRecipeNamesList.setItems(searchResults); //show results in the sidebar
                    searchField.getParent().requestFocus(); //shift focus to a different element, away from the searchField
                } catch (Exception e){
                    errorCtrl.showGenericError(e);
                }

            }
        });
    }

    /**
     * Takes a Proposition that was created in another window and performs a search with it.
     * @param prop
     */
    public void applyExternalSearch(Proposition prop){
        try{
            ObservableList<Recipe> searchResults = FXCollections.observableArrayList(
                    favFilter(
                            searchCtrl.performComplexQuery(prop, storage.getRecipes()),
                            storage.getFavoriteIDs()
                    )

            );
            sidebarRecipeNamesList.setItems(searchResults);
        } catch (Exception e){
            errorCtrl.showGenericError(e);
        }
    }

    /**
     * Updates the sidebar to display favorites.
     */
    public void updateToFav() {
        cancelSearch();
        // Load only favorites or nah
        if (favoriteCheck.isSelected()) {
            searchCtrl.setFavToggle(true);
            List<Recipe> favRecipes = new ArrayList<>();
            for (Recipe r : storage.getRecipes()) {
                if (storage.getFavoriteIDs().contains(r.getId())) {
                    favRecipes.add(r);
                }
            }
            sidebarRecipeNamesList.setItems(FXCollections.observableList(favRecipes));
            sidebarRecipeNamesList.getSelectionModel().select(0);
        } else {
            searchCtrl.setFavToggle(false);
            sidebarRecipeNamesList.setItems(storage.getRecipes());
        }
    }




    /**
     * Enables or disables all recipe-specific UI controls.
     * When inactive, buttons related to the currently selected recipe
     * (download, duplicate, favorite) and the recipe name field are
     * hidden and disabled. When active, they become visible and usable
     * and an informational message is shown.
     *
     * @param active - True if you want them active, false otherwise.
     */
    private void updateRecipeSelectionState(boolean active) {

        downloadButton.setDisable(!active);
        downloadButton.setVisible(active);

        duplicateButton.setDisable(!active);
        duplicateButton.setVisible(active);

        favoriteButton.setDisable(!active);
        favoriteButton.setVisible(active);

        toCart.setDisable(!active);
        toCart.setVisible(active);

        recipeNameField.setDisable(!active);
        recipeNameField.setVisible(active);

        if (!active) {
            Label noRecipeSelectedLabel = new Label("You have not selected any recipe yet!\n" +
                    "Select one in the list on the right or create your very own.");
            noRecipeSelectedLabel.setStyle(
                            "-fx-text-fill: #6b7280; " +
                            "-fx-font-size: 14; " +
                            "-fx-padding: 12;"
            );
            noRecipeSelectedLabel.setWrapText(true);

            recipeView.getChildren().add(noRecipeSelectedLabel);
        }
    }



    /**
     * Loads the contents of the provided recipe to the recipeView UI element
     *
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
        // Favorites
        if (currentRecipe != null && storage.getFavoriteIDs() != null) {
            if (storage.getFavoriteIDs().contains(currentRecipe.getId())) {
                favoriteImage.setImage(favorite);
            } else {
                favoriteImage.setImage(unFavorite);
            }
        }

        recipeNameField.setText(recipe.getName());

        // Activate recipe specific buttons
        updateRecipeSelectionState(true);

    }

    private final int fontSize = 16;

    /**
     * Loads the ingredients within a list to the recipeView UI element
     *
     * @param recipeIngredients - A list of RecipeIngredients
     */
    public void loadIngredients(List<RecipeIngredient> recipeIngredients) {
        Label ingredientsLabel = new Label("Ingredients:");
        ingredientsLabel.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        recipeView.getChildren().add(ingredientsLabel);

        if (recipeIngredients.isEmpty()) {
            recipeView.getChildren().add(new Label("This recipe does not have any ingredients yet!"));
        }

        for (int i = 0; i < recipeIngredients.size(); ++i) {
            RecipeIngredient ri = recipeIngredients.get(i);
            Pair<RecipeIngredientUICtrl, Node> ing = Main.FXML.loadNode(RecipeIngredientUICtrl.class, "client", "modules", "RecipeIngredient.fxml");
            RecipeIngredientUICtrl ingCtrl = ing.getKey();
            Node ingNode = ing.getValue();

            ingCtrl.setText("• " + ri.getIngredient().getName() + " " + ri.getQuantity().toString());
            ingCtrl.setIndex(i);
            VBox.setVgrow(ingNode, Priority.ALWAYS);
            recipeView.getChildren().add(ingNode);

            Long ingredientId = (ri.getIngredient() != null) ? ri.getIngredient().getId() : null;
            long recipeId = currentRecipe.getId();

            ingCtrl.setDeleteIngredient(() -> { //remove ingredient from local recipe
                currentRecipe.getIngredients().remove(ri);
                openRecipe(currentRecipe);

                if (ingredientId != null) { //if it also has id --> remove also from server
                    boolean success = server.deleteIngredient(recipeId, ingredientId);
                    if (!success) {
                        errorCtrl.showGenericError("Failed to delete ingredient from server.");
                    }
                    return;
                }
                openRecipe(currentRecipe);
            });
            // Editing ingredient Logic!
            ingCtrl.setEditIngredient(() -> {
                handleIngredientInput(ri.getIngredient().getName(), ri.getQuantity(), (newName, newQty) -> {
                    ri.setQuantity(newQty);
                    ri.getIngredient().setName(newName);
                    openRecipe(currentRecipe);
                });
            });
        }
        //button for adding an ingredient --> pop up window will show
        Button addButton = new Button("Add Ingredient");
        recipeView.getChildren().add(addButton);
        addButton.setOnAction(e -> {
            showIngredientPopUp("Name", "0.0").ifPresent(pair -> {
                String name = pair.getKey();
                String quantityText = pair.getValue(); //extraction name and quantity

                double quantity;
                try {  //converting string value of quantity to double
                    quantity = Double.parseDouble(quantityText);
                } catch (NumberFormatException err) {
                    if (errorCtrl != null) {
                        errorCtrl.showGenericError("Quantity must be a number.");
                    }
                    return;
                }
                recipeIngredients.add(new RecipeIngredient(currentRecipe,
                        new Ingredient(name, defaultNutritionalValue), quantity));
            });
            Recipe updated = server.updateRecipe(currentRecipe);
            if (updated == null) {
                errorCtrl.showServerUnavailableError();
                return;
            }
            applyUpdatedRecipe(updated);
            openRecipe(currentRecipe);
        });
        recipeView.requestLayout();
    }

    /**
     * Handling input window for ingredient editing
     *
     * @param initName the initial name value to be displayed
     * @param initQty  the initial quantity value to be displayed
     * @param handler  the consumer that handles to call back to the value's usage
     */
    public void handleIngredientInput(String initName, double initQty, BiConsumer<String, Double> handler) {
        showIngredientPopUp(initName, String.valueOf(initQty)).ifPresent(pair -> {
            String name = pair.getKey();
            String quantityText = pair.getValue();
            double quantity;
            try {
                quantity = Double.parseDouble(quantityText);
            } catch (NumberFormatException err) {
                if (errorCtrl != null) {
                    errorCtrl.showGenericError("Quantity must be a number.");
                }
                return;
            }
            handler.accept(name, quantity);
        });
    }

    /**
     * Loads the instructions within a list to the recipeView UI element
     *
     * @param recipeInstructions - A list of Strings (The recipe instructions)
     */
    // This might look like code duplication now, but the way we handle ingredients and steps might change dramatically in the future
    public void loadSteps(List<String> recipeInstructions) {
        Label stepsLabel = new Label("Steps:");
        stepsLabel.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        recipeView.getChildren().add(stepsLabel);

        if (recipeInstructions.isEmpty()) {
            recipeView.getChildren().add(new Label("This recipe does not have any preparation steps yet!"));
        }

        for (int i = 0; i < recipeInstructions.size(); ++i) {
            String instruction = recipeInstructions.get(i);
            Pair<RecipeInstructionUICtrl, Node> ing = Main.FXML.loadNode(RecipeInstructionUICtrl.class, "client", "modules", "RecipeInstruction.fxml");
            RecipeInstructionUICtrl instCtrl = ing.getKey();
            Node ingNode = ing.getValue();
            int currentIndex = i;
            // Delete logic
            instCtrl.setDeleteCheck(() -> {
                // This code runs when .run() is called on click in deleteCheck runnable
                // Not sure if this is a proper solution to the callback though
                recipeInstructions.remove(currentIndex);
                Recipe updated = server.updateRecipe(currentRecipe);
                if (updated == null) {
                    errorCtrl.showServerUnavailableError();
                } else {
                    applyUpdatedRecipe(updated);
                }
                openRecipe(currentRecipe);
                System.out.println("Instruction removed");
            });
            // Edit Logic
            instCtrl.setEditInstruction(newInstruction -> {
                // This code is run when a string is passed into the editInstruction consumer
                System.out.println("Instruction edit from " + recipeInstructions.get(currentIndex) + " to " + newInstruction);
                recipeInstructions.set(currentIndex, newInstruction);
                Recipe updated = server.updateRecipe(currentRecipe);
                if (updated == null) {
                    errorCtrl.showServerUnavailableError();
                } else {
                    applyUpdatedRecipe(updated);
                }
                openRecipe(currentRecipe);
            });
            instCtrl.setText("- " + instruction);
            instCtrl.setIndex(i);
            VBox.setVgrow(ingNode, Priority.ALWAYS);
            recipeView.getChildren().add(ingNode);
            // Check if an instruction was added, and if we are in the new instruction
            // To trigger immediate editing.
            if (newInstructionAdded && (i == recipeInstructions.size() - 1)) {
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
    public void clearRecipeView() {
        recipeView.getChildren().clear();
        recipeNameField.setText("");
    }

    /**
     * Event handler for "Duplicate" button.
     * Clones currently selected recipe and adds it a new name(i)
     */
    @FXML
    public void onCloneRecipe() {
        cancelSearch();
        Recipe recipe = currentRecipe;
        if (recipe == null) {
            recipe = sidebarRecipeNamesList.getSelectionModel().getSelectedItem();
        }
        if (recipe == null) {
            return;
        }
        String newName = createCopyName(recipe.getName());
        Recipe clone = cloneRecipe(recipe, newName);
        Recipe savedRecipe = server.addRecipe(clone);
        if(savedRecipe != null){
            storage.getRecipes().add(savedRecipe);
            sidebarRecipeNamesList.getSelectionModel().select(savedRecipe);
        } else {
            errorCtrl.showGenericError("Recipe not selected to clone.");
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
     *
     * @param baseName the name of recipe that is being duplicated
     * @return a new unique name of the clone
     */
    private String createCopyName(String baseName) {
        int i = 1;
        while(true){
            String candidate = baseName + "(" + i +")";
            boolean exists = storage.getRecipes().stream().anyMatch(r -> r.getName().equals(candidate));
            if(!exists){
                return candidate;
            }
            i++;
        }
    }

    /**
     * Creates a deep copy of the given recipe but with new name.
     *
     * @param original the recipe we want duplicate
     * @param newName  name assigned to this duplicate of the recipe
     * @return a new Recipe that is clone of the original
     */
    private Recipe cloneRecipe(Recipe original, String newName) {
        List<String> stepsCopy = new ArrayList<>(original.getPreparationSteps());

        Recipe clone = new Recipe(newName, null, stepsCopy);

        List<RecipeIngredient> ingredientsCopy = new ArrayList<>();
        for (RecipeIngredient ri : original.getIngredients()) {
            Ingredient oldIng = ri.getIngredient();
            Ingredient newIng = new Ingredient(oldIng.getName(), defaultNutritionalValue);
            RecipeIngredient newRi = new RecipeIngredient(clone, newIng, ri.getQuantity());
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
        cancelSearch();
        Recipe newRecipe = new Recipe(
                "New Recipe",
                new ArrayList<>(),
                new ArrayList<>()
        );
        //calls the fixed ServerUtils method addRecipe
        Optional<Recipe> savedRecipe = dataManipulator.addRecipe(newRecipe);
        savedRecipe.ifPresent(recipe -> sidebarRecipeNamesList.getSelectionModel().select(recipe));
    }

    /**
     * method to save a newly changed name of a recipe
     */
    public void saveRecipeName() {
        if (currentRecipe != null) {
            String newName = recipeNameField.getText().trim();

            // if the new name is empty, revert back to original name
            if (newName.isEmpty()) {
                recipeNameField.setText(currentRecipe.getName());
                return;
            }

            // if the new name is same, just return
            if (newName.equals(currentRecipe.getName())) {
                return;
            }

            Optional<Recipe> updated = dataManipulator.editRecipeName(currentRecipe, newName);
            if (updated.isPresent()) {
                applyUpdatedRecipe(updated.get());
                sidebarRecipeNamesList.refresh();
            }
            else recipeNameField.setText(currentRecipe.getName());
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
        dataManipulator.deleteRecipe(hit);
    }

    @FXML
    private void onShoppingList() {
        showShoppingList();
    }

    /**
     * Fetches the current recipe data from the server and loads it in the local storage of the recipes
     */
    @FXML
    public void refreshLocalRecipes(){
        try{
            clearRecipeView();

            Recipe selectedRecipe = getSelectedRecipe();

            dataManipulator.refreshRecipes();

            //update the recipe UI to contain the new contents of the previously selected recipe:
            if (selectedRecipe == null) return;
            boolean recipeStillExists = false;
            //In the case that the selected recipe was deleted on the server by a different client, make sure nothing is selected after refreshing
            for(Recipe recipe: storage.getRecipes()){
                if(Objects.equals(recipe.getId(), selectedRecipe.getId())){
                    selectedRecipe = recipe;
                    openRecipe(recipe);
                    recipeStillExists = true;
                    break;
                }
            }
            if (!recipeStillExists) return;
            setSelectedRecipe(selectedRecipe);

        } catch (Exception e) {
            errorCtrl.showGenericError(e);
        }

    }

    /**
     * Retrieves the selected recipe
     *
     * @return The selected recipe item, null if no recipe is selected
     */
    public Recipe getSelectedRecipe() {
        MultipleSelectionModel<Recipe> selectionModel = sidebarRecipeNamesList.getSelectionModel();
        ObservableList<Recipe> selectedRecipes = selectionModel.getSelectedItems();
        if (selectedRecipes.isEmpty()) {
            return null; //return null if there are no selected recipes (i.e. the list of selected recipes is empty)
        }
        return selectedRecipes.get(0);
    }

    /**
     * Selects the specified recipe in the ListView (the left bar), does not do anything when recipe is null or not in the ListView
     *
     * @param recipe The recipe to be selected
     */

    public void setSelectedRecipe(Recipe recipe){
        if(!storage.getRecipes().contains(recipe)) return; //if the recipe is not in the list, do nothing
        MultipleSelectionModel<Recipe> selectionModel = sidebarRecipeNamesList.getSelectionModel();
        selectionModel.select(recipe);
    }


    private Optional<Pair<String, String>> showIngredientPopUp(String initialName, String initialQuantity) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/client/modules/IngredientPopUp.fxml")
            );
            Parent root = loader.load();

            IngredientPopUpCtrl ctrl = loader.getController();

            Stage popUpStage = new Stage();
            popUpStage.initModality(Modality.APPLICATION_MODAL);
            popUpStage.setTitle("Edit Ingredient");
            popUpStage.setScene(new Scene(root));

            ctrl.setStage(popUpStage);
            ctrl.setInitialValues(initialName, initialQuantity);

            popUpStage.showAndWait();

            if (ctrl.isOkClicked()) {
                return Optional.of(new Pair<>(ctrl.getName(), ctrl.getQuantity()));
            } else {
                return Optional.empty();
            }

        } catch (IOException e) {
            if (errorCtrl != null) {
                errorCtrl.showGenericError(e);
            } else {
                e.printStackTrace();
            }
            return Optional.empty();
        }
    }

    private void showShoppingList() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/client/scenes/ShoppingList.fxml")
            );
            Parent root = loader.load();

            ShoppingListCtrl ctrl = loader.getController();
            ctrl.setAndShowShoppingList(shoppingList);

            Stage shoppingListStage = new Stage();
            shoppingListStage.setTitle("Shopping List");
            shoppingListStage.setScene(new Scene(root));

            shoppingListStage.show();

        } catch (IOException e) {
            if (errorCtrl != null) {
                errorCtrl.showGenericError(e);
            } else {
                e.printStackTrace();
            }
        }
    }


    private void applyUpdatedRecipe(Recipe updated) {
        if (updated == null) {
            return;
        }
        currentRecipe = updated;
    }

    /**
     * Adds or removes recipe ID to/from favorites list
     */
    public void toggleFavorite() {
        if (storage.getFavoriteIDs() != null) {
            if (storage.getFavoriteIDs().contains(currentRecipe.getId())) {
                storage.getFavoriteIDs().remove(currentRecipe.getId());
                favoriteImage.setImage(unFavorite);
                System.out.println("Removed from favorites!");
            } else {
                storage.getFavoriteIDs().add(currentRecipe.getId());
                favoriteImage.setImage(favorite);
                System.out.println("Added to favorites!");
            }
            // Regardless of change, put new favorites to file.
            dataManipulator.saveFave();
            dataManipulator.loadFavs();
            // Refresh for sidebar look
            sidebarRecipeNamesList.refresh();
        }
    }

    /**
     * Show the ingredient window when the button is clicked.
     */
    @FXML
    public void onIngredientWindowClick() {
        primaryCtrl.showIngredientsWindow();
    }

    /**
     * Clears the contents of the search bar and resets the sidebar
     */
    private void cancelSearch(){
        try {
            searchField.clear();
            cancelSearchButton.setDisable(true);
            if(searchField.isFocused()){
                searchField.getParent().requestFocus();
            }
            sidebarRecipeNamesList.setItems(storage.getRecipes()); //display all recipes again
        } catch (Exception e){
            errorCtrl.showGenericError(e);
        }
    }

    @FXML
    private void onCancelSearch(){
        cancelSearch();
    }

    /**
     * handles button for to be added window
     */
    public void toggleToBeAdded() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client/modules/ToBeAdded.fxml")
        );
        Parent root = loader.load();

        ToBeAddedCtrl ctrl = loader.getController();

        ctrl.setShoppingList(shoppingList);
        ctrl.setOpenShoppingList(this::showShoppingList);
        List<RecipeIngredient> ris = getSelectedRecipe().getIngredients();
        ctrl.loadFromRecipe(ris);

        Stage popUpStage = new Stage();
        popUpStage.initModality(Modality.APPLICATION_MODAL);
        popUpStage.setTitle("To Be Added");
        popUpStage.setScene(new Scene(root));
        popUpStage.initOwner(recipeView.getScene().getWindow());

        popUpStage.showAndWait();
    }


    @FXML
    private void onAdvancedSearch(){
        primaryCtrl.showSearchWindow();
    }

    /**
     * The final search query step, checks if the favorite toggle is toggled, and if so filters for favorite
     * @param initList The initial filtered list containing the recipes that comply with the query
     * @param favIDs The list of ID's for all favorites
     * @return A list of recipes that comply with all query conditions and favorite toggle.
     */
    public List<Recipe> favFilter(List<Recipe> initList, List<Long> favIDs) {
        if(favoriteCheck.isSelected()) {
            return initList.stream()
                    .filter(x -> favIDs.contains(x.getId()))
                    .toList();
        }
        else return initList;
    }

}
