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

    public IngredientController(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    // GET ENDPOINTS
    @GetMapping
    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> getIngredientById(@PathVariable Long id) {
        Optional<Ingredient> ingredient = ingredientRepository.findById(id);

        // if the ingredient is found return 200 OK or else return 404 not found
        return ingredient.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // POST ENDPOINT
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

    // DELETE ENDPOINT
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
