package client.scenes;

import client.IngredientListCell;
import client.Main;
import client.utils.CreateIngredientCtrl;
import client.utils.ErrorCtrl;
import client.utils.ServerUtils;
import commons.Ingredient;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

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

    private ObservableList<Ingredient> ingredients;

    /**
     * Injectable constructor is REQUIRED for Guice to provide dependencies.
     * @param p PrimaryCtrl instance for scene switching.
     * @param c ErrorCtrl instance for displaying errors.
     */
    @Inject
    public IngredientsWindowCtrl(PrimaryCtrl p, ErrorCtrl c) {
        this.primaryCtrl = p;
        this.errorCtrl = c;
    }

    /**
     * Initializes the ingredient window UI.
     */
    public void initialize(){
        ingredients = FXCollections.observableArrayList(server.getIngredients());

        // logic to sort ingredients by name
        FXCollections.sort(ingredients, (i1, i2) -> i1.getName().compareToIgnoreCase(i2.getName()));

        sidebarIngredientNamesList.setItems(ingredients);
        sidebarIngredientNamesList.setCellFactory(icl -> new IngredientListCell());
        sidebarIngredientNamesList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue,
                              newValue) -> {
                    if(newValue != null){
                        openIngredient(newValue);
                    }
                });
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
        Optional<Ingredient> parsed = addIngredientPrompt();
        if (parsed.isEmpty()) return;
        Optional<Ingredient> response = server.addIngredient(parsed.get());
        if (response.isEmpty()) return;
        ingredients.add(response.get());

    }

    /**
     * Opens a popup window where the user is prompted to create an ingredient
     * @return - The parsed ingredient or an empty optional if parsing failed / was cancelled
     */
    public Optional<Ingredient> addIngredientPrompt() {
        Pair<CreateIngredientCtrl, Parent> addIngPair = Main.FXML.load(CreateIngredientCtrl.class, "client", "modules", "CreateIngredient.fxml");

        Parent root = addIngPair.getValue();
        CreateIngredientCtrl createIngCtr = addIngPair.getKey();

        Stage popUpStage = new Stage();
        popUpStage.initModality(Modality.APPLICATION_MODAL);
        popUpStage.setTitle("Create Ingredient"); // Will change to language thing later
        popUpStage.setScene(new Scene(root));

        createIngCtr.setStage(popUpStage);

        popUpStage.showAndWait();

        if (createIngCtr.getParsedIngredient() == null) return Optional.empty();

        return Optional.of(createIngCtr.getParsedIngredient());
    }
     * Event handler for the Back button.
     * Switches the application scene back to the Recipes Window.
     */
    @FXML
    public void onBackToRecipesClick() {
        primaryCtrl.showRecipesWindow();
    }
}
