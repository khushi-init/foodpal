package client.popups;

import client.data.TranslationManager;
import com.google.inject.Inject;
import commons.FormalUnit;
import commons.InformalUnit;
import commons.RecipeIngredientUnit;
import commons.Unit;
import commons.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;


public class IngredientPopUpCtrl {

    private final TranslationManager tm;

    @FXML
    private TextField quantityField;

    @FXML
    private ComboBox<String> unitTypePicker;
    @FXML
    private ComboBox<FormalUnit> formalUnitPicker;
    @FXML
    private TextField informalUnitField;
    @FXML private ComboBox<Ingredient> ingredientComboBox;

    @FXML private Label titleLabel;
    @FXML private Label ingredient;
    @FXML private Label quantityLabel;
    @FXML private Label typeLabel;
    @FXML private Label unitLabel;


    private Stage stage;
    private boolean okClicked = false;

    public void setStage(Stage stage) {
        this.stage = stage;

        ingredientComboBox.setCellFactory(lv -> new ListCell<Ingredient>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        ingredientComboBox.setButtonCell(new ListCell<Ingredient>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        unitTypePicker.getItems().setAll("Formal", "Informal");
        tm.bundleProperty().addListener((obs, old, n) -> applyTexts());

        formalUnitPicker.visibleProperty().bind(unitTypePicker.valueProperty().isEqualTo(tm.tr("formal")));
        informalUnitField.visibleProperty().bind(unitTypePicker.valueProperty().isEqualTo(tm.tr("informal")));

        formalUnitPicker.getItems().setAll(FormalUnit.values());

        applyTexts();
    }

    // Change the first parameter from String to Ingredient
    public void setInitialValues(Ingredient ingredient, Double quantity, RecipeIngredientUnit unitWrapper) {
        // Set the selection in your new ComboBox
        ingredientComboBox.setValue(ingredient);

        quantityField.setText(String.valueOf(quantity));

        if(unitWrapper != null && unitWrapper.toUnit() != null) {
            Unit unit = unitWrapper.toUnit();

            if(unit instanceof FormalUnit formal) {
                unitTypePicker.setValue(tm.tr("formal"));
                formalUnitPicker.setValue(formal);
            }else if (unit instanceof InformalUnit informal) {
                unitTypePicker.setValue(tm.tr("informal"));
                informalUnitField.setText(informal.getDisplayName());
            }else{
                unitTypePicker.setValue(tm.tr("formal"));
            }
        }
    }

    @FXML
    private void handleOk(){
        okClicked = true;
        if(stage!=null){
            stage.close();
        }
    }

    @FXML
    private void handleCancel(){
        okClicked = false;
        if(stage!=null){
            stage.close();
        }
    }

    public boolean isOkClicked(){
        return okClicked;
    }
    public String getQuantity(){
        return quantityField.getText();
    }

    public Unit getSelectedUnit() {

        if (tm.tr("formal").equals(unitTypePicker.getValue())) {
            return formalUnitPicker.getValue();
        }
        return new InformalUnit(informalUnitField.getText());
    }

    public void setIngredients(List<Ingredient> ingredients, Ingredient current) {
        ingredientComboBox.setItems(FXCollections.observableArrayList(ingredients));
        ingredientComboBox.setValue(current);
    }

    public Ingredient getSelectedIngredient() {
        return ingredientComboBox.getValue();
    }


    public void applyTexts() {
        titleLabel.setText(tm.tr("title.editIngredient"));
        ingredient.setText(tm.tr("select.ingredient"));
        quantityLabel.setText(tm.tr("label.quantity"));
        typeLabel.setText(tm.tr("label.type"));
        unitLabel.setText(tm.tr("label.unit"));

        informalUnitField.setPromptText(tm.tr("prompt.pinch"));
        if (unitTypePicker == null) return;

        String currentType = unitTypePicker.getValue();
        String formalStr = tm.tr("formal");
        String informalStr = tm.tr("informal");

        unitTypePicker.getItems().setAll(formalStr, informalStr);

        boolean isFormal = currentType == null || currentType.equals("Formal") || currentType.equals(formalStr);
        unitTypePicker.setValue(isFormal ? formalStr : informalStr);
    }


    @Inject
    public IngredientPopUpCtrl(TranslationManager tm) {
        this.tm = tm;
    }

}
