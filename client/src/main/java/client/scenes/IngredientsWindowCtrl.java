package client.scenes;

import client.IngredientListCell;
import client.Main;
import client.utils.ErrorCtrl;
import client.utils.ServerUtils;
import commons.Ingredient;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

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

    public void initialize(){
        ingredients = FXCollections.observableArrayList(server.getIngredients());
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

    public void openIngredient(Ingredient ingredient){

    }
}
