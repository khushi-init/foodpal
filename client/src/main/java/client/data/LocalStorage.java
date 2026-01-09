package client.data;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Ingredient;
import commons.Recipe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LocalStorage {

    private ObservableList<Recipe> recipes = FXCollections.observableArrayList();

    private ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();

    private ObservableList<Long> favoriteIDs = FXCollections.observableArrayList();

    private Recipe currentRecipe;

    /**
     *Creates a LocalStorage instance initialized with data from the server
     * @param server the server utility used to load recipes and ingredients
     */
    @Inject
    public LocalStorage(ServerUtils server) {
        if (server != null) {
            recipes.addAll(server.getRecipes());
            ingredients.addAll(server.getIngredients());
        }
    }

    public ObservableList<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(ObservableList<Recipe> recipes) {
        this.recipes = recipes;
    }

    public ObservableList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ObservableList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public ObservableList<Long> getFavoriteIDs() {
        return favoriteIDs;
    }

    public void setFavoriteIDs(ObservableList<Long> favoriteIDs) {
        this.favoriteIDs = favoriteIDs;
    }
}
