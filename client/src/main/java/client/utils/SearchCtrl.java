package client.utils;

import java.util.ArrayList;
import java.util.List;

import commons.Recipe;
import commons.RecipeIngredient;
import commons.Ingredient;

public class SearchCtrl {
    /**
     * Performs a query on the provided List of recipes
     * @param query A String containing words recipes must contain, separated by spaces (case insensitive)
     * @param recipes A list of all recipes in which to search
     * @return A List of all recipes that satisfy the search conditions
     */
    public List<Recipe> search(String query, List<Recipe> recipes){
        query = query.toLowerCase();
        List<String> queries = List.of(query.split(" "));
        ArrayList<Recipe> filteredRecipes = new ArrayList<>();

        for(Recipe recipe : recipes){
            //check if this recipe satisfies all queries
            if(queries.stream().allMatch(x -> checkIfRecipeSatisfiesQuery(x, recipe))){
                filteredRecipes.add(recipe);
            }
        }
        return filteredRecipes;
    }

    /**
     * Checks if a recipe satisfies a single query
     * @param query String with single query (is not parsed for spaces), not case sensitive
     * @param recipe The recipe to check
     * @return boolean indicating if the recipe satisfies the String query
     */
    public boolean checkIfRecipeSatisfiesQuery(String query, Recipe recipe){
        List<RecipeIngredient> recipeIngredients = recipe.getIngredients();
        List<Ingredient> ingredients = recipeIngredients.stream()
                .map(x -> x.getIngredient()).toList();
        for(Ingredient ingredient : ingredients){
            if(ingredient.getName().toLowerCase().contains(query)){
                return true;
            }
        }

        List<String> instructions = recipe.getPreparationSteps();
        for(String instruction: instructions){
            if(instruction.toLowerCase().contains(query)){
                return true;
            }
        }

        if(recipe.getName().toLowerCase().contains(query)){
            return true;
        }

        return false;
    }
}
