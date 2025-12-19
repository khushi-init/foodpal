package commons;

import java.util.Objects;

public class ShoppingListIngredient {

    private String nameAmount;
    private boolean checkedOff;
    private String recipeName;

    /**
     * Create a new ShoppingListIngredient
     * @param nameAmount - A string with the name and amount
     */
    public ShoppingListIngredient(String nameAmount) {
        this.nameAmount = nameAmount;
        this.checkedOff = false;
        this.recipeName = null;
    }

    /**
     * Create a new ShoppingListIngredient with recipe name
     * @param nameAmount - A string with the name and amount
     * @param recipeName - The name of the recipe this ingredient comes from
     */
    public ShoppingListIngredient(String nameAmount, String recipeName) {
        this.nameAmount = nameAmount;
        this.checkedOff = false;
        this.recipeName = recipeName;
    }

    public String getNameAmount() {
        return nameAmount;
    }

    public void setNameAmount(String nameAmount) {
        this.nameAmount = nameAmount;
    }

    public boolean isCheckedOff() {
        return checkedOff;
    }

    public void setCheckedOff(boolean checkedOff) {
        this.checkedOff = checkedOff;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingListIngredient that = (ShoppingListIngredient) o;
        return checkedOff == that.checkedOff && Objects.equals(nameAmount, that.nameAmount) && Objects.equals(recipeName, that.recipeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameAmount, checkedOff, recipeName);
    }
}
