package server.service;

import commons.Ingredient;
import commons.NutritionalValue;
import commons.Recipe;
import commons.RecipeIngredient;
import org.springframework.stereotype.Service;
import server.database.IngredientRepository;
import server.database.RecipeRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private NutritionalValue defaultNutritionalValue = new NutritionalValue(0, 0, 0);

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

    /**
     * retrives all recipes in the database
     * @return all recipes
     */
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    /**
     * Finds a recipe by its unique identifier.
     * @param id The ID of the recipe to find.
     * @return An Optional containing the recipe if found, or empty otherwise.
     */
    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    /**
     * Generates a markdown representation of a recipe.
     * @param id The ID of the recipe to convert.
     * @return An Optional containing the byte array of the markdown file,
     * or empty if the recipe isn't found.
     */
    public Optional<byte[]> getRecipeMarkdownBytes(Long id) {
        return recipeRepository.findById(id)
                .map(recipe -> recipe.toMarkdown().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Processes and saves a new recipe, including ingredient lookup and creation.
     * @param incoming The recipe data provided by the user.
     * @return The saved Recipe object with populated associations.
     */
    public Recipe createRecipe(Recipe incoming) {
        Recipe recipe = new Recipe();
        recipe.setName(incoming.getName());
        recipe.setPreparationSteps(incoming.getPreparationSteps());

        List<RecipeIngredient> ingredients = new java.util.ArrayList<>();

        if (incoming.getIngredients() != null) {
            for (RecipeIngredient inRi : incoming.getIngredients()) {
                String name = (inRi.getIngredient() != null) ? inRi.getIngredient().getName() : null;

                if (name == null || name.trim().isEmpty()) {
                    continue;
                }

                // LOGIC: Find existing ingredient or create a new one with defaults
                Ingredient ing = ingredientRepository.findByName(name)
                        .orElseGet(() -> new Ingredient(name, defaultNutritionalValue));

                RecipeIngredient newRi = new RecipeIngredient(recipe, ing, inRi.getQuantity());
                ingredients.add(newRi);
            }
            recipe.setIngredients(ingredients);
        }

        return recipeRepository.save(recipe);
    }

    /**
     * Updates an existing recipe with new data.
     * @param id The ID of the recipe to update.
     * @param incoming The new data to apply.
     * @return An Optional containing the updated recipe, or empty if not found.
     */
    public Optional<Recipe> updateRecipe(Long id, Recipe incoming) {
        return recipeRepository.findById(id).map(existing -> {
            // Update basic fields
            existing.setName(incoming.getName());
            existing.setTotalServings(incoming.getTotalServings());
            existing.setPreparationSteps(incoming.getPreparationSteps());

            if (incoming.getIngredients() != null) {
                for (RecipeIngredient ri : incoming.getIngredients()) {
                    String name = (ri.getIngredient() != null) ? ri.getIngredient().getName() : "";

                    if (name == null || name.trim().isEmpty()) continue;

                    // Logic: Reuse existing ingredient or create new
                    Ingredient ing = ingredientRepository.findByName(name)
                            .orElseGet(() -> new Ingredient(name, defaultNutritionalValue));

                    // Logic: Prevent duplicate ingredients in the same recipe
                    boolean alreadyExists = existing.getIngredients().stream()
                            .anyMatch(existingRi -> existingRi.getIngredient().getName().equals(name));

                    if (!alreadyExists) {
                        existing.getIngredients().add(new RecipeIngredient(existing, ing, ri.getQuantity()));
                    }
                }
            }
            return recipeRepository.save(existing);
        });
    }

    /**
     * Deletes a recipe from the database if it exists.
     * @param id The ID of the recipe to delete.
     * @return true if the recipe was found and deleted, false otherwise.
     */
    public boolean deleteRecipe(Long id) {
        if (recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Removes a specific ingredient from a recipe.
     * @param recipeId The ID of the recipe.
     * @param ingredientId The ID of the ingredient to remove.
     * @return true if the ingredient was found and removed, false otherwise.
     */
    public boolean removeIngredientFromRecipe(Long recipeId, Long ingredientId) {
        Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);

        if (recipeOpt.isEmpty()) {
            return false;
        }

        Recipe recipe = recipeOpt.get();

        // Logic: Remove the ingredient if the IDs match
        boolean removed = recipe.getIngredients().removeIf(ri ->
                ri.getIngredient() != null &&
                        ingredientId.equals(ri.getIngredient().getId())
        );

        if (removed) {
            recipeRepository.save(recipe);
        }

        return removed;
    }


}
