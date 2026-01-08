package server.api;

import commons.Recipe;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.service.RecipeService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;

    /**
     * Creates a RecipeController with the given recipe service
     * @param recipeService the service used to manage recipes
     */
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    // GET ENDPOINTS

    /**
     * Retrieves all recipes from the system via the RecipeService.
     * @return a list of all existing recipes
     */
    @GetMapping
    public List<Recipe> findAllRecipes() {
        return recipeService.getAllRecipes();
    }

    /**
     * Gets a recipe by its ID.
     * @param id recipe ID
     * @return 200 OK with recipe or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> findRecipeById(@PathVariable Long id) {
        // We ask the service for the data
        Optional<Recipe> recipe = recipeService.getRecipeById(id);

        // We handle the "Web" part: mapping the result to a ResponseEntity
        return recipe.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Downloads a recipe as a markdown file.
     * @param id recipe Id
     * @return markdown file or 404 if recipe doesn't exist
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadRecipe(@PathVariable Long id) {
        Optional<Recipe> recipeOpt = recipeService.getRecipeById(id);

        if (recipeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileBytes = recipeOpt.get().toMarkdown().getBytes(StandardCharsets.UTF_8);
        String filename = "recipe-" + recipeOpt.get().getName() + ".md";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=recipe-" +
                                recipeOpt.get().getName() + ".md")
                .contentType(MediaType.TEXT_MARKDOWN)
                .body(fileBytes);
    }

    // POST ENDPOINT

    /**
     * Creates a new recipe.
     * @param incoming recipe data from client
     * @return 201 created or 404 bad request
     */
    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe incoming) {
        // checking if the recipe has a valid name
        if (incoming.getName() == null || incoming.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Recipe savedRecipe = recipeService.createRecipe(incoming);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipe);
    }

    // PUT ENDPOINT

    /**
     * Updates an existing recipe.
     * @param id ID of the recipe to update
     * @param incoming incoming new recipe data
     * @return 200 OK with or 400 bad request or 404 not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> changeRecipe(@PathVariable Long id, @RequestBody Recipe incoming) {
        // checking if the recipe has a valid name
        if(incoming.getName() == null || incoming.getName().trim().isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        Optional<Recipe> updated = recipeService.updateRecipe(id, incoming);

        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE ENDPOINT

    /**
     * Deletes recipe.
     * @param id recipe ID
     * @return 204 no content(success) or 404 not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipeById(@PathVariable Long id) {
        boolean deleted = recipeService.deleteRecipe(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Handles database errors related to unique constraints (like duplicate recipe names)
     * and returns a 409 Conflict status.
     * @param e The thrown data integrity violation exception
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // This is the key: maps the exception to HTTP 409
    public void handleDataIntegrityViolation(DataIntegrityViolationException e) {
        // This message will appear in your server console log, helping you debug.
        System.err.println("Attempted to violate data integrity (e.g., duplicate name): " + e.getMessage());
    }

    /**
     * Handling removing ingredient form recipy.
     * @param recipeId which recipe will have removed ingredient
     * @param ingredientId which ingredient will be removed
     * @return ResponseEntity<Void> (204-success or 404-on fail)
     */
    @DeleteMapping("/{recipeId}/ingredients/{ingredientId}")
    public ResponseEntity<Void> deleteIngredientFromRecipe(
            @PathVariable Long recipeId,
            @PathVariable Long ingredientId) {
        boolean success = recipeService.removeIngredientFromRecipe(recipeId, ingredientId);

        if (success) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
