package client.popups;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class ShoppingListIngredientPopUpCtrl {
    @FXML
    private TextField nameField;

    @FXML
    private TextField quantityField;

    private Stage stage;

    private boolean okClicked = false;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleOk(){
        okClicked = true;

        if (stage != null){
            stage.close();
        }
    }

    @FXML
    private void handleCancel(){
        okClicked = false;

        if (stage != null) {
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

}
