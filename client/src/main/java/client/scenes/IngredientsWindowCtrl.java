package client.scenes;

import client.Main;
import client.utils.ErrorCtrl;
import client.utils.ServerUtils;
import commons.Ingredient;
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
    @FXML
    private Button backButton;
}
