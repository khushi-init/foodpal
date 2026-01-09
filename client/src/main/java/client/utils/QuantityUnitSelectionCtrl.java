package client.utils;

import commons.FormalUnit;
import commons.InformalUnit;
import commons.RecipeIngredientUnit;
import commons.Unit;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class QuantityUnitSelectionCtrl {

    @FXML private TextField quantityField;
    @FXML private RadioButton formalRadio;
    @FXML
    private ComboBox<FormalUnit> formalUnitComboBox;
    @FXML private TextField informalUnitField;

    private Stage stage;
    private boolean okClicked = false;
    private RecipeIngredientUnit resultUnit;
    private Double resultQuantity;

    @FXML
    public void initialize() {
        // Populate the dropdown with your FormalUnit Enum values
        formalUnitComboBox.getItems().setAll(FormalUnit.values());
        formalUnitComboBox.getSelectionModel().selectFirst();

        // Listener to swap UI visibility based on RadioButton selection
        formalRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            formalUnitComboBox.setVisible(isSelected);
            informalUnitField.setVisible(!isSelected);

            if (isSelected) {
                informalUnitField.clear();
            } else {
                formalUnitComboBox.getSelectionModel().clearSelection();
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            resultQuantity = Double.parseDouble(quantityField.getText());
            Unit selectedUnit;
            if (formalRadio.isSelected()) {
                selectedUnit = formalUnitComboBox.getValue();
            } else {
                selectedUnit = new InformalUnit(informalUnitField.getText());
            }
            // Convert our Unit object into the persistable wrapper you created
            resultUnit = RecipeIngredientUnit.fromUnit(selectedUnit);

            okClicked = true;
            stage.close();
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }

    private boolean isInputValid() {
        try {
            Double.parseDouble(quantityField.getText());
        } catch (NumberFormatException e) {
            // You could add an alert here: "Please enter a valid number for quantity"
            return false;
        }

        if (!formalRadio.isSelected() && informalUnitField.getText().isBlank()) {
            return false; // Informal unit needs a name
        }
        return true;
    }

    // Accessors for the Main Controller to call
    public boolean isOkClicked() {
        return okClicked;
    }
    public Double getQuantity() {
        return resultQuantity;
    }
    public RecipeIngredientUnit getUnit() {
        return resultUnit;
    }
}
