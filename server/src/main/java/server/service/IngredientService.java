package server.service;

import commons.*;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import server.database.IngredientRepository;
import server.database.RecipeIngredientRepository;
import java.util.List;
import java.util.Optional;

@Service
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeService recipeService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Ingredient Service Constructor method
     * @param ingredientRepository The ingredient repository to meddle with
     * @param recipeIngredientRepository The Recipe Ingredient repository to meddle with
     * @param recipeService The RecipeService to meddle with
     * @param eventPublisher The EventPublisher for websocket communication
     */
    public IngredientService(IngredientRepository ingredientRepository, RecipeIngredientRepository recipeIngredientRepository, RecipeService recipeService, ApplicationEventPublisher eventPublisher) {
        this.ingredientRepository = ingredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.recipeService = recipeService;
        this.eventPublisher = eventPublisher;
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
    @Transactional
    public Optional<Ingredient> createIngredient(Ingredient ingredient) {
        Optional<Ingredient> saved = Optional.of(ingredient)
                .filter(this::validateIngredientName)
                .map(ingredientRepository::save);
        if(saved.isPresent()){
            eventPublisher.publishEvent(new IngredientAddition(saved.get()));
        }
        return saved;
    }

    /**
     * Validates and updates an existing ingredient.
     * @param ingredient The ingredient data to update
     * @return The updated ingredient, or null if the ID does not exist
     * @throws IllegalArgumentException if the name is invalid
     */
    @Transactional
    public Optional<Ingredient> updateIngredient(Ingredient ingredient){
        System.out.println("Trying ingredient update " + ingredient.getId());
        System.out.println(ingredientRepository.findById(ingredient.getId()).isPresent());
        return ingredientRepository.findById(ingredient.getId()).map(existing -> {
            if(!validateIngredientName(ingredient)) return null;
            boolean nameChange = false;
            if(existing.getName() != null && !existing.getName().equals(ingredient.getName())){
                existing.setName(ingredient.getName());
                nameChange = true;
            }
            existing.setNutritionalValue(ingredient.getNutritionalValue());
            Ingredient saved = ingredientRepository.save(existing);
            if(nameChange){
                eventPublisher.publishEvent(new IngredientNameUpdate(ingredient.getId(), saved.getName()));
            }
            eventPublisher.publishEvent(new IngredientUpdate(ingredient.getId(), saved));
            recipeService.updateRecipesWithChangedIngredient(saved);
            return saved;
        });
    }

    /**
     * Deletes an ingredient and its associated recipe links.
     * @param id The ID of the ingredient to delete
     * @return true if deleted, false if the ingredient was not found
     */
    @Transactional
    public boolean deleteIngredient(Long id) {
        if (!ingredientRepository.existsById(id)) {
            return false;
        }
        List<Long> recipeIds = recipeService.getRecipesWithIngredientID(id);
        // Manually delete all RecipeIngredient link entities associated with this ingredient
        recipeIngredientRepository.deleteByIngredientId(id);

        // Now the constraint is satisfied, delete the parent ingredient
        ingredientRepository.deleteById(id);

        eventPublisher.publishEvent(new IngredientDeletion(id));
        recipeService.sendRecipeUpdateForIds(recipeIds);

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