package server.api;

import commons.Recipe;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.RecipeRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeRepository recipeRepository;

    public RecipeController(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    // GET ENDPOINTS
    @GetMapping
    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> findRecipeById(@PathVariable Long id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);

        // if the recipe is found return 200 OK or else return 404 not found

        return recipe.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST ENDPOINTS
    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        // checking if the recipe has a valid name
        if(recipe.getName() == null || recipe.getName().trim().isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        // saving the new recipe
        Recipe savedRecipe = recipeRepository.save(recipe);

        // returning the HTTPS code for created
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipe);
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
}
