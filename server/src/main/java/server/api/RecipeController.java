package server.api;

import commons.Ingredient;
import commons.NutritionalValue;
import commons.Recipe;
import commons.RecipeIngredient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.IngredientRepository;
import server.database.RecipeRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private NutritionalValue defaultNutritionalValue = new NutritionalValue(0, 0, 0);


    public RecipeController(RecipeRepository recipeRepository, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

    // GET ENDPOINTS
    @GetMapping
    public List<Recipe> findAllRecipes() {
        return recipeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> findRecipeById(@PathVariable Long id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);

        // if the recipe is found return 200 OK or else return 404 not found
        return recipe.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadRecipe(@PathVariable Long id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);

        if (recipe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String text = recipe.get().toMarkdown();
        byte[] fileBytes = text.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=recipe-" +
                                recipe.get().getName() + ".md")
                .contentType(MediaType.TEXT_MARKDOWN)
                .body(fileBytes);
    }

    // POST ENDPOINT
    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe incoming) {
        // checking if the recipe has a valid name
        if(incoming.getName() == null || incoming.getName().trim().isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        Recipe recipe = new Recipe();
        recipe.setName(incoming.getName());
        recipe.setPreparationSteps(incoming.getPreparationSteps());

        List<RecipeIngredient> ingredients = new java.util.ArrayList<>();

        if (incoming.getIngredients() != null) {
            for (RecipeIngredient inRi : incoming.getIngredients()) {
                String name = inRi.getIngredient() != null
                        ? inRi.getIngredient().getName()
                        : null;
                Double quantity = inRi.getQuantity();

                if (name == null || name.trim().isEmpty()) {
                    continue;
                }
                Ingredient ing = ingredientRepository //if this ingredient exist --> use that one
                        .findByName(name)
                        .orElseGet(() -> new Ingredient(name, defaultNutritionalValue)); //if not-->create a new one

                RecipeIngredient newRi = new RecipeIngredient(recipe, ing, quantity);
                ingredients.add(newRi);
            }
            recipe.setIngredients(ingredients);
        }

        // saving the new recipe
        Recipe savedRecipe = recipeRepository.save(recipe);

        // returning the HTTPS code for created
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipe);
    }

    // PUT ENDPOINT
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> changeRecipe(@PathVariable Long id, @RequestBody Recipe incoming) {
        Recipe existing = recipeRepository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        // checking if the recipe has a valid name
        if(incoming.getName() == null || incoming.getName().trim().isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        // the ID is the same of the object being stored
        existing.setName(incoming.getName());
        existing.setPreparationSteps(incoming.getPreparationSteps());

        if (incoming.getIngredients() != null) {
            for (RecipeIngredient ingredient : incoming.getIngredients()) {
                String name = ingredient.getIngredient() != null //if ingredient is not null --> use it name
                        ? ingredient.getIngredient().getName()
                        : ""; //else--> use empty string
                Double quantity = ingredient.getQuantity();

                if (name == null || name.trim().isEmpty()) {
                    continue;
                }

                //finding ingredients with the same name or creating a new one
                Ingredient ing = ingredientRepository
                        .findByName(name)
                        .orElseGet(() -> new Ingredient(name, defaultNutritionalValue));

                //checking if ingredient is already in the recipe
                boolean alreadyExists = existing.getIngredients().stream()
                        .anyMatch(ri -> ri.getIngredient().getName().equals(name));

                if (!alreadyExists) {
                    //adding only new ingredients
                    RecipeIngredient newRi = new RecipeIngredient(existing, ing, quantity);
                    existing.getIngredients().add(newRi);
                }
            }
        }
        Recipe changedRecipe = recipeRepository.save(existing);

        return ResponseEntity.ok(changedRecipe);
    }


    // DELETE ENDPOINT
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipeById(@PathVariable Long id) {
        if(recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            // returns 204 no content signal to explicitly state that no message body will be returned
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Handles database errors related to unique constraints (like duplicate recipe names)
     * and returns a 409 Conflict status.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // This is the key: maps the exception to HTTP 409
    public void handleDataIntegrityViolation(DataIntegrityViolationException e) {
        // This message will appear in your server console log, helping you debug.
        System.err.println("Attempted to violate data integrity (e.g., duplicate name): " + e.getMessage());
    }

    /**
     * Handling removing ingredient form recipy
     * @param recipeId which recipe will have removed ingredient
     * @param ingredientId which ingredient will be removed
     * @return
     */
    @DeleteMapping("/{recipeId}/ingredients/{ingredientId}")
    public ResponseEntity<Void> deleteIngredientFromRecipe(
            @PathVariable Long recipeId,
            @PathVariable Long ingredientId) {

        Recipe recipe = recipeRepository.findById(recipeId).orElse(null);
        if (recipe == null) {
            return ResponseEntity.notFound().build();
        }
        //remove matching ingredient
        boolean removed = recipe.getIngredients().removeIf(ri ->
                ri.getIngredient() != null &&
                        ri.getIngredient().getId() != null &&
                        ingredientId.equals(ri.getIngredient().getId())
        );
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        recipeRepository.save(recipe);//saving changes on database
        return ResponseEntity.noContent().build();
    }
}
