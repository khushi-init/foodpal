package client.popups;

import commons.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;


public class IngredientPopUpCtrl {
    @FXML
    private TextField nameField;

    @FXML
    private TextField quantityField;

    @FXML
    private ComboBox<String> unitTypePicker;
    @FXML
    private ComboBox<FormalUnit> formalUnitPicker;
    @FXML
    private TextField informalUnitField;
    @FXML private ComboBox<Ingredient> ingredientComboBox;

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
        formalUnitPicker.getItems().setAll(FormalUnit.values());

        formalUnitPicker.visibleProperty().bind(unitTypePicker.valueProperty().isEqualTo("Formal"));
        informalUnitField.visibleProperty().bind(unitTypePicker.valueProperty().isEqualTo("Informal"));
    }

    // Change the first parameter from String to Ingredient
    public void setInitialValues(Ingredient ingredient, Double quantity, RecipeIngredientUnit unitWrapper) {
        // Set the selection in your new ComboBox
        ingredientComboBox.setValue(ingredient);

        quantityField.setText(String.valueOf(quantity));

        if (unitWrapper != null && unitWrapper.toUnit() != null) {
            Unit unit = unitWrapper.toUnit();

            if (unit instanceof FormalUnit formal) {
                unitTypePicker.setValue("Formal");
                formalUnitPicker.setValue(formal);
            } else if (unit instanceof InformalUnit informal) {
                unitTypePicker.setValue("Informal");
                informalUnitField.setText(informal.getDisplayName());
            }
        } else {
            unitTypePicker.setValue("Formal");
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
    public String getName(){
        return nameField.getText();
    }
    public String getQuantity(){
        return quantityField.getText();
    }

    public Unit getSelectedUnit() {
        if ("Formal".equals(unitTypePicker.getValue())) {
            return formalUnitPicker.getValue(); // Returns the Enum constant (e.g., GRAM)
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

}
