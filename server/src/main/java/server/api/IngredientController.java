package server.api;

import commons.Ingredient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.service.IngredientService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {
    private final IngredientService ingredientService;

    /**
     * Constructor method
     * @param ingredientService Ingredient service to handle repositories.
     */
    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    // GET ENDPOINTS

    /**
     * Gets all ingredients.
     * @return list of all ingredients
     */
    @GetMapping
    public List<Ingredient> findAllIngredients() {
        return ingredientService.getAllIngredients();
    }

    /**
     * Gets an ingredient by its ID.
     * @param id id of the ingredient
     * @return 200 OK with ingredient or 404 not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> getIngredientById(@PathVariable Long id) {
        Optional<Ingredient> ingredient = ingredientService.getIngredientByID(id);

        // if the ingredient is found return 200 OK or else return 404 not found
        return ingredient.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Returns the amount of recipes in which an ingredient is part of given an ingredient id
     * @param id - The ingredient id to search with
     * @return - The amount of recipes the ingredient was found in
     */
    @GetMapping("/recipecount/{id}")
    public ResponseEntity<Integer> getIngredientUsageNum(@PathVariable Long id) {
        int uses = ingredientService.getRecipeUsageNumber(id);

        return ResponseEntity.status(HttpStatus.OK).body(uses);
    }

    // POST ENDPOINT
    /**
     * Creates a new ingredient.
     * @param ingredient ingredient data
     * @return 201 created or 400 bad request (invalid data)
     */
    @PostMapping
    public ResponseEntity<Ingredient> createIngredient(@RequestBody Ingredient ingredient) {
        if (ingredient.getName() == null || ingredient.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Ingredient> saved = ingredientService.createIngredient(ingredient);
        return saved.map(i -> ResponseEntity.status(HttpStatus.CREATED).body(i))
                .orElse(ResponseEntity.badRequest().build());
    }

    // PUT ENDPOINT
    /**
     * Updates an existing ingredient.
     * @param ingredient ingredient with updated data
     * @return 200 ok with update, 400 if bad request and 404 if not found
     */
    @PutMapping
    public ResponseEntity<Ingredient> updateIngredient(@RequestBody Ingredient ingredient) {
        if (ingredient.getName() == null || ingredient.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Ingredient> updated = ingredientService.updateIngredient(ingredient);
        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE ENDPOINT
    /**
     * Deletes an ingredient based on id.
     * Manually removes all RecipeIngredient links first to avoid foreign key violation.
     * @param id ID of the ingredient to delete
     * @return 204 success deletion or 404 not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIngredient(@PathVariable Long id) {
        boolean deleted = ingredientService.deleteIngredient(id);
        if(deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
