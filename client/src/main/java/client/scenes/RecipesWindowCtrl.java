package client.scenes;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import client.MyFXML;
import client.data.WebSocketManager;
import client.popups.IngredientPopUpCtrl;
import client.utils.*;
import client.data.TranslationManager;
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
import javafx.scene.input.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;


public class RecipesWindowCtrl {

    private final Insets lineMargin = new Insets(0, 15, 0, 15);

    private final ServerUtils server;

    private final MyFXML fxml;

    private final WebSocketManager socker;

    // Make sure to change to *flagPT.jpg* after we made him mad
    private final String francisco = "flatPT.jpg";

    private volatile ExecutorService subscriptionExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    private Long previousRecipeSubscription = -1L;

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
    private Label scaleLabel;

    @FXML TextField scaleTextField;

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
    private ImageView kcalIcon;

    @FXML
    private Label advancedSearchButton;

    @FXML
    private MenuButton languageMenu;

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
    private final int fontSize = 16;

    // global recipe scaling factor
    double recipeScale = 1.0;
    double scaleLimit = 1000.0;

    int hundredtwenty = 120;
    int five = 5;
    int ten = 10;

    private boolean newInstructionAdded = false;

    private boolean ignoreSideBarSelectionEvent = false;

    // This is the Shopping List data that is used in the session.
    private final ShoppingList shoppingList = new ShoppingList();

    private final LocalStorage storage;
    private final DataManipulator dataManipulator;

    private final ErrorCtrl errorCtrl;
    private final SearchService searchService;

    private final NutritionalValue defaultNutritionalValue = new NutritionalValue(0, 0, 0);

    private final PrimaryCtrl primaryCtrl;

    private final RecipeIngredientUnit defaultUnit = RecipeIngredientUnit.fromUnit(FormalUnit.GRAM);

    private final TranslationManager tm;

    private Locale activeLocale = Locale.ENGLISH;
    private String activeFlagPath = "/client/images/flagUS.png";

    private CustomMenuItem englishItem;
    private CustomMenuItem dutchItem;
    private CustomMenuItem slovakItem;
    private CustomMenuItem greekItem;
    private CustomMenuItem portugueseItem;
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
     *             @param tm the translation manager used to localize UI text
     */
    // CHECKSTYLE:OFF
    @Inject
    public RecipesWindowCtrl(WebSocketManager socker, ErrorCtrl c,
                             PrimaryCtrl p, LocalStorage storage, DataManipulator dataManipulator,
                             SearchService s, ServerUtils server, MyFXML fxml, TranslationManager tm) {
        this.socker = socker;
        this.errorCtrl = c;
        this.primaryCtrl = p;
        this.storage = storage;
        this.dataManipulator = dataManipulator;
        this.searchService = s;
        this.server = server;
        this.fxml = fxml;
        this.tm = tm;
    }
    // CHECKSTYLE:ON

    /**
     * Initializes the sidebar items (Recipe names) to track the ObservableList items
     */
    public void initialize() {
        tm.bundleProperty().addListener((obs, oldBundle, newBundle) -> {
            applyTexts();
            refreshDynamicViews();
        });
        dataManipulator.loadFavs();
        favorite = new Image(getClass().getResource("/client/images/favorite.png").toExternalForm());
        unFavorite = new Image(getClass().getResource("/client/images/not_favorite.png").toExternalForm());
        sidebarRecipeNamesList.setItems(storage.getRecipes());
        sidebarRecipeNamesList.setCellFactory(lc -> new RecipeListCell(storage.getFavoriteIDs(), favorite));
        sidebarRecipeNamesList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if(ignoreSideBarSelectionEvent) return;
                    if (newSelection != null) {
                        openRecipe(newSelection, true);
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
        initializeScale();

        // Deactivate recipe specific buttons, since nothing is selected at the start.
        updateRecipeSelectionState(false);
        setUpLanguageDropdown();
        applyTexts();
        updateTotalServingsLabelText();
        initializeSceneEvents();
        intializeSearchElements();

        kcalIcon.setPickOnBounds(true);

        Tooltip tooltip = new Tooltip("0 kcal/100g");
        tooltip.setShowDelay(javafx.util.Duration.millis(processingDelay*2)); // Instant popup
        Tooltip.install(kcalIcon, tooltip);
        kcalIcon.setOnMouseEntered(e -> System.out.println("Mouse is over the leaf!"));
    }

    /**
     * Initialize scale variable with a listener for changes
     */
    private void initializeScale() {
        // New listener for scale
        // Saves the servings amount
        scaleTextField.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    updateScale(Double.parseDouble(scaleTextField.getText()));
                    openRecipe(currentRecipe);
                } catch (NumberFormatException err) {
                    if (errorCtrl != null) {
                        errorCtrl.showGenericError("Scale must be a number.");
                    }
                    return;
                }
                scaleTextField.getParent().requestFocus();
            }
        });
        // Focused Property Listener, saves when the TextField loses focus
        scaleTextField.focusedProperty().addListener((obs,
                                                          oldFocused, newFocused) -> {
            if (oldFocused && !newFocused) {
                try {
                    updateScale(Double.parseDouble(scaleTextField.getText()));
                    openRecipe(currentRecipe);
                } catch (NumberFormatException err) {
                    if (errorCtrl != null) {
                        errorCtrl.showGenericError("Scale must be a number.");
                    }
                }
            }
        });

    }

    /**
     * Update the scale text
     */
    private void updateScale(double newScale) {
        if(newScale < scaleLimit) {
            scaleLabel.setVisible(true);
            scaleTextField.setVisible(true);
            recipeScale = newScale;
            if(currentRecipe != null) {
                scaleTextField.setText(String.valueOf(recipeScale));
            }
        }
        else {
            errorCtrl.showGenericError("Scale limit ("+scaleLimit+") exceeded!");
        }
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

    public void refreshNutritionTooltip(Recipe recipe) {
        // If there's no recipe at all (sidebar cleared), hide the icon
        if (recipe == null) {
            kcalIcon.setVisible(false);
            return;
        }

        // Ensure the icon is visible because a recipe IS selected
        kcalIcon.setVisible(true);

        // Calculate the density
        double density = recipe.calculateRecipeKcalPer100g(recipe);

        // Create the text (e.g., "0 kcal/100g" or "145 kcal/100g")
        String tooltipText = String.format("%.0f kcal/100g", density);

        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(javafx.util.Duration.millis(processingDelay));

        // Force update the Tooltip
        Tooltip.uninstall(kcalIcon, null);
        Tooltip.install(kcalIcon, tooltip);
    }
    /**
     * Sets the total servings label to the correct amount and resets the field.
     */
    private void updateTotalServingsUI() {
        totalServingsLabel.setText(tm.tr("label.totalServings", currentRecipe.getTotalServings() * recipeScale));
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
        initializeTitleSubscription();
        if (previousRecipeSubscription != -1) initializeRecipeSubscription(previousRecipeSubscription, true);
        initializeRecipeAdditionSubscription();
        initializeRecipeDeletionSubscription();
        dataManipulator.refreshRecipes();
    }

    /**
     * This should be run when RecipesWindowCtrl goes out of view
     * It ensures the app isn't subscribed into any unnecessary updates
     */
    public void shutdown() {
        // Unsubscribe from all websockets
        socker.unsubscribe("/updates/title");
        socker.unsubscribe("/updates/recipe/" + previousRecipeSubscription);
        socker.unsubscribe("/updates/recipe-addition");
        socker.unsubscribe("/updates/recipe-deletion");

    }

    /**
     * Adds the title subscription the executor's thread.
     * When a title change occurs, it is reflected in the sidebar.
     */
    public void initializeTitleSubscription(){
        subscriptionExecutor.submit(() -> {
            socker.subscribe("/updates/title", TitleUpdate.class, update -> {
                Platform.runLater(() -> {
                    System.out.println("Title of recipe " + update.id() + " Changed!");
                    dataManipulator.changeNameLocal(update.id(), update.newTitle());
                    // A "softer" refresh is required to keep selection
                    sidebarRecipeNamesList.refresh();
                    if(currentRecipe == null) return;
                });
            });
        });
    }

    /**
     * Subscribes to any update in the recipe with the specified ID, including (Recipe)Ingredient
     * @param id Id of the recipe we want to subscribe to
     * @param forceResubscribe Forces resubscription to the websocket even if its to the same recipe
     */
    public void initializeRecipeSubscription(Long id, boolean forceResubscribe){
        if (previousRecipeSubscription.equals(id) && !forceResubscribe) return;
        // Unsubscribe from previous recipe
        if (previousRecipeSubscription != -1) socker.unsubscribe("/updates/recipe/" + previousRecipeSubscription);
        subscriptionExecutor.submit(() -> {
            socker.subscribe("/updates/recipe/" + Long.toString(id), RecipeUpdate.class, update -> {
                Platform.runLater(() -> {
                    System.out.println("Recipe " + update.id() + " changed.");
                    dataManipulator.updateRecipe(update.recipe());
                    if(currentRecipe.getId().equals(update.id())) currentRecipe = update.recipe();
                    openRecipe(currentRecipe);
                });
            });
        });
        previousRecipeSubscription = id;
    }

    /**
     * Subscribes to all new recipes, and locally stores the new recipes using the DataManipulator
     */
    public void initializeRecipeAdditionSubscription() {
        subscriptionExecutor.submit(() -> {
            socker.subscribe("/updates/recipe-addition", RecipeAddition.class, update -> {
                Platform.runLater(() -> {
                    ignoreSideBarSelectionEvent = true;
                    dataManipulator.updateRecipe(update.recipe());
                    sidebarRecipeNamesList.getSelectionModel().select(currentRecipe);
                    ignoreSideBarSelectionEvent = false;
                });
            });
        });
    }

    /**
     * Subscribes to all deleted recipes, and locally deletes the recipe with the specified id using the DataManipulator
     */
    public void initializeRecipeDeletionSubscription(){
        subscriptionExecutor.submit(() -> {
            socker.subscribe("/updates/recipe-deletion", RecipeDeletion.class, update -> {
                Platform.runLater(() -> {
                    ignoreSideBarSelectionEvent = true;
                    if(currentRecipe != null && update.id().equals(currentRecipe.getId())){
                        clearRecipeView();
                        sidebarRecipeNamesList.getSelectionModel().clearSelection();
                        errorCtrl.showErrorPopup("Recipe deleted", "", "Someone deleted the recipe you were viewing ):");
                    }
                    dataManipulator.deleteRecipeLocal(update.id());
                    ignoreSideBarSelectionEvent = false;
                });
            });
        });
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

        scaleLabel.setDisable(!active);
        scaleLabel.setVisible(active);

        scaleTextField.setDisable(!active);
        scaleTextField.setVisible(active);

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

        languageMenu.setDisable(!active);
        languageMenu.setVisible(active);
        languageMenu.setManaged(active);

        if (!active) {
            Label noRecipeSelectedLabel = new Label(tm.tr("label.noRecipeSelected"));
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
     * @param updateRecipe True iff the recipe should be updated from the server
     */
    public void openRecipe(Recipe recipe, boolean updateRecipe) {
        this.currentRecipe = recipe;
        ignoreSideBarSelectionEvent = true;
        if(updateRecipe){
            this.currentRecipe = dataManipulator.refreshRecipe(recipe.getId());
            recipe = this.currentRecipe;
        }
        sidebarRecipeNamesList.getSelectionModel().select(currentRecipe);
        ignoreSideBarSelectionEvent = false;

        recipeView.getChildren().clear();
        loadIngredients(currentRecipe.getIngredients());
        Separator sep = new Separator();
        recipeView.getChildren().add(sep);
        VBox.setMargin(sep, lineMargin);
        loadSteps(currentRecipe.getPreparationSteps());
        // Favorites
        if (currentRecipe != null && storage.getFavoriteIDs() != null) {
            if (storage.getFavoriteIDs().contains(currentRecipe.getId())) {
                favoriteImage.setImage(favorite);
            } else {
                favoriteImage.setImage(unFavorite);
            }
        }
        initializeRecipeSubscription(currentRecipe.getId(), false);

        recipeNameField.setText(currentRecipe.getName());

        // Activate recipe specific buttons
        updateRecipeSelectionState(true);

        updateTotalServingsUI();

        refreshNutritionTooltip(currentRecipe);

        updateScale(recipeScale);

    }

    /**
     * Loads the content of the provided recipe to the recipeView UI element
     * @param recipe Recipe to load
     */
    public void openRecipe(Recipe recipe){
        openRecipe(recipe, false);
    }

    /**
     * Loads the ingredients within a list to the recipeView UI element
     * @param recipeIngredients - A list of RecipeIngredients
     */
    public void loadIngredients(List<RecipeIngredient> recipeIngredients) {
        Label ingredientsLabel = new Label(tm.tr("label.ingredients"));
        ingredientsLabel.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        recipeView.getChildren().add(ingredientsLabel);

        if (recipeIngredients.isEmpty()) {
            recipeView.getChildren().add(new Label(tm.tr("label.noIngredients")));
        }

        for (int i = 0; i < recipeIngredients.size(); ++i) {
            RecipeIngredient ri = recipeIngredients.get(i);
            Pair<RecipeIngredientUICtrl, Node> ing = fxml.loadNode(RecipeIngredientUICtrl.class, "client", "modules", "RecipeIngredient.fxml");
            RecipeIngredientUICtrl ingCtrl = ing.getKey();
            Node ingNode = ing.getValue();

            ingCtrl.setText(formatIngredientText(ri));

            ingCtrl.setIndex(i);
            VBox.setVgrow(ingNode, Priority.ALWAYS);
            recipeView.getChildren().add(ingNode);
            Long ingredientId = (ri.getIngredient() != null) ? ri.getIngredient().getId() : null;
            long recipeId = currentRecipe.getId();

            ingCtrl.setDeleteIngredient(() -> { //remove ingredient from local recipe
                currentRecipe.getIngredients().remove(ri);
                if (ingredientId != null) {
                    server.deleteIngredient(recipeId, ingredientId);
                }
                server.updateRecipe(currentRecipe);
                openRecipe(currentRecipe);
                refreshNutritionTooltip(currentRecipe);
            });
            ingCtrl.setEditIngredient(() -> {
                showIngredientPopUp(ri.getIngredient().getName(), ri.getQuantity(), ri.getUnit())
                                .ifPresent(result -> {
                                    try {
                                        ri.setQuantity(Double.parseDouble(result.getKey()));
                                        Unit selectedUnit = result.getValue();
                                        ri.setUnit(selectedUnit == null ? null : RecipeIngredientUnit.fromUnit(selectedUnit));
                                        refreshNutritionTooltip(currentRecipe);
                                        openRecipe(currentRecipe);
                                        server.updateRecipe(currentRecipe);
                                    } catch (NumberFormatException err) {
                                        errorCtrl.showGenericError(tm.tr("error.quantityMustBeNumber"));
                                    }
                                });
            });
        }
        //menu button for adding an ingredient --> shows all currently saved ingredients! On click: add it to recipe.
        // Still called here, but logic is moved to a helper
        setupAddIngredientButton();
        recipeView.requestLayout();
    }


    /**
     * Determines the display string for an ingredient's unit.
     *
     * @param ri The recipe ingredient to extract the unit name from.
     * @return A string representing the unit name, or "No unit" if null.
     */
    private String getUnitDisplayName(RecipeIngredient ri) {
        if (ri.getUnit() == null) return tm.tr("label.noUnit");
        Unit actualUnit = ri.getUnit().toUnit();
        if (actualUnit != null) return actualUnit.getDisplayName();

        String informal = ri.getUnit().getInformalUnitName();
        if (informal != null) return informal;

        String formal = ri.getUnit().getFormalUnitName();
        return (formal != null) ? formal : "";
    }


    /**
     * Configures and adds the "Add Ingredient" button to the UI.
     * Handles both the primary button action and the dropdown menu population.
     */
    private void setupAddIngredientButton() {
        SplitMenuButton addButton = new SplitMenuButton(tm.tr("button.addIngredient"));
        recipeView.getChildren().add(addButton);
        addButton.setOnAction(a -> {
            primaryCtrl.getIngredientsWindowCtrl().handlePlusButtonPress()
                    .ifPresent(this::openQuantityDialog);
        });
        addButton.setOnShowing(a -> {
            dataManipulator.refreshIngredients();
            addButton.getItems().clear();
            storage.getIngredients().forEach(ingredient -> {
                MenuItem menu = new MenuItem(ingredient.getName());
                menu.setOnAction(e -> openQuantityDialog(ingredient));
                addButton.getItems().add(menu);
            });
        });
    }

    /**
     * Determines the text to be displayed within the recipe view
     * @param ri The recipeIngredient to handle
     * @return a string describing the recipeIngredient and its attributes
     */
    public String formatIngredientText(RecipeIngredient ri) {
        String unitName = "";
        boolean isInformal = false;
        if (ri.getUnit() != null && ri.getUnit().toUnit() != null) {
            unitName = ri.getUnit().toUnit().getDisplayName();
            isInformal = ri.getUnit().toUnit() instanceof InformalUnit;

        }
        double quantity;
        if(isInformal || recipeScale == 1.0) {
            quantity = ri.getQuantity();
        }
        else {
            quantity = ri.getQuantity() * recipeScale;
        }
        return "• " + ri.getIngredient().getName() + " " + quantity + " " + unitName;
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
        refreshNutritionTooltip(currentRecipe);
    }


//    /**
//     * Handling input window for ingredient editing
//     * @param initName the initial name value to be displayed
//     * @param initQty  the initial quantity value to be displayed
//     * @param handler  the consumer that handles to call back to the value's usage
//     */
//    public void handleIngredientInput(String initName, double initQty, BiConsumer<String, Double> handler) {
//        showIngredientPopUp(initName, String.valueOf(initQty)).ifPresent(pair -> {
//            String name = pair.getKey();
//            String quantityText = pair.getValue();
//            double quantity;
//            try {
//                quantity = Double.parseDouble(quantityText);
//            } catch (NumberFormatException err) {
//                if (errorCtrl != null) {
//                    errorCtrl.showGenericError("Quantity must be a number.");
//                }
//                return;
//            }
//            handler.accept(name, quantity);
//        });
//    }

    /**
     * Loads the instructions within a list to the recipeView UI element
     *
     * @param recipeInstructions - A list of Strings (The recipe instructions)
     */
    // This might look like code duplication now, but the way we handle ingredients and steps might change dramatically in the future
    public void loadSteps(List<String> recipeInstructions) {
        Label stepsLabel = new Label(tm.tr("label.steps"));
        stepsLabel.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        recipeView.getChildren().add(stepsLabel);

        if (recipeInstructions.isEmpty()) {
            recipeView.getChildren().add(new Label(tm.tr("label.noSteps")));
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
                if(!instruction.equals(tm.tr("instruction.new"))) {
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
        Button addButton = new Button(tm.tr("button.addInstruction"));
        recipeView.getChildren().add(addButton);
        addButton.setOnAction(e -> {
            recipeInstructions.add(tm.tr("label.newInstruction"));
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
        refreshNutritionTooltip(null);
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
            ignoreSideBarSelectionEvent = true;
            storage.getRecipes().add(savedRecipe);
            sidebarRecipeNamesList.getSelectionModel().select(savedRecipe);
            ignoreSideBarSelectionEvent = false;
            openRecipe(savedRecipe);
        } else {
            errorCtrl.showGenericError(tm.tr("error.noRecipeToClone"));
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
                tm.tr("recipe.new"),
                0,
                new ArrayList<>(),
                new ArrayList<>()
        );
        //calls the fixed ServerUtils method addRecipe
        Optional<Recipe> savedRecipe = dataManipulator.addRecipe(newRecipe);
        savedRecipe.ifPresent(recipe -> {
            ignoreSideBarSelectionEvent = true;
            sidebarRecipeNamesList.getSelectionModel().select(recipe);
            ignoreSideBarSelectionEvent = false;
            openRecipe(recipe);
        });
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
            errorCtrl.showGenericError(tm.tr("error.servingsNonNegative"));
            return;
        }

        currentRecipe.setTotalServings(servings);
        updateTotalServingsLabelText();

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
        ignoreSideBarSelectionEvent = true;
        dataManipulator.deleteRecipe(hit);
        ignoreSideBarSelectionEvent = false;
        if(getSelectedRecipe() == null) return;
        openRecipe(getSelectedRecipe());
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
    private Optional<Pair<String, Unit>> showIngredientPopUp(String initialName, Double initialQuantity,
                                                             RecipeIngredientUnit unit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/modules/IngredientPopUp.fxml"));
            Parent root = loader.load();
            IngredientPopUpCtrl ctrl = loader.getController();

            Stage popUpStage = new Stage();
            popUpStage.initModality(Modality.APPLICATION_MODAL);
            popUpStage.setTitle(tm.tr("title.EditInstruction"));
            popUpStage.setScene(new Scene(root));

            ctrl.setStage(popUpStage);
            ctrl.setInitialValues(initialName, initialQuantity, unit);

            popUpStage.showAndWait();

            if (ctrl.isOkClicked()) {
                refreshNutritionTooltip(currentRecipe);
                // Pair holds the Quantity String (Key) and the Unit Enum/Object (Value)
                return Optional.of(new Pair<>(ctrl.getQuantity(), ctrl.getSelectedUnit()));
            }

        } catch (IOException e) {
            if (errorCtrl != null) {
                errorCtrl.showGenericError(e);
            } else {
                e.printStackTrace();
            }
        }
        return Optional.empty();
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
        shoppingListStage.setTitle(tm.tr("label.shoppinglist"));
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
        Pair<ToBeAddedCtrl, Parent> addIngPair = fxml.load(ToBeAddedCtrl.class, "client", "modules", "ToBeAdded.fxml");
        Parent root = addIngPair.getValue();
        ToBeAddedCtrl ctrl =addIngPair.getKey();

        ctrl.setShoppingList(shoppingList);
        ctrl.setSourceRecipeName(getSelectedRecipe().getName());
        ctrl.setOpenShoppingList(this::showShoppingList);
        ctrl.loadFromRecipe(getSelectedRecipe().getIngredients());

        Stage popUpStage = new Stage();
        popUpStage.initModality(Modality.APPLICATION_MODAL);
        popUpStage.setTitle(tm.tr("label.tobeadded"));
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
        // 1. Setup the Loader
        Pair<QuantityUnitSelectionCtrl, Parent> pair =
                fxml.load(QuantityUnitSelectionCtrl.class, "client", "modules", "QuantityUnitPopUp.fxml");

        Parent root = pair.getValue();
        QuantityUnitSelectionCtrl ctrl = pair.getKey();

        // 2. Setup the Window (Stage)
        Stage popUpStage = new Stage();
        popUpStage.initModality(Modality.APPLICATION_MODAL); // Blocks interaction with main window
        popUpStage.initOwner(recipeView.getScene().getWindow()); // Links to main window
        popUpStage.setTitle(tm.tr("title.addQuantityFor", ingredient.getName()));

        // 3. Setup the Controller
        ctrl.setStage(popUpStage);

        // 4. Show and Wait
        popUpStage.setScene(new Scene(root));
        popUpStage.showAndWait(); // Execution stops here until window is closed

        // 5. Handle the Result
        if (ctrl.isOkClicked()) {
            RecipeIngredient newEntry = new RecipeIngredient(
                    this.currentRecipe,  // The current recipe you are editing
                    ingredient,           // The ingredient from your list/search
                    ctrl.getQuantity(),
                    ctrl.getUnit()
            );

            // Add to your recipe's internal list
            currentRecipe.getIngredients().add(newEntry);
            // Refresh the UI to show the new item
            updateRefresh();
            refreshNutritionTooltip(currentRecipe);
        }
    }

    @FXML
    private MenuButton addIngredientMenu;

    private Locale currentLocale = Locale.ENGLISH;

    private final int sizeFlag = 16;
    private final int sizeFlag2 = 20;


    private void setUpLanguageDropdown() {
        // 1. Create Labels (Nodes) that can detect hover
        Label engLabel = new Label("English", icon("/client/images/flagUS.png"));
        Label nlLabel = new Label("Nederlands", icon("/client/images/flagNL.png"));
        Label skLabel = new Label("Slovencina", icon("/client/images/flagSK.png"));
        Label grLabel = new Label("Ελληνικά", icon("/client/images/flagGR.jpg"));
        Label ptLabel = new Label("Português", icon("/client/images/" + francisco));

        // Set styling so the hover area fills the menu width
        engLabel.setMinWidth(hundredtwenty);
        nlLabel.setMinWidth(hundredtwenty);
        engLabel.setPadding(new Insets(five, ten, five, ten));
        nlLabel.setPadding(new Insets(five, ten, five, ten));
        skLabel.setPadding(new Insets(five, ten, five, ten));
        grLabel.setPadding(new Insets(five, ten, five, ten));
        ptLabel.setPadding(new Insets(five, ten, five, ten));


        // 2. Initialize the CustomMenuItems with these labels
        englishItem = new CustomMenuItem(engLabel);
        dutchItem = new CustomMenuItem(nlLabel);
        slovakItem = new CustomMenuItem(skLabel);
        greekItem = new CustomMenuItem(grLabel);
        portugueseItem = new CustomMenuItem(ptLabel);

        // 3. Attach Hover Listeners directly to the Labels
        engLabel.setOnMouseEntered(e -> tm.setLanguage(Locale.ENGLISH));
        nlLabel.setOnMouseEntered(e -> tm.setLanguage(new Locale("nl")));
        skLabel.setOnMouseEntered(e -> tm.setLanguage(new Locale("sk")));
        grLabel.setOnMouseEntered(e -> tm.setLanguage(new Locale("gr")));
        ptLabel.setOnMouseEntered(e -> tm.setLanguage(new Locale("pt")));

        // 4. Revert to the "Official" language when the menu is closed
        addIngredientMenu.setOnHidden(e -> tm.setLanguage(activeLocale));

        // 5. Standard Click Logic (Actions)
        englishItem.setOnAction(e -> setLanguage(Locale.ENGLISH, "/client/images/flagUS.png"));
        dutchItem.setOnAction(e -> setLanguage(new Locale("nl"), "/client/images/flagNL.png"));
        slovakItem.setOnAction(e -> setLanguage(new Locale("sk"), "/client/images/flagSK.png"));
        greekItem.setOnAction(e -> setLanguage(new Locale("gr"), "/client/images/flagGR.jpg"));
        portugueseItem.setOnAction(e -> setLanguage(new Locale("pt"), "/client/images/" + francisco));

        addIngredientMenu.getItems().setAll(englishItem, dutchItem, slovakItem, greekItem, portugueseItem);

        // Default starting state
        setLanguage(Locale.ENGLISH, "/client/images/flagUS.png");

        MenuItem visualEng = new MenuItem("", icon("/client/images/flagUS.png"));
        MenuItem visualNl = new MenuItem("", icon("/client/images/flagNL.png"));
        MenuItem visualSk = new MenuItem("", icon("/client/images/flagSK.png"));
        MenuItem visualGr = new MenuItem("", icon("/client/images/flagGR.jpg"));
        MenuItem visualPt = new MenuItem("", icon("/client/images/flagPT.jpg"));

        visualEng.setOnAction(e -> languageMenu.setGraphic(icon("/client/images/flagUS.png")));
        visualNl.setOnAction(e -> languageMenu.setGraphic(icon("/client/images/flagNL.png")));
        visualSk.setOnAction(e -> languageMenu.setGraphic(icon("/client/images/flagSK.png")));
        visualGr.setOnAction(e -> languageMenu.setGraphic(icon("/client/images/flagGR.jpg")));
        visualPt.setOnAction(e -> languageMenu.setGraphic(icon("/client/images/" + francisco)));

        languageMenu.getItems().setAll(visualEng, visualNl, visualSk, visualGr, visualPt);
        languageMenu.setGraphic(icon("/client/images/flagUS.png")); //initial language = english
        languageMenu.setText("");

    }

    private void setLanguage(Locale locale, String flagPath) {
        this.activeLocale = locale;
        this.activeFlagPath = flagPath;

        tm.setLanguage(locale);

        // Update the dropdown button look based on language
        addIngredientMenu.setGraphic(icon(flagPath));
        addIngredientMenu.setText(
                tm.tr("menu.language." + locale.getLanguage())
        );

        languageMenu.setText(""); //we can add the language name if we want??
    }

    private void refreshDynamicViews() {
        recipeView.getChildren().clear();
        if (currentRecipe != null) {
            openRecipe(currentRecipe);
        } else {
            updateRecipeSelectionState(false);
        }
    }

    private int getCurrentServings() {
        return (currentRecipe == null) ? 0 : currentRecipe.getTotalServings();
    }

    private ImageView icon(String path) {
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(path)));
        iv.setFitWidth(sizeFlag2);
        iv.setFitHeight(sizeFlag);
        return iv;
    }


    private void applyTexts() {
        favoriteCheck.setText(tm.tr("checkbox.favorites"));
        recipeNameField.setPromptText(tm.tr("prompt.recipeName"));
        totalServingsField.setPromptText(tm.tr("prompt.servings"));
        totalServingsLabel.setText(tm.tr("label.totalServings", getCurrentServings()));

        // Update the labels inside the custom items
        ((Label) englishItem.getContent()).setText(tm.tr("menu.language.en"));
        ((Label) dutchItem.getContent()).setText(tm.tr("menu.language.nl"));
        ((Label) slovakItem.getContent()).setText(tm.tr("menu.language.sk"));
        ((Label) greekItem.getContent()).setText(tm.tr("menu.language.gr"));
        ((Label) portugueseItem.getContent()).setText(tm.tr("menu.language.pt"));

        // Update the main dropdown button text based on the PREVIEW language
        englishItem.setText(tm.tr("menu.language.en"));
        dutchItem.setText(tm.tr("menu.language.nl"));
        slovakItem.setText(tm.tr("menu.language.sk"));
        greekItem.setText(tm.tr("menu.language.gr"));
        portugueseItem.setText(tm.tr("menu.language.pt"));
    }

    private void updateTotalServingsLabelText() {
        totalServingsLabel.setText(tm.tr("label.totalServings", getCurrentServings()));
    }
}
