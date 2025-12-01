package commons;

import java.util.ArrayList;
import java.util.List;

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
     * Turn the shopping list into Markdown format.
     * @return - A string containing the shopping list in Markdown format.
     */
    public String toMarkDown() {
        return "todo";
    }

    /**
     * Get the shopping list.
     * @return - A new list containing the ingredients in the shopping list.
     */
    public List<String> getIngredients() {
        return List.copyOf(ingredients);
    }
}