package server.service;

import commons.Ingredient;
import org.springframework.stereotype.Service;
import server.database.IngredientRepository;
import server.database.RecipeIngredientRepository;
import java.util.List;
import java.util.Optional;

@Service
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeService recipeService;

    /**
     * Ingredient Service Constructor method
     * @param ingredientRepository The ingredient repository to meddle with
     * @param recipeIngredientRepository The Recipe Ingredient repository to meddle with
     * @param recipeService The RecipeService to meddle with
     */
    public IngredientService(IngredientRepository ingredientRepository, RecipeIngredientRepository recipeIngredientRepository, RecipeService recipeService) {
        this.ingredientRepository = ingredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.recipeService = recipeService;
    }

    /**
     * Gets all ingredients.
     * @return list of all ingredients
     */
    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    /**
     * Gets specific ingredient by ID.
     * @param id The ID to search for
     * @return Optional containing the ingredient if found
     */
    public Optional<Ingredient> getIngredientByID(Long id) {
        return ingredientRepository.findById(id);
    }

    /**
     * Gets the usage count of an ingredient.
     * @param id The ingredient ID
     * @return The number of recipes using this ingredient
     */
    public int getRecipeUsageNumber(Long id) {
        return recipeIngredientRepository.getRecipeUsageNumber(id);
    }

    /**
     * Validates and saves a new ingredient.
     * @param ingredient The ingredient to create
     * @return The created ingredient
     * @throws IllegalArgumentException if the name is invalid
     */
    public Optional<Ingredient> createIngredient(Ingredient ingredient) {
        //validateIngredientName(ingredient);
        //return Optional.of(ingredientRepository.save(ingredient));
        return Optional.of(ingredient)
                .filter(this::validateIngredientName)
                .map(ingredientRepository::save);
    }

    /**
     * Validates and updates an existing ingredient.
     * @param ingredient The ingredient data to update
     * @return The updated ingredient, or null if the ID does not exist
     * @throws IllegalArgumentException if the name is invalid
     */
    public Optional<Ingredient> updateIngredient(Ingredient ingredient) {
        recipeService.updateRecipesWithChangedIngredient(ingredient);
        return Optional.of(ingredient)
                .filter(i -> ingredientRepository.existsById(i.getId()))
                .filter(this::validateIngredientName)
                .map(ingredientRepository::save);
    }

    /**
     * Deletes an ingredient and its associated recipe links.
     * @param id The ID of the ingredient to delete
     * @return true if deleted, false if the ingredient was not found
     */
    public boolean deleteIngredient(Long id) {
        recipeService.updateRecipesWithDeletedIngredient(id);
        if (!ingredientRepository.existsById(id)) {
            return false;
        }
        // Manually delete all RecipeIngredient link entities associated with this ingredient
        recipeIngredientRepository.deleteByIngredientId(id);

        // Now the constraint is satisfied, delete the parent ingredient
        ingredientRepository.deleteById(id);

        return true;
    }

    /**
     * Helper method to validate ingredient's name
     * @param ingredient The ingredient to check
     */
    private boolean validateIngredientName(Ingredient ingredient) {
        return ingredient.getName() != null && !ingredient.getName().trim().isEmpty();
    }
}