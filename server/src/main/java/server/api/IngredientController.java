package server.api;

import commons.Ingredient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.IngredientRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {
    private final IngredientRepository ingredientRepository;

    /**
     * Creates controller for ingredient actions.
     * @param ingredientRepository repository to access ingredient data
     */
    public IngredientController(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    // GET ENDPOINTS

    /**
     * Gets all ingredients.
     * @return list of all ingredients
     */
    @GetMapping
    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    /**
     * Gets an ingredient by its ID.
     * @param id id of the ingredient
     * @return 200 OK with ingredient or 404 not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> getIngredientById(@PathVariable Long id) {
        Optional<Ingredient> ingredient = ingredientRepository.findById(id);

        // if the ingredient is found return 200 OK or else return 404 not found
        return ingredient.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // POST ENDPOINT
    /**
     * Creates a new ingredient.
     * @param ingredient ingredient data
     * @return 201 created or 400 bad request (invalid data)
     */
    @PostMapping
    public ResponseEntity<Ingredient> createIngredient(@RequestBody Ingredient ingredient) {

        if(ingredient.getName() == null || ingredient.getName().trim().isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        // saving the new ingredient
        Ingredient saved =  ingredientRepository.save(ingredient);

        // returning the HTTP code for created
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT ENDPOINT
    /**
     * Updates an existing ingredient.
     * @param ingredient ingredient with updated data
     * @return 200 ok with update or 400 if bad request
     */
    @PutMapping
    public ResponseEntity<Ingredient> updateIngredient(@RequestBody Ingredient ingredient) {
        if(ingredient.getName() == null || ingredient.getName().trim().isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        Ingredient updated = ingredientRepository.save(ingredient);
        return ResponseEntity.ok(updated);
    }

    // DELETE ENDPOINT
    /**
     * Deletes an ingredient based on id.S
     * @param id ID of the ingredient to delete
     * @return 204 success deletion or 404 not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIngredient(@PathVariable Long id) {
        if(ingredientRepository.existsById(id)){
            ingredientRepository.deleteById(id);
            // returns 204 no content signal to explicitly state that no message body will be returned
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
