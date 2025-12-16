package commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShoppingList {

    private final List<String> ingredients;

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
        ingredients.add(ingredient);
    }

    /**
     * Remove an ingredient from the shopping list.
     * @param index - The index of the ingredient to remove.
     */
    public void removeIngredient(int index) {
        ingredients.remove(index);
    }

    /**
     * Change an ingredient.
     * @param index - The index of the ingredient to change.
     * @param changedString - The new content of the ingredient.
     */
    public void changeIngredient(int index, String changedString) {
        ingredients.set(index, changedString);
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
    public List<String> getIngredients() {
        return List.copyOf(ingredients);
    }

    /**
     * Update shopping list after editing one of the ingredients
     * @param index which ingredient in list it is
     * @param newValue new item in the shopping list
     */
    public void updateIngredients(int index, String newValue) {
        ingredients.set(index, newValue);
    }

    /**
     * Turn the shopping list into a Markdown format.
     * @return the string with the Markdown format.
     */
    public String toMarkdown() {
        StringBuilder output = new StringBuilder();

        // Header 1 with the name of the recipe
        output.append("# Shopping List")
                .append("\n\n");

        if (ingredients.isEmpty()) {
            output.append("This shopping list is empty.");
        } else {
            output.append("## Ingredients\n");

            for (String ingredient : ingredients) {
                output.append("* ")
                        .append(ingredient)
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