package client.scenes;

import client.IngredientListCell;
import client.Main;
import client.data.DataManipulator;
import client.data.LocalStorage;
import client.utils.CreateIngredientCtrl;
import client.utils.ErrorCtrl;
import client.utils.ServerUtils;
import commons.Ingredient;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import javafx.event.ActionEvent;
import java.util.Optional;

public class IngredientsWindowCtrl {

    // DEPENDENCIES (Injection)

    // ServerUtils is needed to fetch and save ingredient data
    private final ServerUtils server = Main.INJECTOR.getInstance(ServerUtils.class);
    private PrimaryCtrl primaryCtrl;
    private ErrorCtrl errorCtrl;

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

    private LocalStorage storage;

    private DataManipulator dataManipulator;

    /**
     * Injectable constructor is REQUIRED for Guice to provide dependencies.
     * @param p PrimaryCtrl instance for scene switching.
     * @param c ErrorCtrl instance for displaying errors.
     * @param storage - The local storage injected.
     * @param dataManipulator - The injected data Manipulator
     */
    @Inject
    public IngredientsWindowCtrl(PrimaryCtrl p, ErrorCtrl c, LocalStorage storage, DataManipulator dataManipulator) {
        this.primaryCtrl = p;
        this.errorCtrl = c;
        this.storage = storage;
        this.dataManipulator = dataManipulator;
    }

    /**
     * Initializes the ingredient window UI.
     */
    public void initialize(){

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

        // Temporarily setting the usage label until server call is implemented
        recipesUsedInLabel.setText("N/A recipes (Load data)");
    }

    /**
     * when no ingredient is selected the window is ensured to be blank using this method
     */
    public void clearDetails() {
        ingredientDetailsView.setVisible(false);
        ingredientDetailsView.setManaged(false);
    }

    /**
     * Handles the click on the edit/pencil icon next to the ingredient name.
     * @param event The action event triggered by the button click.
     */
    public void handleEditNameClick(ActionEvent event) {
        // TODO: Implement the logic to open a dialog or switch to an editable field
        // For now, this placeholder method resolves the FXML loading error.
        System.out.println("Edit Name button clicked!");
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
     */
    public void handlePlusButtonPress() {
        Optional<Ingredient> parsed = ingredientDataPrompt("Create Ingredient", "Ingredient to create:");
        if (parsed.isEmpty()) return;
        dataManipulator.addIngredient(parsed.get());
    }

    /**
     * Opens a popup window where the user is prompted to create / edit an ingredient
     * @param title - Window title
     * @param descText - Description text
     * @return - The parsed ingredient or an empty optional if parsing failed / was cancelled
     */
    public Optional<Ingredient> ingredientDataPrompt(String title, String descText) {
        Pair<CreateIngredientCtrl, Parent> addIngPair = Main.FXML.load(CreateIngredientCtrl.class, "client", "modules", "CreateIngredient.fxml");

        Parent root = addIngPair.getValue();
        CreateIngredientCtrl createIngCtr = addIngPair.getKey();

        Stage popUpStage = new Stage();
        popUpStage.initModality(Modality.APPLICATION_MODAL);
        popUpStage.setTitle(title); // Will change to language thing later
        popUpStage.setScene(new Scene(root));

        createIngCtr.setStage(popUpStage);
        createIngCtr.setDescLabelText(descText);

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
        dataManipulator.deleteIngredient(selected);
    }

    // We need an update method to re-sort the ingredients list on updates
}
