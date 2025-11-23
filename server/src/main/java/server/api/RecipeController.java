package server.api;

import commons.Recipe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.IngredientRepository;
import server.database.RecipeRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;

    public RecipeController(IngredientRepository ingredientRepository, RecipeRepository recipeRepository) {
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
    }

    // GET ENDPOINTS

    @GetMapping
    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> findById(@PathVariable Long id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);

        // if the recipe is found return 200 OK or else return 404 not found

        return recipe.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST ENDPOINTS



    // DELETE ENDPOINT

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        if(recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            // returns 204 no content signal to explicitly state that no message body will be returned
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
