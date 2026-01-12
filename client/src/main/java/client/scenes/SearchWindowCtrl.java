package client.scenes;

import com.google.inject.Inject;

import client.searchUI.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SearchWindowCtrl {
    @FXML
    private Button searchButton;
    @FXML
    private VBox treeBox;
    
    @FXML
    private Label helpButton;
    
    private RootPropositionUI rootPropositionUI;
    private PrimaryCtrl primary;

    
    /**
     * Constructor...
     * @param p primaryCtrl instance
     */
    @Inject
    public SearchWindowCtrl(PrimaryCtrl p){
        rootPropositionUI = new RootPropositionUI(this);
        primary = p;
    }

    /**
     * Called by JavaFX, initializes the scene
     */
    public void initialize(){
        updateView();
        searchButton.setOnAction(event -> {
            this.primary.applySearchToRecipesWindow(this.rootPropositionUI.mapToProposition());
        });
    }

    /**
     * Reloads the visualization of the proposition tree
     */
    public void updateView(){
        treeBox.getChildren().clear();
        treeBox.getChildren().add(rootPropositionUI.getView());
    }

    /**
     * Shows a dialog box when the help button is clicked
     */
    @FXML
    public void helpButtonClicked(){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.setTitle("Search Help");
        alert.setContentText(
                "Atomic functions check if a recipe satisfies a certain condition,"
                + " like the max. amount of steps or an ingredient that must be present."
                + " The HAS function checks if the recipe contains the given string in"
                + " any of its ingredients, steps or its name."
                + "\n\nCombined functions let you combine conditions. For example,"
                + " the HAS function is equivalent to AND(HASING, HASSTEP, HASNAME)."
                + " Combined functions can be nested."
        );
        alert.show();
    }

}
