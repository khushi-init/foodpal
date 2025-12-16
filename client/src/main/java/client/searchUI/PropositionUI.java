package client.searchUI;
import java.util.ArrayList;

import client.utils.searchUtils.Proposition;
// import client.utils.searchUtils.Proposition;
import client.utils.searchUtils.SearchFunctions;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
// import javafx.scene.layout.HBox;
// import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;

public abstract class PropositionUI {
    public ComboBox<SearchFunctions> dropdown;
    public ParentPropositionUI parent;
    public final double indentation = 25.0;
    public final double topMargin = 5.0;
    public final int cancelButtonFitSize = 17;
    public Button deleteButton;
    public final Insets boxInsets = new Insets(5,5,5,5);
    /**
     * Constructor...
     * @param functions the functions that are available in the dropdown
     * @param parent the parent...
     */
    public PropositionUI(ParentPropositionUI parent, ArrayList<SearchFunctions> functions){
        dropdown = new ComboBox<SearchFunctions>();
        dropdown.setCellFactory(lc -> new FunctionListCell());
        dropdown.setItems(FXCollections.observableArrayList(functions));
        
        Image image = new Image("/client/images/delete.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(cancelButtonFitSize);
        imageView.setFitHeight(cancelButtonFitSize);
        imageView.setPreserveRatio(true);
        deleteButton = new Button();
        deleteButton.setGraphic(imageView);
        deleteButton.setOnAction((event) -> delete());
        this.parent = parent;
    }

    public Pane getView(){
        return null;
    }

    /**
     * Deletes this instance by deleting it from its parent
     */
    public void delete(){
        parent.deleteChild(this);
    }

    /**
     * Passes the update signal to the top of the tree and rerenders it.
     */
    public void updateView(){
        parent.updateView();
    }

    /**
     * Converts this propositionUI to an actual executable proposition.
     * @return corresponding Proposition
     */
    public Proposition mapToProposition() throws IllegalArgumentException{
        return null;
    }

    /**
     * Returns the currently selected SearchFunctions entry
     * @return selected entry
     */
    public SearchFunctions getSelectedFunction(){
        return this.dropdown.getSelectionModel().getSelectedItem();
    }

    /**
     * Selects a SearchFunction entry
     * @param function .
     */
    protected void setSelectedFunction(SearchFunctions function){
        this.dropdown.getSelectionModel().select(function);
    }

}
