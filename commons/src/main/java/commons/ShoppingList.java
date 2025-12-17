package commons;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShoppingList {

    private final List<ShoppingListIngredient> ingredients;

    /**
     * Create a new shopping list.
     */
    public ShoppingList() {
        this.ingredients = new ArrayList<>();
    }

    /**
     * Add an ingredient to the shopping list.
     * @param ingredient - The ingredient to add.
     */
    public void addIngredient(String ingredient) {
        ingredients.add(new ShoppingListIngredient(ingredient));
    }

    /**
     * Remove an ingredient from the shopping list.
     * @param index - The index of the ingredient to remove.
     */
    public void removeIngredient(int index) {
        ingredients.remove(index);
    }

    /**
     * Reset the shopping list.
     */
    public void resetList() {
        ingredients.clear();
    }

    /**
     * Get the shopping list.
     * @return - A new list containing the ingredients in the shopping list.
     */
    public List<ShoppingListIngredient> getIngredients() {
        return List.copyOf(ingredients);
    }

    /**
     * Update shopping list after editing one of the ingredients
     * @param index which ingredient in list it is
     * @param newValue new item in the shopping list
     */
    public void updateIngredients(int index, String newValue) {
        ingredients.get(index).setNameAmount(newValue);
    }

    /**
     * Update if an ingredient is checked off the shopping list.
     * @param index - Which ingredient in list it is
     * @param checked - True if the ingredient is checked off, false otherwise
     */
    public void updateCheckedOff(int index, boolean checked) {
        ingredients.get(index).setCheckedOff(checked);
    }

    /**
     * Turn the shopping list into a Markdown format.
     * @return the string with the Markdown format.
     */
    public String toMarkdown() {
        StringBuilder output = new StringBuilder();
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm:ss");
        String currentDateTimeFormatted = currentDateTime.format(dateTimeFormat);

        // Header 1 with the name of the recipe
        output.append("# Shopping List")
                .append("\n\n");

        output.append("**")
                .append(currentDateTimeFormatted)
                .append("**\n\n");

        if (ingredients.isEmpty()) {
            output.append("This shopping list is empty.");
        } else {
            output.append("## Ingredients\n");

            for (ShoppingListIngredient ingredient : ingredients) {
                output.append("* ")
                        .append(ingredient.isCheckedOff() ? "~~" : "")
                        .append(ingredient.getNameAmount())
                        .append(ingredient.isCheckedOff() ? "~~" : "")
                        .append("\n");
            }
        }
        return output.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingList that = (ShoppingList) o;
        return Objects.equals(ingredients, that.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ingredients);
    }
}