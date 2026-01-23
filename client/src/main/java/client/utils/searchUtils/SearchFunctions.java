package client.utils.searchUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import commons.Recipe;
import commons.RecipeIngredient;

public enum SearchFunctions {
    AND     ("and", false, -1, new Class<?>[]{Proposition.class}, 
            (Recipe recipe, ArrayList<Object> arguments) -> {
                ArrayList<Proposition> props = mapToPropositionList(arguments); 
                return props.stream().allMatch(x -> x.evaluate(recipe));
            }),
    OR      ("or", false, -1, new Class<?>[]{Proposition.class},
            (recipe, arguments) -> {
                ArrayList<Proposition> props = mapToPropositionList(arguments); 
                return props.stream().anyMatch(x -> x.evaluate(recipe));
            }),
    MAXING  ("maxing", true, 2, new Class<?>[]{String.class, Double.class},
            (recipe, arguments) -> {
                String ingName = (String) arguments.get(0);
                Double amount = Double.parseDouble((String) arguments.get(1));
                List<RecipeIngredient> ingredients = recipe.getIngredients();
                return ingredients.stream().allMatch(x -> {
                    if(x.getIngredient().getName().toLowerCase().contains(ingName.toLowerCase())){
                        return amount >= x.getQuantity();
                    }
                    return true; //if the ingredient is not in the recipe, it satisfies this condition
                });  
            }),
    MINING  ("mining", true, 2, new Class<?>[]{String.class, Double.class},
            (recipe, arguments) -> {
                String ingName = (String) arguments.get(0);
                Double amount = Double.parseDouble((String) arguments.get(1));
                List<RecipeIngredient> ingredients = recipe.getIngredients();
                return ingredients.stream().anyMatch(x -> {
                    if(x.getIngredient().getName().toLowerCase().contains(ingName.toLowerCase())){
                        return amount <= x.getQuantity();
                    }
                    return false; //if the ingredient is not in the recipe to begin with, the recipe does not satisfy this condition.
                });
            }),
    MAXSTEPS("maxsteps", true, 1, new Class<?>[]{Integer.class},
            (recipe, arguments) -> {
                Integer amount = Integer.parseInt((String) arguments.get(0));
                return amount >= recipe.getPreparationSteps().size();
            }),
    MINSTEPS("minsteps", true, 1, new Class<?>[]{Integer.class},
            (recipe, arguments) -> {
                Integer amount = Integer.parseInt((String) arguments.get(0));
                return amount <= recipe.getPreparationSteps().size();
            }),
    NOTNAME ("notname", true, 1, new Class<?>[]{String.class},
            (recipe, arguments) -> {
                String name = (String) arguments.get(0);
                return !recipe.getName().toLowerCase().contains(name.toLowerCase());
            }),
    NOTING  ("noting", true, 1, new Class<?>[]{String.class},
            (recipe, arguments) -> {
                String ingName = (String) arguments.get(0);
                List<RecipeIngredient> ingredients = recipe.getIngredients();
                return ingredients.stream().allMatch(x -> !x.getIngredient().getName().toLowerCase().contains(ingName.toLowerCase()));
            }),
    NOTSTEP ("notstep", true, 1, new Class<?>[]{String.class},
            (recipe, arguments) -> {
                String stepName = (String) arguments.get(0);
                List<String> steps = recipe.getPreparationSteps();
                return steps.stream().allMatch(x -> !x.toLowerCase().contains(stepName.toLowerCase()));
            }),
    NOT     ("not", true, 1, new Class<?>[]{String.class},
            (recipe, arguments) -> {
                return NOTSTEP.evaluate.apply(recipe, arguments)
                        && NOTING.evaluate.apply(recipe, arguments)
                        && NOTNAME.evaluate.apply(recipe, arguments);

            }),
    HAS     ("has", true, 1, new Class<?>[]{String.class},
            (recipe, arguments) -> {
                return !NOT.evaluate.apply(recipe, arguments);
            }),
    HASSTEP ("hasstep", true, 1, new Class<?>[]{String.class},
            (recipe, arguments) -> {
                return !NOTSTEP.evaluate.apply(recipe, arguments);
            }),
    HASING  ("hasing", true, 1, new Class<?>[]{String.class},
            (recipe, arguments) -> {
                return !NOTING.evaluate.apply(recipe, arguments);
            }),
    HASNAME ("hasname", true, 1, new Class<?>[]{String.class},
            (recipe, arguments) -> {
                return !NOTNAME.evaluate.apply(recipe, arguments);
            }),
    HASLANG ("haslang", true, 1 , new Class<?>[]{String.class}, (recipe, arguments) -> {

        return recipe.getLanguage().equalsIgnoreCase((String) arguments.getFirst());
    });

    public final String name;
    public final boolean isAtomic;
    public final int amountOfArguments; //-1 means the function requires at least 1 argument.
    public final Class<?>[] paramTypes;
    public final BiFunction<Recipe, ArrayList<Object>, Boolean> evaluate;

    SearchFunctions(String name, boolean isAtomic, int amountOfArguments, Class<?>[] paramTypes, BiFunction<Recipe, ArrayList<Object>, Boolean> evaluate){
        this.name = name;
        this.isAtomic = isAtomic;
        this.amountOfArguments = amountOfArguments;
        this.paramTypes = paramTypes;
        this.evaluate = evaluate;
    }

    /**
     * Maps all objects in an ArrayList to a Proposition
     * @param input The ArrayList of Objects
     * @return An ArrayList of Propositions
     */
    public static ArrayList<Proposition> mapToPropositionList(ArrayList<Object> input){
        return new ArrayList<>(
                input.stream()
                .map(x -> (Proposition) x)
                .toList()
        );
    }

    /**
     * Retrieves all atomic entries of this enum
     * @return ArrayList of SearchFunctions
     */
    public static ArrayList<SearchFunctions> getAtomicFunctions(){
        return new ArrayList<>(List.of(SearchFunctions.values()).stream().filter(x -> x.isAtomic).toList());
    }

    /**
     * Retrieves all non-atomic entries of this enum
     * @return ArrayList of SearchFunctions
     */
    public static ArrayList<SearchFunctions> getCombinedFunctions(){
        return new ArrayList<>(List.of(SearchFunctions.values()).stream().filter(x -> !x.isAtomic).toList());
    }

}
