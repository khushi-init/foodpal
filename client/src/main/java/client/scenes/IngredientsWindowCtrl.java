package client.scenes;

import java.util.Objects;
import java.util.Optional;

import client.IngredientListCell;
import client.MyFXML;
import client.data.DataManipulator;
import client.data.LocalStorage;
import client.data.TranslationManager;
import client.utils.ServerUtils;
import commons.InformalUnit;
import commons.Ingredient;
import commons.NutritionalValue;
import commons.RecipeIngredient;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

public class IngredientsWindowCtrl {

    // DEPENDENCIES (Injection)

    // ServerUtils is needed to fetch and save ingredient data
    private final ServerUtils server;
    private final PrimaryCtrl primaryCtrl;
    private final ErrorCtrl errorCtrl;
    private final MyFXML fxml;

    // NutriScore points
    private final double nutriScoreA = 1;
    private final double nutriScoreB = 4;
    private final double nutriScoreC = 7;
    private final double nutriScoreD = 10;

    private double ingredientScale = 1.0;

    final int heightImage = 50;

    // FXML FIELDS (UI Elements)

    @FXML
    private ListView<Ingredient> sidebarIngredientNamesList;

    @FXML
    private VBox ingredientDetailsView;

    @FXML
    private Button add;
    @FXML
    private Button remove;
    @FXML
    private Button refresh;

    @FXML
    private Button backButton;

    @FXML
    private Label ingScaleLabel;

    @FXML
    private TextField ingScaleTextField;

    @FXML
    private Label nameLabel;
    @FXML
    private Label proteinLabel;
    @FXML
    private Label fatLabel;
    @FXML
    private Label carbohydratesLabel;
    @FXML
    private Label kcalLabel;
    @FXML
    private Label recipesUsedInLabel;

    @FXML
    private Label name;

    @FXML
    private Label nutriInfo;

    @FXML
    private Label per100;

    @FXML
    private Label protein;

    @FXML
    private Label fats;
    @FXML
    private Label carbs;
    @FXML
    private Label kcalInf;
    @FXML
    private Label usedIn;



    @FXML
    private HBox nutriScoreBox;

    private final LocalStorage storage;

    private final DataManipulator dataManipulator;

    private final TranslationManager tm;

    /**
     * Injectable constructor is REQUIRED for Guice to provide dependencies.
     * @param p PrimaryCtrl instance for scene switching.
     * @param c ErrorCtrl instance for displaying errors.
     * @param storage - The local storage injected.
     * @param dataManipulator - The injected data Manipulator
     * @param server - Injected serverUtils instance
     * @param fxml - Injected MyFXML instance
     *             @param tm the translation manager used to localize UI text
     */
    @Inject
    public IngredientsWindowCtrl(PrimaryCtrl p, ErrorCtrl c, LocalStorage storage, DataManipulator dataManipulator, ServerUtils server, MyFXML fxml, TranslationManager tm) {
        this.primaryCtrl = p;
        this.errorCtrl = c;
        this.storage = storage;
        this.dataManipulator = dataManipulator;
        this.server = server;
        this.fxml = fxml;
        this.tm = tm;
    }

    /**
     * Initializes the ingredient window UI.
     */
    @FXML
    public void initialize(){
        applyTexts();
        tm.bundleProperty().addListener((obs, oldBundle, newBundle) -> {
            applyTexts();
        });

        // logic to sort ingredients by name
        FXCollections.sort(storage.getIngredients(), (i1, i2) -> i1.getName().compareToIgnoreCase(i2.getName()));

        sidebarIngredientNamesList.setItems(storage.getIngredients());
        sidebarIngredientNamesList.setCellFactory(icl -> new IngredientListCell());
        sidebarIngredientNamesList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue,
                              newValue) -> {
                    if(newValue != null){
                        openIngredient(newValue);
                    }
                });

        sidebarIngredientNamesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showIngredientDetails(newValue);
            } else {
                clearDetails();
            }
        });
    }

    /**
     * sets correct values in labels when an ingredient is selected from sidebar
     * @param ingredient the ingredient selected
     */
    public void showIngredientDetails(Ingredient ingredient) {
        ingredientDetailsView.setVisible(true);
        ingredientDetailsView.setManaged(true);

        nameLabel.setText(ingredient.getName());

        nameLabel.setText(ingredient.getName());
        proteinLabel.setText(String.format("%.1f g", ingredient.getNutritionalValue().protein100g()));
        fatLabel.setText(String.format("%.1f g", ingredient.getNutritionalValue().fat100g()));
        carbohydratesLabel.setText(String.format("%.1f g", ingredient.getNutritionalValue().carbs100g()));

        // Inferred Kcal (calculated this based on the 4-4-9 rule)
        double kcal = ingredient.getNutritionalValue().kcal100g();
        kcalLabel.setText(String.format("%d", Math.round(kcal)));

        Optional<Integer> usage = server.getIngredientUsage(ingredient.getId());
        if(usage.isEmpty()) {
            recipesUsedInLabel.setText(tm.tr("server.error.na"));
        } else {
            recipesUsedInLabel.setText(String.format(("%d "+ tm.tr("recipe(s)")), usage.get()));
        }

        showNutriScore(ingredient);
    }

    /**
     * when no ingredient is selected the window is ensured to be blank using this method
     */
    public void clearDetails() {
        ingredientDetailsView.setVisible(false);
        ingredientDetailsView.setManaged(false);
        // Remove any ImageView when clearing
        nutriScoreBox.getChildren().clear();
    }

    @FXML
    public void refreshLocalIngredients() {
        dataManipulator.refreshIngredients();
    }

    /**
     * Handles the click on the edit/pencil icon next to the ingredient name.
     * Prompts user to edit ingredient by calling ingredientDataPrompt, updates the ingredient with the new one using
     * DataManipulator and if successful, re-selects the edited ingredient to update the DetailsView
     * @param event The action event triggered by the button click.
     */
    public void handleEditNameClick(MouseEvent event) {
        Ingredient selected = sidebarIngredientNamesList.getSelectionModel().getSelectedItem();
        Optional<Ingredient> edit = ingredientDataPrompt(tm.tr("edit.ingredient"), tm.tr("ingredient.to.edit"), selected.getName(),
                String.valueOf(selected.getNutritionalValue().fat100g()),
                String.valueOf(selected.getNutritionalValue().protein100g()),
                String.valueOf(selected.getNutritionalValue().carbs100g()));
        if (edit.isEmpty()) return;
        Ingredient replacement = edit.get();
        if (replacement.getName().trim().isEmpty()) replacement.setName(selected.getName());
        replacement.setId(selected.getId());

        boolean successful = dataManipulator.editIngredient(replacement);
        if (!successful) {
            errorCtrl.showGenericError(tm.tr("something.wrong"));
            return;
        }
        int index = 0;
        for (int i = 0; i < storage.getIngredients().size(); i++) {
            if (Objects.equals(storage.getIngredients().get(i).getId(), replacement.getId())) {
                index = i;
                break;
            }
        }
        sidebarIngredientNamesList.getSelectionModel().select(index);

    }

    /**
     * Opens the detail view for the selected ingredient.
     * @param ingredient the ingredient to display details for
     */
    public void openIngredient(Ingredient ingredient){

    }

    /**
     * Runs when the green "plus" button is pressed. Prompts user to create an ingredient and adds it to the server.
     * If added to the server successfully, it is also added to the client-side local list.
     * @return Optional containing the newly created Ingredient if successful
     */
    public Optional<Ingredient> handlePlusButtonPress() {
        Optional<Ingredient> parsed = ingredientDataPrompt(tm.tr("create.ingredient"), tm.tr("ingredient.create"), "", "", "", "");
        if (parsed.isEmpty()) return Optional.empty();
        dataManipulator.addIngredient(parsed.get());
        return parsed;
    }

    /**
     * Opens a popup window where the user is prompted to create / edit an ingredient
     * @param title - Window title
     * @param descText - Description text
     * @param defaultName - The name to display in the name field initially
     * @param defaultFat - The fat to display in the name field initially
     * @param defaultProtein - The protein to display in the name field initially
     * @param defaultCarbs - The carbs to display in the name field initially
     * @return - The parsed ingredient or an empty optional if parsing failed / was cancelled
     */
    public Optional<Ingredient> ingredientDataPrompt(String title, String descText, String defaultName, String defaultFat, String defaultProtein, String defaultCarbs) {
        Pair<CreateIngredientCtrl, Parent> addIngPair = fxml.load(CreateIngredientCtrl.class, "client", "modules", "CreateIngredient.fxml");

        Parent root = addIngPair.getValue();
        CreateIngredientCtrl createIngCtr = addIngPair.getKey();

        Stage popUpStage = new Stage();
        popUpStage.initModality(Modality.APPLICATION_MODAL);
        popUpStage.setTitle(title); // Will change to language thing later
        popUpStage.setScene(new Scene(root));

        createIngCtr.setStage(popUpStage);
        createIngCtr.setDescLabelText(descText);
        createIngCtr.setDefaults(defaultName, defaultFat, defaultProtein, defaultCarbs);

        popUpStage.showAndWait();

        if (createIngCtr.getParsedIngredient() == null) return Optional.empty();

        return Optional.of(createIngCtr.getParsedIngredient());
    }

    /**
     * Runs when edit ingredient button is pressed
     */
    public void handleEditButtonPress() {

    }

    /**
     * Event handler for the Back button.
     * Switches the application scene back to the Recipes Window.
     */
    @FXML
    public void onBackToRecipesClick() {
        primaryCtrl.showRecipesWindow();
    }

    /**
     * Event handler for delete button
     */
    @FXML
    public void deleteIngredientButtonHandler() {
        Ingredient selected = sidebarIngredientNamesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Optional<Integer> uses = server.getIngredientUsage(selected.getId());
        if (uses.isEmpty()) {
            boolean anyway = errorCtrl.displayWarning(
                    tm.tr("ingredient.delete.warning.unused"),
                    tm.tr("button.delete.anyway"),
                    tm.tr("status.wait")
            );
            if (!anyway) return;

        } else if (uses.get() > 0) {
            boolean anyway = errorCtrl.displayWarning(
                    tm.tr("ingredient.delete.warning.used", uses.get()),
                    tm.tr("button.delete.anyway"),
                    tm.tr("status.wait")
            );
            if (!anyway) return;
        }
        dataManipulator.deleteIngredient(selected);
    }

    /**
     * Displays the NutriScore label (image) for the given ingredient
     * Shows nothing if nutritional data is missing or has zero calories
     * @param ingredient Ingredient for which we want to show nutri score
     */
    public void showNutriScore(Ingredient ingredient) {
        // remove any existing ImageView to avoid duplicates
        nutriScoreBox.getChildren().clear();

        NutritionalValue nv = ingredient.getNutritionalValue();
        if (nv == null || nv.kcal100g() == 0) {
            return;
        }

        double points = nv.nutriScorePoints();
        String imagePath = nutriScoreImagePath(points);
        ImageView imageView = new ImageView(
                new Image(getClass().getResource(imagePath).toExternalForm())
        );

        //styling
        imageView.setFitHeight(heightImage);
        imageView.setPreserveRatio(true);

        nutriScoreBox.getChildren().add(imageView);

    }

    private String nutriScoreImagePath(double points) {
        if (points <= nutriScoreA)  return "/client/images/NutriScoreA.png";
        if (points <= nutriScoreB)  return "/client/images/NutriScoreB.png";
        if (points <= nutriScoreC)  return "/client/images/NutriScoreC.png";
        if (points <= nutriScoreD) return "/client/images/NutriScoreD.png";
        return "/client/images/NutriScoreE.png";
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
        if(isInformal || ingredientScale == 1.0) {
            quantity = ri.getQuantity();
        }
        else {
            quantity = ri.getQuantity() * ingredientScale;
        }
        return "• " + ri.getIngredient().getName() + " " + quantity + " " + unitName;
    }
    private void applyTexts() {
        //backButton.setText(tm.tr("button.back"));
        name.setText(tm.tr("ingredient.name"));
        nutriInfo.setText(tm.tr("ingredient.nutrition.info"));
        per100.setText(tm.tr("ingredient.per.100g"));
        protein.setText(tm.tr("ingredient.protein"));
        fats.setText(tm.tr("ingredient.fats"));
        carbs.setText(tm.tr("ingredient.carbohydrates"));
        kcalInf.setText(tm.tr("ingredient.kcal"));
        usedIn.setText(tm.tr("ingredient.used.in.recipes"));
        nameLabel.setText(tm.tr("ingredient.name.Label"));


    }


    // We need an update method to re-sort the ingredients list on updates
}
