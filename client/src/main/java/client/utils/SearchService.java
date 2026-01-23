package client.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import client.utils.searchUtils.AtomicProposition;
import client.utils.searchUtils.CombinedProposition;
import client.utils.searchUtils.Proposition;
import client.utils.searchUtils.SearchFunctions;
import commons.Recipe;
import commons.RecipeIngredient;
import commons.Ingredient;

public class SearchService {
    private boolean favToggle;

    public void setFavToggle(boolean favToggle) {
        this.favToggle = favToggle;
    }

    /**
     * Performs a query on the provided List of recipes
     * @param query A String containing words recipes must contain, separated by spaces (case-insensitive)
     * @param recipes A list of all recipes in which to search
     * @return A List of all recipes that satisfy the search conditions
     */
    public List<Recipe> performSimpleQuery(String query, List<Recipe> recipes){
        query = query.toLowerCase();
        List<String> queries = List.of(query.split(" "));
        ArrayList<Recipe> filteredRecipes = new ArrayList<>();
        for(Recipe recipe : recipes){
            //check if this recipe satisfies all conditions (i.e. contains all keywords)
            if(queries.stream().allMatch(x -> checkIfRecipeSatisfiesQuery(x, recipe))){
                filteredRecipes.add(recipe);
            }

        }
        return filteredRecipes;
    }

    /**
     * Checks if a recipe satisfies a single query
     * @param query String with single query (is not parsed for spaces), not case-sensitive
     * @param recipe The recipe to check
     * @return boolean indicating if the recipe satisfies the String query
     */
    public boolean checkIfRecipeSatisfiesQuery(String query, Recipe recipe){
        //check for ingredients, instructions and the recipe name
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

        return recipe.getName().toLowerCase().contains(query);
    }

    /**
     * Recursively parses functions and parameters into the proper objects.
     * @param input String (will be stripped of whitespace) to evaluate
     * @return a Proposition that can be evaluated on any Recipe.
     */
    public Proposition parseComplexQuery(String input) throws IllegalArgumentException {
        Scanner scanner = new Scanner(input.strip().replaceAll(" ", "").toUpperCase());
        Proposition result = null;
        if(!scanner.hasNext("/.*")){
            //interpret the next partial String k as HAS(k):
            result = new AtomicProposition(SearchFunctions.HAS,
                    new ArrayList<String>(List.of(scanner.next()))
            );
        } else {
            //parse the function
            scanner.skip("/");
            scanner.useDelimiter("\\("); //read until the opening parenthesis
            String functionName = scanner.next();
            SearchFunctions function;
            try {
                function = SearchFunctions.valueOf(functionName);
            } catch (Exception e){
                scanner.close();
                throw new IllegalArgumentException();
            }
            if(function.isAtomic){
                //if the function is atomic, extract the arguments by splitting on commas
                scanner.useDelimiter("\\)"); //read until the closing parenthesis
                ArrayList<String> arguments = new ArrayList<>(List.of(scanner.next().replace("(", "").split(",")));
                result = new AtomicProposition(function, arguments);
            } else {
                //parse combined function
                scanner.useDelimiter("");
                int opening = 0;
                int closing = 0;
                String argumentList = "";
                //put everything from the opening parenthesis until the corresponding closing parenthisis in argumentList
                do {
                    String nextChar = scanner.next();
                    switch(nextChar){
                        case "(" -> opening++;
                        case ")" -> closing++;
                    }
                    argumentList += nextChar;
                } while (opening != closing);
                //recursively parse the arguments
                ArrayList<Proposition> arguments = parsePropositionalArguments(argumentList);
                result = new CombinedProposition(function, arguments);
            }
        }
        scanner.close();
        return result;
    }

    /**
     * Parses the arguments for combined propositions
     * @param args String containing args, starting with opening ( and closing ), arguments are comma separated
     * @return List of propositions
     */
    public ArrayList<Proposition> parsePropositionalArguments(String args){
        args = args.substring(0, args.length() - 1) + ",";
        Scanner scanner = new Scanner(args.replaceFirst("\\(", ""));
        scanner.useDelimiter("");
        ArrayList<Proposition> result = new ArrayList<>();
        while(scanner.hasNext()){
            //loop through all arguments
            int opening = 0;
            int closing = 0;
            scanner.useDelimiter("");
            String propositionText = "";
            while(!(opening == closing && scanner.hasNext(",.*"))){
                String nextChar = scanner.next();
                propositionText += nextChar;
                switch(nextChar){
                    case "(" -> opening++;
                    case ")" -> closing++;
                }
            }
            result.add(parseComplexQuery(propositionText));
            scanner.skip(",");
        }
        scanner.close();
        return result;
    }

    /**
     * Applies a complex query on a List of recipes
     * @param query The complex query, without the asterisk at the start
     * @param recipes The recipes to search through
     * @return List of recipes satisfying the search conditions
     */
    public List<Recipe> performComplexQuery(String query, List<Recipe> recipes){
        Proposition prop = parseComplexQuery(query);
        return recipes.stream().filter(x -> prop.evaluate(x)).toList();
    }

    /**
     * Applies a complex query on a List of recipes
     * @param prop The proposition used to evaluate each recipe
     * @param recipes The recipes to search through
     * @return List of recipes satisfying the search conditions
     */
    public List<Recipe> performComplexQuery(Proposition prop, List<Recipe> recipes){
        return recipes.stream().filter(x -> prop.evaluate(x)).toList();
    }

    /**
     * Applies a query on a List of recipes, and first figures out if the query is complex
     * @param query The (complex) query, with an asterisk at the start to indicate a complex query
     * @param recipes The recipes to search through
     * @return List of recipes satisfying the search conditions
     */
    public List<Recipe> query(String query, List<Recipe> recipes){
        if(query.charAt(0) == '*'){
            return performComplexQuery(query.substring(1), recipes);
        }
        return performSimpleQuery(query, recipes);
    }


}
