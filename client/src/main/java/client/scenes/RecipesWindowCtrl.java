package client.scenes;

import java.io.IOException;
import java.util.*;

import java.util.function.BiConsumer;

import client.MyFXML;
import client.data.WebSocketManager;
import client.popups.IngredientPopUpCtrl;
import client.utils.*;
import com.google.inject.Inject;

import client.RecipeListCell;
import client.data.DataManipulator;
import client.data.LocalStorage;
import client.utils.searchUtils.Proposition;
import commons.*;
import javafx.application.Platform;
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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.springframework.messaging.simp.stomp.StompSession;


public class RecipesWindowCtrl {

    private final Insets lineMargin = new Insets(0, 15, 0, 15);

    private final ServerUtils server;

    private final MyFXML fxml;

    private final WebSocketManager socker;

    private volatile StompSession.Subscription titleSubscription;

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
    private TextField totalServingsField;

    @FXML
    private Label totalServingsLabel;

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

    //Drag n drop delay
    int processingDelay = 50;

    private boolean newInstructionAdded = false;

    // This is the Shopping List data that is used in the session.
    private final ShoppingList shoppingList = new ShoppingList();

    private final LocalStorage storage;
    private final DataManipulator dataManipulator;

    private final ErrorCtrl errorCtrl;
    private final SearchService searchService;

    private final NutritionalValue defaultNutritionalValue = new NutritionalValue(0, 0, 0);

    private final PrimaryCtrl primaryCtrl;

    private final RecipeIngredientUnit defaultUnit = RecipeIngredientUnit.fromUnit(FormalUnit.GRAM);

    /**
     * Injectable constructor for RecipesWindowCtrl
     * @param socker WebSocketManager instance
     * @param c ErrorCtrl instance for error
     * @param p Primary Ctrl instance
     * @param storage - The local storage storing recipes and ingredients
     * @param dataManipulator - The data manipulator
     * @param s - The injected search control
     * @param server - The injected serverUtils instance
     * @param fxml - The injected MyFXML instance
     */
    @Inject
    public RecipesWindowCtrl(WebSocketManager socker, ErrorCtrl c,
                             PrimaryCtrl p, LocalStorage storage, DataManipulator dataManipulator,
                             SearchService s, ServerUtils server, MyFXML fxml) {
        this.socker = socker;
        this.errorCtrl = c;
        this.primaryCtrl = p;
        this.storage = storage;
        this.dataManipulator = dataManipulator;
        this.searchService = s;
        this.server = server;
        this.fxml = fxml;
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

        initializeTotalServings();

        // Deactivate recipe specific buttons, since nothing is selected at the start.
        updateRecipeSelectionState(false);

        initializeSceneEvents();
        intializeSearchElements();
    }

    /**
     * Initializes the total servings field to be of type integer and adds listeners.
     */
    private void initializeTotalServings() {
        // Set text field of servings amount selector to integers
        totalServingsField.setTextFormatter(new TextFormatter<> (e -> {
            if (e.getControlNewText().matches("\\d*")) {
                return e;
            } else {
                return null;
            }
        }));

        // New listener for total servings TextField
        // Saves the servings amount
        totalServingsField.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                addRecipeServings();
                totalServingsField.getParent().requestFocus();
            }
        });
        // Focused Property Listener, saves when the TextField loses focus
        totalServingsField.focusedProperty().addListener((obs,
                                                       oldFocused, newFocused) -> {
            if (oldFocused && !newFocused) {
                addRecipeServings();
            }
        });
    }

    /**
     * Sets the total servings label to the correct amount and resets the field.
     */
    private void updateTotalServingsUI() {
        totalServingsLabel.setText(
                "Total servings: " + currentRecipe.getTotalServings()
        );

        totalServingsField.setText("");
    }

    /**
     * This is the greatest method I have ever written.
     * A new thread is created and subscribes to the websocket for title and only title updates via WebSocketManager.
     * Upon notification of any changes, the program will return to the UI thread via Platform.runLater() and will
     * 1. Change the name of the recipe in the localstorage
     * 2. Refresh the sidebar for the name update
     * 3. If the recipe is currently selected, update the name in the title bar
     * THIS MUST ALWAYS BE RUN WHEN RECIPEWINDOW COMES INTO VIEW
     */
    public void startup() {
        if (titleSubscription != null) {
            return;
        }
        Thread subscribeThread = new Thread(() -> {
            titleSubscription = socker.subscribe("/updates/title", TitleUpdate.class, update -> {
                Platform.runLater(() -> {
                    System.out.println("Title of recipe " + update.id() + " Changed!");
                    dataManipulator.changeNameLocal(update.id(), update.newTitle());
//                    dataManipulator.refreshRecipes();
                    // A "softer" refresh is required to keep selection
                    sidebarRecipeNamesList.refresh();
                    if (currentRecipe.getId().equals(update.id())) {
                        recipeNameField.setText(update.newTitle());
                    }
                });
            });
            if (titleSubscription == null ) {
                Platform.runLater(() -> {
                    errorCtrl.showGenericError("Could not subscribe to title changes, server might be down!");
                });
            }
        });
        subscribeThread.setDaemon(true);
        subscribeThread.start();
        dataManipulator.refreshRecipes();
    }

    /**
     * This should be run when RecipesWindowCtrl goes out of view
     * It ensures the app isn't subscribed into any unnecessary updates
     */
    public void shutdown() {
        if (titleSubscription != null) {
            titleSubscription.unsubscribe();
            titleSubscription = null;
            System.out.println("Unsubscribed from title updates.");
        }
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
                                    searchService.query(
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
                            searchService.performComplexQuery(prop, storage.getRecipes()),
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
            searchService.setFavToggle(true);
            List<Recipe> favRecipes = new ArrayList<>();
            for (Recipe r : storage.getRecipes()) {
                if (storage.getFavoriteIDs().contains(r.getId())) {
                    favRecipes.add(r);
                }
            }
            sidebarRecipeNamesList.setItems(FXCollections.observableList(favRecipes));
            sidebarRecipeNamesList.getSelectionModel().select(0);
        } else {
            searchService.setFavToggle(false);
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

        totalServingsField.setDisable(!active);
        totalServingsField.setVisible(active);

        totalServingsLabel.setDisable(!active);
        totalServingsLabel.setVisible(active);

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

        updateTotalServingsUI();

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
            Pair<RecipeIngredientUICtrl, Node> ing = fxml.loadNode(RecipeIngredientUICtrl.class, "client", "modules", "RecipeIngredient.fxml");
            RecipeIngredientUICtrl ingCtrl = ing.getKey();
            Node ingNode = ing.getValue();

            String unitName = "";
            if (ri.getUnit() != null && ri.getUnit().toUnit() != null) {
                unitName = ri.getUnit().toUnit().getDisplayName();
            }
            ingCtrl.setText("• " + ri.getIngredient().getName() + " " + ri.getQuantity().toString() + " " + unitName);

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
                    System.out.println("Ingredient \"" + ri.getIngredient().getName() + "\" deleted successfully");
                    return;
                }
                openRecipe(currentRecipe);
            });
            ingCtrl.setEditIngredient(() -> {             // Editing ingredient Logic!
                handleIngredientInput(ri.getIngredient().getName(), ri.getQuantity(), (newName, newQty) -> {
                    if (newName == null || newName.trim().isEmpty()) {
                        errorCtrl.showGenericError("Ingredients can't have a blank name!");
                        return;
                    }
                    ri.setQuantity(newQty);
                    ri.getIngredient().setName(newName);
                    openRecipe(currentRecipe);
                    server.updateRecipe(currentRecipe);
                    System.out.println("Ingredient \"" + ri.getIngredient().getName() + "\" updated successfully");
                });
            });
        }
        //menu button for adding an ingredient --> shows all currently saved ingredients! On click: add it to recipe.
        SplitMenuButton addButton = new SplitMenuButton("Add Ingredient");
        recipeView.getChildren().add(addButton);

        addButton.setOnAction((a) -> {
            Optional<Ingredient> parsed = primaryCtrl.getIngredientsWindowCtrl().handlePlusButtonPress();
            // Use the dialog instead of hardcoding 0.0
            parsed.ifPresent(this::openQuantityDialog);
        });

        addButton.setOnShowing((a) -> {
            addButton.getItems().clear(); // Clear to avoid duplicate menu items
            storage.getIngredients().forEach(ingredient -> {
                MenuItem menu = new MenuItem(ingredient.getName());
                menu.setId(ingredient.getId().toString());
                menu.setOnAction((actionEvent) -> {
                    // Open our new dialog for the existing ingredient
                    openQuantityDialog(ingredient);
                });
                addButton.getItems().add(menu);
            });
        });
        recipeView.requestLayout();
    }

    /**
     * Updates the current recipe and refreshes it to reflect changes made
     */
    public void updateRefresh() {
        Recipe updated = server.updateRecipe(currentRecipe);
        if (updated == null) {
            errorCtrl.showServerUnavailableError();
            return;
        }
        applyUpdatedRecipe(updated);
        openRecipe(currentRecipe);

    }


    /**
     * Handling input window for ingredient editing
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
            Pair<RecipeInstructionUICtrl, Node> ing = fxml.loadNode(RecipeInstructionUICtrl.class, "client", "modules", "RecipeInstruction.fxml");
            RecipeInstructionUICtrl instCtrl = ing.getKey();
            Node ingNode = ing.getValue();
            int currentIndex = i;

            // Drag and drop trigger
            detectDrag(ingNode, currentIndex, recipeInstructions);
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
                if(!instruction.equals("New Instruction")) {
                    System.out.println("Instruction edited successfully");
                }
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
            System.out.println("Added instruction");
        });
        recipeView.requestLayout();
    }

    /**
     * Drag and drop function, seperated as a helper. LoadSteps is already gigantic
     * @param n The node, or in this case RecipeInstruction node to move and compare to
     * @param currentIndex The current index of the this node/instruction
     * @param recipeIngredients The list of recipeInredients, to be altered when dragging
     */
    public void detectDrag(Node n, int currentIndex, List<String> recipeIngredients) {
        // Things to do when instructions is dragged
        // Basically just detect it as dragged, and copy the index to the 'clipboard'
        // A clipboard is a required dataformat for the drag board...... These names man
        n.setOnDragDetected(event -> {
            Dragboard db = n.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent index = new ClipboardContent();
            index.putString(String.valueOf(currentIndex));
            db.setContent(index);
            event.consume();
            System.out.println("Drag detected! Moving instruction at index " + currentIndex);
        });
        // Check for any other node (Button, label, whatever) if its eligible
        n.setOnDragOver(dragEvent -> {
            if(dragEvent.getGestureSource() != n && dragEvent.getDragboard().hasString()) {
                dragEvent.acceptTransferModes(TransferMode.MOVE);
            }
            dragEvent.consume();
        });

        // Change border of target to green line for emphasis
        // This depends on if we move up or down
        n.setOnDragEntered(dragEvent -> {
            if(dragEvent.getGestureSource() != n && dragEvent.getDragboard().hasString()) {
                int initIndex = Integer.parseInt(dragEvent.getDragboard().getString());
                String border = "-fx-border-style: solid outside; -fx-border-color: GREENYELLOW;";
                if(initIndex < currentIndex) {
                    // up right down left (0 0 3 0) = draw only top
                    n.setStyle(border + "-fx-border-width: 0 0 3 0;");
                } else if(initIndex > currentIndex) {
                    n.setStyle(border + "-fx-border-width: 3 0 0 0;");
                }
            }
        });

        // Reset style when leaving target
        n.setOnDragExited(dragEvent -> {
            if(dragEvent.getGestureSource() != n && dragEvent.getDragboard().hasString()) {
                n.setStyle("");
            }
        });
        // When dropped on another instruction, remove original, and set on new target index
        n.setOnDragDropped(dragEvent -> {
            boolean succes = false;
            Dragboard db = dragEvent.getDragboard();
            int initIndex = Integer.parseInt(db.getString());
            if(initIndex != currentIndex) {
                String movedItem = recipeIngredients.remove(initIndex);
                recipeIngredients.add(currentIndex,movedItem);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() ->
                                updateRefresh());
                    }
                },
                        processingDelay
                );

                succes = true;
            }
            dragEvent.setDropCompleted(succes);
            dragEvent.consume();

        });
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
        if (currentRecipe == null) {
            currentRecipe = sidebarRecipeNamesList.getSelectionModel().getSelectedItem();
            return;
        }
        String newName = createCopyName(currentRecipe.getName());
        Recipe clone = cloneRecipe(currentRecipe, newName);
        Recipe savedRecipe = server.addRecipe(clone);
        if(savedRecipe != null){
            storage.getRecipes().add(savedRecipe);
            sidebarRecipeNamesList.getSelectionModel().select(savedRecipe);
            System.out.println("Cloned recipe \"" + currentRecipe.getName() + "\" to \"" + newName + "\"");
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
        int totalServings = original.getTotalServings();
        List<String> stepsCopy = new ArrayList<>(original.getPreparationSteps());

        Recipe clone = new Recipe(newName, totalServings, null, stepsCopy);

        List<RecipeIngredient> ingredientsCopy = new ArrayList<>();
        for (RecipeIngredient ri : original.getIngredients()) {
            Ingredient oldIng = ri.getIngredient();
            Ingredient newIng = new Ingredient(oldIng.getName(), defaultNutritionalValue);
            RecipeIngredient newRi = new RecipeIngredient(clone, newIng, ri.getQuantity(), defaultUnit);
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
                0,
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
     * Add the servings entered to the total servings of a recipe.
     */
    public void addRecipeServings() {
        if (currentRecipe == null) {
            return;
        }

        String text = totalServingsField.getText();
        if (text.isEmpty()) {
            return;
        }

        int servings = Integer.parseInt(text);

        if (servings < 0) {
            errorCtrl.showGenericError("The amount of servings must be greater than or equal to 0!");
            return;
        }

        currentRecipe.setTotalServings(servings);

        Recipe updatedRecipe = server.updateRecipe(currentRecipe);
        if (updatedRecipe != null) {
            applyUpdatedRecipe(updatedRecipe);

            openRecipe(currentRecipe);

            totalServingsField.clear();
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
        return selectedRecipes.getFirst();
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

    /**
     * Displays a modal dialog for editing an ingredient and returns the entered values
     *
     * @param initialName ingredient name
     * @param initialQuantity ingredient quantity
     * @return Optional containing the name and quantity if confirmed, otherwise Optional is empty
     */
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

    /**
     * Opens a new window displaying the current shopping list.
     */
    private void showShoppingList() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client/scenes/ShoppingList.fxml")
        );
        Pair<ShoppingListCtrl, Parent> shopListPair = fxml.load(ShoppingListCtrl.class, "client", "scenes", "ShoppingList.fxml");
        Parent root = shopListPair.getValue();

        ShoppingListCtrl ctrl = shopListPair.getKey();
        ctrl.setErrorCtrl(errorCtrl);
        ctrl.setAndShowShoppingList(shoppingList);

        Stage shoppingListStage = new Stage();
        shoppingListStage.setTitle("Shopping List");
        shoppingListStage.setScene(new Scene(root));

        shoppingListStage.show();

    }

    /**
     * Updates the current recipe
     * @param updated the updated recipe, or null to leave unchanged
     */
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
                System.out.println("Removed \""+currentRecipe.getName()+"\" from favorites!");
            } else {
                storage.getFavoriteIDs().add(currentRecipe.getId());
                favoriteImage.setImage(favorite);
                System.out.println("Added \""+currentRecipe.getName()+"\" to favorites!");
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
     * Handles button for to be added window
     */
    public void toggleToBeAdded() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client/modules/ToBeAdded.fxml")
        );
        Parent root = loader.load();

        ToBeAddedCtrl ctrl = loader.getController();

        ctrl.setShoppingList(shoppingList);
        ctrl.setSourceRecipeName(getSelectedRecipe().getName());
        ctrl.setOpenShoppingList(this::showShoppingList);
        ctrl.loadFromRecipe(getSelectedRecipe().getIngredients());

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

    private void openQuantityDialog(Ingredient ingredient) {
        try {
            // 1. Setup the Loader
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/modules/QuantityUnitPopUp.fxml"));
            Parent root = loader.load();

            // 2. Setup the Window (Stage)
            Stage popUpStage = new Stage();
            popUpStage.initModality(Modality.APPLICATION_MODAL); // Blocks interaction with main window
            popUpStage.initOwner(recipeView.getScene().getWindow()); // Links to main window
            popUpStage.setTitle("Add Quantity for " + ingredient.getName());

            // 3. Setup the Controller
            QuantityUnitSelectionCtrl controller = loader.getController();
            controller.setStage(popUpStage);

            // 4. Show and Wait
            popUpStage.setScene(new Scene(root));
            popUpStage.showAndWait(); // Execution stops here until window is closed

            // 5. Handle the Result
            if (controller.isOkClicked()) {
                RecipeIngredient newEntry = new RecipeIngredient(
                        this.currentRecipe,  // The current recipe you are editing
                        ingredient,           // The ingredient from your list/search
                        controller.getQuantity(),
                        controller.getUnit()
                );

                // Add to your recipe's internal list
                currentRecipe.getIngredients().add(newEntry);

                // Refresh the UI to show the new item
                updateRefresh();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
