package client.scenes;

import com.google.inject.Inject;

import client.searchUI.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class SearchWindowCtrl {
    @FXML
    private Button searchButton;
    @FXML
    private VBox treeBox;
    
    private RootPropositionUI rootPropositionUI;
    private PrimaryCtrl primary;

    
    /**
     * Constructor...
     * @param p .
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

}
