package client.utils;

import java.util.ArrayList;
import java.util.List;

import commons.Recipe;
import commons.RecipeIngredient;
import commons.Ingredient;

public class SearchCtrl {
    /**
     * Performs a query on the provided List of recipes
     * @param query For now a simple String the recipe has to contain
     * @param recipes A list of all recipes in which to search
     * @return A List of all recipes that satisfy the search condition
     */
    public List<Recipe> search(String query, List<Recipe> recipes){
        query = query.toLowerCase();
        ArrayList<Recipe> filteredRecipes = new ArrayList<>();

        for(Recipe recipe : recipes){
            List<RecipeIngredient> recipeIngredients = recipe.getIngredients();
            List<Ingredient> ingredients = recipeIngredients.stream()
                    .map(x -> x.getIngredient()).toList();
            System.out.println(ingredients.toString());
            for(Ingredient ingredient : ingredients){
                if(ingredient.getName().toLowerCase().contains(query)){
                    filteredRecipes.add(recipe);
                    continue;
                }
            }

            List<String> instructions = recipe.getPreparationSteps();
            for(String instruction: instructions){
                if(instruction.toLowerCase().contains(query)){
                    filteredRecipes.add(recipe);
                    continue;
                }
            }

            if(recipe.getName().toLowerCase().contains(query)){
                filteredRecipes.add(recipe);
                continue;
            }
        }
        return filteredRecipes;
    }
}
