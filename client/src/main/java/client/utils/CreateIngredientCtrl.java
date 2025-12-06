package client.utils;

import client.Main;
import commons.Ingredient;
import commons.NutritionalValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class CreateIngredientCtrl {

    @FXML
    private Label descLabel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField fatField;

    @FXML
    private TextField proteinField;

    @FXML
    private TextField carbField;

    private Stage stage;

    private Ingredient parsed = null;

    private final NeatUtils neatUtils = Main.INJECTOR.getInstance(NeatUtils.class);

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * When the "cancel" button is pressed the window closes and the parsed object remains null.
     */
    @FXML
    public void handleCancel() {
        parsed = null; // I somehow think that there's some super edge case when cancel can be pressed and this won't be null, which destroys the logic
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * When the "ok" button is pressed, this method parses an ingredient object based on the
     * user input provided and sets it to the 'parsed' field. Then, the window is closed.
     */
    @FXML
    public void handleOk() {
        String name = nameField.getText();
        Double fat = neatUtils.parseDoubleOrZero(fatField.getText());
        Double protein = neatUtils.parseDoubleOrZero(proteinField.getText());
        Double carbs = neatUtils.parseDoubleOrZero(carbField.getText());

        NutritionalValue nuts = new NutritionalValue(fat, protein, carbs);
        parsed = new Ingredient(name, nuts);

        if (stage != null) {
            stage.close();
        }
    }

    public Ingredient getParsedIngredient() {
        return parsed;
    }

    public void setDescLabelText(String text) {
        descLabel.setText(text);
    }
}
