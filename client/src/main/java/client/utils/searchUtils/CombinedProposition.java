package client.utils.searchUtils;

import java.util.ArrayList;

import commons.Recipe;

public class CombinedProposition implements Proposition{
    private SearchFunctions function;
    private ArrayList<Proposition> arguments;

    /**
     * Constructor...
     * @param function The SearchFunctions Enum entry associated with the function
     * @param arguments The propositions passed to the logical operator
     */
    public CombinedProposition(SearchFunctions function, ArrayList<Proposition> arguments) {
        this.function = function;
        this.arguments = arguments;
    }

    /**
     * Checks if the logical proposition holds for a given recipe
     * @param recipe
     * @return boolean representing if the proposition holds.
     */
    public boolean evaluate(Recipe recipe){
        ArrayList<Object> objectArgs = new ArrayList<>(arguments.stream().map(x -> (Object) x).toList());
        return this.function.evaluate.apply(recipe, objectArgs);
    }

    /**
     * toString method...
     * @return String
     */
    public String toString(){
        return "{" + this.function.name + ", " + this.arguments.toString() + "}";
    }
}
