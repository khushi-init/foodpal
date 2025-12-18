package commons;

import java.util.Objects;

public class ShoppingListIngredient {

    private String nameAmount;
    private boolean checkedOff;

    /**
     * Create a new ShoppingListIngredient
     * @param nameAmount - A string with the name and amount
     */
    public ShoppingListIngredient(String nameAmount) {
        this.nameAmount = nameAmount;
        this.checkedOff = false;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingListIngredient that = (ShoppingListIngredient) o;
        return checkedOff == that.checkedOff && Objects.equals(nameAmount, that.nameAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameAmount, checkedOff);
    }
}
