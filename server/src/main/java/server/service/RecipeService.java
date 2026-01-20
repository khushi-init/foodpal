package server.service;

import commons.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.database.IngredientRepository;
import server.database.RecipeRepository;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final ApplicationEventPublisher eventPublisher;
    private NutritionalValue defaultNutritionalValue = new NutritionalValue(0, 0, 0);

    /**
     * The Recipe Service constructor method
     * @param recipeRepository The Recipe repository to meddle with
     * @param ingredientRepository the ingredient repository to meddle with
     * @param eventPublisher The event publisher for sending updates to sockets
     */
    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository, ApplicationEventPublisher eventPublisher) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.eventPublisher = eventPublisher;
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
    @Transactional
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

                RecipeIngredient newRi = new RecipeIngredient(recipe, ing, inRi.getQuantity(), inRi.getUnit());
                ingredients.add(newRi);
            }
            recipe.setIngredients(ingredients);
        }
        eventPublisher.publishEvent(new RecipeAddition(recipe));
        return recipeRepository.save(recipe);
    }

    /**
     * Updates an existing recipe with new data.
     * @param id The ID of the recipe to update.
     * @param incoming The new data to apply.
     * @return An Optional containing the updated recipe, or empty if not found.
     */
    @Transactional
    public Optional<Recipe> updateRecipe(Long id, Recipe incoming) {
        return recipeRepository.findById(id).map(existing -> {
            // Update basic fields
            boolean nameChange = false;
            if (existing.getName() != null && !existing.getName().equals(incoming.getName())){
                existing.setName(incoming.getName());
                nameChange = true;
            }
            existing.setPreparationSteps(incoming.getPreparationSteps());

            // Delete ingredients that are in existing but not in incoming
            removeDeletedIngredients(existing, incoming);

            for (RecipeIngredient ri : incoming.getIngredients()) {
                String name = getIngredientName(ri);
                Long ingId = getIngredientId(ri);

                // Logic: Reuse existing ingredient or create new
                Ingredient ing = ingredientRepository.findById(ingId)
                        .orElseGet(() -> new Ingredient(name, defaultNutritionalValue));

                // Logic: Prevent duplicate ingredients in the same recipe
                Optional<RecipeIngredient> existingMatchingIngredient = existing.getIngredients().stream()
                        .filter(existingRi -> name.equals(existingRi.getIngredient().getName())).findFirst();
                boolean alreadyExists = existingMatchingIngredient.isPresent();

                // Add new RecipeIngredient if it does not exist yet
                if (!alreadyExists) {
                    // Add the unit here
                    existing.getIngredients().add(new RecipeIngredient(existing, ing, ri.getQuantity(), ri.getUnit()));
                } else {
                    // Update both quantity AND unit if they changed
                    RecipeIngredient existingRi = existingMatchingIngredient.get();
                    existingRi.setQuantity(ri.getQuantity());
                    existingRi.setUnit(ri.getUnit()); // Add this line
                }
            }
            Recipe saved = recipeRepository.save(existing);
            // If the name change was successfully commited in the DB, transmit the change in the websocket.
            if (nameChange) {
                eventPublisher.publishEvent(new TitleUpdate(id, incoming.getName()));
            }

            existing.setTotalServings(incoming.getTotalServings());

            eventPublisher.publishEvent(new RecipeUpdate(id, incoming));
            return saved;
        });
    }

    /**
     * Removes ingredients from the existing recipe that are no longer present in the incoming recipe.
     * @param existing the recipe whose ingredients will be updated
     * @param incoming the recipe containing the wanted set of ingredients
     */
    private void removeDeletedIngredients(Recipe existing, Recipe incoming) {
        if (existing == null) {
            return;
        }

        if (incoming == null || incoming.getIngredients() == null) {
            // If incoming has no ingredients--> remove all existing ones
            existing.getIngredients().clear();
            return;
        }
        existing.getIngredients().removeIf(existingRi -> {
            String existingName = getIngredientName(existingRi);
            if (existingName == null) {
                return false;
            }

            return incoming.getIngredients().stream()
                    .noneMatch(incomingRi ->
                            existingName.equals(getIngredientName(incomingRi)));
        });
    }

    /**
     *  Safely extracts and normalizes the ingredient name from a RecipeIngredient
     * @param recipeIngredient the recipe ingredient from which to extract the name
     * @return the trimmed ingredient name, or null if unavailable
     */
    String getIngredientName(RecipeIngredient recipeIngredient) {
        if (recipeIngredient == null || recipeIngredient.getIngredient() == null) {
            return null;
        }
        String name = recipeIngredient.getIngredient().getName();
        return (name == null || name.trim().isEmpty()) ? null : name;
    }

    Long getIngredientId(RecipeIngredient recipeIngredient){
        if (recipeIngredient == null || recipeIngredient.getIngredient() == null) {
            return null;
        }
        return recipeIngredient.getIngredient().getId();
    }

    /**
     * Deletes a recipe from the database if it exists.
     * @param id The ID of the recipe to delete.
     * @return true if the recipe was found and deleted, false otherwise.
     */
    @Transactional
    public boolean deleteRecipe(Long id) {
        if (recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            eventPublisher.publishEvent(new RecipeDeletion(id));
            System.out.println("DELETED RECIPE " + id);
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
    @Transactional
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

        eventPublisher.publishEvent(new RecipeUpdate(recipeId, recipe));

        return removed;
    }

    /**
     * Sends an update over the websocket for all recipes that contain the specified ingredient
     * @param ingredient The changed ingredient
     */
    @Transactional
    public void updateRecipesWithChangedIngredient(Ingredient ingredient){
        for(Recipe recipe: recipeRepository.findAll()){
            ArrayList<RecipeIngredient> ings = new ArrayList<>(recipe.getIngredients());
            if(ings.stream().anyMatch(x -> ingredient.getId() == x.getIngredient().getId())){
                eventPublisher.publishEvent(new RecipeUpdate(recipe.getId(), recipe));
            }
        }
    }

    /**
     * Sends an update over the websocket for all recipes that contain the deleted ingredient
     * @param ingredientId The deleted ingredient
     * @return List of all recipe ID's containing this ingredient
     */
    @Transactional
    public List<Long> getRecipesWithIngredientID(Long ingredientId){
        // for(Recipe recipe: recipeRepository.findAll()){
        //     if(recipe.getIngredients().stream().anyMatch(x -> x.getIngredient().getId().equals(ingredientId))){
        //         eventPublisher.publishEvent(new RecipeUpdate(recipe.getId(), recipe));
        //     }
        // }
        return recipeRepository.findAll().stream().filter(x -> 
            x.getIngredients().stream().anyMatch(
                y -> y.getIngredient().getId().equals(ingredientId)
            )
        )
        .map(x -> x.getId()).toList();
    }

    @Transactional
    public void sendRecipeUpdateForIds(List<Long> ids){
        for(Long id: ids){
            Optional<Recipe> r = recipeRepository.findById(id);
            if(r.isEmpty()) continue;
            eventPublisher.publishEvent(new RecipeUpdate(id, r.get()));
        }
    }
}
