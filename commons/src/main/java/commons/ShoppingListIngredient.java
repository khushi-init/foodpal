package commons;

public class ShoppingListIngredient {

    private String nameAmount;
    private boolean checkedOff;

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
}
