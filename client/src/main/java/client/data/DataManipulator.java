package client.data;

import client.utils.ErrorCtrl;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Ingredient;
import commons.Recipe;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class DataManipulator {

    private LocalStorage storage;
    private ServerUtils server;

    private ErrorCtrl errs;

    // File used for storing favorite ID's. Add additional stuff for config as you wish
    private final File properties = new File(System.getProperty("user.home"), "foodPal.properties");

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
        System.out.println("Your recipe \"" + recipe.getName()+ "\" has been created successfully.");
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
        System.out.println("Your ingredient \"" + ingredient.getName()+ "\" has been created successfully.");
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
            System.out.println("Recipe renamed successfully to: " + newName);
            return Optional.of(updated);
        }
        else{
            errs.showGenericError("A recipe with this name already exists!");
            return Optional.empty();
        }
    }

    /**
     * Updates the name of a recipe client side without sending the update to the server. I promise this is needed
     * @param id - The id of the recipe
     * @param newName - The new name
     */
    public void changeNameLocal(Long id, String newName) {
        for (int i = 0; i < storage.getRecipes().size(); i++) {
            if (Objects.equals(storage.getRecipes().get(i).getId(), id)) {
                storage.getRecipes().get(i).setName(newName);
                break;
            }
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
        System.out.println("Refreshed recipe list");
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
        // Delete recipe
        System.out.println("Recipe \"" + hit.getName() + "\" deleted successfully");
        storage.getFavoriteIDs().remove(hit.getId());
        saveFave();
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
        System.out.println("Ingredient \"" + ingredient.getName() + "\" deleted successfully");
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
                System.out.println("Ingredient \"" + replacement.getName() + "\" updated successfully");
                return true;
            }
        }
        errs.showGenericError("Ingredient updated on server but not found locally");
        return false;
    }

    /**
     * Saves the current favorites to the local persistent properties file.
     */
    public void saveFave() {
        // Convert all favorite ID's to a single string that will be stored
        Properties prop = new Properties();
        // THIS TIME COMPLEXITY SUCKS ASS LMAO O(n^2) (But im lazy, will probably refactor later)
        for (Long id : storage.getFavoriteIDs()) {
            String name = "Unknown";
            for (Recipe r : storage.getRecipes()) {
                if (Objects.equals(r.getId(), id)) {
                    name = r.getName();
                }
            }
            prop.setProperty(id.toString(), name);
        }
        try {
            prop.store(new FileOutputStream(properties), "Favorites");
        } catch (IOException e) {
            errs.showGenericError("Unable to store favorites, try again later!");
        }

    }

    /**
     * Load the favorites from the properties file, and displays a notification if the favorite is GONE
     */
    public void loadFavs() {
        Properties prop = new Properties();
        try {
            FileInputStream fis = new FileInputStream(properties);
            prop.load(fis);
            storage.getFavoriteIDs().clear();
            for (String key : prop.stringPropertyNames()) {
                Long id = Long.parseLong(key);
                storage.getFavoriteIDs().add(id);
            }

            List<Long> toRemove = new ArrayList<>();
            for (Long id : storage.getFavoriteIDs()) {
                if (!getRecipeIDs().contains(id)) {
                    errs.showGenericError("RIP: Recipe " + prop.getProperty(id.toString()) + " not found!");
                    System.out.println(id + prop.getProperty(id.toString()));
                    prop.remove(id.toString());
                    toRemove.add(id);
                }
            }
            if(!toRemove.isEmpty()) {
                storage.getFavoriteIDs().removeAll(toRemove);
            }

            // Remove lost recipe from properties file
            try {
                prop.store(new FileOutputStream(properties), "Favorites Updated");
            } catch (IOException e) {
                errs.showGenericError("Can not update favorites, yikes...");
            }

        } catch (Exception e) {
            errs.showGenericError("Unable to load favorites, try again later!");
            e.printStackTrace();
        }
    }
    public List<Long> getRecipeIDs() {
        List<Long> ids = new ArrayList<>();
        for (Recipe r : storage.getRecipes()) {
            ids.add(r.getId());
        }
        return ids;
    }


}
