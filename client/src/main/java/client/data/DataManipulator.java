package client.data;

import client.utils.ErrorCtrl;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Ingredient;
import commons.Recipe;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DataManipulator {

    private LocalStorage storage;
    private ServerUtils server;

    private ErrorCtrl errs;

    /**
     * Constructor of DataManipulator
     * @param storage - LocalStorage is injected
     * @param server - The serveruitls instance for server interactions. Is also injected
     * @param errs - The injected error controller
     */
    @Inject
    public DataManipulator(LocalStorage storage, ServerUtils server, ErrorCtrl errs) {
        this.storage = storage;
        this.server = server;
        this.errs = errs;
    }

    /**
     * Adds recipe to the server and returns the response entity
     * @param recipe - The recipe to add
     * @return - The response entity / empty optional if it wasn't added successfully
     */
    public Optional<Recipe> addRecipe(Recipe recipe) {
        Recipe savedRecipe = server.addRecipe(recipe); // Assuming 'server' is initialized

        if (savedRecipe == null) {
            // add the server-returned object
            System.err.println("Recipe creation failed. Check server console for details.");
            return Optional.empty();
        }
        storage.getRecipes().add(savedRecipe);
        return Optional.of(savedRecipe);
    }

    /**
     * Adds provided ingredient to the server. If successful, it adds it to the local list as well.
     * @param ingredient - The ingredient to add
     * @return - The added ingredient response entity
     */
    public Optional<Ingredient> addIngredient(Ingredient ingredient) {
        Optional<Ingredient> response = server.addIngredient(ingredient);
        if (response.isEmpty()) return Optional.empty();
        storage.getIngredients().add(response.get());
        return response;
    }

    /**
     * Sets target recipe name to the new name
     * @param target - The recipe to update
     * @param newName - The new name to set
     * @return - Whether successful
     */
    public Optional<Recipe> editRecipeName(Recipe target, String newName) {
        // setting the new name
        target.setName(newName);
        // updating the recipe to store the new name
        Recipe updated = server.updateRecipe(target);
        if (updated != null) {
            updateRecipe(updated);
            System.out.println("Recipe saved successfully to: " + newName);
            return Optional.of(updated);
        }
        else{
            errs.showGenericError("A recipe with this name already exists!");
            return Optional.empty();
        }
    }

    /**
     * Replaces the recipe with the same id in the local list with the updates one
     * @param updated - The recipe to update
     */
    public void updateRecipe(Recipe updated) {
        for (int i = 0; i < storage.getRecipes().size(); i++) {
            if (Objects.equals(storage.getRecipes().get(i).getId(), updated.getId())) {
                storage.getRecipes().set(i, updated);
                break;
            }
        }
    }

    /**
     * Refresh the local recipe list
     */
    public void refreshRecipes() {
        List<Recipe> serverResponse = server.getRecipes();
        storage.getRecipes().setAll(serverResponse);
    }

    /**
     * Tries to delete hit from server. If successful deletes from the local list
     * @param hit - Recipe to delete
     */
    public void deleteRecipe(Recipe hit) {
        boolean successful = server.deleteRecipe(hit.getId());
        if (!successful) {
            System.err.println("RecipesWindowCtrl: Failed to delete recipe from the server, aborting request!");
            return;
        }
        storage.getRecipes().remove(hit);
    }

    /**
     * Tries to delete ingredient from server, ingredient is also deleted from any recipes that use it
     * @param ingredient ingredient to be deleted
     */
    public void deleteIngredient(Ingredient ingredient) {
        // 1. Call the ServerUtils method
        boolean successful = server.deleteIngredient(ingredient.getId());

        if (!successful) {
            // Show error using injected ErrorCtrl
            errs.showGenericError("Failed to delete ingredient from the server.");
            return;
        }

        // 2. If successful, remove from the local list (LocalStorage)
        // The UI (ListView) will update automatically.
        storage.getIngredients().remove(ingredient);
    }

    /**
     * Uses ServerUtils to update the ingredient on the server using replacement
     * If successful, updates the localstorage with the edited ingredient
     * @param replacement - The updated ingredient
     * @return - Whether successful
     */
    public boolean editIngredient(Ingredient replacement) {
        Optional<Ingredient> update = server.editIngredient(replacement);

        if (update.isEmpty()) {
            errs.showGenericError("Failed to update ingredient in the server");
            return false;
        }

        // Replace with update on the local ingredients list
        for (int i = 0; i < storage.getIngredients().size(); i++) {
            if (Objects.equals(storage.getIngredients().get(i).getId(), update.get().getId())) {
                storage.getIngredients().set(i, update.get());
                return true;
            }
        }
        return false;
    }
}
