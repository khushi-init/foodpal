package client.utils;

import commons.FormalUnit;
import commons.InformalUnit;
import commons.RecipeIngredientUnit;
import commons.Unit;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class QuantityUnitSelectionCtrl {

    @FXML private TextField quantityField;

    // Matched to FXML fx:id="unitTypePicker"
    @FXML private ComboBox<String> unitTypePicker;

    // Matched to FXML fx:id="formalUnitPicker"
    @FXML private ComboBox<FormalUnit> formalUnitPicker;

    @FXML private TextField informalUnitField;

    private Stage stage;
    private boolean okClicked = false;
    private RecipeIngredientUnit resultUnit;
    private Double resultQuantity;

    @FXML
    public void initialize() {
        // 1. Setup the Unit Type Picker (Formal vs Informal selection)
        unitTypePicker.getItems().addAll("Formal", "Informal");
        unitTypePicker.getSelectionModel().selectFirst();

        // 2. Populate the Formal Units dropdown with Enum values
        formalUnitPicker.getItems().setAll(FormalUnit.values());
        formalUnitPicker.getSelectionModel().selectFirst();

        // 3. Listener to swap UI visibility based on unitTypePicker selection
        unitTypePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isFormal = "Formal".equals(newVal);

            // Toggle visibility
            formalUnitPicker.setVisible(isFormal);
            informalUnitField.setVisible(!isFormal);

            // Clean up selections when switching
            if (isFormal) {
                informalUnitField.clear();
            } else {
                formalUnitPicker.getSelectionModel().clearSelection();
            }
        });

        // Initial state: ensure informal field is hidden if Formal is selected
        informalUnitField.setVisible(false);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            resultQuantity = Double.parseDouble(quantityField.getText());

            Unit selectedUnit;
            // Use unitTypePicker value to decide which input to read
            if ("Formal".equals(unitTypePicker.getValue())) {
                selectedUnit = formalUnitPicker.getValue();
            } else {
                selectedUnit = new InformalUnit(informalUnitField.getText());
            }

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
        // Validate Quantity
        try {
            Double.parseDouble(quantityField.getText());
        } catch (NumberFormatException e) {
            return false;
        }

        // Validate Unit
        boolean isFormal = "Formal".equals(unitTypePicker.getValue());
        if (isFormal && formalUnitPicker.getValue() == null) {
            return false;
        }
        if (!isFormal && (informalUnitField.getText() == null || informalUnitField.getText().isBlank())) {
            return false;
        }

        return true;
    }

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