package server.api;

import commons.Recipe;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.RecipeRepository;

import java.nio.charset.StandardCharsets;
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

    // PUT ENDPOINT
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> changeRecipe(@PathVariable Long id, @RequestBody Recipe recipe) {
        // checking if the recipe exists using its ID
        if(!recipeRepository.existsById(id)){
            return ResponseEntity.notFound().build();
        }
        // checking if the recipe has a valid name
        if(recipe.getName() == null || recipe.getName().trim().isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        // the ID is the same of the object being stored
        recipe.setId(id);
        // save the updated recipe
        Recipe changedRecipe = recipeRepository.save(recipe);

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
}
