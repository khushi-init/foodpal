package client.scenes;

import client.data.TranslationManager;
import com.google.inject.Inject;
import commons.FormalUnit;
import commons.InformalUnit;
import commons.RecipeIngredientUnit;
import commons.Unit;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class QuantityUnitSelectionCtrl {
    private TranslationManager tm;

    @FXML private TextField quantityField;
    @FXML private Label headerLabel;
    @FXML private Label quantity;
    @FXML private Label unitType;
    @FXML private Button cancel;
    @FXML private Label unit;

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
        applyTexts();
        tm.bundleProperty().addListener((obs, oldBundle, newBundle) -> {
            applyTexts();
        });
        // 1. Setup the Unit Type Picker (Formal vs Informal selection)
        unitTypePicker.getItems().addAll(tm.tr("formal"), tm.tr("informal"));
        unitTypePicker.getSelectionModel().selectFirst();

        // 2. Populate the Formal Units dropdown with Enum values
        formalUnitPicker.getItems().setAll(FormalUnit.values());
        formalUnitPicker.getSelectionModel().selectFirst();

        // 3. Listener to swap UI visibility based on unitTypePicker selection
        unitTypePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isFormal = tm.tr("formal").equals(newVal);

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
            if (tm.tr("formal").equals(unitTypePicker.getValue())) {
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
        boolean isFormal = tm.tr("formal").equals(unitTypePicker.getValue());
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

    @Inject
    public QuantityUnitSelectionCtrl(TranslationManager tm) {
        this.tm = tm;
    }

    private void applyTexts() {
        headerLabel.setText(tm.tr("ingredinetquantity"));
        quantity.setText(tm.tr("quantity"));
        unitType.setText(tm.tr("unitType"));
        cancel.setText(tm.tr("button.cancelCreate"));
        unit.setText(tm.tr("unit"));

    }
}