package commons;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * Add an ingredient to the shopping list with recipe information.
     * @param ingredient - The ingredient to add.
     * @param recipeName - The name of the recipe this ingredient comes from.
     */
    public void addIngredient(String ingredient, String recipeName) {
        ingredients.add(new ShoppingListIngredient(ingredient, recipeName));
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
            // Count occurrences of each ingredient name to identify duplicates
            Map<String, Integer> ingredientNameCounts = new HashMap<>();
            for (ShoppingListIngredient ingredient : ingredients) {
                String ingredientName = extractIngredientName(ingredient.getNameAmount());
                ingredientNameCounts.put(ingredientName, ingredientNameCounts.getOrDefault(ingredientName, 0) + 1);
            }

            output.append("## Ingredients\n");

            for (ShoppingListIngredient ingredient : ingredients) {
                output.append("* ")
                        .append(ingredient.isCheckedOff() ? "~~" : "")
                        .append(ingredient.getNameAmount());
                
                String ingredientName = extractIngredientName(ingredient.getNameAmount());
                // Only show recipe name if this ingredient name appears more than once
                if (ingredientNameCounts.get(ingredientName) > 1 && 
                        ingredient.getRecipeName() != null && !ingredient.getRecipeName().isEmpty()) {
                    output.append(" - ").append(ingredient.getRecipeName());
                }
                
                output.append(ingredient.isCheckedOff() ? "~~" : "")
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

    /**
     * Extract the ingredient name from a formatted string like "Sugar (100g)" or "Sugar (100g) - Recipe Name"
     * @param text The formatted ingredient text
     * @return The ingredient name, or null if it can't be parsed
     */
    private String extractIngredientName(String text) {
        if (text == null || text.trim().isEmpty()) return null;

        // Remove any recipe suffix first
        String withoutRecipe = text.split(" - ")[0].trim();

        // Extract name before quantity
        int quantityStart = withoutRecipe.lastIndexOf(" (");
        if (quantityStart > 0) {
            return withoutRecipe.substring(0, quantityStart).trim();
        }

        return withoutRecipe.trim();
    }
}