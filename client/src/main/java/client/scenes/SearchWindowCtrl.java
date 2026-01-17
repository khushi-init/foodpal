package client.scenes;

import client.data.TranslationManager;
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
    @FXML private Label titleLabel;
    @FXML
    private Label helpButton;
    
    private RootPropositionUI rootPropositionUI;
    private PrimaryCtrl primary;

    private TranslationManager tm;

    
    /**
     * Constructor...
     * @param p primaryCtrl instance
     * @param errorCtrl errorCtrl instance
     *                  @param tm the translation manager used to localize UI text
     */
    @Inject
    public SearchWindowCtrl(PrimaryCtrl p, ErrorCtrl errorCtrl, TranslationManager tm) {
        rootPropositionUI = new RootPropositionUI(this, errorCtrl, tm);
        primary = p;
        this.tm=tm;
    }

    /**
     * Called by JavaFX, initializes the scene
     */
    public void initialize(){
        applyTexts();

        tm.bundleProperty().addListener((obs, oldBundle, newBundle) -> {
            applyTexts();
        });
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
        alert.setTitle(tm.tr("searchHelp"));
        alert.setContentText(tm.tr("search.help.text")
        );
        alert.show();
    }

    private void applyTexts() {
        titleLabel.setText(tm.tr("search.title"));
        searchButton.setText(tm.tr("button.search"));
    }

}
